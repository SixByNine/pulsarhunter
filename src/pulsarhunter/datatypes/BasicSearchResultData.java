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
 * NewClass.java
 *
 * Created on 15 January 2007, 15:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.util.ArrayList;
import java.util.List;
import pulsarhunter.Data;

/**
 *
 * @author mkeith
 */
public abstract class BasicSearchResultData implements Data {
    
    private ArrayList<BasicSearchResult> searchResults = new ArrayList<BasicSearchResult>();
    
    /** Creates a new instance of NewClass */
    public BasicSearchResultData() {
    }
    
    
    public List<BasicSearchResult> getSearchResults() {
        return (List<BasicSearchResult>) (searchResults.clone());
    }
    
    public void addSearchResult(BasicSearchResult res){
        this.searchResults.add(res);
    }
    
    
    public void release() {
        searchResults = null;
    }
    
    public abstract double[] getDmIndex();
    public abstract double[] getAcIndex();
    public abstract double[] getAdIndex();
}
