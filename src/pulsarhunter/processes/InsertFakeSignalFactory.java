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
 * FilterCandidatesFactory.java
 *
 * Created on 15 January 2007, 16:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import java.util.Hashtable;
import pulsarhunter.GlobalOptions.Option;
import pulsarhunter.Data;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.VariableTimeModel;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.WritableMultiChannelTimeSeries;
import pulsarhunter.datatypes.WritableTimeSeries;
import pulsarhunter.processes.folding.AccelJerkTimeModel;

/**
 *
 * @author mkeith
 */
public class InsertFakeSignalFactory implements ProcessFactory {
    
    /** Creates a new instance of FilterCandidatesFactory */
    public InsertFakeSignalFactory() {
    }
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles, PulsarHunterRegistry reg) throws ProcessCreationException {
        
        if (params.length < 2) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        
        
        
        double accel = 0;
        double jerk = 0;
        
        if (reg.getOptions().getArg(Option.accn)!=null){
            accel = (Double)reg.getOptions().getArg(Option.accn);
        }
        
        if (reg.getOptions().getArg(Option.jerk)!=null){
            jerk = (Double)reg.getOptions().getArg(Option.jerk);
        }
        
        VariableTimeModel foldingModel = new AccelJerkTimeModel(accel,jerk);
        
        InsertFakeSignal  proc = null;
        
        Data inFile = dataFiles.get(params[1]);
        Data outFile = dataFiles.get(params[2]);
        if((inFile instanceof TimeSeries)){
            if(! (outFile instanceof WritableTimeSeries))  throw new ProcessCreationException("Argument 2 must be a WritableTimeSeries for process "+this.getName());
            
            proc = new InsertFakeSignal((TimeSeries)inFile,(WritableTimeSeries)outFile,foldingModel);
            
        } else if((inFile instanceof MultiChannelTimeSeries)){
            if(! (outFile instanceof WritableMultiChannelTimeSeries))  throw new ProcessCreationException("Argument 2 must be a WritableTimeSeries for process "+this.getName());
            
            proc = new InsertFakeMultiChannelSignal((MultiChannelTimeSeries)inFile,(WritableMultiChannelTimeSeries)outFile,foldingModel);
            
            
            
            if (reg.getOptions().getArg(Option.dm)!=null){
                ((InsertFakeMultiChannelSignal)proc).setDm((Double)reg.getOptions().getArg(Option.dm));
            }
            
            
        } else throw new ProcessCreationException("Argument 1 must be a TimeSeries for process "+this.getName());
        
        
        
        
        
        if (reg.getOptions().getArg(Option.period)!=null){
            proc.setPeriod((Double)reg.getOptions().getArg(Option.period)/1000.0);
        }
        
        if (reg.getOptions().getArg(Option.fakesnr)!=null){
            proc.setSnr((Double)reg.getOptions().getArg(Option.fakesnr));
        }
        
        if (reg.getOptions().getArg(Option.pulsewidth)!=null){
            proc.setPulseWidth((Double)reg.getOptions().getArg(Option.pulsewidth));
        }
        
        if (reg.getOptions().getArg(Option.fakenulling)!=null){
            proc.setNulling((Boolean)reg.getOptions().getArg(Option.fakenulling));
        }
        
        if (reg.getOptions().getArg(Option.fakenulltimescale)!=null){
            proc.setNulltimescale((Double)reg.getOptions().getArg(Option.fakenulltimescale));
        }
        
        if (reg.getOptions().getArg(Option.fakepulsetimescale)!=null){
            proc.setPulsetimescale((Double)reg.getOptions().getArg(Option.fakepulsetimescale));
        }
        
        if (reg.getOptions().getArg(Option.fakenullvariance)!=null){
            proc.setNullvariance((Double)reg.getOptions().getArg(Option.fakenullvariance));
        }
        if (reg.getOptions().getArg(Option.fakenullstatistics)!=null){
            try {
                InsertFakeSignal.VarianceFunction vf = InsertFakeSignal.VarianceFunction.valueOf(((String)reg.getOptions().getArg(Option.fakenullstatistics)));
                proc.setNullStatistics(vf);
            } catch(IllegalArgumentException e) {
            }
        }
        
        
        return proc;
        
    }
    
    public String getName() {
        return "INSERTFAKE";
    }
    
}
