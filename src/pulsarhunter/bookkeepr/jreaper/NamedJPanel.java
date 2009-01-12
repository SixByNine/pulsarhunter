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
 * NamedJPanel.java
 *
 * Created on 09 June 2005, 15:55
 */

package pulsarhunter.bookkeepr.jreaper;

import javax.swing.JPanel;

/**
 *
 * @author mkeith
 */
public class NamedJPanel extends JPanel{
    private String name;
    /** Creates a new instance of NamedJPanel */
    public NamedJPanel(String name) {
        this.name = name;
    }
    
    public String toString(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
}
