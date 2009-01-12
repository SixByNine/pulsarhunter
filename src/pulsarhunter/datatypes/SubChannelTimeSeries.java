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
 * SubTimeSeries.java
 *
 * Created on 27 September 2006, 10:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.IOException;
import pulsarhunter.*;

/**
 *
 * @author mkeith
 */
public class SubChannelTimeSeries extends MultiChannelTimeSeries{
    
    private MultiChannelTimeSeries masterTS;
    private int startChan;
    private int endChan;
    private MultiChannelTimeSeries.Header header;
    /** Creates a new instance of SubTimeSeries */
    public SubChannelTimeSeries(MultiChannelTimeSeries masterTS, int startChan, int endChan) {
        this.masterTS = masterTS;
        this.startChan = startChan;
        this.endChan = endChan;
        this.header = new MultiChannelTimeSeries.Header();
        header.setBandwidth(masterTS.getHeader().getBandwidth());
        header.setFrequency(masterTS.getHeader().getFrequency());
        header.setSourceID(masterTS.getHeader().getSourceID());
        header.setTSamp(masterTS.getHeader().getTSamp());
        header.setNPoints(masterTS.getHeader().getNPoints());
        
        header.setMjdStart(masterTS.getHeader().getMjdStart());
        header.setTobs(header.getTSamp()*header.getNPoints());
        header.setNumChannel(endChan - startChan+1);
        header.setInterleaved(masterTS.getHeader().isChannelInterleaved());
    }
    
    
    public MultiChannelTimeSeries.Header getHeader() {
        return header;
    }
    
    
    public void release() {
        this.masterTS = null;
        this.header = null;
        
    }
    
    public void flush() throws IOException {
        masterTS.flush();
    }
    
    public TimeSeries getOnechannel(int channelNumber) {
        return masterTS.getOnechannel(channelNumber+startChan);
    }
    
    
    
    public float[][] getDataAsFloats() {
        return null;
    }
    
    
    
    
}
