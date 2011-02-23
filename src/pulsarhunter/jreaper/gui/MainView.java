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
 * MainView.java
 *
 * Created on 24 May 2005, 20:07
 */
package pulsarhunter.jreaper.gui;

import coordlib.Beam;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import pulsarhunter.FrequencyFilter;
import pulsarhunter.Pair;
import pulsarhunter.datatypes.ZapFile;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.HarmonicType;
import pulsarhunter.jreaper.DataLibrary;
import pulsarhunter.jreaper.Detection;
import pulsarhunter.jreaper.JReaper;
import pulsarhunter.jreaper.KnownPSR;
import pulsarhunter.jreaper.PlotType;

/**
 *
 * @author  mkeith
 */
public class MainView extends javax.swing.JFrame {

    private Cand[][] masterData;
    private Cand[][] curData;
    private ClickableGraph plot;
    private DataLibrary dataLibrary;
    private JReaper jreaper;
    private Hashtable<PlotType.axisType, Double> altLimitMax = new Hashtable<PlotType.axisType, Double>();
    private Hashtable<PlotType.axisType, Double> altLimitMin = new Hashtable<PlotType.axisType, Double>();
    private Hashtable<PlotType.axisType, JTextField> limitFieldsMin = new Hashtable<PlotType.axisType, JTextField>();
    private Hashtable<PlotType.axisType, JTextField> limitFieldsMax = new Hashtable<PlotType.axisType, JTextField>();
    private JCheckBox[] beamChecks;
    private ArrayList<Beam> beams;
    private PlotType pType = new PlotType(PlotType.axisType.Period, PlotType.axisType.FoldSNR);
    private boolean memClearActive = false;
    private RefreshThread refreshThread;
    private double lowx,  highx,  lowy,  highy;
    private boolean zoomed = false;
    private Cand lastViewed=null;

    /** Creates new form MainView */
    public MainView(Cand[][] masterData, DataLibrary dataLibrary, JReaper jreaper) {
        initComponents();
        this.setDataLibrary(dataLibrary);
        this.masterData = masterData;
        this.jreaper = jreaper;
        curData = masterData;
        beams = searchForBeams(masterData);
        plot = new ClickableGraph(curData, pType, this);

        for (int i = 0; i < masterData.length; i++) {
            System.out.printf("Loaded: % 6d cands of type %d\n", masterData[i].length, i);
        }


        final LoadingSplash lsplash = new LoadingSplash();
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                setTitle(JReaper.WINDOWTITLE);

                repaint();

                jPanel1.add(plot, java.awt.BorderLayout.CENTER);

                xAxisChooser.setModel(new DefaultComboBoxModel(PlotType.axisType.values()));
                xAxisChooser.setSelectedItem(PlotType.axisType.Period);
                yAxisChooser.setModel(new DefaultComboBoxModel(PlotType.axisType.values()));
                yAxisChooser.setSelectedItem(PlotType.axisType.FoldSNR);
                zAxisChooser.setModel(new DefaultComboBoxModel(PlotType.axisType.values()));
                zAxisChooser.setEnabled(zAxisCheck.isSelected());
                zAxisChooser.setSelectedItem(PlotType.axisType.DM);



                zMinField.setEnabled(zAxisCheck.isSelected());
                zMaxField.setEnabled(zAxisCheck.isSelected());
                zCapCheck.setEnabled(zAxisCheck.isSelected());

                beamChecks = new JCheckBox[beams.size()];
                for (int i = 0; i < beamChecks.length; i++) {
                    beamChecks[i] = new JCheckBox();
                    beamChecks[i].setText(beams.get(i).getName()); // text MUSt be the name of the beam!!! +" \t("+(int)beams.get(i).getCoord().getGl()+", "+(int)beams.get(i).getCoord().getGb()+")");
                    beamChecks[i].setSelected(true);
                    beamChecks[i].addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            refine();
                        }
                    });
                    jPanel_beamList.add(beamChecks[i]);
                }

                harmonicPanel.setName("Harmonics");
                JPanel[] panels = new JPanel[]{selectSurveyPanel, harmonicPanel, excludeBeamsPanel};
                paneChooser.setModel(new DefaultComboBoxModel(panels));
                customPanel.removeAll();
                customPanel.add((Component) paneChooser.getSelectedItem(), java.awt.BorderLayout.CENTER);


                for (PlotType.axisType axisType : PlotType.axisType.values()) {
                    JLabel jl = new JLabel();
                    jl.setText(axisType.name() + " Min");
                    JTextField ta = new JTextField();
                    ta.setText("");
                    limitFieldsMin.put(axisType, ta);
                    altLimitMin.put(axisType, -Double.MAX_VALUE);
                    jPanel_limits.add(jl);
                    jPanel_limits.add(ta);

                    jl = new JLabel();
                    jl.setText(axisType.name() + " Max");
                    ta = new JTextField();
                    ta.setText("");
                    limitFieldsMax.put(axisType, ta);
                    altLimitMax.put(axisType, Double.MAX_VALUE);
                    jPanel_limits.add(jl);
                    jPanel_limits.add(ta);
                }



                jFrame_setLimits.pack();


            }
        });
        refreshThread = new RefreshThread();
        refreshThread.setPriority(Thread.MIN_PRIORITY);
        refreshThread.start();
        this.reZap();
        this.repaint();

    }

    @Override
    public void dispose() {
        this.refreshThread.setStop(true);
        super.dispose();
    }

    private class RefreshThread extends Thread {

        private boolean stop = false;

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        public void run() {


            while (!stop) {
                try {

                    this.sleep(500);
                    checkmem();
                    Runnable r = new Runnable() {

                        public void run() {
                            MainView.this.jLabel_precache.setText("PreCache: " + jreaper.getPrecacheStatus());
                            MainView.this.jProgressBar_precache.setMaximum(jreaper.getPrecacheTotal());
                            MainView.this.jProgressBar_precache.setValue(jreaper.getPrecacheCounter());

                        }
                    };
                    SwingUtilities.invokeAndWait(r);



                } catch (InterruptedException interruptedException) {
                } catch (InvocationTargetException e) {
                }


            }
            System.out.println("Refresh Thread stopped.");

        }
    }

    public void clickOn(double x, double y) {

        if (this.galacticPlotButton.isSelected()) {
            List<KnownPSR> knownPSRList = dataLibrary.getKnownPulsars();
            KnownPSR[] knownPSRs = new KnownPSR[knownPSRList.size()];
            knownPSRList.toArray(knownPSRs);
            KnownPSR c = getNearest(knownPSRs, x, y, plot.getXscale(), plot.getYscale());
            new MessageBox("PSR: " + c.getName() + "\nPeriod: " + c.getPeriod() + "\nPosn: " + c.getPosition().toString()).setVisible(true);
        } else {
            Cand c = pType.getNearest(curData, x, y, plot.getXscale(), plot.getYscale());
            if (c == null) {
                return;
            }
            if (!c.getCandidateFile().getFile().exists()) {
                new MessageBox("Could not find file for candidate " + c.getName() + "\n" + c.getCandidateFile().getFile().getAbsolutePath()).setVisible(true);
                return;
            }

            JFrame plotFrame = c.getCandidateFile().getCandDisplayFrame(c, this);
            plotFrame.setVisible(true);
            c.setViewed(true);
            this.setLastViewed(c);
        }
        replot();
    }
    private ArrayBlockingQueue<JFrame> frameQueue = new ArrayBlockingQueue<JFrame>(500);

    public void clickOnHold(double x, double y) {

        if (this.galacticPlotButton.isSelected()) {
            clickOn(x, y);
        } else {
            Cand c = pType.getNearest(curData, x, y, plot.getXscale(), plot.getYscale());
            if (c == null) {
                return;
            }

            JFrame plotFrame = c.getCandidateFile().getCandDisplayFrame(c, this);
            if (frameQueue.remainingCapacity() < 1) {
                System.err.println("Could not add plot to queue, already full!");
                return;
            }
            try {
                frameQueue.put(plotFrame);
                c.setViewed(true);
            } catch (InterruptedException e) {
            }
            jButton3.setEnabled(true);
            jButton3.setText("Display Held (" + MainView.this.frameQueue.size() + ")");
        }
        replot();

    }

    private void clickOnHold(Cand c) throws IOException {
        if (c == null) {
            return;
        }
        if (!c.getCandidateFile().getFile().exists()) {
            new MessageBox("Could not find file for candidate " + c.getName() + "\n" + c.getCandidateFile().getFile().getAbsolutePath()).setVisible(true);
            throw new IOException("Could not find file for candidate " + c.getName());
        }
        JFrame plotFrame = c.getCandidateFile().getCandDisplayFrame(c, this);
        if (frameQueue.remainingCapacity() < 1) {
            System.err.println("Could not add plot to queue, already full!");
            return;
        }
        try {
            frameQueue.put(plotFrame);
            c.setViewed(true);
        } catch (InterruptedException e) {
        }
        jButton3.setEnabled(true);
        jButton3.setText("Display Held (" + MainView.this.frameQueue.size() + ")");
    }
    private boolean clickOnReleaseRunning = false;

    public void clickOnRelease() {
        if (clickOnReleaseRunning) {
            return;
        }

        clickOnReleaseRunning = true;
        Thread thread = new Thread() {

            public void run() {
                JFrame f = frameQueue.poll();
                CandidateDisplayFrame cdf = null;
                CandidateDisplayFrame pcdf = null;

                while (f != null) {
                    if (f instanceof CandidateDisplayFrame) {
                        cdf = (CandidateDisplayFrame) f;
                        if (pcdf != null && !pcdf.isVisible()) {
                            pcdf = null;
                        }
                        MainView.this.setLastViewed(cdf.getCand());
                        replot();
                    }
                    if (pcdf != null) {
                        pcdf.swap(cdf);
                        pcdf.closed = false;
                        cdf = pcdf;
                    } else {
                        f.setVisible(true);
                    }
                    if (cdf != null && pcdf == null) {
                        pcdf = cdf;
                    }

                    if (pcdf != null) {
                        pcdf.setNremaining(frameQueue.size());
                    }


                    while (true) {
                        if (pcdf == null && !f.isVisible()) {
                            break;
                        }
                        if (pcdf != null && pcdf.closed) {
                            break;
                        }
                        try {
                            this.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    f = frameQueue.poll();


                }
                MainView.this.clickOnReleaseRunning = false;
                replot();
            }
        };
        thread.start();

    }

    public void reZap() {
        Thread task = new Thread() {

            public void run() {
                final LoadingSplash lsplash = new LoadingSplash();
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        lsplash.setText("Marking Duds... Please wait");
                        lsplash.setVisible(true);
                    }
                });

                clearDuds();

                // Load zap lists..
                ArrayList<FrequencyFilter> zapFiles = new ArrayList<FrequencyFilter>();
                String zapRoot = MainView.this.dataLibrary.getOptions().getZapFileRoot();
                for (Pair<String, Boolean> pair : MainView.this.dataLibrary.getOptions().getZapFiles()) {

                    if (pair.getB()) {
                        ZapFile z = new ZapFile(new File(zapRoot + pair.getA() + ".zapfile"));
                        try {
                            z.read();

                            zapFiles.addAll(z.getFilters());
                        } catch (IOException ex) {
                            System.err.println("Error: Cannot read dud list: " + new File(zapRoot + pair.getA() + ".zapfile").getAbsolutePath());
                        }
                    }

                }



                for (Cand[] arr : MainView.this.curData) {
                    for (Cand c : arr) {
                        for (FrequencyFilter f : zapFiles) {
                            if (f.periodMatch(c.getPeriod() / 1000.0)) {
                                c.setDud(true);
                                break;
                            }
                        }
                    }
                }


                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        MainView.this.replot();
                        lsplash.setVisible(false);
                        lsplash.dispose();
                    }
                });
            }
        };

        task.start();
    }

    public void clearDuds() {
        for (Cand[] arr : MainView.this.curData) {
            for (Cand c : arr) {
                c.setDud(false);
            }
        }
    }

    public void toggleSpecificBeam(String beamName) {
        for (Component c : jPanel_beamList.getComponents()) {
            if (c instanceof JCheckBox) {
                if (((JCheckBox) c).getText().equals(beamName)) {
                    ((JCheckBox) c).setSelected(!((JCheckBox) c).isSelected());
                }
            }
        }
        refine();
    }

    public void selectAllBeams() {
        for (Component c : jPanel_beamList.getComponents()) {
            if (c instanceof JCheckBox) {
                ((JCheckBox) c).setSelected(true);
            }
        }
        refine();
    }

    public void selectNoBeams() {
        for (Component c : jPanel_beamList.getComponents()) {
            if (c instanceof JCheckBox) {
                ((JCheckBox) c).setSelected(false);
            }
        }
        refine();
    }

    public void setLastViewed(Cand last){
        this.lastViewed=last;
    }

    public void clickArea(final double x1, final double x2, final double y1, final double y2, final boolean viewed) {

        Thread task = new Thread() {

            public void run() {
                final LoadingSplash lsplash = new LoadingSplash();
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        lsplash.setText("Selecting Candidates... Please wait");
                        lsplash.setVisible(true);
                    }
                });

                for (Cand[] arr : MainView.this.curData) {
                    for (Cand c : arr) {
                        double xval = MainView.this.pType.getXval(c);
                        double yval = MainView.this.pType.getYval(c);
                        if ((xval < x1 && xval > x2) && (yval > y1 && yval < y2)) {
                            if (c.getCandClass() >= 0) {
                                continue;
                            }

                            if (!viewed && c.beenViewed()) {
                                continue;
                            }
                            try {

                                MainView.this.clickOnHold(c);
                            } catch (IOException ex) {
                                java.awt.EventQueue.invokeLater(new Runnable() {

                                    public void run() {

                                        lsplash.setVisible(false);
                                        lsplash.dispose();
                                    }
                                });
                                return;
                            }

                        }
                    }
                }

                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        MainView.this.replot();
                        lsplash.setVisible(false);
                        lsplash.dispose();
                    }
                });


            }
        };
        task.start();


    }

    private KnownPSR getNearest(KnownPSR[] cands, double x, double y, double xscale, double yscale) {
        double distmin = Double.MAX_VALUE;
        double curdist = 0.0;
        KnownPSR curCand = null;
        for (int i = 0; i < cands.length; i++) {

            curdist = Math.pow(((cands[i].getPosition().getGl() - x) * xscale), 2.0) + Math.pow(((cands[i].getPosition().getGb() - y) * yscale), 2.0);
            if (curdist < distmin) {
                distmin = curdist;
                curCand = cands[i];

            }
        }
        return curCand;

    }

    private ArrayList<Beam> searchForBeams(Cand[][] masterData) {
        ArrayList<Beam> beams = new ArrayList<Beam>();
        for (int i = 0; i < masterData.length; i++) {
            for (int j = 0; j < masterData[i].length; j++) {
                if (!beams.contains(masterData[i][j].getBeam())) {
                    beams.add(masterData[i][j].getBeam());
                }
            }
        }
        return beams;
    }

    public void findHarmonics(Cand cand, int candClass, String name) {

        dataLibrary.getRefiner().findHarmonics(masterData, cand.getPeriod(), name, cand.getBeam().getCoord(), candClass);


        this.repaint();
    }

    public void findHarmonics(Cand cand, int candClass) {
        if (cand.getCandClass() == 0 && candClass != 0) {
            findHarmonics(cand, candClass, cand.getCandidateFile().getUniqueIdentifier());
        } else {
            findHarmonics(cand, candClass, cand.getCandidateFile().getName());
        }
        this.repaint();
    }

    public boolean mark(HarmonicType type) {
        switch (type) {
            case Principal:
                return this.markFundBox.isSelected();
            case Integer:
                return markIntBox.isSelected();
            case SimpleNonInteger:
                return this.markNIBox.isSelected();
            case ComplexNonInteger:
                return this.markCNIbox.isSelected();
        }

        return true;
    }

    public boolean show(HarmonicType type) {
        switch (type) {
            case Principal:
                return !this.hideFundBox.isSelected();
            case Integer:
                return !this.hideIntBox.isSelected();
            case SimpleNonInteger:
                return !this.hideNIBox.isSelected();
            case ComplexNonInteger:
                return !this.hideComplexBox.isSelected();
        }

        return true;
    }
    private Stack<double[]> oldZooms = new Stack<double[]>();

    public void zoomU(double lowx, double highx, double lowy, double highy) {

        oldZooms.push(new double[]{this.lowx, this.highx, this.lowy, this.highy});

        zoom(lowx, highx, lowy, highy);
    }

    public void zoom(double lowx, double highx, double lowy, double highy) {
        /*curData = pType.zoom(masterData,lowx,highx,lowy,highy);
        replot();*/
        zoomed = true;
        this.lowx = lowx;
        this.highx = highx;
        this.lowy = lowy;
        this.highy = highy;
    }

    public void rezoom() {
        if (zoomed) {
            plot.zoomN(lowx, lowy, highx, highy);
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    zoomed = true;
                    lowx = plot.getXRange()[0];
                    highx = plot.getXRange()[1];
                    lowy = plot.getYRange()[0];
                    highy = plot.getYRange()[1];
                }
            });
        }
    }

    public void deZoom() {
        zoomed = true;
        if (oldZooms.empty()) {
            autoZoom();
        } else {
            double[] old = oldZooms.pop();
            this.lowx = old[0];
            this.highx = old[1];
            this.lowy = old[2];
            this.highy = old[3];
        }

//        this.lowx -= 0.1 * (this.highx - this.lowx);
//        this.highx += 0.1 * (this.highx - this.lowx);
//        this.lowy -= 0.1 * (this.highy - this.lowy);
//        this.highy += 0.1 * (this.highy - this.lowy);
        replot();
    }

    public void autoZoom() {
        zoomed = false;
        replot();



    }

    public void refine() {
//        double snrminv,dmminv;
//        try{
//            snrminv = Double.parseDouble(snrBox.getText());
//        }catch(NumberFormatException e){
//            snrminv = 0.0;
//        }
//        try{
//            dmminv = Double.parseDouble(dmBox.getText());
//        }catch(NumberFormatException e){
//            dmminv = 0.0;
//        }
//        final double snrmin = snrminv;
//        final double dmmin = dmminv;



        Thread thread = new Thread() {

            public void run() {
                ArrayList<String> excludes = new ArrayList<String>();
                for (int i = 0; i < beamChecks.length; i++) {
                    if (!beamChecks[i].isSelected()) {
                        excludes.add(beamChecks[i].getText());
                    }
                }

                Hashtable<PlotType.axisType, Double> minVals = new Hashtable<PlotType.axisType, Double>();
                Hashtable<PlotType.axisType, Double> maxVals = new Hashtable<PlotType.axisType, Double>();
                for (PlotType.axisType axisType : PlotType.axisType.values()) {
                    JTextField tf = MainView.this.limitFieldsMin.get(axisType);
                    double val = 0.0;
                    try {
                        if (tf.getText().equals("")) {
                            val = -Double.MAX_VALUE;
                        } else {
                            val = Double.parseDouble(tf.getText());
                        }
                    } catch (NumberFormatException e) {
                        val = 0.0;
                    }
                    minVals.put(axisType, val);

                    tf = MainView.this.limitFieldsMax.get(axisType);
                    val = 0.0;
                    try {
                        if (tf.getText().equals("")) {
                            val = Double.MAX_VALUE;
                        } else {
                            val = Double.parseDouble(tf.getText());
                        }

                    } catch (NumberFormatException e) {
                        val = 0.0;
                    }
                    maxVals.put(axisType, val);

                }


                String[] excludeBeams = new String[excludes.size()];
                System.arraycopy(excludes.toArray(), 0, excludeBeams, 0, excludes.size());
                //MainView.this.curData = Main.getInstance().getDataLibrary().getRefiner().refine(MainView.this.masterData,snrmin,dmmin,new boolean[]{MainView.this.series1Check.isSelected(),MainView.this.series2Check.isSelected(),MainView.this.series3Check.isSelected(),MainView.this.series4Check.isSelected()},excludeBeams);
                MainView.this.curData = MainView.this.dataLibrary.getRefiner().refine(MainView.this.masterData, minVals, maxVals, new boolean[]{MainView.this.series1Check.isSelected(), MainView.this.series2Check.isSelected(), MainView.this.series3Check.isSelected(), MainView.this.series4Check.isSelected()}, excludeBeams);
                MainView.this.replot();
            }
        };
        thread.start();

    }
    private boolean gal = false;

    public void replot() {
        Runnable task = new Runnable() {

            public void run() {
                jButton3.setEnabled(!frameQueue.isEmpty());
                jButton3.setText("Display Held (" + MainView.this.frameQueue.size() + ")");
                if (galacticPlotButton.isSelected()) {

                    ArrayList<String> excludes = new ArrayList<String>();
                    for (int i = 0; i < beamChecks.length; i++) {
                        if (!beamChecks[i].isSelected()) {
                            excludes.add(beamChecks[i].getText());
                        }
                    }

                    String[] excludeBeams = new String[excludes.size()];
                    System.arraycopy(excludes.toArray(), 0, excludeBeams, 0, excludes.size());
                    plot.galacticPlot(beams, MainView.this.dataLibrary.getKnownPulsars(), null, excludeBeams, curData);
                } else {

                    if (pType.hasZ()) {
                        double minval = Double.MIN_VALUE, maxval = Double.MAX_VALUE;
                        try {
                            minval = Double.parseDouble(zMinField.getText());
                            maxval = Double.parseDouble(zMaxField.getText());
                        } catch (Exception e) {
                            pType.calibrateZ(curData);
                        }
                        if (zCapCheck.isSelected()) {
                            pType.calibrateZ(curData, maxval, minval);
                        } else {
                            pType.calibrateZ(curData);
                        }
                    }
                    //Test!!!
                    plot.changeData(curData, pType, true,lastViewed);
                }
                MainView.this.repaint();
            }
        };
        SwingUtilities.invokeLater(task);
    }

    public void markAreaDud(final double x1, final double x2, final boolean unmark) {
        //System.out.println(x1+" "+x2);
        Thread task = new Thread() {

            public void run() {


                for (Cand[] arr : MainView.this.curData) {
                    for (Cand c : arr) {
                        double val = MainView.this.pType.getXval(c);

                        if (val < x1 && val > x2) {
                            if (unmark) {
                                c.addDetection(new Detection("Unmarked Dud Area", "", 5, HarmonicType.Principal, c.getPeriod()));
                            } else {
                                c.addDetection(new Detection("Marked Dud Area", "", 4, HarmonicType.Principal, c.getPeriod()));
                            }
                        }
                    }
                }


                MainView.this.replot();
            }
        };
        task.start();


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        harmonicPanel = new NamedJPanel("Harmonic");
        jButton_removeBeams = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        markFundBox = new javax.swing.JCheckBox();
        markIntBox = new javax.swing.JCheckBox();
        markNIBox = new javax.swing.JCheckBox();
        markCNIbox = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        hideFundBox = new javax.swing.JCheckBox();
        hideIntBox = new javax.swing.JCheckBox();
        hideNIBox = new javax.swing.JCheckBox();
        hideComplexBox = new javax.swing.JCheckBox();
        selectSurveyPanel = new NamedJPanel("Results Sets");
        jLabel10 = new javax.swing.JLabel();
        series1Check = new javax.swing.JCheckBox();
        series2Check = new javax.swing.JCheckBox();
        series3Check = new javax.swing.JCheckBox();
        series4Check = new javax.swing.JCheckBox();
        jButton5 = new javax.swing.JButton();
        excludeBeamsPanel = new NamedJPanel("Select Beams");
        beamListPanel = new javax.swing.JScrollPane();
        selectBeamsPanel = new javax.swing.JPanel();
        jButton_selectBeams_invert = new javax.swing.JButton();
        jButton_selectBeams_selectAll = new javax.swing.JButton();
        jButton_selectBeams_selectNone = new javax.swing.JButton();
        jButton_selectBeams_selectSpecific = new javax.swing.JButton();
        customBeamSelectionFrame = new javax.swing.JFrame();
        customBeamSelectionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel_beamList = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jFrame_setLimits = new javax.swing.JFrame();
        jPanel_limits = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        xAxisChooser = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        yAxisChooser = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        zAxisChooser = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        zMinField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        zMaxField = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        zCapCheck = new javax.swing.JCheckBox();
        zAxisCheck = new javax.swing.JCheckBox();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        galacticPlotButton = new javax.swing.JToggleButton();
        jButton7 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        paneChooser = new javax.swing.JComboBox();
        customPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel_precache = new javax.swing.JLabel();
        jProgressBar_precache = new javax.swing.JProgressBar();
        jPanel10 = new javax.swing.JPanel();
        jLabel_mem = new javax.swing.JLabel();
        jProgressBar_mem = new javax.swing.JProgressBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        dataMenu = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem_en_memclear = new javax.swing.JMenuItem();
        jMenuItem_dis_memclear = new javax.swing.JMenuItem();

        harmonicPanel.setBackground(new java.awt.Color(255, 249, 230));
        harmonicPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Harmonics"));
        harmonicPanel.setOpaque(false);
        harmonicPanel.setLayout(new javax.swing.BoxLayout(harmonicPanel, javax.swing.BoxLayout.Y_AXIS));

        jButton_removeBeams.setText("Remove Beams");
        jButton_removeBeams.setToolTipText("de-selects beams with known pulsars in");
        jButton_removeBeams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_removeBeamsActionPerformed(evt);
            }
        });
        harmonicPanel.add(jButton_removeBeams);

        jLabel8.setText("Mark");
        harmonicPanel.add(jLabel8);

        markFundBox.setSelected(true);
        markFundBox.setText("Fundemental");
        markFundBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        harmonicPanel.add(markFundBox);

        markIntBox.setSelected(true);
        markIntBox.setText("Integer Harms");
        markIntBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        harmonicPanel.add(markIntBox);

        markNIBox.setText("Non-Integer");
        markNIBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        harmonicPanel.add(markNIBox);

        markCNIbox.setText("Complex NI");
        markCNIbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        harmonicPanel.add(markCNIbox);

        jLabel9.setText("Hide");
        harmonicPanel.add(jLabel9);

        hideFundBox.setText("Fundemental");
        hideFundBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        harmonicPanel.add(hideFundBox);

        hideIntBox.setText("Integer Harms");
        hideIntBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        harmonicPanel.add(hideIntBox);

        hideNIBox.setText("Non-Integer");
        hideNIBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        harmonicPanel.add(hideNIBox);

        hideComplexBox.setText("Complex NI");
        hideComplexBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        harmonicPanel.add(hideComplexBox);

        selectSurveyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Results Sets"));
        selectSurveyPanel.setOpaque(false);
        selectSurveyPanel.setLayout(new javax.swing.BoxLayout(selectSurveyPanel, javax.swing.BoxLayout.Y_AXIS));

        jLabel10.setText("Show");
        selectSurveyPanel.add(jLabel10);

        series1Check.setSelected(true);
        series1Check.setText("Std FFT");
        series1Check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        selectSurveyPanel.add(series1Check);

        series2Check.setSelected(true);
        series2Check.setText("Accel FFT");
        series2Check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        selectSurveyPanel.add(series2Check);

        series3Check.setSelected(true);
        series3Check.setText("Long Prd");
        series3Check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        selectSurveyPanel.add(series3Check);

        series4Check.setSelected(true);
        series4Check.setText("Other/Unk");
        series4Check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replotActionPerformed(evt);
            }
        });
        selectSurveyPanel.add(series4Check);

        jButton5.setText("Update");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refineActionPerformed(evt);
            }
        });
        selectSurveyPanel.add(jButton5);

        excludeBeamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Beams"));
        excludeBeamsPanel.setOpaque(false);
        excludeBeamsPanel.setLayout(new javax.swing.BoxLayout(excludeBeamsPanel, javax.swing.BoxLayout.Y_AXIS));

        selectBeamsPanel.setLayout(new java.awt.GridLayout(4, 1));

        jButton_selectBeams_invert.setText("Invert");
        jButton_selectBeams_invert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_selectBeams_invertActionPerformed(evt);
            }
        });
        selectBeamsPanel.add(jButton_selectBeams_invert);

        jButton_selectBeams_selectAll.setText("Select All");
        jButton_selectBeams_selectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_selectBeams_selectAllActionPerformed(evt);
            }
        });
        selectBeamsPanel.add(jButton_selectBeams_selectAll);

        jButton_selectBeams_selectNone.setText("Select None");
        jButton_selectBeams_selectNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_selectBeams_selectNoneActionPerformed(evt);
            }
        });
        selectBeamsPanel.add(jButton_selectBeams_selectNone);

        jButton_selectBeams_selectSpecific.setText("Select From List");
        jButton_selectBeams_selectSpecific.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excludeBeamsSelectFromList(evt);
            }
        });
        selectBeamsPanel.add(jButton_selectBeams_selectSpecific);

        beamListPanel.setViewportView(selectBeamsPanel);

        excludeBeamsPanel.add(beamListPanel);

        customBeamSelectionPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanel_beamList.setAutoscrolls(true);
        jPanel_beamList.setLayout(new javax.swing.BoxLayout(jPanel_beamList, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(jPanel_beamList);

        customBeamSelectionPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.Y_AXIS));

        jButton2.setText("Close");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customBeamCloseButtonPressed(evt);
            }
        });
        jPanel7.add(jButton2);

        customBeamSelectionPanel.add(jPanel7, java.awt.BorderLayout.SOUTH);

        customBeamSelectionFrame.getContentPane().add(customBeamSelectionPanel, java.awt.BorderLayout.CENTER);

        jPanel_limits.setLayout(new java.awt.GridLayout(12, 4));
        jFrame_setLimits.getContentPane().add(jPanel_limits, java.awt.BorderLayout.CENTER);

        jButton6.setText("Close and Refine");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton6);

        jFrame_setLimits.getContentPane().add(jPanel8, java.awt.BorderLayout.SOUTH);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("JReaper 2.2");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 249, 230));
        jPanel1.setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setBackground(new java.awt.Color(255, 249, 230));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setBackground(new java.awt.Color(255, 249, 230));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        jPanel2.setBackground(new java.awt.Color(255, 249, 230));
        jPanel2.setLayout(new java.awt.GridLayout(5, 2));

        jLabel4.setBackground(new java.awt.Color(255, 249, 230));
        jLabel4.setText("X Axis");
        jPanel2.add(jLabel4);

        xAxisChooser.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                xAxisChooserItemStateChanged(evt);
            }
        });
        xAxisChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xAxisChooserActionPerformed(evt);
            }
        });
        jPanel2.add(xAxisChooser);

        jLabel5.setText("Y Axis");
        jPanel2.add(jLabel5);

        yAxisChooser.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                yAxisChooserItemStateChanged(evt);
            }
        });
        jPanel2.add(yAxisChooser);

        jLabel1.setText("Z Axis");
        jPanel2.add(jLabel1);

        zAxisChooser.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zAxisChooserItemStateChanged(evt);
            }
        });
        zAxisChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zAxisChooserActionPerformed(evt);
            }
        });
        jPanel2.add(zAxisChooser);

        jLabel6.setText("Z min");
        jLabel6.setToolTipText("Adjust the Z range by capping values");
        jPanel2.add(jLabel6);

        zMinField.setText("-");
        jPanel2.add(zMinField);

        jLabel7.setText("Z max");
        jLabel7.setToolTipText("Adjust the Z range by capping values");
        jPanel2.add(jLabel7);

        zMaxField.setText("-");
        jPanel2.add(zMaxField);

        jPanel4.add(jPanel2);

        jPanel5.setBackground(new java.awt.Color(255, 249, 230));
        jPanel5.setLayout(new java.awt.GridLayout(10, 1));

        zCapCheck.setBackground(new java.awt.Color(255, 249, 230));
        zCapCheck.setText("Cap Z vals");
        zCapCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zCapCheckItemStateChanged(evt);
            }
        });
        jPanel5.add(zCapCheck);

        zAxisCheck.setBackground(new java.awt.Color(255, 249, 230));
        zAxisCheck.setText("Use Z axis");
        zAxisCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zAxisCheckItemStateChanged(evt);
            }
        });
        zAxisCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zAxisCheckActionPerformed(evt);
            }
        });
        jPanel5.add(zAxisCheck);

        jButton3.setText("Display Held");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton3);

        jButton1.setText("Set Limits");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton1);

        jToggleButton1.setText("Alt Limits");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        jPanel5.add(jToggleButton1);
        jPanel5.add(jSeparator1);

        galacticPlotButton.setText("Galactic Plot");
        galacticPlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                galacticPlotButtonActionPerformed(evt);
            }
        });
        jPanel5.add(galacticPlotButton);

        jButton7.setText("Undo Zoom");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton7);

        jButton4.setText("Reset Zoom");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton4);

        paneChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paneChooserActionPerformed(evt);
            }
        });
        jPanel5.add(paneChooser);

        jPanel4.add(jPanel5);

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        customPanel.setBackground(new java.awt.Color(255, 249, 230));
        customPanel.setLayout(new java.awt.BorderLayout());
        jPanel3.add(customPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.EAST);

        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.LINE_AXIS));

        jPanel9.setLayout(new java.awt.GridLayout(1, 2));

        jLabel_precache.setText("Precache");
        jPanel9.add(jLabel_precache);
        jPanel9.add(jProgressBar_precache);

        jPanel6.add(jPanel9);

        jPanel10.setLayout(new java.awt.GridLayout(1, 2));

        jLabel_mem.setText("Mem");
        jPanel10.add(jLabel_mem);
        jPanel10.add(jProgressBar_mem);

        jPanel6.add(jPanel10);

        getContentPane().add(jPanel6, java.awt.BorderLayout.SOUTH);

        jMenu1.setText("JReaper");

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setText("Save");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem12);

        jMenuItem3.setText("Exit");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        dataMenu.setText("Data");

        jMenuItem2.setText("Select New Data");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        dataMenu.add(jMenuItem2);

        jMenuItem4.setText("Write PM style Class Files");
        jMenuItem4.setToolTipText("This may take a while. Appends the loaded info to the file.");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        dataMenu.add(jMenuItem4);

        jMenuBar1.add(dataMenu);

        jMenu2.setText("Options");

        jMenuItem1.setText("DataLibrary Options");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem5.setText("Re-plot Scores");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuItem6.setText("Mark all cands as not viewed");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem6);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Debug");

        jMenuItem8.setText("Garbage Collect");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem8);

        jMenuItem7.setText("Check Mem");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem7);

        jMenuItem9.setText("Suspend Precache");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem9);

        jMenuItem10.setText("Stop Precache");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem10);

        jMenuItem11.setText("Resume Precache");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem11);

        jMenuItem_en_memclear.setText("Enable MemClear");
        jMenuItem_en_memclear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_en_memclearActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem_en_memclear);

        jMenuItem_dis_memclear.setText("Diable MemClear");
        jMenuItem_dis_memclear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem_dis_memclearActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem_dis_memclear);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-1024)/2, (screenSize.height-768)/2, 1024, 768);
    }// </editor-fold>//GEN-END:initComponents
    private void jButton_removeBeamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_removeBeamsActionPerformed
        this.jButton_removeBeams.setEnabled(false);
        Thread task = new Thread() {

            public void run() {


                for (Cand[] arr : MainView.this.curData) {
                    for (Cand c : arr) {
                        if (c.getCandClass() == 0) {
                            if (c.getDetectionList().get(0).getHarmType() == HarmonicType.Principal && !markFundBox.isSelected()) {
                                continue;
                            }
                            if (c.getDetectionList().get(0).getHarmType() == HarmonicType.Integer && !markIntBox.isSelected()) {
                                continue;
                            }
                            if (c.getDetectionList().get(0).getHarmType() == HarmonicType.SimpleNonInteger && !markNIBox.isSelected()) {
                                continue;
                            }
                            if (c.getDetectionList().get(0).getHarmType() == HarmonicType.ComplexNonInteger && !markCNIbox.isSelected()) {
                                continue;
                            }

                            for (int i = 0; i < beamChecks.length; i++) {

                                if (beamChecks[i].getText().equals(c.getBeam().getName())) {
                                    beamChecks[i].setSelected(false);
                                }
                            }
                        }
                    }


                    MainView.this.refine();
                    Runnable task = new Runnable() {

                        public void run() {
                            MainView.this.jButton_removeBeams.setEnabled(true);
                        }
                    };
                    SwingUtilities.invokeLater(task);
                }
            }
        };
        task.start();

    }//GEN-LAST:event_jButton_removeBeamsActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        for (Cand[] cl : this.masterData) {
            for (Cand c : cl) {
                c.setViewed(false);
            }
        }
        MainView.this.replot();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        for (Cand[] cl : this.masterData) {
            for (Cand c : cl) {
                c.recalcScore();
            }
        }
        MainView.this.replot();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DataLibraryOptionsFrame(dataLibrary).setVisible(true);
            }
        });
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        jreaper.writePMStypeClassFiles();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Runnable runnable = new Runnable() {

            public void run() {
                jreaper.close();
            }
        };
        new Thread(runnable).start();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    }//GEN-LAST:event_formWindowClosed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        for (PlotType.axisType axisType : PlotType.axisType.values()) {

            double val = -Double.MAX_VALUE;
            try {
                if (this.limitFieldsMin.get(axisType).getText().equals("")) {
                    val = -Double.MAX_VALUE;
                } else {
                    val = Double.parseDouble(this.limitFieldsMin.get(axisType).getText());
                }
            } catch (NumberFormatException ex) {
            }

            if (this.altLimitMin.get(axisType).equals(-Double.MAX_VALUE)) {
                this.limitFieldsMin.get(axisType).setText("");
            } else {
                this.limitFieldsMin.get(axisType).setText(this.altLimitMin.get(axisType).toString());
            }
            this.altLimitMin.remove(axisType);
            this.altLimitMin.put(axisType, val);



            val = Double.MAX_VALUE;
            try {
                if (this.limitFieldsMax.get(axisType).getText().equals("")) {
                    val = Double.MAX_VALUE;
                } else {
                    val = Double.parseDouble(this.limitFieldsMax.get(axisType).getText());
                }
            } catch (NumberFormatException ex) {
            }

            if (this.altLimitMax.get(axisType).equals(Double.MAX_VALUE)) {
                this.limitFieldsMax.get(axisType).setText("");
            } else {
                this.limitFieldsMax.get(axisType).setText(this.altLimitMax.remove(axisType).toString());
            }
            this.altLimitMax.remove(axisType);
            this.altLimitMax.put(axisType, val);




        }

        this.refine();
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        this.jFrame_setLimits.setVisible(false);
        this.refine();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton_selectBeams_selectNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_selectBeams_selectNoneActionPerformed
        this.selectNoBeams();
    }//GEN-LAST:event_jButton_selectBeams_selectNoneActionPerformed

    private void jButton_selectBeams_selectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_selectBeams_selectAllActionPerformed
        this.selectAllBeams();
    }//GEN-LAST:event_jButton_selectBeams_selectAllActionPerformed

    public void pack() {
    }

    public DataLibrary getDataLibrary() {
        return dataLibrary;
    }

    public void setDataLibrary(DataLibrary dataLibrary) {
        this.dataLibrary = dataLibrary;
    }

    private void excludeBeamsSelectFromList(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excludeBeamsSelectFromList
        this.customBeamSelectionFrame.setSize(300, 400);
        this.customBeamSelectionFrame.setVisible(true);

    }//GEN-LAST:event_excludeBeamsSelectFromList

    private void customBeamCloseButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customBeamCloseButtonPressed
        this.customBeamSelectionFrame.setVisible(false);
    }//GEN-LAST:event_customBeamCloseButtonPressed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.clickOnRelease();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton_selectBeams_invertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_selectBeams_invertActionPerformed
        for (Component c : jPanel_beamList.getComponents()) {
            if (c instanceof JCheckBox) {
                ((JCheckBox) c).setSelected(!((JCheckBox) c).isSelected());
            }
        }
        refine();
    }//GEN-LAST:event_jButton_selectBeams_invertActionPerformed

    private void galacticPlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_galacticPlotButtonActionPerformed
        replot();
    }//GEN-LAST:event_galacticPlotButtonActionPerformed

    private void refineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refineActionPerformed
        refine();
    }//GEN-LAST:event_refineActionPerformed

    private void replotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replotActionPerformed
        replot();
    }//GEN-LAST:event_replotActionPerformed

    private void paneChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paneChooserActionPerformed
        Runnable task = new Runnable() {

            public void run() {
                MainView.this.customPanel.setVisible(false);
                MainView.this.customPanel.removeAll();
                MainView.this.customPanel.add((Component) paneChooser.getSelectedItem(), java.awt.BorderLayout.CENTER);
                MainView.this.customPanel.setVisible(true);
                MainView.this.repaint();

            }
        };
        SwingUtilities.invokeLater(task);
    }//GEN-LAST:event_paneChooserActionPerformed

    private void zCapCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zCapCheckItemStateChanged
        replot();
    }//GEN-LAST:event_zCapCheckItemStateChanged

    private void zAxisCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zAxisCheckActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_zAxisCheckActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        Runnable runnable = new Runnable() {

            public void run() {
                jreaper.close();
            }
        };
        new Thread(runnable).start();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed

        Runnable runnable = new Runnable() {

            public void run() {
                jreaper.chooseCandLists();
            }
        };
        new Thread(runnable).start();


    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void zAxisCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zAxisCheckItemStateChanged
        zAxisChooser.setEnabled(zAxisCheck.isSelected());
        zMinField.setEnabled(zAxisCheck.isSelected());
        zMaxField.setEnabled(zAxisCheck.isSelected());
        zCapCheck.setEnabled(zAxisCheck.isSelected());
        final boolean zaxis = zAxisCheck.isSelected();
        Runnable task = new Runnable() {

            public void run() {
                if (zaxis) {
                    MainView.this.pType = new PlotType((PlotType.axisType) xAxisChooser.getSelectedItem(), (PlotType.axisType) yAxisChooser.getSelectedItem(), (PlotType.axisType) zAxisChooser.getSelectedItem());
                //MainView.this.pType.calibrateZ(MainView.this.curData);
                } else {
                    MainView.this.pType = new PlotType((PlotType.axisType) xAxisChooser.getSelectedItem(), (PlotType.axisType) yAxisChooser.getSelectedItem());
                }
                MainView.this.replot();

            }
        };
        SwingUtilities.invokeLater(task);
    }//GEN-LAST:event_zAxisCheckItemStateChanged

    private void zAxisChooserItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zAxisChooserItemStateChanged
        final boolean zaxis = zAxisCheck.isSelected();
        Runnable task = new Runnable() {

            public void run() {
                if (zaxis) {
                    MainView.this.pType = new PlotType((PlotType.axisType) xAxisChooser.getSelectedItem(), (PlotType.axisType) yAxisChooser.getSelectedItem(), (PlotType.axisType) zAxisChooser.getSelectedItem());
                //MainView.this.pType.calibrateZ(MainView.this.curData);
                } else {
                    MainView.this.pType = new PlotType((PlotType.axisType) xAxisChooser.getSelectedItem(), (PlotType.axisType) yAxisChooser.getSelectedItem());
                }
                MainView.this.replot();

            }
        };
        SwingUtilities.invokeLater(task);
    }//GEN-LAST:event_zAxisChooserItemStateChanged

    private void zAxisChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zAxisChooserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_zAxisChooserActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Runnable task = new Runnable() {

            public void run() {
                MainView.this.autoZoom();
            }
        };
        SwingUtilities.invokeLater(task);

    }//GEN-LAST:event_jButton4ActionPerformed

    private void yAxisChooserItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_yAxisChooserItemStateChanged
        final boolean zaxis = zAxisCheck.isSelected();
        Runnable task = new Runnable() {

            public void run() {
                if (zaxis) {
                    MainView.this.pType = new PlotType((PlotType.axisType) xAxisChooser.getSelectedItem(), (PlotType.axisType) yAxisChooser.getSelectedItem(), (PlotType.axisType) zAxisChooser.getSelectedItem());
                //MainView.this.pType.calibrateZ(MainView.this.curData);
                } else {
                    MainView.this.pType = new PlotType((PlotType.axisType) xAxisChooser.getSelectedItem(), (PlotType.axisType) yAxisChooser.getSelectedItem());
                }
                MainView.this.replot();

            }
        };
        SwingUtilities.invokeLater(task);
    }//GEN-LAST:event_yAxisChooserItemStateChanged

    private void xAxisChooserItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_xAxisChooserItemStateChanged
        final boolean zaxis = zAxisCheck.isSelected();
        Runnable task = new Runnable() {

            public void run() {
                if (zaxis) {
                    MainView.this.pType = new PlotType((PlotType.axisType) xAxisChooser.getSelectedItem(), (PlotType.axisType) yAxisChooser.getSelectedItem(), (PlotType.axisType) zAxisChooser.getSelectedItem());
                //MainView.this.pType.calibrateZ(MainView.this.curData);
                } else {
                    MainView.this.pType = new PlotType((PlotType.axisType) xAxisChooser.getSelectedItem(), (PlotType.axisType) yAxisChooser.getSelectedItem());
                }
                MainView.this.replot();

            }
        };
        SwingUtilities.invokeLater(task);
    }//GEN-LAST:event_xAxisChooserItemStateChanged

    private void xAxisChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xAxisChooserActionPerformed
    }//GEN-LAST:event_xAxisChooserActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
//        refine();
        this.jFrame_setLimits.validate();
        this.jFrame_setLimits.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        System.gc();
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        checkmem();
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        jreaper.suspendPrecache();
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        jreaper.stopPrecache();
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        jreaper.resumePrecache();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem_en_memclearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_en_memclearActionPerformed
        this.memClearActive = true;
        this.jMenuItem_en_memclear.setEnabled(!this.memClearActive);
        this.jMenuItem_dis_memclear.setEnabled(this.memClearActive);
}//GEN-LAST:event_jMenuItem_en_memclearActionPerformed

    private void jMenuItem_dis_memclearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_dis_memclearActionPerformed
        this.memClearActive = false;
        this.jMenuItem_en_memclear.setEnabled(!this.memClearActive);
        this.jMenuItem_dis_memclear.setEnabled(this.memClearActive);
}//GEN-LAST:event_jMenuItem_dis_memclearActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        Runnable task = new Runnable() {

            public void run() {
                MainView.this.deZoom();
            }
        };
        SwingUtilities.invokeLater(task);




    }//GEN-LAST:event_jButton7ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed

        new Thread(){

            @Override
            public void run() {
                super.run();
                jreaper.saveCandLists();
            }

        }.start();
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void checkmem() {
        long memused = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long totalmem = Runtime.getRuntime().maxMemory();
        System.gc();
        StringBuffer memstr = new StringBuffer();
        Formatter formatter = new Formatter(memstr);
        String enabled = "Memclear off";
        if (memClearActive) {
            enabled = "Memclear on";
        }
        formatter.format("Mem: %d/%d MB (%s)", (int) (memused / 1e6), (int) (totalmem / 1e6), enabled);
        this.jProgressBar_mem.setMaximum(100);
        this.jProgressBar_mem.setValue((int) (100 * ((double) memused / (double) totalmem)));
        this.jLabel_mem.setText(memstr.toString());
        //   System.out.println(memstr);
        if (memClearActive) {
            if ((double) memused / (double) totalmem > 0.80) {

                System.out.printf(" --- ATEMPTING TO CLEAR MEM ---");
                for (Cand[] cArr : masterData) {
                    for (Cand c : cArr) {
                        c.getCandidateFile().release();
                    }
                }
                System.gc();

            }
            formatter.format("Mem: %d/%d MB (%s)", (int) (memused / 1e6), (int) (totalmem / 1e6), enabled);
            this.jLabel_mem.setText(memstr.toString());
        // System.out.println(memstr);
        }
    }
    /**
     * @param args the command line arguments
     */
    /*public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
    public void run() {
    new MainView().setVisible(true);
    }
    });
    }*/
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane beamListPanel;
    private javax.swing.JFrame customBeamSelectionFrame;
    private javax.swing.JPanel customBeamSelectionPanel;
    private javax.swing.JPanel customPanel;
    private javax.swing.JMenu dataMenu;
    private javax.swing.JPanel excludeBeamsPanel;
    private javax.swing.JToggleButton galacticPlotButton;
    private javax.swing.JPanel harmonicPanel;
    private javax.swing.JCheckBox hideComplexBox;
    private javax.swing.JCheckBox hideFundBox;
    private javax.swing.JCheckBox hideIntBox;
    private javax.swing.JCheckBox hideNIBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton_removeBeams;
    private javax.swing.JButton jButton_selectBeams_invert;
    private javax.swing.JButton jButton_selectBeams_selectAll;
    private javax.swing.JButton jButton_selectBeams_selectNone;
    private javax.swing.JButton jButton_selectBeams_selectSpecific;
    private javax.swing.JFrame jFrame_setLimits;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_mem;
    private javax.swing.JLabel jLabel_precache;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JMenuItem jMenuItem_dis_memclear;
    private javax.swing.JMenuItem jMenuItem_en_memclear;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel_beamList;
    private javax.swing.JPanel jPanel_limits;
    private javax.swing.JProgressBar jProgressBar_mem;
    private javax.swing.JProgressBar jProgressBar_precache;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JCheckBox markCNIbox;
    private javax.swing.JCheckBox markFundBox;
    private javax.swing.JCheckBox markIntBox;
    private javax.swing.JCheckBox markNIBox;
    private javax.swing.JComboBox paneChooser;
    private javax.swing.JPanel selectBeamsPanel;
    private javax.swing.JPanel selectSurveyPanel;
    private javax.swing.JCheckBox series1Check;
    private javax.swing.JCheckBox series2Check;
    private javax.swing.JCheckBox series3Check;
    private javax.swing.JCheckBox series4Check;
    private javax.swing.JComboBox xAxisChooser;
    private javax.swing.JComboBox yAxisChooser;
    private javax.swing.JCheckBox zAxisCheck;
    private javax.swing.JComboBox zAxisChooser;
    private javax.swing.JCheckBox zCapCheck;
    private javax.swing.JTextField zMaxField;
    private javax.swing.JTextField zMinField;
    // End of variables declaration//GEN-END:variables
}
