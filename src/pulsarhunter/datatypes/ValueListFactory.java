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
 * ValueListFactory.java
 *
 * Created on 14 February 2007, 15:47
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
public class ValueListFactory implements DataFactory{
    
    /** Creates a new instance of ValueListFactory */
    public ValueListFactory() {
    }
    
    public Data createData(String filename) throws IncorrectDataTypeException {
        throw new IncorrectDataTypeException("createData not supported on "+this.getName());
    }
    
    public Data loadData(String filename, int buffersize) throws IncorrectDataTypeException {
        if(!filename.endsWith(".list"))        throw new IncorrectDataTypeException("ValueList data files must end with .list");
        try {
            return new ValueList(new File(filename));
        } catch (IOException ex) {
            throw new IncorrectDataTypeException("IOException trying to load file "+filename+" of type "+this.getName(),ex);
        }
    }
    
    public String getName() {
        return "VALUELIST";
    }
    
    
    
}
