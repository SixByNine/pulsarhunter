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
 * phfile_CandidateReader.java
 *
 * Created on 26 May 2005, 10:29
 */

package pulsarhunter.jreaper.pmsurv;

import coordlib.Beam;
import java.io.File;

import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.CandList;
import pulsarhunter.jreaper.CandScorer;
import pulsarhunter.jreaper.CandidateReader;
import pulsarhunter.jreaper.Main;
import pulsarhunter.jreaper.Score;


/**
 *
 * @author mkeith
 */
public class aphfile_CandidateReader implements CandidateReader {
    
    
    
    private CandList testCandList = new CandList(null,new Cand[0][0],null);
    /** Creates a new instance of phfile_CandidateReader */
    public aphfile_CandidateReader() {
        
        testCandList.setFch1(1374.0f);
        testCandList.setBand(288.0);
        testCandList.setTobs(2100);
    }
    
    public Cand getFromFile(File file,CandScorer scorer) {
        try{
            PulsarFile_aph phfile = new PulsarFile_aph(file);
            phfile.read();
            double period = phfile.getPeriod();
            float SNR = phfile.getSNR();
            float DM = phfile.getDM();
            double accel = phfile.getAccel();
            Beam beam = phfile.getBeam();
            double mjd = phfile.getMJD();
            
            double specsnr = phfile.getSpecSnr();
            double reconsnr = phfile.getReconSnr();
            Score score = null;

            testCandList.setBeam(phfile.getBeam());
            Cand pCand = new Cand(phfile,period,SNR,DM,0,0,null,mjd);
            pCand.setCandList(this.testCandList);
            int np = pCand.getNPulses();
            
            if(scorer != null) score = scorer.score(pCand); // Create a temp cand to score...
            phfile = null;
            pCand = null;
            
            Cand result = new Cand(new PulsarFile_aph(file),period,SNR,DM,accel,0,score,mjd);
            result.setSpecSNR((float)specsnr);
            result.setReconSNR((float)reconsnr);
            result.setCandList(this.testCandList);
            result.setNPulses(np);
            return result;
            
        } catch (Exception e){
            //e.printStackTrace();
            return null;
        }
    }
    
    public int getSearchType(){
        return 1;
    }
}
