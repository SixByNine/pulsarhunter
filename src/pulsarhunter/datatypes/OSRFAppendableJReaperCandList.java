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
 * AppendableJReaperCandList.java
 *
 * Created on 04 November 2006, 11:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import coordlib.Beam;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import pulsarhunter.Data;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.CandList;
import pulsarhunter.jreaper.CandListHeader;
import pulsarhunter.jreaper.Score;
import pulsarhunter.jreaper.peckscorer.PeckScorer;

/**
 *
 * @author mkeith
 */
public class OSRFAppendableJReaperCandList implements Data<CandListHeader> {
    private CandListHeader header = null;
    private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(15);
    private File file;
    private CandList cl= null;
    
    /** Creates a new instance of AppendableJReaperCandList */
    public OSRFAppendableJReaperCandList(File file) throws IOException {
        this.file = file;
        
        if(file.exists()){
            cl = new CandList(new BufferedReader(new FileReader(file)));
            this.header = cl.getHeader();
            
        } else {
            file.createNewFile();
        }
    }
    
    public String getDataType() {
        return null;
    }
    
    private PrintStream getPrintStream()throws IOException{
        return new PrintStream(new FileOutputStream(file,true));
    }
    
    public void append(PulsarHunterCandidate osrf, int type,String resultsDir,String beamID)throws IOException{
        if(header == null){
            if(beamID == null)beamID = osrf.getHeader().getSourceID();
            
            header = new CandListHeader();
            header.setBandwidth(osrf.getHeader().getBandwidth());
            header.setFrequency(osrf.getHeader().getFrequency());
            header.setBeam(new Beam(beamID,osrf.getHeader().getCoord()));
            
            
            header.setName(beamID);
            header.setTelescope(osrf.getHeader().getTelescope());
            header.setTobs(osrf.getHeader().getTobs());
            PrintStream out = this.getPrintStream();
            out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
            out.println("<CandList>");
            header.write(out);
            
            
            out.println("<CLBody version=\""+CandList.VERSION+"\">");
            
        }
        if(cl==null) cl = new CandList(header);
        
        Cand c = osrf.extractJReaperCand(resultsDir);
        c.setCandList(cl);
        Score score = new PeckScorer().score(c);
        c.setScore(score);
        String s = CandList.candToString(c,type);
        while(!queue.offer(s)){
            this.flush();
        }
        
        
        
    }
    
    public void write() throws IOException{
        PrintStream out = this.getPrintStream();
        while(!queue.isEmpty()){
            String s = queue.poll();
            out.println(s);
        }
    }
    
    public void release() {
        this.file = null;
        this.queue = null;
    }
    
    public CandListHeader getHeader() {
        return header;
    }
    
    public void flush() throws IOException {
        this.write();
    }
    
}
