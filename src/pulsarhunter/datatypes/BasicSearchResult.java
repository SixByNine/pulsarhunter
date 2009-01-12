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
 * BasicSearchResult.java
 *
 * Created on 15 January 2007, 11:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

/**
 *
 * @author mkeith
 */
public class BasicSearchResult {
    
    private double period;
    private double accn=0;
    private double jerk=0;
    private double DM;
    private double spectralSignalToNoise=0;
    private double reconstructedSignalToNoise=0;
    private double foldSignalToNoise=0;
    private double tsamp = -1;
    private int harmfold = 0;
    
    /** Creates a new instance of BasicSearchResult */
    public BasicSearchResult(double period, double DM) {
        this.period = period;
        this.DM = DM;
    }

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    public double getAccn() {
        return accn;
    }

    public void setAccn(double accn) {
        this.accn = accn;
    }

    public double getJerk() {
        return jerk;
    }

    public void setJerk(double jerk) {
        this.jerk = jerk;
    }

    public double getDM() {
        return DM;
    }

    public void setDM(double DM) {
        this.DM = DM;
    }

    public double getSpectralSignalToNoise() {
        return spectralSignalToNoise;
    }

    public void setSpectralSignalToNoise(double spectralSignalToNoise) {
        this.spectralSignalToNoise = spectralSignalToNoise;
    }

    public double getReconstructedSignalToNoise() {
        return reconstructedSignalToNoise;
    }

    public void setReconstructedSignalToNoise(double reconstructedSignalToNoise) {
        this.reconstructedSignalToNoise = reconstructedSignalToNoise;
    }

    public double getFoldSignalToNoise() {
        return foldSignalToNoise;
    }

    public void setFoldSignalToNoise(double foldSignalToNoise) {
        this.foldSignalToNoise = foldSignalToNoise;
    }

    public int getHarmfold() {
        return harmfold;
    }

    public void setHarmfold(int harmfold) {
        this.harmfold = harmfold;
    }

    public double getTsamp() {
        return tsamp;
    }

    public void setTsamp(double tsamp) {
        this.tsamp = tsamp;
    }

}
