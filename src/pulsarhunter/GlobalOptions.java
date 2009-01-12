/*
 * GlobalOptions.java
 *
 * Created on July 22, 2007, 11:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

import java.util.Hashtable;

/**
 *
 * @author mkeith
 */
public class GlobalOptions {
    
    public enum Option{
        // CandList Creation
        reswd(String.class),resdir(String.class),searchtype(Integer.class),beamid(String.class),dmlist(String.class),
        // FilterCandidates
        minsnr(Double.class),maxresults(Integer.class),userecon(Boolean.class),zapfile(String.class),
        minprofilebins(Double.class),dumpharmonics(Boolean.class),matchfactor(Double.class),
        nophcx(Boolean.class),
        // PeriodTune
        period(Double.class),baryperiod(Double.class),periodstep(Double.class),periodrange(Double.class),periodrangepercent(Double.class),
        useaccn(Boolean.class),accn(Double.class),accnstep(Double.class),accnrange(Double.class),
        usejerk(Boolean.class),jerk(Double.class),jerkstep(Double.class),jerkrange(Double.class),
        dm(Double.class),dmrange(Double.class),dmstep(Double.class),
        harmonic(Double.class),polyco(String.class),name(String.class),
        nbins(Integer.class),nsub(Integer.class),maxsub(Integer.class),
        ibands(String.class),isub(String.class),interactive(Boolean.class),iloudsints(Boolean.class),
        fudgeinitialsnr(Boolean.class),
        fakesnr(Double.class),fakenulling(Boolean.class),fakenulltimescale(Double.class),
        fakepulsetimescale(Double.class),fakenullvariance(Double.class),fakenullstatistics(String.class),
        pulsewidth(Double.class), 
        // viewphcf
        imageoutput(Boolean.class);
        
        
        private Class objectType;
        Option(Class objectType){
            this.objectType = objectType;
        }
        
        public Class getObjectType() {
            return objectType;
        }
        
    };
    
    
    
    Hashtable<Option,Object> options = new Hashtable<Option,Object>();
    
    
    /** Creates a new instance of GlobalOptions */
    public GlobalOptions() {
    }
    
    public void parseArgs(String argList){

        String[] elems = argList.split("\\s+");
        for(int i = 0; i < elems.length; i++){
            String argname = elems[i].replace("-","").trim().toLowerCase();
            Option opt = null;
            try {
                opt = Option.valueOf(argname);
            } catch (IllegalArgumentException e) {
            }
            if(opt==null){
                System.err.println("Unknown global option "+argname+" Passed...");
                continue;
            }
            if(opt.getObjectType().equals(Boolean.class)){
                this.setArg(opt,true);
                continue;
            }
            if(opt.getObjectType().equals(String.class)){
                this.setArg(opt,elems[++i]);
                continue;
            }
            if(opt.getObjectType().equals(Double.class)){
                this.setArg(opt,Double.parseDouble(elems[++i]));
                continue;
            }
            if(opt.getObjectType().equals(Integer.class)){
                this.setArg(opt,Integer.parseInt(elems[++i]));
                continue;
            }
            
            
        }
    }
    
    public Object getArg(Option opt){
        return this.options.get(opt);
    }
    
    public void setArg(Option opt, Object value) throws IllegalArgumentException{
        if(value==null || value.getClass().equals(opt.getObjectType())){
            this.options.remove(opt);
            this.options.put(opt,value);
        } else {
            throw new IllegalArgumentException("Option "+opt.toString()+" requires an argument of type "+opt.getObjectType());
        }
    }
    
    
}
