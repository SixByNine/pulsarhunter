/*
 * MultiprofileFile.java
 *
 * Created on July 22, 2007, 2:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import pulsarhunter.Data;

/**
 *
 * @author mkeith
 */
public interface MultiprofileFile extends Data<MultiprofileFile.Header>{
    public void addProfile(double[] profile,double time, double freq,double band);
    
    public double[] getProfile(int band, int time, int pol);
    
    public boolean isComplete();
    
    public abstract class Header extends Data.Header{
        private int binsPerProfile;
        
        private int numberOfPolarisations;
        private int numberOfFreqPerPol;
        private int numberOfProfPerFreq;
        private int numberOfFoldsPerProfile;
        private double binWidth;
        private double timeResolution;
        private double foldingPeriod;
        
        public int getBinsPerProfile() {
            return binsPerProfile;
        }
        
        public void setBinsPerProfile(int binsPerProfile) {
            this.binsPerProfile = binsPerProfile;
        }
        
        public int getNumberOfPolarisations() {
            return numberOfPolarisations;
        }
        
        public void setNumberOfPolarisations(int numberOfPolarisations) {
            this.numberOfPolarisations = numberOfPolarisations;
        }
        
        public int getNumberOfChannels() {
            return numberOfFreqPerPol;
        }
        
        public void setNumberOfChannels(int numberOfFreqPerPol) {
            this.numberOfFreqPerPol = numberOfFreqPerPol;
        }
        
        public int getNumberOfTimeStamps() {
            return numberOfProfPerFreq;
        }
        
        public void setNumberOfTimeStamps(int numberOfProfPerFreq) {
            this.numberOfProfPerFreq = numberOfProfPerFreq;
        }
        
        public double getBinWidth() {
            return binWidth;
        }
        
        public void setBinWidth(double binWidth) {
            this.binWidth = binWidth;
        }
        
        public double getTimeResolution() {
            return timeResolution;
        }
        
        public void setTimeResolution(double timeResolution) {
            this.timeResolution = timeResolution;
        }
        
        
        
        public abstract double getBandRMS(int band, int pol);
        
        public abstract double getBandFreq(int band);
        
        public abstract double getBandWidth(int band);

        public int getNumberOfFoldsPerProfile() {
            return numberOfFoldsPerProfile;
        }

        public void setNumberOfFoldsPerProfile(int numberOfFoldsPerProfile) {
            this.numberOfFoldsPerProfile = numberOfFoldsPerProfile;
        }

        public double getFoldingPeriod() {
            return foldingPeriod;
        }

        public void setFoldingPeriod(double foldingPeriod) {
            this.foldingPeriod = foldingPeriod;
        }
        
    }
}
