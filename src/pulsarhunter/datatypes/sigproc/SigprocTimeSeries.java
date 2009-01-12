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

import coordlib.Coordinate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel.MapMode;
import pulsarhunter.DataRecordType;
import pulsarhunter.PulsarHunter;
import coordlib.Telescope;
import java.lang.reflect.Method;
import pulsarhunter.datatypes.BulkReadable;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.WritableTimeSeries;
import pulsarhunter.recipies.RecipeParser;


/**
 *
 * @author mkeith
 */
public class SigprocTimeSeries  extends TimeSeries implements WritableTimeSeries,BulkReadable<TimeSeries.Header>{
    
    
    
    private File timFile;
    private SigprocTimeSeries.Header header;
    private int bufferSize;
    private FloatBuffer fb;
    //   private FileInputStream in = null;
    private RandomAccessFile raStream = null;
    
    
    private long currentFilePos = Long.MAX_VALUE;
    /** Creates a new instance of SigprocTimeSeries */
    public SigprocTimeSeries(File timFile,int bufferSize) throws IOException{
        
        bufferSize /= 4;
        this.timFile = timFile;
        this.header = new SigprocTimeSeries.Header(timFile);
        
        if(timFile.length() > 0){
            long fileSize = timFile.length()-header.getHeaderLength();
            // System.out.println("hl:"+header.getHeaderLength());
            if(bufferSize > fileSize/4){
                bufferSize = (int)fileSize/4;
            }
        }
        
        this.bufferSize = (int)(RecipeParser.getMem(bufferSize*4)/4);
        
    }
    
    public TimeSeries.Header getHeader() {
        return this.header;
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
                this.header.write(timFile);
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
                bb = raStream.getChannel().map(MapMode.READ_WRITE, ((long) header.getHeaderLength() + bin * 4), bufs * 4);
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
            
            if(reading && header.getHeaderLength()+bin*4+ this.bufferSize*4 > this.timFile.length()){
                addj = (int)((long)(header.getHeaderLength()+bin*4+ this.bufferSize*4) - this.timFile.length());
            }
            
            // System.out.println(this.timFile.getName()+">> ("+addj+")");
            //           int bufs = (int)(RecipeParser.getMem(bufferSize*4)/4);
            fb = null;
            bufs = (int)(RecipeParser.getMem(bufs*4)/4);
            ByteBuffer bb = null;
            try {
                
                bb = raStream.getChannel().map(MapMode.READ_WRITE, header.getHeaderLength() + bin * 4 - addj, bufs * 4);
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
            if(header.getHeaderLength()+bin*4+ this.bufferSize*4 > this.timFile.length()){
//                int bufs = (int)(RecipeParser.getMem(bufferSize*4)/4);
                addj = (int)((long)(header.getHeaderLength()+bin*4+ bufs*4) - this.timFile.length());
            }
            ByteBuffer bb = null;
            try {
                bb = raStream.getChannel().map(MapMode.READ_WRITE, header.getHeaderLength() + bin * 4 - addj, this.bufferSize * 4);
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
            
//            if(out != null){
//                out.close();
//                out = null;
//            }
//
//            if(in == null ){
//
//                in = new FileInputStream(this.timFile);
//
//                //  System.out.println(((long)header.getHeaderLength()+bin*4L)+"\t"+this.bufferSize*4+"\t"+timFile.length());
//                ByteBuffer bb = in.getChannel().map(MapMode.READ_ONLY,((long)header.getHeaderLength()+bin*4L),this.bufferSize*4);
//                bb.order(ByteOrder.nativeOrder());
//                fb = bb.asFloatBuffer();
//                currentFilePos = bin;
//                //System.out.println(fb.order().toString() + " Native "+ByteOrder.nativeOrder().toString());
//                //System.out.println("N");
//            }
//            if(bin >= currentFilePos+this.bufferSize){
//                //System.out.println(">>");
//                int addj = 0;
//                if(header.getHeaderLength()+bin*4+ this.bufferSize*4 > this.timFile.length()){
//                    addj = (int)((long)(header.getHeaderLength()+bin*4+ this.bufferSize*4) - this.timFile.length());
//                }
//                ByteBuffer bb = in.getChannel().map(MapMode.READ_ONLY,header.getHeaderLength()+bin*4-addj,this.bufferSize*4);
//                bb.order(ByteOrder.nativeOrder());
//                bb.position(addj);
//                fb = bb.asFloatBuffer();
//                currentFilePos = bin;
//
//            }
//
//            if(bin > currentFilePos+fb.position()){
//                //System.out.println(">");
//                while(bin != currentFilePos+fb.position()){
//                    fb.get();
//
//                }
//
//            }
//
//            if(bin < currentFilePos+fb.position()){
//                //System.out.println("<<");
//                in.close();
//                in = new FileInputStream(this.timFile);
//                // System.out.println(((long)header.getHeaderLength()+bin*4L)+"\t"+this.bufferSize*4+"\t"+timFile.length());
//                int addj = 0;
//                if(header.getHeaderLength()+bin*4+ this.bufferSize*4 > this.timFile.length()){
//                    addj = (int)((long)(header.getHeaderLength()+bin*4+ this.bufferSize*4) - this.timFile.length());
//                }
//                ByteBuffer bb = in.getChannel().map(MapMode.READ_ONLY,header.getHeaderLength()+bin*4-addj,this.bufferSize*4);
//                bb.order(ByteOrder.nativeOrder());
//                fb = bb.asFloatBuffer();
//                currentFilePos = bin;
//
//            }
//
            
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
    
    
    public SigprocHeader getSigprocHeader(){
        return header.getSigprocHeader();
    }
    
    public void copySigprocHeader(SigprocHeader h){
        this.header.copySigprocHeader(h);
    }
    
    
    private class Header extends TimeSeries.Header{
        
        
        
        private SigprocHeader header;
        private long npoints=0;
        
        private double bandwidth = 1;
        
        private Header(File file) throws IOException{
            super();
            header = new SigprocHeader(file);
            
        }
        
        
        public void write(File timfile) throws IOException{
            if(this.header.getNchans() < 1)this.header.setData_type(2); //Time series (apparently)
            if(this.header.getNchans() < 1)this.header.setNchans(1);
            this.header.setNbeams(1);
            this.header.setNbits(32);
            this.header.setIbeam(1);
            this.header.setNifs(1);
            if(this.header.getNchans() < 1)this.header.setFoff(bandwidth);
            this.header.write(timfile);
        }
        
        public double getTSamp() {
            return this.header.getTsamp();
        }
        
        public long getNPoints() {
            this.npoints = (timFile.length() - header.getHeaderLength())/header.getNbits()  * 8;
            return npoints;
        }
        
        public double getFrequency() {
            return this.header.getFch1();
        }
        
        public double getMjdStart() {
            return this.header.getTstart();
        }
        
        public String getSourceID() {
            return this.header.getSource_name();
        }
        
        public double getBandwidth() {
            
            return this.header.getFoff()*this.header.getNchans();
        }
        
        public double getTobs() {
            return this.getTSamp()*this.getNPoints();
        }
        
        public int getHeaderLength(){
            return header.getHeaderLength();
        }
        
        public Coordinate getCoord() {
            return header.getCoordinate();
        }
        
        public void setTelescope(Telescope telescope) {
            this.header.setTelescope(telescope);
        }
        
        public Telescope getTelescope() {
            return this.header.getTelescope();
        }
        
        public void setDm(double dm) {
            this.header.setRefdm(dm);
        }
        
        public double getDm() {
            return this.header.getRefdm();
        }
        
        public void setNumChannel(int numChannel) {
            this.header.setNchans(numChannel);
        }
        
        public int getNumChannel() {
            return this.header.getNchans();
        }
        
        
        SigprocHeader getSigprocHeader(){
            return header;
        }
        
        public void setSourceID(String sourceID) {
            
            this.header.setSource_name(sourceID);
        }
        
        public void setTobs(double obstime) {
            
        }
        
        public void setTSamp(double tSamp) {
            this.header.setTsamp(tSamp);
        }
        
        public void setMjdStart(double mjdStart) {
            this.header.setMjdobs(mjdStart);
            this.header.setTstart(mjdStart);
        }
        
        public void setFrequency(double frequency) {
            header.setFch1(frequency);
        }
        
        public void setBandwidth(double bandwidth) {
            this.bandwidth = bandwidth;
        }
        
        public void setCoord(Coordinate coord) {
            header.setCoordinate(coord);
        }
        
        public void setBarryCentered(boolean barryCentered) {
            if(barryCentered)header.setBarycentric(1);
            else header.setBarycentric(0);
        }
        
        public void setNPoints(long nPoints) {
            
        }
        
        public boolean isBarryCentered() {
            return header.getBarycentric()!=0;
        }
        
        public void copySigprocHeader(SigprocHeader h) {
            for(Method m : header.getClass().getDeclaredMethods()){
                if(m.getName().startsWith("get")){
                    Method setter = null;
                    try {
                        setter = header.getClass().getMethod(m.getName().replace("get", "set"), m.getReturnType());
                    } catch (SecurityException ex) {
                        
                    } catch (NoSuchMethodException ex) {
                        
                    }
                    if(setter!=null){
                        try {
                            setter.invoke(header,m.invoke(h));
                        } catch (IllegalArgumentException ex) {
                            
                        } catch (IllegalAccessException ex) {
                            
                        } catch (InvocationTargetException ex) {
                            
                        }
                    }
                }
            }
        }
        
    }
    
    public int getBufferSize() {
        return bufferSize;
    }
    
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    public void read(long startPosn, float[] data) throws IOException{
        FileInputStream stream = new FileInputStream(this.timFile);
        ByteBuffer bb = stream.getChannel().map(MapMode.READ_ONLY,(long)header.getHeaderLength()+startPosn*4L,(long)data.length*4L);
        bb.order(ByteOrder.nativeOrder());
        fb = bb.asFloatBuffer();
        fb.get(data);
        stream.close();
    }
    
    public void read(long startPosn, byte[] data) throws IOException {
        FileInputStream stream = new FileInputStream(this.timFile);
        ByteBuffer bb = stream.getChannel().map(MapMode.READ_ONLY,(long)header.getHeaderLength()+startPosn,(long)data.length);
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
