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
 * PMDataLibrary.java
 *
 * Created on 28 May 2005, 11:42
 */

package pulsarhunter.jreaper.pmsurv;

import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import coordlib.Telescope;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.CandList;
import pulsarhunter.jreaper.DataLibrary;


import pulsarhunter.jreaper.WebDataLibrary;
import pulsarhunter.jreaper.gui.LoadingSplash;
/**
 *
 * @author mkeith
 */
public class PMDataLibrary extends DataLibrary implements WebDataLibrary,InputStreamGenerator{
    static final long serialVersionUID = 1000L;
    private File webRoot;
    private File realRoot;
    private URL webURL;
    private File resultsRoot;
    private transient float[] dmindex;
    private File dmindexFile;
    
    File RemoteDataLibraryFile = null;
    
    
    private transient boolean webSyncronised = false;
    private transient WebUpdater wu;
    /** Creates a new instance of PMDataLibrary */
    public PMDataLibrary(File rootPath, File webRoot,URL webURL,File resultsRoot) {
        super(rootPath);
        this.realRoot = rootPath;
        this.setWebRoot(webRoot);
        this.setWebURL(webURL);
        this.setResultsRoot(resultsRoot);
        if(!rootPath.exists())rootPath.mkdirs();
    }
    
    public PMDataLibrary() throws IOException{
    }
    
//
//    public void read(BufferedReader in) throws IOException{
//        super.read(in);
//        String[] elems;
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("RESULTSROOT")){
//            throw new IOException("Malformed DataLibrary");
//        }
//        this.resultsRoot = new File(elems[1]);
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("DMIDX")){
//            throw new IOException("Malformed DataLibrary");
//        }
//        this.dmindexFile = new File(elems[1]);
//
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("WEBROOT")){
//            throw new IOException("Malformed DataLibrary");
//        }
//        this.webRoot = new File(elems[1]);
//
//
//        elems = in.readLine().split("=");
//        if(elems.length!=2 || !elems[0].equals("WEBURL")){
//            throw new IOException("Malformed DataLibrary");
//        }
//        try {
//            this.webURL = new URL(elems[1]);
//        } catch (MalformedURLException ex) {
//            ex.printStackTrace();
//        }
//
//        this.realRoot = this.getRootPath();
//    }
    
    
    public void write(PrintStream out) throws IOException{
        super.write(out);
        out.println("RESULTSROOT="+this.resultsRoot.getPath());
        out.println("DMIDX="+this.dmindexFile.getPath());
        out.println("WEBROOT="+this.webRoot.getPath());
        out.println("WEBURL="+this.webURL.toString());
    }
    
    
    
    public String[] getRequirements(){
        return new String[]{"PMSurv"};
    }
    
    private transient Stack<OfflinePlotPanel> imageStack = new Stack<OfflinePlotPanel>();
    private synchronized void  addImagePanel(OfflinePlotPanel item){
        if(imageStack==null) imageStack = new Stack<OfflinePlotPanel>();
        imageStack.push(item);
    }
    private OfflinePlotPanel getNextImagePanel(){
        if(imageStack==null) imageStack = new Stack<OfflinePlotPanel>();
        OfflinePlotPanel result = null;
        try{
            Thread.currentThread().sleep(1000);
        } catch(InterruptedException e2){
            
        }
        while(result == null){
            try{
                synchronized(imageStack){
                    result = imageStack.pop();
                }
            } catch (EmptyStackException e) {
                try{
                    Thread.currentThread().sleep(1000);
                } catch(InterruptedException e2){
                    
                }
                result = null;
            }
        }
        return result;
    }
    
    private static boolean runonce = false;
    public void webUpdate(final Cand cand){
        if(!(cand.getCandidateFile() instanceof PulsarCandFile) || cand.getCandClass() == 4 || cand.getCandClass() == 5 || cand.getCandClass() < 0){
            return;
        }
        
        if(!runonce){
            File webCandList = new File(getRealRoot().getAbsolutePath()+File.separatorChar+"webCandList");
            
            File webCandListLock = new File(getRealRoot().getAbsolutePath()+File.separatorChar+"webCandList.lock");
            
            webCandListLock.delete();
            
            runonce = true;
        }
        
        
        if(imageStack==null) imageStack = new Stack<OfflinePlotPanel>();
        if(webSyncronised){
            final File plotDir = new File(getWebRoot().getAbsolutePath()+File.separatorChar+"plots"+File.separatorChar+cand.getCandidateFile().getName()+".png");
            if(!plotDir.exists()){
                System.out.println(Thread.activeCount());
                while(imageStack.size() > 5 || Thread.activeCount() > 18){
                    try{
                        Thread.currentThread().sleep(1000);
                    } catch(InterruptedException e){
                        throw new RuntimeException(e);
                    }
                }
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        final OfflinePlotPanel pan = new OfflinePlotPanel(cand);
                        PMDataLibrary.this.addImagePanel(pan);
                    }
                });
                
                
                Thread task = new Thread() {
                    public void run() {
                        final JFrame test = new JFrame();
                        
                        OfflinePlotPanel pan = getNextImagePanel();
                        test.setLayout(new BorderLayout());
                        test.add(pan,BorderLayout.CENTER);
                        
                        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                        test.pack();
                        test.setBounds(-600, -800, 600, 800);
                        test.validate();
                        BufferedImage img = pan.getCandImg();
                        
                        try{
                            
                            plotDir.mkdirs();
                            System.out.println("Writing "+plotDir.getName());
                            ImageIO.write(img,"png",plotDir);
                            test.dispose();
                        } catch(IOException e){
                            throw new RuntimeException(e.getMessage(),e);
                        }
                    }
                };
                task.start();
            }
            //java.awt.EventQueue.invokeLater(task);
            File webCandList = new File(getRealRoot().getAbsolutePath()+File.separatorChar+"webCandList");
            System.out.println("Updating master web file");
            try{
                File webCandListLock = new File(getRealRoot().getAbsolutePath()+File.separatorChar+"webCandList.lock");
                try{
                    while(webCandListLock.exists()){
                        Thread.currentThread().sleep(1000);
                    }
                } catch(InterruptedException e){
                    throw new RuntimeException(e);
                }
                webCandListLock.createNewFile();
                PrintStream out = new PrintStream(new FileOutputStream(webCandList,true));
                out.print(cand.getCandidateFile().getName());
                out.print("||");
                out.print(cand.getName());
                out.print("||");
                out.print(cand.getBeam().getCoord().getRA().toString(false));
                out.print("||");
                out.print(cand.getBeam().getCoord().getDec().toString(false));
                out.print("||");
                out.print(cand.getBeam().getCoord().getGl());
                out.print("||");
                out.print(cand.getBeam().getCoord().getGb());
                out.print("||");
                out.print(cand.getCandClass());
                out.print("||");
                out.print(cand.getPeriod());
                out.print("||");
                out.print(cand.getAccel());
                out.print("||");
                out.print(cand.getDM());
                out.print("||");
                out.print(cand.getSNR());
                out.print("||");
                out.print(cand.getComment());
                
                out.close();
                /*
                 *  Added a faking of the old style web cand lists...
                 */
                File oldWebCandList = new File(getRealRoot().getAbsolutePath()+File.separatorChar+"oldStyleWebList");
                out = new PrintStream(new FileOutputStream(oldWebCandList,true));
                out.print("\n"+cand.getCandClass()+" ");
                out.print("@#");
                out.print(" ");
                out.print( cand.getCandidateFile().getName() );
                out.print(" ");
                out.print((((PulsarCandFile)cand.getCandidateFile()).getGridID().substring(1)));  // Grid Number
                out.print(" ");
                out.printf("%d:%d:%2.0f",cand.getBeam().getCoord().getRA().getHours(),cand.getBeam().getCoord().getRA().getMinutes(),cand.getBeam().getCoord().getRA().getSeconds());
                out.print(" ");
                out.printf("%d:%d:%2.0f",cand.getBeam().getCoord().getDec().getDegrees(), cand.getBeam().getCoord().getDec().getArcmins(),cand.getBeam().getCoord().getDec().getArcseconds());
                out.print(" ");
                out.printf("%3.3f",cand.getBeam().getCoord().getGl());
                out.print(" ");
                out.printf("%3.3f",cand.getBeam().getCoord().getGb());
                out.print(" ");
                out.print((int)cand.getMJD());
                out.print(" ");
                DateFormat df = new SimpleDateFormat("HH:mm");
                out.print(df.format(mjd2Date(cand.getMJD())));
                out.print(" ");
                out.print(cand.getCandidateFile().getName().substring(11,13));
                out.print(" ");
                out.printf("%5.6f", ((PulsarCandFile)cand.getCandidateFile()).getBarryPeriod());
                out.print(" ");
                out.print((int)(1000*((PulsarCandFile)cand.getCandidateFile()).getPeriodError()));  // Error
                out.print(" ");
                out.printf("%4.1f",cand.getDM());
                out.print(" ");
                out.print(((PulsarCandFile)cand.getCandidateFile()).getWidthBins());
                out.print(" ");
                out.printf("%4.2f",cand.getSNR());
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
                out.close();
                
                webCandListLock.delete();
            }catch (IOException e){
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(),e);
            }
            if(PMSurv.autoUpdateWebsite){
                Thread thread = new Thread(){
                    public void run(){
                        System.out.println("Updating website.");
                        System.out.println(cand.getCandidateFile().getFile().getName());
                        //main.lsplash.setText("Updating Website");
                        //main.lsplash.setVisible(true);
                        if(wu == null){
                            wu = new WebUpdater(new File(getRealRoot().getAbsolutePath()+File.separatorChar+"webCandList"), getWebRoot());
                        }
                        PMDataLibrary.this.wu.update();
                        //main.lsplash.setVisible(false);
                    }
                };
                thread.start();
            }
        }
    }
    
    private Date mjd2Date(double MJD){
        double remainder = MJD - 40587;
        long unixTime = (long)remainder * 24 * 3600; // Convert days since 01/01/1970 to seconds since.
        return new Date(unixTime);
    }
    
    
    public void webSync(CandList cl){
        
        Cand[][] data = cl.getCands();
        
        final LoadingSplash lsplash = new LoadingSplash();
        
        lsplash.setText("Synchronising with website");
        lsplash.setVisible(true);
        
        synchronized(this){
            
            webSyncronised = false;
            
            String line;
            String[] history;
            double period;
            Coordinate coord;
            for(int i = -1;i<4;i++){
                
                try{
                    char classChar = Integer.toString(i).charAt(0);
                    if(classChar=='-') classChar = 'U';
                    File webSyncFile = new File(getWebRoot().getAbsolutePath()+File.separatorChar+"cands"+File.separatorChar+"class"+classChar);
                    if(!webSyncFile.exists()){
                        System.err.println("Cannot sync with web as class files don't exist (yet).");
                        continue;
                    }
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(getInputStream(webSyncFile)));  //new FileReader(webSyncFile));
                    if(!webSyncFile.exists()) continue;
                    while((line = in.readLine()) != null){
                        String[] components = line.split("\\|\\|");
                        
                        if(components.length<11) continue;
                        try{
                            
                            period = Double.parseDouble(components[6]);
                            RA ra = new RA(0).generateNew("J "+components[2]);
                            Dec dec = new Dec(0).generateNew("J "+components[3]);
                            coord = new Coordinate(ra,dec);
                            history = components[10].split("\\$\\$");
                        } catch (NumberFormatException e){
                            
                            e.printStackTrace();
                            continue;
                        }
                        
                        getRefiner().findHarmonics(data,period,components[1],coord,i,new String[0]);
                    }
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
            
            webSyncronised = true;
            lsplash.setVisible(false);
            lsplash.dispose();
        }
    }
    
    public File getWebRoot() {
        return webRoot;
    }
    
    public void setWebRoot(File webRoot) {
        if(this.webRoot != null && !this.webRoot.equals(webRoot)){
            webRoot.mkdirs();
            this.webRoot = webRoot;
        }
        this.webRoot = webRoot;
    }
    
    public File getRealRoot() {
        return realRoot;
    }
    
    
    public URL getWebURL() {
        return webURL;
    }
    
    public void setWebURL(URL webURL) {
        this.webURL = webURL;
    }
    
    public File getResultsRoot() {
        return resultsRoot;
    }
    
    public void setResultsRoot(File resultsRoot) {
        this.resultsRoot = resultsRoot;
    }
    
    public float[] getDmindex() {
        if(dmindex == null){
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream(dmindexFile)));
                String line;
                int count = 0;
                while((line = reader.readLine()) != null){
                    try{
                        Float.parseFloat(line.trim());
                        count ++;
                    }catch (NumberFormatException e){
                        
                    }
                }
                reader.close();
                reader = new BufferedReader(new InputStreamReader(getInputStream(dmindexFile)));
                dmindex = new float[count];
                int pos = 0;
                while((line = reader.readLine()) != null){
                    try{
                        dmindex[pos] = Float.parseFloat(line.trim());
                        pos++;
                        if(pos >= count) break;
                    }catch (NumberFormatException e){
                        System.err.println("Skiping dmindex "+line+" as it is not a number");
                    }
                }
                reader.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        return dmindex;
    }
    
    public void setDmindex(float[] dmindex) {
        this.dmindex = dmindex;
    }
    
    public File getDmindexFile() {
        return dmindexFile;
    }
    
    public void setDmindexFile(File dmindexFile) {
        if(this.dmindexFile == null || !this.dmindexFile.equals(dmindexFile)){
            setDmindex(null);
        }
        
        this.dmindexFile = dmindexFile;
        
    }
    
    
    public InputStreamGenerator getInputStreamGenerator(){
        return this;
    }
    
    
    public java.io.InputStream getInputStream(File file) throws IOException {
        return new java.io.FileInputStream(file);
    }
    
    
    public boolean saveCandList(CandList cList,String group) {
        boolean retValue;
        cList.setFch1(1374.0);
        cList.setBand(288.0);
        cList.setTelescope(Telescope.PARKES);
        cList.setTobs(2100);
        retValue = super.saveCandList(cList, group);
        
        return retValue;
    }
    
    
}
