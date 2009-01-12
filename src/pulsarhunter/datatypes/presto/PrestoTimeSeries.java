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
 * PrestoTimeSeries.java
 *
 * Created on 26 September 2006, 15:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes.presto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel.MapMode;
import pulsarhunter.datatypes.TimeSeries;

/**
 *
 * @author mkeith
 */
public class PrestoTimeSeries extends TimeSeries {
    
    
    private int bufferSize;
    private File dataFile;
    private File headerFile;
    private FloatBuffer fb;
    private FileInputStream in;
    private long currentFilePos = Long.MAX_VALUE;
    private PrestoTimeSeries.Header header;
    private long fileLength;
    
    public PrestoTimeSeries(File dataFile, File headerFile, int bufferSize) {
        header = new PrestoTimeSeries.Header(headerFile);
        this.bufferSize = bufferSize;
        this.headerFile = headerFile;
        this.dataFile = dataFile;
        this.fileLength =  this.dataFile.length();
        
    }
    /** Creates a new instance of PrestoTimeSeries */
  /*  public PrestoTimeSeries(File dataFile, File headerFile, int bufferSize,long startBin, long endBin) {
        header = new PrestoTimeSeries.Header(headerFile);
        this.bufferSize = bufferSize;
        this.headerFile = headerFile;
        this.dataFile = dataFile;
        this.startBin = startBin;
        this.endBin = endBin;
        this.header.N = endBin - startBin;
   
    }*/
    
    
    
    public TimeSeries.Header getHeader() {
        return this.header;
        
    }
    
    public float[] getDataAsFloats() {
        return null;
    }
    
    
    
    
  /*  public float getBin(long bin) {
        float f = -1.0f;
       // byte[] byteBuffer = new byte[4];
        ByteBuffer byteBuffer  = new ByteBuffer();
        try {
   
            if(bin < currentFilePos){
                in  = new BufferedInputStream(new FileInputStream(this.dataFile));
                currentFilePos = 0;
                for(int i = 0 ; i < this.bufferSize; i++){
                    in.read(byteBuffer);
                    this.buffer[i] = arr2float(byteBuffer,0);
                }
            }
   
            if( bin >= currentFilePos + this.bufferSize){
                long skipBytes = 4*(bin - currentFilePos-1);
   
                in.skip(skipBytes);
   
                currentFilePos += skipBytes/4;
                for(int i = 0 ; i < this.bufferSize; i++){
                    in.read(byteBuffer.array());
   
                }
            }
   
            f =  this.buffer[(int)(bin - currentFilePos)];
   
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return f;
    }
   */
    
    
    
    public float getBin(long bin) {
        float f = -1.0f;
        
        
        try {
            if(in == null ){
                
                in = new FileInputStream(this.dataFile);
                ByteBuffer bb = in.getChannel().map(MapMode.READ_ONLY,bin*4,this.bufferSize*4);
                bb.order(ByteOrder.nativeOrder());
                fb = bb.asFloatBuffer();
                currentFilePos = bin;
                //System.out.println(fb.order().toString() + " Native "+ByteOrder.nativeOrder().toString());
                
            }
            long lim = 0L;
            if(bin >= currentFilePos+this.bufferSize){
                
                if((bin+this.bufferSize)*4 > fileLength){
                    lim = fileLength - (this.bufferSize*4);
                } else lim = bin*4;
                ByteBuffer bb= in.getChannel().map(MapMode.READ_ONLY, lim, this.bufferSize * 4);
                
                bb.order(ByteOrder.nativeOrder());
                fb = bb.asFloatBuffer();
                currentFilePos = lim/4;
            }
            
            if(bin > currentFilePos+fb.position()){
                
                while(bin != currentFilePos+fb.position()-1){
                    
                    fb.get();
                    
                    
                    
                }
                
            }
            
            if(bin < currentFilePos+fb.position()){
                in.close();
                in = new FileInputStream(this.dataFile);
                ByteBuffer bb = in.getChannel().map(MapMode.READ_ONLY,bin*4,this.bufferSize*4);
                bb.order(ByteOrder.nativeOrder());
                fb = bb.asFloatBuffer();
                currentFilePos = bin;
                
            }
            
            if(bin == currentFilePos+fb.position()){
                f = fb.get();
            }else {
                
            }
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return f;
    }
    
    private void readFloats(float[] readIn, long startbin){
        
        
    }
    
    class Header extends TimeSeries.Header{
        String[] bands = new String[]{"Radio", "IR", "Optical", "UV", "X-ray", "Gamma"};
        
        String[] scopes = new String[]{"None (Artificial Data Set)", "Arecibo", "Parkes", "VLA", "MMT", "Las Campanas 2.5m", "Mt. Hopkins 48in", "Other"};
        
        //PrestoParams
        
        double ra_s;                /* Right ascension seconds (J2000)       */
        double dec_s;               /* Declination seconds (J2000)           */
        double N;                   /* Number of bins in the time series     */
        double dt;                  /* Width of each time series bin (sec)   */
        double fov;                 /* Diameter of Beam or FOV in arcsec     */
        double mjd_f;               /* Epoch of observation (MJD) frac part  */
        double dm;                  /* Radio -- Dispersion Measure (cm-3 pc) */
        double freq;                /* Radio -- Low chan central freq (Mhz)  */
        double freqband;            /* Radio -- Total Bandwidth (Mhz)        */
        double chan_wid;            /* Radio -- Channel Bandwidth (Mhz)      */
        double wavelen;             /* IR,Opt,UV -- central wavelength (nm)  */
        double waveband;            /* IR,Opt,UV -- bandpass (nm)            */
        double energy;              /* x-ray,gamma -- central energy (kev)   */
        double energyband;          /* x-ray,gamma -- energy bandpass (kev)  */
        double[] onoff = new double[40]; /* Bin number pairs where obs is "on"    */
        int num_chan;               /* Radio -- Number Channels              */
        int mjd_i;                  /* Epoch of observation (MJD) int part   */
        int ra_h;                   /* Right ascension hours (J2000)         */
        int ra_m;                   /* Right ascension minutes (J2000)       */
        int dec_d;                  /* Declination degrees (J2000)           */
        int dec_m;                  /* Declination minutes (J2000)           */
        int bary;                   /* Barycentered?  1=yes, 0=no            */
        int numonoff;               /* The number of onoff pairs in the data */
        String notes;            /* Any additional notes                  */
        String name;             /* Data file name without suffix         */
        String object;           /* Object being observed                 */
        String instrument;       /* Instrument used                       */
        String observer;         /* Observer[s] for the data set          */
        String analyzer;         /* Who analyzed the data                 */
        String telescope;         /* Telescope used                        */
        String band;              /* Type of observation (EM band)         */
        String filt;               /* IR,Opt,UV -- Photometric Filter       */
        
        
        
        private Header(File infFile){
            super();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(infFile));
                String line = reader.readLine();
                this.name = line.split("=\\s")[1].trim();
                
                line = reader.readLine();
                this.telescope = line.split("=\\s")[1].trim();
                
                if(!name.equals(scopes[0])){
                    
                    line = reader.readLine();
                    this.instrument = line.split("=\\s")[1].trim();
                    
                    line = reader.readLine();
                    this.object = line.split("=\\s")[1].trim();
                    
                    line = reader.readLine();
                    String raString = line.split("=\\s")[1].trim();
                    String[] elems = raString.split("\\:");
                    this.ra_h =Integer.parseInt(elems[0].trim());
                    this.ra_m =Integer.parseInt(elems[1].trim());
                    this.ra_s =Double.parseDouble(elems[2].trim());
                    
                    line = reader.readLine();
                    String deString = line.split("=\\s")[1].trim();
                    elems = deString.split("\\:");
                    this.dec_d =Integer.parseInt(elems[0].trim());
                    this.dec_m =Integer.parseInt(elems[1].trim());
                    this.dec_s =Double.parseDouble(elems[2].trim());
                    
                    
                    line = reader.readLine();
                    this.observer = line.split("=\\s")[1].trim();
                    
                    line = reader.readLine();
                    String mjdString = line.split("=\\s")[1].trim();
                    elems = mjdString.split("\\.");
                    this.mjd_i = Integer.parseInt(elems[0]);
                    this.mjd_f = Double.parseDouble("0."+elems[1]);
                    
                    line = reader.readLine();
                    this.bary = Integer.parseInt(line.split("=\\s")[1].trim());
                    
                } else {
                    this.object = "fake pulsar";
                }
                
                
                line = reader.readLine();
                this.N = Double.parseDouble(line.split("=\\s")[1]);
                
                line = reader.readLine();
                this.dt = Double.parseDouble(line.split("=\\s")[1]);
                
                
                line = reader.readLine();
                this.numonoff = Integer.parseInt(line.split("=\\s")[1].trim());
                
                if(this.numonoff > 0){
                    int ii = 0;
                    do{
                        line = reader.readLine();
                        String datLine = line.split("=\\s")[1];
                        String[] elems = datLine.split(",");
                        this.onoff[ii] = Double.parseDouble(elems[0]);
                        this.onoff[ii+1] = Double.parseDouble(elems[1]);
                        ii += 2;
                        
                    } while((this.onoff[ii-1] < (this.N - 1)) && (ii < onoff.length));
                    this.numonoff = ii / 2;
                    if(this.numonoff > this.onoff.length){
                        throw new IOException("There are two many OnOff values. Max: "+onoff.length);
                    }
                }
                
                if(!name.equals(scopes[0])){
                    line = reader.readLine();
                    this.band = line.split("=\\s")[1].trim();
                    
                    if(this.band.equals(this.bands[0])){
                        line = reader.readLine();
                        this.fov = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.dm = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.freq = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.freqband = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.num_chan = Integer.parseInt(line.split("=\\s")[1].trim());
                        line = reader.readLine();
                        this.chan_wid = Double.parseDouble(line.split("=\\s")[1]);
                    } else if(this.band.equals(this.bands[4]) || this.band.equals(this.bands[5])){
                        line = reader.readLine();
                        this.fov = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.energy = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.energyband = Double.parseDouble(line.split("=\\s")[1]);
                    } else {
                        line = reader.readLine();
                        this.filt = line.split("=\\s")[1].trim();
                        line = reader.readLine();
                        this.fov = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.wavelen = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.waveband = Double.parseDouble(line.split("=\\s")[1]);
                    }
                    
                }
                
                line = reader.readLine();
                this.analyzer = line.split("=\\s")[1].trim();
                line = reader.readLine();
                line = reader.readLine();
                this.notes = line.trim();
                
                
                reader.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        public void setSourceID(String sourceID) {
            this.object = sourceID;
        }
        
        public void setNPoints(long nPoints) {
            this.N = nPoints;
        }
        
        public void setFrequency(double frequency) {
            this.freq = frequency;
        }
        
        public void setTobs(double obstime) {
        }
        
        public void setBandwidth(double bandwidth) {
            this.freqband = bandwidth;
        }
        
        public void setTSamp(double tSamp) {
            this.dt = tSamp;
        }
        
        public void setMjdStart(double mjdStart) {
            this.mjd_i = (int)mjdStart;
            this.mjd_f = mjdStart - this.mjd_i;
        }
        
        public double getTSamp() {
            return this.dt;
            
        }
        
        public double getTobs() {
            return this.dt*this.N;
            
        }
        
        public double getFrequency() {
            return this.freq;
        }
        
        public long getNPoints() {
            return (long)this.N;
            
        }
        
        public double getBandwidth() {
            return this.freqband;
        }
        
        public double getMjdStart() {
            return this.mjd_i + this.mjd_f;
        }
        
        public String getSourceID() {
            return this.object;
        }
        
        
        
    }
    
    
    
    
    
    public static float arr2float(byte[] arr, int start) {
        
        int i = 0;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];
        for (i = start; i < (start + len); i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }
        int accum = 0;
        i = 0;
        for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
            accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
            i++;
        }
        return Float.intBitsToFloat(accum);
    }
    
    public void release() {
        this.dataFile = null;
        this.header = null;
        this.headerFile = null;
        this.fb = null;
        this.in = null;
    }
    
    public void flush() throws IOException {
        throw new UnsupportedOperationException("Flush not supported on TestTimeSeries");
    }
    
    
}
