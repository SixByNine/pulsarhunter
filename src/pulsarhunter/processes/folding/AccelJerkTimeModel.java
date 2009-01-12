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
 * AccelJerkSampleTimeModel.java
 *
 * Created on 06 February 2007, 14:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import pulsarhunter.Convert;
import pulsarhunter.VariableTimeModel;

/**
 *
 * 
 *
 *
 *
 * @author mkeith
 */
public class AccelJerkTimeModel implements VariableTimeModel{
    
    
    double acc,jerk;
    
    /** Creates a new instance of AccelJerkSampleTimeModel */
    public AccelJerkTimeModel(double acc, double jerk) {
        this.acc = acc;
        this.jerk = jerk;
    //    System.out.println("acc: "+acc+"jerk: "+jerk);
    }

    public double getAddjustedTime(double naturalTime) {
        return naturalTime
                 - acc*naturalTime*naturalTime/(2.0*Convert.SPEED_OF_LIGHT)
                 - jerk*naturalTime*naturalTime*naturalTime/(6.0*Convert.SPEED_OF_LIGHT);
    }

    public double getRefreshTime() {
        if(acc==0&&jerk==0) return 0;
        else return 0.0001;
                
    }
    
    
    
}
