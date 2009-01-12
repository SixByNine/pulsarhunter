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
 * PolyCoFileDataFactory.java
 *
 * Created on 15 March 2007, 16:35
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import pulsarhunter.Data;
import pulsarhunter.DataFactory;
import pulsarhunter.IncorrectDataTypeException;

/**
 *
 * @author mkeith
 */
public class PolyCoFileDataFactory implements DataFactory{

    /** Creates a new instance of PolyCoFileDataFactory */
    public PolyCoFileDataFactory() {
    }

    public Data createData(String filename) throws IncorrectDataTypeException {
        throw new IncorrectDataTypeException("Cannot create PolyCo with pulsarhunter, use tempo!");
    }

    public Data loadData(String filename, int buffersize) throws IncorrectDataTypeException {
        PolyCoFile pcf = new PolyCoFile();
         try {
            pcf.read(new BufferedReader(new FileReader(new File(filename))));
        } catch (FileNotFoundException ex) {
            throw new IncorrectDataTypeException("Polyco file "+filename+" does not exist!",ex);
        } catch (Exception ex) {
            throw new IncorrectDataTypeException("Errror trying to read polyco file "+filename+" Perhaps wrong file type",ex);
        }
        return pcf;
    }

    public String getName() {
        return "POLYCO";
    }
    
}
