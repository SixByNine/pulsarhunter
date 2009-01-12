/*
 * EpnFile.java
 *
 * Created on July 22, 2007, 2:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;


import coordlib.Coordinate;
import coordlib.Telescope;
import epn.EPNStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author mkeith
 */
public class EpnFile extends epn.EPNFile implements MultiprofileFile{
    private Header header;// = new Header();
    private File file = null;
    
    public EpnFile(File file){
        this.file = file;
        header = new Header(this);
    }
    
    
    public String getDataType() {
        return "EPN";
    }
    public void read()throws IOException{
        if(file!=null){
            try {
                this.readEPN(new FileReader(file));
                
            } catch (FileNotFoundException ex) {
                throw ex;
            } catch (IOException ex) {
                throw new IOException("Error reading epn file "+file.getName());
            }
        }
        
    }
    
    
    public void write()throws IOException{
        if(file!=null){
            try {
                Writer w = new FileWriter(file);
                this.writeEPN(w);
                w.close();
            } catch (FileNotFoundException ex) {
                throw ex;
            } catch (IOException ex) {
                throw new IOException("Error writing epn file "+file.getName());
            }
        }
        
    }
    
    public void release() {
        this.file=null;
        this.setStreams(null);
        this.header=null;
    }
    
    public Header getHeader() {
        return this.header;
    }
    
    public void flush() throws IOException {
        this.write();
    }
    
    public void addProfile(double[] profile, double time, double freq, double band) {
        EPNStream s = this.createStream(profile,time,freq,band);
        s.setIdfield("I");
    }
    
    public double[] getProfile(int band, int time, int pol) {
        return this.getStream(band,time,pol).getProfile();
    }
    
    private class Header extends MultiprofileFile.Header{
        epn.EPNFile epnfile;
        Header(epn.EPNFile epnfile){
            this.epnfile = epnfile;
        }
        
        public void setTobs(double obstime) {
            
        }
        
        public void setSourceID(String sourceID) {
            this.epnfile.setCname(sourceID);
            this.epnfile.setJname(sourceID);
        }
        
        public void setMjdStart(double mjdStart) {
            this.epnfile.setEpoch(mjdStart);
        }
        
        
        /* ?? */
        public void setFrequency(double frequency) {
        }
        
        public void setBandwidth(double bandwidth) {
        }
        
        
        public void setTelescope(Telescope telescope) {
            this.epnfile.setTelescope(telescope);
        }
        
        public void setCoord(Coordinate coord) {
            this.epnfile.setCoord(coord);
        }
        
        public void setBarryCentered(boolean barryCentered) {
        }
        
        public boolean isBarryCentered() {
            return false;
        }
        
        /**
         * We have to guess tobs as it is not stored in the epn file
         *
         * Assume that tobs = (timestamp2-timestamp1)*ntimestamps...
         */
        public double getTobs() {
            // this can only be done for multitimestamp files...
            if(this.getNumberOfTimeStamps()==1)return -1.0;
            else{
                
                double t1 = this.epnfile.getStream(0,this.epnfile.getNTimeStamps(0)-1,0).getTstart();
                double t0 = this.epnfile.getStream(0,0,0).getTstart();
                return (t1-t0)/10e6;
            }
        }
        
        public Telescope getTelescope() {
            return epnfile.getTelescope();
        }
        
        public String getSourceID() {
            return epnfile.getJname();
        }
        
        public double getMjdStart() {
            return this.epnfile.getEpoch();
        }
        
        public double getFrequency() {
            return 0.0;
        }
        
        public Coordinate getCoord() {
            return this.epnfile.getCoord();
        }
        
        public double getBandwidth() {
            return 0.0;
        }
        
        public void setBinsPerProfile(int binsPerProfile) {
            this.epnfile.setNbins(binsPerProfile);
        }
        
        public void setNumberOfTimeStamps(int numberOfProfPerFreq) {
            
        }
        
        public void setNumberOfChannels(int numberOfFreqPerPol) {
            
        }
        
        public void setNumberOfPolarisations(int numberOfPolarisations) {
            
        }
        
        public int getBinsPerProfile() {
            return this.epnfile.getNbins();
        }
        
        public int getNumberOfChannels() {
            return this.epnfile.getNFreqChannels();
        }
        
        public int getNumberOfTimeStamps() {
            return this.epnfile.getNTimeStamps(0);
        }
        
        public int getNumberOfPolarisations() {
            return this.epnfile.getNpol();
        }
        
        public void setBinWidth(double binWidth) {
            this.epnfile.setTbin(binWidth*1e6);
        }
        
        public void setTimeResolution(double timeResolution) {
            this.epnfile.setTres(timeResolution*1e6);
        }
        
        public double getBinWidth() {
            return this.epnfile.getTbin()/1e6;
        }
        
        public double getTimeResolution() {
            return this.epnfile.getTres()/1e6;
        }
        
        
        public double getBandRMS(int band, int pol) {
            return this.epnfile.getStream(band,0,pol).getRms();
        }
        
        public double getBandWidth(int band) {
            return this.epnfile.getStream(band,0,0).getBand();
        }
        
        public double getBandFreq(int band) {
            return this.epnfile.getStream(band,0,0).getFreq();
        }
        
        public int getNumberOfFoldsPerProfile() {
            return this.epnfile.getNint();
        }
        
        public void setFoldingPeriod(double foldingPeriod) {
            this.epnfile.setPeriod(foldingPeriod);
        }
        
        public void setNumberOfFoldsPerProfile(int numberOfFoldsPerProfile) {
            return;
        }
        
        public double getFoldingPeriod() {
            return this.epnfile.getPeriod();
        }
        
        
        
        
    }
    
    
}
