/*
 * MultiProfilePlotterFactory.java
 *
 * Created on July 30, 2007, 6:27 PM
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
import pulsarhunter.datatypes.MultiprofileFile;

/**
 *
 * @author mkeith
 */
public class MultiProfilePlotterFactory implements ProcessFactory{
    
    /** Creates a new instance of MultiProfilePlotterFactory */
    public MultiProfilePlotterFactory() {
    }
    
    
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles, PulsarHunterRegistry reg) throws ProcessCreationException {
        
        if (params.length < 2) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        Data inFile = dataFiles.get(params[1]);
        
        if(inFile instanceof MultiprofileFile){
            
            
            return new MultiProfilePlotter((MultiprofileFile)inFile);
            
        } else {
            throw new ProcessCreationException("Argument 1 must be a multiprofile file for "+this.getName());
        }
        
        
        
    }
    
    
    
    
    public String getName() {
        return "PLOTPROFILES";
    }
}
