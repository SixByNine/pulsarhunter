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
 * DefaultDataLibraryType.java
 *
 * Created on 01 August 2006, 10:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.jreaper;

import pulsarhunter.jreaper.gui.NewDatabaseDataLibraryForm;

/**
 *
 * @author mkeith
 */
public class DatabaseDataLibraryType implements DataLibraryType{
    JReaper jreaper;
    /** Creates a new instance of DefaultDataLibraryType */
    public DatabaseDataLibraryType(JReaper jreaper) {
        this.jreaper  = jreaper;
    }

    public String toString() {
      return "Database DataLibrary [new : with website]";
    }

    public void showCreationDialog() {
        new NewDatabaseDataLibraryForm(jreaper).setVisible(true);
    }
    
}
