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
 * TimeSeries.java
 *
 * Created on 26 September 2006, 15:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import pulsarhunter.*;

/**
 *
 * @author mkeith
 */
public abstract class TimeSeries implements Data<TimeSeries.Header>{
    
    
    
    
    
    //public Data subData(double startTime, double endTime);
    public abstract float[] getDataAsFloats();
    public abstract float getBin(long bin);

    
    public TimeSeries subData(long startBin, long endBin) {
        return new SubTimeSeries(this,startBin,endBin);
    }
    
    
    public BulkReadable getBulkReadableInterface(){
        return null;
    }
    
    public String getDataType() {
        return null;
    }
    
    public BulkWritable getBulkWritableInterface(){
        return null;
    }
    
    public double getRMS(long startbin,long nbins){
        long endbin = startbin+nbins;
        double sum = 0;
        double ssq = 0;
        
        for(long ptr = startbin ; ptr < endbin; ptr++){
            float v = this.getBin(ptr);
            ssq += v*v;
            sum+=v;
        }
        double meansq = (sum/nbins)*(sum/nbins);
        double rms = Math.sqrt(ssq/nbins-meansq);
        return rms;
    }
    
    public class Header extends Data.Header{
        
        private long nPoints;
        private double tSamp;
        private double dm;
        
        
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
        
        public double getDm() {
            return dm;
        }
        
        public void setDm(double dm) {
            this.dm = dm;
        }
        
    }
}
