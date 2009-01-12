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
 * Folder.java
 *
 * Created on 15 March 2007, 16:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import pulsarhunter.datatypes.BulkReadable;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.TimeSeries;

/**
 *
 * @author mkeith
 */
public interface TimeSeriesFolder {
    public double[] fold(TimeSeries data,int profileWidth);
    public double[] fold(BulkReadable<TimeSeries.Header> data,int profileWidth);
    
    public double[][] multiFold(TimeSeries[] data,int profileWidth);
   

    public double[][] multiFold(BulkReadable<MultiChannelTimeSeries.Header> data,int profileWidth);
    
    
}
