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
 * CandidateDisplayFrame.java
 *
 * Created on 29 January 2007, 14:47
 */
package pulsarhunter.jreaper.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.Score;

/**
 *
 * @author  mkeith
 */
public class CandidateDisplayFrame extends javax.swing.JFrame {

    Cand cand;


    JPanel displayPanel;
    MainView master;
    private int nremaining = 0;
    boolean closed = false;

    /** Creates new form CandidateDisplayFrame */
    public CandidateDisplayFrame(MainView master, Cand cand, JPanel displayPanel) {
        initComponents();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        if (screenSize.width > 2 * screenSize.height) {
            setBounds((screenSize.width / 2 - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2, this.getWidth(), this.getHeight());
        } else {
            setBounds((screenSize.width - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2, this.getWidth(), this.getHeight());
        }
        this.master = master;
        setup(cand, displayPanel);
    }

    public void swap(CandidateDisplayFrame cdf) {
        this.remove(this.displayPanel);
        this.setup(cdf.cand, cdf.displayPanel);
    }

    public void setNremaining(int nremaining) {
        this.nremaining = nremaining;
        if (nremaining != 0) {
            this.jButton1.setText("Next (" + nremaining + ")");
        } else {
            this.jButton1.setText("Close");
        }
    }
public Cand getCand() {
        return cand;
    }
    private void setup(final Cand cand, final JPanel displayPanel) {


        Runnable run = new Runnable() {

            public void run() {
                CandidateDisplayFrame.this.cand = cand;
                if (CandidateDisplayFrame.this.displayPanel != null) {
                    CandidateDisplayFrame.this.remove(CandidateDisplayFrame.this.displayPanel);
                }


                CandidateDisplayFrame.this.displayPanel = displayPanel;


                CandidateDisplayFrame.this.add(displayPanel, BorderLayout.CENTER);
                CandidateDisplayFrame.this.jToggleButton1.setSelected(cand.isDud());

                noclassbutton.setEnabled(true);



                if (cand.getCandClass() < 1) {
                    noclassbutton.setEnabled(false);
                }
                //this.jLabel_header1.setText("Name: "+cand.getName()+"  Period: "+cand.getPeriod()+"  DM: "+cand.getDM()+"  Score: "+cand.getScore());
                StringWriter stringWriter = new StringWriter();
                PrintWriter out = new PrintWriter(stringWriter);
                out.printf("Name: %s  Period: %10.4f  DM: %5.1f  Score: %5.2f ", cand.getName(), cand.getPeriod(), cand.getDM(), cand.getScore());
                CandidateDisplayFrame.this.jLabel_header1.setText(stringWriter.toString());

                if (cand.getDetectionList() != null) {
                    CandidateDisplayFrame.this.jComboBox_history.setModel(new DefaultComboBoxModel(cand.getDetectionList().toArray()));
                } else {
                    CandidateDisplayFrame.this.jComboBox_history.setEnabled(false);
                }

                if (cand.getComments() != null) {
                    CandidateDisplayFrame.this.jComboBox_comments.setModel(new DefaultComboBoxModel(cand.getComments().toArray()));
                }

                CandidateDisplayFrame.this.validate();
                CandidateDisplayFrame.this.repaint();
            }
        };

        SwingUtilities.invokeLater(run);

    }

    private void done() {
        if (this.nremaining == 0) {
            this.setVisible(false);
            this.dispose();
            closed = true;
        } else {
            closed = true;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel_header1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        class1button = new javax.swing.JButton();
        noclassbutton = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jButton_showScores = new javax.swing.JButton();
        jButton_showHarms = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jComboBox_history = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jComboBox_comments = new javax.swing.JComboBox();

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                click(evt);
            }
        });

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jLabel_header1.setText("jLabel3");
        jPanel2.add(jLabel_header1);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new java.awt.GridLayout(2, 1));

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        class1button.setText("Re-Classify");
        class1button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                class1buttonActionPerformed(evt);
            }
        });
        jPanel1.add(class1button);

        noclassbutton.setText("De-Classify");
        noclassbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noclassbuttonActionPerformed(evt);
            }
        });
        jPanel1.add(noclassbutton);

        jToggleButton1.setText("Add to RFI List");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jToggleButton1);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel1.add(jSeparator1);

        jLabel4.setText("Show:");
        jPanel1.add(jLabel4);

        jButton_showScores.setText("Scores");
        jButton_showScores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_showScoresActionPerformed(evt);
            }
        });
        jPanel1.add(jButton_showScores);

        jButton_showHarms.setText("Harmonics");
        jButton_showHarms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_showHarmsActionPerformed(evt);
            }
        });
        jPanel1.add(jButton_showHarms);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel1.add(jSeparator2);

        jLabel1.setText("Select Beams:");
        jPanel1.add(jLabel1);

        jButton5.setText("Toggle This");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton5);

        jButton6.setText("Only This");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton6);

        jButton7.setText("All But This");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton7);

        jPanel3.add(jPanel1);

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        jButton1.setText("Close");
        jButton1.setFocusCycleRoot(true);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton1);

        jLabel3.setText("Detections:");
        jPanel4.add(jLabel3);

        jComboBox_history.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None" }));
        jPanel4.add(jComboBox_history);

        jLabel5.setText("Comments:");
        jPanel4.add(jLabel5);

        jComboBox_comments.setEditable(true);
        jComboBox_comments.setMaximumRowCount(20);
        jComboBox_comments.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Type Here to Add" }));
        jComboBox_comments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_commentsActionPerformed(evt);
            }
        });
        jPanel4.add(jComboBox_comments);

        jPanel3.add(jPanel4);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-1024)/2, (screenSize.height-768)/2, 1024, 768);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox_commentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_commentsActionPerformed
        if (evt.getActionCommand().equalsIgnoreCase("comboBoxEdited")) {
            this.cand.addComment(this.jComboBox_comments.getSelectedItem().toString());
            if (cand.getComments() != null) {
                this.jComboBox_comments.setModel(new DefaultComboBoxModel(cand.getComments().toArray()));
            }
        }
    }//GEN-LAST:event_jComboBox_commentsActionPerformed

    private void jButton_showHarmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_showHarmsActionPerformed
        new MessageBox("Sorry...\nFunction not implemented (yet)").setVisible(true);
    }//GEN-LAST:event_jButton_showHarmsActionPerformed

    private void jButton_showScoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_showScoresActionPerformed
        Score s = cand.getScoreObject();
        if (s != null) {
            new ScoreDisplayFrame(cand, s).setVisible(true);
        } else {
            new MessageBox("No score object associated with this candidate").setVisible(true);
        }
    }//GEN-LAST:event_jButton_showScoresActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        done();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void click(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_click
        done();
    }//GEN-LAST:event_click

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        master.selectAllBeams();
        master.toggleSpecificBeam(cand.getBeam().getName());
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        master.selectNoBeams();
        master.toggleSpecificBeam(cand.getBeam().getName());
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        master.toggleSpecificBeam(cand.getBeam().getName());
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
//        cand.addComment("Candidate Declasified");
//        class1button.setEnabled(true);
//        class2button.setEnabled(true);
//        class3button.setEnabled(true);
//        noclassbutton.setEnabled(false);
//        Thread task = new Thread(){
//            public void run(){
//                if(jToggleButton1.isSelected()) master.findHarmonics(cand,4);
//                else master.findHarmonics(cand,5);
//                master.replot();
//            }
//        };
//        task.start();
//        

        new AddToZapFile(master, 1000.0 / this.cand.getPeriod(), 0.001, 10, 10).setVisible(true);


        done();
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void noclassbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noclassbuttonActionPerformed
        cand.addComment("Candidate Declasified");
        noclassbutton.setEnabled(false);
        Thread task = new Thread() {

            public void run() {
                master.findHarmonics(cand, -1, cand.getCandidateFile().getUniqueIdentifier());
                master.replot();
            }
        };
        task.start();
        done();
    }//GEN-LAST:event_noclassbuttonActionPerformed

    private void class1buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_class1buttonActionPerformed

        new MakeCandidatePanel(cand, master).setVisible(true);

        done();
    }//GEN-LAST:event_class1buttonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton class1button;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton_showHarms;
    private javax.swing.JButton jButton_showScores;
    private javax.swing.JComboBox jComboBox_comments;
    private javax.swing.JComboBox jComboBox_history;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel_header1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton noclassbutton;
    // End of variables declaration//GEN-END:variables
}
