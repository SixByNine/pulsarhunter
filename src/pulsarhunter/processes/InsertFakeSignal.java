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

import pulsarhunter.PulsarHunter;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.VariableTimeModel;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.WritableTimeSeries;
import pulsarhunter.datatypes.sigproc.SigprocTimeSeries;

/**
 *
 * @author mkeith
 */
public class InsertFakeSignal implements PulsarHunterProcess{
    private double period=1.0;
    private double snr=1;
    private double pulseWidth=0.01;
    
    private boolean nulling = false;
    private double nulltimescale = 100.0;
    private double pulsetimescale = 10.0;
    private double nullvariance = 1.0;
    private VarianceFunction nullStatistics = VarianceFunction.Gausian;
    
    private TimeSeries data;
    private WritableTimeSeries outTS;
    private VariableTimeModel foldingModel;
    
    public enum VarianceFunction{Gausian};
    
    public InsertFakeSignal(TimeSeries data,WritableTimeSeries outTS,VariableTimeModel foldingModel){
        this.foldingModel = foldingModel;
        this.data = data;
        this.outTS = outTS;
    }
    
    
    
    public void fake(TimeSeries data,WritableTimeSeries outTS){
        double rms = data.getRMS(0,10000);
        double power = rms*snr;
        power /= Math.sqrt(data.getHeader().getNPoints());
        
        double sampleRate = data.getHeader().getTSamp();
        
        long nbins = data.getHeader().getNPoints();
        double mult = 1.0/(2*Math.PI*Math.pow(pulseWidth,2));
	double exp = 1.0/(2*pulseWidth);
        
        // PulsarHunter.out.println("TimeSeriesFolder - Using slower period drift folding");
        double naturalTimePassed=0.0;
        double timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
        double targetBin_d = 0;
        double nulltime = 0.0;
        int targetBin;
        double complete = 0;
        double completeEta =  30.0*1.0/(double)nbins;
        boolean pulsing = true;
        
        PulsarHunter.out.println("InsertFake - |0%                        100%|");
        PulsarHunter.out.print("InsertFake - [");
        
        // if we are nulling, randomly be on or off...
        if(isNulling()) pulsing = Math.random() < getPulsetimescale()/(getPulsetimescale()+getNulltimescale());
        // The first time we are a random length through a pulse or null
        double nulllength = Math.random() * Math.pow(Math.random(),2) * getPulsetimescale();
        for(long bin = 0; bin < nbins; bin++){
            complete += completeEta;
            while(complete > 1){
                PulsarHunter.out.print(".");
                PulsarHunter.out.flush();
                complete -= 1.0;
            }
            if(this.isNulling()){
                if(nulltime > nulllength){
                    pulsing = ! pulsing;
                    nulllength = this.getNullLength(pulsing);
                    
                    nulltime = 0.0;
                }
            }
            
            timePassed = foldingModel.getAddjustedTime(naturalTimePassed);
            
            double pulsePhase = timePassed / period - (int)(timePassed/period);
            pulsePhase -= 0.5;
            
            double pulsepower = power * varianceFunction(pulsePhase,getPulseWidth()*getPulseWidth(),0.0,VarianceFunction.Gausian);//mult*Math.exp(-exp*Math.pow(pulsePhase,2));
            if(!pulsing)pulsepower = 0.0;
            outTS.writeBin(bin,(float)(data.getBin(bin) + pulsepower));
            //  outTS.writeBin(bin,(float)(pulsepower));
            
            naturalTimePassed += sampleRate;
            nulltime += sampleRate;
        }
        PulsarHunter.out.println("] done.");
        
    }
    
    public void run() {
        // set the header info!
        if(data instanceof SigprocTimeSeries && outTS instanceof SigprocTimeSeries ){
            // Both headers are the same so copy them!
            ((SigprocTimeSeries)outTS).copySigprocHeader(((SigprocTimeSeries)data).getSigprocHeader());
//            outTS.getHeader().setSourceID(data.getHeader().getSourceID()+" PH-INSERTFAKE");
        }
        
        
        fake(data,outTS);
    }
    
    public double getPeriod() {
        return period;
    }
    
    public void setPeriod(double period) {
        this.period = period;
    }
    
    public double getSnr() {
        return snr;
    }
    
    public void setSnr(double snr) {
        this.snr = snr;
    }
    
    public double getPulseWidth() {
        return pulseWidth;
    }
    
    public void setPulseWidth(double pulseWidth) {
        this.pulseWidth = pulseWidth;
    }
    
    public boolean isNulling() {
        return nulling;
    }
    
    public void setNulling(boolean nulling) {
        this.nulling = nulling;
    }
    
    public double getNulltimescale() {
        return nulltimescale;
    }
    
    public void setNulltimescale(double nulltimescale) {
        this.nulltimescale = nulltimescale;
    }
    
    public double getPulsetimescale() {
        return pulsetimescale;
    }
    
    public void setPulsetimescale(double pulsetimescale) {
        this.pulsetimescale = pulsetimescale;
    }
    double getNullLength(boolean pulsing){
	double timescale = getNulltimescale();
        if(pulsing)timescale = getPulsetimescale();

	double variance = nullvariance * timescale;
	double eta = variance/20.0;
        double nulllength = 0.0;
        
        double random = Math.random();
        
	
        double x = timescale - 6*variance;
        double cumulitive = 0.0;

        while(cumulitive < random){
            double y1 = varianceFunction(x,variance,timescale,this.nullStatistics);
//            System.out.println("x1="+x+"\ty1="+y1+"\tra="+random);
            x+=eta;
            double y2 = varianceFunction(x,variance,timescale,this.nullStatistics);
//            System.out.println("x1="+x+"\ty2="+y2+"\tra="+random);
            double area = eta*(y1+y2)/2.0;
//            System.out.println("cumu="+cumulitive);
            cumulitive+=area;
//		System.out.println("x="+x+"\ty1="+y1+"\ty2="+y2+"\tr="+random+"\tc="+cumulitive);
            nulllength = x - eta/2.0;
	    if(x > 6.0*variance)break;
        }
	if(nulllength < 0)nulllength = period; 

        return nulllength;
    }
    
    protected double varianceFunction(double x,double sigma, double mean,VarianceFunction func){
        switch(func){
            default:
            case Gausian:
                double A = 1.0/(Math.sqrt(2*Math.PI)*sigma);
		double B = 1.0/(2*Math.pow(sigma,2));

                return A*Math.exp(-B*Math.pow(x-mean,2));
        }
    }
    
    public double getNullvariance() {
        return nullvariance;
    }
    
    public void setNullvariance(double nullvariance) {
        this.nullvariance = nullvariance;
    }
    
    public VarianceFunction getNullStatistics() {
        return nullStatistics;
    }
    
    public void setNullStatistics(VarianceFunction nullStatistics) {
        this.nullStatistics = nullStatistics;
    }
}
