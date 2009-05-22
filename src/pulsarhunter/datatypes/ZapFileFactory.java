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
 * ZapFileFactory.java
 *
 * Created on 27 February 2007, 10:01
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
public class ZapFileFactory implements DataFactory {
    
    /** Creates a new instance of ZapFileFactory */
    public ZapFileFactory() {
    }
    
    
    
    public Data loadData(String filename, int buf) throws IncorrectDataTypeException {
        ZapFile zapfile = null;
        try {
            File f = new File(filename);
            zapfile = new ZapFile(f);
            zapfile.read(f);
        } catch (Exception ex) {
            throw new IncorrectDataTypeException("Cannot read zapfile "+filename,ex);
        }
        return zapfile;
        
    }
    
    public Data createData(String filename) throws IncorrectDataTypeException {
        ZapFile zapfile = null;
        try {
            File f = new File(filename);
            zapfile = new ZapFile(f);
            zapfile.write(f);
        } catch (IOException ex) {
            throw new IncorrectDataTypeException("Cannot create zapfile "+filename,ex);
        }
        return zapfile;
    }
    
    public String getName() {
        return "ZAPFILE";
    }
    
}
