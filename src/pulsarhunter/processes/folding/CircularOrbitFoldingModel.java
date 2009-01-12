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
 * BinaryFoldingModel.java
 *
 * Created on 27 September 2006, 17:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import pulsarhunter.Convert;
import pulsarhunter.FoldingModel;

/**
 *
 * @author mkeith
 */
public class CircularOrbitFoldingModel implements FoldingModel{
    double period;
    double asini;
    double binaryPeriod;
    double omegaB;
    double t0;
    double resolution;
    private static double c = 2.99792458e8;
    /** Creates a new instance of BinaryFoldingModel */
    public CircularOrbitFoldingModel(double period, double orbitalPeriod,double asini,double t0, double resolution) {
        this.period = period;
        this.binaryPeriod = orbitalPeriod;
        omegaB = 2*Math.PI/orbitalPeriod;
        this.asini = asini;
        this.t0 = t0;
        this.resolution = resolution;
    }
    
    
    
    public double getPeriod(double mjd){
        
        double time = Convert.mjdToSec(mjd);
        
        double E = omegaB*(time-t0);
        double AtE = E;
        
        double V = omegaB*asini*Math.cos(AtE);
     
        double pEff = period * (1+V);
     //   System.out.println("New Period: "+pEff+ " Time: "+time + " time - t0: "+(time-t0));
        return pEff;
    }

    public double getRefreshTime() {
        return resolution;
    }
}
