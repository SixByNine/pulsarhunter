/*
 * SubIntegrationMakerFactory.java
 *
 * Created on July 22, 2007, 7:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes.folding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import pulsarhunter.BarryCenter;
import pulsarhunter.Data;
import pulsarhunter.GlobalOptions.Option;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.MultiprofileFile;
import pulsarhunter.datatypes.PolyCoFile;
import pulsarhunter.datatypes.TimeSeries;

/**
 *
 * @author mkeith
 */
public class SubIntegrationMakerFactory implements ProcessFactory{
    
    /** Creates a new instance of SubIntegrationMakerFactory */
    public SubIntegrationMakerFactory() {
    }
    
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles,PulsarHunterRegistry reg) throws ProcessCreationException {
        if(params.length < 3) throw new ProcessCreationException("MakeSubints requires a 2 arguments, a timeseries file and a output file");
        
        Data infile = dataFiles.get(params[1]);
        if(infile == null || !((infile instanceof TimeSeries) ||(infile instanceof MultiChannelTimeSeries)) ){
            throw new ProcessCreationException("MakeSubints argument 1 must be a timeseries file");
        }
        Data outfile = dataFiles.get(params[2]);
        if(outfile == null || !(outfile instanceof MultiprofileFile)){
            throw new ProcessCreationException("MakeSubints argument 2 must be a multi profile (e.g. EPN) file");
        }
        
        int nsub = 64;
        int nbins = 128;
        
        if(reg.getOptions().getArg(Option.nsub)!=null)
            nsub = (Integer)reg.getOptions().getArg(Option.nsub);
        
        if(reg.getOptions().getArg(Option.nbins)!=null)
            nbins = (Integer)reg.getOptions().getArg(Option.nbins);
        
        
        
        
        
        // Are we polyco?
        if(reg.getOptions().getArg(Option.polyco) != null){
            String pcfname = (String)reg.getOptions().getArg(Option.polyco);
            PolyCoFile pcf = null;
            try {
                pcf = (PolyCoFile) dataFiles.get(pcfname);
            } catch(ClassCastException e) {
                pcfname = pcfname+".polyco";
            }
            if(pcf==null){
                try {
                    pcf = new PolyCoFile();
                    pcf.read(new BufferedReader(new FileReader(new File(pcfname))));
                    dataFiles.put(pcfname,pcf);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    
                }
                
            }
            
            if(infile instanceof TimeSeries){
                return new SubIntegrationMaker((TimeSeries)infile,(MultiprofileFile)outfile,pcf,nsub,nbins);
            }
            if(infile instanceof MultiChannelTimeSeries){
                return new SubIntegrationMaker((MultiChannelTimeSeries)infile,(MultiprofileFile)outfile,pcf,nsub,nbins);
            }
        } else {
            // we should get a period etc
            double period = 0.0;
            if(reg.getOptions().getArg(Option.period)==null){
                if(reg.getOptions().getArg(Option.baryperiod)==null){
                    throw new ProcessCreationException("MakeSubints: You must set a period or bary-period!");
                }
                // we have no period, but do have a baryperiod
                double dopp = 1.0;
                if(BarryCenter.isAvaliable()){
                    
                    BarryCenter bc = new BarryCenter(infile.getHeader().getMjdStart(),
                            infile.getHeader().getTelescope(),
                            infile.getHeader().getCoord().getRA().toDegrees(),
                            infile.getHeader().getCoord().getDec().toDegrees());
                    
                    dopp = bc.getDopplerFactor();
                    
                }
                period  = (Double)reg.getOptions().getArg(Option.baryperiod)*dopp / 1000.0;
                
            } else{
                // we have a period
                period = (Double)reg.getOptions().getArg(Option.period)/1000.0;
            }
            
            double accn = 0.0;
            double jerk = 0.0;
            if(reg.getOptions().getArg(Option.accn)!=null)
                accn = (Double)reg.getOptions().getArg(Option.accn);
            if(reg.getOptions().getArg(Option.jerk)!=null)
                jerk = (Double)reg.getOptions().getArg(Option.jerk);
            if(infile instanceof TimeSeries){
                return new SubIntegrationMaker((TimeSeries)infile,(MultiprofileFile)outfile,period,accn,jerk,nsub,nbins);
            }
            if(infile instanceof MultiChannelTimeSeries){
                return new SubIntegrationMaker((MultiChannelTimeSeries)infile,(MultiprofileFile)outfile,period,accn,jerk,nsub,nbins);
            }
        }
        
        return null;
    }
    
    public String getName() {
        return "MAKESUBINTS";
    }
    
}
