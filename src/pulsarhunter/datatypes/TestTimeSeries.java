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
 * TestTimeSeries.java
 *
 * Created on 27 September 2006, 09:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.IOException;
import java.util.Random;

/**
 *
 * @author mkeith
 */
public class TestTimeSeries extends TimeSeries {
    TimeSeries.Header header =  new TimeSeries.Header();
    float[] data;
    /** Creates a new instance of TestTimeSeries */
    public TestTimeSeries(int nbins,double tsamp,double period) {
        data = new float[nbins];
        header.setNPoints(nbins);
        header.setTSamp(tsamp);
        header.setTobs(nbins*header.getTSamp());
        
        Random rand = new Random();
        
        int profileWidth = (int)(period / header.getTSamp());
        
        for(int i = 0; i < nbins ; i++){
            data[i] = (float)rand.nextGaussian();
            //data[i] = 0;
            
            double time = i * tsamp+ 0.5*period;
            
            if((time / period - (int)(time/period)) < 0.1){
                data[i]+=10;
            }
            
            
            // System.out.println(profbin +" "+data[i]);
        }
        
        
    }
    
    
    public TimeSeries.Header getHeader() {
        
        return header;
        
    }
    
    public float[] getDataAsFloats() {
        return data;
    }
    
    public float getBin(long bin) {
        return data[(int)bin];
    }
    
    public void release() {
        this.header = null;
        this.data = null;
    }
    
    public void flush() throws IOException {
        throw new UnsupportedOperationException("Flush not supported on TestTimeSeries");
    }
    
}
