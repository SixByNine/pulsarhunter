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
 * PolyCoFolder.java
 *
 * Created on 15 March 2007, 16:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import java.io.IOException;
import pulsarhunter.Convert;
import pulsarhunter.datatypes.BulkReadable;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.PolyCoFile;
import pulsarhunter.datatypes.PulsarPolyco;
import pulsarhunter.datatypes.TimeSeries;

/**
 *
 * @author mkeith
 */
public class PolyCoFolder implements TimeSeriesFolder{
    
    private PolyCoFile pcf;
    private String pulsarName;
    
    /** Creates a new instance of PolyCoFolder */
    public PolyCoFolder(PolyCoFile pcf,String pulsarName) {
        this.pcf = pcf;
        this.pulsarName = pulsarName;
        
    }
    
    public PolyCoFolder(PolyCoFile pcf) {
        this(pcf,null);
    }
    
    public double[][] multiFold(TimeSeries[] data, int profileWidth) {
        String pulsarName = this.pulsarName;
        if(pulsarName==null){
            pulsarName = data[0].getHeader().getSourceID();
        }
        PulsarPolyco ppc = pcf.getPolyCo(pulsarName);
        
        double sampleRate = data[0].getHeader().getTSamp();
        
        long nbins = data[0].getHeader().getNPoints();
        
        
        double mjdstart = data[0].getHeader().getMjdStart();
        double mjd = mjdstart;
        double mjdinc = Convert.secToMJD(sampleRate);
        
        
        double period = ppc.getPeriodAt(mjd);
        
        
        if(profileWidth > (int)(period / sampleRate+0.5)){
            profileWidth = (int)(period / sampleRate+0.5);
        }
        
        double[][] profile = new double[data.length][profileWidth];
        double targetBin_d = 0;
        int targetBin;
        double frac = 0.0;
        double val;
        nbins--;
        for(long bin = 0; bin < nbins; bin++){
            
            targetBin_d = ppc.getPhaseAt(mjd) * profileWidth + 0.5;
            //  targetBin = (int)targetBin_d;
            targetBin = ((int)targetBin_d)%profileWidth;
            
            frac = targetBin_d - targetBin;
            
            
            
            
            for(int  s = 0 ; s < data.length; s++){
//                val = data[s].getBin(bin);
//                profile[s][targetBin] += val*frac;
//
//                targetBin++;
//                if(targetBin >= profile.length){
//                    targetBin=0;
//                }
//                profile[s][targetBin] += val*(1-frac);
                
                profile[s][targetBin] += data[s].getBin(bin);
                
            }
            
            
            
            
            
            mjd = mjdstart + mjdinc*bin;
            
        }
        return profile;
    }
    
    public double[] fold(BulkReadable<TimeSeries.Header> data, int profileWidth) {
        String pulsarName = this.pulsarName;
        if(pulsarName==null){
            pulsarName = data.getHeader().getSourceID();
        }
        PulsarPolyco ppc = pcf.getPolyCo(pulsarName);
        
        
        
        double sampleRate = data.getHeader().getTSamp();
        
        int nbins = (int) data.getHeader().getNPoints();
        
        float[] fArr = new float[nbins];
        try {
            data.read(0,fArr);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        double mjdstart = data.getHeader().getMjdStart();
        double mjd = mjdstart;
        double mjdinc = Convert.secToMJD(sampleRate);
        double time = Convert.mjdToSec(mjdstart);
        
        double period = ppc.getPeriodAt(mjd);
        
        
        if(profileWidth > (int)(period / sampleRate+0.5)){
            profileWidth = (int)(period / sampleRate+0.5);
        }
        
        
        
        double[] profile = new double[profileWidth];
        double targetBin_d = 0;
        int targetBin;
        double frac = 0.0;
        double val;
        nbins--;
        for(int bin = 0; bin < nbins; bin++){
            
            
            //mjd = Convert.secToMJD(time);
            
            targetBin_d = ppc.getPhaseAt(mjd) * profileWidth + 0.5;
            
            targetBin = ((int)targetBin_d)%profileWidth;
            
            profile[targetBin] +=fArr[bin];
            
//            frac = targetBin_d - targetBin;
//            //frac=1;
//            frac = 1.0-frac;
//            val = fArr[bin];
//            profile[targetBin] += val*frac;
//
//            targetBin++;
//            if(targetBin >= profile.length){
//                targetBin=0;
//            }
//            profile[targetBin] += val*(1-frac);
            
            mjd = mjdstart + mjdinc*bin;
            //time+=sampleRate;
        }
        return profile;
    }
    
    public double[][] multiFold(BulkReadable<MultiChannelTimeSeries.Header> data, int profileWidth) {
        String pulsarName = this.pulsarName;
        if(pulsarName==null){
            pulsarName = data.getHeader().getSourceID();
        }
        PulsarPolyco ppc = pcf.getPolyCo(pulsarName);
        
        double sampleRate = data.getHeader().getTSamp();
        int nchan = data.getHeader().getNumChannel();
        int nbins = (int)data.getHeader().getNPoints();
        
        float[] fArr = new float[nbins*nchan];
        try {
            data.read(0,fArr);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        double mjdstart = data.getHeader().getMjdStart();
        double mjd = mjdstart;
        double mjdinc = Convert.secToMJD(sampleRate);
        
        
        double period = ppc.getPeriodAt(mjd);
        
        
        if(profileWidth > (int)(period / sampleRate+0.5)){
            profileWidth = (int)(period / sampleRate+0.5);
        }
        
        double[][] profile = new double[nchan][profileWidth];
        double targetBin_d = 0;
        int targetBin;
        double frac = 0.0;
        double val;
        nbins--;
        for(int bin = 0; bin < nbins; bin++){
            
            targetBin_d = ppc.getPhaseAt(mjd) * profileWidth + 0.5;
            
            //  targetBin = (int)targetBin_d;
            targetBin = ((int)targetBin_d)%profileWidth;
            
            frac = 1.0 - (targetBin_d - targetBin);
            
            
            //     System.out.println(mjd+"\t"+targetBin_d+"\t"+targetBin+"\t"+frac);
            
            
            
            for(int  s = 0 ; s < nchan; s++){
//                val =  fArr[s + nchan*bin];
//                profile[s][targetBin] += val*frac;
//
//                targetBin++;
//                if(targetBin >= profile.length){
//                    targetBin=0;
//                }
//                profile[s][targetBin] += val*(1.0-frac);
                
                profile[s][targetBin] +=fArr[s + nchan*bin];
            }
            
            
            
            
            
            //   mjd += Convert.secToMJD(sampleRate);
            mjd = mjdstart + mjdinc*bin;
            
        }
        return profile;
    }
    
    public double[] fold(TimeSeries data, int profileWidth) {
        String pulsarName = this.pulsarName;
        if(pulsarName==null){
            pulsarName = data.getHeader().getSourceID();
        }
        PulsarPolyco ppc = pcf.getPolyCo(pulsarName);
        
        double sampleRate = data.getHeader().getTSamp();
        
        long nbins = data.getHeader().getNPoints();
        
        
        double mjdstart = data.getHeader().getMjdStart();
        double mjd = mjdstart;
        double mjdinc = Convert.secToMJD(sampleRate);
        
        
        double period = ppc.getPeriodAt(mjd);
        
        
        if(profileWidth > (int)(period / sampleRate+0.5)){
            profileWidth = (int)(period / sampleRate+0.5);
        }
        
        double[] profile = new double[profileWidth];
        double targetBin_d = 0;
        int targetBin;
        double frac = 0.0;
        double val;
        nbins--;
        for(long bin = 0; bin < nbins; bin++){
            
            targetBin_d = ppc.getPhaseAt(mjd) * profileWidth + 0.5;
            
            //  targetBin = (int)targetBin_d;
            targetBin = ((int)targetBin_d)%profileWidth;
            
            frac = targetBin_d - targetBin;
            
            
            
//            val = data.getBin(bin);
//            profile[targetBin] += val*frac;
//
//            targetBin++;
//            if(targetBin >= profile.length){
//                targetBin=0;
//            }
//            profile[targetBin] += val*(1-frac);
//
            profile[targetBin] += data.getBin(bin);
            
            mjd = mjdstart + mjdinc*bin;
            
        }
        return profile;
    }
}
