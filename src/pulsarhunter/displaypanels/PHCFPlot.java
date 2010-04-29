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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.Formatter;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import pulsarhunter.Convert;
import pulsarhunter.datatypes.PHCSection;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.displaypanels.MKPlot.LineStyle;
import pulsarhunter.jreaper.Colourmap;
import pulsarhunter.processes.DisplayPHCF;

/**
 *
 * @author  mkeith
 */
public class PHCFPlot extends javax.swing.JPanel {

    private PulsarHunterCandidate phcf;
    private Colourmap colourmap;
    private Color c1;
    private Color c2;

    /** Creates new form PHCFPlot */
    public PHCFPlot(PulsarHunterCandidate phcf, Colourmap colourmap, Color c1, Color c2) {
        this.colourmap = colourmap;
        this.c1 = c1;
        this.c2 = c2;
        initComponents();
        this.phcf = phcf;
        init();
        this.validate();
    }

    public void init() {

        StringBuffer line = new StringBuffer();
        line.append("File: " + phcf.getFile().getName());
        line.append("   RA:" + phcf.getHeader().getCoord().getRA().toString(false));
        line.append("   Dec:" + phcf.getHeader().getCoord().getDec().toString(false));

        line.append("   Gl:" + round(phcf.getHeader().getCoord().getGl(), 100));
        line.append("   Gb:" + round(phcf.getHeader().getCoord().getGb(), 100));
        line.append("   MJD:" + round(phcf.getHeader().getMjdStart(), 100));




        this.jLabel_header_1.setText(line.toString());

        line = new StringBuffer();

        line.append("ObsFreq:" + round(phcf.getHeader().getFrequency(), 10) + "MHz");
        line.append("   Tobs:" + round(phcf.getHeader().getTobs(), 1) + "s");
        if (phcf.getInitialSec().getTsamp() > 0) {
            line.append("   Tsamp:" + (int) (phcf.getHeader().getTsamp()) + "us");
        }
        line.append("   SourceID:" + phcf.getHeader().getSourceID());
        line.append("   Telescope:" + phcf.getHeader().getTelescope().toString());


        this.jLabel_header_2.setText(line.toString());

        line = new StringBuffer();


        if (phcf.getInitialSec().getExtraValue("SPECSNR") != null) {
            line.append("SpecSNR:" + phcf.getInitialSec().getExtraValue("SPECSNR"));
        }
        if (phcf.getInitialSec().getExtraValue("Recon") != null || phcf.getInitialSec().getExtraValue("RECONSNR") != null) {
            if (phcf.getInitialSec().getExtraValue("RECONSNR") != null) {
                line.append("   ReconSNR:" + round(Double.parseDouble(phcf.getInitialSec().getExtraValue("RECONSNR")), 100));
            } else {
                line.append("   ReconSNR:" + round(Double.parseDouble(phcf.getInitialSec().getExtraValue("Recon")), 100));
            }
        }
        if (phcf.getInitialSec().getExtraValue("HFOLD") != null) {
            line.append("   H-Fold:" + phcf.getInitialSec().getExtraValue("HFOLD"));
        }

        if (phcf.getHeader().getExtraValue("ZAP") != null) {
            line.append("   Zap:" + phcf.getHeader().getExtraValue("ZAP"));
        }


        this.jLabel_header_3.setText(line.toString());


        String[] sections = phcf.listSections().toArray(new String[]{});

        JPanel[] profilePanels = new JPanel[sections.length];
        JPanel[] pdmPanels = new JPanel[sections.length];
        JPanel[] sintPanels = new JPanel[sections.length];
        JPanel[] sbandPanels = new JPanel[sections.length];
        JPanel[] dmCurvePanels = new JPanel[sections.length];
        JPanel[] pdotCurvePanels = new JPanel[sections.length];

        for (int i = 0; i < sections.length; i++) {

            PHCSection sec = phcf.getSection(sections[i]);

            /****************
             *   Profiles   *
             ****************/
            {
                if (sec.getPulseProfile() != null) {
                    int nbins = sec.getPulseProfile().length;
                    double[] yaxis = Convert.wrapDoubleArr(sec.getPulseProfile(), 1.5);
                    double[] xaxis = new double[yaxis.length];
                    for (int j = 0; j < xaxis.length; j++) {
                        xaxis[j] = (double) j / (double) sec.getPulseProfile().length;
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


                        int numP = phcf.getNPulses();
                        xpoints = new double[numP];
                        ypoints = new double[numP];
                        double spacing = (double) nbins / (double) numP;
                        for (int j = 1; j <= numP; j++) {
                            int bin = (int) (spacing * j) + maxposn;
                            while (bin >= nbins) {
                                bin -= nbins;
                            }

                            xpoints[j - 1] = bin / ((double) nbins);
                            ypoints[j - 1] = 0;
                        }
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
                        double[] yaxis = sec.getSnrBlock().getPeriodCurve(sec.getBestDm(), sec.getBestAccn(), sec.getBestJerk());
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



                        double[] model = Convert.generatePeriodCurve(xaxis, period, sec.getBestWidth(), phcf.getHeader().getTobs());

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
                if (sec.getSubints() != null && sec.getSubints().length > 1) {
                    double[][] map_d = Convert.wrapDoubleArr(Convert.rotateDoubleArray(sec.getSubints()), 1.5);
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
                if (sec.getSubbands() != null && sec.getSubbands().length > 1) {
                    double[][] map_d = Convert.wrapDoubleArr(Convert.rotateDoubleArray(sec.getSubbands()), 1.5);

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
                    if (width < 0) {
                        width = phcf.getOptimisedSec().getBestWidth();
                    }


                    double[] model = Convert.generateDmCurve(xaxis, period, width, sec.getBestDm(), phcf.getHeader().getBandwidth(), phcf.getHeader().getFrequency());
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
        panel.add(new JLabel("" + this.round(phcf.getHeader().getInitialBaryPeriod() * 1000, 1e9)));
        panel.add(new JLabel("" + this.round(phcf.getHeader().getOptimisedBaryPeriod() * 1000, 1e9)));
        panel.add(new JLabel("ms"));

        panel.add(new JLabel("Bary Freq"));
        panel.add(new JLabel(this.round(1.0 / phcf.getHeader().getInitialBaryPeriod(), 1e9)));
        panel.add(new JLabel(this.round(1.0 / phcf.getHeader().getOptimisedBaryPeriod(), 1e9)));
        panel.add(new JLabel("Hz"));

        panel.add(new JLabel("Topo Period"));
        panel.add(new JLabel(this.round(phcf.getHeader().getInitialTopoPeriod() * 1000, 1e9)));
        panel.add(new JLabel(this.round(phcf.getHeader().getOptimisedTopoPeriod() * 1000, 1e9)));
        panel.add(new JLabel("ms"));


        panel.add(new JLabel("Topo Freq"));
        panel.add(new JLabel("" + this.round(1.0 / phcf.getHeader().getInitialTopoPeriod(), 1e9)));
        panel.add(new JLabel("" + this.round(1.0 / phcf.getHeader().getOptimisedTopoPeriod(), 1e9)));
        panel.add(new JLabel("Hz"));

        panel.add(new JLabel("Accn"));
        panel.add(new JLabel(this.shortStringOf(phcf.getHeader().getInitialAccn())));
        panel.add(new JLabel(this.shortStringOf(phcf.getHeader().getOptimisedAccn())));
        panel.add(new JLabel("m/s/s"));


        panel.add(new JLabel("Jerk"));
        panel.add(new JLabel(this.shortStringOf(phcf.getHeader().getInitialJerk())));
        panel.add(new JLabel(this.shortStringOf(phcf.getHeader().getOptimisedJerk())));
        panel.add(new JLabel("m/s/s/s"));

        panel.add(new JLabel("DM"));
        panel.add(new JLabel("" + this.round(phcf.getHeader().getInitialDm(), 100.0)));
        panel.add(new JLabel("" + this.round(phcf.getHeader().getOptimizedDm(), 100.0)));
        panel.add(new JLabel("cm/pc"));

        panel.add(new JLabel("Width"));
        panel.add(new JLabel("" + this.round(phcf.getHeader().getInitialWidth(), 100.0)));
        panel.add(new JLabel("" + this.round(phcf.getHeader().getOptimizedWidth(), 100.0)));
        panel.add(new JLabel("periods"));

        panel.add(new JLabel("SNR"));
        panel.add(new JLabel("" + this.round(phcf.getHeader().getInitialSNR(), 100.0)));
        panel.add(new JLabel("" + this.round(phcf.getHeader().getOptimizedSNR(), 100.0)));
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
        if (d == -1) {
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_header = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel_header_1 = new javax.swing.JLabel();
        jLabel_header_2 = new javax.swing.JLabel();
        jLabel_header_3 = new javax.swing.JLabel();
        jButton_image = new javax.swing.JButton();
        jPanel_main = new javax.swing.JPanel();
        jPanel_main_left = new javax.swing.JPanel();
        jPanel_main_right = new javax.swing.JPanel();

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        jPanel_header.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_header.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel_headerMouseClicked(evt);
            }
        });
        jPanel_header.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jLabel_header_1.setText("jLabel1");
        jPanel1.add(jLabel_header_1);

        jLabel_header_2.setText("jLabel2");
        jPanel1.add(jLabel_header_2);

        jLabel_header_3.setText("jLabel1");
        jPanel1.add(jLabel_header_3);

        jPanel_header.add(jPanel1, java.awt.BorderLayout.CENTER);

        jButton_image.setText("Image");
        jButton_image.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_imageActionPerformed(evt);
            }
        });
        jPanel_header.add(jButton_image, java.awt.BorderLayout.LINE_END);

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

    private void jButton_imageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_imageActionPerformed

        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File(this.phcf.getName() + ".png"));
        int returnVal = chooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            DisplayPHCF displayer = new DisplayPHCF(phcf, true);
            displayer.setOutputFileName(chooser.getSelectedFile().getAbsolutePath());
            displayer.run();
        }

}//GEN-LAST:event_jButton_imageActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_image;
    private javax.swing.JLabel jLabel_header_1;
    private javax.swing.JLabel jLabel_header_2;
    private javax.swing.JLabel jLabel_header_3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_header;
    private javax.swing.JPanel jPanel_main;
    private javax.swing.JPanel jPanel_main_left;
    private javax.swing.JPanel jPanel_main_right;
    // End of variables declaration//GEN-END:variables
}
