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
package pulsarhunter;

import coordlib.Telescope;
import java.io.File;

/**
 * Yes, I know it's spelt wrongly!
 *
 */
public class BarryCenter{
    
    private double dopplerFactor;
    private double barryMJD;
    private double tobs;
    private double xma;
    private boolean telescopeFound = true;
    
    public BarryCenter(double mjd, Telescope telescope, double ra, double dec){
        if(!BarryCenter.isAvaliable())throw new RuntimeException("Cannot use barrycenter code if the library is not avaliable");
        if(telescope == Telescope.UNKNOWN || telescope.getId() > 15){
            System.out.println("BaryCentre - Cannot barycentre as telescope is not in database");
            this.tobs = 0.0;
            this.dopplerFactor = 1.0;
            this.xma = 0.0;
            this.barryMJD = mjd;
            this.setTelescopeFound(false);
        }else{
            double[] result = this.getPsrEphJ(Convert.mjdToSec(mjd),telescope.getId(),ra,dec,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0);
            
            this.tobs = result[0];
            this.dopplerFactor = result[1];
            this.xma = result[2];
            this.barryMJD = result[3];
        }
    }
    
    
    
    
    private double[] getPsrEphJ(double epoch, int itelno, double ra, double dec, double mjd, double period, double pdot, double epbin, double pbin, double asini, double wbin, double ecc){
        double[] result = new double[4];
        //  System.out.printf("%g %d %g %g %g %g %g %g %g %g %g %g \n",epoch,  itelno,  ra,  dec,  mjd,  period,  pdot,  epbin,  pbin,  asini,  wbin,  ecc);
        BarryCenter.psrephj(epoch,  itelno,  ra,  dec,  mjd,  period,  pdot,  epbin,  pbin,  asini,  wbin,  ecc,result);
        
        //  System.out.printf("%g %g %g %g\n",result[0],result[1],result[2],result[3]);
        
        
        return result;
        
    }
    
    
    
    private static boolean avaliable = false;
    
    
    public static boolean isAvaliable(){
        return avaliable;
    }
    
    /*
     *  Try and load the libary on class load... Otherwise we set avaliable to false.
     *
     */
    static {
        
        try{
            
            
            PulsarHunter.loadLibrary("barycentre"); 
            avaliable = true;
        } catch(java.lang.UnsatisfiedLinkError err){
            System.err.println(err.getMessage());
            avaliable = false;
        }
    }
    
    private static native void psrephj(double epoch, int itelno, double ra, double dec, double mjd, double period, double pdot, double epbin, double pbin, double asini, double wbin, double ecc, double[] result);
    
    public double getDopplerFactor() {
        return dopplerFactor;
    }
    
    public double getBarryMJD() {
        return Convert.secToMJD(barryMJD);
    }

    public boolean isTelescopeFound() {
        return telescopeFound;
    }

    public void setTelescopeFound(boolean telescopeFound) {
        this.telescopeFound = telescopeFound;
    }
    
}
