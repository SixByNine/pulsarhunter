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
 * DetectionComparator.java
 *
 * Created on 09 June 2005, 19:13
 */

package pulsarhunter.jreaper;

import java.util.Comparator;

/**
 *
 * @author mkeith
 */
public class DetectionComparator implements Comparator<Detection>{
    Detection latest;
    /** Creates a new instance of DetectionComparator */
    public DetectionComparator(Detection latest) {
        this.latest = latest;
    }
    
    public int compare(Detection o1, Detection o2) {
        if(o2.getHarmType().getRank() - o1.getHarmType().getRank() == 0){
            if(o1.equals(latest)) return -100;
            if(o2.equals(latest)) return 100;
        }
        return o2.getHarmType().getRank() - o1.getHarmType().getRank();
        
    }
    
    
    
}
