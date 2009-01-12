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
 * DiplayPHCFFactory.java
 *
 * Created on 25 January 2007, 17:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import java.util.Hashtable;
import pulsarhunter.Data;
import pulsarhunter.GlobalOptions;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.datatypes.PulsarHunterCandidate;

/**
 *
 * @author mkeith
 */
public class DisplayPHCFFactory implements  ProcessFactory{
    
    /** Creates a new instance of DiplayPHCFFactory */
    public DisplayPHCFFactory() {
    }
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles, PulsarHunterRegistry reg) throws ProcessCreationException {
        
        if (params.length < 2) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        
        Data dat = dataFiles.get(params[1]);
        if(! (dat instanceof PulsarHunterCandidate))  throw new ProcessCreationException("Argument 1 must be a PulsarHunterCandidate for process "+this.getName());
        PulsarHunterCandidate phcf = (PulsarHunterCandidate)dat;
        boolean makeImage = false;
        Object opt = reg.getOptions().getArg(GlobalOptions.Option.imageoutput);
        if(opt!=null){
            makeImage = ((Boolean)opt).booleanValue();
        }
        
        return new DisplayPHCF(phcf,makeImage);
        
    }
    
    public String getName() {
        return "PHCFPlotter";
    }
    
}
