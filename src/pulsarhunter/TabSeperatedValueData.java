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
 * TabSeperatedValueData.java
 *
 * Created on October 26, 2006, 9:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author mkeith
 */
public class TabSeperatedValueData implements Data{
    
    private File file;
    private PulsarHunterTSVHeader header;
    private ArrayList data = new ArrayList();
    private Queue toBeWritten = new ArrayBlockingQueue(20);
    /** Old File*/
    public TabSeperatedValueData(File source) throws IOException {
        this.file = source;
        this.header = new PulsarHunterTSVHeader(source);
        
    }
    /** New File */
    public TabSeperatedValueData(File target,Data.Header header,int nrows) throws IOException{
        this.file = target;
        this.header = new PulsarHunterTSVHeader(header,nrows);
        PrintStream out = new PrintStream(new FileOutputStream(target));
        this.header.writeHeader(out);
        out.close();
        
    }
    
    private void read() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while(line != null){
            line = reader.readLine();
            int commentposn = line.indexOf("#");
            if(commentposn >= 0) line = line.substring(0,commentposn);
            if(line.trim().equals(""))continue;
            
            String[] elems = line.split("\\s+");
            
            if(elems.length != this.header.getNcols()){
//  TODO: Error handling?
                continue;
            }
            
            double[] row = new double[this.header.getNcols()];
            for(int i = 0; i < row.length; i++){
                row[i] = Double.parseDouble(elems[i]);
            }
            
            this.data.add(row);
        }
    }
    
    public void addRow(double[] row)throws IOException{
        if(row.length!=header.getNcols()) throw new RuntimeException("Wrong number of cols ("+header.getNcols()+") to write to file "+file.getName());
        else{
            this.data.add(row);
            boolean queued  = false;
            while(!queued){
                this.flush();
                queued = this.toBeWritten.offer(row);
            }
        }
    }
    
    public void addRows(double[][] rows)throws IOException{
        for(double[] row : rows){
            this.addRow(row);
        }
        
        this.flush();
        
    }
    
    public void write() throws IOException{
        PrintStream out = new PrintStream(new FileOutputStream(file,true));
        
        while(!this.toBeWritten.isEmpty()){
            double[] row = (double[])this.toBeWritten.poll();

                for(double d: row){
                    out.print(d+"\t");
                }
                out.println();

        }
        out.close();
    }
    
    public void flush() throws IOException{
        this.write();
    }
    public void release(){
        this.data = null;
        this.file = null;
        this.header = null;
        this.toBeWritten = null;
    }
    
    public Header getHeader() {
        return header;
    }

    public String getDataType() {
        return null;
    }
    
    public class PulsarHunterTSVHeader extends Data.Header{
        private int ncols;
        public PulsarHunterTSVHeader(Data.Header master, int nrows){
            super();
            this.setBandwidth(master.getBandwidth());
            this.setFrequency(master.getFrequency());
            this.setMjdStart(master.getMjdStart());
            this.setNrows(nrows);
            this.setTobs(master.getTobs());
            this.setSourceID(master.getSourceID());
            this.setCoord(master.getCoord());
            
        }
        public PulsarHunterTSVHeader(File source)throws IOException{
            BufferedReader reader = new BufferedReader(new FileReader(source));
            String line = reader.readLine();
            boolean headerEnd = false;
            
            while(line != null && !line.contains("##PH_TSV_HEADER_START##"))line = reader.readLine();
            if(line==null) throw new IOException("This is not a valid PulsarTSV file, no header present");
            String[] elems;
            line = reader.readLine();
            elems = line.split("=");
            this.setSourceID(elems[1]);
            
            line = reader.readLine();
            elems = line.split("=");
            double ra = Double.parseDouble(elems[1]);
            line = reader.readLine();
            elems = line.split("=");
            double dec= Double.parseDouble(elems[1]);
            this.setCoord(new Coordinate(new RA(ra),new Dec(dec)));
            
            line = reader.readLine();
            elems = line.split("=");
            this.setFrequency(Double.parseDouble(elems[1]));
            
            line = reader.readLine();
            elems = line.split("=");
            this.setMjdStart(Double.parseDouble(elems[1]));
            
            line = reader.readLine();
            elems = line.split("=");
            this.setTobs(Double.parseDouble(elems[1]));
            
            line = reader.readLine();
            elems = line.split("=");
            this.setNrows(Integer.parseInt(elems[1].trim()));
            
        }
        
        
        public void writeHeader(PrintStream out) throws IOException{
            out.println("##PH_TSV_HEADER_START##");
            out.println("#SourceID="+this.getSourceID());
            out.println("#RAJ="+this.getCoord().getRA().toDegrees());
            out.println("#DECJ="+this.getCoord().getDec().toDegrees());
            out.println("#FCH1="+this.getFrequency());
            out.println("#MJD="+this.getMjdStart());
            out.println("#TOBS="+this.getTobs());
            out.println("#NCOLS="+this.getNcols());
            out.println("##PH_TSV_HEADER_END##");
        }
        
        public int getNcols() {
            return ncols;
        }
        
        public void setNrows(int nrows) {
            this.ncols = nrows;
        }
        
    }
}
