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
package pulsarhunter.datatypes;

import java.util.Comparator;
import pulsarhunter.datatypes.BasicSearchResult;
import pulsarhunter.datatypes.PeriodSearchResultGroup.SortField;

public class SearchResultComparator <R extends BasicSearchResult> implements Comparator<R>{
    private SortField sortField;
    
    public SearchResultComparator(SortField sortField){
        this.sortField = sortField;
    }
    
    public int compare(R o1, R o2) {
        int rv;
        switch(sortField){
            case PERIOD:
                return (int)(1000000*(o1.getPeriod() - o2.getPeriod()));
            case DM:
                return (int)(1000000*(o1.getDM() - o2.getDM()));
            case SPECTRAL_SNR:
                rv = (int)(1000000*(o1.getSpectralSignalToNoise() - o2.getSpectralSignalToNoise()));
                if(rv == 0){
                    return (int)(1000000*(o1.getReconstructedSignalToNoise() - o2.getReconstructedSignalToNoise()));
                } else return rv;
            case RECONSTRUCTED_SNR:
                rv = (int)(1000000*(o1.getReconstructedSignalToNoise() - o2.getReconstructedSignalToNoise()));
                if(rv == 0){
                    return (int)(1000000*(o1.getSpectralSignalToNoise() - o2.getSpectralSignalToNoise()));
                } else return rv;

            case FOLD_SNR:
                return (int)(1000000*(o1.getFoldSignalToNoise() - o2.getFoldSignalToNoise()));
            case PDOT:
                return (int)(1000000*(o1.getAccn() - o2.getAccn()));
            case PDDOT:
                return (int)(1000000*(o1.getJerk() - o2.getJerk()));
            case DM_PDOT_PDDOT:
                if( o1.getDM() == o2.getDM() ){
                    if( o1.getAccn() == o2.getAccn() ){
                        if( o1.getJerk() == o2.getJerk() ){
                            return 0;
                        } else return (int)(1000000*(o1.getJerk() - o2.getJerk()));
                    } else return (int)(1000000*(o1.getAccn() - o2.getAccn()));
                } else return (int)(1000000*(o1.getDM() - o2.getDM()));
                
            default:
                return 0;
        }
        
        
    }
    
    
}
