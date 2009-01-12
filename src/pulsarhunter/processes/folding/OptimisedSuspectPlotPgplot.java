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
 * OptimisedSuspectPlotPgplot.java
 *
 * Created on 02 November 2006, 15:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import pulsarhunter.Convert;
import pulsarhunter.PgplotInterface;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.datatypes.PulsarHunterCandidate;

/**
 *
 * @author mkeith
 */
public class OptimisedSuspectPlotPgplot implements PulsarHunterProcess {
    
    PulsarHunterCandidate phcf;
    String format;
    /** Creates a new instance of OptimisedSuspectPlotPgplot */
    public OptimisedSuspectPlotPgplot(PulsarHunterCandidate phcf, String format) {
        this.phcf = phcf;
        this.format = format;
    }
    
    
    public void run(){
        
        PgplotInterface plotter = null;
        while(plotter == null){
            plotter = PgplotInterface.getPlotter();
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        double nprd = 1.5;
        
        boolean needToFitDmcurve = phcf.hasDMCurve();
        boolean squashSubints = false;
        
        
        plotter.jpgopen(format);
        plotter.jpgsch(0.8f);
        drawTitle(plotter);
        plotter.jpgsch(0.75f);
        drawText(plotter);
        plotter.jpgsch(0.5f);
        
        if(phcf.getOptimisedSec().getPulseProfile()!=null && phcf.getOptimisedSec().getPulseProfile().length>0) drawOptimisedProfile(plotter,nprd);
        if(phcf.getInitialSec().getPulseProfile()!=null && phcf.getInitialSec().getPulseProfile().length>0)drawInitialProfile(plotter,nprd);
        
        if (phcf.getInitialSec().getSnrBlock()!=null && phcf.getInitialSec().getSnrBlock().getAccnIndex().length>1 )drawPdotCurve(plotter);
        else if (phcf.getOptimisedSec().getSnrBlock()!=null && phcf.getOptimisedSec().getSnrBlock().getAccnIndex().length>1 )drawPdotCurve(plotter);
        else if(needToFitDmcurve){
            drawDmCurve(plotter,false);
            needToFitDmcurve = false;
        }
        
        if(   phcf.getOptimisedSec().getSnrBlock().getPeriodIndex().length>1
                && phcf.getOptimisedSec().getSnrBlock().getDmIndex().length>1) drawPDM(plotter);
        else if(phcf.getOptimisedSec().getSnrBlock().getPeriodIndex().length>1) drawPeriodCurve(plotter);
        
        if(needToFitDmcurve){
            squashSubints = true;
            drawDmCurve(plotter,true);
        }
        
        if(phcf.getOptimisedSec().getSubints() != null && phcf.getOptimisedSec().getSubints().length>0)drawSubints(plotter,true,nprd, squashSubints,true);
        else if(phcf.getInitialSec().getSubints() != null && phcf.getInitialSec().getSubints().length>0)drawSubints(plotter,true,nprd, squashSubints, false);
        
        if(phcf.getOptimisedSec().getSubbands() != null && phcf.getOptimisedSec().getSubbands().length>0)drawSubbands(plotter,true,nprd);
        plotter.jpgclose();
    }
    
    
    private void drawTitle(PgplotInterface plotter){
        
        plotter.jpgsvp(0.0f,1.0f,0.0f,1.0f);
        
        StringBuffer line = new StringBuffer();
        line.append("File: "+phcf.getFile().getName());
        line.append("   RA:"+phcf.getHeader().getCoord().getRA().toString(false));
        line.append("   Dec:"+phcf.getHeader().getCoord().getDec().toString(false));
        
        line.append("   Gl:"+round(phcf.getHeader().getCoord().getGl(),100));
        line.append("   Gb:"+round(phcf.getHeader().getCoord().getGb(),100));
        line.append("   MJD:"+round(phcf.getHeader().getMjdStart(),100));
        plotter.jpgtext(line.toString(),0.05f,0.95f);
        
        
        line = new StringBuffer();
        
        line.append("ObsFreq:"+round(phcf.getHeader().getFrequency(),10)+"MHz");
        line.append("   Tobs:"+round(phcf.getHeader().getTobs(),1)+"s");
        if(phcf.getInitialSec().getTsamp()>0)line.append("   Tsamp:"+(int)(phcf.getHeader().getTsamp())+"us");
        line.append("   SourceID:"+phcf.getHeader().getSourceID());
        line.append("   Telescope:"+phcf.getHeader().getTelescope().toString());
        
        plotter.jpgtext(line.toString(),0.05f,0.93f);
        
        line = new StringBuffer();
        
        if(phcf.getInitialSec().getExtraValue("SPECSNR")!=null){
            line.append("SpecSNR:"+phcf.getInitialSec().getExtraValue("SPECSNR"));
        }
        
        if(phcf.getInitialSec().getExtraValue("Recon")!=null||phcf.getInitialSec().getExtraValue("RECONSNR")!=null){
            if(phcf.getInitialSec().getExtraValue("RECONSNR")!=null){
                line.append("   ReconSNR:"+round(Double.parseDouble(phcf.getInitialSec().getExtraValue("RECONSNR")),100));
            }else {
                line.append("   ReconSNR:"+round(Double.parseDouble(phcf.getInitialSec().getExtraValue("Recon")),100));
            }
            
        }
        if(phcf.getInitialSec().getExtraValue("HFOLD")!=null){
            line.append("   H-Fold:"+phcf.getInitialSec().getExtraValue("HFOLD"));
        }
        
        if(phcf.getHeader().getExtraValue("ZAP")!=null){
            line.append("   Zap:"+phcf.getHeader().getExtraValue("ZAP"));
        }
        
        plotter.jpgtext(line.toString(),0.05f,0.91f);
        
    }
    
    
    private void drawText(PgplotInterface plotter){
        
        plotter.jpgsvp(0.53f,0.99f,0.48f,0.65f);
        plotter.jpgbox("ABC",0,0,"ABC",0,0);
        
        plotter.jpgtext("Initial",0.35f,0.91f);
        plotter.jpgtext("Optimised",0.66f,0.91f);
        plotter.jpgsch(0.7f);
        
        plotter.jpgtext("Bary Period",0.01f,0.81f);
        plotter.jpgtext("Bary Freq",0.01f,0.72f);
        plotter.jpgtext("Topo Period",0.01f,0.63f);
        plotter.jpgtext("Topo Freq",0.01f,0.54f);
        plotter.jpgtext("Accn",0.01f,0.45f);
        plotter.jpgtext("Jerk",0.01f,0.36f);
        plotter.jpgtext("DM",0.01f,0.27f);
        plotter.jpgtext("Width",0.01f,0.18f);
        plotter.jpgtext("SNR",0.01f,0.09f);
        
        DecimalFormat fmt = new DecimalFormat("0.####E0");
        
        plotter.jpgtext(""+this.round(phcf.getHeader().getInitialBaryPeriod()*1000,1e9),0.27f,0.81f);
        plotter.jpgtext(""+this.round(1.0/phcf.getHeader().getInitialBaryPeriod(),1e9),0.27f,0.72f);
        plotter.jpgtext(""+this.round(phcf.getHeader().getInitialTopoPeriod()*1000,1e9),0.27f,0.63f);
        plotter.jpgtext(""+this.round(1.0/phcf.getHeader().getInitialTopoPeriod(),1e9),0.27f,0.54f);
        if(phcf.getHeader().getInitialAccn() == -1) plotter.jpgtext("N/A",0.27f,0.45f);
        else plotter.jpgtext(fmt.format(phcf.getHeader().getInitialAccn(),new StringBuffer(),new FieldPosition(0)).toString(),0.27f,0.45f);
        if(phcf.getHeader().getInitialJerk() == -1) plotter.jpgtext("N/A",0.27f,0.36f);
        else plotter.jpgtext(fmt.format(phcf.getHeader().getInitialJerk(),new StringBuffer(),new FieldPosition(0)).toString(),0.27f,0.36f);
        plotter.jpgtext(""+this.round(phcf.getHeader().getInitialDm(),100.0),0.27f,0.27f);
        plotter.jpgtext(""+this.round(phcf.getHeader().getInitialWidth(),100.0),0.27f,0.18f);
        plotter.jpgtext(""+this.round(phcf.getHeader().getInitialSNR(),100.0),0.27f,0.09f);
        
        
        plotter.jpgtext(""+this.round(phcf.getHeader().getOptimisedBaryPeriod()*1000,1e9),0.6f,0.81f);
        plotter.jpgtext(""+this.round(1.0/phcf.getHeader().getOptimisedBaryPeriod(),1e9),0.6f,0.72f);
        plotter.jpgtext(""+this.round(phcf.getHeader().getOptimisedTopoPeriod()*1000,1e9),0.6f,0.63f);
        plotter.jpgtext(""+this.round(1.0/phcf.getHeader().getOptimisedTopoPeriod(),1e9),0.6f,0.54f);
        if(phcf.getHeader().getOptimisedAccn() == -1) plotter.jpgtext("N/A",0.6f,0.45f);
        else plotter.jpgtext(fmt.format(phcf.getHeader().getOptimisedAccn(),new StringBuffer(),new FieldPosition(0)).toString(),0.6f,0.45f);
        if(phcf.getHeader().getOptimisedJerk() == -1) plotter.jpgtext("N/A",0.6f,0.36f);
        else plotter.jpgtext(fmt.format(phcf.getHeader().getOptimisedJerk(),new StringBuffer(),new FieldPosition(0)).toString(),0.6f,0.36f);
        plotter.jpgtext(""+this.round(phcf.getHeader().getOptimizedDm(),100.0),0.6f,0.27f);
        plotter.jpgtext(""+this.round(phcf.getHeader().getOptimizedWidth(),100.0),0.6f,0.18f);
        plotter.jpgtext(""+this.round(phcf.getHeader().getOptimizedSNR(),100.0),0.6f,0.09f);
        
        
        plotter.jpgtext("ms",0.9f,0.81f);
        plotter.jpgtext("Hz",0.9f,0.72f);
        plotter.jpgtext("ms",0.9f,0.63f);
        plotter.jpgtext("Hz",0.9f,0.54f);
        plotter.jpgtext("m/s/s",0.9f,0.45f);
        plotter.jpgtext("m/s/s/s",0.9f,0.36f);
        plotter.jpgtext("cm/pc",0.9f,0.27f);
        plotter.jpgtext("period",0.9f,0.18f);
        plotter.jpgtext("",0.9f,0.09f);
        
    }
    
    private void drawDmCurve(PgplotInterface plotter,boolean inSubintPanel){
        
        if(inSubintPanel)plotter.jpgsvp(0.05f,0.49f,0.5f,0.65f);
        else plotter.jpgsvp(0.05f,0.49f,0.74f,0.86f);
        
        
        
        double period = phcf.getInitialSec().getBestTopoPeriod();
        if(phcf.getInitialSec().getSnrBlock().isBarrycenter())period = phcf.getInitialSec().getBestBaryPeriod();
        
        float xVals[] = Convert.toFloatArr(phcf.getInitialSec().getSnrBlock().getDmIndex(),1);
        //float yVals[] = Convert.toFloatArr(phcf.getInitialSec().getSnrBlock().getDmCurve(period,phcf.getInitialSec().getBestAccn(),phcf.getInitialSec().getBestJerk()),1);
        float yVals[] = Convert.toFloatArr(phcf.getInitialSec().getSnrBlock().getFlatDmCurve(),1);
        
        
        
        if(xVals.length != yVals.length)return;
        
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for(float f : yVals){
            if(f > max)max = f;
            if(f < min)min = f;
        }
        
        plotter.jpgswin(xVals[0],xVals[xVals.length-1], min, max);
        
        
        plotter.jpgbox("ABCN",0,0,"ABCN",0,0);
        
        plotter.jpglab("DM", "SNR","DM Curve");
        
        plotter.jpgmove(xVals[0],yVals[0]);
        for(int i = 0; i < xVals.length; i++){
            plotter.jpgdraw(xVals[i],yVals[i]);
            plotter.jpgmove(xVals[i],yVals[i]);
        }
        
    }
    
    private void drawPdotCurve(PgplotInterface plotter){
        
        
        //  plotter.jpgsvp(0.05f,0.49f,0.75f,0.88f);
        plotter.jpgsvp(0.05f,0.49f,0.74f,0.86f);
        double period = 0;
        if(phcf.getOptimisedSec().getSnrBlock() != null) period = phcf.getOptimisedSec().getBestTopoPeriod();
        if(phcf.getInitialSec().getSnrBlock() != null) period = phcf.getInitialSec().getBestTopoPeriod();
        if(phcf.getOptimisedSec().getSnrBlock() != null &&  phcf.getOptimisedSec().getSnrBlock().isBarrycenter())period = phcf.getOptimisedSec().getBestBaryPeriod();
        if(phcf.getInitialSec().getSnrBlock() != null &&  phcf.getInitialSec().getSnrBlock().isBarrycenter())period = phcf.getInitialSec().getBestBaryPeriod();
        
        
        float[] xVals;
        float[] yVals;
        
        
        if(phcf.getInitialSec().getSnrBlock() != null && phcf.getInitialSec().getSnrBlock().getAccnIndex().length > 1){
            
            xVals = Convert.toFloatArr(phcf.getInitialSec().getSnrBlock().getAccnIndex(),1);
            //yVals = Convert.toFloatArr(phcf.getInitialSec().getSnrBlock().getAccnCurve(phcf.getInitialSec().getBestDm(),period,phcf.getInitialSec().getBestJerk()),1);
            yVals = Convert.toFloatArr(phcf.getInitialSec().getSnrBlock().getFlatAccCurve(),1);
            
        } else {
            
            xVals = Convert.toFloatArr(phcf.getOptimisedSec().getSnrBlock().getAccnIndex(),1);
            //yVals = Convert.toFloatArr(phcf.getOptimisedSec().getSnrBlock().getAccnCurve(phcf.getInitialSec().getBestDm(),period,phcf.getInitialSec().getBestJerk()),1);
            yVals = Convert.toFloatArr(phcf.getOptimisedSec().getSnrBlock().getFlatAccCurve(),1);
            
        }
        
        
        
        if(xVals.length != yVals.length)return;
        
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for(float f : yVals){
            if(f > max)max = f;
            if(f < min)min = f;
        }
        
        plotter.jpgswin(xVals[0],xVals[xVals.length-1], min, max);
        
        
        plotter.jpgbox("ABCN",0,0,"ABCN",0,0);
        
        plotter.jpglab("Accn", "SNR","Accn Curve");
        
        plotter.jpgmove(xVals[0],yVals[0]);
        for(int i = 0; i < xVals.length; i++){
            plotter.jpgdraw(xVals[i],yVals[i]);
            plotter.jpgmove(xVals[i],yVals[i]);
        }
        
    }
    
    private void drawPeriodCurve(PgplotInterface plotter){
        
        
        // plotter.jpgsvp(0.53f,0.99f,0.75f,0.88f);
        plotter.jpgsvp(0.53f,0.99f,0.74f,0.86f);
        
        
        
        float[] xVals;
        float[] yVals;
        
        
        if(phcf.getOptimisedSec().getSnrBlock() != null && phcf.getOptimisedSec().getSnrBlock().getPeriodIndex().length > 1){
            
            xVals = Convert.toFloatArr(phcf.getOptimisedSec().getSnrBlock().getPeriodIndex(),1);
            yVals = Convert.toFloatArr(phcf.getOptimisedSec().getSnrBlock().getPeriodCurve(phcf.getOptimisedSec().getBestDm(),phcf.getOptimisedSec().getBestAccn(),phcf.getOptimisedSec().getBestJerk()),1);
        } else {
            
            xVals = Convert.toFloatArr(phcf.getInitialSec().getSnrBlock().getPeriodIndex(),1);
            yVals = Convert.toFloatArr(phcf.getInitialSec().getSnrBlock().getPeriodCurve(phcf.getInitialSec().getBestDm(),phcf.getInitialSec().getBestAccn(),phcf.getInitialSec().getBestJerk()),1);
            
            
        }
        
        
        
        if(xVals.length != yVals.length)return;
        
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for(float f : yVals){
            if(f > max)max = f;
            if(f < min)min = f;
        }
        
        plotter.jpgswin(xVals[0]*1000.0f,xVals[xVals.length-1]*1000.0f, min, max);
        
        
        plotter.jpgbox("ABCN",0,0,"ABCN",0,0);
        
        plotter.jpglab("Period", "SNR","Period Curve");
        
        plotter.jpgmove(xVals[0]*1000.0f,yVals[0]);
        for(int i = 0; i < xVals.length; i++){
            plotter.jpgdraw(xVals[i]*1000.0f,yVals[i]);
            plotter.jpgmove(xVals[i]*1000.0f,yVals[i]);
            
        }
        
    }
    
    
    private String round(double d, double r){
        if(d==-1)return "N/A";
        else return Double.toString(((long)(d*r))/r);
    }
    
    
    private void drawSubints(PgplotInterface plotter,boolean quickgray,double nprd, boolean squash,boolean optimised){
        
        double[][] dArr = null;
        if(optimised) dArr = phcf.getOptimisedSec().getSubints();
        else dArr = phcf.getOptimisedSec().getSubints();
        boolean normalise = true;
        if(normalise){
            for(double[] arr : dArr){
                double max = -Double.MAX_VALUE;
                double min = Double.MAX_VALUE;
                for(double d : arr){
                    if(d < min)min = d;
                    if(d > max)max = d;
                }
                for(int i = 0; i < arr.length; i++){
                    arr[i] = (arr[i]-min)/(max-min);
                }
            }
            
            
        }
        
        
        float[] subints = Convert.toFortranFloatArrSwap(dArr,nprd);
        
        int nbins = (int)(dArr[0].length*nprd);
        int nsubints = dArr.length;
        
        
        if(squash)plotter.jpgsvp(0.05f,0.49f,0.30f,0.45f);
        else plotter.jpgsvp(0.05f,0.49f,0.30f,0.65f);
        
        
        if(quickgray){
            
            plotter.jpgswin(0,((float)nbins),0,(float)nsubints);
            
            plotter.quickgray(subints,nbins,nsubints,nbins);
            
        }else{
            float max1 = -Float.MAX_VALUE;
            float min1 = Float.MAX_VALUE;
            for(float f : subints){
                if(f > max1)max1 = f;
                if(f < min1)min1 = f;
                
            }
            float max = -Float.MAX_VALUE;
            float min = Float.MAX_VALUE;
            
            for(int i = 0; i < subints.length; i++){
                
                float f = (subints[i]-min1)/(max1-min1);
                f *= f;
                subints[i] = f;
                if(f > max)max = f;
                if(f < min)min = f;
            }
            
            
            plotter.jpgswin(0, (float)nbins, 0, (float)nsubints);
            float[] area = plotter.jpgqwin();
            float x1 = area[0];
            float x2 = area[1];
            float y1 = area[2];
            float y2 = area[3];
            
            float xscale = ( x2 - x1 ) / nbins;
            float yscale = ( y2 - y1 ) / nsubints;
            float scale = ( xscale < yscale ) ? xscale : yscale;
            
            
            float xleft   = 0.5f * ( x1 + x2 - nbins * scale );
            float xright  = 0.5f * ( x1 + x2 + nbins * scale );
            float ybottom = 0.5f * ( y1 + y2 - nsubints * scale );
            float ytop    = 0.5f * ( y1 + y2 + nsubints * scale );
            
            float[] tr = new float[6];
            
            tr[0] = xleft - 0.5f * scale;
            tr[1] = scale;
            tr[2] = 0.0f;
            tr[3] = ybottom - 0.5f * scale;
            tr[4] = 0.0f;
            tr[5] = scale;
            
            
            
            
            plotter.jpgswin(0.0f,((float)nbins),0.0f,(float)nsubints);
            
            plotter.jpggray(subints,nbins,nsubints,1,nbins,1,nsubints,max,min,tr);
            
        }
        
        
        plotter.jpgbox("ABCN",0,0,"ABCN",0,0);
        
        if(optimised)plotter.jpglab("Bin", "Time","Optimised Subints");
        else plotter.jpglab("Bin", "Time","Initial Subints");
    }
    
    private void drawSubbands(PgplotInterface plotter,boolean quickgray,double nprd){
        
        double[][] dArr = phcf.getOptimisedSec().getSubbands();
        
        
        float[] subbands = Convert.toFortranFloatArr(dArr,nprd);
        
        int nbins = (int)(dArr.length*nprd);
        int nsubbands = dArr[0].length;
        
        
        plotter.jpgsvp(0.53f,0.99f,0.30f,0.44f);
        
        
        if(quickgray){
            
            plotter.jpgswin(0,((float)nbins),1,(float)nsubbands);
            
            plotter.quickgray(subbands,nbins,nsubbands,nbins);
            
        }else{
            float max1 = Float.MIN_VALUE;
            float min1 = Float.MAX_VALUE;
            for(float f : subbands){
                if(f > max1)max1 = f;
                if(f < min1)min1 = f;
                
            }
            float max = Float.MIN_VALUE;
            float min = Float.MAX_VALUE;
            
            for(int i = 0; i < subbands.length; i++){
                
                float f = (subbands[i]-min1)/(max1-min1);
                f *= f;
                subbands[i] = f;
                if(f > max)max = f;
                if(f < min)min = f;
            }
            
            
            plotter.jpgswin(0, (float)nbins, 0, (float)nsubbands);
            float[] area = plotter.jpgqwin();
            float x1 = area[0];
            float x2 = area[1];
            float y1 = area[2];
            float y2 = area[3];
            
            float xscale = ( x2 - x1 ) / nbins;
            float yscale = ( y2 - y1 ) / nsubbands;
            float scale = ( xscale < yscale ) ? xscale : yscale;
            
            
            float xleft   = 0.5f * ( x1 + x2 - nbins * scale );
            float xright  = 0.5f * ( x1 + x2 + nbins * scale );
            float ybottom = 0.5f * ( y1 + y2 - nsubbands * scale );
            float ytop    = 0.5f * ( y1 + y2 + nsubbands * scale );
            
            float[] tr = new float[6];
            
            tr[0] = xleft - 0.5f * scale;
            tr[1] = scale;
            tr[2] = 0.0f;
            tr[3] = ybottom - 0.5f * scale;
            tr[4] = 0.0f;
            tr[5] = scale;
            
            
            
            
            plotter.jpgswin(0.0f,((float)nbins),0.0f,(float)nsubbands);
            
            plotter.jpggray(subbands,nbins,nsubbands,1,nbins,1,nsubbands,max,min,tr);
            
        }
        
        
        plotter.jpgbox("ABCN",0,0,"ABCN",0,0);
        
        plotter.jpglab("Bin", "Frequency","Optimised SubBands");
        
    }
    
    
    
    private void drawOptimisedProfile(PgplotInterface plotter,double nprd){
        
        float[] prof = Convert.toFloatArr(phcf.getOptimisedSec().getPulseProfile(),nprd);
        
        
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for(float f : prof){
            if(f > max)max = f;
            if(f < min)min = f;
        }
        plotter.jpgsvp(0.53f,0.99f,0.05f,0.2f);
        
        plotter.jpgswin(0.0f, (float)(prof.length/prof.length*nprd), min, max);
        
        plotter.jpgbox("ABCSN",0,0,"ABCN",0,0);
        
        plotter.jpglab("Phase", "Intensity","Optimised Profile");
        
        plotter.jpgmove(0.0f,prof[prof.length-1]);
        for(int i = 0; i < prof.length; i++){
            plotter.jpgdraw((float)((float)i/(float)prof.length*nprd),prof[i]);
            plotter.jpgmove((float)((float)i/(float)prof.length*nprd),prof[i]);
            
        }
        
        
        
    }
    
    private void drawInitialProfile(PgplotInterface plotter, double nprd){
        
        float[] prof = Convert.toFloatArr(phcf.getInitialSec().getPulseProfile(),nprd);
        
        
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for(float f : prof){
            if(f > max)max = f;
            if(f < min)min = f;
        }
        plotter.jpgsvp(0.05f,0.49f,0.05f,0.2f);
        
        plotter.jpgswin(0.0f, (float)nprd, min, max);
        
        plotter.jpgbox("ABCSN",0,0,"ABCN",0,0);
        
        plotter.jpglab("Phase", "Intensity","Initial Profile");
        
        plotter.jpgmove(0.0f,prof[prof.length-1]);
        for(int i = 0; i < prof.length; i++){
            plotter.jpgdraw((float)((float)i/(float)prof.length*nprd),prof[i]);
            plotter.jpgmove((float)((float)i/(float)prof.length*nprd),prof[i]);
            
        }
        
        
        
    }
    private void drawPDM(PgplotInterface plotter){
        
        double[][] dArr = phcf.getOptimisedSec().getSnrBlock().getPDmPlane(phcf.getOptimisedSec().getBestAccn(),phcf.getOptimisedSec().getBestJerk());
        float[] pdm = Convert.toFortranFloatArr(dArr,1.0);
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for(float f : pdm){
            if(f > max)max = f;
            if(f < min)min = f;
        }
        
        /*double periodStep = 2.0*phcf.getOptimisedSec().getSnrBlock().getPeriodIndex()[0]/phcf.getOptimisedSec().getSnrBlock().getPeriodIndex().length;
         
        double dmStep = 2.0*phcf.getOptimisedSec().getSnrBlock().getDmIndex()[0]/phcf.getOptimisedSec().getSnrBlock().getDmIndex().length;
         */
        float np = (float)dArr.length;
        float ndm = (float)dArr[0].length;
        
        //  plotter.jpgsvp(0.53f,0.99f,0.75f,0.88f);
        plotter.jpgsvp(0.53f,0.99f,0.74f,0.86f);
        
        
        /*plotter.jpgswin((float)(-(np/2.0)*periodStep*1000.0),
                (float)((np/2.0)*periodStep*1000),
                (float)(-(ndm/2.0)*dmStep+phcf.getHeader().getInitialDm()),
                (float)((ndm/2.0)*dmStep+phcf.getHeader().getInitialDm()));*/
        
        plotter.jpgswin((float)(phcf.getOptimisedSec().getSnrBlock().getPeriodIndex()[0]*1000.0),
                (float)(phcf.getOptimisedSec().getSnrBlock().getPeriodIndex()[dArr.length-1]*1000.0),
                (float)(phcf.getOptimisedSec().getSnrBlock().getDmIndex()[0]),
                (float)(phcf.getOptimisedSec().getSnrBlock().getDmIndex()[dArr[0].length-1])
                );
        
        
        
        float[] area = plotter.jpgqwin();
        float x1 = area[0];
        float x2 = area[1];
        float y1 = area[2];
        float y2 = area[3];
        
        
        float xscale = ( x2 - x1 ) / np ;
        float yscale = ( y2 - y1 ) / ndm;
        float scale = ( xscale < yscale ) ? xscale : yscale;
        
        
        float xleft   = 0.5f * ( x1 + x2 - np * xscale );
        float xright  = 0.5f * ( x1 + x2 + np * xscale );
        float ybottom = 0.5f * ( y1 + y2 - ndm * yscale );
        float ytop    = 0.5f * ( y1 + y2 + ndm * yscale );
        
        float[] tr = new float[6];
        
        tr[0] = xleft - 0.5f * xscale;
        tr[1] = xscale;
        tr[2] = 0.0f;
        tr[3] = ybottom - 0.5f * yscale;
        tr[4] = 0.0f;
        tr[5] = yscale;
        
        
        
        plotter.jpglab("Period (ms)", "DM","P-DM Plane");
        
        plotter.jpggray(pdm,(int)np,(int)ndm,1,(int)np,1,(int)ndm,max,min,tr);
        
        // plotter.quickgray(ppdot,(int)np,(int)npdot,(int)np);
        
        plotter.jpgbox("ABCN",0,0,"ABCN",0,0);
        
    }
    
   /* private void drawPPdot(PgplotInterface plotter){
    
        double[][] dArr = phcf.getPPdotPlane();
        float[] ppdot = Convert.toFortranFloatArr(dArr,1.0);
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for(float f : ppdot){
            if(f > max)max = f;
            if(f < min)min = f;
        }
    
        float np = (float)dArr.length;
        float npdot = (float)dArr[0].length;
    
        plotter.jpgsvp(0.05f,0.49f,0.75f,0.88f);
        plotter.jpgswin((float)(-(np/2.0)*phcf.getHeader().getPeriodStep()*1000.0),
                (float)((np/2.0)*phcf.getHeader().getPeriodStep()*1000.0),
                (float)(-(npdot/2.0)*phcf.getHeader().getPdotStep()),
                (float)((npdot/2.0)*phcf.getHeader().getPdotStep()));
        //plotter.jpgswin(0,np,0,npdot);
    
    
        float[] area = plotter.jpgqwin();
        float x1 = area[0];
        float x2 = area[1];
        float y1 = area[2];
        float y2 = area[3];
    
    
        float xscale = ( x2 - x1 ) / np ;
        float yscale = ( y2 - y1 ) / npdot;
        float scale = ( xscale < yscale ) ? xscale : yscale;
    
    
        float xleft   = 0.5f * ( x1 + x2 - np * xscale );
        float xright  = 0.5f * ( x1 + x2 + np * xscale );
        float ybottom = 0.5f * ( y1 + y2 - npdot * yscale );
        float ytop    = 0.5f * ( y1 + y2 + npdot * yscale );
    
        float[] tr = new float[6];
    
        tr[0] = xleft - 0.5f * xscale;
        tr[1] = xscale;
        tr[2] = 0.0f;
        tr[3] = ybottom - 0.5f * yscale;
        tr[4] = 0.0f;
        tr[5] = yscale;
    
    
    
        plotter.jpglab("Period Offset (ms)", "Pdot Offset","P-Pdot Plane");
    
        plotter.jpggray(ppdot,(int)np,(int)npdot,1,(int)np,1,(int)npdot,max,min,tr);
    
        // plotter.quickgray(ppdot,(int)np,(int)npdot,(int)np);
    
        plotter.jpgbox("ABCN",0,0,"ABCN",0,0);
    
    }
    */
    
    
    
    
}

