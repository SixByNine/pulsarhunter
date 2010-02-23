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
 * DataLibrary.java
 *
 * Created on 26 May 2005, 15:33
 */

package pulsarhunter.jreaper;

import coordlib.Coordinate;
import coordlib.CoordinateDistanceComparitor;
import coordlib.Dec;
import coordlib.RA;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import pulsarhunter.Pair;
import pulsarhunter.jreaper.gui.LoadingSplash;

/**
 * This is a class that represents a library of data in the PulsarSweep/JReaper system.
 * The DataLibrary contains a number of candidate lists ({@link CandList} objects)
 * that represent all the data appropriate to the library.
 * The DataLibrary also gives out default <CODE>JPanel</CODE>s for loading and importing data.
 * These can be overridden by subclasses to allow for custom load panels etc.
 * The DataLibrary also returns a reference to a {@link CandRefine} class that performs refinements
 * on the data, e.g. locating harmonics and filtering.
 *
 * Subclasses that automaticaly sync with a external data source should implement the {@link WebDataLibrary} interface.
 * @author mkeith
 */
public class DataLibrary{
    static final long serialVersionUID = 1000L;
    
    private enum xmlElements{DataLibrary};
    
    private Hashtable<String,Hashtable<String,File>> data = new Hashtable<String,Hashtable<String,File>>();
    private Hashtable<String,Hashtable<String,Coordinate[]>> coverage = new Hashtable<String,Hashtable<String,Coordinate[]>>();
    //  private Hashtable<String,CandListHeader> headers = new Hashtable<String,CandListHeader>();
    private Hashtable<String,Hashtable<String,CandListHeader>> groups = new Hashtable<String,Hashtable<String,CandListHeader>>();
    
    private ArrayList<KnownPSR> knownPSR = null;
    
    private File rootpath;
    
    private Options options = new Options();
    
    private CandRefine refine = null;
    
    /**
     * Creates a new instance of DataLibrary
     * @param rootpath The root path that the DataLibrary will store all its candidate files.
     */
    public DataLibrary(File rootpath) {
        this.rootpath = rootpath;
        rootpath.mkdirs();
    }
    
    public DataLibrary(){
    }
    
    /**
     * Allows the rootpath to be changed.
     * @param rootpath The new location for candidate files.
     */
    protected void setRootPath(File rootpath){
        this.rootpath = rootpath;
    }
    
    
    
    public void write(PrintStream out) throws IOException{
        
        final LoadingSplash lsplash = new LoadingSplash();
        
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        out.println("<DataLibrary java_class=\""+this.getClass().getName()+"\">");
        out.println("<RootPath>"+this.getRootPath()+"</RootPath>");
        options.write(out);
        int i = 0;
        for(String gkey : groups.keySet()){
            i += groups.get(gkey).size();
        }
        final int max = i;
        i = 0;
        
        
        for(String gkey : groups.keySet()){
            out.println("<CandLists group_name=\""+gkey+"\">");
            Hashtable<String,CandListHeader> group = groups.get(gkey);
            for(String ckey : group.keySet()){
                final int count = i++;
                java.awt.EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        lsplash.setProgress("Saving Data Library",count,max);
                        lsplash.setVisible(true);
                    }
                });
                
                
                out.println("<CandList file=\""+data.get(gkey).get(ckey).getPath()+"\">");
                group.get(ckey).write(out);
                out.println("</CandList>");
            }
            out.println("</CandLists>");
        }
        writeExtra(out);
        out.println("</DataLibrary>");
        out.close();
        java.awt.EventQueue.invokeLater(new Runnable(){
            public void run(){
                lsplash.setVisible(false);
                lsplash.dispose();
            }
        });
    }
    
    protected void writeExtra(PrintStream out) throws IOException{
        
    }
    
    protected void setCandListGroup(String name, Hashtable<String,CandListHeader> group, Hashtable<String,File> files){
        Hashtable<String,CandListHeader> removed = groups.remove(name);
        
        if(removed != null){
            // there was already one here!
            // delete the entries from the data and coverage table
            for(String key : removed.keySet()){
                data.remove(name+"::"+key);
                coverage.remove(name+"::"+key);
                
            }
        }
        
        // Add the group...
        this.groups.put(name,group);
        
        
        // add the data and coordinate entries...
        for(String key : group.keySet()){
            if(!this.coverage.containsKey(name)) this.coverage.put(name,new Hashtable<String,Coordinate[]>());
            if(!this.data.containsKey(name)) this.data.put(name,new Hashtable<String,File>());
            this.coverage.get(name).put(key,new Coordinate[]{group.get(key).getCoord()});
            this.data.get(name).put(key,files.get(key));
            
        }
        
    }
    
    public void recalcuateCoverage(){
        
    }
    
    
    
    protected Hashtable<String, Hashtable<String,Coordinate[]>> getCoverage() {
        return coverage;
    }
    
    
    public void importClistsFromDir(File file) {
        importClistsFromDir(file,null);
    }


    public void importClistsFromDir(final File file,String group) {
        importClistsFromDir(file, group,null);
    }
    public void importClistsFromDir(final File file,String group,final LoadingSplash corelsplash) {
        final LoadingSplash lsplash;
        if(corelsplash==null)lsplash = new LoadingSplash();
        else lsplash=corelsplash;
        // System.out.println("Reading Dir "+file.getName());
        File[] fList = file.listFiles(new FileFilter(){
            public boolean accept(File pathname){
                return pathname.isDirectory() || pathname.getName().endsWith("clist")|| pathname.getName().endsWith("clist.gz");
            }
        });
        if(fList==null){
            System.err.println("Dir "+file.getName()+" Does not exist!");
        } else {
            int i = 0;
            final int dirsize = fList.length;
            for(final File f : fList){
                final int count = i++;
                if(f.isDirectory()) {
                    if(group == null) importClistsFromDir(f,f.getName(),lsplash);
                    else importClistsFromDir(f,group,lsplash);
                } else if(f.getName().endsWith(".clist.gz") || f.getName().endsWith(".clist")){
                    if(!data.contains(f)){
                        
                        java.awt.EventQueue.invokeLater(new Runnable(){
                            public void run(){
                                lsplash.setProgress("Reading Dir: "+file.getName(),count,dirsize);
                                lsplash.setVisible(true);
                            }
                        });
                        
                        if(group==null){
                            importClistFromFile(f,"default");
                        } else {
                            importClistFromFile(f,group);
                        }
                    }
                }
            }
            
        }
        if(corelsplash==null){
        java.awt.EventQueue.invokeLater(new Runnable(){
            public void run(){
                lsplash.setVisible(false);
                lsplash.dispose();
            }
        });
        }
    }
    
    
    
    private void importClistFromFile(final File f,String group) {
        try {
            String clname = f.getName().substring(0,f.getName().indexOf(".clist"));
            Hashtable<String, CandListHeader> groupTable = this.groups.get(group);
            if(groupTable == null){
                groupTable = new Hashtable<String, CandListHeader>();
                this.groups.put(group,groupTable);
            }
            CandListHeader cHead = groupTable.get(clname);
            if(cHead==null){
                //  System.out.println("Reading new header from file "+f.getName());
                CandList.IGNORE_ERRORS = true;
                try {
                    CandList tmp = new CandList(new BufferedReader(this.getReader(f)));
                    cHead = tmp.getHeader();
                    if(!cHead.getName().equals(clname)){
                        System.err.println("Renaming candlidate list "+cHead.getName()+" to match filename "+clname);
                        cHead.setName(clname);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
                CandList.IGNORE_ERRORS = false;
                groups.get(group).put(cHead.getName(),cHead);
                
                if(!this.coverage.containsKey(group)) this.coverage.put(group,new Hashtable<String,Coordinate[]>());
                if(!this.data.containsKey(group)) this.data.put(group,new Hashtable<String,File>());
                
                this.coverage.get(group).put(cHead.getName(),new Coordinate[]{cHead.getBeam().getCoord()});
                data.get(group).put(cHead.getName(),f);
            }
            
            
            
            
        } catch (Exception ee) {
            ee.printStackTrace();
            System.err.println();
            System.err.println();
            System.err.println();
            System.err.println("DEBUG: Error trying to read candidate list: "+f.getAbsolutePath());
            System.err.println("Check this file is really a candidate list");
            System.exit(99);
        }
    }
    
    public void clearAll(){
        data = new Hashtable<String,Hashtable<String,File>>();
        coverage = new Hashtable<String,Hashtable<String,Coordinate[]>>();
        groups = new Hashtable<String,Hashtable<String,CandListHeader>>();
        
    }
    
    
    
    
    public Reader getReader(File file) throws IOException{
        if(file.getName().endsWith(".clist.gz")) return new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
        else  return new FileReader(file);
    }
    
    public OutputStream getOutputStream(File file) throws IOException{
        if(file.getName().endsWith(".clist.gz")) return new GZIPOutputStream(new FileOutputStream(file));
        else return  new FileOutputStream(file);
    }
    
    public File getCandListFile(String name,String groupname){
        return data.get(groupname).get(name);
    }
    
    
    public List<String> getGroups(){
        return new ArrayList<String>(this.groups.keySet());
    }
    
    /**
     * Returns a {@link CandList} from the library with the name specified.
     * @param name The {@link CandList} to fetch
     * @return The coresponding {@link CandList} from the library.
     */
    public CandList getCandList(String name,String groupname ){
        
        File infile = data.get(groupname).get(name);
        
        if(infile == null) throw new RuntimeException("Cannot load Data "+name+", data file not in this data library");
        String clname = infile.getName().substring(0,infile.getName().indexOf(".clist"));
        try{
            
            BufferedReader instream = new BufferedReader(this.getReader(infile));
            CandList cList = new CandList(instream);
            cList.setDataLibrary(this);
            
            if(!cList.getHeader().getName().equals(clname)){
                System.err.println("Renaming candlidate list "+cList.getHeader().getName()+" to match filename "+clname);
                cList.getHeader().setName(clname);
            }
            
            // update our header cache...
            
            groups.get(groupname).remove(name);
            groups.get(groupname).put(name,cList.getHeader());
            
            instream.close();
            return cList;
        } catch (IOException e){
            //throw new RuntimeException("Cannot load Data "+name+", error reading data file.",e);
		System.err.println("WARNING: Could not load Candlist "+name+" ("+groupname+")");
		System.err.println("\terror was '"+e.getMessage()+"'");
		return null;
        }
    }
    
    
    
    /**
     * Returns a <CODE>List</CODE> the names of the candlists that are contained in this DataLibrary.
     * This will be filtered by the supplied string.
     * @param search A string to search for.
     * @return The resulting strings.
     */
    public List<Pair<String,String>> searchCandLists(String search){
        ArrayList<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
        
        for(String gname : groups.keySet()){
            for(String cname : groups.get(gname).keySet()){
                if(cname.contains(search)) result.add(new Pair<String,String>(gname,cname));
            }
        }
        
        return result;
    }
    
    /**
     * Returns the root path of the DataLibrary as a <CODE>File</CODE>
     * @return The root path of the DataLibrary
     */
    public File getRootPath(){
        return rootpath;
    }
    /**
     * Returns true if the file could be saved
     */
    public boolean saveCandList(CandList cList,String groupName){
        String name = cList.getHeader().getName();
        
        File file = data.get(groupName).get(name);
        if(file==null){
            System.err.println("Could not save candlist "+name+"... Nothing is known about it");
            return false;
        }
        // update our header cache...
        groups.get(groupName).remove(name);
        groups.get(groupName).put(name,cList.getHeader());
        try {
            // Save the file
            PrintStream outstream =  new PrintStream(this.getOutputStream(file));
            cList.write(outstream);
            outstream.close();
        } catch (IOException ex) {
            System.err.println("Could not save candlist "+name+"... An IO error occured");
            System.err.println("The error message was: "+ex.getLocalizedMessage());
            return false;
        }
        
        
        return true;
    }
    
    /**
     * Returns a custom {@link CandRefine} object that provides functionality to refine the candidates in this DataLibrary.
     * By default this is a standard CandRefine object.
     * @return A {@link CandRefine} Object that refines the data in this library.
     */
    public CandRefine getRefiner(){
        if(refine == null)refine=new CandRefine(this);
        return refine;
    }
    
    
    public List<Pair<String,String>> searchCandListsNear(String searchString,Coordinate coord,double distance){
        
        final LoadingSplash lsplash = new LoadingSplash();
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                lsplash.setText("Analysing Candidate Lists...");
                lsplash.setVisible(true);
            }
        });
        
        List<Pair<String,String>> names = this.searchCandLists(searchString);
        
        ArrayList<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
        CoordinateDistanceComparitor comp = new CoordinateDistanceComparitor();
        for(Pair<String,String> name : names){
            Coordinate[] thiscoverage = getCoverage().get(name.getA()).get(name.getB());
            if(thiscoverage == null) continue;
            for(Coordinate testcoord : thiscoverage){
                if(comp.difference(testcoord, coord) < distance){
                    result.add(name);
                    break;
                }
            }
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                lsplash.setVisible(false);
                lsplash.dispose();
            }
        });
        
        return result;
    }
    
    public Options getOptions() {
        return options;
    }
    
    public void setOptions(Options options) {
        this.options = options;
    }
    
    
    public ArrayList<KnownPSR> getKnownPulsars(){
        
        if(this.knownPSR == null){
            this.knownPSR = new ArrayList<KnownPSR>();
            File pulsarFile = new File(this.getOptions().getKnownPsrFile());
            
            if(pulsarFile.exists()){
                
                try{
                    BufferedReader reader = new BufferedReader(new FileReader(pulsarFile));
                    String line;
                    
                    String[] format = this.getOptions().getKnownPsrFormat().split("\\s+");
                    while((line = reader.readLine())!=null){
                        double period=0;
                        RA ra = new RA(0);
                        Dec dec = new Dec(0);
                        double dm = -1;
                        String[] data = line.split(" +");
                        String name1 = "";
                        if(data.length < format.length) continue;
                        for(int i = 0;i<format.length;i++){
                            if(format[i].equalsIgnoreCase("PERIOD_MS")){
                                period = Double.parseDouble(data[i]);
                            }
                            if(format[i].equalsIgnoreCase("PERIOD")){
                                period = 1000*Double.parseDouble(data[i]);
                            }else if(format[i].equalsIgnoreCase("RA")){
                                ra = ra.generateNew("J "+data[i]);
                            }else if(format[i].equalsIgnoreCase("DEC")){
                                dec = dec.generateNew("J "+data[i]);
                            }else if(format[i].equalsIgnoreCase("NAME")){
                                name1 = data[i];
                            } else if(format[i].equalsIgnoreCase("DM")){
                                dm = Double.parseDouble(data[i]);
                            }
                        }
                        final String name = name1;
                        
                        if(period == 0 || name.equals("") || ra == null || dec == null) continue;
                        
                        Coordinate coord = new Coordinate(ra,dec);
                        knownPSR.add(new KnownPSR(name,coord,period,dm));
                        
                    }
                }catch(IOException e){
                    
                }
            }
        }
        return this.knownPSR;
    }
    
    
    public void checkCandListForKnownPSRs(final CandList clist){
        
        final CandRefine cr = this.getRefiner();
        CoordinateDistanceComparitor cc = new CoordinateDistanceComparitor();
        for(KnownPSR kp : this.getKnownPulsars()){
            if(cc.difference(clist.getBeam().getCoord(),kp.getPosition())>this.getOptions().getDistmax())continue;
            cr.findHarmonics(clist.getCands(),kp.getPeriod(),kp.getName(),kp.getPosition(),0);
        }
        clist.getHeader().setKnownpsrSearched(true);
    }
    
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
    }
    
    
    
    
    
}
