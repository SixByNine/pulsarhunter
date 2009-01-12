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
 * HoughTransform.java
 *
 * Created on 11 November 2005, 11:40
 *
 */

package pulsarhunter.jreaper.peckscorer;

import java.util.Arrays;

/**
 *
 * @author mkeith
 */
public class HoughTransform {
    
    /** Creates a new instance of HoughTransform */
    public HoughTransform() {
    }
    
    public double[][] linearTransform(double[][] input, double[] a1s, int cRes, double threshold){
        double[][][] plot3d = quadraticTransform(input,a1s,new double[]{0}, cRes,threshold);
        
        double[][] result = new double[plot3d.length][plot3d[0][0].length];
        
        for(int i = 0; i < result.length ; i++){
            for(int j = 0; j < result[0].length; j++){
                result[i][j] = plot3d[i][0][j];
            }
        }
        
        return result;
        
    }
    
    public double[][][] quadraticTransform(double[][] input, double[] a1s, double[]a2s, int cRes, double threshold){
        // Calculate 'c' (intercept) range.
        // xMax = input.length;
        // yMax = input[0].length
        // yMin = 0;
        // yMin = 0;
        
        double cMin = - a1s[a1s.length-1]*input.length -  a2s[a2s.length-1]*input.length*input.length;
        double cMax = input[0].length - a1s[0]*input.length -  a2s[0]*input.length*input.length;
        double cStep = (cMax - cMin) / cRes;

        double [][][] accumulator = new double[a1s.length][a2s.length][cRes];
        
        // fill with zeros;
        for(double[][] arr2d : accumulator){
            for(double[] arr : arr2d){
                Arrays.fill(arr,0);
            }
        }
        
        
        // Loop through the 'image' array
        for(int x = 0; x < input.length; x++){
            for(int y = 0; y < input[x].length; y++){
                double value = input[x][y];
                if(value > threshold){
                    
                    
                    for(int i = 0; i < a1s.length; i++){
                        for(int j = 0; j < a2s.length; j++){
                            // c = y - a1*x - a2*x^2
                            double c = y - a1s[i]*x - a2s[j]*x*x;
                            int cBin = -1;
                            while(c > cMin){
                                c -= cStep;
                                cBin++;
                            }
                            /*
                             *Give each gradient and intercept a signal strength depending on the xy signal strength.
                             */
                            accumulator[i][j][cBin] += value;
                        }
                    } // end of a1 loop
                    
                } // end of if
                
            }
        }// end of x loop
        
        
        return accumulator;
    }
    
    
    
    
    
}
