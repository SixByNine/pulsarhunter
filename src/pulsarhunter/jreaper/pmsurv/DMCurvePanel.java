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
 * profilePanel.java
 *
 * Created on 27 May 2005, 15:37
 */

package pulsarhunter.jreaper.pmsurv;

import ptolemy.plot.Plot;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.Main;


/**
 *
 * @author  mkeith
 */
public class DMCurvePanel extends Plot {
    
    /** Creates new form profilePanel */
    public DMCurvePanel(final Cand candidate) {
        initComponents();
        _setPadding(0.0);
        clear(true);
        final PulsarCandFile phfile = (PulsarCandFile)candidate.getCandidateFile();
        
        setTitle("DM Curve");
        setXLabel("DM");
        setYLabel("SNR");
        setMarksStyle("dots");
        setMarksStyle("none",1);
        //TODO fix this data library usage
        float[] dmcurvindex;
        if(candidate.getCandList().getDataLibrary() instanceof PMDataLibrary){
            dmcurvindex = ((PMDataLibrary)candidate.getCandList().getDataLibrary()).getDmindex();
        } else {
            dmcurvindex = PulsarCandFile.getDmindex();
        }
        float[] prof = phfile.getDMCurveRaw();
        for(int i = 0;i<prof.length && i < dmcurvindex.length;i++){
            addPoint(0,dmcurvindex[i],prof[i],true);
        }
        
        float[] dmcalc = this.makeDMCurve(prof, dmcurvindex, candidate.getDM(), candidate.getPeriod(), phfile.getWidthBins());
        for(int i = 0;i<dmcalc.length && i < dmcurvindex.length;i++){
            addPoint(1,dmcurvindex[i],dmcalc[i],true);
        }
        
    }
    
    private double bandwidth = 288.0;
    private double freq = 1374.0;
    private double kdm = 8.3e6;
    private double dmVal(int width,double period, double dmoff){
        double weff = Math.sqrt(Math.pow((((double)width/64.0)*period),2) + Math.pow( (kdm * Math.abs(dmoff) * (bandwidth/Math.pow(freq,3)) ) ,2) );
        if(weff > period) return 0;
        else return  Math.sqrt((period - weff) / weff);
    }
    
    public float[] makeDMCurve(float[] dmcurv, float[] dmidex,double centerDM,double period,int width){
        float[] result = new float[dmidex.length];
        float max = 0.0f;
        for(int i = 0;i<result.length;i++){
            
            result[i] = (float)dmVal(width,period,centerDM-dmidex[i]);
            if(result[i] > max)max = result[i];
            
        }
        for(int i = 0;i<result.length;i++){
            result[i] = result[i] / max;
        }
        
        return result;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
