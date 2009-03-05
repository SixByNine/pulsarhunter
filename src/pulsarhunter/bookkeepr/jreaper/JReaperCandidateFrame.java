/*
 * JReaperCandidateFrame.java
 *
 * Created on 9 September 2008, 15:14
 */
package pulsarhunter.bookkeepr.jreaper;

import bookkeepr.xmlable.RawCandidateBasic;
import bookkeepr.xmlable.ClassifiedCandidate;
import bookkeepr.xmlable.Psrxml;
import bookkeepr.xmlable.RawCandidateMatched;
import coordlib.Coordinate;
import java.awt.BorderLayout;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import pulsarhunter.jreaper.CandClass;
import pulsarhunter.jreaper.HarmonicType;

/**
 *
 * @author  kei041
 */
public class JReaperCandidateFrame extends javax.swing.JFrame {

    private JReaper jreaper;
    private RawCandidateBasic cand;
    private Psrxml header;
    private String url;
    private int nViewsLeft = 0;

    /** Creates new form JReaperCandidateFrame */
    public JReaperCandidateFrame(RawCandidateBasic cand, Psrxml header, JReaper jreaper, String url) {
        initComponents();
        this.jreaper = jreaper;
        this.cand = cand;
        this.header = header;
        this.url = url;
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        if (screenSize.width > 2 * screenSize.height) {
            setBounds((screenSize.width / 2 - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2, this.getWidth(), this.getHeight());
        } else {
            setBounds((screenSize.width - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2, this.getWidth(), this.getHeight());
        }

        this.jComboBox_harmtype.setModel(new DefaultComboBoxModel(HarmonicType.values()));

        this.jComboBox_candClass.setModel(new DefaultComboBoxModel(CandClass.values()));
        if (this.cand.getCandlistCoordinate() != null) {

            Coordinate coord = cand.getCandlistCoordinate();
            StringBuffer newCandName = new StringBuffer();
            Formatter formatter = new Formatter(newCandName);
            char ch = '+';
            if (coord.getDec().toDegrees() < 0) {
                ch = '-';
            }
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddhhmm");
            formatter.format("CAND J%02d%02d%c%02d:%s", coord.getRA().getHours(), coord.getRA().getMinutes(), ch, Math.abs(coord.getDec().getDegrees()), dateFormat.format(date));
            this.jTextField_newCandName.setText(newCandName.toString());
        }

        ArrayList<ClassifiedCandidate> list = null;
        List<ClassifiedCandidate> list1 = jreaper.getClassifiedCandidatesMatching(cand.getId());
        if (list1 != null) {
            list = new ArrayList<ClassifiedCandidate>(list1);
        }
        List<ClassifiedCandidate> list2 = jreaper.getClassifiedCandidatesPossMatching(cand.getId());



        if (list2 != null) {
            if (list == null) {
                list = new ArrayList<ClassifiedCandidate>(list2);
            } else {
                list.addAll(list2);

            }
        }

        if (list != null) {
            this.jComboBox1.setModel(new DefaultComboBoxModel(list.toArray()));
            this.jLabel_existingMatches.setText("Existing Matches (" + list.size() + ")");
        } else {
            this.jComboBox1.setEnabled(false);
        }
        jComboBox1ItemStateChanged(null);
        this.getCandsNear();
        this.fillHeader();
    }

    void swapContents(JReaperCandidateFrame f2) {
        this.remove(this.jPanel4);
        JPanel old = this.jPanel4;
        this.jPanel4 = f2.jPanel4;
        this.add(this.jPanel4);
        f2.jPanel4 = old;
        repaint();
    }

    public int getNViewsLeft() {
        return nViewsLeft;
    }

    public void setNViewsLeft(int nViewsLeft) {
        this.nViewsLeft = nViewsLeft;
        if(nViewsLeft==0)this.jButton_close.setText("Close");
        else this.jButton_close.setText("Close ("+nViewsLeft+" Candidates left to view)");
    }

    public void getCandsNear() {
        if (cand.getCandlistCoordinate() == null) {
            jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Candidate has missing coordinate info!"}));
            jComboBox2.setEnabled(false);
            jButton3.setEnabled(false);
            return; // some protection against null pointer exceptions

        }
        double distmax;
        try {
            distmax = Double.parseDouble(this.jTextField1.getText());
        } catch (NumberFormatException ex) {
            distmax = 0.5;
        }
        List<ClassifiedCandidate> list = jreaper.getClassifiedCandidatesNear(cand.getCandlistCoordinate(), distmax);
        if (list != null) {
            this.jComboBox2.setModel(new DefaultComboBoxModel(list.toArray()));
            this.jComboBox2.setEnabled(true);
            jButton3.setEnabled(true);
        } else {
            jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"No Nearby Objects"}));
            jComboBox2.setEnabled(false);
            jButton3.setEnabled(false);
        }
        jComboBox2ItemStateChanged(null);
        repaint();
    }

    public void addDisplayPanel(JPanel displayPanel) {
        this.jPanel_plots.add(displayPanel, BorderLayout.CENTER);
    }

    private void fillHeader() {
        this.jPanel_header.add(new JLabel("Source ID"));
        this.jPanel_header.add(new JLabel(header.getSourceName()));

        this.jPanel_header.add(new JLabel("Start Coordinate"));
        this.jPanel_header.add(new JLabel(header.getStartCoordinate().toString(false)));
        this.jPanel_header.add(new JLabel(""));
        this.jPanel_header.add(new JLabel(header.getStartCoordinate().toString(true)));

        this.jPanel_header.add(new JLabel("Centre Freq Channel 1 (MHz)"));
        this.jPanel_header.add(new JLabel(String.valueOf(header.getCentreFreqFirstChannel())));

        this.jPanel_header.add(new JLabel("Channel Offset (MHz)"));
        this.jPanel_header.add(new JLabel(String.valueOf(header.getChannelOffset())));

        this.jPanel_header.add(new JLabel("Observing time actual/requested"));
        this.jPanel_header.add(new JLabel(header.getActualObsTime() + " / " + header.getRequestedObsTime()));



        this.jPanel_header.add(new JLabel("Start time (UTC)"));
        this.jPanel_header.add(new JLabel(header.getUtc()));

        this.jPanel_header.add(new JLabel("Start Az/El"));
        this.jPanel_header.add(new JLabel(header.getStartAz() + ", " + header.getStartEl()));

        this.jPanel_header.add(new JLabel("End Az/El"));
        this.jPanel_header.add(new JLabel(header.getEndAz() + ", " + header.getEndEl()));

        this.jPanel_header.add(new JLabel("Start PA"));
        this.jPanel_header.add(new JLabel(header.getStartParalacticAngle() + ", " + header.getStartParalacticAngle()));

        this.jPanel_header.add(new JLabel("End PA"));
        this.jPanel_header.add(new JLabel(header.getEndParalacticAngle() + ", " + header.getEndParalacticAngle()));

        this.jPanel_header.add(new JLabel("Telescope"));
        this.jPanel_header.add(new JLabel(header.getTelescopeIdentifyingString()));
        this.jPanel_header.add(new JLabel("Receiver"));
        this.jPanel_header.add(new JLabel(header.getReceiverIdentifyingString()));
        this.jPanel_header.add(new JLabel("Backend"));
        this.jPanel_header.add(new JLabel(header.getBackendIdentifyingString()));

        this.jPanel_header.add(new JLabel("Obs Programme"));
        this.jPanel_header.add(new JLabel(header.getObservingProgramme()));

        this.jPanel_header.add(new JLabel("Observer"));
        this.jPanel_header.add(new JLabel(header.getObserverName()));

        this.jPanel_header.add(new JLabel("Obs Type"));
        this.jPanel_header.add(new JLabel(header.getObservationType()));


    }

    public RawCandidateBasic getCand() {
        return cand;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jButton_close = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel_plots = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel_existingMatches = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel_existingType = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel_existingCandClass = new javax.swing.JLabel();
        jLabel_existingCandName = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel_period = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel_dm = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel_existingHarm = new javax.swing.JLabel();
        jButton_existingConfButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextField_newCandName = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jComboBox_candClass = new javax.swing.JComboBox();
        jButton2 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jTextField1 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel_dm1 = new javax.swing.JLabel();
        jLabel_period1 = new javax.swing.JLabel();
        jLabel_existingCandName1 = new javax.swing.JLabel();
        jLabel_existingCandClass1 = new javax.swing.JLabel();
        jLabel_existingType1 = new javax.swing.JLabel();
        jComboBox_harmtype = new javax.swing.JComboBox();
        jLabel_nearbyPossHarmonic = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel_header = new javax.swing.JPanel();
        jButton_headerBrowser = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jButton_close.setText("Close");
        jButton_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_closeActionPerformed(evt);
            }
        });
        jPanel2.add(jButton_close, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

        jTabbedPane1.setFont(new java.awt.Font("Tahoma", 0, 14));

        jPanel_plots.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Candidate Plots", jPanel_plots);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Existing Matches"));

        jLabel_existingMatches.setText("Existing Matches:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Associated Classified Candidates" }));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Type:");

        jLabel_existingType.setText("-");

        jLabel4.setText("Name:");

        jLabel5.setText("Class:");

        jLabel_existingCandClass.setText("-");

        jLabel_existingCandName.setText("-");

        jLabel3.setText("Period (ms):");

        jLabel_period.setText("-");

        jLabel7.setText("DM (cm-3pc):");

        jLabel_dm.setText("-");

        jLabel18.setText("Harmonic:");

        jLabel_existingHarm.setText("-");

        jButton_existingConfButton.setText("Confirm Association");
        jButton_existingConfButton.setEnabled(false);
        jButton_existingConfButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_existingConfButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButton_existingConfButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel18, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel5)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel4)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_existingMatches, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBox1, 0, 819, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_existingCandClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_existingCandName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_dm, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_period, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_existingType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_existingHarm, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(188, 188, 188))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel_existingMatches))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jLabel_existingType))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jLabel_existingCandClass))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jLabel_existingCandName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jLabel_period))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jLabel_dm))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel18)
                    .add(jLabel_existingHarm))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton_existingConfButton)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Create New Classified Candidate"));

        jLabel6.setText("Name of Candidate");

        jTextField_newCandName.setText("jTextField1");

        jLabel8.setText("Candidate Class:");

        jComboBox_candClass.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton2.setText("Classify Candidate");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel8)
                            .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 161, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTextField_newCandName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE)
                            .add(jComboBox_candClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 155, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jButton2))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField_newCandName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel8)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(jComboBox_candClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton2)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Nearby Classified Candidates/Known Psrs"));

        jLabel9.setText("Nearby Objects:");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Nearby Objects" }));
        jComboBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox2ItemStateChanged(evt);
            }
        });

        jTextField1.setText("0.5");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel10.setText("Max Seperation (Degrees)");

        jButton1.setText("Recompute");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel11.setText("Type:");

        jLabel12.setText("Class:");

        jLabel13.setText("Name:");

        jLabel14.setText("Period (ms):");

        jLabel15.setText("DM (cm-3pc):");

        jLabel_dm1.setText("-");

        jLabel_period1.setText("-");

        jLabel_existingCandName1.setText("-");

        jLabel_existingCandClass1.setText("-");

        jLabel_existingType1.setText("-");

        jComboBox_harmtype.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel_nearbyPossHarmonic.setText("-");

        jLabel16.setText("Harmonic:");

        jLabel17.setText("Harmonic Type:");

        jButton3.setText("Attach this Candidate");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel6Layout.createSequentialGroup()
                                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jButton1))
                            .add(jComboBox2, 0, 825, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel6Layout.createSequentialGroup()
                                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel12)
                                    .add(jLabel13)
                                    .add(jLabel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                                    .add(jLabel15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(jPanel6Layout.createSequentialGroup()
                                .add(jLabel16)
                                .add(28, 28, 28)))
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel6Layout.createSequentialGroup()
                                .add(jLabel_nearbyPossHarmonic, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 118, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jLabel17)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jComboBox_harmtype, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 243, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_existingCandClass1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_existingCandName1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_dm1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_period1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel_existingType1))
                                .add(724, 724, 724))))
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jButton3)
                        .addContainerGap(845, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)
                    .add(jButton1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(jLabel_existingType1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(jLabel_existingCandClass1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(jLabel_existingCandName1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(jLabel_period1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel15)
                    .add(jLabel_dm1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel16)
                    .add(jLabel_nearbyPossHarmonic)
                    .add(jLabel17)
                    .add(jComboBox_harmtype, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton3)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Candidate Classification", jPanel1);

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1009, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 735, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Scores", jPanel7);

        jPanel_header.setLayout(new java.awt.GridLayout(18, 2));

        jButton_headerBrowser.setText("View in Browser");
        jButton_headerBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_headerBrowserActionPerformed(evt);
            }
        });
        jPanel_header.add(jButton_headerBrowser);

        jButton5.setText("jButton5");
        jPanel_header.add(jButton5);

        jTabbedPane1.addTab("Observation", jPanel_header);

        jPanel4.add(jTabbedPane1);

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        setBounds(0, 0, 1024, 819);
    }// </editor-fold>//GEN-END:initComponents

private void jButton_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_closeActionPerformed

    if (nViewsLeft <= 0) {
        this.setVisible(false);
        this.dispose();
    } else {
        this.setNViewsLeft(nViewsLeft-1);
    }


}//GEN-LAST:event_jButton_closeActionPerformed

private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextField1ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

    this.getCandsNear();
}//GEN-LAST:event_jButton1ActionPerformed

private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
}//GEN-LAST:event_jComboBox1ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

    this.setVisible(false);
    this.dispose();
    new Thread() {

        @Override
        public void run() {
            super.run();
            try {
                jreaper.classifyNewCandidate(cand, jTextField_newCandName.getText(), (CandClass) jComboBox_candClass.getSelectedItem());
            } catch (BookKeeprCommunicationException ex) {
                Logger.getLogger(JReaperCandidateFrame.class.getName()).log(Level.SEVERE, "Could not classify candidate!", ex);
            }
        }
    }.start();

}//GEN-LAST:event_jButton2ActionPerformed

private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
// TODO add your handling code here:
    Object o = this.jComboBox1.getSelectedItem();
    if (o instanceof ClassifiedCandidate) {
        ClassifiedCandidate c = (ClassifiedCandidate) o;

        RawCandidateMatched m = c.getRawCandidateMatched(this.cand.getId());
        if (m != null) {
            if (m.getConfirmed()) {
                this.jLabel_existingType.setText(m.getHarmType() + " (Confirmed)");
                this.jButton_existingConfButton.setEnabled(false);
            } else {
                this.jLabel_existingType.setText(m.getHarmType() + " (Possible)");
                this.jButton_existingConfButton.setEnabled(true);
            }
        }



        this.jLabel_existingCandClass.setText(c.getCandClass().toString());
        this.jLabel_existingCandName.setText(c.getName());
        this.jLabel_period.setText(String.valueOf(c.getPreferedCandidate().getBaryPeriod() * 1000.0) + "  (" + String.valueOf(this.cand.getBaryPeriod() / c.getPreferedCandidate().getBaryPeriod()) + ")");

        this.jLabel_dm.setText(String.valueOf(c.getPreferedCandidate().getDm()));

        double period1 = this.cand.getBaryPeriod();
        double period2 = c.getPreferedCandidate().getBaryPeriod();
        double eta = 0.001;
        if (Math.abs(period1 - period2) < eta * period2) {

            this.jLabel_existingHarm.setText("1 (Fundimental)");
//            this.jComboBox_harmtype.setSelectedItem(HarmonicType.Principal);
            return;
        }

        for (int intFactor = 2; intFactor < 16; intFactor++) {
            if (Math.abs(period1 - period2 * intFactor) < eta * period2 * intFactor) {

                this.jLabel_existingHarm.setText("1/" + intFactor);
//                this.jComboBox_harmtype.setSelectedItem(HarmonicType.Integer);

                return;

            }
        }


        for (int bottomfactor = 1; bottomfactor <= 16; bottomfactor++) {
            for (int topfactor = 1; topfactor < 16; topfactor++) {
                double factor = ((double) topfactor) / ((double) bottomfactor);
                //System.out.println(topfactor +"/"+bottomfactor+" "+factor+" "+ Math.abs(masterData[i][j].getPeriod()*factor - period));
                if (Math.abs(period1 - period2 * factor) < eta * period2 * factor) {

                    this.jLabel_existingHarm.setText(topfactor + "/" + bottomfactor);
                    if (topfactor == 1) {
//                        this.jComboBox_harmtype.setSelectedItem(HarmonicType.SimpleNonInteger);
                    } else {
//                        this.jComboBox_harmtype.setSelectedItem(HarmonicType.ComplexNonInteger);
                    }
                    return;
                }
            }
        }

    }
}//GEN-LAST:event_jComboBox1ItemStateChanged

private void jComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox2ItemStateChanged
// TODO add your handling code here:


    Object o = this.jComboBox2.getSelectedItem();
    if (o instanceof ClassifiedCandidate) {
        ClassifiedCandidate c = (ClassifiedCandidate) o;
        this.jLabel_existingCandClass1.setText(c.getCandClass().toString());
        this.jLabel_existingCandName1.setText(c.getName());
        this.jLabel_period1.setText(String.valueOf(c.getPreferedCandidate().getBaryPeriod() * 1000.0) + "  (" + String.valueOf(this.cand.getBaryPeriod() / c.getPreferedCandidate().getBaryPeriod()) + ")");
        this.jLabel_dm1.setText(String.valueOf(c.getPreferedCandidate().getDm()));
        double period1 = this.cand.getBaryPeriod();
        double period2 = c.getPreferedCandidate().getBaryPeriod();
        double eta = 0.001;
        if (Math.abs(period1 - period2) < eta * period2) {



            this.jLabel_nearbyPossHarmonic.setText("1");
            this.jComboBox_harmtype.setSelectedItem(HarmonicType.Principal);
            return;
        }

        for (int intFactor = 2; intFactor < 16; intFactor++) {
            if (Math.abs(period1 - period2 * intFactor) < eta * period2 * intFactor) {

                this.jLabel_nearbyPossHarmonic.setText("1/" + intFactor);
                this.jComboBox_harmtype.setSelectedItem(HarmonicType.Integer);

                return;

            }
        }


        for (int bottomfactor = 1; bottomfactor <= 16; bottomfactor++) {
            for (int topfactor = 1; topfactor < 16; topfactor++) {
                double factor = ((double) topfactor) / ((double) bottomfactor);
                //System.out.println(topfactor +"/"+bottomfactor+" "+factor+" "+ Math.abs(masterData[i][j].getPeriod()*factor - period));
                if (Math.abs(period1 - period2 * factor) < eta * period2 * factor) {

                    this.jLabel_nearbyPossHarmonic.setText(topfactor + "/" + bottomfactor);
                    if (topfactor == 1) {
                        this.jComboBox_harmtype.setSelectedItem(HarmonicType.SimpleNonInteger);
                    } else {
                        this.jComboBox_harmtype.setSelectedItem(HarmonicType.ComplexNonInteger);
                    }
                    return;
                }
            }
        }
        this.jLabel_nearbyPossHarmonic.setText("???");
        this.jComboBox_harmtype.setSelectedItem(HarmonicType.None);
    }


}//GEN-LAST:event_jComboBox2ItemStateChanged

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
// TODO add your handling code here:
    this.setVisible(false);
    this.dispose();
    new Thread() {

        @Override
        public void run() {
            super.run();
            try {
                jreaper.classifyToExistingCandidate((ClassifiedCandidate) jComboBox2.getSelectedItem(), cand, (HarmonicType) jComboBox_harmtype.getSelectedItem());

            } catch (BookKeeprCommunicationException ex) {
                Logger.getLogger(JReaperCandidateFrame.class.getName()).log(Level.SEVERE, "Could not classify candidate!", ex);
            }
        }
    }.start();
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton_existingConfButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_existingConfButtonActionPerformed
    this.setVisible(false);
    Object o = this.jComboBox1.getSelectedItem();
    final ClassifiedCandidate c = (ClassifiedCandidate) o;

    RawCandidateMatched m = c.getRawCandidateMatched(this.cand.getId());


    this.dispose();
    if (m != null) {
        final HarmonicType ht = m.getHarmonicType();

        new Thread() {

            @Override
            public void run() {
                super.run();


                try {

                    jreaper.classifyToExistingCandidate(c, cand, ht);
                } catch (BookKeeprCommunicationException ex) {
                    Logger.getLogger(JReaperCandidateFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }.start();

    }
}//GEN-LAST:event_jButton_existingConfButtonActionPerformed

private void jButton_headerBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_headerBrowserActionPerformed
    try {
        Browser.openUrl(url);
    } catch (IOException ex) {
        Logger.getLogger(JReaperCandidateFrame.class.getName()).log(Level.SEVERE, "Couldn't open your browser, sorry!", ex);
    }
}//GEN-LAST:event_jButton_headerBrowserActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton_close;
    private javax.swing.JButton jButton_existingConfButton;
    private javax.swing.JButton jButton_headerBrowser;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox_candClass;
    private javax.swing.JComboBox jComboBox_harmtype;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_dm;
    private javax.swing.JLabel jLabel_dm1;
    private javax.swing.JLabel jLabel_existingCandClass;
    private javax.swing.JLabel jLabel_existingCandClass1;
    private javax.swing.JLabel jLabel_existingCandName;
    private javax.swing.JLabel jLabel_existingCandName1;
    private javax.swing.JLabel jLabel_existingHarm;
    private javax.swing.JLabel jLabel_existingMatches;
    private javax.swing.JLabel jLabel_existingType;
    private javax.swing.JLabel jLabel_existingType1;
    private javax.swing.JLabel jLabel_nearbyPossHarmonic;
    private javax.swing.JLabel jLabel_period;
    private javax.swing.JLabel jLabel_period1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel_header;
    private javax.swing.JPanel jPanel_plots;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField_newCandName;
    // End of variables declaration//GEN-END:variables
}
