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
 * Created on 26 September 2006, 15:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import java.io.IOException;
import java.util.Arrays;
import pulsarhunter.*;
import pulsarhunter.datatypes.BulkReadable;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.TimeSeries;

/**
 *
 * @author mkeith
 */
public class TimeModelFolder implements TimeSeriesFolder {
    
    
    double refMJD = -1;
    VariableTimeModel foldingModel;
    double period;
    
    /** Creates a new instance of Folder */
    public TimeModelFolder(VariableTimeModel foldingModel,double period,double refMJD) {
        this.period = period;
        this.foldingModel = foldingModel;
        this.refMJD = refMJD;
    }
    
    
    
    private static int nnn = 0;
    
    
    public double[] fold(TimeSeries data,int profileWidth){
        
//        System.out.println("OLDFOLD");
        
        double sampleRate = data.getHeader().getTSamp();
        //System.out.println("Tsamp: "+sampleRate );
        
        long nbins = data.getHeader().getNPoints();
        // System.out.println("Nbins: "+nbins );
        
        
      /*  if(refMJD <= 0 ){
            refMJD = data.getHeader().getMjdStart();
        }*/
        
        //double toff = Convert.mjdToSec(data.getHeader().getMjdStart());
        
        double toff_ref = Convert.mjdToSec(data.getHeader().getMjdStart() - refMJD);
        
        
        
//        double period = foldingModel.getPeriod(Convert.secToMJD(toff));
        double naturalTimePassed=toff_ref;
        double timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
        
        double refreshTime = foldingModel.getRefreshTime();
        if(profileWidth > (int)(period / sampleRate+0.5)){
            profileWidth = (int)(period / sampleRate+0.5);
        }
        
        //System.out.println("Toff:"+toff);
        
        //===
        /*double fme = ((PdotFoldingModel) foldingModel).getEpoch();
      //  if(nnn >= 127) period =  ((PdotFoldingModel) foldingModel).period;
        System.out.println("\nnnn     : "+nnn+
                           "\nRef     : "+refMJD+
                           "\ntoff_ref: "+toff_ref+
                           "\ntoff    : "+toff+
                           "\nfmepoch : "+Convert.mjdToSec(Convert.secToMJD(toff)-fme)+
                           "\nperiod  : "+period);
        TimeSeriesFolder.nnn++;
         */
        //===
        
//
//        System.out.println("TEST");
//        System.out.println(period);
//        System.out.println(data.getHeader().getMjdStart());
//        System.out.println(refMJD);
//        System.out.println(toff_ref);
//        System.out.println(toff);
//        System.out.println("ENDTEST");
        
        //nbins = 100000;
        
        //double scalef = (double)nbins/profileWidth;
        //double scalef = (period / sampleRate+0.5)/profileWidth;
        
  /*      double waste = ((nbins*sampleRate/ period +toff)) - (int)((nbins*sampleRate/ period +toff));
        nbins -= waste*profileWidth;*/
        
        
        double[] profile = new double[profileWidth];
        double[] count = new double[profileWidth];
        double carry = 0;
        if(foldingModel.getRefreshTime()==0){
            //  PulsarHunter.out.println("TimeSeriesFolder - Using fast fixed folding");
            this.foldFixed(profile,count,profileWidth,period,data,nbins,sampleRate,toff_ref);
        } else {
            // PulsarHunter.out.println("TimeSeriesFolder - Using slower period drift folding");
            
            double targetBin_d = 0;
            int targetBin;
            for(long bin = 0; bin < nbins; bin++){
                
                timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
                
                
                targetBin_d = (timePassed/ period ) * profileWidth +0.5;
                
                
                targetBin = (((int)targetBin_d) % profileWidth);
                double diff =  (targetBin_d - (int)targetBin_d);
                if( targetBin_d < 0){
                    targetBin--;
                    
                    targetBin += profileWidth;
                }
                
                profile[targetBin] += data.getBin(bin);
                count[targetBin] += 1;
                naturalTimePassed += sampleRate;
                
            }
            
        }
        
        
        double countoff = nbins/profile.length;
        for(int i =0; i < profile.length; i++)profile[i]/=(count[i]/countoff);
        return profile;
    }
    
    private final void foldFixed(final double[] profile,final double[] count,final int profileWidth, final double period, final TimeSeries data, final long nbins, final double sampleRate, final double toff){
        double targetBin_d;
        int targetBin;
        for(long bin = 0 ; bin < nbins; bin++){
            targetBin_d = (((bin*sampleRate+toff)/ period )) * profileWidth + 0.5;
            
            targetBin = (((int)targetBin_d) % profileWidth);
            
            if( targetBin_d < 0){
                targetBin--;
                
                targetBin += profileWidth;
            }
            
            profile[targetBin] += data.getBin(bin);
            count[targetBin] += 1;
        }
    }
    
    public double[] fold(BulkReadable<TimeSeries.Header> data,int profileWidth){
        
        
//        System.out.println("NEWFOLD");
        
        double sampleRate = data.getHeader().getTSamp();
        int nbins = (int)data.getHeader().getNPoints();
        
        float[] fArr = new float[nbins];
        try {
            data.read(0,fArr);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        double toff_ref = Convert.mjdToSec(data.getHeader().getMjdStart() - refMJD);
        
        
        
        
        double naturalTimePassed=toff_ref;
        double timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
        
        double refreshTime = foldingModel.getRefreshTime();
        if(profileWidth > (int)(period / sampleRate+0.5)){
            profileWidth = (int)(period / sampleRate+0.5);
        }
        
        double[] profile = new double[profileWidth];
        double[] count = new double[profileWidth];
        
        int nbins_fix = nbins;//(int)( (int)(nbins*sampleRate/period) * period/sampleRate  );
        
        
        double targetBin_d = 0;
        int targetBin;
        if(foldingModel.getRefreshTime()==0){
            
            
            
            for(int bin = 0 ; bin < nbins_fix; bin++){
                targetBin_d = (((bin*sampleRate+toff_ref)/ period )) * profileWidth + 0.5;
                //targetBin_d = (((bin*sampleRate)/ period )) * profileWidth;
                
                targetBin = (((int)targetBin_d) % profileWidth);
                
                
                
//                    System.out.println(diff);
//                    System.out.println(targetBin+ " "+targetBin_d);
                if( targetBin_d < 0){
                    targetBin--;
                    targetBin += profileWidth;
                }
                // nchan = 1;
                // System.out.println(nchan);
                
                
                profile[targetBin] += fArr[bin];
                //profile[targetBin] += Math.random();
                
                count[targetBin] += 1;
                
                
            }
            
            
            
            
        } else {
            
            
            for(int bin = 0; bin < nbins_fix; bin++){
                
                
                timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
                
                
                targetBin_d = (timePassed/ period ) * profileWidth +0.5;
                
                
                targetBin = (((int)targetBin_d) % profileWidth);
                
                
                
                if( targetBin_d < 0){
                    targetBin--;
                    
                    targetBin += profileWidth;
                }
                
                profile[targetBin] += fArr[bin];
                count[targetBin] += 1;
                
                
                naturalTimePassed += sampleRate;
                
                
            }
            
        }
//        double countoff = count[0];
//        int n = 0;
//        double sum=0;
//        for(int i =0; i < profile.length; i++){
//            n++;
//            sum+=count[i];
//        }
//        countoff = sum / n;
        
        
        double countoff = nbins_fix/profile.length;
        for(int i =0; i < profile.length; i++)profile[i]/=(count[i]/countoff);
        
        
//        double[] profile = new double[profileWidth];
//
//
//        if(foldingModel.getRefreshTime()==0){
//            double targetBin_d;
//            int targetBin;
//            for(int bin = 0 ; bin < nbins; bin++){
//                targetBin_d = (((bin*sampleRate+toff_ref)/ period )) * profileWidth + 0.5;
//
//                targetBin = (((int)targetBin_d) % profileWidth);
//                if( targetBin_d < 0){
//                    targetBin--;
//
//                    targetBin += profileWidth;
//                }
//                profile[targetBin] += fArr[bin];
//            }
//        } else {
//
//            double targetBin_d = 0;
//            int targetBin;
//            for(int bin = 0; bin < nbins; bin++){
//
//                timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
//
//
//                targetBin_d = (timePassed/ period ) * profileWidth +0.5;
//
//
//                targetBin = (((int)targetBin_d) % profileWidth);
//
//                if( targetBin_d < 0){
//                    targetBin--;
//
//                    targetBin += profileWidth;
//                }
//
//                profile[targetBin] += fArr[bin];
//
//                naturalTimePassed += sampleRate;
//
//            }
//
//        }
        
        
        return profile;
    }
    
    
    
    
    
    
    
    public double[][] multiFold(TimeSeries[] data,int profileWidth){
        
        
//        System.out.println("OLDFOLD-M");
        
        int nsub = data.length;
        
        double sampleRate = data[0].getHeader().getTSamp();
        long nbins = data[0].getHeader().getNPoints();
        
        /*if(refMJD < 0 ){
            refMJD = data[0].getHeader().getMjdStart();
        }*/
        
        
        // double toff = Convert.mjdToSec(data[0].getHeader().getMjdStart());
        double toff_ref = Convert.mjdToSec(data[0].getHeader().getMjdStart() - refMJD);
        
        double naturalTimePassed=toff_ref;
        double timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
        
        
        
//        double period = foldingModel.getPeriod(data[0].getHeader().getMjdStart());
        
        double refreshTime = foldingModel.getRefreshTime();
        if(profileWidth > (int)(period / sampleRate+0.5)){
            profileWidth = (int)(period / sampleRate+0.5);
        }
        
        
        //System.out.println("nsub:"+nsub+" wid:"+profileWidth+ "period: "+period);
        double[][] profile = new double[nsub][profileWidth];
        //PulsarHunter.out.println("PW:"+profileWidth);
        double[][] count = new double[nsub][profileWidth];
        
        
        
        
        
        
        if(foldingModel.getRefreshTime()==0){
            // PulsarHunter.out.println("TimeSeriesFolder - Using fast fixed folding");
            this.mFoldFixed(profile,count,profileWidth,period,data,nbins,sampleRate,toff_ref);
            
            
        } else {
            // PulsarHunter.out.println("TimeSeriesFolder - Using slower period drift folding");
            
            
            
            double targetBin_d = 0;
            int targetBin;
            for(long bin = 0; bin < nbins; bin++){
                
                timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
                
                
                targetBin_d = (((timePassed)/ period )) * profileWidth +0.5;
                
                targetBin = (((int)targetBin_d) % profileWidth);
                
                if( targetBin_d < 0){
                    targetBin--;
                    targetBin += profileWidth;
                }
                
                for(int  s = 0 ; s < nsub; s++){
                    
                    profile[s][targetBin] += data[s].getBin(bin);
                    count[s][targetBin] += 1;
                }
                
                
                
                naturalTimePassed += sampleRate;
                
            }
        }
        
        double countoff = nbins/profile[0].length;
        for(int s=0; s < nsub; s++)for(int i =0; i < profile[s].length; i++)profile[s][i]/=(count[s][i]/countoff);
        
        return profile;
        
        
    }
    
    
    private final void mFoldFixed(final double[][] profile,final double[][] count,final int profileWidth, final double period, final TimeSeries[] data, final long nbins, final double sampleRate, final double toff){
        double targetBin_d;
        int targetBin;
        
        final int nsub = data.length;
        for(long bin = 0 ; bin < nbins; bin++){
            targetBin_d = (((bin*sampleRate+toff)/ period )) * profileWidth + 0.5;
            
            targetBin = (((int)targetBin_d) % profileWidth);
            
            if( targetBin_d < 0){
                targetBin--;
                targetBin += profileWidth;
            }
            for(int  s = 0 ; s < nsub; s++){
                // System.out.println("fl: "+bin);
                profile[s][targetBin] += data[s].getBin(bin);
                count[s][targetBin] += 1;
                
            }
            
        }
        
        
    }
    
    
    public double[][] multiFold(BulkReadable<MultiChannelTimeSeries.Header> data,int profileWidth){
        
//        System.out.println("NEWFOLD-M");
        
        final int nchan = data.getHeader().getNumChannel();
        
        
        boolean interleaved = data.getHeader().isChannelInterleaved();
        double sampleRate = data.getHeader().getTSamp();
        
        int nbins = (int)data.getHeader().getNPoints();
        
        float[] fArr = new float[nbins*nchan];
        try {
            data.read(0,fArr);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        double toff_ref = Convert.mjdToSec(data.getHeader().getMjdStart() - refMJD);
        
        double naturalTimePassed=toff_ref;
        double timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
        
        
        double refreshTime = foldingModel.getRefreshTime();
        if(profileWidth > (int)(period / sampleRate+0.5)){
            profileWidth = (int)(period / sampleRate+0.5);
        }
        
        
        double[][] profile = new double[nchan][profileWidth];
        double[][] count = new double[nchan][profileWidth];
        for(int  s = 0 ; s < nchan; s++)Arrays.fill(profile[s],0);
        for(int  s = 0 ; s < nchan; s++)Arrays.fill(count[s],0);
        final int nbins_fix = nbins;//(int)( (int)(nbins*sampleRate/period) * period/sampleRate  );
       // System.out.println(nbins_fix+" "+nbins);
        
        double targetBin_d = 0;
        int targetBin;
        if(foldingModel.getRefreshTime()==0){
            
            if(interleaved){
                
                for(int bin = 0 ; bin < nbins_fix; bin++){
                    targetBin_d = (((bin*sampleRate+toff_ref)/ period )) * profileWidth + 0.5;
                    //targetBin_d = (((bin*sampleRate)/ period )) * profileWidth;
                    
                    targetBin = (((int)targetBin_d) % profileWidth);
                    
                    if( targetBin_d < 0){
                        targetBin--;
                        targetBin += profileWidth;
                    }


                    for(int  s = 0 ; s < nchan; s++){

                         profile[s][targetBin] += fArr[s + nchan*bin];// fArr[s + nchan*bin];
                       // profile[s][targetBin] += 1;//Math.random()*(s+1)*(s+1);

                        count[s][targetBin] += 1;
                        
                    }
                    
                    
                }
            }  else {
                for(int bin = 0 ; bin < nbins_fix; bin++){
                    targetBin_d = (((bin*sampleRate+toff_ref)/ period )) * profileWidth + 0.5;
                    
                    targetBin = (((int)targetBin_d) % profileWidth);
                    
                    
                    if( targetBin_d < 0){
                        targetBin--;
                        targetBin += profileWidth;
                    }
                    for(int  s = 0 ; s < nchan; s++){
                        // System.out.println("fl: "+bin);
                        profile[s][targetBin] += fArr[bin + nbins*s];
                        count[s][targetBin] += 1;
                    }
                    
                }
            }
        } else {
            
            if(interleaved){
                for(int bin = 0; bin < nbins_fix; bin++){
                    
                    timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
                    
                    
                    targetBin_d = (((timePassed)/ period )) * profileWidth + 0.5;
                    
                    targetBin = (((int)targetBin_d) % profileWidth);
                    
                    if( targetBin_d < 0){
                        targetBin--;
                        targetBin += profileWidth;
                    }
                    
                    for(int  s = 0 ; s < nchan; s++){
                        profile[s][targetBin] += fArr[s + nchan*bin];
                        count[s][targetBin] += 1;
                    }
                    
                    naturalTimePassed += sampleRate;
                }
            } else {
                for(int bin = 0; bin < nbins_fix; bin++){
                    
                    timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
                    
                    
                    targetBin_d = (((timePassed)/ period )) * profileWidth +0.5;
                    
                    targetBin = (((int)targetBin_d) % profileWidth);
                    
                    if( targetBin_d < 0){
                        targetBin--;
                        targetBin += profileWidth;
                    }
                    
                    for(int  s = 0 ; s < nchan; s++){
                        profile[s][targetBin] += fArr[bin + nbins*s];
                        count[s][targetBin] += 1;
                    }
                    
                    naturalTimePassed += sampleRate;
                }
            }
        }
//        double countoff = count[0][0];
//        int n = 0;
//        double sum=0;
//        for(int s=0; s < nchan; s++)for(int i =0; i < profile[s].length; i++){
//            n++;
//            sum+=count[s][i];
//        }
//        countoff = sum / n;
//
//        for(int s=0; s < nchan; s++)for(int i =0; i < profile[s].length; i++)profile[s][i]/=(count[s][i]/countoff);
        
        //for(int i =0; i < profile[0].length; i++)System.out.println(profile[0][i]+"\t"+count[0][i]);
        
        double countoff = nbins_fix/profile[0].length;
        for(int s=0; s < nchan; s++)for(int i =0; i < profile[s].length; i++)profile[s][i]/=(count[s][i]/countoff);
         //       for(int i =0; i < profile[0].length; i++)System.out.println(profile[0][i]);
        return profile;
        
        
    }
}

