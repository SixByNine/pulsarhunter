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
 * OptimisedSuspectResultDataFactory.java
 *
 * Created on 02 November 2006, 11:47
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
public class PulsarHunterCandidateDataFactory implements DataFactory{
    
    /** Creates a new instance of OptimisedSuspectResultDataFactory */
    public PulsarHunterCandidateDataFactory() {
    }

    public Data loadData(String filename, int buf) throws IncorrectDataTypeException {
        PulsarHunterCandidate data = new PulsarHunterCandidate(new File(filename));
        try {
            data.read();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IncorrectDataTypeException("IOException trying to read osrf file "+filename+" Perhaps wrong file type",ex);
        }
        
        return data;
    }

    public Data createData(String filename) throws IncorrectDataTypeException {
        PulsarHunterCandidate data = new PulsarHunterCandidate(new File(filename));
        data.setReadOnDemand(false);
        return data;
    }

    public String getName() {
        return "PHCF";
    }
    
    
}
