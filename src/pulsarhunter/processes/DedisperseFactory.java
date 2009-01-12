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
 * DedisperseFactory.java
 *
 * Created on 14 February 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import java.util.Hashtable;
import pulsarhunter.Data;
import pulsarhunter.IncorrectDataTypeException;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.ValueList;
import pulsarhunter.datatypes.WritableTimeSeries;

/**
 *
 * @author mkeith
 */
public class DedisperseFactory implements ProcessFactory{
    
    /** Creates a new instance of DedisperseFactory */
    public DedisperseFactory() {
    }
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles, PulsarHunterRegistry reg) throws ProcessCreationException {
        
        if (params.length < 4) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        Data infile = dataFiles.get(params[1]);
        Data outfile = dataFiles.get(params[2]);
        
        if (!(infile instanceof MultiChannelTimeSeries))  throw new ProcessCreationException("First argument must be a MultiChannelTimeSeries file for process "+this.getName());
        
        if (outfile instanceof ValueList){
            
            ValueList vals = (ValueList)outfile;
            
            double[] dms = vals.getData();
            
            String rootoutputname = params[3];
            
            WritableTimeSeries[] outfiles = new WritableTimeSeries[dms.length];
            
            for(int i = 0; i < dms.length; i++){
                try {
                    outfiles[i] = (WritableTimeSeries)reg.getDataFactory("SIGPROCTIMESERIES").createData(rootoutputname+dms[i]+".tim");
                } catch (IncorrectDataTypeException ex) {
                    ex.printStackTrace();
                }
                dataFiles.put(rootoutputname+dms[i]+".tim",(Data)(outfiles[i]));
            }
            
            return new DedisperseFloat((MultiChannelTimeSeries)infile,outfiles,dms);
            
        } else {
            
            if (!(outfile instanceof WritableTimeSeries))  throw new ProcessCreationException("Seccond argument must be a WritableTimeSeries file for process "+this.getName());
            
            double dm1 = Double.parseDouble(params[3]);
            
            return new DedisperseFloat((MultiChannelTimeSeries)infile,new WritableTimeSeries[]{(WritableTimeSeries)outfile},new double[]{dm1});
        }
    }
    
    public String getName() {
        return "DEDISPERSE";
    }
    
}
