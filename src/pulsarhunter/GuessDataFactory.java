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
 * GuessDataFactory.java
 *
 * Created on 25 October 2006, 11:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

import java.io.File;



/**
 *
 * @author mkeith
 */
public class GuessDataFactory implements DataFactory{
    
    private PulsarHunterRegistry registry;
    
    /** Creates a new instance of GuessDataFactory */
    public GuessDataFactory(PulsarHunterRegistry registry) {
        this.registry = registry;
    }
    
    public Data loadData(String filename,int buf) throws IncorrectDataTypeException{
        Data result = null;
        PulsarHunter.out.print("Guessing data type for "+filename+"... ");
        if(!new File(filename).exists()){
            throw new IncorrectDataTypeException("File "+filename+" Does not exist!");
        }
        for(String name : this.registry.getDataFactoryList()){
            if(name.equals(this.getName())) continue;
            try{
                System.out.print("Trying: "+name);
                result = this.registry.getDataFactory(name).loadData(filename,buf);
            } catch (IncorrectDataTypeException e){
                System.out.println("FAIL");
                result = null;
                e.printStackTrace();
                continue;
            }
//            System.out.println("OK");
            // If we are here then we managed to parse the data file!
            PulsarHunter.out.println(" Importing file as "+name);
            break;
        }
        if(result == null)throw new IncorrectDataTypeException("Cannot find a DataFactory to parse the data in file"+filename);
        return result;
    }
    
    
    
    
    public String getName() {
        return "Guess";
    }

    public Data createData(String filename) throws IncorrectDataTypeException {
        throw new IncorrectDataTypeException("GuessDataFactory: Cannot create new file for unknown data type!");
    }
    
}
