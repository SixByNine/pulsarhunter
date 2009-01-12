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
 * AppendOsrfToJReaperCandListProcess.java
 *
 * Created on 04 November 2006, 12:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import java.io.IOException;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.datatypes.OSRFAppendableJReaperCandList;
import pulsarhunter.datatypes.PulsarHunterCandidate;

/**
 *
 * @author mkeith
 */
public class AppendOsrfToJReaperCandListProcess implements PulsarHunterProcess{
    
    private PulsarHunterCandidate osrf;
    private OSRFAppendableJReaperCandList candList;
    private int type;
    private String resultsDir = null;
    private String beamID = null;
    /** Creates a new instance of AppendOsrfToJReaperCandListProcess */
    public AppendOsrfToJReaperCandListProcess(PulsarHunterCandidate osrf, OSRFAppendableJReaperCandList candList, int type, String resultsDir, String beamID) {
        this.osrf = osrf;
        this.candList = candList;
        this.type=type;
        this.resultsDir = resultsDir;
        this.beamID = beamID;
    }
    
    
    
    public void run() {
        try {
            this.candList.append(osrf,type,this.resultsDir,this.beamID);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}

