/*
 * JReaper.java
 *
 * Created on 10 October 2007, 10:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.jreaper;

import coordlib.CoordinateDistanceComparitor;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import pulsarhunter.Pair;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.jreaper.gui.InitialFrame;
import pulsarhunter.jreaper.gui.LoadingSplash;
import pulsarhunter.jreaper.gui.MainView;
import pulsarhunter.jreaper.gui.MessageBox;
import pulsarhunter.jreaper.pmsurv.PulsarCandFile;

/**
 *
 * @author Mike Keith
 */
public class JReaper {

    private static boolean sod_the_db_sync = false;
    private static String external_cand_file = null;
    private static String wd = ".";
    private static boolean noGuiTestMode = false;
    public final static String VERSION = "4.0 (beta 7)";
    public final static String WINDOWTITLE = "JReaper v" + VERSION;
    private static final String HISTORYFILENAME = "jreaper.hist";
    public static final String WELCOME_MESSAGE = "Welcome to JReaper v" + VERSION + "\n" +
            "To continue, please load an existing Data Library or create a new one.\n" +
            "The Data Library will store references to all your candidates, so you can quickly load them.";
    private String[] history = null;
    private DataLibrary currentDataLibrary = null;
    private File currentDataLibraryFile = null;
    private JFrame currentWindow = null;
    private ArrayList<DataLibraryType> dataLibraryTypes = new ArrayList<DataLibraryType>();
    private ArrayList<Pair<String, CandList>> loadedCandLists = new ArrayList<Pair<String, CandList>>();
    private PreCacheThread precacheThread;

    /** Creates a new instance of JReaper */
    public JReaper() {
        this.addDataLibraryType(new DefaultDataLibraryType(this));
        this.addDataLibraryType(new DatabaseDataLibraryType(this));
    }

    /*
     * Static methods
     *
     */
    public static boolean isNoGuiTestMode() {
        return noGuiTestMode;
    }

    public static void setNoGuiTestMode(boolean aNoGuiTestMode) {
        noGuiTestMode = aNoGuiTestMode;
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("--sod-the-db-sync")) {
                    sod_the_db_sync = true;
                }
                if (args[i].equalsIgnoreCase("--wd")) {
                    wd=args[++i];
                }
                if (args[i].equalsIgnoreCase("--extpsrs")) {
                    external_cand_file = new File(wd+File.separatorChar+args[++i]).getAbsolutePath();
                }
            }
        }
        JReaper jreaper = new JReaper();
        // read the args!


        jreaper.init();
    }

    /*
     * Class Methods
     *
     */
    /*
     * Perform Actions...
     *
     */
    /*
     * Start JReaper!
     */
    public void init() {
        this.closeDataLibrary();
        closeWindow();

        this.currentWindow = new InitialFrame(this);
        this.currentWindow.setVisible(true);
    }

    /**
     * Save and Exit JReaper!
     *
     */
    public void close() {
        if (precacheThread != null) {
            stopPrecache();
        }
        this.closeDataLibrary();
        closeWindow();

        System.exit(0);
    }

    /**
     * Save the candidate lists that have been loaded.
     *
     */
    public void saveCandLists() {
        System.out.println("Saving Candidate Lists");
        final LoadingSplash lsplash = new LoadingSplash();
        int i = 0;
        final int max = this.loadedCandLists.size();
        for (final Pair<String, CandList> pair : this.loadedCandLists) {
            final int count = i++;
            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    lsplash.setProgress("Saving Cand List: " + pair.getB().getName(), count, max);
                    lsplash.setVisible(true);
                }
            });

            this.saveCandList(pair.getA(), pair.getB());
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                lsplash.setVisible(false);
                lsplash.dispose();
            }
        });
    }

    public void saveCandList(String group, CandList clist) {
        this.currentDataLibrary.saveCandList(clist, group);

    }

    public void closeCandLists() {
        this.loadedCandLists = new ArrayList<Pair<String, CandList>>();
    }

    /**
     * Save the current data library.
     *
     */
    private void saveDataLibrary() {
        if (this.currentDataLibraryFile != null && this.currentDataLibrary != null) {
            System.out.println("Saving data library");
            try {
                this.currentDataLibrary.write(new PrintStream(new FileOutputStream(this.currentDataLibraryFile)));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeWindow() {
        if (this.currentWindow != null) {
            try {
                if (EventQueue.isDispatchThread()) {
                    currentWindow.setVisible(false);
                    currentWindow.dispose();
                } else {
                    // Close active window...
                    java.awt.EventQueue.invokeAndWait(new Runnable() {

                        public void run() {
                            currentWindow.setVisible(false);
                            currentWindow.dispose();
                        }
                    });
                }
            } catch (InvocationTargetException ex) {
            } catch (InterruptedException ex) {
            }

        }
    }

    /*
     * Save and close the current datalibrary
     */
    public void closeDataLibrary() {
        closeWindow();
        if (this.currentDataLibrary != null) {
            // save progress...
            saveCandLists();
            closeCandLists();
            saveDataLibrary();
        }
        // clear the pointers
        loadedCandLists = loadedCandLists = new ArrayList<Pair<String, CandList>>();
        currentDataLibrary = null;
        currentDataLibraryFile = null;
        currentWindow = null;

    }

    /*
     * select the passed data library and save it to a file.
     * Closes any open datalibrary.
     */
    public void newDataLibrary(DataLibrary dl, File file) {
        closeDataLibrary();
        this.currentDataLibrary = dl;
        this.currentDataLibraryFile = file;

        saveDataLibrary();
        addToDataLibraryHistory(file.getPath());

        dl.clearAll();
        dl.importClistsFromDir(dl.getRootPath());
        // open new Candlist choosing screen.

        chooseCandLists();
    }

    public void openDataLibrary(File file) {
        closeDataLibrary();

        try {
            this.currentDataLibrary = DataLibraryLoader.load(new BufferedReader(new FileReader(file)));
        } catch (FileNotFoundException ex) {
            new MessageBox("Requested Data Library file " + file.getPath() + " not found.").setVisible(true);
            return;
        } catch (IOException ex) {
            new MessageBox("IO error occured loading Data Library " + file.getPath()).setVisible(true);
            return;
        }

        this.currentDataLibraryFile = file;
        addToDataLibraryHistory(file.getPath());

        // open new Candlist choosing screen.

        chooseCandLists();
    }

    /**
     * Go to the candlist choosing screen!
     *
     */
    public void chooseCandLists() {

        // Patch for getting a dm index loaded!
        File dmindexFile = new File(currentDataLibraryFile.getAbsolutePath() + ".dmlist");
        if (dmindexFile.exists()) {
            PulsarCandFile.setDmindexFile(dmindexFile);
        }

        if (precacheThread != null) {
            stopPrecache();
        }

        this.closeWindow();
        this.currentWindow = new pulsarhunter.jreaper.gui.CandListChooseFrame(this, this.currentDataLibrary);
        this.currentWindow.setVisible(true);


    }

    /**
     * Go to the main plot...
     *
     *
     */
    public void goToPlot() {
        this.closeWindow();
        final LoadingSplash lsplash = new LoadingSplash();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                lsplash.setText("Looking for known pulsars");
                lsplash.setVisible(true);
            }
        });
        /*
         * Look for known pulsars
         *
         *
         */
        int ii = 0;
        final int max = this.loadedCandLists.size();


        ArrayList<KnownPSR> extPsrs = null;
        if (external_cand_file != null) {
            extPsrs = this.currentDataLibrary.getKnownPulsarsFromFile(new File(external_cand_file));
            System.out.println("Read "+extPsrs.size()+" pulsars to flag from external source");
        }

        for (Pair<String, CandList> pair : this.loadedCandLists) {
            final CandList clist = pair.getB();
            //
            if (extPsrs != null) {

                final CandRefine cr = this.currentDataLibrary.getRefiner();
                final CoordinateDistanceComparitor cc = new CoordinateDistanceComparitor();
                for (KnownPSR kp : extPsrs) {

                    System.out.println(clist.getBeam().getCoord().toString(false) + " *** " + kp.getPosition().toString(false) );
                    if (cc.difference(clist.getBeam().getCoord(), kp.getPosition()) > this.currentDataLibrary.getOptions().getDistmax()) {
                        continue;
                    }
                    cr.findHarmonics(clist.getCands(), kp.getPeriod(), kp.getName(), kp.getPosition(), 3);
                }
                if (this.currentDataLibrary instanceof WebDataLibrary && !sod_the_db_sync) {
                    ((WebDataLibrary) this.currentDataLibrary).webSync(pair.getB());
                }
            }

            final int count = ii++;
            if (this.currentDataLibrary.getOptions().isAlwaysCheckForKnownPSRs() || !clist.getHeader().isKnownpsrSearched()) {
                // need to check this candlist...
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        lsplash.setProgress("Checking " + clist.getName(), count, max);
                    }
                });
                for (Cand[] cArr : clist.getCands()) {
                    for (Cand c : cArr) {
                        if (c.getCandClass() == 0) {
                            c.clearDetectionList();
                        }
                    }
                }
                this.currentDataLibrary.checkCandListForKnownPSRs(pair.getB());
                if (this.currentDataLibrary instanceof WebDataLibrary && !sod_the_db_sync) {
                    ((WebDataLibrary) this.currentDataLibrary).webSync(pair.getB());
                }
//                for (Cand[] carr : pair.getB().getCands()) {
//                    for (Cand cand : carr) {
//                        cand.getPhfile().release();
//                    }
//                }
//                System.gc();
            }

        }

        System.gc();

        /*
         * Make the big array...
         *
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                lsplash.setText("Creating the main data array...");

            }
        });
        final Cand[][] masterData = new Cand[5][0];
        int lengths[] = new int[5];

        for (Pair<String, CandList> pair : this.loadedCandLists) {
            Cand[][] data = pair.getB().getCands();

            for (int i = 0; i < 5; i++) {
                lengths[i] += data[i].length;
            }
        }

        for (int i = 0; i < 5; i++) {
            masterData[i] = new Cand[lengths[i]];
        }

        int off[] = {0, 0, 0, 0, 0};

        for (Pair<String, CandList> pair : this.loadedCandLists) {
            Cand[][] data = pair.getB().getCands();
            for (int i = 0; i < 5; i++) {
                if (data[i].length > 0) {
                    System.arraycopy(data[i], 0, masterData[i], off[i], data[i].length);
                }
                off[i] += data[i].length;
            }

        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                lsplash.setText("Starting Pre-cache");
            }
        });

        if (precacheThread != null) {
            stopPrecache();
        }
        precacheThread = precache(masterData);




        System.gc();

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                lsplash.setText("Starting the main plot...");
                currentWindow = new MainView(masterData, currentDataLibrary, JReaper.this);
                currentWindow.setVisible(true);
                lsplash.setVisible(false);
                lsplash.dispose();
            }
        });



    }

    public void suspendPrecache() {
        this.precacheThread.setSuspend(true);
    }

    public void resumePrecache() {
        this.precacheThread.setSuspend(false);
    }

    public void stopPrecache() {
        this.precacheThread.setStop(true);
    }

    public int getPrecacheTotal() {
        return this.precacheThread.getTotal();
    }

    public int getPrecacheCounter() {
        return this.precacheThread.getCounter();
    }

    public String getPrecacheStatus() {
        return this.precacheThread.getStatus();
    }

    public PreCacheThread precache(final Cand[][] masterData) {

        PreCacheThread thread = new PreCacheThread(masterData);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        return thread;
    }

    private class PreCacheThread extends Thread {

        private final Cand[][] masterData;
        boolean stop = false, suspend = true;
        private int counter;
        private int total;
        private String status;

        public int getCounter() {
            return counter;
        }

        public String getStatus() {
            return status;
        }

        public int getTotal() {
            return total;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        public void setSuspend(boolean suspend) {
            this.suspend = suspend;
        }

        PreCacheThread(final Cand[][] masterData) {
            this.masterData = masterData;
        }

        public void run() {
            final int nsteps = 5;
            double[] stepsToVal = new double[]{14, 12, 10, 9, 8};

            ArrayList[] precacheArray = new ArrayList[nsteps];
            for (int i = 0; i < nsteps; i++) {
                precacheArray[i] = new ArrayList();
            }
            for (Cand[] cArr : masterData) {
                for (Cand c : cArr) {
                    for (int i = 0; i < nsteps; i++) {
                        if (c.getSNR() > stepsToVal[i]) {
                            precacheArray[i].add(c);
                        }

                    }
                }
            }

            total = 0;
            System.out.println("\nNumber of cands with:");
            for (int i = 0; i < nsteps; i++) {
                System.out.printf("SNR > % 3.1f : %d\n", stepsToVal[i], precacheArray[i].size());
                total += precacheArray[i].size();
            }

            if (total > 200) {
                total = 200;
            }
            for (int i = 0; i < nsteps; i++) {
                System.out.println("\nStarting precache for SNR > " + stepsToVal[i]);
                for (Object o : precacheArray[i]) {
                    status = "Running";
                    if (stop) {
                        System.out.println("\nPrecache stoped");
                        status = "Stopped";
                        return;
                    }
                    while (suspend) {
                        if (stop) {
                            System.out.println("\nPrecache stoped");
                            status = "Stopped";
                            return;
                        }
                        status = "Suspended";
                        try {
                            this.sleep(1000);
                        } catch (InterruptedException interruptedException) {
                        }
                    }
                    ((Cand) o).getCandidateFile().precache();
                    counter++;
                    if (counter >= total) {
                        System.out.println("\nPrecache stoped");
                        status = "Stopped";
                        return;
                    }
                }
            }
        }
    }

    public void saveHistory() {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(new File(HISTORYFILENAME)));
            for (String s : history) {
                out.println(s);
            }
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Change the status of the system...
     *
     */
    public void addDataLibraryType(DataLibraryType dlt) {
        this.dataLibraryTypes.add(dlt);
    }

    public DataLibraryType[] getDataLibraryTypes() {
        return this.dataLibraryTypes.toArray(new DataLibraryType[0]);
    }

    public void addToDataLibraryHistory(String newElem) {
        int start = history.length - 1;
        for (int i = 0; i < history.length; i++) {
            if (history[i].equals(newElem)) {
                start = i;
                break;
            }
        }

        for (int i = start; i > 0; i--) {
            history[i] = history[i - 1];

        }
        history[0] = newElem;

        saveHistory();

    }

    public String[] getDataLibraryHistory() {
        if (this.history == null) {
            this.history = new String[20];
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File(HISTORYFILENAME)));

                String s = reader.readLine();
                for (int i = 0; i < history.length; i++) {
                    if (s == null) {
                        s = "";
                    }
                    history[i] = s;
                    s = reader.readLine();
                }
                reader.close();
            } catch (FileNotFoundException e) {
                saveHistory();
            } catch (IOException e) {
                Arrays.fill(history, "");
            }
        }
        return this.history;
    }

    public void loadCandList(String group, String name) {
        CandList cl = this.currentDataLibrary.getCandList(name, group);

        if (cl != null) {
            this.loadedCandLists.add(new Pair(group, cl));
        }
    }

    public List<Pair<String, CandList>> getLoadedCandLists() {
        return (List<Pair<String, CandList>>) this.loadedCandLists.clone();
    }

    public void closeCandList(String group, CandList clist) {
        this.saveCandList(group, clist);

        this.loadedCandLists.remove(new Pair<String, CandList>(group, clist));

    }

    public void writePMStypeClassFiles() {

        final LoadingSplash lsplash = new LoadingSplash();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                currentWindow.setVisible(false);
                lsplash.setText("Writing to the 'class' files");
                lsplash.setVisible(true);
            }
        });
        for (int i = 1; i < 4; i++) {
            final File file = new File(this.currentDataLibraryFile.getPath() + ".class" + i);
            try {
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {

                        lsplash.setText("Writing to " + file.getName());

                    }
                });
                this.writePMStyleClassFile(file, i);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                lsplash.setText("Done");
                currentWindow.setVisible(true);
                lsplash.setVisible(false);
                lsplash.dispose();
            }
        });

    }

    public void writePMStyleClassFile(File file, int candClass) throws IOException {
        /*
         *  Added a faking of the old style web cand lists...
         */

        PrintStream out = new PrintStream(new FileOutputStream(file, true));

        for (Pair<String, CandList> pair : this.loadedCandLists) {
            for (Cand[] carr : pair.getB().getCands()) {
                for (Cand cand : carr) {
                    if (cand.getCandClass() == candClass) {
                        out.print("@#");
                        out.print(" ");
                        out.print(cand.getCandidateFile().getName());
                        out.print(" ");
                        if (cand.getCandidateFile() instanceof PulsarCandFile) {
                            out.print((((PulsarCandFile) cand.getCandidateFile()).getGridID().substring(1)));  // Grid Number

                        } else if (cand.getCandidateFile() instanceof PulsarHunterCandidate) {

                            String s = ((PulsarHunterCandidate) cand.getCandidateFile()).getHeader().getSourceID();
                            if (s.length() > 7) {
                                s = s.substring(1, 8);
                            }
                            while (s.length() < 7) {
                                s = s + "0";
                            }
                            out.print(s);
                        } else {
                            String s = cand.getName();
                            if (s.length() > 7) {
                                s = s.substring(0, 7);
                            }
                            while (s.length() < 7) {
                                s = s + "0";
                            }
                            out.print(s);  // Put down seven random chars!

                        }
                        out.print(" ");
                        out.printf("%d:%d:%2.0f", cand.getBeam().getCoord().getRA().getHours(), cand.getBeam().getCoord().getRA().getMinutes(), cand.getBeam().getCoord().getRA().getSeconds());
                        out.print(" ");
                        out.printf("%d:%d:%2.0f", cand.getBeam().getCoord().getDec().getDegrees(), cand.getBeam().getCoord().getDec().getArcmins(), cand.getBeam().getCoord().getDec().getArcseconds());
                        out.print(" ");
                        out.printf("%3.3f", cand.getBeam().getCoord().getGl());
                        out.print(" ");
                        out.printf("%3.3f", cand.getBeam().getCoord().getGb());
                        out.print(" ");
                        out.print((int) cand.getMJD());
                        out.print(" ");
                        DateFormat df = new SimpleDateFormat("HH:mm");
                        out.print(df.format(mjd2Date(cand.getMJD())));
                        out.print(" ");
                        out.print(cand.getCandidateFile().getName().substring(11, 13));
                        out.print(" ");
                        if (cand.getCandidateFile() instanceof PulsarCandFile) {
                            out.printf("%5.6f", ((PulsarCandFile) cand.getCandidateFile()).getBarryPeriod());
                        } else if (cand.getCandidateFile() instanceof PulsarHunterCandidate) {
                            out.printf("%5.6f", ((PulsarHunterCandidate) cand.getCandidateFile()).getHeader().getOptimisedBaryPeriod());
                        } else {
                            out.printf("%5.6f", cand.getPeriod());
                        }

                        out.print(" ");
                        if (cand.getCandidateFile() instanceof PulsarCandFile) {
                            out.print((int) (99999));  // Error

                        } else if (cand.getCandidateFile() instanceof PulsarHunterCandidate) {
                            out.print((int) (99999));  // Error

                        } else {
                            out.print((int) (99999));  // Error

                        }

                        out.print(" ");
                        out.printf("%4.1f", cand.getDM());
                        out.print(" ");
                        if (cand.getCandidateFile() instanceof PulsarCandFile) {
                            out.print(((PulsarCandFile) cand.getCandidateFile()).getWidthBins());
                        } else if (cand.getCandidateFile() instanceof PulsarHunterCandidate) {
                            out.print((int) (((PulsarHunterCandidate) cand.getCandidateFile()).getHeader().getOptimizedWidth() *
                                    ((PulsarHunterCandidate) cand.getCandidateFile()).getOptimisedSec().getPulseProfile().length));
                        } else {
                            out.print(99);
                        }

                        out.print(" ");
                        out.printf("%4.2f", cand.getSNR());
                        /*
                        - beam name
                        - grid ID number (it's printed with a G, in the PSR name field on the
                        plot)
                        - raj
                        - decj
                        - gl
                        - gb
                        - reference mjd
                        - reference time of the observation (you can calculate it from the
                        fractional part of the mjd),
                        - .ph file number
                        - baricentric period,
                        - error onthe last quoted digits of the period,
                        - dm
                        - best width
                        - detection signal-to-noise
                         */
                        out.println();
                    }
                }
            }
        }
        out.close();
    }

    private Date mjd2Date(double MJD) {
        double remainder = MJD - 40587;
        long unixTime = (long) remainder * 24 * 3600; // Convert days since 01/01/1970 to seconds since.

        return new Date(unixTime);
    }
}
