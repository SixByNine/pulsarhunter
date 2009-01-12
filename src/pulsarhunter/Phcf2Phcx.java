/*
 * Phcf2Phcx.java
 *
 * Created on 19 October 2007, 15:07
 *
 *
Copyright (C) 2005-2007 Michael Keith
 
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

package pulsarhunter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import pulsarhunter.datatypes.PulsarHunterCandidate;

/**
 *
 * @author mkeith
 */
public class Phcf2Phcx {
    
    public static void main(String[] args){
        
        for(String s : args){
            try {
                
                File file = new File(s);
                
                File outFile = null;
                
                if(s.endsWith(".phcf")){
                    outFile = new File(s.substring(0,s.length()-5)+".phcx.gz");
                } else if(s.endsWith(".phcf.gz")){
                    outFile = new File(s.substring(0,s.length()-7)+".phcx.gz");
                }
                
                if(outFile == null){
                    System.err.println("File "+s+" doesn't seem to be a phcf file!");
                } else {
                    System.out.println(s+" -> "+outFile.toString());
                    PulsarHunterCandidate phcf = new PulsarHunterCandidate(file);
                    phcf.read();
                    GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(outFile));
                    phcf.writeXML(out);
                    out.close();
                }
                
                
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
        
        
    }
    
}
