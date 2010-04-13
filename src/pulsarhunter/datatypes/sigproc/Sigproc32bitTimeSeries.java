/*
Copyright (C) 2005-2007 Michael Keith, University Of Manchester
 
email: mkeith@pulsarastronomy.net
www  : www.pulsarastronomy.net/wiki/Software/PulsarHunter
 
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 
 */
/*
 * SigprocTimeSeries.java
 *
 * Created on 28 September 2006, 10:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes.sigproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel.MapMode;
import pulsarhunter.DataRecordType;
import pulsarhunter.PulsarHunter;
import pulsarhunter.datatypes.BulkReadable;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.WritableTimeSeries;
import pulsarhunter.recipies.RecipeParser;


/**
 *
 * @author mkeith
 */
public class Sigproc32bitTimeSeries  extends SigprocTimeSeries implements WritableTimeSeries,BulkReadable<TimeSeries.Header>{
    
    
    
    private int bufferSize;
    private FloatBuffer fb;
    //   private FileInputStream in = null;
    private RandomAccessFile raStream = null;
    
    
    private long currentFilePos = Long.MAX_VALUE;
    /** Creates a new instance of SigprocTimeSeries */
    public Sigproc32bitTimeSeries(File timFile,int bufferSize) throws IOException{
        super(timFile);
        bufferSize /= 4;

        
        if(timFile.length() > 0){
            long fileSize = timFile.length()-getSigprocHeader().getHeaderLength();
            // System.out.println("hl:"+header.getHeaderLength());
            if(bufferSize > fileSize/4){
                bufferSize = (int)fileSize/4;
            }
        }
        
        this.bufferSize = (int)(RecipeParser.getMem(bufferSize*4)/4);
        
    }
    
    
    public float[] getDataAsFloats() {
        return null;
    }
    
    public void writeBins(long startBin, float[] value,int srcStart, int nbins){
        
        try {
            checkBuffers(startBin,nbins-1180,false);
            
            
            if(startBin == currentFilePos+fb.position()){
                try{
                    fb.put(value,srcStart,nbins);
                    
                } catch(java.nio.BufferUnderflowException e){
                    System.out.println("Ran over end of file");
                    System.out.println("bin:"+startBin+"\ncfp:"+currentFilePos+"\npos:"+fb.position()+"\ncap:"+fb.capacity()+"\nbfs:"+this.bufferSize);
                    System.exit(-2);
                }
            } else{
                System.out.println("AN UNEXPECTED ERROR HAS OCCURED! ");
            }
            
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        
    }
    
    public void writeBin(long bin, float value){
        
        
        
        try {
            if(!this.getSigprocHeader().isWritten()){
                this.getSigprocHeader().write(timFile);
            }
            checkBuffers(bin,0,false);
            
            
            
            if(bin == currentFilePos+fb.position()){
                try{
                    fb.put(value);
                    
                } catch(java.nio.BufferUnderflowException e){
                    System.out.println("Ran over end of file");
                    System.out.println("bin:"+bin+"\ncfp:"+currentFilePos+"\npos:"+fb.position()+"\ncap:"+fb.capacity()+"\nbfs:"+this.bufferSize);
                    System.exit(-2);
                }
            }else {
                //    System.out.println("<");
                try{
                    fb.put((int)(bin-fb.position()),value);
                    
                } catch(java.nio.BufferUnderflowException e){
                    System.out.println("Ran over end of file");
                    System.out.println("bin:"+bin+"\ncfp:"+currentFilePos+"\npos:"+fb.position()+"\ncap:"+fb.capacity()+"\nbfs:"+this.bufferSize);
                    System.exit(-2);
                }
            }
//            if(raStream == null ){
//                raStream = new RandomAccessFile(this.timFile,"rw");
//            }
//
//            if(((long)header.getHeaderLength()+bin*4L) != raStream.getFilePointer()){
//                System.out.println("w->>");
//                raStream.seek(((long)header.getHeaderLength()+bin*4L));
//            }
//
//            raStream.writeFloat(value);
            
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println(this.timFile.getName());
            System.out.println("Bin:"+bin);
            ex.printStackTrace();
            System.exit(-1);
        }
        
        
    }
    
    private void checkBuffers(final long bin, int nbins, final boolean reading) throws FileNotFoundException, IOException {
        int bufs = bufferSize;
        
        if(nbins > bufs) throw new IOException("Trying to read/write more bins than the buffer size!");
        
        if(fb==null || raStream == null ){
            
            raStream = new RandomAccessFile(this.timFile,"rw");
            fb = null;
            bufs = (int)(RecipeParser.getMem(bufs*4)/4);
            ByteBuffer bb = null;
            try {
                bb = raStream.getChannel().map(MapMode.READ_WRITE, ((long) getSigprocHeader().getHeaderLength() + bin * 4), bufs * 4);
            } catch (IOException ex) {
                
                bufferSize/=2.0;
                
                ex.printStackTrace();
                PulsarHunter.out.println("Reduced Buffer Size... "+bufferSize);
                checkBuffers(bin,nbins,reading);
                return;
                
            }
            bb.order(ByteOrder.nativeOrder());
            
            fb = bb.asFloatBuffer();
            currentFilePos = bin;
            //System.out.println(fb.order().toString() + " Native "+ByteOrder.nativeOrder().toString());
            //  System.out.println("N");
        }
        if(bin + nbins >= currentFilePos+fb.capacity()){
            
            int addj = 0;
            
            if(reading && getSigprocHeader().getHeaderLength()+bin*4+ this.bufferSize*4 > this.timFile.length()){
                addj = (int)((long)(getSigprocHeader().getHeaderLength()+bin*4+ this.bufferSize*4) - this.timFile.length());
            }
            
            // System.out.println(this.timFile.getName()+">> ("+addj+")");
            //           int bufs = (int)(RecipeParser.getMem(bufferSize*4)/4);
            fb = null;
            bufs = (int)(RecipeParser.getMem(bufs*4)/4);
            ByteBuffer bb = null;
            try {
                
                bb = raStream.getChannel().map(MapMode.READ_WRITE, getSigprocHeader().getHeaderLength() + bin * 4 - addj, bufs * 4);
            } catch (IOException ex) {
                bufferSize/=2.0;
                ex.printStackTrace();
                PulsarHunter.out.println("Reduced Buffer Size... "+bufferSize);
                checkBuffers(bin,nbins,reading);
                return;
                
            }
            
            bb.order(ByteOrder.nativeOrder());
            bb.position(addj);
            fb = bb.asFloatBuffer();
            currentFilePos = bin;
            
        }
        if(nbins > 1 && bin == currentFilePos+fb.position()) return;
        
        if(bin > currentFilePos+fb.position()){
            //  System.out.println(">");
            while(bin != currentFilePos+fb.position()){
                fb.get();
                
            }
            
        }
        
        if(bin < fb.position()){
            
            
            //System.out.println("<<");
            raStream.close();
            raStream = new RandomAccessFile(this.timFile,"rw");
            
            // System.out.println(((long)header.getHeaderLength()+bin*4L)+"\t"+this.bufferSize*4+"\t"+timFile.length());
            int addj = 0;
            if(getSigprocHeader().getHeaderLength()+bin*4+ this.bufferSize*4 > this.timFile.length()){
//                int bufs = (int)(RecipeParser.getMem(bufferSize*4)/4);
                addj = (int)((long)(getSigprocHeader().getHeaderLength()+bin*4+ bufs*4) - this.timFile.length());
            }
            ByteBuffer bb = null;
            try {
                bb = raStream.getChannel().map(MapMode.READ_WRITE, getSigprocHeader().getHeaderLength() + bin * 4 - addj, this.bufferSize * 4);
            } catch (IOException ex) {
                bufferSize/=2.0;
                PulsarHunter.out.println("Reduced Buffer Size... "+bufferSize);
                checkBuffers(bin,nbins,reading);
                return;
            }
            
            bb.order(ByteOrder.nativeOrder());
            fb = bb.asFloatBuffer();
            currentFilePos = bin;
            
        }
    }
    
    
    public float getBin(long bin) {
        float f = -1.0f;
        
        try {
        
            
            this.checkBuffers(bin,0,true);
            
            //System.out.println("ts: " + bin);
            if(bin == currentFilePos+fb.position()){
                try{
                    f = fb.get();
                    
                } catch(java.nio.BufferUnderflowException e){
                    System.out.println("Ran over end of file");
                    System.out.println("bin:"+bin+"\ncfp:"+currentFilePos+"\npos:"+fb.position()+"\ncap:"+fb.capacity()+"\nbfs:"+this.bufferSize);
                    System.exit(-2);
                }
            }else {
                // System.out.println("<.");
                try{
                    f = fb.get((int)(bin-fb.position()));
                    
                    
                } catch(java.nio.BufferUnderflowException e){
                    System.out.println("Ran over end of file");
                    System.out.println("bin:"+bin+"\ncfp:"+currentFilePos+"\npos:"+fb.position()+"\ncap:"+fb.capacity()+"\nbfs:"+this.bufferSize);
                    System.exit(-2);
                }
            }
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println(this.timFile.getName());
            System.out.println("Bin:"+bin);
            ex.printStackTrace();
            System.exit(-1);
        }
        return f;
    }
    
    public void flush() throws IOException {
        
    }
    
    public void release() {
        try {
            if(this.raStream!=null)this.raStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
        this.header = null;
        this.fb = null;
        
        this.raStream = null;
        this.timFile = null;
    }
    
    
    
    
    public int getBufferSize() {
        return bufferSize;
    }
    
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    public void read(long startPosn, float[] data) throws IOException{
        FileInputStream stream = new FileInputStream(this.timFile);
        ByteBuffer bb = stream.getChannel().map(MapMode.READ_ONLY,(long)getSigprocHeader().getHeaderLength()+startPosn*4L,(long)data.length*4L);
        bb.order(ByteOrder.nativeOrder());
        fb = bb.asFloatBuffer();
        fb.get(data);
        stream.close();
    }
    
    public void read(long startPosn, byte[] data) throws IOException {
        FileInputStream stream = new FileInputStream(this.timFile);
        ByteBuffer bb = stream.getChannel().map(MapMode.READ_ONLY,(long)getSigprocHeader().getHeaderLength()+startPosn,(long)data.length);
        bb.order(ByteOrder.nativeOrder());
        bb.get(data);
        stream.close();
    }
    
    public void read(long startPosn, long[] data)  throws IOException{
    }
    
    public void read(long startPosn, double[] data)  throws IOException{
    }
    
    public void read(long startPosn, short[] data)  throws IOException{
    }
    
    public void read(long startPosn, int[] data) throws IOException {
    }
    
    public BulkReadable getBulkReadableInterface() {
        if(this.getHeader().getNPoints() < Integer.MAX_VALUE){
            return this;
        } else{
            return null;
        }
    }
    
    public DataRecordType getDataRecordType() {
        return DataRecordType.FLOAT_FLOAT;
    }
    
    public String getDataType() {
        return "SIGPROCTIMESERIES";
    }
    
}
