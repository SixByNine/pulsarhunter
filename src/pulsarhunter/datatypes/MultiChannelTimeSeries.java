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
 * MultichannelTimeSeries.java
 *
 * Created on 26 September 2006, 15:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.IOException;
import pulsarhunter.*;

/**
 * This represents time series that is composed of more than one frequency channel.
 * @author mkeith
 */
public abstract class MultiChannelTimeSeries implements Data<MultiChannelTimeSeries.Header>{
    
    
    
    
    //public Data subData(double startTime, double endTime);
    public abstract float[][] getDataAsFloats();
    /**
     * Returns a TimeSeries that represents a single channel in this data.
     * The returned TimeSeries will call the data access methods on this object.
     *
     * Note that if the data isChannelInterleaved the bins read by the returned timeseries
     * will be spaced by nchans, so care is neaded not to read the file multiple times
     * if the data is not entirely in memory.
     * @param channelNumber
     * @return
     */
    public abstract TimeSeries getOnechannel(int channelNumber);
    
    public MultiChannelTimeSeries subData(long startBin, long endBin) {
        return new SubMultiChannelTimeSeries(this,startBin,endBin);
    }
    
    /**
     * Returns a MultiChannelTimeSeries with a subset of the bands in this MultiChannelTimeSeries.
     * The type of the returned time series may be different, and should call the underlying methods on this timeseries.
     * This does not copy the data.
     * @param bandStart The first band to include
     * @param bandEnd The last band to include
     * @return The MultiChannelTimeSeries with the sub bands.
     */
    public MultiChannelTimeSeries subBands(int bandStart, int bandEnd) {
        return new SubChannelTimeSeries(this,bandStart,bandEnd);
    }
    
    
    public BulkReadable getBulkReadableInterface(){
        return null;
    }
    
    
    public BulkWritable getBulkWritableInterface(){
        return null;
    }
    
    public abstract void release();
    
    public abstract MultiChannelTimeSeries.Header getHeader();
    
    public abstract void flush() throws IOException;
    
    
    
    public String getDataType() {
       return null;
    }
    
    public class Header extends Data.Header{
        
        private long nPoints;
        private double tSamp;
        private int numChannel;
        private double Dm = 0;
        private boolean interleaved = true;
        
        public long getNPoints() {
            return nPoints;
        }
        
        public void setNPoints(long nPoints) {
            this.nPoints = nPoints;
        }
        
        public double getTSamp() {
            return tSamp;
        }
        
        public void setTSamp(double tSamp) {
            this.tSamp = tSamp;
        }
        
        public int getNumChannel() {
            return numChannel;
        }
        
        public void setNumChannel(int numChannel) {
            this.numChannel = numChannel;
        }
        
        public double getDm() {
            return Dm;
        }
        
        public void setDm(double Dm) {
            this.Dm = Dm;
        }
        
        
        /**
         * This returns true if the data is stored in a single file in the order:
         *
         * b1c1,b1c2,b1c3,b2c1,b2c2,b2c3,...
         *
         * or similar. This is used to deterine if folding should be done by channel first or by bin first.
         * @return true if the data is arranged in consecutive channels, or false if the channels are seperate
         */
        public boolean isChannelInterleaved(){
            return interleaved;
        }

        protected void setInterleaved(boolean interleaved) {
            this.interleaved = interleaved;
        }
        
        
        
    }
    
}
