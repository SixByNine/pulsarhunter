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
 * Polyco.java
 *
 * Created on 15 March 2007, 14:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Taken from phcalc.c
 * Therefore it's a little messy from the C -> Java translation!
 * @author mkeith
 */
public class oldPolyco {
    
    
  /*
   *C struct:
   *
   char name[15];
  char date[15];
  float utc;
  double mjdmid[30];
  double mjd1mid[30];
  double dm;
  double z4[30];
  double resid;
  double rphase[30];
  double f0[30];
  double coeff[60][30];
  int nobs;
  int nblk;
  int ncoeff;
  int iset;
  float obsfrq;
  char binphase[60][30];
  double p;
  double mjd0midout;
  double mjd1midout;
  double coeffout[30];
  double rphaseout;
  double f0out;
  double z4out;
  int ncoeffout;
  int jcurr;
   */
    
    
    private String name = "1753-2243";
    private String date;
    private double utc;
    private double dm;
    
    private double resid;
    private int nobs;
    private int nblk;
    private int ncoeff;
    private int iset;
    private double obsfrq;
    
    private double p;
    private double mjd0midout;
    private double mjd1midout;
    
    private double rphaseout;
    private double f0out;
    private double z4out;
    private int ncoeffout;
    private int jcurr;
    private double[] coeffout = new double[60];
    private ArrayList<PolycoBlock> blocks = new ArrayList<PolycoBlock>();
    
    private class PolycoBlock{
        private double coeffout;
        private String binphase;
        private double rphase;
        private double f0;
        private double[] coeff = new double[60];
        private double z4;
        private double mjdmid;
        private double mjd1mid;
    }
    
    public static void main(String[] args){
        oldPolyco pc = new oldPolyco();
        double mjd = 54029.123;
        try {
            pc.read(new BufferedReader(new FileReader(new File("/home/mkeith/polyco.dat"))));
            
            double[] params = pc.phcalc(mjd,0.0);
            System.out.println("Phase: "+params[0]);
            
            System.out.println("Period : "+(1.0/params[1]));
            System.out.println();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        PolyCoFile pcf = new PolyCoFile();
        try {
            
            pcf.read(new BufferedReader(new FileReader(new File("/home/mkeith/polyco.dat"))));
            
            PulsarPolyco ppc = pcf.getPolyCo("1753-2243");
            
            System.out.println("Phase: "+ppc.getPhaseAt(mjd));
            System.out.println("Period : "+ppc.getPeriodAt(mjd));
            
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
    }
    /** Creates a new instance of Polyco */
    public oldPolyco() {
    }
    
    public void read(BufferedReader in) throws IOException{
        String line = in.readLine().trim();
        int j = 0;
        while(line!=null){
            line = line.trim();
            PolycoBlock block = new PolycoBlock();
            
            String[] elems = line.split("\\s+");
            String name = elems[0];
            String date = elems[1];
            // What is elems[2]??? time?
            block.mjdmid = (int)Double.parseDouble(elems[3]);
            block.mjd1mid = Double.parseDouble(elems[3]) - block.mjdmid;
            double dm0 = Double.parseDouble(elems[4]);
            block.z4 = Double.parseDouble(elems[5]);
            // elems[6] = rms apparently, but it's not used...
            line = in.readLine().trim();
            elems = line.split("\\s+",7);
            
            block.rphase = Double.parseDouble(elems[0]);
            block.f0 = Double.parseDouble(elems[1]);
            int jobs = Integer.parseInt(elems[2]);
            
            this.nblk = Integer.parseInt(elems[3]);
            int ncoeff = Integer.parseInt(elems[4]);
            this.obsfrq = Double.parseDouble(elems[5]);
            
            block.binphase = elems[6];
            
            line = in.readLine().trim();
            elems = line.split("\\s+");
            int arrPos = 0;
            Arrays.fill(block.coeff,0.0);
            for(int k=0;k<ncoeff;k++) {
                
                if(arrPos >= elems.length){
                    line = in.readLine().trim();
                    elems = line.split("\\s+");
                    arrPos = 0;
                }
                
                String dummy  = elems[arrPos];
                dummy = dummy.replace('D','e');
                
                
                block.coeff[k] = Double.parseDouble(dummy);
                arrPos++;
            }
            
            if(block.mjdmid  < 20000) block.mjdmid += 39126.;
            if(name.equals(this.name)){
                // right pulsar this time
                this.dm = dm0;
                this.date = date;
                this.nobs = jobs;
                this.ncoeff = ncoeff;
                boolean binary = block.binphase.trim().equals("");
                
                
                this.p = 1000.0/block.f0;
                block.rphase -= Math.floor(block.rphase);
                if(block.rphase < 0.0 || block.rphase > 1.0){
                    System.err.println("ERROR in polyco: rphase = "+block.rphase);
                }
                
            }
            blocks.add(block);
            
            line = in.readLine();
            j++;
        }
        this.iset = j;
    }
    
    public double[] phcalc(double mjd0,double mjd1){
        double dtmin=0.;
        
        int show=0,icurr=0;
        double phase=-1;
        
        
        icurr = -1;
        double psrfreq = this.blocks.get(0).f0;
        for (int j=0;j<this.iset;j++) {
            dtmin = (mjd0-this.blocks.get(j).mjdmid);
            dtmin = (dtmin +(mjd1-this.blocks.get(j).mjd1mid))*1440.; /* Time from center of this set*/
            if (Math.abs(dtmin) <= this.nblk/2.) {
                psrfreq = 0.;                    /* Compute psrfreq and phase from */
                phase = blocks.get(j).coeff[this.ncoeff-1];      /* the polynomial coeffs. */
                //if (show) printf("phase = %21.15e   :%21.15e\n",*phase,pol_dat.coeff[j][pol_dat.ncoeff-1]);
                for(int i=this.ncoeff-1;i>0;--i) {
                    psrfreq = dtmin*(psrfreq) + i*this.blocks.get(j).coeff[i];
                    phase = dtmin*(phase) + this.blocks.get(j).coeff[i-1];
                    //if (show) printf("phase = %21.15e   :%21.15e\n",*phase,pol_dat.coeff[j][i-1]);
                }
                
                psrfreq = this.blocks.get(j).f0 + psrfreq/60.;  /* Add in the DC terms and scale */
                phase += this.blocks.get(j).rphase+dtmin*60.*this.blocks.get(j).f0;
                //   if (show) printf("phase = %21.15e   f0: %21.15e\n",*phase,pol_dat.f0[j]);
                phase -= Math.floor(phase);
                if ((phase < 0.) || (phase > 1.)) {
                    System.err.printf("phase = %21.15f\n",phase);
                    System.exit(4);
                }
                icurr=j;
                this.jcurr = j;
                this.f0out = this.blocks.get(j).f0;
                this.z4out = this.blocks.get(j).z4;
                this.rphaseout = this.blocks.get(j).rphase;
                this.mjd0midout = this.blocks.get(j).mjdmid;
                this.mjd1midout = this.blocks.get(j).mjd1mid;
                this.ncoeffout = this.ncoeff;
                for(int i=0;i<this.ncoeff;i++) {
                    this.coeffout[i] = this.blocks.get(j).coeff[i];
                }
                break;
            }
        }
        if (icurr == -1) {
            System.err.println("Polyco error:");
            System.err.printf("MJD %9.3f out of range (%9.3f to %9.3f)\n",
                    (mjd0+mjd1),this.blocks.get(0).mjdmid-this.nblk/2880.,this.blocks.get(this.iset-1).mjdmid+this.nblk/2880.);
            phase = -999.;
            System.err.printf("isets = %d\n",this.iset);
            System.exit(4);
        }
        
        
        return new double[]{phase,psrfreq};
    }
    
    
    
    
    
    
    
    
    
    
    
}

