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
 * ValueList.java
 *
 * Created on 14 February 2007, 15:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import pulsarhunter.Data;

/**
 *
 * @author mkeith
 */
public class ValueList implements Data<Data.Header>{
    
    private double[] data;
    
    
    /** Creates a new instance of ValueList */
    public ValueList(File file) throws IOException{
        ArrayList<Double> vals = new ArrayList<Double>();
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line = in.readLine();
        
        while(line != null){
            try{
                double d = Double.parseDouble(line);
                vals.add(d);
            }catch (NumberFormatException e){
                
            }
            line = in.readLine();
        }
        
        data = new double[vals.size()];
        for(int i= 0 ; i < data.length; i++){
            data[i] = vals.get(i);
        }
        
    }
    
    public String getDataType() {
       return "VALUELIST";
    }
    public void release() {
        this.data = null;
    }
    
    public Data.Header getHeader() {
        return null;
    }
    
    public void flush() throws IOException {
        return;
    }

    public double[] getData() {
        return data;
    }
    
}
