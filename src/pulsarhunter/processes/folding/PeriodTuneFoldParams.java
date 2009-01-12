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
package pulsarhunter.processes.folding;

import java.io.IOException;
import pulsarhunter.Convert;
import pulsarhunter.Data;
import pulsarhunter.PulsarHunter;



public class PeriodTuneFoldParams implements Data{
    private int nsub = 64;
    private double centrePeriod = 100.0;

    private double centerAccn = 0.0;
    private double centerJerk = 0.0;
    
    private double pcoarseness = -1;
    private double prange = -1;
    
    
//    private double pdcoarseness = -1;
//    private double pdrange = -1;
//    private double pddcoarseness = -1;
//    private double pddrange = -1;
    
    private double accnRange = 100.0;
    private double accnStep = -1;
    
    private double jerkRange = 2.0;
    private double jerkStep = -1;
    
    
    private double dmcoarseness = 1;
    private double dmrange = 5;
    
    private boolean useAccn = false;
    private boolean useJerk = false;
    private boolean recalcInitialSNR = false;
    private boolean recalcSubints = false;
    
    private int[] ignoreBands = new int[0];
    private int[] ignoreSints = new int[0];
    
    private int nprofilebins = 128;
    private double centerDM = 0.0;
    
    private double sintIgnoreThreashold = 1.2;
    private boolean ignoreLoudSubbands = false;
    private double sbandIgnoreThreashold = 1.2;
    

    
    public int getNsub() {
        return nsub;
    }
    
    public void setNsub(int nsub) {
        this.nsub = nsub;
    }
    
    public double getCentrePeriod() {
        return centrePeriod;
    }
    
    public void setCenterPeriod(double centrePeriod) {
        this.centrePeriod = centrePeriod;
    }
    
    
    public double getCenterPdot(){
        return Convert.accToPdot(centrePeriod,centerAccn);
        
    }
    
    public String getDataType() {
       return null;
    }
    
    public double getCenterPddot(){
        return Convert.jerkToPddot(centrePeriod,centerJerk,this.getCenterPdot());
    }
    
    public void setCentrePdot(double centerPdot) {
        this.setCenterAccn(Convert.pdotToAcc(this.centrePeriod,centerPdot));
    }
    
    
    public int getNprofilebins() {
        return nprofilebins;
    }
    
    public void setNprofilebins(int nprofilebins) {
        this.nprofilebins = nprofilebins;
    }
    
    
    
    public void release() {
        return;
    }
    
    public Header getHeader() {
        return null;
    }
    
    public void flush() throws IOException {
        return;
    }
    
    public double getPstep() {
        if(pcoarseness<0)this.pcoarseness = this.getPrange()/100;
        return pcoarseness;
    }
    
    public void setPstep(double pcoarseness) {
        
        this.pcoarseness = pcoarseness;
    }
    
    public double getPrange() {
        if(prange < 0){
            prange = 0.001*this.centrePeriod;
            PulsarHunter.out.println("TuneParams - Using default period range of "+(prange*1000.0)+" ms");
            
        }
        
        
        return prange;
    }
    
    public void setPrange(double prange) {
        this.prange = prange;
    }
    
    
    public void setPdstep(double pdcoarseness) {
        
        this.setAccnStep(Convert.pdotToAcc(centrePeriod,pdcoarseness));
    }
    
    
    public void setPdrange(double pdrange) {
        this.setAccnRange(Convert.pdotToAcc(centrePeriod,pdrange));
    }
    
    public double getDmstep() {
        return dmcoarseness;
    }
    
    public void setDmstep(double dmcoarseness) {
        this.dmcoarseness = dmcoarseness;
    }
    
    public double getDmrange() {
        return dmrange;
    }
    
    public void setDmrange(double dmrange) {
        this.dmrange = dmrange;
    }
    
    
    public void setUsePDot(boolean usePDot) {
        this.setUseAccn(isUseAccn());
    }
    
    public int[] getIgnoreBands() {
        return ignoreBands;
    }
    
    public void setIgnoreBands(int[] ignoreBands) {
        this.ignoreBands = ignoreBands;
    }
    
    public double getCenterDM() {
        return centerDM;
    }
    
    public void setCenterDM(double centerDM) {
        this.centerDM = centerDM;
    }
    
    public double getSintIgnoreThreashold() {
        return sintIgnoreThreashold;
    }
    
    public void setSintIgnoreThreashold(double sintIgnoreThreashold) {
        this.sintIgnoreThreashold = sintIgnoreThreashold;
    }
    
    
    
    public double getSbandIgnoreThreashold() {
        return sbandIgnoreThreashold;
    }
    
    public void setSbandIgnoreThreashold(double sbandIgnoreThreashold) {
        this.sbandIgnoreThreashold = sbandIgnoreThreashold;
    }
    
    public boolean isIgnoreLoudSubbands() {
        return ignoreLoudSubbands;
    }
    
    public void setIgnoreLoudSubbands(boolean ignoreLoudSubbands) {
        this.ignoreLoudSubbands = ignoreLoudSubbands;
    }
    
    public int[] getIgnoreSints() {
        return ignoreSints;
    }
    
    public void setIgnoreSints(int[] ignoreSints) {
        this.ignoreSints = ignoreSints;
    }
    
    
    public void setCentrePddot(double centerPddot) {
        this.centerJerk = Convert.jerkToPddot(centrePeriod,centerPddot,this.getCenterPdot());
    }
    
    
    public void setPddstep(double pddcoarseness) {
        this.jerkStep = Convert.pddotToJerk(this.centrePeriod,pddcoarseness,0);
    }
    
    
    public void setPddrange(double pddrange) {
        this.jerkRange = Convert.pddotToJerk(this.centrePeriod,pddrange,0);
    }
    
    
    public void setUsePDDot(boolean usePDDot) {
        this.setUseJerk(usePDDot);
    }
    
    public double getCenterAccn() {
        return centerAccn;
    }
    
    public void setCenterAccn(double centerAccn) {
        this.centerAccn = centerAccn;
    }
    
    public double getCenterJerk() {
        return centerJerk;
    }
    
    public void setCenterJerk(double centerJerk) {
        this.centerJerk = centerJerk;
    }
    
    public double getAccnRange() {
        return accnRange;
    }
    
    public void setAccnRange(double accnRange) {
        this.accnRange = accnRange;
    }
    
    public double getAccnStep() {
        if(accnStep<0)this.accnStep = this.getAccnRange()/50;
        return accnStep;
    }
    
    public void setAccnStep(double accnStep) {
        
        this.accnStep = accnStep;
    }
    
    public double getJerkRange() {
        return jerkRange;
    }
    
    public void setJerkRange(double jerkRange) {
        this.jerkRange = jerkRange;
    }
    
    public double getJerkStep() {
        if(jerkStep<0)this.jerkStep = this.getJerkRange()/10;
        return jerkStep;
    }
    
    public void setJerkStep(double jerkStep) {
        this.jerkStep = jerkStep;
    }
    
    public boolean isUseAccn() {
        return useAccn;
    }
    
    public void setUseAccn(boolean useAccn) {
        this.useAccn = useAccn;
    }
    
    public boolean isUseJerk() {
        return useJerk;
    }
    
    public void setUseJerk(boolean useJerk) {
        this.useJerk = useJerk;
    }

    public boolean getRecalcInitialSNR() {
        return recalcInitialSNR;
    }

    public void setRecalcInitialSNR(boolean recalcInitialSNR) {
        this.recalcInitialSNR = recalcInitialSNR;
    }

    public boolean getRecalcSubints() {
        return recalcSubints;
    }

    public void setRecalcSubints(boolean recalcSubints) {
        this.recalcSubints = recalcSubints;
    }

   
    
}
