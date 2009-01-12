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
 * DisplayZapFileFactory.java
 *
 * Created on 27 February 2007, 13:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import java.util.Hashtable;
import pulsarhunter.Data;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.datatypes.ZapFile;

/**
 *
 * @author mkeith
 */
public class DisplayZapFileFactory implements ProcessFactory{
    
    /** Creates a new instance of DisplayZapFileFactory */
    public DisplayZapFileFactory() {
    }
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles, PulsarHunterRegistry reg) throws ProcessCreationException {
        if (params.length < 2) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        
        Data dat = dataFiles.get(params[1]);
        if(! (dat instanceof ZapFile))  throw new ProcessCreationException("Argument 1 must be a ZapFile for process "+this.getName());
        ZapFile zapFile = (ZapFile)dat;
        
        return new DisplayZapFile(zapFile);
    }
    
    public String getName() {
        return "DISPLAYZAPFILE";
    }
    
}
