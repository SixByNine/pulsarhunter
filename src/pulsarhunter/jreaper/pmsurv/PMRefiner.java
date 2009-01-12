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
 * PMRefiner.java
 *
 * Created on 28 September 2005, 11:15
 */

package pulsarhunter.jreaper.pmsurv;

import coordlib.Beam;
import coordlib.Coordinate;
import coordlib.CoordinateDistanceComparitor;

import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.HarmonicType;
import pulsarhunter.jreaper.CandRefine;
import pulsarhunter.jreaper.DataLibrary;
import pulsarhunter.jreaper.Detection;
import pulsarhunter.jreaper.Main;
import pulsarhunter.jreaper.WebDataLibrary;



/**
 *
 * @author mkeith
 */
public class PMRefiner extends CandRefine {
    
    /** Creates a new instance of PMRefiner */
    public PMRefiner(DataLibrary dataLibrary) {
        super(dataLibrary);
    }
    
    
    public void findHarmonics(Cand[][] masterData, double period,String name,Coordinate coord,int candClass,String[] history){
        
        double factor;
        //System.out.println(period);
        Beam beam;
        CoordinateDistanceComparitor comp = new CoordinateDistanceComparitor();
        for(int i =0;i<masterData.length;i++){
            for(int j = 0;j<masterData[i].length;j++)
                
                top:{
                    
                    beam = masterData[i][j].getBeam();
                    if(candClass == 4 || candClass == 5 || comp.difference(beam.getCoord(),coord) < getDistmax()){
                        
                        if(Math.abs(masterData[i][j].getPeriod() - period) < getEta()*period){
                            
                            // Check num pulses
                            if(masterData[i][j].getNPulses() == 1){
                                for(String s : history){
                                    //masterData[i][j].addComment(s);
                                }
                                masterData[i][j].addDetection(new Detection(name,"Fundemental",candClass,HarmonicType.Principal,period));
                                
                                if(this.getDataLibrary() instanceof WebDataLibrary){
                                    WebDataLibrary dl = (WebDataLibrary)this.getDataLibrary();
                                    dl.webUpdate(masterData[i][j]);
                                }
                                
                            }else{
                                masterData[i][j].addComment("Warning... This is possibly a detection of "+name);
                            }
                            
                        }else {
                            for(String s : history){
                               // masterData[i][j].addComment(s);
                            }
                            masterData[i][j].addDetection(new Detection(name,"Fundemental",candClass,HarmonicType.Principal,period));
                            
                            if(this.getDataLibrary() instanceof WebDataLibrary){
                                WebDataLibrary dl = (WebDataLibrary)this.getDataLibrary();
                                dl.webUpdate(masterData[i][j]);
                            }
                            
                        }
                        
                        
                        continue;
                    }
                    
                    for(int intFactor = 2;intFactor<16;intFactor++){
                        if(Math.abs(masterData[i][j].getPeriod() -period*intFactor) < getEta()*period*intFactor){
                            if(masterData[i][j].getNPulses() > 0){
                                // Check num pulses
                                if(masterData[i][j].getNPulses() == intFactor){
                                    masterData[i][j].addDetection(new Detection(name,intFactor+ "th Harmonic",candClass,HarmonicType.Integer,period));
                                }else{
                                    masterData[i][j].addComment("Warning... This is possibly a "+intFactor+ "th Harmonic of "+name);
                                }
                                
                            }else {
                                masterData[i][j].addDetection(new Detection(name,intFactor+ "th Harmonic",candClass,HarmonicType.Integer,period));
                            }
                            
                            break top;
                        }
                    }
                    
                    
                    for(int bottomfactor = 1; bottomfactor <= 32;bottomfactor++) {
                        for(int topfactor = 1; topfactor < 32;topfactor++){
                            factor = ((double)topfactor)/((double)bottomfactor);
                            //System.out.println(topfactor +"/"+bottomfactor+" "+factor+" "+ Math.abs(masterData[i][j].getPeriod()*factor - period));
                            if(Math.abs(masterData[i][j].getPeriod() - period*factor) < getEta()*period*factor){
                                if(masterData[i][j].getNPulses() > 0){
                                    // Check num pulses
                                    if(masterData[i][j].getNPulses() == topfactor){
                                        if(topfactor==1)masterData[i][j].addDetection(new Detection(name,topfactor+"/"+bottomfactor+ "th Harmonic",candClass,HarmonicType.SimpleNonInteger,period));
                                        else masterData[i][j].addDetection(new Detection(name,topfactor+"/"+bottomfactor+ "th Harmonic",candClass,HarmonicType.ComplexNonInteger,period));
                                    }else{
                                        masterData[i][j].addComment("Warning... This is possibly a "+topfactor+"/"+bottomfactor+ "th Harmonic of "+name);
                                    }
                                    
                                    
                                }else {
                                    if(topfactor==1)masterData[i][j].addDetection(new Detection(name,topfactor+"/"+bottomfactor+ "th Harmonic",candClass,HarmonicType.SimpleNonInteger,period));
                                    else masterData[i][j].addDetection(new Detection(name,topfactor+"/"+bottomfactor+ "th Harmonic",candClass,HarmonicType.ComplexNonInteger,period));
                                }
                                
                                break top;
                            }
                        }
                    }
                }
                
                
        }
    }
    
    
}
