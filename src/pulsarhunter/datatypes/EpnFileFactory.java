/*
 * EpnFileFactory.java
 *
 * Created on July 22, 2007, 7:51 PM
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
public class EpnFileFactory implements DataFactory{
    
    /** Creates a new instance of EpnFileFactory */
    public EpnFileFactory() {
    }
    
    public Data createData(String filename) throws IncorrectDataTypeException {
        return new EpnFile(new File(filename)); 
    }
    
    public Data loadData(String filename, int buffersize) throws IncorrectDataTypeException {
        if(!filename.endsWith(".epn")) throw new IncorrectDataTypeException("EPN data file names must end with .epn");
        try {
            EpnFile epn = new EpnFile(new File(filename));
            epn.read();
            return epn;
        } catch (IOException ex) {
            throw new IncorrectDataTypeException("IOException trying to load file "+filename+" of type "+this.getName(),ex);
        }
        
    }
    
    public String getName() {
        return "EPN";
    }
    
}
