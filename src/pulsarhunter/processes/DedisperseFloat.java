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
 * DedisperseFloat.java
 *
 * Created on 14 February 2007, 12:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import java.util.Arrays;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.WritableTimeSeries;

/**
 *
 * @author mkeith
 */
public class DedisperseFloat implements PulsarHunterProcess{
    
    private final int nchans;
    private final int ndms;
    
    
    private final double[] dms;
    private final MultiChannelTimeSeries inTimeSeries;
    private final TimeSeries[] chanels;
    private final WritableTimeSeries[] output;
    
    
    /** Creates a new instance of DedisperseFloat */
    public DedisperseFloat(MultiChannelTimeSeries inTimeSeries, WritableTimeSeries[] output, double[] dms) {
        this.nchans = inTimeSeries.getHeader().getNumChannel();
        this.ndms = dms.length;
        this.dms = dms;
        this.output = output;
        this.inTimeSeries = inTimeSeries;
        this.chanels = new TimeSeries[nchans];
        for(int chan = 0; chan < nchans; chan++){
            this.chanels[chan] = inTimeSeries.getOnechannel(chan);
        }
        
    }
    
    public void run() {
        int[][] offsets = new int[nchans][ndms];
        
        int minOff = Integer.MAX_VALUE;
        int maxOff = 0;
        for(int dmidx = 0; dmidx < ndms; dmidx++){
            WritableTimeSeries wts = output[dmidx];
            
            TimeSeries outts = (TimeSeries)wts;
            outts.getHeader().setSourceID(inTimeSeries.getHeader().getSourceID());
            outts.getHeader().setNPoints(inTimeSeries.getHeader().getNPoints());
            outts.getHeader().setTSamp(inTimeSeries.getHeader().getTSamp());
            outts.getHeader().setCoord(inTimeSeries.getHeader().getCoord());
            outts.getHeader().setFrequency(inTimeSeries.getHeader().getFrequency());
            outts.getHeader().setDm(dms[dmidx]);
            outts.getHeader().setMjdStart(inTimeSeries.getHeader().getMjdStart());
            outts.getHeader().setTelescope(inTimeSeries.getHeader().getTelescope());
            
        }
        double fch0;
        int toWrite = 100;
        boolean reverse = false;
        if(chanels[0].getHeader().getFrequency() > chanels[nchans-1].getHeader().getFrequency())reverse = true;
        if(reverse){
            //fch0 = (chanels[nchans-1].getHeader().getFrequency() + chanels[nchans-1].getHeader().getBandwidth()/2.0)/1000.0;
            fch0 = (chanels[nchans-1].getHeader().getFrequency())/1000.0;
        } else {
            //fch0 = (chanels[0].getHeader().getFrequency() + chanels[0].getHeader().getBandwidth()/2.0)/1000.0;
            fch0 = (chanels[0].getHeader().getFrequency())/1000.0;
        }
        for(int dmidx = 0; dmidx < ndms; dmidx++){
            for(int chan = 0; chan < nchans; chan++){
                // centre freq in GHz
                //double fch = (chanels[chan].getHeader().getFrequency() + chanels[chan].getHeader().getBandwidth()/2.0)/1000.0;
                double fch = (chanels[chan].getHeader().getFrequency())/1000.0;
                double timeOffset;
                if(reverse){
                    timeOffset = 4.148808e-3*(-1.0/(fch*fch) + 1.0/(fch0*fch0))*dms[dmidx];
                } else {
                    timeOffset = 4.148808e-3*(1.0/(fch*fch) - 1.0/(fch0*fch0))*dms[dmidx];
                }
                offsets[chan][dmidx] = (int)(timeOffset / inTimeSeries.getHeader().getTSamp());
                
                if(offsets[chan][dmidx] < minOff)minOff = offsets[chan][dmidx];
                if(offsets[chan][dmidx] > maxOff)maxOff = offsets[chan][dmidx];
            }
        }
        
        float[][] buffer = new float[ndms][maxOff+101];
        for(float[] arr : buffer){
            Arrays.fill(arr,0);
        }
        int posn = 0;
        
        
        long nbins = inTimeSeries.getHeader().getNPoints();
        
        for(long bin = 0 ; bin < nbins; bin++){
            
            
            
            for(int chan = 0; chan < nchans; chan++){
                float val = chanels[chan].getBin(bin);

                
                for(int dmidx = 0; dmidx < ndms; dmidx++){
                    int bufloc = posn + offsets[chan][dmidx];
                    if(bufloc >= buffer[0].length) bufloc -= buffer[0].length;
                    // if(bufloc < 0 ) bufloc += buffer[0].length;
                    buffer[dmidx][bufloc] += val;
                }
                
            }
            for(int dmidx = 0; dmidx < ndms; dmidx++){
                output[dmidx].writeBin(bin,buffer[dmidx][posn]);
                buffer[dmidx][posn] = 0;
            }
            posn++;
            if(posn == buffer[0].length) posn = 0;
            //if(posn < 0)posn = buffer[0].length -1;
            
        }
        
        
    }
    
    
    
    
    
}
