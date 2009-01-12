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
 * AppendOsrfToJReaperCandListProcessFactory.java
 *
 * Created on 04 November 2006, 12:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import java.util.Hashtable;
import pulsarhunter.Data;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.datatypes.OSRFAppendableJReaperCandList;
import pulsarhunter.datatypes.PulsarHunterCandidate;

/**
 *
 * @author mkeith
 */
public class AppendOsrfToJReaperCandListProcessFactory implements ProcessFactory{
    
    /** Creates a new instance of AppendOsrfToJReaperCandListProcessFactory */
    public AppendOsrfToJReaperCandListProcessFactory() {
    }
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles,PulsarHunterRegistry reg) throws ProcessCreationException {
        
        
        PulsarHunterProcess result = null;
        int type = 3; // default == unknown
        String resultsDir = null;
        String beamID = null;
        if (params.length < 3) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        Data infile = dataFiles.get(params[1]);
        Data outfile = dataFiles.get(params[2]);
        
        for(int i = 3; i < params.length; i++){
            if(params[i].trim().equalsIgnoreCase("-resdir")){
                resultsDir = params[++i];
            }
            
            if(params[i].trim().equalsIgnoreCase("-searchtype")){
                type = Integer.parseInt(params[++i].trim());
            }
            
            if(params[i].trim().equalsIgnoreCase("-beamid")){
                beamID = params[++i].trim();
            }
        }
        
        
        
//        if(params.length > 3){
//            type = Integer.parseInt(params[3]);
//        }
//        if(params.length > 4){
//            resultsDir = params[4];
//        }
//        if(params.length > 5){
//            beamID = params[5];
//        }
        if(infile instanceof PulsarHunterCandidate){
            if(outfile instanceof OSRFAppendableJReaperCandList){
                
                result = new AppendOsrfToJReaperCandListProcess((PulsarHunterCandidate)infile,(OSRFAppendableJReaperCandList)outfile,type,resultsDir,beamID);
                
            } else throw new  ProcessCreationException("Argument 2 for PHCF2CANDLIST must be a PHCFCANDLIST file");
            
            
        }else throw new ProcessCreationException("Argument 1 for PHCF2CANDLIST must be a PHCF file");
        
        
        
        return result;
    }
    
    public String getName() {
        return "PHCF2CANDLIST";
    }
    
}
