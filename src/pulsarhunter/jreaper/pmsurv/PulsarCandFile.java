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
 * PulsarCandFile.java
 *
 * Created on 24 May 2005, 16:11
 */

package pulsarhunter.jreaper.pmsurv;

import com.bbn.openmap.dataAccess.shape.input.LittleEndianInputStream;
import coordlib.Beam;
import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFrame;
import pulsarhunter.Data;
import coordlib.Telescope;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.CandidateFile;
import pulsarhunter.jreaper.Main;
import pulsarhunter.jreaper.gui.MainView;
import pulsarhunter.jreaper.peckscorer.PeckScoreableCandFile;

/**
 *
 * @author mkeith
 */
public abstract class PulsarCandFile implements PeckScoreableCandFile,Data {
    
    private static float[] dmindex = null;
    
    protected File file;
    
    protected boolean read;
    
    protected transient int nfdot;
    
    protected transient float dma;
    
    protected transient float dmb;
    
    protected transient float dmc;
    
    protected transient float dmmax;
    
    protected transient float dmmaxe;
    protected transient float[] frph;
    
    protected transient int kwmax;
    
    protected transient int nband;
    
    protected transient int nbin;
    
    protected transient int nbine;
    
    protected transient int nchan;
    
    
    protected transient int ndm;
    
    protected transient int ndms;
    
    
    protected transient int nn;
    
    protected transient int nprd;
    
    protected transient int npsub;
    
    
    protected transient int nsub;
    
    protected transient float pa;
    
    protected transient float pb;
    
    protected transient double pc;
    protected transient float[] pdma;
    
    protected transient String plhd1;
    
    protected transient String plhd2;
    
    protected transient String plhd3;
    
    
    protected transient String plhd4;
    
    protected transient String plhd4a;
    
    protected transient String plhd5;
    
    protected transient String plhd5a;
    
    protected transient String plhd6;
    
    protected transient String plhd7;
    
    protected transient String plhd8;
    
    protected transient String plhd9;
    
    protected transient double ppmax;
    
    protected transient float ppmaxe;
    protected transient float[] prmax;
    
    
    
    
    protected transient float rms;
    
    protected transient float rmss;
    protected transient float[] snlist;
    
    protected transient float snrmax;
    
    protected transient double tsmp;
    protected transient float[] wrk;
    protected transient float ymax;
    protected transient float ymin;
    
    
    
    public PulsarCandFile(File file){
        this.file = file;
    }
    
    public abstract void read();
    
/*
    public abstract int getNsub();
    public abstract int getNbin();
 
    public abstract int[][] getFreqGraph(int scaleTo,double phases);
    public abstract String getText();
    public abstract float[] getDMCurveRaw();
    public abstract float[] getRawFreqGraph();
    public abstract float[] getRawPDMProf();
    public abstract float[] getRawSubints();
    public abstract float[] getProfile(double phases);
    public abstract int[][] getPDMprof(int scaleTo);
    public abstract String[] getHeaders();
    public abstract String[] getPDMParams();
 
 
    public abstract int getNPrd();
    public abstract int getNBand();
    public abstract int getNDM();
    public abstract String getFile();
    public abstract int getWidthBins();
    public abstract double getBarryPeriod();
    public abstract double getPeriod();
 
    public abstract double getPeriodError();
    public abstract String getGridID();
 */
    
    public void changeFile(File file){
        this.file = file;
    }
    
    public LittleEndianInputStream getInputStream()throws FileNotFoundException{
        return new LittleEndianInputStream(new BufferedInputStream(new FileInputStream(file)));
    }
    
    public boolean hasDMCurve(){
        return true;
    }
    /**
     * Obtains the dm curve data, a 2 by ndm array. The first array has the dm at each trial dm, the seccond has the snr (or fractional snr) at each dm.
     * @return The DM curve data
     */
    public float[][] getDMCurve() {
        try{
            float[][]  dmcurv = new float[2][];
            dmcurv[0] = this.getDMCurveRaw();
            dmcurv[1] = this.getDmindex();
            
//            // TODO! FIX THIS STUFF
//            if(cand.getCandList().getDataLibrary() instanceof PMDataLibrary){
//                dmcurv[1] = ((PMDataLibrary)cand.getCandList().getDataLibrary()).getDmindex();
//            } else {
//                dmcurv[1] = PulsarCandFile.getDmindex();
//            }
            return dmcurv;
        }  catch(Exception e){
            e.printStackTrace();
        }
        return new float[0][0];
    }
    
    public String getDataType() {
        return null;
    }
    
    public float getWidth() {
        return (float) (((this.getWidthBins())/64.0));
    }
    
    public boolean hasSubints(){
        return true;
    }
    
    public float[][] getSubints(){
        /*float[] vals = ((PulsarCandFile)this.getCandidateFile()).getRawSubints();
        int nbins = ((PulsarCandFile)this.getCandidateFile()).getNbin();
        int nsub = ((PulsarCandFile)this.getCandidateFile()).getNsub();
        float[][] subintsfold = new  float[nsub][nbins];
        int posn = 0;
        for(int i = 0; i < nsub ; i++){
            for(int j =0;j<nbins;j++){
                while(j>=nbins)j -= nbins;
                posn = nbins*i+j;
                subintsfold[i][j] = vals[posn];
            }
        }*/
        
        
        int[][] subintsInt = this.getSubints(32, 1, 32);
        float[][] subintsfold = new  float[subintsInt[0].length][subintsInt.length];
        for(int i = 0; i < subintsInt.length ; i++){
            for(int j =0;j<subintsInt[0].length;j++){
                subintsfold[j][i] = subintsInt[i][j];
            }
        }
        
        
        /*for(float[] arr :subintsfold ){
            float sum = 0;
            for(float f : arr){
                sum += f;
                System.out.print(f+" ");
            }
            System.out.println("  ......"+sum);
        }
        System.out.println();
        System.out.println();
        System.out.println();
         */
        return subintsfold;
    }
    
    public boolean hasFrequencyChannels(){
        return true;
    }
    /**
     * Obtains the frequency chanel data, made of a float array such that floats[band][phase]. Can have more than one period across data.
     * @return the frequenct/phase data.
     */
    public float[][] getFrequencyChanels() {
        float[] rawFC =  this.getRawFreqGraph();
        int nbin = this.getNbin();
        int nband = this.getNBand();
        int bin = 0;
        int posn = 0;
        float[][] freqs = new float[nbin][nband];
        
        for(int i = 0;i<nband;i++){
            
            for(int j = 0;j<nbin;j++){
                
                bin = j;
                while(bin>=nbin)bin -= nbin;
                
                posn = nbin*i+bin;
                freqs[j][i] = ((rawFC[posn]));
            }
        }
        
        return freqs;
    }
    
    public boolean hasProfile(){
        return true;
    }
    
    public float[] getProfile() {
        return this.getProfile(1);
        
    }
    
    public boolean hasAditionalScore(){
        return false;
    }
    /**
     *If this candidate type has an aditional scoring method, it can be given here. The returned score should be a number between 0.0 and 1.0;
     */
    public float getAdditionalScore(){
        return 0;
    }
    
    
    
    public int[][] getSubints(int scaleTo, double phases, int reqSubInts){
        read();
        
        float max = 0.0f;
        float min = 0.0f;
        
        int posn;
        int nbins = (int)(nbin*phases);
        int ns = 1;
        if(nsub > reqSubInts) ns = nsub / reqSubInts;
        //int[][] subints = new int[nbins][nsub];
        float[][] vals = new float[nbins][nsub/ns];
        int bin;
        for (int i = 0; i<nsub; i+=ns){
            for (int j = 0; j<nbins; j++){
                bin = j;
                while(bin>=nbin)bin -= nbin;
                posn = nbin*i+bin;
                //subints[j][i] = (int)(((wrk[posn]-min)/max)*scaleTo);
                int b = i/ns;
                if(b >= vals[j].length)b = vals[j].length-1;
                
                vals[j][b] = 0;
                
                for (int k = 0; k<ns; k++){
                    if(posn+(k*nbin) >= wrk.length) vals[j][b] += wrk[posn];
                    else vals[j][b] += wrk[posn+(k*nbin)];
                }
                if(vals[j][b]>max) max = vals[j][b];
                if(vals[j][b]<min) min = vals[j][b];
                
            }
        }
        
        max = max-min;
        
        
        int[][] subintsfold = new  int[nbins][nsub/ns];
        for (int j = 0; j<nbins; j++){
            for (int i = 0; i<vals[j].length; i++){
                subintsfold[j][i] = (int) (((vals[j][i]-min)/max)*scaleTo);
            }
        }
        return subintsfold;
    }
    
    
    
    public int[][] getFreqGraph(int scaleTo,double phases){
        read();
        float max = 0.0f;
        float min = 0.0f;
        for(int i = 0;i<frph.length;i++){
            if(frph[i]>max) max = frph[i];
            if(frph[i]<min) min = frph[i];
        }
        for(int i = 0;i<frph.length;i++){
            frph[i] = frph[i]-min;
        }
        max = max - min;
        int posn;
        int nbins = (int)(nbin*phases);
        int[][] freqs = new int[nbins][nband];
        int bin;
        for(int i = 0;i<nband;i++){
            
            for(int j = 0;j<nbins;j++){
                
                bin = j;
                while(bin>=nbin)bin -= nbin;
                
                posn = nbin*i+bin;
                freqs[j][i] = (int)((frph[posn]/max)*scaleTo);
            }
        }
        return freqs;
    }
    
    public float[] getProfile(double phases){
        read();
        int totBins = (int)(prmax.length*phases);
        float[] prof = new float[totBins];
        for(int i = 0;i < phases;i++){
            int len = prmax.length;
            if(len*(i+1) > totBins) len = totBins - (len*(i));
            System.arraycopy(prmax, 0, prof, i*prmax.length, len);
        }
        
        return prof;
    }
    
    
    public float[] getDMCurveRaw(){
        read();
        return snlist;
    }
    
    
    
    public int[][] getPDMprof(int scaleTo){
        read();
        float max = 0.0f;
        float min = 0.0f;
        for(int i = 0;i<pdma.length;i++){
            if(pdma[i]>max) max = pdma[i];
            if(pdma[i]<min) min = pdma[i];
        }
        for(int i = 0;i<pdma.length;i++){
            pdma[i] = pdma[i]-min;
        }
        max = max - min;
        int posn;
        
        int[][] res = new int[nprd][ndm];
        int prd;
        for(int i = 0;i<ndm;i++){
            for(int j = 0;j<nprd;j++){
                
                prd = j;
                while(prd>=nprd)prd -= nprd;
                
                posn = nprd*i+prd;
                
                res[j][i] = (int)((pdma[posn]/max)*scaleTo);
            }
        }
        return res;
    }
    
    
    public int getNsub(){
        read();
        return nsub;
    }
    
    public int getNbin(){
        read();
        return nbin;
    }
    
    public String getName(){
        
        return this.file.getName();
    }
    
    public double getSpecSnr(){
        read();
        String[] snrStr = plhd3.split(":");
        if(snrStr.length >1){
            return Double.parseDouble(snrStr[2].trim().split("\\s+")[0]);
        } else {
            return 0.0;
        }
    }
    
    public double getReconSnr(){
        return 0.0;
    }
    
    public double getPeriod(){
        read();
        String[] periodStr = plhd4.split(":");
        if(periodStr.length >1){
            return Double.parseDouble(periodStr[1].trim());
        } else {
            return 0.0;
        }
        
    }
    public double getBarryPeriod(){
        read();
        String[] periodStr = plhd4a.split(":");
        if(periodStr.length >1){
            return Double.parseDouble(periodStr[1].split("Err")[0]);
        } else {
            return 0.0;
        }
        
    }
    
    public float getSNR(){
        read();
        String[] snrstring = plhd7.split(":");
        
        try{
            return Float.parseFloat(snrstring[2].trim());
        }catch(NumberFormatException e){
            System.err.println("Problem reading SNR from "+file.getName() +"\nGenerating fake SNR.");
            return 0.0f;
        }
    }
    
    public float getDM(){
        read();
        String[] dmstring = plhd6.split(":");
        
        return Float.parseFloat(dmstring[1].split("Err")[0].trim());
    }
    
    public Beam getBeam(){
        read();
        String[] beamString = plhd1.split(":");
        String bName = beamString[1].split("RAJ")[0];
        String raStr = (beamString[2]+":"+beamString[3]+":"+beamString[4]).split("DecJ")[0];
        String decStr = (beamString[5]+":"+beamString[6]+":"+beamString[7]).split("Gl")[0];
        RA ra = (new RA(0).generateNew("J "+raStr.trim()));
        Dec dec = (new Dec(0).generateNew("J "+decStr.trim()));
        return new Beam(bName,new Coordinate(ra,dec));
    }
    public double getMJD(){
        read();
        String[] dmstring = plhd9.split(":");
        return Double.parseDouble(dmstring[1].split("BC")[0]);
    }
    
    
    public String getText(){
        read();
        return plhd1 + "\n" + plhd2 + "\n" + plhd3 + "\n" + plhd4 + "\n" + plhd4a + "\n" + plhd5 + "\n" +
                plhd5a + "\n" + plhd6 + "\n" + plhd7 + "\n" + plhd8 + "\n" + plhd9;
        
    }
    
    public JFrame getCandDisplayFrame(Cand c,MainView main){
        
        return new PlotFrame(c,main);
        
    }
    
    
    public String[] getHeaders(){
        read();
        return new String[] {
            plhd1,plhd2,plhd3,
        };
    }
    
    public String[] getPDMParams(){
        read();
        return new String[]{
            plhd4,plhd4a,plhd5,plhd5a,plhd6,plhd7,plhd8,plhd9
        };
    }
    
    public double getAccel(){
        return 0;
        
    }
    
    public double getJerk(){
        return 0;
    }
    
    
    
    public int getWidthBins(){
        read();
        String[] dmstring = plhd7.split(":");
        return Integer.parseInt(dmstring[1].split("Best")[0].trim());
    }
    
    public float[] getRawSubints() {
        return wrk;
    }
    
    public float[] getRawPDMProf() {
        return pdma;
    }
    
    public float[] getRawFreqGraph() {
        return frph;
    }
    
    public int getNPrd() {
        read();
        return nprd;
    }
    
    public int getNDM() {
        read();
        return ndm;
    }
    
    
    public int getNBand() {
        read();
        return nband;
    }
    
    
    public File getFile() {
        return file;
    }
    
    public CandidateFile deepClone() {
        return new PulsarFile_sph(file);
    }
    
    public double getPeriodError(){
        read();
        String[] periodStr = plhd4a.split(":");
        if(periodStr.length >2){
            return Double.parseDouble(periodStr[2].trim());
        } else {
            return 0.0;
        }
    }
    public String getGridID(){
        try{
            return plhd8.split("PSR")[1].trim();
        } catch(Exception e){
            return "xxx";
        }
    }
    
    public boolean hasHoughPlane() {
        return false;
    }
    
    public double[][] getHoughPlane() {
        return null;
    }
    
    public int getNPulses(){
        
        float[] profile = this.getProfile();
        float max = 0;
        float sum = 0;
        int mPos = 1;
        int nBin = profile.length;
        for(int i = 0; i < nBin; i++){
            float f = profile[i];
            if(f > max){
                max = f;
                mPos = i;
            }
            sum += f;
        }
        float average = sum / nBin;
        float threashold = (max - average) / 2.5f + average;
            /*boolean onpulse = false;
            int numP = 0;
            for(int i = 0; i < profile.length; i++){
                if(onpulse){
                    if(profile[i] < threashold){
                        onpulse = false;
                        //System.out.println("off:"+((double)i/(double)profile.length));
                    }
                } else {
                    if(profile[i] > threashold){
                        numP ++;
                        onpulse = true;
                        //System.out.println("on:"+((double)i/(double)profile.length));
                    }
                }
            }*/
        int numP;
        for(numP = 32; numP > 1 ; numP--){
            boolean good = true;
            float spacing = (float)nBin / (float)numP;
            for(int i = 1; i <= numP; i++){
                int bin = (int)(spacing * i) + mPos;
                while(bin >= nBin) bin -= nBin;
                if(profile[bin] > threashold){
                } else {
                    good = false;
                    break;
                }
            }
            if(good){  // IE we see all the pulses...
                break;
            }
        }
        
        
        return numP;
        
    }
    
    
    public String getUniqueIdentifier() {
        return this.getFile().getName();
    }
    
    public File createImage(Cand c, String path) {
        return null;
    }
    
    public boolean hasPeriodCurve() {
        return false;
    }
    
    public boolean hasAccnCurve() {
        return false;
    }
    
    public double[][] getPeriodCurve() {
        return null;
    }
    
    public double[][] getAccnCurve() {
        return null;
    }
    
    public double getTobs() {
        return -1;
    }
    
    public static void setDmindexFile(File dmlistFile){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(dmlistFile));
            String line;
            int count = 0;
            while((line = reader.readLine()) != null){
                try{
                    Float.parseFloat(line.trim());
                    count ++;
                }catch (NumberFormatException e){
                    
                }
            }
            reader.close();
            reader = new BufferedReader(new FileReader(dmlistFile));
            dmindex = new float[count];
            int pos = 0;
            while((line = reader.readLine()) != null){
                try{
                    dmindex[pos] = Float.parseFloat(line.trim());
                    pos++;
                    if(pos >= count) break;
                }catch (NumberFormatException e){
                    System.err.println("Skiping dmindex "+line+" as it is not a number");
                }
            }
            reader.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    
    
    public static float[]  getDmindex() {
        if(dmindex == null){
            
            System.err.println("\n\nFATAL ERROR!");
            System.err.println("If you use minifind style .ph/.aph/.sph files");
            System.err.println("you must have a 'datalibraryname'.dmlist file");
            System.err.println("which contains the dm values used in the search");
            System.err.println("\nSorry for any inconveniance...\n");
            System.exit(22);
            
        }
        return dmindex;
    }
    
    
    public Data.Header header;
    
    public void flush() throws IOException {
    }
    
    public void release() {
        this.dmindex=null;
        this.frph=null;
        this.pdma=null;
        this.read = false;
        this.prmax=null;
        this.wrk=null;
        this.snlist = null;
    }
    
    public Header getHeader() {
        if(header ==null){
            this.read();
            header = new Data.Header();
            header.setSourceID(this.getGridID());
            header.setBandwidth(288.0);
            header.setFrequency(1374.0);
            header.setMjdStart(this.getMJD());
            header.setTobs(2100.0);
            header.setTelescope(Telescope.PARKES);
            header.setBarryCentered(false);
            header.setCoord(this.getBeam().getCoord());
        }
        return header;
    }
}
