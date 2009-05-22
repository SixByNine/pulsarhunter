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
 * PeriodTuneFold.java
 *
 * Created on 25 October 2006, 15:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import java.util.Arrays;
import pulsarhunter.BarryCenter;
import pulsarhunter.Convert;
import pulsarhunter.PulsarHunter;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.datatypes.BulkReadable;
import pulsarhunter.datatypes.DummyTimeSeries;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.PHCSection;
import pulsarhunter.datatypes.PolyCoFile;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.datatypes.SNRBlock;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.displaypanels.TuneDisplayFrame;

/**
 *
 * @author mkeith
 */
public class PeriodTuneFold implements PulsarHunterProcess{
    private TimeSeries[] timeSeries;
    private MultiChannelTimeSeries multiTS = null;
    private PolyCoFile polyco = null;
    private PeriodTuneFoldParams params;
    private double dopp = -1;
    private boolean multiFold = false;
    
    private boolean interactive = false;
    
    private PulsarHunterCandidate phcfResult;
    private double[] dmIndex;
    private double[] periodIndex;
    private double[] accnIndex;
    private double[] jerkIndex;
    private double[][][][] snrBlock;
    
    
    private class StackResult{
        
       /* private double[][] ppdotPlane;
        private double[][] pdotpddotPlane;
        private double[][][] snrPlane;
        private int bestPdotRow;
        private int bestPddotRow;
        private int bestPRow;*/
        private double[] bestProfile;
        
        private double bestPeriod;
        private double bestAccn;
        private double bestJerk;
        private double bestSnr;
        private double bestWidth;
        
        private double bestPFactor;
        private double bestPdotFactor;
        private double bestPddotFactor;
        
        
        public double getBestPeriod() {
            return bestPeriod;
        }
        
        public void setBestPeriod(double bestPeriod) {
            this.bestPeriod = bestPeriod;
        }
        
        
        public double getBestWidth() {
            return bestWidth;
        }
        
        public void setBestWidth(double bestWidth) {
            this.bestWidth = bestWidth;
        }
        
        public double getBestSnr() {
            return bestSnr;
        }
        
        public void setBestSnr(double bestSnr) {
            this.bestSnr = bestSnr;
        }
        
        
        public double[] getBestProfile() {
            return bestProfile;
        }
        
        public void setBestProfile(double[] bestProfile) {
            this.bestProfile = bestProfile;
        }
        
        public double getBestAccn() {
            return bestAccn;
        }
        
        public void setBestAccn(double bestAccn) {
            this.bestAccn = bestAccn;
        }
        
        public double getBestJerk() {
            return bestJerk;
        }
        
        public void setBestJerk(double bestJerk) {
            this.bestJerk = bestJerk;
        }
        
        public double getBestPFactor() {
            return bestPFactor;
        }
        
        public void setBestPFactor(double bestPFactor) {
            this.bestPFactor = bestPFactor;
        }
        
        public double getBestPdotFactor() {
            return bestPdotFactor;
        }
        
        public void setBestPdotFactor(double bestPdotFactor) {
            this.bestPdotFactor = bestPdotFactor;
        }
        
        public double getBestPddotFactor() {
            return bestPddotFactor;
        }
        
        public void setBestPddotFactor(double bestPddotFactor) {
            this.bestPddotFactor = bestPddotFactor;
        }
        
    }
    
    public PeriodTuneFold(MultiChannelTimeSeries timeSeries,PeriodTuneFoldParams params,PulsarHunterCandidate phcfResult,boolean interactive) {
        this(timeSeries,params,phcfResult,interactive,null);
    }
    public PeriodTuneFold(MultiChannelTimeSeries timeSeries,PeriodTuneFoldParams params,PulsarHunterCandidate phcfResult,boolean interactive,PolyCoFile pcf) {
        
        this.polyco=pcf;
        this.interactive = interactive;
        if(timeSeries.getHeader().isChannelInterleaved()){
            multiFold = true;
            System.out.println("PeriodTune - Using MultiFold for interleaved channels");
        }
        // multiFold = false; //TEST TESTTESTTEST
        this.params = params;
        this.timeSeries = new TimeSeries[timeSeries.getHeader().getNumChannel()];
        multiTS = timeSeries;
        
        for(int i = 0; i < timeSeries.getHeader().getNumChannel(); i++){
            boolean ignore = false;
            for(int j : params.getIgnoreBands()){
                if(i==j)ignore =  true;
            }
            if(!ignore){
                this.timeSeries[i] = timeSeries.getOnechannel(i);
                
            }else{
                PulsarHunter.out.println("Ignoring channel "+i);
                this.timeSeries[i] = new DummyTimeSeries(timeSeries.getOnechannel(i));
            }
            
        }
        
        this.phcfResult = phcfResult;
        
        if(BarryCenter.isAvaliable()){
            BarryCenter bc = new BarryCenter(timeSeries.getHeader().getMjdStart(),
                    timeSeries.getHeader().getTelescope(),
                    timeSeries.getHeader().getCoord().getRA().toDegrees(),
                    timeSeries.getHeader().getCoord().getDec().toDegrees());
            
            dopp = bc.getDopplerFactor();
            
        }
        
        
        
        
        
      /*  this.phcfResult.getHeader().setSourceID(timeSeries.getHeader().getSourceID());
        this.phcfResult.getHeader().setBandwidth(timeSeries.getHeader().getBandwidth());
        this.phcfResult.getHeader().setFrequency(timeSeries.getHeader().getFrequency());
        this.phcfResult.getHeader().setCoord(timeSeries.getHeader().getCoord());
        this.phcfResult.getHeader().setMjdStart(timeSeries.getHeader().getMjdStart());
        this.phcfResult.getHeader().setTobs(timeSeries.getHeader().getTobs());
        this.phcfResult.getHeader().setTelescope(timeSeries.getHeader().getTelescope());
       
        this.phcfResult.getHeader().setInitialPdot(params.getCentrePdot());
        this.phcfResult.getHeader().setInitialPddot(params.getCentrePddot());
       
        if(timeSeries.getHeader().isBarryCentered()){
            this.phcfResult.getHeader().setInitialBaryPeriod(params.getCentrePeriod());
            if(dopp > 0){
                this.phcfResult.getHeader().setInitialTopoPeriod(params.getCentrePeriod()*dopp);
            }
        } else {
            this.phcfResult.getHeader().setInitialTopoPeriod(params.getCentrePeriod());
            if(dopp > 0){
                this.phcfResult.getHeader().setInitialBaryPeriod(params.getCentrePeriod()/dopp);
            }
        }
        this.phcfResult.getHeader().setInitialDm(params.getCenterDM());
       */
    }
    
    
    
    
    /** Creates a new instance of PeriodTuneFold */
    public PeriodTuneFold(TimeSeries timeSeries,PeriodTuneFoldParams params,PulsarHunterCandidate phcfResult,boolean interactive) {
        this(timeSeries,params,phcfResult,interactive,null);
    }
    public PeriodTuneFold(TimeSeries timeSeries,PeriodTuneFoldParams params,PulsarHunterCandidate phcfResult,boolean interactive,PolyCoFile pcf) {
        this.polyco = pcf;
        this.interactive = interactive;
        this.params = params;
        this.timeSeries = new TimeSeries[]{timeSeries};
        this.phcfResult = phcfResult;
        
        if(BarryCenter.isAvaliable()){
            BarryCenter bc = new BarryCenter(timeSeries.getHeader().getMjdStart(),
                    timeSeries.getHeader().getTelescope(),
                    timeSeries.getHeader().getCoord().getRA().toDegrees(),
                    timeSeries.getHeader().getCoord().getDec().toDegrees());
            
            dopp = bc.getDopplerFactor();
            
        }
        
        
        
      /*  this.phcfResult.getHeader().setSourceID(timeSeries.getHeader().getSourceID());
        this.phcfResult.getHeader().setBandwidth(timeSeries.getHeader().getBandwidth());
        this.phcfResult.getHeader().setFrequency(timeSeries.getHeader().getFrequency());
        this.phcfResult.getHeader().setCoord(timeSeries.getHeader().getCoord());
        this.phcfResult.getHeader().setMjdStart(timeSeries.getHeader().getMjdStart());
        this.phcfResult.getHeader().setTobs(timeSeries.getHeader().getTobs());
        this.phcfResult.getHeader().setTelescope(timeSeries.getHeader().getTelescope());
       
        this.phcfResult.getHeader().setInitialPdot(params.getCentrePdot());
       
        if(timeSeries.getHeader().isBarryCentered()){
            this.phcfResult.getHeader().setInitialBaryPeriod(params.getCentrePeriod());
            if(dopp > 0){
                this.phcfResult.getHeader().setInitialTopoPeriod(params.getCentrePeriod()*dopp);
            }
        } else {
            this.phcfResult.getHeader().setInitialTopoPeriod(params.getCentrePeriod());
            if(dopp > 0){
                this.phcfResult.getHeader().setInitialBaryPeriod(params.getCentrePeriod()/dopp);
            }
        }
        this.phcfResult.getHeader().setInitialDm(params.getCenterDM());
       */
        
        
    }
    
    public void run() {
        
        
        
        // PulsarHunter.out.println("Starting PeriodTune...");
        
        // Get the rms in the first 10000 bins
        double rms = 0;
        double[] rmsInBand = new double[timeSeries.length];
        double[][][] bandedSubints  = new double[timeSeries.length][][];
        // multiFold = false;
        
        PulsarHunter.out.println("PeriodTune - Computing RMS noise");
        for(int b = 0; b < timeSeries.length; b++){
            double rmstmp = this.timeSeries[b].getRMS(0,10000);
            rms += rmstmp*rmstmp;
            //  rmsInBand[b] = rmstmp;
            
        }
        bandedSubints = makeBandedSubints();
        
        
        // root rms sum of squares.
        rms = Math.sqrt(rms);
//	System.out.println("RMS in data is:"+rms);
        // for(int b = 0; b < timeSeries.length; b++){
        //    System.out.println("RMS in band "+b+": "+rmsInBand[b]);
        // }
        
        
        //double[][] sints = new double[bandedSubints[0].length][bandedSubints[0][0].length];
        
        for(int i : params.getIgnoreSints()){
            for(int b = 0; b < bandedSubints.length; b++){
                for(int j = 0; j < bandedSubints[0][0].length; j++){
                    bandedSubints[b][i][j] = 0;
                }
            }
        }
        
        if(params.isIgnoreLoudSubbands()){
            
            double[] means = new double[bandedSubints[0].length];
            double[] rmsSints = new double[bandedSubints[0].length];
            for(int i = 0; i < bandedSubints[0].length; i++){
                double sum = 0;
                double squ = 0;
                for(int b = 0; b < bandedSubints.length; b++){
                    for(int j = 0; j < bandedSubints[0][0].length; j++){
                        sum += bandedSubints[b][i][j];
                        squ += bandedSubints[b][i][j]*bandedSubints[b][i][j];
                    }
                }
                means[i] = sum / (bandedSubints.length)*(bandedSubints[0][0].length);
                rmsSints[i] = Math.sqrt(squ / (bandedSubints.length)*(bandedSubints[0][0].length));
                //System.out.println("Mean si "+i+": "+mean);
            }
            double meanMeans = 0;
            for(double d : means)meanMeans+=d;
            meanMeans /= bandedSubints[0].length;
            
            double meanRMSs = 0;
            for(double d : rmsSints)meanRMSs+=d;
            meanRMSs /= bandedSubints[0].length;
            
            for(int i = 0; i < bandedSubints[0].length; i++){
                
/*            if(Math.sqrt(means[i]/meanMeans) > (params.getSintIgnoreThreashold()) || Math.sqrt(means[i]/meanMeans) < (1.0/params.getSintIgnoreThreashold())){
                PulsarHunter.out.println("SubInt "+i+": Mean power is "+(means[i]/meanMeans)+" times the mean. Ignoring");
                double p = meanMeans / (bandedSubints[0][0].length*bandedSubints.length);
                for(int b = 0; b < bandedSubints.length; b++){
                    for(int j = 0; j < bandedSubints[0][0].length; j++){
                        bandedSubints[b][i][j] = 0;
                    }
                }
            }*/
                
                if((rmsSints[i]/meanRMSs) > params.getSintIgnoreThreashold()){
                    PulsarHunter.out.println("SubInt "+i+": RMS power is "+(rmsSints[i]/meanRMSs)+" times the mean rms. Ignoring");
                    double p = meanRMSs / (bandedSubints[0][0].length*bandedSubints.length);
                    for(int b = 0; b < bandedSubints.length; b++){
                        for(int j = 0; j < bandedSubints[0][0].length; j++){
                            bandedSubints[b][i][j] = 0;
                        }
                    }
                }
            }
            
            
        }
        
        int nbin = bandedSubints[0][0].length;
        int nsub = bandedSubints[0].length;
        double numFoldsPerSubint = (timeSeries[0].getHeader().getTobs()/params.getCentrePeriod())/params.getNsub();
        double npulsebin = params.getCentrePeriod()/timeSeries[0].getHeader().getTSamp();
        
        double bestDm = 0.0;
        
        
        // The rms in a subint
        
        double rmss = rms*Math.sqrt(numFoldsPerSubint*(npulsebin/nbin));
        //System.out.println("RMS: "+rms+" RMSS:"+rmss);
        
        
        
        
        
        //double dmcoursness = 1.0/(double)bandedSubints.length*2;
        //dmcoursness*=params.getDmcoarseness();
        
        
        
        double binWidth = params.getCentrePeriod()/(double)nbin;
        
        // double deltaT = dmcoursness * bandedSubints.length * binWidth;
        
        double flo = timeSeries[0].getHeader().getFrequency()/1000.0;//GHz
        double fhi = timeSeries[timeSeries.length-1].getHeader().getFrequency()/1000.0;//GHz
        if(flo > fhi){
            double fswap = fhi;
            fhi = flo;
            flo = fswap;
        }
        final double kdm = 4.148808e-3;//s
        double minDdm = binWidth / (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi)));
        
        double deltaDm = params.getDmstep();
        
        if(deltaDm < minDdm){
            params.setDmrange((minDdm/deltaDm)*params.getDmrange());
            deltaDm = minDdm;
            System.out.println("PeriodTune - Dm Step below min threashold: reset to "+deltaDm);
            params.setDmstep(deltaDm);
        }
        
        
        System.out.println("PeriodTune - Dm Step "+deltaDm);
        double dmcoursness = (deltaDm *  (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi))))
        /
                (bandedSubints.length * binWidth);
        /*
        if(bandedSubints.length > 1){
            boolean fixedDmstep = false;
            while((int)(dmcoursness*bandedSubints.length/4.0) < 1){
                params.setDmstep(deltaDm*2.0);
                params.setDmrange(params.getDmrange()*2.0);
                deltaDm = params.getDmstep();
         
                dmcoursness = (deltaDm *  (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi))))
                /
                        (bandedSubints.length * binWidth);
                fixedDmstep = true;
            }
         
            if(fixedDmstep) System.out.println("PeriodTune - Dm Step below min threashold: reset to "+params.getDmstep());
        }
         **/
//        System.out.println("DMc "+dmcoursness);
//        System.out.println("DeltaDM "+deltaDm);
        
        StackResult srBest = null;
        
        int ndms = (int)(params.getDmrange()/deltaDm);
        
        StackResult[] results = new StackResult[2*ndms+1];
        double[][] bestSubints = null;
        
        
        
        
        
        
        if(timeSeries.length == 1){
            ndms = 0;
            deltaDm = 0;
        }
        
        
        dmIndex = new double[2*ndms+1];
        int d = 0;
        for(int dms = -ndms; dms <= ndms; dms++){
            dmIndex[d++] = params.getCenterDM() + dms*deltaDm;
            //      System.out.println(dmIndex[d-1]);
        }
        
        
        
        
        int dmStep = 0;
        
//        double timeOffset = params.getCenterDM() * (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi)));
        
        //      double coreDmOffset = (timeOffset/params.getCentrePeriod()) * nbin / timeSeries.length;
        
        
        // coreDmOffset = 53.96*1.14;
        
        
        PulsarHunter.out.println("PeriodTune - Dedispersing subbands");
        for(int b = 0; b < bandedSubints.length; b++){
            //double b2 = (double)b - bandedSubints.length/2.0;
            
            double fch = timeSeries[b].getHeader().getFrequency()/1000.0;
            double timeOffset = params.getCenterDM() * (kdm*(1.0/(fch*fch) - 1.0/(fhi*fhi)));
            
            double coreDmOffset = (timeOffset/params.getCentrePeriod()) * nbin;
            //System.out.println("flo:"+fch+" fhi:"+fhi+" DM:"+params.getCenterDM()+"  coreDmOffset="+coreDmOffset+"  t:"+timeOffset);
            
            int ptr = (int)((coreDmOffset));

            for(int i = 0; i < nsub; i++){
                
                double[] tmpArr = new double[nbin];
                
                System.arraycopy(bandedSubints[b][i],0,tmpArr,0,nbin);
                for (int j = 0; j < nbin; j++){
                    while(ptr >= nbin)ptr-=nbin;
                    while(ptr < 0)ptr+=nbin;
                    
                    bandedSubints[b][i][j] = tmpArr[ptr];
                    if(b==0 && i==0 && j==0)System.out.println(bandedSubints[b][i][j]);
                    ptr++;
                }
            }
        }
        
        if(interactive){
            TuneDisplayFrame frame = new TuneDisplayFrame(bandedSubints,params);
            frame.setVisible(true);
            frame.waitToTerminate();
        }
        
        
        PulsarHunter.out.println("PeriodTune - "+(2*ndms+1)+" DM trials...");
        
        for(int dms = -ndms; dms <= ndms; dms++){
            
            double dm = dms;
            double dmf = dms*dmcoursness;
            PulsarHunter.out.println("PeriodTune - DM "+(dms*deltaDm+this.params.getCenterDM()));
            double[][] subints = dedisperseSints(dmf, bandedSubints);
            
            
            //PulsarHunter.out.println("PeriodTune - Stacking Subints");
            
            
            StackResult stackResult = this.stackSubints(subints,params.getCentrePeriod(), params.getCenterAccn(),params.getCenterJerk(), params.isUseAccn(),params.isUseJerk(),rmss,this.timeSeries[0].getHeader().getTobs(),params.getPstep(),params.getPrange(),params.getAccnStep(),params.getAccnRange(), params.getJerkStep(),params.getJerkRange(),dmStep);
            
            results[dmStep] = stackResult;
            
            //System.out.println("P: "+stackResult.getBestPeriod()+"\nPD:"+stackResult.getBestPdot()+"\nW: "+stackResult.getBestWidth()+"\nS: "+stackResult.getBestSnr());
            
            if(srBest == null || srBest.getBestSnr() < stackResult.getBestSnr()){
                srBest = stackResult;
                bestSubints = subints;
                bestDm = dmf;
                // System.out.println("DMF: "+dmf);
            }
            dmStep++;
        }
        
        
        
        /*
         * Generate the output file!
         *
         *
         *
         */
        
        
        // Create a new section for the file.
        String sectionName="PH-Tune";
        
        if(phcfResult.getOptimisedSec() != null) sectionName = phcfResult.getOptimisedSec().getName()+"-Tuned";
        
        PHCSection section = new PHCSection(sectionName);
        
        StackResult stackResult = srBest;
        
        
        
        // Do we have subbands, if so, add them!
        
        
        if(timeSeries.length == 1){
            bestDm = -1;
        } else {
            
            
            
            
            // compute subband plot
            double[][] sbands = new double[bandedSubints.length][nbin];
            double[][] oldSbands = new double[bandedSubints.length][nbin];
            for(int i =0; i < oldSbands.length; i++)Arrays.fill(oldSbands[i],0);
            for(int i =0; i < sbands.length; i++)Arrays.fill(sbands[i],0);
            
            
            
            for(int dd = 0; dd < bandedSubints.length ; dd++){
                // for(int i =0; i < sbands.length; i++){
                
                double b2 = (double)dd - bandedSubints.length/2.0;
                
                int ddptr = (int)((bestDm)*b2);
                
                
                
                
                
                
                double s2 = -(bandedSubints[dd].length/2.0);
                
                for(int s = 0; s < bandedSubints[dd].length; s++){
                    s2 += 1;
                    int pptr = (int)(stackResult.getBestPFactor()*s2 + stackResult.getBestPdotFactor()*s2*s2 + stackResult.getBestPddotFactor()*s2*s2*s2);
                    int ptr = ddptr + pptr;
                    if(ptr >= nbin)ptr-=nbin;
                    while(ptr < 0)ptr+=nbin;
                    while(pptr < 0)pptr+=nbin;
                    
                    for(int i = 0; i < nbin; i++){
                        
                        while(ptr >= nbin)ptr-=nbin;
                        while(pptr >= nbin)pptr-=nbin;
                        try {
                            sbands[dd][i]+=bandedSubints[dd][s][ptr];
                            
                            oldSbands[dd][i]+=bandedSubints[dd][s][pptr];
                            
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println("Ran out of bounds while making sub-band plot "+ddptr);
                            System.exit(1);
                        }
                        ptr++;
                        pptr++;
                    }
                    
                    
                }
                
                
                
//                    for(int s = 0; s < bandedSubints[dd].length; s++){
//                        sbands[i][dd] += bandedSubints[dd][s][i];
//                    }
//                }
                
            }
            // Now make bestDm have the best dm...
            double deltaT = bestDm * bandedSubints.length * binWidth;
            
            deltaDm = deltaT / (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi)));
            
            bestDm = this.params.getCenterDM() + deltaDm;
            
            
            
            // normalise
            for(int i =0; i < sbands.length; i++){
                double max = -Double.MAX_VALUE;
                double min = Double.MAX_VALUE;
                for(int j =0; j < sbands[0].length; j++){
                    
                    double val = sbands[i][j];
                    if(val > max)max = val;
                    if(val < min)min = val;
                }
                if(max!=min){
                    for(int j =0; j < sbands[0].length; j++){
                        sbands[i][j] = (sbands[i][j] - min)/(max-min);
                    }
                }
            }
            for(int i =0; i < oldSbands.length; i++){
                double max = -Double.MAX_VALUE;
                double min = Double.MAX_VALUE;
                for(int j =0; j < oldSbands[0].length; j++){
                    
                    double val = oldSbands[i][j];
                    if(val > max)max = val;
                    if(val < min)min = val;
                }
                if(max!=min){
                    for(int j =0; j < oldSbands[0].length; j++){
                        oldSbands[i][j] = (oldSbands[i][j] - min)/(max-min);
                    }
                }
            }
            
            
            section.setSubbands(sbands);
            if(phcfResult.getInitialSec() != null && phcfResult.getInitialSec().getSubbands() == null){
                phcfResult.getInitialSec().setSubbands(oldSbands);
            }
        }
        
        
        //PulsarHunter.out.println("PeriodTune - Folding Final Profile");
        //double epoch = timeSeries.getHeader().getMjdStart() + Convert.secToMJD(timeSeries.getHeader().getObstime())/2.0;
        
        //TimeSeriesFolder folder = new TimeSeriesFolder(new PdotFoldingModel(stackResult.getBestPeriod(),stackResult.getBestPdot(),epoch,timeSeries.getHeader().getTSamp()));
        //double[] newProf = folder.fold(timeSeries,nbin);
        
        if(phcfResult.getInitialSec() != null && phcfResult.getInitialSec().getPulseProfile() == null){
            
            double[] oldProf = new double[nbin];
            Arrays.fill(oldProf,0);
            for(double[][] subints : bandedSubints){
                for(double[] subint : subints){
                    for(int i = 0; i < oldProf.length; i++){
                        oldProf[i] += subint[i];
                    }
                }
            }
            phcfResult.getInitialSec().setPulseProfile(oldProf);
            
            
            if(this.params.getRecalcInitialSNR()){
                phcfResult.getInitialSec().setBestSnr(this.snrBlock[(snrBlock.length-1)/2][(snrBlock[0].length-1)/2][(snrBlock[0][0].length-1)/2][(snrBlock[0][0][0].length-1)/2]);
            }
            
        }
        
        
        
        
      /*  for(int i =0; i < bestSubints.length; i++){
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            for(int j =0; j < bestSubints[0].length; j++){
       
                double val = bestSubints[i][j];
                if(val > max)max = val;
                if(val < min)min = val;
            }
            for(int j =0; j < bestSubints[0].length; j++){
                bestSubints[i][j] = (bestSubints[i][j] - min)/(max-min);
            }
        }*/
        
        // Now we output the data to the osrf file
        
        PulsarHunter.out.println("PeriodTune - Writing Output");
        
        
        if(phcfResult.getInitialSec() != null && phcfResult.getInitialSec().getSubints() == null){
            phcfResult.getInitialSec().setSubints(bestSubints);
        }
        
        // make some addjusted Subints
        
        double[][] betterSubints = new double[bestSubints.length][bestSubints[0].length];
        
        
        double s2 = -(bestSubints.length/2.0);
        
        for(int s = 0; s < bestSubints.length; s++){
            s2 += 1;
            
            int ptr = (int)(stackResult.getBestPFactor()*s2 + stackResult.getBestPdotFactor()*s2*s2 + stackResult.getBestPddotFactor()*s2*s2*s2);
            if(ptr >= nbin)ptr-=nbin;
            while(ptr < 0)ptr+=nbin;
            
            for(int i = 0; i < nbin; i++){
                while(ptr >= nbin)ptr-=nbin;
                try {
                    betterSubints[s][i]+=bestSubints[s][ptr];
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Ran out of bounds while making sub-int plot");
                    System.exit(1);
                }
                ptr++;
            }
            
            
        }
        
        
        section.setSubints(betterSubints);
        section.setPulseProfile(srBest.getBestProfile());
        section.setBestSnr(stackResult.getBestSnr());
        section.setBestWidth(stackResult.getBestWidth());
        section.setBestDm(bestDm);
        section.setBestAccn(stackResult.getBestAccn());
        section.setBestJerk(stackResult.getBestJerk());
        
        
        
        SNRBlock block = new SNRBlock(dmIndex,periodIndex,accnIndex,jerkIndex,snrBlock);
        
        if(timeSeries[0].getHeader().isBarryCentered()){
            section.setBestBaryPeriod(stackResult.getBestPeriod());
            if(dopp > 0){
                section.setBestTopoPeriod(stackResult.getBestPeriod()*dopp);
            }
            block.setBarrycenter(true);
        } else {
            section.setBestTopoPeriod(stackResult.getBestPeriod());
            if(dopp > 0){
                section.setBestBaryPeriod(stackResult.getBestPeriod()/dopp);
            }
            block.setBarrycenter(false);
        }
        section.setSnrBlock(block);
        
        
        this.phcfResult.addSection(section);
        
/*        if(stackResult.getPpdotPlane().length > 1&& stackResult.getPpdotPlane()[0].length > 1)this.phcfResult.setPPdotPlane(stackResult.getPpdotPlane());
 
        // TEST TEST
        //this.phcfResult.setPPdotPlane(stackResult.getPdotpddotPlane());
 
        this.phcfResult.setInitalSints(bestSubints);
 
        this.phcfResult.setInitialProfile(oldProf);
        this.phcfResult.setOptimisedProfile(srBest.getBestProfile());
 
        this.phcfResult.getHeader().setOptimizedDm(bestDm);
        this.phcfResult.getHeader().setOptimizedSNR(stackResult.getBestSnr());
        this.phcfResult.getHeader().setOptimizedWidth(stackResult.getBestWidth());
        if(timeSeries[0].getHeader().isBarryCentered()){
            this.phcfResult.getHeader().setOptimisedBaryPeriod(stackResult.getBestPeriod());
            if(dopp > 0){
                this.phcfResult.getHeader().setOptimisedTopoPeriod(stackResult.getBestPeriod()*dopp);
            }
        } else {
            this.phcfResult.getHeader().setOptimisedTopoPeriod(stackResult.getBestPeriod());
            if(dopp > 0){
                this.phcfResult.getHeader().setOptimisedBaryPeriod(stackResult.getBestPeriod()/dopp);
            }
        }
        this.phcfResult.getHeader().setOptimisedPdot(stackResult.getBestPdot());
        this.phcfResult.getHeader().setOptimisedPddot(stackResult.getBestPddot());
 
 */
        
        PulsarHunter.out.println("Completed Period Tune.");
        
        return;
        
    }
    
    public static double[][] dedisperseSints(final double dmf, final double[][][] bandedSubints) {
        final int nsub = bandedSubints[0].length;
        final int nbin = bandedSubints[0][0].length;
        double[][] subints = new double[nsub][nbin];
        
        for(int i = 0; i < nsub; i++){
            Arrays.fill(subints[i],0);
        }
        
        for(int b = 0; b < bandedSubints.length; b++){
            double b2 = (double)b - bandedSubints.length/2.0;
            
            int ptr = (int)((dmf)*b2);
            
            for(int i = 0; i < nsub; i++){
                
                for(int j = 0; j < nbin; j++){
                    while(ptr >= nbin)ptr-=nbin;
                    while(ptr < 0)ptr+=nbin;
                    
                    subints[i][j] += bandedSubints[b][i][ptr];
                    ptr++;
                }
            }
            
        }
        return subints;
    }
    
    private double[][][] makeBandedSubints() {
        double[][][] bandedSubints =  new double[timeSeries.length][][];
        PulsarHunter.out.println("PeriodTune - Making Subints");
        if(multiFold){
            System.out.println("PeriodTune - Using multi-channel folding routine.");
            bandedSubints = this.getFoldedSubints(timeSeries, params.getNsub(), params.getNprofilebins(), params.getCentrePeriod(), params.getCenterAccn(),params.getCenterJerk());
            
        } else {
            
            for(int b = 0; b < timeSeries.length; b++){
                
                double[][] subints = this.getFoldedSubints(timeSeries[b], params.getNsub(), params.getNprofilebins(), params.getCentrePeriod(), params.getCenterAccn(),params.getCenterJerk());
                bandedSubints[b] = subints;
                
            }
        }
        
        return bandedSubints;
    }
    
  /*  public void run() {
   
        PulsarHunter.out.println("Starting PeriodTune...");
   
        // Get the rms in the first 10000 bins
        double[] rms = new double[timeSeries.length];
   
                this.timeSeries.getRMS(0,10000);
   
        PulsarHunter.out.println("PeriodTune - Making Subints");
        double[][] subints = this.getFoldedSubints(timeSeries, params.getNsub(), params.getNprofilebins(), params.getCentrePeriod(), params.getCenterPdot());
   
        int nbin = subints[0].length;
        double numFoldsPerSubint = (timeSeries.getHeader().getObstime()/params.getCentrePeriod())/params.getNsub();
        double npulsebin = params.getCentrePeriod()/timeSeries.getHeader().getTSamp();
   
        // The rms in a subint
   
        double rmss = rms*Math.sqrt(numFoldsPerSubint*(npulsebin/nbin));
        System.out.println("RMS: "+rms+" RMSS:"+rmss);
   
        PulsarHunter.out.println("PeriodTune - Stacking Subints");
   
        StackResult stackResult = this.stackSubints(subints,params.getCentrePeriod(), params.getCenterPdot(), true,rmss,this.timeSeries.getHeader().getObstime(),params.getcoarseness(),params.getRange());
   
        System.out.println("P: "+stackResult.getBestPeriod()+"\nPD:"+stackResult.getBestPdot()+"\nW: "+stackResult.getBestWidth()+"\nS: "+stackResult.getBestSnr());
   
   
        PulsarHunter.out.println("PeriodTune - Folding Final Profile");
        double epoch = timeSeries.getHeader().getMjdStart() + Convert.secToMJD(timeSeries.getHeader().getObstime())/2.0;
   
        TimeSeriesFolder folder = new TimeSeriesFolder(new PdotFoldingModel(stackResult.getBestPeriod(),stackResult.getBestPdot(),epoch,timeSeries.getHeader().getTSamp()));
        double[] newProf = folder.fold(timeSeries,nbin);
   
        double[] oldProf = new double[subints[0].length];
        Arrays.fill(oldProf,0);
        for(double[] subint : subints){
            for(int i = 0; i < oldProf.length; i++){
                oldProf[i] += subint[i];
            }
        }
   
   
    PulsarHunter.out.println("PeriodTune - Writing Output");
    this.phcfResult.setPPdotPlane(stackResult.getSnrPlane());
    this.phcfResult.setSubints(subints);
    this.phcfResult.setInitialProfile(oldProf);
    this.phcfResult.setOptimisedProfile(newProf);
   
    this.phcfResult.getHeader().setOptimizedSNR(stackResult.getBestSnr());
    this.phcfResult.getHeader().setOptimizedWidth(stackResult.getBestWidth());
    if(timeSeries.getHeader().isBarryCentered()){
        this.phcfResult.getHeader().setOptimisedBaryPeriod(stackResult.getBestPeriod());
        if(dopp > 0){
            this.phcfResult.getHeader().setOptimisedTopoPeriod(stackResult.getBestPeriod()*dopp);
        }
    } else {
        this.phcfResult.getHeader().setOptimisedTopoPeriod(stackResult.getBestPeriod());
        if(dopp > 0){
            this.phcfResult.getHeader().setOptimisedBaryPeriod(stackResult.getBestPeriod()/dopp);
        }
    }
    this.phcfResult.getHeader().setOptimisedPdot(stackResult.getBestPdot());
   
   
    PulsarHunter.out.println("Completed Period Tune.");
   
    return;
   
}*/
    
    private StackResult stackSubints(double[][] subints, double centerPeriod, double centerAccn, double centerJerk, boolean useAccn, boolean useJerk,double rmss,double tobs,double pstep, double prange,double accnStep,double accnRange,double jerkStep,double jerkRange,int dmTrial){
        
        
        int nsub = subints.length;
        int nbins = subints[0].length;
        
        int nperiodTrials = (int)(prange/pstep);
        
        int nAccnTrials = 0;
        
        if(useAccn)nAccnTrials = (int)(accnRange/accnStep);
        int nJerkTrials = 0;
        
        if(useJerk)nJerkTrials = (int)(jerkRange/jerkStep);
        
        double binWidth = centerPeriod/(double)nbins;
        double bestWidth = 0;
        double bestSnr = 0;
        double bestPeriod = 0;
        double bestAccn = 0;
        double bestJerk = 0;
        
        int pTrial=0;
        int accnTrial=0;
        int jerkTrial=0;
        
        int bestPtrial  = 0;
        int bestAccnTrial = 0;
        int bestJerkTrial = 0;
        
        double bestPFactor = 0;
        double bestPdotFactor = 0;
        double bestPddotFactor = 0;
        
        double[] bestProfile =null;
        // rms in profile
        double rmsp = rmss*Math.sqrt(nsub);
        System.out.println("rmsp="+rmsp);
        double sHalf = (nsub/2.0);
        
        
        //   this.phcfResult.getHeader().setPeriodStep(pstep);
        //System.out.println("PStep: "+pstep+" N:"+nperiodTrials+" N*PStep"+nperiodTrials*pstep);
        
        //   this.phcfResult.getHeader().setPdotStep(pdstep);
        
        //      System.out.println(pdrange+" "+pdstep);
        if(this.snrBlock == null){
            periodIndex = new double[nperiodTrials*2+1];
            accnIndex = new double[nAccnTrials*2+1];
            jerkIndex = new double[nJerkTrials*2+1];
            int pp = 0;
            for(int ps = -nperiodTrials; ps <= nperiodTrials; ps++){
                periodIndex[pp++] = params.getCentrePeriod() + ps*pstep;
            }
            pp = 0;
            for(int ps = -nAccnTrials; ps <= nAccnTrials; ps++){
                accnIndex[pp++] = centerAccn +  ps*accnStep;
            }
            pp = 0;
            for(int ps = -nJerkTrials; ps <= nJerkTrials; ps++){
                jerkIndex[pp++] = centerJerk +  ps*jerkStep;
            }
            
            this.snrBlock = new double[dmIndex.length][periodIndex.length][accnIndex.length][jerkIndex.length];
            
        }
        
        double pcoarseness = (pstep*tobs)/(centerPeriod*nsub*binWidth);
//        double accnCoarseness = -(pdstep*tobs*tobs)/(centerPeriod*nsub*nsub*binWidth)/2.0;
//        double jerkCoarseness = -(pddstep*tobs*tobs)/(centerPeriod*nsub*nsub*nsub*binWidth);
        
        double pdConversion =   (tobs*tobs)/(2.0*nsub*nsub*binWidth*Convert.SPEED_OF_LIGHT);
        double pddConversion =  (tobs*tobs*tobs)/(6.0*nsub*nsub*nsub*binWidth*Convert.SPEED_OF_LIGHT);
        
//            double[][]  ppdotPlane = new double[nperiodTrials*2+1][npdotTrials*2+1];
//            double[][]  pdotpddotPlane = new double[npdotTrials*2+1][npddotTrials*2+1];
//        double[][][] snrPlane = new double[nperiodTrials*2+1][nAccnTrials*2+1][nAccnTrials*2+1];
//        double[][][] widthPlane = new double[nperiodTrials*2+1][nAccnTrials*2+1][nAccnTrials*2+1];
        
        double complete = 0;
        int totalTrials = (nperiodTrials*2+1)*(nAccnTrials*2+1)*(nJerkTrials*2+1);
        double completeEta = 30.0*1.0/(double)totalTrials;
        //  PulsarHunter.out.println("PeriodTune - Total Trials = "+totalTrials);
        
        
        PulsarHunter.out.println("PeriodTune - P-Pdot trials:"+totalTrials);
        PulsarHunter.out.println("PeriodTune - |0%                        100%|");
        PulsarHunter.out.print("PeriodTune - [");
        
        // Search periods
        pTrial=-1;
        for(int ps = -nperiodTrials; ps <= nperiodTrials; ps++){
            pTrial++;
            double periodFactor = (double)ps * (double)pcoarseness;
            
            // Search pdots
            accnTrial=-1;
            for(int acs = -nAccnTrials; acs <= nAccnTrials; acs++){
                accnTrial++;
                // double pdotFactor = 0;
                //   if(useAccn)pdotFactor = (double)pds * (double)pdcoarseness;
                
                double accnOffset = acs*accnStep;
                
                
                double pdotFactor = pdConversion*accnOffset;
                
                // Search pddots
                jerkTrial=-1;
                for(int jes = -nJerkTrials; jes <= nJerkTrials; jes++){
                    jerkTrial++;
                    // double pddotFactor = 0;
                    
                    //       if(nJerkTrials > 0)pddotFactor = (double)pdds * (double)pddcoarseness;
                    double jerkOffset = jes*jerkStep;
                    
                    double pddotFactor = pddConversion*jerkOffset;
                    
                    
                    
                    complete += completeEta;
                    while(complete > 1){
                        PulsarHunter.out.print(".");
                        PulsarHunter.out.flush();
                        complete -= 1.0;
                    }
                    
                    double[] profile = new double[nbins];
                    Arrays.fill(profile,0);
                    double s2 = - sHalf;
                    
                    for(int s = 0; s < nsub; s++){
                        s2 += 1;
                        int ptr = (int)(periodFactor*s2 + pdotFactor*s2*s2 + pddotFactor*s2*s2*s2);
                        if(ptr >= nbins)ptr-=nbins;
                        while(ptr < 0)ptr+=nbins;
                        
                        for(int i = 0; i < nbins; i++){
                            while(ptr >= nbins)ptr-=nbins;
                            try {
                                profile[i]+=subints[s][ptr];
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.out.println("s:"+s+" i:"+i+" ptr:"+ptr+" proflen:"+profile.length+" silen:"+subints.length);
                                System.exit(1);
                            }
                            ptr++;
                        }
                        
                        
                    }
                    
                    // Now calculate the SNR for this trial...
                    double[] snr_width_sm = this.smooth(profile,32,rmsp);
                    double snr = snr_width_sm[0];
                    double width = snr_width_sm[1];
                    //     System.out.println(nbins+" "+rmsp+" "+(width*nbins)+" "+snr+" "+snr_width_sm[2]);
                    //   ppdotPlane[pTrial][pdotTrial] = snr;
                    //   pdotpddotPlane[pdotTrial][pddotTrial] = snr;
                    //   snrPlane[pTrial][pdotTrial][pddotTrial] = snr;
//                    widthPlane[pTrial][accnTrial][jerkTrial] = width;
                    snrBlock[dmTrial][pTrial][accnTrial][jerkTrial] = snr;
                    if(snr > bestSnr){
                        
                        bestSnr = snr;
                        bestWidth = width;
                        //deltaT = periodFactor*nsub*binWidth;
                        bestPeriod = centerPeriod + ps*pstep;
                        
                        
                        //deltaT = pdotFactor*sHalf*sHalf*binWidth;
                        bestAccn = centerAccn + accnOffset;
                        
                        //deltaT = pddotFactor*sHalf*sHalf*sHalf*binWidth;
                        bestJerk = centerJerk + jerkOffset;
                        // bestPdot = deltaT;
                        bestPtrial  = pTrial;
                        bestAccnTrial = accnTrial;
                        bestJerkTrial = jerkTrial;
                        
                        bestPFactor = periodFactor;
                        bestPdotFactor = pdotFactor;
                        bestPddotFactor = pddotFactor;
                        
                        bestProfile = profile;
                        
                    }
                }
            }
        }
        PulsarHunter.out.println("] done.");
        StackResult result = new StackResult();
        
        
        
        // result.setPpdotPlane(ppdotPlane);
        //  result.setPdotpddotPlane(pdotpddotPlane);
        
        // result.setSnrPlane(snrPlane);
//        result.setWidthPlane(widthPlane);
        result.setBestSnr(bestSnr);
        result.setBestPeriod(bestPeriod);
        result.setBestAccn(bestAccn);
        result.setBestJerk(bestJerk);
        result.setBestWidth(bestWidth);
        //   result.setBestPRow(bestPtrial);
        //   result.setBestPdotRow(bestPdotTrial);
        //   result.setBestPddotRow(bestPddotTrial);
        
        result.setBestPFactor(bestPFactor);
        result.setBestPdotFactor(bestPdotFactor);
        result.setBestPddotFactor(bestPddotFactor);
        
        result.setBestProfile(bestProfile);
        
        
        return result;
    }
    
//       private StackResult stackSubints(double[][] subints, double centerPeriod, double centerPdot, double centerPddot, boolean usePdot, boolean usePddot,double rmss,double tobs,double pstep, double prange,double pdstep,double pdrange,double pddstep,double pddrange,int dmTrial){
//        /**=2.0;
//         * prange/=2.0;
//         * pdcoarseness*=5e-4;
//         * pdrange*=5e-4;
//         * pddcoarseness*=16e-5;
//         * pddrange*=32e-5;*/
//
//        int nsub = subints.length;
//        int nbins = subints[0].length;
//        //int nperiodTrials = (int)(nsub*prange/pcoarseness);
//        int nperiodTrials = (int)(prange/pstep);
//        //pcoarseness/=nsub;
//        int npdotTrials = 0;
//        //if(usePdot)npdotTrials = (int)(nsub*pdrange/pdcoarseness);
//        if(usePdot)npdotTrials = (int)(pdrange/pdstep);
//        int npddotTrials = 0;
//        //if(usePddot)npddotTrials = (int)(nsub*pddrange/pddcoarseness);
//        if(usePddot)npddotTrials = (int)(pddrange/pddstep);
//        double binWidth = centerPeriod/(double)nbins;
//        double bestWidth = 0;
//        double bestSnr = 0;
//        double bestPeriod = 0;
//        double bestPdot = 0;
//        double bestPddot = 0;
//        int pTrial=0;
//        int pdotTrial=0;
//        int pddotTrial=0;
//
//        int bestPtrial  = 0;
//        int bestPdotTrial = 0;
//        int bestPddotTrial = 0;
//
//        double[] bestProfile =null;
//        // rms in profile
//        double rmsp = rmss*Math.sqrt(nsub);
//
//        double sHalf = (nsub/2.0);
//
//
//        //   this.phcfResult.getHeader().setPeriodStep(pstep);
//        //System.out.println("PStep: "+pstep+" N:"+nperiodTrials+" N*PStep"+nperiodTrials*pstep);
//
//        //   this.phcfResult.getHeader().setPdotStep(pdstep);
//
//        //      System.out.println(pdrange+" "+pdstep);
//        if(this.snrBlock == null){
//            periodIndex = new double[nperiodTrials*2+1];
//            pDotIndex = new double[npdotTrials*2+1];
//            pDdotIndex = new double[npddotTrials*2+1];
//            int pp = 0;
//            for(int ps = -nperiodTrials; ps <= nperiodTrials; ps++){
//                periodIndex[pp++] = params.getCentrePeriod() + ps*pstep;
//            }
//            pp = 0;
//            for(int ps = -npdotTrials; ps <= npdotTrials; ps++){
//                pDotIndex[pp++] = params.getCenterPdot() +  ps*pdstep;
//            }
//            pp = 0;
//            for(int ps = -npddotTrials; ps <= npddotTrials; ps++){
//                pDdotIndex[pp++] = params.getCentrePddot() +  ps*pddstep;
//            }
//
//            this.snrBlock = new double[dmIndex.length][periodIndex.length][pDotIndex.length][pDdotIndex.length];
//
//        }
//
//        double pcoarseness = (pstep*tobs)/(centerPeriod*nsub*binWidth);
//        double pdcoarseness = -(pdstep*tobs*tobs)/(centerPeriod*nsub*nsub*binWidth)/2.0;
//        double pddcoarseness = -(pddstep*tobs*tobs)/(centerPeriod*nsub*nsub*nsub*binWidth);
//
//        double[][]  ppdotPlane = new double[nperiodTrials*2+1][npdotTrials*2+1];
//        double[][]  pdotpddotPlane = new double[npdotTrials*2+1][npddotTrials*2+1];
//        double[][][] snrPlane = new double[nperiodTrials*2+1][npdotTrials*2+1][npddotTrials*2+1];
//        double[][][] widthPlane = new double[nperiodTrials*2+1][npdotTrials*2+1][npddotTrials*2+1];
//
//        double complete = 0;
//        int totalTrials = (nperiodTrials*2+1)*(npdotTrials*2+1)*(npddotTrials*2+1);
//        double completeEta = 30.0*1.0/(double)totalTrials;
//        //  PulsarHunter.out.println("PeriodTune - Total Trials = "+totalTrials);
//
//
//        PulsarHunter.out.println("PeriodTune - P-Pdot trials:"+totalTrials);
//        PulsarHunter.out.println("PeriodTune - |0%                        100%|");
//        PulsarHunter.out.print("PeriodTune - [");
//
//        // Search periods
//        pTrial=-1;
//        for(int ps = -nperiodTrials; ps <= nperiodTrials; ps++){
//            pTrial++;
//            double periodFactor = (double)ps * (double)pcoarseness;
//
//            // Search pdots
//            pdotTrial=-1;
//            for(int pds = -npdotTrials; pds <= npdotTrials; pds++){
//                pdotTrial++;
//                double pdotFactor = 0;
//                if(npdotTrials > 0)pdotFactor = (double)pds * (double)pdcoarseness;
//
//                // Search pddots
//                pddotTrial=-1;
//                for(int pdds = -npddotTrials; pdds <= npddotTrials; pdds++){
//                    pddotTrial++;
//                    double pddotFactor = 0;
//                    if(npddotTrials > 0)pddotFactor = (double)pdds * (double)pddcoarseness;
//
//
//
//                    complete += completeEta;
//                    while(complete > 1){
//                        PulsarHunter.out.print(".");
//                        PulsarHunter.out.flush();
//                        complete -= 1.0;
//                    }
//
//                    double[] profile = new double[nbins];
//                    Arrays.fill(profile,0);
//                    double s2 = - sHalf;
//
//                    for(int s = 0; s < nsub; s++){
//                        s2 += 1;
//                        int ptr = (int)(periodFactor*s2 + pdotFactor*s2*s2 + pddotFactor*s2*s2*s2);
//                        if(ptr >= nbins)ptr-=nbins;
//                        while(ptr < 0)ptr+=nbins;
//
//                        for(int i = 0; i < nbins; i++){
//                            while(ptr >= nbins)ptr-=nbins;
//                            try {
//                                profile[i]+=subints[s][ptr];
//                            } catch (ArrayIndexOutOfBoundsException e) {
//                                System.out.println("s:"+s+" i:"+i+" ptr:"+ptr+" proflen:"+profile.length+" silen:"+subints.length);
//                                System.exit(1);
//                            }
//                            ptr++;
//                        }
//
//
//                    }
//
//                    // Now calculate the SNR for this trial...
//                    double[] snr_width_sm = this.smooth(profile,32,rmsp);
//                    double snr = snr_width_sm[0];
//                    double width = snr_width_sm[1];
//                    //     System.out.println(nbins+" "+rmsp+" "+(width*nbins)+" "+snr+" "+snr_width_sm[2]);
//                    //   ppdotPlane[pTrial][pdotTrial] = snr;
//                    //   pdotpddotPlane[pdotTrial][pddotTrial] = snr;
//                    //   snrPlane[pTrial][pdotTrial][pddotTrial] = snr;
//                    widthPlane[pTrial][pdotTrial][pddotTrial] = width;
//                    snrBlock[dmTrial][pTrial][pdotTrial][pddotTrial] = snr;
//                    if(snr > bestSnr){
//
//                        bestSnr = snr;
//                        bestWidth = width;
//                        //deltaT = periodFactor*nsub*binWidth;
//                        bestPeriod = centerPeriod + ps*pstep;
//
//
//                        //deltaT = pdotFactor*sHalf*sHalf*binWidth;
//                        bestPdot = centerPdot + pds*pdstep;
//
//                        //deltaT = pddotFactor*sHalf*sHalf*sHalf*binWidth;
//                        bestPddot = centerPddot + pdds*pddstep;
//                        // bestPdot = deltaT;
//                        bestPtrial  = pTrial;
//                        bestPdotTrial = pdotTrial;
//                        bestProfile = profile;
//
//                    }
//                }
//            }
//        }
//        PulsarHunter.out.println("] done.");
//        StackResult result = new StackResult();
//
//
//
//        // result.setPpdotPlane(ppdotPlane);
//        //  result.setPdotpddotPlane(pdotpddotPlane);
//
//        // result.setSnrPlane(snrPlane);
//        result.setWidthPlane(widthPlane);
//        result.setBestSnr(bestSnr);
//        result.setBestPeriod(bestPeriod);
//        result.setBestPdot(bestPdot);
//        result.setBestPddot(bestPddot);
//        result.setBestWidth(bestWidth);
//        //   result.setBestPRow(bestPtrial);
//        //   result.setBestPdotRow(bestPdotTrial);
//        //   result.setBestPddotRow(bestPddotTrial);
//        result.setBestProfile(bestProfile);
//
//
//        return result;
//    }
    
    
    
    /*
     
      subroutine smooth_mw(pr,nbin,maxwidth, rmsp,kwmax,snrmax,smmax)
c******************************************************************
c
c  convolves profile pr(nbin) with a boxcar of width kw.  it returns
c    the width kwmax which gave the highest s/n ratio snrmax, and the
c    corresponding pulse amplitude smmax.
c
c RTE 17 Feb 00, parameterized maximum boxcar witdth
c
c******************************************************************
c
     
      integer nbin,kwmax, maxwidth
      real*4 pr(*),rmsp,snrmax,smmax
c
      integer ksm,j,k,kw,nn,ja,jj
      real*4 s,wrk(512),al,an,sn,smax
c
      snrmax=0.
c---------------------------------------
c  remove baseline
      ksm=nbin/2.5+0.5
      smax=1.e30
      do 10 j = 1,nbin
        s=0.0
        do 20 k = 1,ksm
          s = s + pr(mod(j+k-1,nbin)+1)
   20   continue
        if(s.lt.smax) smax=s
   10 continue
      smax=smax/ksm
      do 30 j = 1,nbin
        pr(j) = pr(j) - smax
   30 continue
c--------------------------------------
c
c
      do 40 nn=1,1000
        kw=2**(nn-1)
        if (kw.gt.maxwidth) goto 70
        if(kw.gt.nbin/2) return
        s=0.0
        do 50 k=1,kw
          s=s+pr(k)
          wrk(k)=pr(k)
   50   continue
        ja=0
        smax=s
        do 60 j=2,nbin
          ja=ja+1
          if(ja.gt.kw) ja=ja-kw
          al=wrk(ja)
          jj=j+kw-1
          if(jj.gt.nbin)jj=jj-nbin
          an=pr(jj)
          s=s+an-al
          wrk(ja)=an
          if(s.gt.smax) smax=s
   60   continue
     
        sn=smax/(rmsp*sqrt(kw*(1.+float(kw)/nbin)))
        if(sn.gt.snrmax) then
          snrmax=sn
          kwmax=kw
          smmax=smax/kw
        endif
   40 continue
     
 70   end
     
     */
    
    private int mod(int a, int b){
        while(a >= b)a-=b;
        while(a < 0)a+=b;
        return a;
    }
    
    /**
     *
     * convolves profile pr(nbin) with a boxcar of width kw. Returns the width kwmax which gave the highest s/n ratio snrmax, and the corresponding pulse amplitude smmax, As a 3 element double array.
     * @returns new double[]{snrmax,fractionalWidth,smmax}
     *
     * MKeith 2006: Convert to Java.
     * RTE 17 Feb 00, parameterized maximum boxcar witdth
     *
     **/
    private double[] smooth(double[] pr, int  maxwidth, double rmsp){
        
        
        int kwmax=0,nbin;
        double snrmax=0,smmax=0;
        int ksm,kw,ja,jj;
        double s,al,an,sn,smax;
        snrmax=0.;
        nbin = pr.length;
        
        //  remove baseline
        ksm=(int)(nbin/2.5+0.5);
        smax=Double.MAX_VALUE;
        for(int j = 0; j < nbin; j++){
            s=0.0;
            for(int k = 0; k < ksm; k++){
                s = s + pr[mod(j+k,nbin)];
            }
            if(s < smax) smax=s;
        }
        smax=smax/ksm;
        for(int j = 0; j < nbin; j++){
            pr[j] = pr[j] - smax;
        }
        //--------------------------------------
        
        
        kw = 1;
        //for(int nn = 0; nn < 1000; nn++){
        while(kw <= maxwidth && kw < nbin/2){
            
            
            s=0.0;
            
            for(int k =0; k < kw; k++){
                s=s+pr[k];
                // wrk[k]=pr[k];
            }
            // ja=-1;
            smax=s;
            
            
            // Not sure why the old method is so complex... it should just be a top hat convolve.
           /* for(int j = 1; j < nbin; j++){
                ja=ja+1;
                if(ja > kw) ja=ja-kw;
                al=wrk[ja];
                jj=j+kw;
                if(jj >= nbin)jj=jj-nbin;
                an=pr[jj];
                s=s+an-al;
                wrk[ja]=an;
                if(s > smax) smax=s;
            }*/
            
            // New convolve
            for(int j = 0; j < nbin;j++){
                
                ja = mod(j+kw,nbin);
                s-=pr[j];
                s+=pr[ja];
                if(s > smax) smax=s;
            }
            
            sn=smax/(rmsp*Math.sqrt(kw*(1.0+(float)kw/(float)nbin)));
            
            //sn=smax/(rmsp*(kw + (float)(kw)/(float)nbin));
            if( sn > snrmax){ // TEST only consider width 1
                snrmax=sn;
                kwmax=kw;
                smmax=smax/kw;
                
            }
            // Double width
            kw*=2;
        }
        
        
        return new double[]{snrmax,(double)kwmax/(double)pr.length,smmax};
    }
    
    
    
    
    private double[][] getFoldedSubints(TimeSeries timeSeries, int nsub,int nbins, double period, double accn,double jerk){
        double[][] foldedSubints = new double[nsub][];
        double epoch = timeSeries.getHeader().getMjdStart() + Convert.secToMJD(timeSeries.getHeader().getTobs())/2.0;
        
        TimeSeriesFolder folder;
        if(polyco==null){
            folder = new TimeModelFolder(new AccelJerkTimeModel(accn,jerk),period,epoch);
        } else{
            PulsarHunter.out.println("PeriodTune - Folding with polyco, slow!");
            PulsarHunter.out.println("PeriodTune - Warning: accn/jerk vals will be wrong.");
            folder = new PolyCoFolder(polyco,this.phcfResult.getHeader().getSourceID());
        }
        
        
        // Compute first subint
        long start = 0;
        long end = start + timeSeries.getHeader().getNPoints()/nsub;
        
        double complete = 0;
        double completeEta = 30*(1.0/nsub);
        
        
        
        PulsarHunter.out.print("PeriodTune - [");
        for(int i = 0; i < nsub; i++){
            
            //get the current subint...
            TimeSeries subTS = timeSeries.subData(start,end);
            BulkReadable bulkTimeSeries = subTS.getBulkReadableInterface();
            //  bulkTimeSeries = null;
            if(bulkTimeSeries==null){
//            Date sss = new Date();
                foldedSubints[i] = folder.fold(subTS,nbins);
//            Date mmm = new Date();
            } else {
                foldedSubints[i] = folder.fold(bulkTimeSeries,nbins);
            }
//            Date eee = new Date();
//            System.out.println("B: "+(eee.getTime()-mmm.getTime()));
//            System.out.println("O: "+(mmm.getTime()-sss.getTime()));
//            System.out.println();
            
            
            start = end;
            end = start + timeSeries.getHeader().getNPoints()/nsub;
            
            
            complete += completeEta;
            while(complete > 1){
                PulsarHunter.out.print(".");
                PulsarHunter.out.flush();
                complete -= 1.0;
            }
            
        }
        
        PulsarHunter.out.println("]");
        
        return foldedSubints;
    }
    
    private double[][][] getFoldedSubints(TimeSeries timeSeries[], int nsub,int nbins, double period, double accn,double jerk){
        final int nbands = timeSeries.length;
        
        double[][][] foldedSubints = null;
        double epoch = timeSeries[0].getHeader().getMjdStart() + Convert.secToMJD(timeSeries[0].getHeader().getTobs())/2.0;
        TimeSeriesFolder folder;
        if(polyco==null){
            folder = new TimeModelFolder(new AccelJerkTimeModel(accn,jerk),period,epoch);
        } else{
            PulsarHunter.out.println("PeriodTune - Folding with polyco, slow!");
            PulsarHunter.out.println("PeriodTune - Warning: accn/jerk vals will be wrong.");
            folder = new PolyCoFolder(polyco,this.phcfResult.getHeader().getSourceID());
        }
        
        
        
        
        // Compute first subint
        long start = 0;
        long end = start + timeSeries[0].getHeader().getNPoints()/nsub;
        
        TimeSeries[] subTS = new TimeSeries[nbands];
        MultiChannelTimeSeries subTSAll;
        double complete = 0;
        double completeEta = 30*(1.0/nsub);
        
        PulsarHunter.out.print("PeriodTune - [");
        
      /*  for(int b = 0; b < timeSeries.length; b++){
       
       
            double[][] subints = this.getFoldedSubints(timeSeries[b], params.getNsub(), params.getNprofilebins(), params.getCentrePeriod(), params.getCenterPdot());
            foldedSubints[b] = subints;
        }*/
        
        for(int i = 0; i < nsub; i++){
            //get the current subint...
            for(int b = 0; b < nbands; b++){
                subTS[b] = timeSeries[b].subData(start,end);
            }
            
            
            BulkReadable bulkTimeSeries = null;
            if(multiTS!= null){
                
                subTSAll = multiTS.subData(start,end);
                bulkTimeSeries = subTSAll.getBulkReadableInterface();
            }
            //bulkTimeSeries = null;
            
            // we get sband x bin back... we want to fit this in sband x sint x bin
            double[][] crazySubints;
            
            if(bulkTimeSeries==null){
                
                crazySubints = folder.multiFold(subTS,nbins);
                
            } else {
                
                crazySubints = folder.multiFold(bulkTimeSeries,nbins);
            }
            
            if(foldedSubints == null){
                foldedSubints = new double[nbands][nsub][crazySubints[0].length];
            }
            
            
            
            
            for(int b = 0; b < nbands; b++){
                boolean ignore = false;
                for(int j : params.getIgnoreBands()){
                    if(b==j)ignore =  true;
                }
                if(!ignore)System.arraycopy(crazySubints[b],0,foldedSubints[b][i],0,crazySubints[b].length);
                
            }
            
            
            start = end;
            end = start + timeSeries[0].getHeader().getNPoints()/nsub;
            
            complete += completeEta;
            while(complete > 1){
                PulsarHunter.out.print(".");
                PulsarHunter.out.flush();
                complete -= 1.0;
            }
        }
        
        PulsarHunter.out.println("]");
        
        
        return foldedSubints;
    }
    
    
    
}
