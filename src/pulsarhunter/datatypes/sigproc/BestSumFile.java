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
 * BestSumFile.java
 *
 * Created on 03 November 2006, 19:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes.sigproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import pulsarhunter.Data;

/**
 *
 * @author mkeith
 */
public class BestSumFile implements Data{
    private File file;
    
    private int bestMode;
    private double period;
    private double acc = 0.0;
    private double adot = 0.0;
    private double snr;
    private double dm = 0.0;
    private String sourceFile;
    private int fold = 0;
    
    private double[] dmcurve;
    private double[] dmidx;
    
    /** Creates a new instance of BestSumFile */
    public BestSumFile(File file) {
        this.file = file;
    }
    
    public String getDataType() {
       return "BESTSUMFILE";
    }
    
    public void read()throws IOException{
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        sourceFile = reader.readLine().trim();
        
        String line;
        
        // line = reader.readLine();
        //  this.bestMode = Integer.parseInt(line.trim());
        this.bestMode = 1;
        
        switch(getBestMode()){
            case 1:
                line = reader.readLine();
                Scanner scan = new Scanner(line);
                scan.useDelimiter("\\s+");
                this.period = scan.nextDouble();
                this.snr = scan.nextDouble();
                this.dm = scan.nextDouble();
                this.fold = scan.nextInt();
                
                
                ArrayList<String> lines = new ArrayList<String>();
                line = reader.readLine();
                while(line!=null){
                    lines.add(line);
                    line = reader.readLine();
                }
                
                dmidx = new double[lines.size()];
                dmcurve = new double[lines.size()];
                for(int i = 0; i < lines.size(); i++){
                    line = lines.get(i);
                    scan = new Scanner(line);
                    scan.useDelimiter("\\s+");
                    scan.nextInt();
                    dmidx[i] = scan.nextDouble();
                    dmcurve[i] = scan.nextDouble();
                    
                }
                return;
                
            default:
                throw new IOException("Cannot read .sum files that are mode "+getBestMode());
                
        }
        
        
    }
    
    
    public void release() {
        this.sourceFile = null;
        this.file = null;
        this.dmcurve=this.dmidx = null;
    }
    
    public Header getHeader() {
        return null;
    }
    
    public void flush() throws IOException {
    }
    
    public int getBestMode() {
        return bestMode;
    }
    
    public double getPeriod() {
        return period;
    }
    
    public double getAcc() {
        return acc;
    }
    
    public double getAdot() {
        return adot;
    }
    
    public double getSnr() {
        return snr;
    }
    
    public double getDm() {
        return dm;
    }
    
    public String getSourceFile() {
        return sourceFile;
    }
    
    public int getFold() {
        return fold;
    }
    
    public double[] getDmcurve() {
        return dmcurve;
    }
    
    public double[] getDmidx() {
        return dmidx;
    }
    
    
    
}
