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
 * Detection.java
 *
 * Created on 09 June 2005, 19:02
 */

package pulsarhunter.jreaper;

import java.io.Serializable;
import pulsarhunter.jreaper.HarmonicType;

/**
 *
 * @author mkeith
 */
public class Detection implements Serializable{
    
    
    private int candClass;
    private String name;
    private HarmonicType harmType;
    private double fundPeriod;
    private String harmonic;
    
    /** Creates a new instance of Detection */
    public Detection(String name,String harmonic, int candClass, HarmonicType harmType,double fundPeriod) {
        this.candClass = candClass;
        this.name = name;
        this.harmType = harmType;
        this.fundPeriod= fundPeriod;
        this.harmonic = harmonic;
    }
    
    
    public int getCandClass(){
        return candClass;
        
    }
    
    
    public String getName(){
        return name;
    }
    
    public HarmonicType getHarmType(){
        return harmType;
    }
    
    public double getFundPeriod(){
        return fundPeriod;
    }
    
    public String getHarmonic(){
        return harmonic;
    }
    
    public boolean equals(Object obj) {
        if(obj instanceof Detection){
            return (((Detection)obj).fundPeriod-fundPeriod)<0.001*fundPeriod && ((Detection)obj).candClass == candClass && ((Detection)obj).harmType == harmType;
        } else return false;
    }
    public String toString(){
        return "("+candClass+")"+this.name+" "+harmonic+" [P0:"+fundPeriod+"]";
        
    }
}
