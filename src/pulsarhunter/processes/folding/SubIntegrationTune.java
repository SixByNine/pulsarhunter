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
import pulsarhunter.datatypes.MultiprofileFile;
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
public class SubIntegrationTune implements PulsarHunterProcess{
    private MultiprofileFile timeSeries;
    
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
        
    }
    
    public SubIntegrationTune(MultiprofileFile timeSeries,PeriodTuneFoldParams params) {
        
        
        this.timeSeries = timeSeries;
        
        this.params = params;
        
        
        this.phcfResult = phcfResult;
        
        if(BarryCenter.isAvaliable()){
            BarryCenter bc = new BarryCenter(timeSeries.getHeader().getMjdStart(),
                    timeSeries.getHeader().getTelescope(),
                    timeSeries.getHeader().getCoord().getRA().toDegrees(),
                    timeSeries.getHeader().getCoord().getDec().toDegrees());
            
            dopp = bc.getDopplerFactor();
            
        }
        
    }
    
    
    public void run() {
        
        if(!this.timeSeries.isComplete()){
            System.err.println("TuneSubints - Cannot Use Multiprofile (EPN) files which are not 'complete'");
            System.err.println("Check the number of bins/profiles per channel/timestamp etc are all equal.");
            return;
        }
        
        
        double[][][] bandedSubints  = new double[this.timeSeries.getHeader().getNumberOfChannels()][this.timeSeries.getHeader().getNumberOfTimeStamps()][this.timeSeries.getHeader().getBinsPerProfile()];
        
        // PulsarHunter.out.println("Starting PeriodTune...");
        
        // Get the rms in the first 10000 bins
        double rms = 0;
        double[] rmsInBand = new double[timeSeries.getHeader().getNumberOfChannels()];
        
        
        for(int b = 0; b < rmsInBand.length; b++){
            rmsInBand[b] = this.timeSeries.getHeader().getBandRMS(b,0);
            rms += rmsInBand[b]*rmsInBand[b];
        }
        
        
        // root rms sum of squares.
        rms = Math.sqrt(rms);
        
        
        // Zero out the ignored subints...
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
        
        // Have to assume it's the same for all the profiles...
        double numFoldsPerSubint = this.timeSeries.getHeader().getNumberOfFoldsPerProfile();
       
        double bestDm = 0.0;
        
        
        // The rms in a subint
        
        double rmss = rms*Math.sqrt(numFoldsPerSubint*(this.timeSeries.getHeader().getBinWidth()/this.timeSeries.getHeader().getTimeResolution()));
        //System.out.println("RMS: "+rms+" RMSS:"+rmss);
        
        
        
        
        
        //double dmcoursness = 1.0/(double)bandedSubints.length*2;
        //dmcoursness*=params.getDmcoarseness();
        
        
        
        double binWidth = params.getCentrePeriod()/(double)nbin;
        
        // double deltaT = dmcoursness * bandedSubints.length * binWidth;
        
        double flo = timeSeries.getHeader().getBandFreq(0)/1000.0;//GHz
        double fhi = timeSeries.getHeader().getBandFreq(bandedSubints.length-1)/1000.0;//GHz
        if(flo > fhi){
            double fswap = fhi;
            fhi = flo;
            flo = fswap;
        }
        final double kdm = 4.148808e-3;//s
        //double deltaDm = deltaT / (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi)));
        
        double deltaDm = params.getDmstep();
        
        double dmcoursness = (deltaDm *  (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi))))
        /
                (bandedSubints.length * binWidth);
        
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
            
            if(fixedDmstep) System.out.println("TuneSubints - Dm Step below min threashold: reset to "+params.getDmstep());
        }
//        System.out.println("DMc "+dmcoursness);
//        System.out.println("DeltaDM "+deltaDm);
        
        StackResult srBest = null;
        
        int ndms = (int)(params.getDmrange()/deltaDm);
        
        StackResult[] results = new StackResult[2*ndms+1];
        double[][] bestSubints = null;
        
        
        
        
        
        
        if(bandedSubints.length == 1){
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
            
            double fch = timeSeries.getHeader().getBandFreq(b)/1000.0;
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
                    ptr++;
                }
            }
        }
        
        if(interactive){
            TuneDisplayFrame frame = new TuneDisplayFrame(bandedSubints,params);
            frame.setVisible(true);
            frame.waitToTerminate();
        }
        
        
        PulsarHunter.out.println("TuneSubints - "+(2*ndms+1)+" DM trials...");
        
        for(int dms = -ndms; dms <= ndms; dms++){
            
            double dm = dms;
            double dmf = dms*dmcoursness;
            PulsarHunter.out.println("TuneSubints - DM "+(dms*deltaDm+this.params.getCenterDM()));
            double[][] subints = dedisperseSints(dmf, bandedSubints);
            
            
            //PulsarHunter.out.println("PeriodTune - Stacking Subints");
            
            
            StackResult stackResult = this.stackSubints(subints,params.getCentrePeriod(), params.getCenterAccn(),params.getCenterJerk(), params.isUseAccn(),params.isUseJerk(),rmss,this.timeSeries.getHeader().getTobs(),params.getPstep(),params.getPrange(),params.getAccnStep(),params.getAccnRange(), params.getJerkStep(),params.getJerkRange(),dmStep);
            
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
        
        
        
        
        
        // Do we have subbands, if so, add them!
        
        
        if(bandedSubints.length == 1){
            bestDm = -1;
        } else {
            double deltaT = bestDm * bandedSubints.length * binWidth;
            
            deltaDm = deltaT / (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi)));
            
            
            bestDm = this.params.getCenterDM() + deltaDm;
            
            
            
            // compute subband plot
            double[][] sbands = new double[nbin][bandedSubints.length];
            for(int i =0; i < sbands.length; i++)Arrays.fill(sbands[i],0);
            for(int dd = 0; dd < sbands[0].length; dd++){
                for(int i =0; i < sbands.length; i++){
                    for(int s = 0; s < bandedSubints[dd].length; s++){
                        sbands[i][dd] += bandedSubints[dd][s][i];
                        
                    }
                }
            }
            // normalise
            for(int i =0; i < sbands[0].length; i++){
                double max = -Double.MAX_VALUE;
                double min = Double.MAX_VALUE;
                for(int j =0; j < sbands.length; j++){
                    
                    double val = sbands[j][i];
                    if(val > max)max = val;
                    if(val < min)min = val;
                }
                if(max!=min){
                    for(int j =0; j < sbands.length; j++){
                        sbands[j][i] = (sbands[j][i] - min)/(max-min);
                    }
                }
            }
            section.setSubbands(sbands);
        }
        
        StackResult stackResult = srBest;
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
        
        
        
        
        
        
        
        section.setSubints(bestSubints);
        section.setPulseProfile(srBest.getBestProfile());
        section.setBestSnr(stackResult.getBestSnr());
        section.setBestWidth(stackResult.getBestWidth());
        section.setBestDm(bestDm);
        section.setBestAccn(stackResult.getBestAccn());
        section.setBestJerk(stackResult.getBestJerk());
        
        
        
        SNRBlock block = new SNRBlock(dmIndex,periodIndex,accnIndex,jerkIndex,snrBlock);
        
        if(timeSeries.getHeader().isBarryCentered()){
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
        
        double[] bestProfile =null;
        // rms in profile
        double rmsp = rmss*Math.sqrt(nsub);
        
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
    
    
    
    
    
    
    
    
}
