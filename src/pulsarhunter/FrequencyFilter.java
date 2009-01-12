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
 * FrequencyFilter.java
 *
 * Created on 26 February 2007, 15:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

/**
 *
 * @author mkeith
 */
public class FrequencyFilter {
    
    private double start = -1;
    private double end = -1;
    private String name;
    private int matches = 0;
    /** Creates a new instance of FrequencyFilter */
    public FrequencyFilter(double start, double end) {
        this(start,end,0);
    }
    
    public FrequencyFilter(double start, double end, int matches) {
        this.start = start;
        this.end = end;
        this.matches = matches;
        
    }
    
    public boolean periodMatch(double period){
        
        return this.frequencyMatch(1.0/period);
    }
    
    
    public boolean frequencyMatch(double freq){
       // System.out.println(freq+" "+(freq > start && freq < end));
        return freq > start && freq < end;
    }
    
    public String getName(){
        if(name!=null)return name;
        else return ((start+end)/2.0)+"Hz";
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getStart() {
        return start;
    }
    
    public void setStart(double start) {
        this.start = start;
    }
    
    public double getEnd() {
        return end;
    }
    
    public void setEnd(double end) {
        this.end = end;
    }
    
    public int getMatches() {
        return matches;
    }
    
    public void setMatches(int matches) {
        this.matches = matches;
    }
    
    public double getRange() {
        return getEnd() - getStart();
    }
    
    public void setRange(double range) {
        double centre = this.getCentre();
        setStart(centre - range/2.0);
        setEnd(centre + range/2.0);
    }
    
    public double getCentre() {
        return getStart() + (getEnd() - getStart())/2.0;
    }
    
    public void setCentre(double centre) {

        double range = this.getRange();
        setStart(centre - range/2.0);
        setEnd(centre + range/2.0);
    }
    
    
}
