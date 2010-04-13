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
 * TimeseriesResampler.java
 *
 * Created on 05 February 2007, 13:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import pulsarhunter.Convert;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.sigproc.Sigproc32bitTimeSeries;
import pulsarhunter.datatypes.sigproc.SigprocTimeSeries;
import pulsarhunter.processes.folding.PdotFoldingModel;

/**
 *
 * @author mkeith
 */
public class TimeseriesResampler implements PulsarHunterProcess{
    
    
    private TimeSeries input, output;
    
    /** Creates a new instance of TimeseriesResampler */
    public TimeseriesResampler(TimeSeries input, TimeSeries output) {
        this.input = input;
        this.output = output;
        
    }
    
    
    public void run() {
        
        
        
        Sigproc32bitTimeSeries outts = (Sigproc32bitTimeSeries)output;
        
        outts.getHeader().setSourceID(input.getHeader().getSourceID());
        outts.getHeader().setNPoints(input.getHeader().getNPoints());
        outts.getHeader().setTSamp(input.getHeader().getTSamp());
        outts.getHeader().setCoord(input.getHeader().getCoord());
        outts.getHeader().setFrequency(input.getHeader().getFrequency());
        outts.getHeader().setDm(input.getHeader().getDm());
        outts.getHeader().setMjdStart(input.getHeader().getMjdStart());
        outts.getHeader().setTelescope(input.getHeader().getTelescope());
        
        double tint = input.getHeader().getTobs();
        double tsamp = input.getHeader().getTSamp();
        double ac = 0;
        double ad = 1;
        

        
//        double ph_epoch = input.getHeader().getMjdStart() + Convert.secToMJD(tint)/2.0;
        
//        PdotFoldingModel foldModel = new PdotFoldingModel(tsamp,Convert.accToPdot(tsamp,180*2),0.0,ph_epoch,tsamp);
        
        
        double tau0=tsamp/(1.0+ac*tint/2.0/Convert.SPEED_OF_LIGHT + ad*tint*tint/6.0/Convert.SPEED_OF_LIGHT);
//        
//        double ph_time = input.getHeader().getMjdStart();
//        
//        double ph_tau0 = foldModel.getPeriod(ph_time);
//        System.out.println(tau0+"\t"+ph_tau0);
        double tav = 0;
        double nav = 0;
        
        double taut = tau0;
        double next = taut;
        boolean inc_b = true;
        
        float sampi = 0;
        float sampj = 0;
        int j = 0;
        long b = -1;
        while(b < input.getHeader().getNPoints()-1){
//        while(b < 100){
            if(inc_b){
                b++;
                sampi = input.getBin(b);
//                ph_time += Convert.secToMJD(tsamp);
                inc_b = false;
            }
//            System.out.println("b:"+b+" j:"+j+" next:"+next+" t:"+(b+1)*tsamp);
            if(next > (b+1)*tsamp){
                sampj += sampi*(((float)b+1.0)*tsamp - (next - taut))/tsamp;
                inc_b = true;
            }
            if(next <= (b+1)*tsamp){
                
                sampj += sampi*(next - (b)*tsamp)/tsamp;
//                System.out.println(sampj+" "+sampi);
                outts.writeBin(j,sampj);
                sampj = 0.0f;
                j++;
                
                double time = next;
                taut=tau0*(1.0 + ac*time/Convert.SPEED_OF_LIGHT + 0.5*ad*time*time/Convert.SPEED_OF_LIGHT);
//                ph_tau0 = foldModel.getPeriod(ph_time);
//                taut = ph_tau0;
//                System.out.println(tau0+"\t"+ph_tau0);
                tav+=taut;
                nav++;
                next+=taut;
            }
            
            
//            outts.writeBin(b,input.getBin(b));
        }
        
    }
    
}
