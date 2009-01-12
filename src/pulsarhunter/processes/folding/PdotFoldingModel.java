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
 * PdotFoldingModel.java
 *
 * Created on 27 September 2006, 16:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import pulsarhunter.*;

/**
 *
 * @author mkeith
 */
public class PdotFoldingModel implements FoldingModel {
//    public static boolean botchPdotAndPddot = false;
    
    private double period;
    private double pdot;
    private double pddot;
    private double mjdEpoch;
    private double resolution;
    /** Creates a new instance of PdotFoldingModel */
    
    public PdotFoldingModel(double period, double pdot, double pddot, double mjdEpoch,double resolution) {
//        if(PdotFoldingModel.botchPdotAndPddot){
//            pdot/=2;
//            pddot*=2;
//        }
        
        
        this.period = period;
        this.pdot = pdot;
        this.pddot = pddot;
        this.mjdEpoch = mjdEpoch;
        this.resolution = resolution;
        
        
        
    }
  /*  public double getEpoch(){
        return mjdEpoch;
    }*/
   // private static double  oldperiod = 0;
    public double getPeriod(double mjd) {
      /*  System.out.println("PDOTTEST");
        System.out.println(period);
        System.out.println(pdot);
        System.out.println(mjd);
        System.out.println(this.mjdEpoch);
        System.out.println(Convert.mjdToSec(mjd-this.mjdEpoch));
       
        System.out.println("ENDPDOTTEST");*/
        /*if(Math.abs(Convert.mjdToSec(mjd-this.mjdEpoch)) < 0.01){
            System.out.println("PDOTTEST");
            System.out.println(period);
            System.out.println(period + pdot*Convert.mjdToSec(mjd-this.mjdEpoch));
            System.out.println(pdot);
            System.out.println(mjd);
            System.out.println(this.mjdEpoch);
            System.out.println(Convert.mjdToSec(mjd-this.mjdEpoch));
            
            System.out.println("ENDPDOTTEST");
            
            
        }
        double p2 = period + pdot*Convert.mjdToSec(mjd-this.mjdEpoch);
        System.out.println("dp:"+(p2-period)+"");
        oldperiod = p2;*/
        double t = Convert.mjdToSec(mjd-this.mjdEpoch);
        return period + pdot*t + 0.5*pddot*t*t;
    }
    
    public double getRefreshTime() {
        if(pdot == 0 && pddot == 0){
            return 0;
        } else return resolution;
    }
    
}
