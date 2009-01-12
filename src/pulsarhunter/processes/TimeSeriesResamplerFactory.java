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
 * TimeSeriesResamplerFactory.java
 *
 * Created on 05 February 2007, 13:41
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
import pulsarhunter.datatypes.TimeSeries;

/**
 *
 * @author mkeith
 */
public class TimeSeriesResamplerFactory implements ProcessFactory{
    
    /** Creates a new instance of TimeSeriesResamplerFactory */
    public TimeSeriesResamplerFactory() {
    }
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles, PulsarHunterRegistry reg) throws ProcessCreationException {
        
        
        if (params.length < 2) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        Data tim1 = dataFiles.get(params[1]);
        Data tim2 = dataFiles.get(params[2]);
        

        
        if(! (tim1 instanceof TimeSeries))  throw new ProcessCreationException("Argument 1 must be a TimeSeries for process "+this.getName());
        if(! (tim2 instanceof TimeSeries))  throw new ProcessCreationException("Argument 2 must be a TimeSeries for process "+this.getName());
        
        return new TimeseriesResampler((TimeSeries)tim1,(TimeSeries)tim2);
    }
    
    public String getName() {
        return "RESAMPLER";
    }
    
}
