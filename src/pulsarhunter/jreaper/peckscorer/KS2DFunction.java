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
 * KS2DFunction.java
 *
 * Created on 20 October 2005, 15:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package pulsarhunter.jreaper.peckscorer;

/**
 *
 * @author mkeith
 */
public class KS2DFunction{
        double sizeX;
        double sizeY;
        double totalArea;
        public KS2DFunction(double sizeX, double sizeY){
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            totalArea = sizeX * sizeY;
        }
        
        public QuadCount call(double x, double y){
            QuadCount q = new QuadCount();
            q.a = (sizeX - x)*(sizeY - y)/totalArea;
            q.b = (x)*(sizeY - y)/totalArea;
            q.c = (x)*(y)/totalArea;
            q.d = (sizeX - x)*(y)/totalArea;
            return q;
        }
        
    }
