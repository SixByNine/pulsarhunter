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
 * colormapgen.java
 *
 * Created on 26 May 2005, 20:14
 */

package pulsarhunter.jreaper;

import coordlib.Dec;
import coordlib.RA;

/**
 *
 * @author mkeith
 */
public class colormapgen {
    public static void main(String[] args){
        
        /*int r,g,b;
        String rhex,ghex,bhex;
        for(r = 255;r>=0;r-=2){
            g = 255-r;
            b = 0;
            System.out.println("new Color(0x"+toHex(r)+toHex(g)+toHex(b)+"),");
        }
        for(g = 255;g>=0;g-=2){
            b = 255-g;
            r = 0;
            System.out.println("new Color(0x"+toHex(r)+toHex(g)+toHex(b)+"),");
        }*/
        /*for(b = 255;b>=0;b-=1){
            
            r = b;
            g = b;
            System.out.println("new Color(0x"+toHex(r)+toHex(g)+toHex(b)+"),");
        }*/
        
        
        double gcra = 192.859;
        double gcdec = 27.12825;
        
        //double gcra = new RA(12,51,0).toDegrees();
        //double gcdec = new Dec(27,07,0).toDegrees();
        
        double ra = new RA(19,7,20).toDegrees();
        double dec = new Dec(4,13,40,false).toDegrees();
        
        //double ra = new RA(11,9,00).toDegrees();
        //double dec = new Dec(-7,00,00).toDegrees();
         //dec = 90-dec;
        
        double cosalphas = Math.cos(Math.toRadians(ra-gcra));
        double sinalphas = Math.sin(Math.toRadians(ra-gcra));
        double sinGC = Math.sin(Math.toRadians(gcdec));
        double cosGC = Math.cos(Math.toRadians(gcdec));
        double lcp = Math.toRadians(122.932);
        double sindec = Math.sin(Math.toRadians(dec));
        double cosdec = Math.cos(Math.toRadians(dec));
        
        double b = Math.toDegrees(Math.asin(sinGC*sindec + cosGC*cosdec*cosalphas));
        double l = Math.toDegrees(lcp - Math.atan((cosdec*sinalphas)/(cosGC*sindec-sinGC*cosdec*cosalphas)));
        
        System.out.println("b= "+b);
        System.out.println("l= "+l);
        
    }
    
    static String toHex(int r){
        String rhex;
        if(r<16) rhex = "0"+Integer.toHexString(r);
        else rhex = Integer.toHexString(r);
        return rhex;
    }
}
