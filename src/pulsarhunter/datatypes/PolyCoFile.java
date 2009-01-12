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
 * PolyCoFile.java
 *
 * Created on 15 March 2007, 16:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import pulsarhunter.Data;

/**
 *
 * @author mkeith
 */
public class PolyCoFile implements Data{
    
    Hashtable<String,PulsarPolyco> pulsars = new Hashtable<String,PulsarPolyco>();
    
    /** Creates a new instance of PolyCoFile */
    public PolyCoFile() {
        
    }
    
    public String getDataType() {
return "POLYCO";
    }
    
    public void read(BufferedReader in) throws IOException{
        String line = in.readLine().trim();
        int j = 0;
        while(line!=null){
            line = line.trim();
            
            String[] elems = line.split("\\s+");
            String name = elems[0];
            String date = elems[1];
            // What is elems[2]??? time?
            
            double mjdmid = Double.parseDouble(elems[3]);
            double dm = Double.parseDouble(elems[4]);
            double z4 = Double.parseDouble(elems[5]);
            // elems[6] = rms apparently, but it's not used...
            line = in.readLine().trim();
            elems = line.split("\\s+",7);
            
            double rphase = Double.parseDouble(elems[0]);
            double f0 = Double.parseDouble(elems[1]);
            int jobs = Integer.parseInt(elems[2]);
            
            int blocklength = Integer.parseInt(elems[3]);
            int ncoeff = Integer.parseInt(elems[4]);
            double obsfrq = Double.parseDouble(elems[5]);
            String binphase = "";
            if(elems.length > 6){
             binphase = elems[6];
            }
            line = in.readLine().trim();
            elems = line.split("\\s+");
            int arrPos = 0;
            double[] coeff = new double[ncoeff];
            
            for(int k=0;k<ncoeff;k++) {
                
                if(arrPos >= elems.length){
                    line = in.readLine().trim();
                    elems = line.split("\\s+");
                    arrPos = 0;
                }
                
                String dummy  = elems[arrPos];
                dummy = dummy.replace('D','e');
                
                coeff[k] = Double.parseDouble(dummy);
                arrPos++;
            }
            
            if(mjdmid  < 20000) mjdmid += 39126.;
            
            
            
            PulsarPolyco pulsarPolyco = this.pulsars.get(name);
            if(pulsarPolyco == null){
                pulsarPolyco = new PulsarPolyco(name,dm);
                this.pulsars.put(name,pulsarPolyco);
            }
            //public PolyCoBlock(String psrName, double mjdmid, double[] coeff, double z4, double rphase, double f0, double obsfrq, int blocklengthh) {
            pulsarPolyco.addBlock(new PolyCoBlock(name,mjdmid,coeff,z4,rphase,f0,obsfrq,blocklength));
            
            line = in.readLine();
            j++;
        }
    }
    
    public PulsarPolyco getPolyCo(String name){
        PulsarPolyco ppc = this.pulsars.get(name);
        if(ppc==null){
            if(!Character.isDigit(name.charAt(0)) && name.length()<1){
                ppc= getPolyCo(name.substring(1));
            }
        }
        return ppc;
    }
    
    public void release() {
        this.pulsars = null;
        return;
    }
    
    public Header getHeader() {
        return null;
    }
    
    public void flush() throws IOException {
        return;
    }
    
}
