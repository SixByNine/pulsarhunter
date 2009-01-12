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
 *  WebDataLibrary.java
 *
 * Created on 28 May 2005, 13:54
 */

package pulsarhunter.jreaper;



/**
 *  A Datalibrary implementing this interface stores a list of selected canadidates
 *  externaly to the jreaper datalibrary. The methods on this interface allow
 * the external data to be synced to the jreaper datalibrary
 * @author mkeith
 */
public interface WebDataLibrary {
    
    /**
     * The passed Cand has been changed and should be updated in the external
     * database
     * @param cand The candidate that needs to be updated in the external database
     */
    public void webUpdate(Cand cand);
    
    /**
     * Synchronise the given candidates with the external database.
     * @param data The data that needs to be updated in the local representation
     */
    public void webSync(CandList data);
}
