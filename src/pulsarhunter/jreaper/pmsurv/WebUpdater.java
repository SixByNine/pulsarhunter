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
 * WebUpdater.java
 *
 * Created on 28 May 2005, 17:28
 */

package pulsarhunter.jreaper.pmsurv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import java.util.Hashtable;
import pulsarhunter.jreaper.DataLibrary;

/**
 *
 * @author mkeith
 */
public class WebUpdater {
    
    
    File logFile;
    File class1File;
    File class2File;
    File class3File;
    File classUFile;
    File class0File;
    
    public WebUpdater(DataLibrary dl){
        //DataLibrary dl =Main.getInstance().getDataLibrary();
        if(dl instanceof PMDataLibrary){
            logFile = new File(((PMDataLibrary)dl).getRealRoot().getAbsolutePath()+File.separatorChar+"webCandList");
            File webRoot = ((PMDataLibrary)dl).getWebRoot();
            this.class1File = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class1");
            this.class2File = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class2");
            this.class3File = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class3");
            this.classUFile = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"classU");
            this.class0File = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class0");
        } else {
            throw new RuntimeException("Cannot update website unless conected to a PMSurv database");
        }
    }
    
    /** Creates a new instance of WebUpdater */
    public WebUpdater(File logFile, File webRoot) {
        this.logFile = logFile;
        this.class1File = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class1");
        this.class2File = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class2");
        this.class3File = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class3");
        this.classUFile = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"classU");
        this.class0File = new File(webRoot.getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class0");
    }
    
    public boolean waiting = false;
    
    public void update(){
        if(waiting) return;
        synchronized (this){
            File webCandListLock = new File(logFile.getAbsolutePath()+"lock");
            try{
                while(webCandListLock.exists()){
                    waiting = true;
                    Thread.currentThread().wait(1000);
                }
            } catch(InterruptedException e){
                throw new RuntimeException(e);
            }
            
            
            //waiting = false;
            long mod = logFile.lastModified();
            Hashtable<String,LogFileData> data = new Hashtable<String,LogFileData>();
            try{
                BufferedReader instream = new BufferedReader(new FileReader(logFile));
                String line;
                
                while((line=instream.readLine())!=null){
                    String[] contents = line.split("\\|\\|");
                    if(contents.length < 12 )continue;
                    String hist = contents[11];
                    if(data.containsKey(contents[0])){
                        LogFileData tmp = data.get(contents[0]);
                        hist = tmp.getHistory() +"$$"+ hist;
                        data.remove(contents[0]);
                    }
                    
                    
                    LogFileData dat = new LogFileData(contents[0],contents[1],contents[2],contents[3],contents[4],contents[5],Integer.parseInt(contents[6].trim()),Double.parseDouble(contents[7]),Double.parseDouble(contents[8]),Double.parseDouble(contents[9]),Double.parseDouble(contents[10]),hist);
                    data.put(contents[0], dat);
                    
                    
                }
                instream.close();
                if(logFile.lastModified() == mod){
                    logFile.delete();
                    PrintWriter out = new PrintWriter(new FileWriter(logFile));
                    Enumeration<LogFileData> e = data.elements();
                    while(e.hasMoreElements()){
                        LogFileData dat = e.nextElement();
                        out.print(dat.getPHname());
                        out.print("||");
                        out.print(dat.candName);
                        out.print("||");
                        out.print(dat.getRA());
                        out.print("||");
                        out.print(dat.getDec());
                        out.print("||");
                        out.print(dat.getGl());
                        out.print("||");
                        out.print(dat.getGb());
                        out.print("||");
                        out.print(dat.getpClass());
                        out.print("||");
                        out.print(dat.getPeriod());
                        out.print("||");
                        out.print(dat.getAccel());
                        out.print("||");
                        out.print(dat.getDM());
                        out.print("||");
                        out.print(dat.getSNR());
                        out.print("||");
                        out.print(dat.getHistory());
                        out.println("");
                    }
                    out.close();
                }
                if(class1File.exists())class1File.delete();
                if(class2File.exists())class2File.delete();
                if(class3File.exists())class3File.delete();
                if(classUFile.exists())classUFile.delete();
                if(class0File.exists())class0File.delete();
                
                class1File.getParentFile().mkdirs();
                class2File.getParentFile().mkdirs();
                class3File.getParentFile().mkdirs();
                classUFile.getParentFile().mkdirs();
                class0File.getParentFile().mkdirs();
                
                PrintWriter class1Out = new PrintWriter(new FileWriter(class1File));
                PrintWriter class2Out = new PrintWriter(new FileWriter(class2File));
                PrintWriter class3Out = new PrintWriter(new FileWriter(class3File));
                PrintWriter classUOut = new PrintWriter(new FileWriter(classUFile));
                PrintWriter class0Out = new PrintWriter(new FileWriter(class0File));
                PrintWriter out = null;
                Enumeration<LogFileData> e = data.elements();
                while(e.hasMoreElements()){
                    LogFileData dat = e.nextElement();
                    switch(dat.getpClass()){
                        case 0:
                            out = class0Out;
                            break;
                        case 1:
                            out = class1Out;
                            break;
                        case 2:
                            out = class2Out;
                            break;
                        case 3:
                            out = class3Out;
                            break;
                        default:
                            out = classUOut;
                            break;
                    }
                    out.print(dat.getPHname());
                    out.print("||");
                    out.print(dat.candName);
                    out.print("||");
                    out.print(dat.getRA());
                    out.print("||");
                    out.print(dat.getDec());
                    out.print("||");
                    out.print(dat.getGl());
                    out.print("||");
                    out.print(dat.getGb());
                    out.print("||");
                    out.print(dat.getPeriod());
                    out.print("||");
                    out.print(dat.getAccel());
                    out.print("||");
                    out.print(dat.getDM());
                    out.print("||");
                    out.print(dat.getSNR());
                    out.print("||");
                    out.print(dat.getHistory());
                    out.println("");
                }
                class1Out.close();
                class2Out.close();
                class3Out.close();
                classUOut.close();
                class0Out.close();
            }catch(IOException e){
                throw new RuntimeException(e.getMessage(),e);
            }
            waiting = false;
        }
    }
    
    private class LogFileData {
        
        private String phname,candName;
        private String ra;
        private String dec;
        private double period,accel;
        private double dm;
        private double snr;
        private String history;
        private int pclass;
        private String gl,gb;
        public LogFileData(String phname,String candName, String ra,String dec,String gl,String gb,int pclass,double period,double accel,double dm,double snr,String history){
            this.candName = candName;
            this.dec = dec;
            this.ra = ra;
            this.dm = dm;
            this.phname = phname;
            this.snr = snr;
            this.history = history;
            this.pclass = pclass;
            this.period = period;
            this.accel  =accel;
            this.gb = gb;
            this.gl = gl;
        }
        
        public boolean equals(Object obj) {
            
            if(obj instanceof LogFileData){
                LogFileData dat = (LogFileData)obj;
                return phname.equals(dat.phname);
            }
            return false;
        }
        
        public String getPHname(){
            return phname;
        }
        
        public String getRA(){
            return ra;
        }
        
        public String getDec(){
            return dec;
        }
        
        public double getPeriod(){
            return period;
        }
        
        public double getDM(){
            return dm;
        }
        
        public double getSNR(){
            return snr;
        }
        
        public String getGl(){
            return gl;
            
        }
        
        
        public String getGb(){
            return gb;
        }
        
        
        public double getAccel(){
            return accel;
        }
        
        public String getHistory(){
            return history;
        }
        
        
        public int getpClass(){
            return pclass;
        }
        
    }
    
    
    /*
     *
                out.print(cand.getPHfile().getName());
                out.print("||");
                out.print(cand.getName());
                out.print("||");
                out.print(cand.getBeam().getCoord().getRA());
                out.print("||");
                out.print(cand.getBeam().getCoord().getDec());
                out.print("||");
                out.print(cand.getCandClass());
                out.print("||");
                out.print(cand.getPeriod());
                out.print("||");
                out.print(cand.getDM());
                out.print("||");
                out.print(cand.getSNR());
                out.print("||");
                out.print(cand.getComment());
     */
}
