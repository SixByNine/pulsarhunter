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
 * OSRFAppendableJReaperCandListFactory.java
 *
 * Created on 04 November 2006, 12:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.File;
import java.io.IOException;
import pulsarhunter.Data;
import pulsarhunter.DataFactory;
import pulsarhunter.IncorrectDataTypeException;

/**
 *
 * @author mkeith
 */
public class OSRFAppendableJReaperCandListFactory implements DataFactory{
    
    /** Creates a new instance of OSRFAppendableJReaperCandListFactory */
    public OSRFAppendableJReaperCandListFactory() {
    }
    
    public Data loadData(String filename,int buf) throws IncorrectDataTypeException {
        if(!filename.endsWith(".clist"))throw new IncorrectDataTypeException("JReaper candlist must end with .clist");
        File file = new File(filename);
        //if(!file.exists()) throw new IncorrectDataTypeException("Cannot load candlist file "+filename+" as it does not exist");
        OSRFAppendableJReaperCandList data;
        try {
            data = new OSRFAppendableJReaperCandList(file);
        } catch (Exception ex) {
            throw new IncorrectDataTypeException("File "+filename+" does not appear to be a JReaper candlist",ex);
        }
        return data;
    }
    
    public Data createData(String filename) throws IncorrectDataTypeException {
        File file = new File(filename);
        file.delete();
        OSRFAppendableJReaperCandList data;
        try {
            data = new OSRFAppendableJReaperCandList(file);
        } catch (IOException ex) {
            throw new IncorrectDataTypeException("Cannot create file "+filename+" An IO error occured on creation, does the path exist?",ex);
        }
        return data;
    }
    
    public String getName() {
        return "PHCFCANDLIST";
    }
    
}
