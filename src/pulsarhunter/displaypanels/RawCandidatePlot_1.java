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
 * PHCFPlot.java
 *
 * Created on 25 January 2007, 16:47
 */
package pulsarhunter.displaypanels;

import bookkeepr.xml.StringConvertable;
import bookkeepr.xmlable.RawCandidate;
import bookkeepr.xmlable.RawCandidateSection;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Formatter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pulsarhunter.Convert;
import pulsarhunter.datatypes.PHCSection;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.displaypanels.MKPlot.LineStyle;
import pulsarhunter.jreaper.Colourmap;

/**
 *
 * @author  mkeith
 */
public class RawCandidatePlot_1 extends javax.swing.JPanel {

    private RawCandidate rawCandidate;
    private Colourmap colourmap;
    private Color c1;
    private Color c2;

    /** Creates new form PHCFPlot */
    public RawCandidatePlot_1(RawCandidate rawCandidate, Colourmap colourmap, Color c1, Color c2) {
        this.colourmap = colourmap;
        this.c1 = c1;
        this.c2 = c2;
        initComponents();
        this.rawCandidate = rawCandidate;
        init();
        this.validate();
    }

    public void init() {

        StringBuffer line = new StringBuffer();
        line.append("ID: " + StringConvertable.ID.toString(rawCandidate.getId()));
        line.append("   RA:" + rawCandidate.getCoordinate().getRA().toString(false));
        line.append("   Dec:" + rawCandidate.getCoordinate().getDec().toString(false));

        line.append("   Gl:" + round(rawCandidate.getCoordinate().getGl(), 100));
        line.append("   Gb:" + round(rawCandidate.getCoordinate().getGb(), 100));
        line.append("   MJD:" + round(rawCandidate.getMjdStart(), 100));




        this.jLabel_header_1.setText(line.toString());

        line = new StringBuffer();

        line.append("ObsFreq:" + round(rawCandidate.getCentreFreq(), 10) + "MHz");
        line.append("   Tobs:" + round(rawCandidate.getTobs(), 1) + "s");
        line.append("   Tsamp:" + (int) (rawCandidate.getOptimisedSec().getTsamp()) + "us");
        line.append("   SourceID:" + rawCandidate.getSourceId());
        line.append("   Telescope:" + rawCandidate.getTelescope());


        this.jLabel_header_2.setText(line.toString());

        line = new StringBuffer();



        line.append("SpecSNR:" + round(rawCandidate.getSpectralSnr(), 100));
        line.append("   ReconSNR:" + round(rawCandidate.getReconstructedSnr(), 100));

//        if (rawCandidate.getInitialSec().getExtraValue("HFOLD") != null) {
//            line.append("   H-Fold:" + rawCandidate.getInitialSec().getExtraValue("HFOLD"));
//        }
//
//        if (rawCandidate.getHeader().getExtraValue("ZAP") != null) {
//            line.append("   Zap:" + rawCandidate.getHeader().getExtraValue("ZAP"));
//        }


        this.jLabel_header_3.setText(line.toString());


        String[] sections = rawCandidate.getKeys().toArray(new String[]{});

        JPanel[] profilePanels = new JPanel[sections.length];
        JPanel[] pdmPanels = new JPanel[sections.length];
        JPanel[] sintPanels = new JPanel[sections.length];
        JPanel[] sbandPanels = new JPanel[sections.length];
        JPanel[] dmCurvePanels = new JPanel[sections.length];
        JPanel[] pdotCurvePanels = new JPanel[sections.length];

        for (int i = 0; i < sections.length; i++) {

            RawCandidateSection sec = rawCandidate.getSections().get(sections[i]);

            /****************
             *   Profiles   *
             ****************/
            {
                if (sec.getEncodedProfile() != null) {
                    double[] prof = sec.getEncodedProfile().getDoubleArr();
                    int nbins = prof.length;
                    double[] yaxis = Convert.wrapDoubleArr(prof, 1.5);
                    double[] xaxis = new double[yaxis.length];
                    for (int j = 0; j < xaxis.length; j++) {
                        xaxis[j] = (double) j / (double) prof.length;
                    }
                    double[] xpoints = new double[0];
                    double[] ypoints = new double[0];

                    if (i == (sections.length - 1)) {

                        double maxVal = 0;
                        int maxposn = 0;
                        for (int j = 0; j < nbins; j++) {
                            if (yaxis[j] > maxVal) {
                                maxVal = yaxis[j];
                                maxposn = j;
                            }

                        }


//                        int numP = rawCandidate.getNPulses();
//                        xpoints = new double[numP];
//                        ypoints = new double[numP];
//                        double spacing = (double) nbins / (double) numP;
//                        for (int j = 1; j <= numP; j++) {
//                            int bin = (int) (spacing * j) + maxposn;
//                            while (bin >= nbins) {
//                                bin -= nbins;
//                            }
//                            xpoints[j - 1] = bin / ((double) nbins);
//                            ypoints[j - 1] = 0;
//                        }
                    }

                    profilePanels[i] = new PlotOneDim("Profile", "Phase", "", xaxis, yaxis, xpoints, ypoints, c2, c1);
                } else {
                    profilePanels[i] = new JPanel();
                }
            }

            /****************
             * Period / PDM *
             ****************/
            {
                if (sec.getSnrBlock() != null && sec.getSnrBlock().getPeriodIndex().length > 1) {
                    if (sec.getSnrBlock().getDmIndex().length > 1) {
                        // We have PDM to draw...

                        double[][] map_d = sec.getSnrBlock().getPDmPlane(sec.getBestAccn(), sec.getBestJerk());
                        //  int[][] map_i = Convert.doubleArrToIntArr(map_d,0,255,1);

                        double[] periodIdx = sec.getSnrBlock().getPeriodIndex();

                        String pType;
                        double period = 0;
                        if (sec.getSnrBlock().isBarrycenter()) {
                            period = sec.getBestBaryPeriod();
                            pType = "Bary ";
                        } else {
                            period = sec.getBestTopoPeriod();
                            pType = "Topo ";
                        }
                        for (int j = 0; j < periodIdx.length; j++) {
                            periodIdx[j] -= period;
                            periodIdx[j] *= 1000.0;
                        }


                        double[] dmIdx = sec.getSnrBlock().getDmIndex();

                        //  double dmStep = dmIdx[1] - dmIdx[0];
                        //  double pStep = (periodIdx[1] - periodIdx[0])*1000.0;


                        //pdmPanels[i] = new PlotTwoDim("Period-DM Plane","Period","DM",map_i,periodIdx[0]*1000.0,pStep,dmIdx[0],dmStep);
                        pdmPanels[i] = new PlotTwoDim("Period-DM Plane", pType + "Period Offset from " + (1000.0 * period) + "ms", "DM", periodIdx, dmIdx, map_d, colourmap);
                    } else {
                        // We have just Period to draw...
                        double[] xaxis = sec.getSnrBlock().getPeriodIndex();
                        double dm = sec.getBestDm();
                        if(Double.isNaN(dm))dm = 0;
                        double[] yaxis = sec.getSnrBlock().getPeriodCurve(dm, sec.getBestAccn(), sec.getBestJerk());
                        double period;
                        String pType;
                        if (sec.getSnrBlock().isBarrycenter()) {
                            period = sec.getBestBaryPeriod();
                            pType = "Bary ";
                        } else {
                            period = sec.getBestTopoPeriod();
                            pType = "Topo ";
                        }
                        double minV = sec.getBestSnr();
                        for (double v : yaxis) {
                            if (v < minV) {
                                minV = v;
                            }
                        }

                        double width = sec.getBestWidth();
                        if (Double.isNaN(width) || width < 0) {
                            width = rawCandidate.getOptimisedSec().getBestWidth();
                        }

                        double[] model = Convert.generatePeriodCurve(xaxis, period, width, rawCandidate.getTobs());

                        // Convert range to plot range.
                        for (int j = 0; j < model.length; j++) {
                            model[j] = model[j] * (sec.getBestSnr() - minV) + minV;
                        }


                        for (int j = 0; j < xaxis.length; j++) {
                            xaxis[j] -= period;
                            xaxis[j] *= 1000.0;
                        }

                        pdmPanels[i] = new PlotOneDim("Period Curve", pType + "Period Offset from " + (1000.0 * period) + "ms", "SNR", xaxis, model, xaxis, yaxis, c1, c2);
                        ((PlotOneDim) pdmPanels[i]).setJoinDots(true);
                        ((PlotOneDim) pdmPanels[i]).setLinestyle(LineStyle.JoinTheDots);
                    }

                } else {
                    // there are no period info... Put a blank plot for now
                    pdmPanels[i] = new JPanel();
                }
            }

            /****************
             *    Subints   *
             ****************/
            {
                if (sec.getEncodedSubintegrations() != null) {
                    double[][] map_d = Convert.wrapDoubleArr(Convert.rotateDoubleArray(sec.getEncodedSubintegrations().getDoubleArr()), 1.5);
                    //int[][]    map_i = Convert.doubleArrToIntArr(map_d,0,255,1.5);



                    sintPanels[i] = new PlotTwoDim("Sub-Integrations", "Bin Number", "Sub-Int Number", map_d, colourmap);
                } else {
                    // There is no subints...
                    sintPanels[i] = new JPanel();
                }



            }

            /****************
             *    SubBands   *
             ****************/
            {
                if (sec.getEncodedSubbands() != null) {
                    double[][] map_d = Convert.wrapDoubleArr(Convert.rotateDoubleArray(sec.getEncodedSubbands().getDoubleArr()), 1.5);

                    //   int[][]    map_i = Convert.doubleArrToIntArr(map_d,0,255,1.5);

                    sbandPanels[i] = new PlotTwoDim("Sub-Bands", "Bin Number", "Sub-Band Number", map_d, colourmap);
                } else {
                    // There is no subbands...
                    sbandPanels[i] = new JPanel();
                }



            }

            /****************
             *    DM Curve   *
             ****************/
            {
                if (sec.getSnrBlock() != null && sec.getSnrBlock().getDmIndex().length > 1) {

                    double[] xaxis = sec.getSnrBlock().getDmIndex();

                    double[] yaxis;
                    double period;
                    if (sec.getSnrBlock().isBarrycenter()) {
                        period = sec.getBestBaryPeriod();
                    } else {
                        period = sec.getBestTopoPeriod();
                    }

                    //yaxis = sec.getSnrBlock().getDmCurve(period,sec.getBestAccn(),sec.getBestJerk());
                    yaxis = sec.getSnrBlock().getFlatDmCurve();


                    double width = sec.getBestWidth();
                    if (Double.isNaN(width) || width < 0) {
                        width = rawCandidate.getOptimisedSec().getBestWidth();
                    }
                    double[] model = Convert.generateDmCurve(xaxis, period, width, sec.getBestDm(), rawCandidate.getBandwidth(), rawCandidate.getCentreFreq());
                    for (int j = 0; j < model.length; j++) {
                        model[j] *= sec.getBestSnr();

                    }



                    dmCurvePanels[i] = new PlotOneDim("DM Curve", "DM", "SNR", xaxis, model, xaxis, yaxis, c1, c2);
                    ((PlotOneDim) dmCurvePanels[i]).setJoinDots(true);
                    ((PlotOneDim) dmCurvePanels[i]).setLinestyle(LineStyle.JoinTheDots);

                } else {
                    // There is no dm curve...
                    dmCurvePanels[i] = new JPanel();
                }

            }


            /****************
             *  Acc Curve   *
             ****************/
            {
                if (sec.getSnrBlock() != null && sec.getSnrBlock().getAccnIndex().length > 1) {
                    if (sec.getSnrBlock().getJerkIndex().length > 1) {
                        double[][] map_d;
                        if (sec.getSnrBlock().isBarrycenter()) {

                            map_d = sec.getSnrBlock().getAccnJerkPlane(sec.getBestDm(), sec.getBestBaryPeriod());
                        } else {

                            map_d = sec.getSnrBlock().getAccnJerkPlane(sec.getBestDm(), sec.getBestTopoPeriod());
                        }

                        //  int[][] map_i = Convert.doubleArrToIntArr(map_d,0,255,1);

                        double[] acIndex = sec.getSnrBlock().getAccnIndex();
                        double[] jeIndex = sec.getSnrBlock().getJerkIndex();

                        //  double dmStep = dmIdx[1] - dmIdx[0];
                        //  double pStep = (periodIdx[1] - periodIdx[0])*1000.0;


                        //pdmPanels[i] = new PlotTwoDim("Period-DM Plane","Period","DM",map_i,periodIdx[0]*1000.0,pStep,dmIdx[0],dmStep);
                        pdotCurvePanels[i] = new PlotTwoDim("Accn-Jerk Plane", "Accn (m/s/s)", "Jerk (m/s/s/s)", acIndex, jeIndex, map_d, colourmap);

                    } else {
                        // pdot only
                        double[] xaxis = sec.getSnrBlock().getAccnIndex();
                        double[] yaxis;
                        /*
                        if(sec.getSnrBlock().isBarrycenter()){
                        yaxis = sec.getSnrBlock().getAccnCurve(sec.getBestDm(),sec.getBestBaryPeriod(),sec.getBestJerk());
                        } else {
                        yaxis = sec.getSnrBlock().getAccnCurve(sec.getBestDm(),sec.getBestTopoPeriod(),sec.getBestJerk());
                        }
                         */
                        yaxis = sec.getSnrBlock().getFlatAccCurve();

                        pdotCurvePanels[i] = new PlotOneDim("Accn Curve", "Accn (m/s/s)", "SNR", new double[0], new double[0], xaxis, yaxis, c1, c2);
                        ((PlotOneDim) pdotCurvePanels[i]).setJoinDots(true);
                    }

                } else if (sec.getSnrBlock() != null && sec.getSnrBlock().getJerkIndex().length > 1) {
                    // pddot only
                    double[] xaxis = sec.getSnrBlock().getJerkIndex();
                    double[] yaxis;
                    if (sec.getSnrBlock().isBarrycenter()) {
                        yaxis = sec.getSnrBlock().getJerkCurve(sec.getBestDm(), sec.getBestBaryPeriod(), sec.getBestAccn());
                    } else {
                        yaxis = sec.getSnrBlock().getJerkCurve(sec.getBestDm(), sec.getBestTopoPeriod(), sec.getBestAccn());
                    }
                    pdotCurvePanels[i] = new PlotOneDim("Jerk Curve", "Jerk (m/s/s/s)", "SNR", new double[0], new double[0], xaxis, yaxis, c1, c2);
                    ((PlotOneDim) pdotCurvePanels[i]).setJoinDots(true);
                } else {
                    // There is no pdot or pddot curve...
                    pdotCurvePanels[i] = new JPanel();
                }


            }
        }



        MultiPlotPanel profileMpp = new MultiPlotPanel("Profile", sections, profilePanels, profilePanels.length - 1);
        MultiPlotPanel pdmMpp = new MultiPlotPanel("Period/Dm", sections, pdmPanels, pdmPanels.length - 1);
        MultiPlotPanel sintMpp = new MultiPlotPanel("Sub Integrations", sections, sintPanels, sintPanels.length - 1);
        MultiPlotPanel sbandMpp = new MultiPlotPanel("Sub Bands", sections, sbandPanels, sbandPanels.length - 1);
        MultiPlotPanel dmcMpp = new MultiPlotPanel("Dm Curve", sections, dmCurvePanels, 0);
        MultiPlotPanel pdotMpp = new MultiPlotPanel("Accn/Jerk", sections, pdotCurvePanels, 0);
        // right
        this.jPanel_main_right.add(pdmMpp);
        this.jPanel_main_right.add(dmcMpp);
        this.jPanel_main_right.add(pdotMpp);
        // left
        this.jPanel_main_left.add(sbandMpp);
        this.jPanel_main_left.add(sintMpp);
        this.jPanel_main_left.add(profileMpp);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 4));

        panel.add(new JLabel(""));
        panel.add(new JLabel("Initial"));
        panel.add(new JLabel("Optimised"));
        panel.add(new JLabel(""));

        panel.add(new JLabel("Bary Period"));
        panel.add(new JLabel("" + this.round(rawCandidate.getInitialSec().getBestBaryPeriod() * 1000, 1e9)));
        panel.add(new JLabel("" + this.round(rawCandidate.getOptimisedSec().getBestBaryPeriod() * 1000, 1e9)));
        panel.add(new JLabel("ms"));

        panel.add(new JLabel("Bary Freq"));
        panel.add(new JLabel(this.round(1.0 / rawCandidate.getInitialSec().getBestBaryPeriod(), 1e9)));
        panel.add(new JLabel(this.round(1.0 / rawCandidate.getOptimisedSec().getBestBaryPeriod(), 1e9)));
        panel.add(new JLabel("Hz"));

        panel.add(new JLabel("Topo Period"));
        panel.add(new JLabel(this.round(rawCandidate.getInitialSec().getBestTopoPeriod() * 1000, 1e9)));
        panel.add(new JLabel(this.round(rawCandidate.getOptimisedSec().getBestTopoPeriod() * 1000, 1e9)));
        panel.add(new JLabel("ms"));


        panel.add(new JLabel("Topo Freq"));
        panel.add(new JLabel("" + this.round(1.0 / rawCandidate.getInitialSec().getBestTopoPeriod(), 1e9)));
        panel.add(new JLabel("" + this.round(1.0 / rawCandidate.getOptimisedSec().getBestTopoPeriod(), 1e9)));
        panel.add(new JLabel("Hz"));

        panel.add(new JLabel("Accn"));
        panel.add(new JLabel(this.shortStringOf(rawCandidate.getInitialSec().getBestAccn())));
        panel.add(new JLabel(this.shortStringOf(rawCandidate.getOptimisedSec().getBestAccn())));
        panel.add(new JLabel("m/s/s"));


        panel.add(new JLabel("Jerk"));
        panel.add(new JLabel(this.shortStringOf(rawCandidate.getInitialSec().getBestJerk())));
        panel.add(new JLabel(this.shortStringOf(rawCandidate.getOptimisedSec().getBestJerk())));
        panel.add(new JLabel("m/s/s/s"));

        panel.add(new JLabel("DM"));
        panel.add(new JLabel("" + this.round(rawCandidate.getInitialSec().getBestDm(), 100.0)));
        panel.add(new JLabel("" + this.round(rawCandidate.getOptimisedSec().getBestDm(), 100.0)));
        panel.add(new JLabel("cm/pc"));

        panel.add(new JLabel("Width"));
        panel.add(new JLabel("" + this.round(rawCandidate.getInitialSec().getBestWidth(), 100.0)));
        panel.add(new JLabel("" + this.round(rawCandidate.getOptimisedSec().getBestWidth(), 100.0)));
        panel.add(new JLabel("periods"));

        panel.add(new JLabel("SNR"));
        panel.add(new JLabel("" + this.round(rawCandidate.getInitialSec().getBestSnr(), 100.0)));
        panel.add(new JLabel("" + this.round(rawCandidate.getOptimisedSec().getBestSnr(), 100.0)));
        panel.add(new JLabel(""));
        int i = 0;
        for (Component c : panel.getComponents()) {
            c.setFont(new java.awt.Font("Dialog", 0, 10));
            if ((int) (i / 4.0) == (double) i / 4.0) {
                c.setFont(new java.awt.Font("Dialog", Font.BOLD, 10));
            }
            i++;
        }

        panel.getComponent(1).setFont(new java.awt.Font("Dialog", Font.BOLD, 10));
        panel.getComponent(2).setFont(new java.awt.Font("Dialog", Font.BOLD, 10));

        panel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        panel.setPreferredSize(new Dimension(120, 120));

        this.jPanel_main_right.add(panel);

    /*
    plotter.jpgtext(""+this.round(phcf.getHeader().getInitialBaryPeriod()*1000,1e9),0.21f,0.81f);
    plotter.jpgtext(""+this.round(1.0/phcf.getHeader().getInitialBaryPeriod(),1e9),0.21f,0.72f);
    plotter.jpgtext(""+this.round(phcf.getHeader().getInitialTopoPeriod()*1000,1e9),0.21f,0.63f);
    plotter.jpgtext(""+this.round(1.0/phcf.getHeader().getInitialTopoPeriod(),1e9),0.21f,0.54f);
    if(phcf.getHeader().getInitialPdot() == -1) plotter.jpgtext("N/A",0.21f,0.45f);
    else plotter.jpgtext(fmt.format(phcf.getHeader().getInitialPdot(),new StringBuffer(),new FieldPosition(0)).toString(),0.21f,0.45f);
    if(phcf.getHeader().getInitialPddot() == -1) plotter.jpgtext("N/A",0.21f,0.36f);
    else plotter.jpgtext(fmt.format(phcf.getHeader().getInitialPddot(),new StringBuffer(),new FieldPosition(0)).toString(),0.21f,0.36f);
    plotter.jpgtext(""+this.round(phcf.getHeader().getInitialDm(),100.0),0.21f,0.27f);
    plotter.jpgtext(""+this.round(phcf.getHeader().getInitialWidth(),100.0),0.21f,0.18f);
    plotter.jpgtext(""+this.round(phcf.getHeader().getInitialSNR(),100.0),0.21f,0.09f);
     **/
    }

    private String round(double d, double r) {
        if (Double.isNaN(d)) {
            return "N/A";
        } else {
            return Double.toString(((long) (d * r)) / r);
        }
    }

    private String shortStringOf(double d) {

        StringBuilder build = new StringBuilder();
        Formatter formater = new Formatter(build);
        if (d == 0) {
            build.append("0.00");
        } else if (Math.abs(d) > 1000) {
            formater.format("%5.4e", d);
        } else if (Math.abs(d) < 0.01) {
            formater.format("%5.4e", d);
        } else {
            formater.format("%3.4f", d);
        }

        return build.toString();
    }

    private void close() {
        if (this.getParent() instanceof JFrame) {
            ((JFrame) this.getParent()).setVisible(false);
            ((JFrame) this.getParent()).dispose();
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel_header = new javax.swing.JPanel();
        jLabel_header_1 = new javax.swing.JLabel();
        jLabel_header_2 = new javax.swing.JLabel();
        jLabel_header_3 = new javax.swing.JLabel();
        jPanel_main = new javax.swing.JPanel();
        jPanel_main_left = new javax.swing.JPanel();
        jPanel_main_right = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        jPanel_header.setLayout(new javax.swing.BoxLayout(jPanel_header, javax.swing.BoxLayout.Y_AXIS));

        jPanel_header.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_header.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel_headerMouseClicked(evt);
            }
        });

        jLabel_header_1.setText("jLabel1");
        jPanel_header.add(jLabel_header_1);

        jLabel_header_2.setText("jLabel2");
        jPanel_header.add(jLabel_header_2);

        jLabel_header_3.setText("jLabel1");
        jPanel_header.add(jLabel_header_3);

        add(jPanel_header, java.awt.BorderLayout.NORTH);

        jPanel_main.setLayout(new java.awt.GridLayout(1, 2));

        jPanel_main_left.setLayout(new javax.swing.BoxLayout(jPanel_main_left, javax.swing.BoxLayout.Y_AXIS));

        jPanel_main.add(jPanel_main_left);

        jPanel_main_right.setLayout(new javax.swing.BoxLayout(jPanel_main_right, javax.swing.BoxLayout.Y_AXIS));

        jPanel_main.add(jPanel_main_right);

        add(jPanel_main, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        close();
    }//GEN-LAST:event_formMouseClicked

    private void jPanel_headerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel_headerMouseClicked
        close();
    }//GEN-LAST:event_jPanel_headerMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel_header_1;
    private javax.swing.JLabel jLabel_header_2;
    private javax.swing.JLabel jLabel_header_3;
    private javax.swing.JPanel jPanel_header;
    private javax.swing.JPanel jPanel_main;
    private javax.swing.JPanel jPanel_main_left;
    private javax.swing.JPanel jPanel_main_right;
    // End of variables declaration//GEN-END:variables
}
