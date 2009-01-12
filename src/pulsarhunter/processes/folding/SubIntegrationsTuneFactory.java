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
package pulsarhunter.processes.folding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import pulsarhunter.BarryCenter;
import pulsarhunter.Convert;
import pulsarhunter.Data;
import pulsarhunter.GlobalOptions.Option;
import pulsarhunter.GuessDataFactory;
import pulsarhunter.IncorrectDataTypeException;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunter;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.PulsarHunterRegistry;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.PHCSection;
import pulsarhunter.datatypes.PolyCoFile;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.datatypes.PulsarPolyco;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.sigproc.BestSumFile;

public class SubIntegrationsTuneFactory implements ProcessFactory{
    public PulsarHunterProcess createProcess(String[] params, Hashtable<String, Data> dataFiles,PulsarHunterRegistry reg) throws ProcessCreationException {
        if(params[1].equals("-h"))this.printUsage();
        
        if (params.length < 3) throw new ProcessCreationException("Too few arguments to create process "+this.getName());
        
        Data infile = dataFiles.get(params[1]);
        boolean interactive = false;
        Data outfile = dataFiles.get(params[2]);
        
        if (!(outfile instanceof PulsarHunterCandidate))  throw new ProcessCreationException("Seccond argument must be a osrf file for process "+this.getName());
        PolyCoFile pcf = null;
        PulsarHunterCandidate phcfPar = (PulsarHunterCandidate) outfile;
        int i = 0;
        PeriodTuneFoldParams ptfparams = new PeriodTuneFoldParams();
        
        int maxSubInts = 500;
        
        if(phcfPar.getOptimisedSec()!=null){
            // we have an old pulsarhunter candidate...
            
            //  System.out.println(phcfPar.getSection("FFT").getBestTopoPeriod());
            
            ptfparams.setCenterPeriod( phcfPar.getHeader().getOptimisedTopoPeriod());
            
            
            ptfparams.setCenterAccn(phcfPar.getHeader().getOptimisedAccn());
            ptfparams.setCenterJerk( phcfPar.getHeader().getOptimisedJerk());
            ptfparams.setCenterDM(phcfPar.getHeader().getOptimizedDm());
            PulsarHunter.out.println("PeriodTune - Using optimised parameters from existing file...");
            PulsarHunter.out.println("PeriodTune - P:"+ptfparams.getCentrePeriod()
            +"  Accn:"+ptfparams.getCenterAccn()
            +"  Jerk:"+ptfparams.getCenterJerk()
            
            );
            
        }
        
        
        
        
        if(infile instanceof BestSumFile){
            
            PulsarHunter.out.println("WARNING: Sigproc 'Best' .sum file support is now depricated.\n See ph-best script.");
            i = 3;
            BestSumFile bsFile = (BestSumFile)infile;
            
            ptfparams.setCenterPeriod(bsFile.getPeriod()/1000.0);
            //  ((PulsarHunterCandidate)outfile).setDmcurve(bsFile.getDmcurve());
            //  ((PulsarHunterCandidate)outfile).setDmindex(bsFile.getDmidx());
            
            ptfparams.setCenterDM(bsFile.getDm());
            try {
                
                infile = new GuessDataFactory(reg).loadData(bsFile.getSourceFile(),1024*1024);
                dataFiles.put(params[1]+".timfile",infile);
            } catch (IncorrectDataTypeException ex) {
                throw new ProcessCreationException("Cannot load data from "+bsFile.getSourceFile()+" as specified in sum file" ,ex);
            }
            
        }else {
            if(params.length > 3){
                if (params[3].startsWith("-")){
                    
                    if (infile instanceof TimeSeries) {
                        ptfparams.setCenterDM(((TimeSeries)infile).getHeader().getDm());
                    } else if(infile instanceof MultiChannelTimeSeries){
                        ptfparams.setCenterDM(((MultiChannelTimeSeries)infile).getHeader().getDm());
                        
                    }
                    
                    i = 3;
                    
                }  else {
                    
                    Data paramfile = dataFiles.get(params[3]);
                    if (!(paramfile instanceof PeriodTuneFoldParams))  throw new ProcessCreationException("Third argument must be  PeriodTuneParam data, or a -period/-pdot/-coarsness/-range for process "+this.getName());
                    ptfparams = (PeriodTuneFoldParams) paramfile;
                    i = 4;
                }
            } else {
                i = 3;
            }
        }
        
        
        phcfPar.getHeader().setBandwidth(infile.getHeader().getBandwidth());
        phcfPar.getHeader().setBarryCentered(infile.getHeader().isBarryCentered());
        phcfPar.getHeader().setCoord(infile.getHeader().getCoord());
        phcfPar.getHeader().setFrequency(infile.getHeader().getFrequency());
        phcfPar.getHeader().setMjdStart(infile.getHeader().getMjdStart());
        phcfPar.getHeader().setSourceID(infile.getHeader().getSourceID());
        phcfPar.getHeader().setTelescope(infile.getHeader().getTelescope());
        phcfPar.getHeader().setTobs(infile.getHeader().getTobs());
        
        // get global options:
        
        if (reg.getOptions().getArg(Option.period)!=null){
            ptfparams.setCenterPeriod((Double)reg.getOptions().getArg(Option.period)/1000.0);
        }
        if (reg.getOptions().getArg(Option.baryperiod)!=null){
            double dopp = 1.0;
            if(BarryCenter.isAvaliable()){
                
                BarryCenter bc = new BarryCenter(infile.getHeader().getMjdStart(),
                        infile.getHeader().getTelescope(),
                        infile.getHeader().getCoord().getRA().toDegrees(),
                        infile.getHeader().getCoord().getDec().toDegrees());
                
                dopp = bc.getDopplerFactor();
                
            }
            double p  = (Double)reg.getOptions().getArg(Option.baryperiod)*dopp;
            ptfparams.setCenterPeriod(p/1000.0);

        }
        
        
        
        if (reg.getOptions().getArg(Option.periodstep)!=null){
            ptfparams.setPstep((Double)reg.getOptions().getArg(Option.periodstep)/1000.0);
        }
         if (reg.getOptions().getArg(Option.accnstep)!=null){
            ptfparams.setAccnStep((Double)reg.getOptions().getArg(Option.accnstep));
        }
         if (reg.getOptions().getArg(Option.jerkstep)!=null){
            ptfparams.setJerkStep((Double)reg.getOptions().getArg(Option.jerkstep));
        }
         if (reg.getOptions().getArg(Option.dmstep)!=null){
            ptfparams.setDmstep((Double)reg.getOptions().getArg(Option.dmstep));
        }
         if (reg.getOptions().getArg(Option.dm)!=null){
            ptfparams.setCenterDM((Double)reg.getOptions().getArg(Option.dm));
        }
        if (reg.getOptions().getArg(Option.harmonic)!=null){
            ptfparams.setCenterPeriod((Integer)reg.getOptions().getArg(Option.harmonic) * ptfparams.getCentrePeriod());
        }
        if (reg.getOptions().getArg(Option.interactive)!=null){
            interactive = true;
        }
        if (reg.getOptions().getArg(Option.polyco)!=null){
            

            String pcfname = (String)reg.getOptions().getArg(Option.polyco);
            
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
        }
        
        if (reg.getOptions().getArg(Option.name)!=null){
          
            phcfPar.getHeader().setSourceID((String)reg.getOptions().getArg(Option.name));
        }
               /* if (params[i].equalsIgnoreCase("-range")){
                    double range = Double.parseDouble(params[++i]);
                    ptfparams.setPrange(range);
                    ptfparams.setPdrange(range);
                    ptfparams.setPddrange(range);
                    ptfparams.setDmrange(range);
                    continue;
                
                }*/
        
        if (reg.getOptions().getArg(Option.periodrange)!=null){
            ptfparams.setPrange((Double)reg.getOptions().getArg(Option.periodrange)/1000.0);
        }
         if (reg.getOptions().getArg(Option.periodrangepercent)!=null){
            ptfparams.setPrange((Double)reg.getOptions().getArg(Option.periodrangepercent)*ptfparams.getCentrePeriod()/100.0);
        }
        
        if (reg.getOptions().getArg(Option.accnrange)!=null){
            ptfparams.setAccnRange((Double)reg.getOptions().getArg(Option.accnrange));
        }
        
        if (reg.getOptions().getArg(Option.jerkrange)!=null){
            ptfparams.setJerkRange((Double)reg.getOptions().getArg(Option.jerkrange));
        }
        
        if (reg.getOptions().getArg(Option.dmrange)!=null){
            ptfparams.setDmrange((Double)reg.getOptions().getArg(Option.dmrange));
        }
        if (reg.getOptions().getArg(Option.nbins)!=null){
            ptfparams.setNprofilebins((Integer)reg.getOptions().getArg(Option.nbins));
        }
        
        
        if (reg.getOptions().getArg(Option.nsub)!=null){
            ptfparams.setNsub((Integer)reg.getOptions().getArg(Option.nsub));
        }
        
        if (reg.getOptions().getArg(Option.maxsub)!=null){
            maxSubInts = (Integer)reg.getOptions().getArg(Option.maxsub);
        }
        
        
        
        if (reg.getOptions().getArg(Option.useaccn)!=null){
            ptfparams.setUseAccn(true);
        }
        
        if (reg.getOptions().getArg(Option.usejerk)!=null){
            ptfparams.setUseJerk(true);
        }
        
        
        if (reg.getOptions().getArg(Option.accn)!=null){
            ptfparams.setCenterAccn((Double)reg.getOptions().getArg(Option.accn));
        }
        
        if (reg.getOptions().getArg(Option.jerk)!=null){
            ptfparams.setCenterJerk((Double)reg.getOptions().getArg(Option.jerk));
        }
        
        
        if (reg.getOptions().getArg(Option.ibands)!=null){
            String[] elems = ((String)reg.getOptions().getArg(Option.ibands)).split(",");
            int[] ibands = new int[elems.length];
            for(int j = 0; j < elems.length; j++){
                ibands[j] = Integer.parseInt(elems[j].trim());
            }
            ptfparams.setIgnoreBands(ibands);
            
        }
        if (reg.getOptions().getArg(Option.isub)!=null){
            String[] elems = ((String)reg.getOptions().getArg(Option.isub)).split(",");
            int[] ibands = new int[elems.length];
            for(int j = 0; j < elems.length; j++){
                ibands[j] = Integer.parseInt(elems[j].trim());
            }
            ptfparams.setIgnoreSints(ibands);
        }
        
        if (reg.getOptions().getArg(Option.iloudsints)!=null){
            ptfparams.setIgnoreLoudSubbands(true);
        }
        
        
        
        if (reg.getOptions().getArg(Option.fudgeinitialsnr)!=null){
            ptfparams.setRecalcInitialSNR(true);
        }
        
        
        
        // Now check the session only switches...
        
        i--; // as we add one the first time round
        while (i < params.length-1){
            i++;
            if (params[i].equalsIgnoreCase("-h")){
                this.printUsage();
                return null;
            }
            
            
            
            if (params[i].equalsIgnoreCase("-period") || params[i].equalsIgnoreCase("-p")){
                ptfparams.setCenterPeriod(Double.parseDouble(params[++i])/1000.0);
                continue;
            }
            if (params[i].equalsIgnoreCase("-bary-period") || params[i].equalsIgnoreCase("-p")){
                double dopp = 1.0;
                if(BarryCenter.isAvaliable()){
                    
                    BarryCenter bc = new BarryCenter(infile.getHeader().getMjdStart(),
                            infile.getHeader().getTelescope(),
                            infile.getHeader().getCoord().getRA().toDegrees(),
                            infile.getHeader().getCoord().getDec().toDegrees());
                    
                    dopp = bc.getDopplerFactor();
                    
                }
                double p  = Double.parseDouble(params[++i])*dopp;
                ptfparams.setCenterPeriod(p/1000.0);
                continue;
            }
            
            
            
            if (params[i].equalsIgnoreCase("-period-step")){
                ptfparams.setPstep(Double.parseDouble(params[++i])/1000.0);
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-accn-step")){
                ptfparams.setAccnStep(Double.parseDouble(params[++i]));
                continue;
            }
            if (params[i].equalsIgnoreCase("-jerk-step")){
                ptfparams.setJerkStep(Double.parseDouble(params[++i]));
                continue;
            }
            if (params[i].equalsIgnoreCase("-pdot-step")){
                ptfparams.setPdstep(Double.parseDouble(params[++i]));
                continue;
            }
            if (params[i].equalsIgnoreCase("-pddot-step")){
                ptfparams.setPddstep(Double.parseDouble(params[++i]));
                continue;
            }
            if (params[i].equalsIgnoreCase("-dm-step")){
                ptfparams.setDmstep(Double.parseDouble(params[++i]));
                continue;
            }
            
            
            if (params[i].equalsIgnoreCase("-dm")){
                ptfparams.setCenterDM(Double.parseDouble(params[++i]));
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-harmonic")){
                ptfparams.setCenterPeriod(Double.parseDouble(params[++i]) * ptfparams.getCentrePeriod());
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-interactive")){
                interactive = true;
                continue;
            }
            if (params[i].equalsIgnoreCase("-polyco")){
                
                i++;
                String pcfname = params[i];
                
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
                
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-name")){
                
                i++;
                phcfPar.getHeader().setSourceID(params[i]);
                continue;
            }
           /* if (params[i].equalsIgnoreCase("-range")){
                double range = Double.parseDouble(params[++i]);
                ptfparams.setPrange(range);
                ptfparams.setPdrange(range);
                ptfparams.setPddrange(range);
                ptfparams.setDmrange(range);
                continue;
            
            }*/
            
            if (params[i].equalsIgnoreCase("-period-range")){
                ptfparams.setPrange(Double.parseDouble(params[++i])/1000.0);
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-period-range-percent")){
                ptfparams.setPrange((Double.parseDouble(params[++i])/100.0)*ptfparams.getCentrePeriod());
                continue;
            }
            if (params[i].equalsIgnoreCase("-accn-range")){
                ptfparams.setAccnRange(Double.parseDouble(params[++i]));
                continue;
            }
            if (params[i].equalsIgnoreCase("-jerk-range")){
                ptfparams.setJerkRange(Double.parseDouble(params[++i]));
                continue;
            }
            if (params[i].equalsIgnoreCase("-pdot-range")){
                ptfparams.setPdrange(Double.parseDouble(params[++i]));
                continue;
            }
            if (params[i].equalsIgnoreCase("-pddot-range")){
                ptfparams.setPddrange(Double.parseDouble(params[++i]));
                continue;
            }
            if (params[i].equalsIgnoreCase("-dm-range")){
                ptfparams.setDmrange(Double.parseDouble(params[++i]));
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-nbins")){
                ptfparams.setNprofilebins(Integer.parseInt(params[++i].trim()));
                continue;
            }
            
            
            
            if (params[i].equalsIgnoreCase("-nsub")){
                ptfparams.setNsub(Integer.parseInt(params[++i].trim()));
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-maxsub")){
                maxSubInts = Integer.parseInt(params[++i].trim());
                continue;
            }
            
            
            
            if (params[i].equalsIgnoreCase("-useaccn")||params[i].equalsIgnoreCase("-usepdot")){
                ptfparams.setUseAccn(true);
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-usejerk")||params[i].equalsIgnoreCase("-usepddot")){
                ptfparams.setUseJerk(true);
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-pdot")){
                ptfparams.setCentrePdot(Double.parseDouble(params[++i]));
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-pddot")){
                ptfparams.setCentrePddot(Double.parseDouble(params[++i]));
                continue;
            }
            
            
            if (params[i].equalsIgnoreCase("-accn")){
                ptfparams.setCenterAccn(Double.parseDouble(params[++i]));
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-jerk")){
                ptfparams.setCenterJerk(Double.parseDouble(params[++i]));
                continue;
            }
            
            if (params[i].equalsIgnoreCase("-ibands")){
                String[] elems = params[++i].split(",");
                int[] ibands = new int[elems.length];
                for(int j = 0; j < elems.length; j++){
                    ibands[j] = Integer.parseInt(elems[j].trim());
                }
                ptfparams.setIgnoreBands(ibands);
                continue;
            }
            if (params[i].equalsIgnoreCase("-isub")){
                String[] elems = params[++i].split(",");
                int[] ibands = new int[elems.length];
                for(int j = 0; j < elems.length; j++){
                    ibands[j] = Integer.parseInt(elems[j].trim());
                }
                ptfparams.setIgnoreSints(ibands);
                continue;
            }
            if (params[i].equalsIgnoreCase("-iloudsints")){
                ptfparams.setIgnoreLoudSubbands(true);
                if(params.length > i+1 && !params[i+1].startsWith("-")){
                    ptfparams.setSintIgnoreThreashold(Double.parseDouble(params[++i]));
                }
                continue;
            }
            
            
            
            if (params[i].equalsIgnoreCase("-fudge-initial-snr")){
                ptfparams.setRecalcInitialSNR(true);
                continue;
            }
            
//            if (params[i].equalsIgnoreCase("-pdothack")){
//                PdotFoldingModel.botchPdotAndPddot = true;
//                PulsarHunter.out.println("==                   Warning                  ==");
//                PulsarHunter.out.println("== Pdot/pddot values are being halved/doubled ==");
//                PulsarHunter.out.println("== This is a hack!                            ==");
//                PulsarHunter.out.println();
//                continue;
//            }
            
            // if we haven't continued... Like default from a switch:
            
            PulsarHunter.out.println("PeriodTune: Cannot understand command "+params[i]);
            this.printUsage();
            return null;
            
        }
        
        
        
        
        if(pcf!=null){
            PulsarPolyco ppc = pcf.getPolyCo(phcfPar.getHeader().getSourceID());
            if(ppc != null){
                ptfparams.setCenterPeriod(ppc.getPeriodAt(infile.getHeader().getMjdStart() + Convert.secToMJD(infile.getHeader().getTobs()/2.0)));
            }else {
                PulsarHunter.out.println("PeriodTune: Cannot load polyco for "+phcfPar.getHeader().getSourceID());
                pcf = null;
            }
            
        }
        
        if(phcfPar.getOptimisedSec()==null){
            PHCSection sec = new PHCSection("UserSpecified");
            
            double dopp = 1.0;
            if(BarryCenter.isAvaliable()){
                
                BarryCenter bc = new BarryCenter(infile.getHeader().getMjdStart(),
                        infile.getHeader().getTelescope(),
                        infile.getHeader().getCoord().getRA().toDegrees(),
                        infile.getHeader().getCoord().getDec().toDegrees());
                
                dopp = bc.getDopplerFactor();
                
            }
            double bp  = ptfparams.getCentrePeriod()/dopp;
            
            sec.setBestBaryPeriod(bp);
            sec.setBestTopoPeriod(ptfparams.getCentrePeriod());
            sec.setBestDm(ptfparams.getCenterDM());
            sec.setBestAccn(ptfparams.getCenterAccn());
            sec.setBestJerk(ptfparams.getCenterJerk());
            if ((infile instanceof TimeSeries)) {
                sec.setTsamp(((TimeSeries)infile).getHeader().getTSamp()*1000.0*1000.0);
            } else if(infile instanceof MultiChannelTimeSeries){
                sec.setTsamp(((MultiChannelTimeSeries)infile).getHeader().getTSamp()*1000.0*1000.0);
            }
            
            
            phcfPar.addSection(sec);
        }
        boolean changedRange = false;
        boolean changedNsub = false;
        double driftrate = ptfparams.getPrange()/ptfparams.getCentrePeriod();
        double tsub = infile.getHeader().getTobs()/ptfparams.getNsub();
        while(driftrate*tsub > ptfparams.getCentrePeriod()/10.0){
            
            if(ptfparams.getNsub() > maxSubInts){
                changedRange = true;
                ptfparams.setPrange(ptfparams.getPrange()/2.0);
            } else {
                changedNsub = true;
                ptfparams.setNsub(ptfparams.getNsub()*2);
            }
            driftrate = ptfparams.getPrange()/ptfparams.getCentrePeriod();
            tsub = infile.getHeader().getTobs()/ptfparams.getNsub();
        }
        
        
        if(changedNsub)PulsarHunter.out.println("PeriodTune - Period range too large, nsub increased to "+ptfparams.getNsub());
        if(changedRange)PulsarHunter.out.println("PeriodTune - Period range too large, range reduced to "+(ptfparams.getPrange()*1000.0)+"ms");
        if(changedRange)PulsarHunter.out.println("PeriodTune - To force larger range, increase nsub manualy.");
        
        
        
        
        
        
        if ((infile instanceof TimeSeries)) {
            return new PeriodTuneFold((TimeSeries) infile, ptfparams, phcfPar,interactive,pcf);
        } else if(infile instanceof MultiChannelTimeSeries){
            return new PeriodTuneFold((MultiChannelTimeSeries) infile, ptfparams, phcfPar,interactive,pcf);
        } else {
            throw new ProcessCreationException("First argument must be a timeseries for process "+this.getName());
        }
    }
    
    public String getName() {
        return "TUNE";
    }
    private void printUsage(){
        PulsarHunter.out.println("PeriodTuneFold Usage:");
        PulsarHunter.out.println("=====================");
        PulsarHunter.out.println();
        PulsarHunter.out.println(" -period      set the topocentric center period (ms)");
        PulsarHunter.out.println(" -bary-period set the barycentric center period (ms)");
        PulsarHunter.out.println(" -accn        set the center accn (m/s/s)");
        PulsarHunter.out.println(" -jerk        set the center jerk (m/s/s/s)");
        PulsarHunter.out.println();
        PulsarHunter.out.println(" -useaccn     search in accn space");
        PulsarHunter.out.println(" -usejerk     search in jerk space");
        PulsarHunter.out.println();
        //PulsarHunter.out.println(" -step set the step size");
        //PulsarHunter.out.println(" -period-step, -pdot-step, -dm-step\n\tSet the appropriate step sizes");
        
        PulsarHunter.out.println(" -period-range set the period search range (ms)");
        PulsarHunter.out.println(" -accn-range   set the accn search range (m/s/a)");
        PulsarHunter.out.println(" -jerk-range   set the jerk search range (m/s/s/s)");
        PulsarHunter.out.println(" -dm-range     set the dm search range (cm^-3/pc)");
        //PulsarHunter.out.println(" -range      set the range, 2 = 2x wider range");
        //PulsarHunter.out.println(" -period-step, -pdot-step, -dm-step\n\tSet the appropriate range");
        PulsarHunter.out.println(" -period-range set the period search step size (ms)");
        PulsarHunter.out.println(" -accn-range   set the accn search step size (m/s/s)");
        PulsarHunter.out.println(" -jerk-range   set the jerk search step size (m/s/s/s)");
        PulsarHunter.out.println(" -dm-range     set the dm search step size (cm^-3/pc)");
        PulsarHunter.out.println();
        //PulsarHunter.out.println("    The number of steps is the same if range and step are equal");
        PulsarHunter.out.println(" -harmonic     multiply the period by this value");
        PulsarHunter.out.println();
        PulsarHunter.out.println(" -nbins        set the number of bins in the output profile");
        PulsarHunter.out.println(" -nsub         set the number of subints to use");
        PulsarHunter.out.println(" -ibands       give comma seperated list of bands to ignore");
        PulsarHunter.out.println(" -isub         give comma seperated list of subints to ignore");
        PulsarHunter.out.println(" -iloudsints   specify rms threashold above which subints are ignored (compared to mean rms)");
        PulsarHunter.out.println("");
        PulsarHunter.out.println("Untested options:");
        PulsarHunter.out.println(" -fudge-initial-snr  Force set the 'initial snr' to the folded, but unoptimised snr.");
        PulsarHunter.out.println(" -interactive        Use the 'interactive RFI zapper'. Useful, but far from finished.");
        
        
        
    }
    
    
    
}
