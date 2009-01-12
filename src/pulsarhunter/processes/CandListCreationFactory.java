/*
 * CandListCreationFactory.java
 *
 * Created on 11 April 2007, 14:30
 *
 *
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
package pulsarhunter.processes;

import java.io.File;
import java.util.Hashtable;
import pulsarhunter.GlobalOptions.Option;
import pulsarhunter.Data;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.jreaper.pmsurv.PulsarCandFile;

/**
 *
 * @author mkeith
 */
public class CandListCreationFactory implements ProcessFactory {

    /** Creates a new instance of CandListCreationFactory */
    public CandListCreationFactory() {
    }

    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles, PulsarHunterRegistry reg) throws ProcessCreationException {
        PulsarHunterProcess result = null;
        int type = 3; // default == unknown
        String resultsDir = null;
        String beamID = null;
        boolean is_reswd = true;
        
        if (params.length < 2) {
            throw new ProcessCreationException("Too few arguments to create process " + this.getName());
        }

        File[] infile = new File[params.length-1];
        for (int i = 0; i < params.length-1; i++) {
            infile[i] = new File(params[i+1]);
            if (!infile[i].isDirectory()) {
                throw new ProcessCreationException("Argument "+i+" for DIR2CANDLIST is not a valid directory\n("+infile[i]+")");
            }
        }



        if (reg.getOptions().getArg(Option.resdir) != null) {
            resultsDir = (String) reg.getOptions().getArg(Option.resdir);
            is_reswd = false;
        }
         if (reg.getOptions().getArg(Option.reswd) != null) {
            resultsDir = (String) reg.getOptions().getArg(Option.reswd);
            is_reswd = true;
        }
        if (reg.getOptions().getArg(Option.searchtype) != null) {
            type = (Integer) reg.getOptions().getArg(Option.searchtype);
        }
        if (reg.getOptions().getArg(Option.beamid) != null) {
            beamID = (String) reg.getOptions().getArg(Option.beamid);
        }
        if (reg.getOptions().getArg(Option.dmlist) != null) {
            String dmlistFile = (String) reg.getOptions().getArg(Option.dmlist);
            PulsarCandFile.setDmindexFile(new File(dmlistFile));
        }

//        for(int i = 2; i < params.length; i++){
//            if(params[i].trim().equalsIgnoreCase("-resdir")){
//                resultsDir = params[++i];
//            }
//            
//            if(params[i].trim().equalsIgnoreCase("-searchtype")){
//                type = Integer.parseInt(params[++i].trim());
//            }
//            
//            if(params[i].trim().equalsIgnoreCase("-beamid")){
//                beamID = params[++i].trim();
//            }
//            
//            if(params[i].trim().equalsIgnoreCase("-dmlist")){
//                String dmlistFile = (String)reg.getOptions().getArg(Option.dmlist);
//                PulsarCandFile.setDmindexFile(new File(dmlistFile));
//            }
//        }
        if (resultsDir == null) {
            resultsDir = new File("").getAbsolutePath()+File.separator;
        } else {
            resultsDir = resultsDir+File.separator;
        }
        result = new CandListCreation(infile, resultsDir, beamID,is_reswd);
        return result;
    }

    public String getName() {
        return "DIR2CANDLIST";
    }
}
