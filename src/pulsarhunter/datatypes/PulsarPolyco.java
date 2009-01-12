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
 * PulsarPolyco.java
 *
 * Created on 15 March 2007, 15:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.util.ArrayList;

/**
 *
 * @author mkeith
 */
public class PulsarPolyco {
    
    
    private String psrName;
    private double dm;
    private PolyCoBlock targetBlock = null;
    public  ArrayList<PolyCoBlock> blocks = new ArrayList<PolyCoBlock>();
    
    /** Creates a new instance of PulsarPolyco */
    public PulsarPolyco(String name, double dm) {
        this.setPsrName(name);
        this.setDm(dm);
    }
    
    
    public  double getPhaseAt(double targetMjd){
        PolyCoBlock pcb =  getTarget(targetMjd);
        if(pcb!=null)return pcb.getPhaseAt(targetMjd);
        else return -1;
    }
    
    public  double getPeriodAt(double targetMjd){
        PolyCoBlock pcb =  getTarget(targetMjd);
        if(pcb!=null)return pcb.getPeriodAt(targetMjd);
        else return -1;
        
    }
    
    public  PolyCoBlock getTarget(double targetMjd){
        double dtmin = (targetMjd - targetBlock.getMjdmid())*1440.0;
        if (Math.abs(dtmin) <= targetBlock.getBlockLength()/2.) {
            return targetBlock;
        } else {
            for(PolyCoBlock block : blocks){
                dtmin = (targetMjd - block.getMjdmid())*1440.0;
                if (Math.abs(dtmin) <= block.getBlockLength()/2.) {
                    targetBlock = block;
                    return block;
                }
            }
        }
        return null;
    }
    
    public String getPsrName() {
        return psrName;
    }
    
    public void setPsrName(String psrName) {
        this.psrName = psrName;
    }
    
    public double getDm() {
        return dm;
    }
    
    public void setDm(double dm) {
        this.dm = dm;
    }
    
    public void addBlock(PolyCoBlock pcb){
        this.blocks.add(pcb);
        this.targetBlock = pcb;
    }
    
}
