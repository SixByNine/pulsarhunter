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
 * RecipeParser.java
 *
 * Created on 25 October 2006, 11:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.recipies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import pulsarhunter.Data;
import pulsarhunter.ProcessCreationException;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.DataFactory;
import pulsarhunter.GlobalOptions.Option;
import pulsarhunter.GuessDataFactory;
import pulsarhunter.IncorrectDataTypeException;
import pulsarhunter.ProcessFactory;
import pulsarhunter.PulsarHunter;
import pulsarhunter.PulsarHunterRegistry;

/**
 *
 * @author mkeith
 */
public class RecipeParser {
    private ArrayList<String> recipiePaths = new ArrayList<String>();
    private enum Commands{END,EXIT,QUIT,FOLLOW,PROCESS,SET,UNSET,LIST,IMPORT,VIRTUAL,EXPORT,CLOSE,FOR,NEXT,ECHO,USAGE,HELP};
    
    private Hashtable<String,Data> dataFiles = new Hashtable<String,Data>();
    private Hashtable<String,Boolean> writePermittedFiles = new Hashtable<String,Boolean>();
    
    
    private PulsarHunterRegistry pulsarHunterRegistry;
    private boolean prompt;
    private boolean usageMode;
    /** Creates a new instance of RecipeParser */
    public RecipeParser( PulsarHunterRegistry pulsarHunterRegistry, boolean prompt,boolean usageMode) {
        this.pulsarHunterRegistry =  pulsarHunterRegistry;
        this.prompt = prompt;
    }
    
    
    
    public void parse(BufferedReader input,String[] args) throws IOException,RecipeParseException{
        if(prompt)System.out.print("PulsarHunter> ");
        
        
        String line = input.readLine();
        while(line != null){
            line = line.trim();
            if(line.startsWith("#")){
                // This is a comment
                line = input.readLine();
                continue;
            }
            try{
                if(line.length()>0){
                    String[] elems = line.split(" ",2);
                    
                    Commands cmd = null;
                    try {
                        cmd = Commands.valueOf(elems[0].toUpperCase());
                    } catch(IllegalArgumentException e) {
                        throw new RecipeParseException("Command "+elems[0]+" is not recognised. Use HELP for help");
                    }
                    if(cmd==Commands.END||cmd==Commands.EXIT|| cmd==Commands.QUIT||  (usageMode&&cmd!=Commands.USAGE)){
                        end();
                        break;
                    }
                    if(cmd==Commands.HELP){
                        System.out.println("PulsarHunter Help");
                        System.out.println("Sorry, the full help is not implemented yet...");
                        System.out.println("List of valid commands:");
                        for(Commands c : Commands.values()){
                            System.out.println(c);
                        }
                        
                    } else {
                        if(elems.length!=2)throw new RecipeParseException("Invalid command. There must be a keyword and one or more arguments");
                        
                        if(elems[1].contains("$ARGS"))elems[1] = replaceArgs(elems[1],args);
                        if(elems[1].contains("$SW"))elems[1] = replaceSwitches(elems[1]);
                        switch(cmd){
                            case IMPORT:
                                this.importFile(elems[1]);
                                break;
                       /* case IMPORTIFEXISTS:
                            try {
                                this.importFile(elems[1]);
                            } catch (RecipeParseException ex) {
                                this.exportFile(elems[1]);
                            }
                            break;*/
                            case EXPORT:
                                this.exportFile(elems[1]);
                                break;
                            case FOLLOW:
                                follow(elems[1]);
                                break;
                            case PROCESS:
                                process(elems[1]);
                                break;
                            case FOR:
                                forloop(elems[1]);
                                break;
                            case NEXT:
                                next(elems[1]);
                                break;
                            case SET:
                                set(elems[1]);
                                break;
                            case UNSET:
                                unset(elems[1]);
                                break;
                            case LIST:
                                list(elems[1]);
                                break;
                            case CLOSE:
                                closeFile(elems[1]);
                                break;
                            case ECHO:
                                PulsarHunter.out.println(elems[1]);
                                break;
                            case USAGE:
                                if(usageMode)PulsarHunter.out.println(elems[1]);
                                break;
                            case HELP:
                                
                                break;
                            default:
                                throw new RecipeParseException("Command "+elems[0]+" is not recognised. Use HELP for help");
                        }
                    }
                }
                
            } catch(RecipeParseException e){
                if(prompt){
                    System.err.println("Error: "+e.getMessage());
                    if(e.getCause()!=null){
                        System.err.println("Err Cause:"+e.getCause().getMessage()); 
                    }
                } else{
                    
                    throw e;
                }
            }
            if(prompt)System.out.print("PulsarHunter> ");
            line = input.readLine();
        }
        
    }
    
    private String replaceArgs(String target, String[] args) throws RecipeParseException{
        StringBuffer buf = new StringBuffer(target);
        
        while(true){
            int posn = buf.indexOf("$ARGS");
            if(posn<0)break;
            try{
                if(posn+5 >= buf.length()){
                    //throw new RecipeParseException("Badly formated argument specifier in command "+target);
                                                buf.insert(posn, " ");
                    buf.replace(posn,posn+6,args[args.length-1]);

                    for(int i = args.length-2 ; i >=0 ; i--){
                                                buf.insert(posn, " ");
                        buf.insert(posn, args[i]);
                    }
                     //                                       System.out.println(posn+"\t"+buf);
                    continue;
                }
                int i = Integer.parseInt(String.valueOf(buf.charAt(posn+5)))-1;
                if(i >= args.length) throw new RecipeParseException("This recipe tried to use argument "+(i+1)+", however only "+args.length+" were specified");
                buf.replace(posn,posn+6,args[i]);
            } catch (NumberFormatException e){
                throw new RecipeParseException("Badly formated argument specifier in command "+target);
            }
        }
        
        return buf.toString();
    }
    
    
    /**
     * 
     * @deprecated Use global options rather than SW switches.
     * @param target 
     * @throws pulsarhunter.recipies.RecipeParseException 
     * @return 
     */
    private String replaceSwitches(String target) throws RecipeParseException{
        StringBuffer buf = new StringBuffer(target);
        System.out.println("Warning: Use of $SW switches is depricated, use global options instead. ("+target+")");
        while(true){
            int posn = buf.indexOf("$SW");
            if(posn<0)break;
            try{
                if(posn+3 >= target.length())throw new RecipeParseException("Badly formated switches specifier in command "+target);
                String ss = buf.substring(posn+3);
                String key = ss.split("\\s+")[0];
                String switches = this.pulsarHunterRegistry.getSwitches(key);
                if(switches == null) switches = "";
                buf.replace(posn,posn+3+key.length(),switches);
            } catch (NumberFormatException e){
                throw new RecipeParseException("Badly formated argument specifier in command "+target);
            }
        }
        
        return buf.toString();
    }
    
    
    
    private void end(){
        for(String fid : this.dataFiles.keySet()){
            this.closeFile(fid);
        }
    }
    
    private void list(String command)throws RecipeParseException{
        
        if(command.equalsIgnoreCase("files")){
            for(String s : this.dataFiles.keySet()){
                System.out.print(s+": "+this.getDataType(dataFiles.get(s)));
                if(this.writePermittedFiles.get(s)==null || !this.writePermittedFiles.get(s)){
                    System.out.print("  [READONLY]");
                }
                System.out.println();
            }
            return;
        }
        if(command.equalsIgnoreCase("options")){
            for(Option o: Option.values()){
                if(this.pulsarHunterRegistry.getOptions().getArg(o)!=null){
                    
                    System.out.println(o+": "+this.pulsarHunterRegistry.getOptions().getArg(o));
                }
            }
            return;
        }
        if(command.equalsIgnoreCase("filetypes")){
            for(String s : this.pulsarHunterRegistry.getDataFactoryList()){
                System.out.println(s);
            }
            return;
        }
        if(command.equalsIgnoreCase("processtypes")){
            for(String s : this.pulsarHunterRegistry.getProcessFactory()){
                System.out.println(s);
            }
            return;
        }
        
        throw new RecipeParseException("Valid choices are files, options, filetypes, processtypes");
    }
    private void set(String command)throws RecipeParseException{
        
        this.pulsarHunterRegistry.getOptions().parseArgs(command);
    }
    
    private void unset(String command){
        
        Option opt=null;
        try{
            opt = Option.valueOf(command);
        } catch (IllegalArgumentException e){
            System.err.println("Unknown option "+opt+"\n Possible choices are:");
            
            for(Option o : Option.values()){
                
                System.err.println(o.toString());
            }
        }
        
        
        this.pulsarHunterRegistry.getOptions().setArg(opt,null);
    }
    
    private void closeFile(String command){
        Data d = this.dataFiles.remove(command);
        if(this.writePermittedFiles.get(command)!=null && this.writePermittedFiles.get(command)){
            try {
                System.out.println("Writing data "+command);
                d.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            d.release();
        } else {
            System.out.println("Data "+command+" not written (read-only)");
        }
        
    }
    private void importFile(String command)throws RecipeParseException{
        String[] elems = command.split(" ");
        String filename = "_nofilegiven_";
        String importname = null;
        int mem = 1000000;
        boolean writable = true;
        DataFactory df = new GuessDataFactory(this.pulsarHunterRegistry);
        
        filename = elems[0];
        
//        switch(elems.length){
//            case 7:
//                if(elems[5].equalsIgnoreCase("AS")){
//                    df = this.pulsarHunterRegistry.getDataFactory(elems[6].toUpperCase());
//                    if(df==null){
//                        Data asFile = this.dataFiles.get(elems[6]);
//                        if(asFile!=null){
//                            df = this.pulsarHunterRegistry.getDataFactory(this.getDataType(asFile).toUpperCase());
//                        }
//                    }
//                } else if(elems[5].equalsIgnoreCase("TO")){
//                    importname = elems[6];
//                } else if(elems[5].equalsIgnoreCase("MEM")){
//                    if(elems[6].equalsIgnoreCase("MAX")){
//                        mem = Integer.MAX_VALUE;
//                    } else {
//                        mem = Integer.parseInt(elems[6]);
//                    }
//                }
//            case 5:
//                if(elems[3].equalsIgnoreCase("AS")){
//                    df = this.pulsarHunterRegistry.getDataFactory(elems[4].toUpperCase());
//                    if(df==null){
//                        Data asFile = this.dataFiles.get(elems[4]);
//                        if(asFile!=null){
//                            df = this.pulsarHunterRegistry.getDataFactory(this.getDataType(asFile).toUpperCase());
//                        }
//                    }
//                } else if(elems[3].equalsIgnoreCase("TO")){
//                    importname = elems[4];
//                } else if(elems[3].equalsIgnoreCase("MEM")){
//                    if(elems[4].equalsIgnoreCase("MAX")){
//                        mem = Integer.MAX_VALUE;
//                    } else {
//                        mem = Integer.parseInt(elems[4]);
//                    }
//                }
//            case 3:
//                if(elems[1].equalsIgnoreCase("AS")){
//                    df = this.pulsarHunterRegistry.getDataFactory(elems[2].toUpperCase());
//                    if(df==null){
//                        Data asFile = this.dataFiles.get(elems[2]);
//                        if(asFile!=null){
//                            df = this.pulsarHunterRegistry.getDataFactory(this.getDataType(asFile).toUpperCase());
//                        }
//                    }
//                } else if(elems[1].equalsIgnoreCase("TO")){
//                    importname = elems[2];
//                } else if(elems[1].equalsIgnoreCase("MEM")){
//                    if(elems[4].equalsIgnoreCase("MAX")){
//                        mem = Integer.MAX_VALUE;
//                    } else {
//                        mem = Integer.parseInt(elems[2]);
//                    }
//                }
//            case 1:
//                filename = elems[0];
//                break;
//            default:
//                throw new RecipeParseException("IMPORT: Badly Formated command "+command);
//
//        }
        
        
        for(int i = 1 ; i < elems.length; i++){
            
            if(elems[i].equalsIgnoreCase("AS")){
                i++;
                df = this.pulsarHunterRegistry.getDataFactory(elems[i].toUpperCase());
                if(df==null){
                    Data asFile = this.dataFiles.get(elems[i]);
                    if(asFile!=null){
                        df = this.pulsarHunterRegistry.getDataFactory(this.getDataType(asFile).toUpperCase());
                    }
                }
                continue;
            }
            
            if(elems[i].equalsIgnoreCase("TO")){
                i++;
                importname = elems[i];
                continue;
            }
            
            if(elems[i].equalsIgnoreCase("MEM")){
                i++;
                if(elems[i].equalsIgnoreCase("MAX")){
                    mem = Integer.MAX_VALUE;
                } else {
                    mem = Integer.parseInt(elems[i]);
                }
                continue;
            }
            if(elems[i].equalsIgnoreCase("RO")){
                writable = false;
                continue;
            }
            
            throw new RecipeParseException("IMPORT: Badly Formated command "+command);
            
        }
        
        
        
        mem = (int)getMem(mem);
        
        if(importname == null)importname = filename;
        Data data = null;
        if(df==null)  throw new RecipeParseException("IMPORT: Unknown data format for file "+filename);
        try{
            data = df.loadData(filename,mem);
        } catch(IncorrectDataTypeException e){
            
            if(!new File(filename).exists()){
                PulsarHunter.out.println("File "+filename+" does not exist, trying to create new...");
                try{
                    data = df.createData(filename);
                } catch(IncorrectDataTypeException ex){
                    throw new RecipeParseException("IMPORT: Incorrect or unknown data format for file "+filename,ex);
                }
                
            } else {
                
                throw new RecipeParseException("IMPORT: Incorrect or unknown data format for file "+filename,e);
            }
        }
        this.dataFiles.put(importname,data);
        this.writePermittedFiles.put(importname,writable);
        
        
    }
    
    
    private void exportFile(String command)throws RecipeParseException{
        String[] elems = command.split(" ");
        
        String importname = null;
        DataFactory df =null;
        if(elems.length!=5) throw new RecipeParseException("IMPORT: Badly Formated command "+command);
        
        if(elems[3].equalsIgnoreCase("AS")){
            df = this.pulsarHunterRegistry.getDataFactory(elems[4].toUpperCase());
            if(df==null){
                Data asFile = this.dataFiles.get(elems[4]);
                if(asFile!=null){
                    df = this.pulsarHunterRegistry.getDataFactory(this.getDataType(asFile).toUpperCase());
                }
            }
        } else if(elems[3].equalsIgnoreCase("TO")){
            importname = elems[4];
        }
        
        if(elems[1].equalsIgnoreCase("AS")){
            df = this.pulsarHunterRegistry.getDataFactory(elems[2].toUpperCase());
            if(df==null){
                Data asFile = this.dataFiles.get(elems[2]);
                if(asFile!=null){
                    df = this.pulsarHunterRegistry.getDataFactory(this.getDataType(asFile).toUpperCase());
                }
            }
        } else if(elems[1].equalsIgnoreCase("TO")){
            importname = elems[2];
        }
        
        
        
        
        String filename = elems[0];
        
        Data data = null;
        if(df==null){
            throw new RecipeParseException("EXPORT: Incorrect or unknown data format for file "+filename);
        }
        try{
            data = df.createData(filename);
        } catch(IncorrectDataTypeException e){
            throw new RecipeParseException("EXPORT: Incorrect or unknown data format for file "+filename,e);
        }
        this.dataFiles.put(importname,data);
        // When exporting RO makes no sense!
        this.writePermittedFiles.put(importname,true);
        
        
    }
    
    private void follow(String command) throws RecipeParseException{
        String[] split = command.split(" ",2);
        String[] elems = new String[0];
        if(elems.length < 2)  elems = split[1].split(" ");
        File f = new File(split[0]);
        
        for(String s: recipiePaths){
            if(f.exists())break;
            f = new File(s+split[0]);
        }
        
        if(f.exists()){
            try{
                BufferedReader reader = new BufferedReader(new FileReader(f));
                this.parse(reader,elems);
            } catch(IOException e){
                throw new RecipeParseException("FOLLOW: IOException parsing recipe file "+command,e);
            }
        } else throw new RecipeParseException("FOLLOW: Could not locate recipe file for"+command);
        
        
    }
    private void process(String command) throws RecipeParseException{
        String[] elems = command.split(" ");
        String procName = elems[0];
        ProcessFactory pf = this.pulsarHunterRegistry.getProcessFactory(procName.toUpperCase());
        if(pf==null) throw new RecipeParseException("PROCESS: Process "+procName+" non existant.");
        PulsarHunterProcess p;
        try {
            p = pf.createProcess(elems, dataFiles,this.pulsarHunterRegistry);
        } catch (ProcessCreationException ex) {
            throw new RecipeParseException("PROCESS: Error creating "+procName+" "+ex.getMessage());
        }
        if(p!= null)p.run();
    }
    private void forloop(String command) throws RecipeParseException{
        
    }
    private void next(String command) throws RecipeParseException{
        
    }
    
    public Hashtable<String, Data> getDataFiles() {
        return dataFiles;
    }
    
    public static long getMem(long requested){
        /// Runtime.getRuntime().gc();
        long mem = requested;
        long availMem = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/10l;
        if(mem > availMem){
            
            Runtime.getRuntime().gc();
            availMem = Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/10l;
            if(mem > availMem){
                mem = (int)(availMem*0.4);
            }
            //  System.out.println("MEM:"+requested+" - "+mem+" avail:"+availMem);
        }
//        System.out.println("MEM:"+requested+" - "+mem+" avail:"+availMem);
        return mem;
    }
    
    
    
    private String getDataType(Data dataType){
        String res = dataType.getDataType();
        if(res==null){
            res = dataType.getClass().toString();
        }
        return res;
    }
}
