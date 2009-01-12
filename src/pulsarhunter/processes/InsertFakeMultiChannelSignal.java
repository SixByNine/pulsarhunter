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
 * Created on 15 March 2007, 16:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.VariableTimeModel;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.WritableMultiChannelTimeSeries;
import pulsarhunter.datatypes.WritableTimeSeries;
import pulsarhunter.datatypes.sigproc.SigprocBandedTimeSeries;

/**
 *
 * @author mkeith
 */
public class InsertFakeMultiChannelSignal extends InsertFakeSignal{
    
    
    private double dm = 0.0;
    
    private MultiChannelTimeSeries data;
    private WritableMultiChannelTimeSeries outTS;
    private VariableTimeModel foldingModel;
    
    
    public InsertFakeMultiChannelSignal(MultiChannelTimeSeries data,WritableMultiChannelTimeSeries outTS,VariableTimeModel foldingModel){
        super(null,null,foldingModel);
        this.foldingModel = foldingModel;
        this.data = data;
        this.outTS = outTS;
    }
    
    
    
    private void fake(MultiChannelTimeSeries data,WritableMultiChannelTimeSeries outTS){
        
        double[] power = new double[data.getHeader().getNumChannel()];
        for(int c = 0; c < this.data.getHeader().getNumChannel();c++){
            double rms = data.getOnechannel(0).getRMS(0,10000);
            power[c] = rms*super.getSnr();
            power[c] /= Math.sqrt(data.getHeader().getNPoints());
            power[c] /= data.getHeader().getNumChannel();
        }
        
        
        double sampleRate = data.getHeader().getTSamp();
        
        long nbins = data.getHeader().getNPoints();
        double mult = 1.0/(2*Math.PI*Math.pow(super.getPulseWidth()/(Math.PI*2.0),2));
        
        // PulsarHunter.out.println("TimeSeriesFolder - Using slower period drift folding");
        double naturalTimePassed=0.0;
        double timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
        double targetBin_d = 0;
        double nulltime = 0.0;
        int targetBin;
        
        final double kdm = 4.148808e3;//s
        
        
        double flo = data.getOnechannel(0).getHeader().getFrequency();
        double[] delays = new double[data.getHeader().getNumChannel()];
        for(int c = 0; c < this.data.getHeader().getNumChannel();c++){
            
            double fhi = data.getOnechannel(c).getHeader().getFrequency();
            
            delays[c] = (getDm() *  (kdm*(1.0/(flo*flo) - 1.0/(fhi*fhi))));
            
            delays[c]/=super.getPeriod();
        }
        
        boolean pulsing = true;
        // if we are nulling, randomly be on or off...
        if(isNulling()) pulsing = Math.random() < getPulsetimescale()/(getPulsetimescale()+getNulltimescale());
        // The first time we are a random length through a pulse or null
        double nulllength = Math.random() * Math.pow(Math.random(),2) * getPulsetimescale();
        for(long bin = 0; bin < nbins; bin++){
            if(this.isNulling()){
                if(nulltime > nulllength){
                    pulsing = ! pulsing;
                    nulllength = this.getNullLength(pulsing);
                    
                    nulltime = 0.0;
                }
            }
            
            
            timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
            
            double pulsePhase = timePassed / super.getPeriod() - (int)(timePassed/super.getPeriod());
            
            
            for(int c = 0; c < this.data.getHeader().getNumChannel();c++){
//                    System.out.println(delays[c]);
                double dm_pulsephase = pulsePhase + delays[c];
                while(dm_pulsephase < 0.0)dm_pulsephase+=1.0;
                while(dm_pulsephase > 1.0)dm_pulsephase-=1.0;
                
                dm_pulsephase -= 0.5;
                double pulsepower = power[c] * varianceFunction(pulsePhase,getPulseWidth()*getPulseWidth(),0.0,VarianceFunction.Gausian);
                //double pulsepower = power[c]* mult*Math.exp(-mult*Math.pow(dm_pulsephase,2));
                if(!pulsing)pulsepower = 0.0;
                outTS.writeBin(bin,c,(float)(data.getOnechannel(c).getBin(bin) + pulsepower));
            }
            //  outTS.writeBin(bin,(float)(pulsepower));
            
            naturalTimePassed += sampleRate;
            nulltime += sampleRate;
        }
        
        
    }
    
    public void run() {
        // set the header info!
        if(data instanceof SigprocBandedTimeSeries && outTS instanceof SigprocBandedTimeSeries ){
            // Both headers are the same so copy them!
            ((SigprocBandedTimeSeries)outTS).copySigprocHeader(((SigprocBandedTimeSeries)data).getSigprocHeader());
            outTS.getHeader().setSourceID(data.getHeader().getSourceID()+" PH-INSERTFAKE");
        }
        
        
        fake(data,outTS);
    }
    
    public double getDm() {
        return dm;
    }
    
    public void setDm(double dm) {
        this.dm = dm;
    }
    
    
}
