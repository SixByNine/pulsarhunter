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
 * PeckScoreableCandidate.java
 *
 * Created on 25 August 2005, 14:08
 */

package pulsarhunter.jreaper.peckscorer;

import pulsarhunter.jreaper.CandidateFile;



/**
 *
 * @author mkeith
 */
public interface PeckScoreableCandFile extends CandidateFile{

    
    boolean hasDMCurve();
    /**
     * Obtains the dm curve data, a 2 by ndm array. The first array has the dm at each trial dm, the seccond has the snr (or fractional snr) at each dm.
     * @return The DM curve data
     */
    float[][] getDMCurve();
    
    float getWidth();
    
    boolean hasSubints();
    float[][] getSubints();
    
    boolean hasFrequencyChannels();
    /**
     * Obtains the frequency chanel data, made of a float array such that floats[band][phase]. Can have more than one period across data.
     * @return the frequenct/phase data.
     */
    float[][] getFrequencyChanels();
    
    boolean hasProfile();
    float[] getProfile();
    
    boolean hasAditionalScore();
    /**
     *If this candidate type has an aditional scoring method, it can be given here. The returned score should be a number between 0.0 and 1.0;
     */
    float getAdditionalScore();
    
    
    boolean hasHoughPlane();
    double[][] getHoughPlane();
    
    
    boolean hasPeriodCurve();
    double[][] getPeriodCurve();
    boolean hasAccnCurve();
    double[][] getAccnCurve();
    double getTobs();
}
