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
 * PolyCo.java
 *
 * Created on 15 March 2007, 15:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

/**
 *
 * @author mkeith
 */
public class PolyCoBlock {
    
    private String psrName;
    
    private double mjdmid;
    private double[] coeff;
    private double z4;
    private double rphase;
    private double f0;
    private double obsfrq;
    private int blockLength;
    
    private double targetMjd = -1;
    private double targetPhase = -1;
    private double targetPeriod = -1;
    
    /** Creates a new instance of PolyCo */
    public PolyCoBlock(String psrName, double mjdmid, double[] coeff, double z4, double rphase, double f0, double obsfrq, int blockLength) {
        this.setPsrName(psrName);
        this.setMjdmid(mjdmid);
        this.setCoeff(coeff);
        this.setZ4(z4);
        this.setRphase(rphase);
        this.setF0(f0);
        this.setObsfrq(obsfrq);
        this.setBlockLength(blockLength);
    }
    
    
    
    public synchronized double getPhaseAt(double targetMjd){
        if(targetMjd != this.targetMjd){
            generateTarget(targetMjd);
        }
        return targetPhase;
    }
    
    public synchronized double getPeriodAt(double targetMjd){
        if(targetMjd != this.targetMjd){
            generateTarget(targetMjd);
        }
        return targetPeriod;
    }
    
    private synchronized void generateTarget(double targetMjd){
        this.targetMjd = targetMjd;
        
        int ncoeff = coeff.length;
        
        
        double psrfreq = 0.;                 /* Compute psrfreq and phase from */
        double phase = coeff[ncoeff-1];      /* the polynomial coeffs. */
        
        double dtmin = (targetMjd - mjdmid)*1440.0;
        
        for(int i=ncoeff-1;i>0;--i) {
            psrfreq = dtmin*psrfreq + i*coeff[i];
            phase = dtmin*phase + coeff[i-1];
        }
        
        psrfreq = f0 + psrfreq/60.;  /* Add in the DC terms and scale */
        phase += rphase+dtmin*60.*f0;
        
        phase -= Math.floor(phase);
        if ((phase < 0.) || (phase > 1.)) {
            System.err.printf("phase = %21.15f\n",phase);
            System.exit(4);
        }
        this.targetPhase = phase;
        this.targetPeriod = 1.0/psrfreq;
    }

    public String getPsrName() {
        return psrName;
    }

    public void setPsrName(String psrName) {
        this.psrName = psrName;
    }

    public double getMjdmid() {
        return mjdmid;
    }

    public void setMjdmid(double mjdmid) {
        this.mjdmid = mjdmid;
    }

    public double[] getCoeff() {
        return coeff;
    }

    public void setCoeff(double[] coeff) {
        this.coeff = coeff;
    }

    public double getZ4() {
        return z4;
    }

    public void setZ4(double z4) {
        this.z4 = z4;
    }

    public double getRphase() {
        return rphase;
    }

    public void setRphase(double rphase) {
        this.rphase = rphase;
    }

    public double getF0() {
        return f0;
    }

    public void setF0(double f0) {
        this.f0 = f0;
    }

    public double getObsfrq() {
        return obsfrq;
    }

    public void setObsfrq(double obsfrq) {
        this.obsfrq = obsfrq;
    }

    public int getBlockLength() {
        return blockLength;
    }

    public void setBlockLength(int blockLength) {
        this.blockLength = blockLength;
    }
    
}
