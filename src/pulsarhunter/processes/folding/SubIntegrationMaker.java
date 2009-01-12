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

import pulsarhunter.BarryCenter;
import pulsarhunter.Convert;
import pulsarhunter.PulsarHunter;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.datatypes.BulkReadable;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.MultiprofileFile;
import pulsarhunter.datatypes.PolyCoFile;
import pulsarhunter.datatypes.TimeSeries;

/**
 *
 * @author mkeith
 */
public class SubIntegrationMaker implements PulsarHunterProcess{
    private TimeSeries[] timeSeries;
    private MultiChannelTimeSeries multiTS = null;
    private MultiprofileFile output;
    private PolyCoFile polyco = null;
    
    private double dopp = -1;
    private boolean multiFold = false;
    
    private double period;
    private double accn;
    private double jerk;
    private int nsub;
    private int nbins;
    
    
    public SubIntegrationMaker(MultiChannelTimeSeries timeSeries,MultiprofileFile output,double period, double accn, double jerk,int nsub,int nbins) {
        this(timeSeries,output,null,period,accn,jerk,nsub,nbins);
    }
    public SubIntegrationMaker(MultiChannelTimeSeries timeSeries,MultiprofileFile output,PolyCoFile pcf,int nsub,int nbins){
        this(timeSeries,output,pcf,0,0,0,nsub,nbins);
    }
    private SubIntegrationMaker(MultiChannelTimeSeries timeSeries,MultiprofileFile output,PolyCoFile pcf,double period, double accn, double jerk,int nsub,int nbins) {
        
        this.polyco=pcf;
        
        if(timeSeries.getHeader().isChannelInterleaved()){
            multiFold = true;
            System.out.println("SubintFolder - Using MultiFold for interleaved channels");
        }
        
        this.timeSeries = new TimeSeries[timeSeries.getHeader().getNumChannel()];
        multiTS = timeSeries;
        
        if(BarryCenter.isAvaliable()){
            BarryCenter bc = new BarryCenter(timeSeries.getHeader().getMjdStart(),
                    timeSeries.getHeader().getTelescope(),
                    timeSeries.getHeader().getCoord().getRA().toDegrees(),
                    timeSeries.getHeader().getCoord().getDec().toDegrees());
            
            dopp = bc.getDopplerFactor();
        }
        this.period = period;
        this.accn = accn;
        this.jerk = jerk;
        this.nsub = nsub;
        this.nbins = nbins;
        this.output = output;
    }
    
    
    
    /** Creates a new instance of PeriodTuneFold */
    public SubIntegrationMaker(TimeSeries timeSeries,MultiprofileFile output,double period, double accn, double jerk,int nsub,int nbins) {
        this(timeSeries,output,null,period,accn,jerk,nsub,nbins);
    }
    public SubIntegrationMaker(TimeSeries timeSeries,MultiprofileFile output,PolyCoFile pcf,int nsub,int nbins){
        this(timeSeries,output,pcf,0,0,0,nsub,nbins);
    }
    
    private SubIntegrationMaker(TimeSeries timeSeries,MultiprofileFile output,PolyCoFile pcf,double period, double accn, double jerk,int nsub,int nbins) {
        this.polyco = pcf;
        
        this.timeSeries = new TimeSeries[]{timeSeries};
        
        if(BarryCenter.isAvaliable()){
            BarryCenter bc = new BarryCenter(timeSeries.getHeader().getMjdStart(),
                    timeSeries.getHeader().getTelescope(),
                    timeSeries.getHeader().getCoord().getRA().toDegrees(),
                    timeSeries.getHeader().getCoord().getDec().toDegrees());
            
            dopp = bc.getDopplerFactor();
            
        }
        
        this.period = period;
        this.accn = accn;
        this.jerk = jerk;
        this.nsub = nsub;
        this.nbins = nbins;
        this.output = output;

    }
    
    public void run(){
        
        
        
        /*
         * Fill in the subints.
         *
         */
        
        double[][][] bandedsubints = this.makeBandedSubints();
        double subtime = timeSeries[0].getHeader().getTobs()/(double)nsub;
        for(int b = 0; b < bandedsubints.length; b++){
            double freq = timeSeries[b].getHeader().getFrequency();
            double band = timeSeries[b].getHeader().getBandwidth();
            for(int s = 0; s < bandedsubints[b].length; s++){
                double time = s*subtime;
                this.output.addProfile(bandedsubints[b][s],time,freq,band);
                
            }
        }
        
        /*
         * Arrange for the headers to be set right!
         */
        
        output.getHeader().setBinsPerProfile(bandedsubints[0][0].length);
        output.getHeader().setBinWidth(this.period / bandedsubints[0][0].length);
        output.getHeader().setTimeResolution(this.period / bandedsubints[0][0].length);
        output.getHeader().setCoord(this.timeSeries[0].getHeader().getCoord());
        output.getHeader().setMjdStart(this.timeSeries[0].getHeader().getMjdStart());
        output.getHeader().setSourceID(this.timeSeries[0].getHeader().getSourceID());
        
        output.getHeader().setTelescope(this.timeSeries[0].getHeader().getTelescope());
        if(this.polyco!=null)output.getHeader().setFoldingPeriod(this.period);
        
        
        
    }
    
    private double[][][] makeBandedSubints() {

        double[][][] bandedSubints =  new double[timeSeries.length][][];
        PulsarHunter.out.println("SubintFolder - Making Subints");
        if(multiFold){
            System.out.println("SubintFolder - Using multi-channel folding routine.");
            bandedSubints = this.getFoldedSubints(timeSeries, nsub, nbins, period, accn,jerk);
            
        } else {
            
            for(int b = 0; b < timeSeries.length; b++){
                
                double[][] subints = this.getFoldedSubints(timeSeries[b], nsub, nbins, period, accn, jerk);
                bandedSubints[b] = subints;
                
            }
        }
        
        return bandedSubints;
    }
    
    private double[][] getFoldedSubints(TimeSeries timeSeries, int nsub,int nbins, double period, double accn,double jerk){
        double[][] foldedSubints = new double[nsub][];
        double epoch = timeSeries.getHeader().getMjdStart() + Convert.secToMJD(timeSeries.getHeader().getTobs())/2.0;
        
        TimeSeriesFolder folder;
        if(polyco==null){
            folder = new TimeModelFolder(new AccelJerkTimeModel(accn,jerk),period,epoch);
        } else{
            PulsarHunter.out.println("SubintFolder - Folding with polyco, slow!");
            PulsarHunter.out.println("SubintFolder - Warning: accn/jerk vals will be wrong.");
            folder = new PolyCoFolder(polyco,timeSeries.getHeader().getSourceID());
        }
        
        
        // Compute first subint
        long start = 0;
        long end = start + timeSeries.getHeader().getNPoints()/nsub;
        
        double complete = 0;
        double completeEta = 30*(1.0/nsub);
        
        
        
        PulsarHunter.out.print("SubintFolder - [");
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
            PulsarHunter.out.println("SubintFolder - Folding with polyco, slow!");
            PulsarHunter.out.println("SubintFolder - Warning: accn/jerk vals will be wrong.");
            folder = new PolyCoFolder(polyco,timeSeries[0].getHeader().getSourceID());
        }
        
        
        
        
        // Compute first subint
        long start = 0;
        long end = start + timeSeries[0].getHeader().getNPoints()/nsub;
        
        TimeSeries[] subTS = new TimeSeries[nbands];
        MultiChannelTimeSeries subTSAll;
        double complete = 0;
        double completeEta = 30*(1.0/nsub);
        
        PulsarHunter.out.print("SubintFolder - [");
        
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
                System.arraycopy(crazySubints[b],0,foldedSubints[b][i],0,crazySubints[b].length);
                
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
