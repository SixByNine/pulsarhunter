///*
//Copyright (C) 2005-2007 Michael Keith, University Of Manchester
// 
//email: mkeith@pulsarastronomy.net
//www  : www.pulsarastronomy.net/wiki/Software/PulsarHunter
// 
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
// 
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
// 
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// 
// */
///*
// * Main.java
// *
// * Created on 24 May 2005, 14:53
// */
//
package pulsarhunter.jreaper;
//
//import coordlib.Coordinate;
//import coordlib.CoordinateDistanceComparitor;
//import coordlib.Dec;
//import coordlib.RA;
//import java.awt.Color;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import pulsarhunter.jreaper.gui.MessageBox;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Hashtable;
//import java.util.List;
//import javax.swing.JPanel;
//import pulsarhunter.jreaper.gui.Colourmap;
//import pulsarhunter.jreaper.gui.DataLoadFrame;
//import pulsarhunter.jreaper.gui.InitialFrame;
//import pulsarhunter.jreaper.gui.LoadingSplash;
//import pulsarhunter.jreaper.gui.MainView;
//import pulsarhunter.jreaper.gui.MessageBox;
//import pulsarhunter.jreaper.peckscorer.Peck;
//import pulsarhunter.jreaper.pmsurv.PMSurv;
//import pulsarhunter.jreaper.tune.Tune;
//
///**
// * This is the main class for the PulsarSweep/JReaper software. Only one instance of this should be run per VM.
// * @author mkeith
// */
public class Main{
//    
//    public static boolean nogui = false;
//    
//    /**
//     * True if this is a test build
//     */
//    public static final boolean TEST = false;
//    /*
//     * The Major version number of this build
//     */
//    public static final int MAJVERSION = 3;
//    /**
//     * The minor version number of this build
//     */
//    public static final int MINORVERSION = 1;
//    /**
//     * The build number of this build
//     */
//    public static final int BUILD = 11;
//    /**
//     * A version string of the form [Major Version].[Minor Version]:[Build Number]
//     */
//    public static final String VERSION = ""+MAJVERSION+"."+MINORVERSION+":"+BUILD;
//    /**
//     * The date that this version was built
//     */
//    public static final String DATE = "13-06-2007";
//    
//    public static final String WINDOWTITLE = "JReaper "+MAJVERSION+"."+MINORVERSION;
//    
//    private static final String OPTIONFILENAME = "jreaper.conf";
//    private static final String HISTORYFILENAME = "jreaper.hist";
//    
//    private final Plugin[] plugins = new Plugin[]{new PMSurv(), new Peck(), new Tune()};
//    
//    
//    private double eta = 0.001;
//    private double distmax = 0.5; //degrees
//    
//    private int historyLength = 10;
//    private String[] history;
//    private DataLibrary dataLibrary;
//    private File dataLibFile;
//    private Hashtable<String,CandidateReader> candReaders = new Hashtable<String,CandidateReader>();
//    private Hashtable<String,ArchiveReader> archReaders = new Hashtable<String,ArchiveReader>();
//    private ArrayList<DataLibraryType> dataLibraryTypes = new ArrayList<DataLibraryType>();
//    private DataLoadFrame dFrame;
//    private MainView mFrame;
//    private ArrayList<CandList> loadedData = new ArrayList<CandList>();
//    private Cand[][] masterData = new Cand[5][];
//    private ArrayList<KnownPSR> knownPSR = new ArrayList<KnownPSR>();
//    private CandScorer defaultCandScorer = null;
//    private Hashtable<String,CandScorer> candScorers = new Hashtable<String,CandScorer>();
//    String knownPsrFile = "./knownpulsars.list";
//    String knownPsrFormat = "NAME PERIOD X RA X DEC X";
//    private boolean useScratch = false;
//    private File scratch;
//    private boolean readonly = false; // Is the current datalibrary in read only mode?
//    private File lockfile = null;
//    private boolean forceKnownPSRs = false;
//    /**
//     * This is the loading splash screen. This is a public variable to allow other classes access to the spash screen, and to keep only one instance of the splash at any time.
//     */
//    public LoadingSplash lsplash;// = new LoadingSplash();
//    
//    public enum Verbosity {
//        Quiet(0),Default(10),Verbose(20),UltraVerbose(30);
//        private int rank;
//        Verbosity(int rank){
//            this.rank = rank;
//        }
//    };
//    private Verbosity verbosity;
//    
//    private boolean debug = false;
//    /**
//     * Creates a new insatnce of PulsarSweep/JReaper.
//     *
//     * This starts the plugin manager and loads the options file, before starting the load data screen.
//     */
//    public Main() {
//        
//    }
//    
//    
//    
//    public void init(){
//        Thread.setDefaultUncaughtExceptionHandler(this);
//        this.addDataLibraryType(new DefaultDataLibraryType());
//        this.addDataLibraryType(new DatabaseDataLibraryType());
//        
//        
//        for(Plugin p : plugins){
//            p.init();
//        }
//        
//        if(!nogui){
//            lsplash = new LoadingSplash();
//            new InitialFrame().setVisible(true);
//        }
//        if(this.isDebug()){
//            System.out.println();
//            System.out.println("======================");
//            System.out.println("= DEBUG MODE ENABLED =");
//            System.out.println("======================");
//            System.out.println();
//        }
//    }
//    
    
//    
//    
//    /**
//     * Returns the file containing the list of known pulsars. This is used to mark known pulsars and harmonics when loading data.
//     * @return A string caontaining the path to the known pulsar file.
//     * @see pulsarsweep.Main#getKnownPulsarFormat
//     * @see pulsarsweep.Main#setKnownPulsarFile
//     */
//    public String getKnownPulsarFile(){
//        return knownPsrFile;
//    }
//    
//    /**
//     * Returns a string detailing the format of the pulsar file.
//     * Currently accepted fields are:
//     * <ul>
//     * <li>PERIOD  -  The period of the pulsar</li>
//     * <li>NAME - The pulsar name</li>
//     * <li>RA - The Right Assension of the pulsar</li>
//     * <li>DEC - The Declination of the pulsar</li>
//     * <li>X - A field to be discarded</li>
//     * </ul>
//     *
//     * e.g. NAME PERIOD X RA X DEC X
//     * @return The formating string as detailed above
//     * @see pulsarsweep.Main#setKnownPulsarFile
//     */
//    public String getKnownPulsarFormat(){
//        return knownPsrFormat;
//    }
//    
//    /**
//     * Sets the file for which the list of known pulsars is read.
//     * The format of the list must be specified using space delimited format string.
//     * Currently accepted fields are:
//     *  <ul>
//     * <li>PERIOD  -  The period of the pulsar</li>
//     * <li>NAME - The pulsar name</li>
//     * <li>RA - The Right Assension of the pulsar</li>
//     * <li>DEC - The Declination of the pulsar</li>
//     * <li>X - A field to be discarded</li>
//     * </ul>
//     *
//     * e.g. NAME PERIOD X RA X DEC X
//     * @param filename A string containing the path to the list of known pulsars
//     * @param format The format of the known pulsar list as above
//     * @see pulsarsweep.Main#getKnownPulsarFormat
//     */
//    public void setKnownPulsarFile(String filename, String format){
//        this.knownPsrFile = filename;
//        this.knownPsrFormat = format;
//    }
//    
//    
//    
//    
//    /**
//     * Loads the datalibrary history from the default file
//     * @throws java.io.IOException Throws an IOException if there is an error loading the file
//     */
//    public void loadHistory() throws IOException{
//        history = new String[historyLength];
//        try{
//            BufferedReader reader = new BufferedReader(new FileReader(new File(HISTORYFILENAME)));
//            
//            String s = reader.readLine();
//            for(int i = 0;i<history.length;i++){
//                if(s == null) break;
//                history[i] = s;
//                s = reader.readLine();
//            }
//            reader.close();
//        } catch(FileNotFoundException e){
//            saveHistory();
//        }
//    }
//    /**
//     * Writes out the datalibrary history to a file
//     * @throws java.io.IOException Throws an IOExcpeption if there is an error writing to the file
//     */
//    public void saveHistory()  throws IOException{
//        PrintWriter out = new PrintWriter(new FileOutputStream(new File(HISTORYFILENAME)));
//        for(String s : history){
//            out.println(s);
//        }
//        out.close();
//    }
//    
//    /**
//     * Adds a datalibrary to the history. Will replace if already in the history.
//     * @param newElem The datalibrary to add
//     */
//    public void addToHistory(String newElem){
//        int start =  history.length -1;
//        for(int i = 0;i < history.length; i++){
//            if(history[i].equals(newElem)){
//                start = i;
//                break;
//            }
//        }
//        
//        for(int i = start ;i > 0 ; i--){
//            history[i] = history[i-1];
//            
//        }
//        history[0]=newElem;
//        try{
//            saveHistory();
//        } catch (IOException e){
//            this.log(e);
//        }
//    }
//    
//    /**
//     * Returns the history of the past loaded datalibraries as an array of strings
//     * @return The history array.
//     */
//    public String[] getHistory(){
//        try{
//            loadHistory();
//        } catch (IOException e){
//            this.log(e);
//            return new String[0];
//        }
//        for(int i = 0;i < history.length; i++){
//            if(history[i]==null){
//                history[i] = "";
//            }
//        }
//        return history;
//    }
//    
//    public void addDataLibraryType(DataLibraryType t){
//        dataLibraryTypes.add(t);
//    }
//    
//    public DataLibraryType[] getDataLibraryTypes(){
//        DataLibraryType[] arr = new DataLibraryType[dataLibraryTypes.size()];
//        return dataLibraryTypes.toArray(arr);
//    }
//    
//    /**
//     * Adds a {@link CandidateReader} to be used to read candidates with the given extension.
//     * @param cReader The {@link CandidateReader} to be added.
//     * @param extention The file extension that this candidate reader is to be used for.
//     */
//    public void addCandReader(CandidateReader cReader,String extention){
//        candReaders.put(extention, cReader);
//    }
//    
//    public void addArchiveReader(ArchiveReader cReader,String extention){
//        archReaders.put(extention, cReader);
//    }
//    
//    /**
//     *Adds a {@link CandScorer} that can be used to score candidates in the system. Use the setDefaultCandScorer to specify which scorer to use for default scoring
//     *@param scorer the {@link CandScorer} to be added
//     *@see pulsarsweep.Main.setDefaultCandScorer
//     */
//    public void addCandScorer(CandScorer scorer){
//        candScorers.put(scorer.getName(),scorer);
//    }
//    
//    
//    /**
//     * Returns the Default {@link CandScorer}
//     * @return
//     */
//    public CandScorer getDefaultCandScorer(){
//        return this.defaultCandScorer;
//    }
//    
//    public CandScorer[] getCandScoreres(){
//        CandScorer[] out = new CandScorer[candScorers.size()];
//        candScorers.values().toArray(out);
//        return out;
//    }
//    
//    public CandScorer getCandScorerFromName(String name){
//        return candScorers.get(name);
//    }
//    public void setDefaultCandScorer(CandScorer scorer){
//        this.defaultCandScorer = scorer;
//    }
//    
//    
//    /**
//     * Starts up the Data Load frame, allowing the user to choose which data to view.
//     */
//    public void selectNewData(){
//        dFrame = new DataLoadFrame();
//        if(dataLibrary != null){
//            dFrame.setDataLoadPanel(dataLibrary.getDataLoaderPanel());
//            dFrame.setImportPanel(dataLibrary.getImportPanel());
//        }
//        
//        // Check for new candlists etc...
////        if(dataLibrary!=null){
////            dataLibrary.reloadCandListList();
////        }
//        dFrame.setVisible(true);
//        
//        
//    }
//    
//    /**
//     * This changes the {@link DataLibrary} for the current session. The data library specified will be de-serialised and loaded.
//     * @param name The path to the serialised data library file
//     * @return <CODE>true</CODE> if the data library was loaded sucsessfully, <CODE>false</CODE> otherwise.
//     */
//    public boolean setDataLibrary(String name){
//        this.closeDataLibrary();
//        File file = null;
//        try{
//            BufferedReader instream  =null;
//            if(name.startsWith("http://") || name.startsWith("ftp://") || name.startsWith("https://")){
//                try{
//                    URL url = new URL(name);
//                    instream = new BufferedReader(new InputStreamReader((url.openStream())));
//                    file = new File(url.getFile());
//                    readonly = true;
//                }catch (MalformedURLException e){
//                    this.log(e);
//                    return false;
//                }
//            } else {
//                file = new File(name);
//                dataLibFile = file;
//                lockDataLibrary();
//                if(readonly){
//                    new MessageBox("Note: This datalibrary appears to be in use, loading read only.\nUse the file menu for force exiting read only mode.").setVisible(true);
//                }
//                
//                instream = new BufferedReader(new FileReader(file));
//            }
//            try {
//                
//                this.dataLibrary = DataLibraryLoader.load(instream);
//            } catch (IOException ex) {
//                this.log("Failure to read DL... Perhaps this is an old Data Library");
//                new DataLibraryUpdater(file);
//                return false;
//            }
//            
//            dataLibFile = file;
//            instream.close();
//        } catch(IOException e){
//            this.log(e);
//            new MessageBox("Cannot connect to specified Data Library").setVisible(true);
//            return false;
//        }
//        if(dFrame != null){
//            dFrame.setDataLoadPanel(dataLibrary.getDataLoaderPanel());
//            dFrame.setImportPanel(dataLibrary.getImportPanel());
//        }
//        new MessageBox("Connected to "+name+"\n"+dataLibrary.getDataList("").size()+" Data Sets Avaliable").setVisible(true);
//        return true;
//    }
//    
//    /**
//     * This returns the data load panel for the current {@link DataLibrary }.
//     * Effectively calles getDataLoadPanel() on the {@link DataLibrary }
//     * @return The current {@link DataLibrary }'s data load panel.
//     */
//    public JPanel getDataLoadPanel(){
//        return dataLibrary.getDataLoaderPanel();
//    }
//    
//    /**
//     * This returns the import  panel for the current {@link DataLibrary }.
//     * Effectively calles getImportPanel() on the {@link DataLibrary }
//     * @return The current import panel to be used for the current DataLibrary
//     */
//    public JPanel getImportPanel(){
//        return dataLibrary.getImportPanel();
//    }
//    
//    /**
//     * This is no longer used as datalibraries can be used by many users
//     * because the candlists are now dynamicaly loaded.
//     */
//    private void lockDataLibrary(){
//        readonly = false;
//        
//        
//       /* lockfile = new File(dataLibFile.getAbsolutePath()+".lock");
//        if(lockfile.exists()) {
//            readonly = true;
//        } else {
//            readonly = false;
//            try{
//                lockfile.createNewFile();
//            } catch (IOException e){
//                readonly = true;
//            }
//        
//        }*/
//    }
//    
//    private void closeDataLibrary(){
//        this.saveProgress();
//        if(dataLibrary !=null){
//            
//            
//        }
//        dataLibrary = null;
//        dataLibFile = null;
//        
//        
//        this.loadedData = new ArrayList<CandList>();
//        
//        
//    }
//    
//    
//    /**
//     * Creates a new empty {@link DataLibrary}. This method is called by the default data library constructor.
//     * @param rootFolder The root folder that this {@link DataLibrary } will store its candidate lists.
//     * @param file The file to store the data library serialised form.
//     */
//    public void newDataLibrary(String rootFolder,String filename){
//        File file = new File(filename);
//        closeDataLibrary();
//        File root = new File(rootFolder);
//        
//        if(!root.isDirectory()) new MessageBox("Root Dir Must be valid directory").setVisible(true);
//        dataLibrary = new DataLibrary(root);
//        dataLibFile = file;
//        lockDataLibrary();
//        dataLibrary.writeDataLibray(file);
//        this.addToHistory(filename);
//        this.dFrame.updateHistoryList();
//        
//    }
//    public void newCustomDataLibrary(String name){
//        this.addToHistory(name);
//        this.dFrame.updateHistoryList();
//    }
//    
//    
//    
//    /**
//     * Adds candidates ot the current data library, runs in a seperate thread, so safe to call from the UI.
//     * This cales the importCands() method on the current {@link DataLibrary}, followed by writing the current data library.
//     * @param name The name of the new candidate list
//     * @param dir The directory to read candidate files from
//     * @param subDirs Specifies whether to load subdirectories.
//     */
//    public void addCandList(final String name,final File dir, final boolean subDirs){
//        
//        Thread task = new Thread(){
//            public void run(){
//                dataLibrary.importCands(dir, subDirs, name);
//                saveDataLibrary();
//                new MessageBox("Finished Loading Candidates...").setVisible(true);
//            }
//        };
//        task.start();
//    }
//    
//    public void saveDataLibrary(){
//        if(dataLibrary != null){
//            if(readonly){
//                this.log("Read only mode enabled. Not saving datalibrary");
//            }else{
//                this.log("Saving DataLibrary...",Main.Verbosity.Default);
//                dataLibrary.writeDataLibray(dataLibFile);
//            }
//        }
//    }
//    
//    
//    
//    /**
//     * Allows direct access to the current {@link DataLibrary}
//     * @return The current {@link DataLibrary}
//     */
//    public DataLibrary getDataLibrary(){
//        return dataLibrary;
//    }
//    
//    /**
//     * Informs the main system that data loading has fininshed, and to display the main window.
//     * This causes the system to call the webSync method if the loaded data library implements {@link WebDataLibrary},
//     * and to hunt for any known pulsars in the data.
//     */
//    public void dataLoadFinished(){
//        if(dataLibrary == null) this.close();
//        Thread task = new Thread(){
//            public void run(){
//                
//                //masterData = new Cand[5][0];
//                int lengths[] = new int[5];
//                boolean doKnownPSRs = forceKnownPSRs;
//                for(CandList c : loadedData){
//                    Cand[][] data = c.getCands();
//                    if(!c.getHeader().isKnownpsrSearched()){
//                        doKnownPSRs = true;
//                    }
//                    for(int i = 0;i<5;i++){
//                        lengths[i] += data[i].length;
//                    }
//                }
//                
//                if(Main.getInstance().isDebug()){
//                    for(int i = 0; i < 5; i++){
//                        Main.getInstance().log("Counted "+lengths[i]+" type "+i+" candidates",Verbosity.Verbose);
//                    }
//                    
//                    
//                    Main.getInstance().log("Initialising master candidate arrays",Verbosity.Verbose);
//                    Main.getInstance().log("MemFree:"+Runtime.getRuntime().freeMemory(),Verbosity.Verbose);
//                    
//                }
//                
//                for(int i = 0;i<5;i++){
//                    masterData[i] = new Cand[lengths[i]];
//                }
//                int off[] = {0,0,0,0,0};
//                if(Main.getInstance().isDebug()){
//                    Main.getInstance().log("Copying candidates into master arrays",Verbosity.Verbose);
//                    Main.getInstance().log("MemFree:"+Runtime.getRuntime().freeMemory(),Verbosity.Verbose);
//                }
//                for(CandList c : loadedData){
//                    Cand[][] data = c.getCands();
//                    for(int i = 0;i<5;i++){
//                        if(data[i].length > 0)
//                            System.arraycopy(data[i], 0, masterData[i], off[i], data[i].length);
//                        off[i] +=data[i].length;
//                    }
////                    c.release();
//                }
///*                if(Main.getInstance().isDebug()){
//                    Main.getInstance().log("Unloading candidate lists to save memory...",Verbosity.Verbose);
//                    Main.getInstance().log("Pre-MemFree:"+Runtime.getRuntime().freeMemory(),Verbosity.Verbose);
//                }     */
////                loadedData = null;
////                loadedData = new ArrayList<CandList>();
///*                if(Main.getInstance().isDebug()){
//                    
//                    Main.getInstance().log("Post-MemFree:"+Runtime.getRuntime().freeMemory(),Verbosity.Verbose);
//                }*/
//                if(dataLibrary instanceof WebDataLibrary){
//                    if(Main.getInstance().isDebug()){
//                        Main.getInstance().log("Performing a web sync",Verbosity.Verbose);
//                    }
//                    ((WebDataLibrary)dataLibrary).webSync(masterData);
//                }
//                double odm = Main.this.getDistmax();
//                Main.this.setDistmax(10);
//                if(Main.getInstance().isDebug()){
//                    Main.getInstance().log("Searching for Vela...",Verbosity.Verbose);
//                }
//                java.awt.EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        Main.this.lsplash.setVisible(true);
//                        Main.this.lsplash.setText("Engaging VelaKiller (tm)...");
//                        Main.this.lsplash.repaint();
//                    }
//                });
//                dataLibrary.getRefiner().findHarmonics(masterData,89.328385,"Vela",new Coordinate(new RA(8,35,20.6),new Dec(-45,10,34.8)),0);   // VELA killer!
//                if(Main.getInstance().isDebug()){
//                    Main.getInstance().log("Looking for known pulsars",Verbosity.Verbose);
//                }
//                Main.this.setDistmax(odm);
//                File pulsarFile = new File(knownPsrFile);
//                if(doKnownPSRs && pulsarFile.exists()){
//                    if(Main.getInstance().isDebug()){
//                        Main.getInstance().log("Readingk known pulsar file "+knownPsrFile,Verbosity.Verbose);
//                    }
//                    try{
//                        BufferedReader reader = new BufferedReader(new FileReader(pulsarFile));
//                        String line;
//                        
//                        String[] format = Main.this.knownPsrFormat.split("\\s+");
//                        while((line = reader.readLine())!=null){
//                            double period=0;
//                            RA ra = new RA(0);
//                            Dec dec = new Dec(0);
//                            String[] data = line.split(" +");
//                            String name1 = "";
//                            if(data.length < format.length) continue;
//                            for(int i = 0;i<format.length;i++){
//                                if(format[i].equalsIgnoreCase("PERIOD_MS")){
//                                    period = Double.parseDouble(data[i]);
//                                }
//                                if(format[i].equalsIgnoreCase("PERIOD")){
//                                    period = 1000*Double.parseDouble(data[i]);
//                                }else if(format[i].equalsIgnoreCase("RA")){
//                                    ra = ra.generateNew("J "+data[i]);
//                                }else if(format[i].equalsIgnoreCase("DEC")){
//                                    dec = dec.generateNew("J "+data[i]);
//                                }else if(format[i].equalsIgnoreCase("NAME")){
//                                    name1 = data[i];
//                                }
//                            }
//                            final String name = name1;
//                            //System.out.println(name+ " "+ra.toString() + " "+dec.toString());
//                            if(period == 0 || name.equals("") || ra == null || dec == null) continue;
//                            Coordinate coord = new Coordinate(ra,dec);
//                            if(!inBeams(coord)) continue;
//                            knownPSR.add(new KnownPSR(name,coord,period,0.0));
//                            
//                            java.awt.EventQueue.invokeLater(new Runnable() {
//                                public void run() {
//                                    Main.this.lsplash.setVisible(true);
//                                    Main.this.lsplash.setText("Searching for Hamonics of "+name);
//                                    Main.this.lsplash.repaint();
//                                }
//                            });
//                            if(Main.getInstance().isDebug()){
//                                Main.getInstance().log("Finding known PSR harmonics",Verbosity.Verbose);
//                            }
//                            dataLibrary.getRefiner().findHarmonics(masterData,period,name,coord,0);
//                            
//                            
//                        }
//                    }catch(IOException e){
//                        
//                    }
//                }
//                
//                java.awt.EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        Main.this.lsplash.setVisible(false);
//                    }
//                });
//                if(Main.getInstance().isDebug()){
//                    Main.getInstance().log("Initialising the MainView...",Verbosity.Verbose);
//                }
//                java.awt.EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        new MainView(masterData).setVisible(true);
//                    }
//                });
//            }
//        };
//        task.start();
//        
//    }
//    
//    /**
//     * Returns the master candidate list currently loaded. This is a 2-D array containing all the loaded candidates.
//     * The first index is the data search type, e.g. 0 - standard search, 1 - accelerated search, 2 - long period search.
//     * @return The master data array as specified above.
//     */
//    public Cand[][] getMasterData(){
//        return masterData;
//    }
//    
//    /**
//     * Determines whether the specified coordinate is near coordinates of data currently loaded.
//     * @param coord The coordinate to look for
//     * @return <CODE>true</CODE> if the coordinate is in the currently loaded data, <CODE>false</CODE> otherwise.
//     */
//    public boolean inBeams(Coordinate coord){
//        double eta = 3;
//        CoordinateDistanceComparitor comp = new CoordinateDistanceComparitor();
//        for(CandList c : loadedData){
//            for(Coordinate beam : c.coverage()){
//                if(comp.difference(beam,coord) < eta) return true;
//            }
//        }
//        return false;
//    }
//    
//    /**
//     * Causes the system to shutdown.
//     */
//    public void close(){
//        this.masterData = null;
//        if(this.dataLibrary!=null){
//            this.saveProgress();
//            this.loadedData = new ArrayList<CandList>();
//        }
//        this.closeDataLibrary();
//        this.log("Exiting.",Main.Verbosity.Default);
//        System.exit(0);
//    }
//    
//    public void saveProgress(){
//        if(this.getLoadedCandLists().size() > 0 && this.dataLibrary != null){
//            this.log("Saving Candidate Lists...",Main.Verbosity.Default);
//            
//            this.lsplash.setText("Saving status");
//            this.lsplash.setVisible(true);
//            
//            for(CandList cList : this.getLoadedCandLists()){
//                
//                this.lsplash.setText("Saving Candidate List "+cList.getName());
//                
//                cList.getDataLibrary().addCandList(cList);
//            }
//        }
//        
//        if(this.dataLibrary != null){
//            this.lsplash.setText("Saving Data Library ");
//            this.saveDataLibrary();
//            this.lsplash.setVisible(false);
//        }
//    }
//    
//    public List<KnownPSR> getKnownPulsars(){
//        return this.knownPSR;
//    }
//    
//    /**
//     * Adds the specified string to the list of candidate files to be loaded. When the system has finished specifying loaded data, the specified data will be loaded.
//     * @param name The name of the candidate list to be loaded. This must be one of the candidate lists in the current {@link DataLibrary}
//     */
//    public void loadCandList(String name){
//        
//        CandList clist = null;
//        try{
//            clist = dataLibrary.getCandList(name);
//        }catch (RuntimeException e){
//            new MessageBox(e.getMessage()).setVisible(true);
//            log(e);
//            return;
//        }
//        if(!loadedData.contains(clist)){
//            loadedData.add(clist);
//        }
//    }
//    
//    
//    public void unloadCandList(CandList clist){
//        
//        //if(loadedData.contains(clist)){
//        if(clist != null){
//            this.dataLibrary.addCandList(clist);
//            loadedData.remove(clist);
//        }
//        //}
//    }
//    
//    /**
//     * Returns a list of {@link CandList}s representing the candidate lists that are specified to be loaded.
//     * @return A <CODE>List</CODE> of {@link CandList}s that will be loaded.
//     */
//    public List<CandList> getLoadedCandLists(){
//        return loadedData;
//    }
//    
//    /**
//     * Returns a List of all the {@link CandList}s that are avaliable in this {@link DataLibrary}
//     * Specify a non empty string to refine the list by those containing the string in their name.
//     * @param search The string to search by.
//     * @return The List of {@link CandList}s.
//     */
//    public List<String> getCandLists(String search){
//        if(dataLibrary==null) return new ArrayList<String>();
//        try{
//            return dataLibrary.getDataList(search);
//        }catch(RuntimeException e){
//            new MessageBox(e.getMessage()).setVisible(true);
//            return new ArrayList<String>();
//        }
//    }
//    
//    /**
//     * Returns the name of the currently loaded {@link DataLibrary}
//     * @return The name of the current datalibrary.
//     */
//    public String getCurrentLib(){
//        if(dataLibFile==null) return "None";
//        return dataLibFile.getName();
//    }
//    
//    /**
//     * Returns the candidate reader for the specified extension.
//     * @param fileExtention The extension to get the candidate reader for.
//     * @return The {@link CandidateReader} specified to handle the extention specified, or null if none found.
//     */
//    public CandidateReader getCandReader(String fileExtention){
//        return candReaders.get(fileExtention);
//    }
//    
//    public ArchiveReader getArchiveReader(String fileExtention){
//        return archReaders.get(fileExtention);
//    }
//    
//    /**
//     * Calls the FindHarmonics() method on the current {@link DataLibrary}.
//     * Also will find any candidates with the specified period in the specified sky region, unless they have already been clasified appropriately.
//     * @param masterData The data to search
//     * @param period The period to look for harmonics of
//     * @param name The name of the target, for use in commenting harmonics.
//     * @param coord The sky region to search around
//     * @param candClass The class that found harmonics should be given. Must be -1,0,1,2,3.
//     */
//    public  void findHarmonics(Cand[][] masterData, double period,String name,Coordinate coord,int candClass){
//        dataLibrary.getRefiner().findHarmonics(masterData,period,name,coord,candClass);
//    }
//    
//    
//    public void log(String message, Verbosity verb){
//        if(verb.rank <= this.getVerbosity().rank){
//            this.log(message);
//        }
//    }
//    
//    /**
//     * Appends a string to the log.
//     * @param message The message to append to the log
//     */
//    public void log(String message){
//        if(this.getVerbosity()==Verbosity.Quiet) return;
//        else System.out.println(message);
//    }
//    
//    /**
//     * Appends an Exception to the log.
//     * @param exception Writes a stack trace to the log.
//     */
//    public void log(Throwable exception){
//        
//        log(exception.toString());
//        StackTraceElement[] stackTrace = exception.getStackTrace();
//        for(StackTraceElement e : stackTrace){
//            log(e.toString());
//        }
//    }
//    
//    private static Main main;
//    public static Main getInstance(){
//        if(main == null) main = new Main();
//        return main;
//    }
//    
//    
//    private static String helpText = "Jreaper: Version "+VERSION+"\n" +
//            "Command line options:\n" +
//            "--verbose (-v) Print fairly verbose logs\n" +
//            "--veryverbose (-V) Print max verbose logs\n" +
//            "--quiet (-q) do not print any logs\n";
//    
//    /**
//     * Starts the Reaper
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        
//        Verbosity verb = Verbosity.Default;
//        boolean useScratch = false;
//        File scratchFile = null;
//        boolean debug = false;
//        if(TEST){
//            System.out.println("TEST VERSION - v"+VERSION);
//            System.out.println("Created on "+DATE);
//            System.out.println("Test mode enabled: Setting verbosity to max: ");
//            verb = Verbosity.UltraVerbose;
//        } else {
//            
//            System.out.println("Starting the REAPER: v"+VERSION);
//            
//        }
//        
//        for(int i = 0; i < args.length; i++){
//            if(args[i].trim().length() < 1)continue;
//            if(args[i].contains("help")){
//                System.out.println(helpText);
//                System.exit(0);
//            }
//            if(args[i].startsWith("--")){
//                String command = args[i].substring(2);
//                if(command.equalsIgnoreCase("scratch")){
//                    if(args.length > i){
//                        scratchFile = new File(args[++i]);
//                        useScratch = true;
//                    }
//                    continue;
//                }
//                if(command.equalsIgnoreCase("verbose")){
//                    verb = Verbosity.Verbose;
//                    continue;
//                }
//                if(command.equalsIgnoreCase("veryverbose")){
//                    verb = Verbosity.UltraVerbose;
//                    continue;
//                }
//                if(command.equalsIgnoreCase("quiet")){
//                    verb = Verbosity.Quiet;
//                    continue;
//                }
//                if(command.equalsIgnoreCase("debug")){
//                    verb = Verbosity.UltraVerbose;
//                    debug = true;
//                    continue;
//                }
//            } else if(args[i].startsWith("-")){
//                String commands = args[i].substring(1);
//                for(char c : commands.toCharArray()){
//                    if(c=='v'){
//                        verb = Verbosity.Verbose;
//                        continue;
//                    }
//                    if(c=='V'){
//                        verb = Verbosity.UltraVerbose;
//                        continue;
//                    }
//                    if(c=='q'){
//                        verb = Verbosity.Quiet;
//                        continue;
//                    }
//                }
//            } else{
//                
//                System.out.println("Unknown Argument "+args[i]+" passed on command line...");
//            }
//            
//        }
//        
//        
//        Main.getInstance().setVerbosity(verb);
//        Main.getInstance().setDebug(debug);
//        if(useScratch) Main.getInstance().setScratch(scratchFile);
//        Main.getInstance().init();
//        
//        //masterData = new Cand[0][0];
//        //MainView mainview = new MainView(Main.masterData);
//        //mainview.setVisible(true);
//        //masterData = main.getAllFromDir(new File("C:\\Documents and Settings\\mkeith\\java\\PulsarSweep\\"),true);
//        //masterData = main.getAllFromDir(new File(args[0]),true);
//        
//        
//        //mainview.setVisible(false);
//        //mainview.dispose();
//        ///mainview = null;
//        //CandRefine.findHarmonics(masterData, 510.1288706, "That Pulsar wot is there",new Coordinate(new RA(7,30,18.0),new Dec(-18,39,8)),0);
//        
//        /*java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new MainView(Main.masterData).setVisible(true);
//            }
//        });*/
//    }
//    
//    public File getDataLibFile() {
//        return dataLibFile;
//    }
//    
//    public void setDataLibFile(File dataLibFile) {
//        this.dataLibFile = dataLibFile;
//    }
//    
//    public double getEta() {
//        return eta;
//    }
//    
//    public void setEta(double eta) {
//        this.eta = eta;
//    }
//    
//    public double getDistmax() {
//        return distmax;
//    }
//    
//    public void setDistmax(double distmax) {
//        this.distmax = distmax;
//    }
//    
//    public Verbosity getVerbosity() {
//        return verbosity;
//    }
//    
//    public void setVerbosity(Verbosity verbosity) {
//        this.verbosity = verbosity;
//    }
//    
//    public boolean isUseScratch() {
//        return useScratch;
//    }
//    
//    public void setUseScratch(boolean useScratch) {
//        this.useScratch = useScratch;
//    }
//    
//    public File getScratch() {
//        return scratch;
//    }
//    
//    public void setScratch(File scratch) {
//        this.setUseScratch(true);
//        this.scratch = scratch;
//    }
//    
//    public boolean isReadonly() {
//        return readonly;
//    }
//    
//    public void setReadonly(boolean readonly) {
//        this.readonly = readonly;
//        if(readonly == false){
//            lockfile.delete();
//            this.lockDataLibrary();
//        }
//    }
//    
//    public Plugin[] getPlugins() {
//        return plugins;
//    }
//    
//    public boolean isDebug() {
//        return debug;
//    }
//    
//    public void setDebug(boolean debug) {
//        this.debug = debug;
//    }
//    
//    public boolean getForceKnownPSRs() {
//        return forceKnownPSRs;
//    }
//    
//    public void setForceKnownPSRs(boolean forceKnownPSRs) {
//        this.forceKnownPSRs = forceKnownPSRs;
//    }
//    
//    
//    
//    
//    
//    
//    
//    
}
