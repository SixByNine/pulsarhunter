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
 * FilterCandidatesFactory.java
 *
 * Created on 15 January 2007, 16:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import pulsarhunter.Data;
import pulsarhunter.FrequencyFilter;
import pulsarhunter.GlobalOptions.Option;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.datatypes.BasicSearchResultData;
import pulsarhunter.datatypes.PeriodSearchResultGroup;
import pulsarhunter.datatypes.ZapFile;

/**
 *
 * @author mkeith
 */
public class FilterCandidatesFactory implements ProcessFactory {
    
    /** Creates a new instance of FilterCandidatesFactory */
    public FilterCandidatesFactory() {
    }
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles, PulsarHunterRegistry reg) throws ProcessCreationException {
        
        if (params.length < 3) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        
        Data dat = dataFiles.get(params[1]);
        if(! (dat instanceof BasicSearchResultData))  throw new ProcessCreationException("Argument 1 must be a BasicSearchResultData for process "+this.getName());
        String rootname = params[2];
        
        
        double minsnr = 7.0;
        int maxResults = 999;
        boolean useAccn = false;
        boolean dumpHarmonics = false;
        double matchfactor = 0.0025;
        double minProfileBins = 4.0;
        boolean nophcx=false;
        PeriodSearchResultGroup.SortField snrField =  PeriodSearchResultGroup.SortField.SPECTRAL_SNR;
        
        FrequencyFilter[] filters = new FrequencyFilter[0];
        
        if (reg.getOptions().getArg(Option.nophcx)!=null){
            nophcx = (Boolean)reg.getOptions().getArg(Option.nophcx);
        }
        if (reg.getOptions().getArg(Option.minsnr)!=null){
            minsnr = (Double)reg.getOptions().getArg(Option.minsnr);
        }
        if (reg.getOptions().getArg(Option.matchfactor)!=null){
            matchfactor = (Double)reg.getOptions().getArg(Option.matchfactor);
        }
        if (reg.getOptions().getArg(Option.minprofilebins)!=null){
            minProfileBins = (Double)reg.getOptions().getArg(Option.minprofilebins);
        }
        if (reg.getOptions().getArg(Option.maxresults)!=null){
            maxResults = (Integer)reg.getOptions().getArg(Option.maxresults);
        }
        if (reg.getOptions().getArg(Option.useaccn)!=null){
            useAccn = (Boolean)reg.getOptions().getArg(Option.useaccn);
        }
        if (reg.getOptions().getArg(Option.userecon)!=null){
            snrField = PeriodSearchResultGroup.SortField.RECONSTRUCTED_SNR;
        }
        if (reg.getOptions().getArg(Option.dumpharmonics)!=null){
            dumpHarmonics = (Boolean)reg.getOptions().getArg(Option.dumpharmonics);
        }
        if (reg.getOptions().getArg(Option.zapfile)!=null){
            String zapfilename = (String)reg.getOptions().getArg(Option.zapfile);
            ZapFile zapFile = null;
            try {
                zapFile = (ZapFile) dataFiles.get(zapfilename);
            } catch(ClassCastException e) {
                zapfilename = zapfilename+".zap";
            }
            if(zapFile==null){
                try {
                    zapFile = new ZapFile(new File(zapfilename));
                    zapFile.read();
                    dataFiles.put(zapfilename,zapFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    
                }
                
            }
            filters = zapFile.getFilters().toArray(filters);
        }
        
        
        
        
//        for(int i = 3; i < params.length; i++){
//            if(params[i].trim().equalsIgnoreCase("-minsnr")){
//                minsnr = Double.parseDouble(params[++i]);
//            }
//
//            if(params[i].trim().equalsIgnoreCase("-maxresults")){
//                maxResults = Integer.parseInt(params[++i].trim());
//            }
//
//            if(params[i].trim().equalsIgnoreCase("-showaccn"))useAccn = true;
//            if(params[i].trim().equalsIgnoreCase("-userecon"))
//            if(params[i].trim().equalsIgnoreCase("-zapfile")){
//                i++;
//                String zapfilename = params[i];
//                ZapFile zapFile = null;
//                try {
//                    zapFile = (ZapFile) dataFiles.get(zapfilename);
//                } catch(ClassCastException e) {
//                    zapfilename = zapfilename+".zap";
//                }
//                if(zapFile==null){
//                    try {
//                        zapFile = new ZapFile(new File(zapfilename));
//                        zapFile.read();
//                        dataFiles.put(zapfilename,zapFile);
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//
//                    }
//
//                }
//                filters = zapFile.getFilters().toArray(filters);
//            }
//        }
        
        
        FilterCandidates proc =  new FilterCandidates((BasicSearchResultData)dat,snrField,matchfactor,rootname,minsnr,maxResults,dumpHarmonics,minProfileBins,nophcx);
        proc.setUseAccn(useAccn);
        proc.setFilters(filters);
        return proc;
        
        
    }
    
    public String getName() {
        return "FILTER";
    }
    
}
