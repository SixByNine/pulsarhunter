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
 * Data.java
 *
 * Created on 26 September 2006, 15:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;
import coordlib.Telescope;
import java.io.IOException;


/**
 *
 * @author mkeith
 */
public interface Data<E extends Data.Header> {
    
    
    /**
     * Get the asociated header element for this data
     * @return The header for this data file.
     */
    public E getHeader();
    /**
     * Flush writes any changes to the data to the underlying data file. Not supported by read-only data.
     * @throws java.io.IOException This is thrown if there is an IO exception writing to the underlying file.
     */
    public void flush() throws IOException;
    /**
     * This tells the data object to release references to stored values, hopefully allowing for garbage collection. Upon execution, the object will become invalid and exceptions may be thrown when calling methods.
     */
    public void release();
    
    public String getDataType();
    
    
    
    public class Header{
        private String sourceID = "UNK";
        private double frequency = 0;
        private double bandwidth = 0;
        private double mjdStart = 0;
        private double obstime = 0;
        private Coordinate coord = new Coordinate(new RA(0),new Dec(0));
        private boolean barryCentered = false;
        private Telescope telescope = Telescope.UNKNOWN;
        
        /**
         * Get the Source ID
         * @return A description of the source, usualy a grid number or a JName.
         */
        public String getSourceID() {
            return sourceID;
        }
        
        public void setSourceID(String sourceID) {
            this.sourceID = sourceID;
        }
        
        public double getFrequency() {
            return frequency;
        }
        
        public void setFrequency(double frequency) {
            this.frequency = frequency;
        }
        
        public double getBandwidth() {
            return bandwidth;
        }
        
        public void setBandwidth(double bandwidth) {
            this.bandwidth = bandwidth;
        }
        
        public double getMjdStart() {
            return mjdStart;
        }
        
        public void setMjdStart(double mjdStart) {
            this.mjdStart = mjdStart;
        }
        
        public double getTobs() {
            return obstime;
        }
        
        public void setTobs(double obstime) {
            this.obstime = obstime;
        }
        
        public Coordinate getCoord() {
            return coord;
        }
        
        public void setCoord(Coordinate coord) {
            this.coord = coord;
        }
        
        public boolean isBarryCentered() {
            return barryCentered;
        }
        
        public void setBarryCentered(boolean barryCentered) {
            this.barryCentered = barryCentered;
        }

        public Telescope getTelescope() {
            return telescope;
        }

        public void setTelescope(Telescope telescope) {
            this.telescope = telescope;
        }
        
        
        
    }
}
