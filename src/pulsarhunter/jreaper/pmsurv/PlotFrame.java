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
 * TestFrame.java
 *
 * Created on 24 May 2005, 17:12
 */

package pulsarhunter.jreaper.pmsurv;

import javax.swing.JLabel;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.HarmonicType;
import pulsarhunter.jreaper.gui.MainView;




/**
 *
 * @author  mkeith
 */
public class PlotFrame extends javax.swing.JFrame {
    
    public PlotFrame(){}
    
    private PulsarCandFile phfile;
    private Cand cand;
    private MainView master;
    /** Creates new form TestFrame */
    public PlotFrame(final Cand candidate,final MainView master) {
        initComponents();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                
                PlotFrame.this.master = master;
                PlotFrame.this.cand = candidate;
                
                PlotFrame.this.phfile = (PulsarCandFile)candidate.getCandidateFile();
                jLabel1.setText(candidate.getComment());
                jLabel2.setText("Location: "+candidate.getBeam().getCoord().toString());
                
                //infoBox.setText(phfile.getText());
                commentHistory.setText(candidate.getCommentHistory());
                switch(candidate.getCandClass()){
                    case 0:
                        if(cand.getHarmonicType()==HarmonicType.Principal){
                            class1button.setEnabled(false);
                            class2button.setEnabled(false);
                            class3button.setEnabled(false);
                            noclassbutton.setEnabled(false);
                        }
                        break;
                    case 1:
                        class1button.setEnabled(false);
                        break;
                    case 2:
                        class2button.setEnabled(false);
                        break;
                    case 3:
                        class3button.setEnabled(false);
                        break;
                    case 4:
                        jToggleButton1.setSelected(true);
                        break;
                    default:
                        noclassbutton.setEnabled(false);
                }
                
                phfile.read();
                //jLabel2.setText(phfile.getText());
                
                FrequencyPanel frequencyPanel = new FrequencyPanel(phfile,candidate,1.5);
                
                
                
                freqPhasePanel.add(frequencyPanel, java.awt.BorderLayout.CENTER);
                ProfilePanel profilePanel = new ProfilePanel(candidate,1.5);
                profPanel.add(profilePanel, java.awt.BorderLayout.CENTER);
                
                DMCurvePanel dmCurvePanel = new DMCurvePanel(candidate);
                dmPanel.add(dmCurvePanel, java.awt.BorderLayout.CENTER);
                
                
                //SubintPanel subintPanel = new SubintPanel(phfile,1.5);
                //timePhasePanel.add(subintPanel, java.awt.BorderLayout.CENTER);
                double snr = cand.getSNR();
                int subints = 64;
                if(snr < 12) subints /= 2;
                if(snr < 10) subints /= 2;
                if(snr < 8) subints /= 2;
                jPanel2.add(new SubintPanel(phfile,cand,1.5,subints));
                jPanel2.add(new PDMFrame(phfile,candidate));
                
                
                String[] headers = phfile.getHeaders();
                for(String header : headers){
                    JLabel headerLabel = new JLabel();
                    headerLabel.setText(header);
                    headerPanel.add(headerLabel);
                    
                }
                
                headers = phfile.getPDMParams();
                for(String header : headers){
                    JLabel headerLabel = new JLabel();
                    headerLabel.setText(header);
                    headerLabel.setFont(new java.awt.Font("Tahoma", 0, 10));
                    pdmHeaderPanel.add(headerLabel);
                }
                
                
                String score = cand.getScoreBreakdown();
                
                String[] lines = score.split("\n");
                
                headers = phfile.getPDMParams();
                for(String header : lines){
                    JLabel headerLabel = new JLabel();
                    headerLabel.setText(header);
                    headerLabel.setFont(new java.awt.Font("Courier New", 0, 11));
                    
                    scorePanel.add(headerLabel);
                }
                
                // repaint();
                
            }
        });
    }
    
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        class1button = new javax.swing.JButton();
        class2button = new javax.swing.JButton();
        class3button = new javax.swing.JButton();
        noclassbutton = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        commentHistory = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        centerPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        dmPanel = new javax.swing.JPanel();
        freqPhasePanel = new javax.swing.JPanel();
        profPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        pdmHeaderPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        scorePanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ClickHandler(evt);
            }
        });

        jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.Y_AXIS));

        jPanel5.setMaximumSize(new java.awt.Dimension(100, 23));
        jPanel5.setMinimumSize(new java.awt.Dimension(100, 23));
        jPanel5.setPreferredSize(new java.awt.Dimension(100, 23));
        jLabel3.setText("Cand Class");
        jPanel5.add(jLabel3);

        class1button.setText("1");
        class1button.setMaximumSize(new java.awt.Dimension(100, 23));
        class1button.setMinimumSize(new java.awt.Dimension(100, 23));
        class1button.setPreferredSize(new java.awt.Dimension(100, 23));
        class1button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                class1buttonActionPerformed(evt);
            }
        });

        jPanel5.add(class1button);

        class2button.setText("2");
        class2button.setMaximumSize(new java.awt.Dimension(100, 23));
        class2button.setMinimumSize(new java.awt.Dimension(100, 23));
        class2button.setPreferredSize(new java.awt.Dimension(100, 23));
        class2button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                class2buttonActionPerformed(evt);
            }
        });

        jPanel5.add(class2button);

        class3button.setText("3");
        class3button.setMaximumSize(new java.awt.Dimension(100, 23));
        class3button.setMinimumSize(new java.awt.Dimension(100, 23));
        class3button.setPreferredSize(new java.awt.Dimension(100, 23));
        class3button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                class3buttonActionPerformed(evt);
            }
        });

        jPanel5.add(class3button);

        noclassbutton.setText("None");
        noclassbutton.setMaximumSize(new java.awt.Dimension(100, 23));
        noclassbutton.setMinimumSize(new java.awt.Dimension(100, 23));
        noclassbutton.setPreferredSize(new java.awt.Dimension(100, 23));
        noclassbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noclassbuttonActionPerformed(evt);
            }
        });

        jPanel5.add(noclassbutton);

        jToggleButton1.setText("Dud");
        jToggleButton1.setToolTipText("Mark this candidate (and harmonics) as dud, i.e. interference etc");
        jToggleButton1.setMaximumSize(new java.awt.Dimension(100, 23));
        jToggleButton1.setMinimumSize(new java.awt.Dimension(100, 23));
        jToggleButton1.setPreferredSize(new java.awt.Dimension(100, 23));
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });
        jToggleButton1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                keyPressedListener(evt);
            }
        });

        jPanel5.add(jToggleButton1);

        jSeparator1.setMaximumSize(new java.awt.Dimension(100, 5));
        jSeparator1.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator1.setPreferredSize(new java.awt.Dimension(50, 5));
        jPanel5.add(jSeparator1);

        jLabel5.setText("Select Beams");
        jPanel5.add(jLabel5);

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton1.setText("Toggle This");
        jButton1.setMaximumSize(new java.awt.Dimension(100, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(100, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(100, 23));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel5.add(jButton1);

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton2.setText("All But This");
        jButton2.setMaximumSize(new java.awt.Dimension(100, 23));
        jButton2.setMinimumSize(new java.awt.Dimension(100, 23));
        jButton2.setPreferredSize(new java.awt.Dimension(100, 23));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel5.add(jButton2);

        jButton3.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton3.setText("Only This");
        jButton3.setMaximumSize(new java.awt.Dimension(100, 23));
        jButton3.setMinimumSize(new java.awt.Dimension(100, 23));
        jButton3.setPreferredSize(new java.awt.Dimension(100, 23));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel5.add(jButton3);

        jLabel4.setText("History");
        jPanel5.add(jLabel4);

        jScrollPane1.setMaximumSize(new java.awt.Dimension(100, 500));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 500));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(100, 500));
        commentHistory.setColumns(50);
        commentHistory.setEditable(false);
        jScrollPane1.setViewportView(commentHistory);

        jPanel5.add(jScrollPane1);

        getContentPane().add(jPanel5, java.awt.BorderLayout.EAST);

        jPanel3.setLayout(new java.awt.BorderLayout());

        centerPanel.setLayout(new java.awt.GridLayout(1, 2));

        jPanel2.setLayout(new java.awt.GridLayout(2, 1));

        centerPanel.add(jPanel2);

        jPanel1.setLayout(new java.awt.GridLayout(4, 1));

        dmPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.add(dmPanel);

        freqPhasePanel.setLayout(new java.awt.BorderLayout());

        jPanel1.add(freqPhasePanel);

        profPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.add(profPanel);

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        pdmHeaderPanel.setLayout(new javax.swing.BoxLayout(pdmHeaderPanel, javax.swing.BoxLayout.Y_AXIS));

        jLabel2.setText("PDM Header");
        pdmHeaderPanel.add(jLabel2);

        jScrollPane2.setViewportView(pdmHeaderPanel);

        jTabbedPane1.addTab("PDM header", jScrollPane2);

        scorePanel.setLayout(new javax.swing.BoxLayout(scorePanel, javax.swing.BoxLayout.Y_AXIS));

        scorePanel.setFont(new java.awt.Font("Courier New", 0, 11));
        jTabbedPane1.addTab("Score Breakdown", scorePanel);

        jPanel1.add(jTabbedPane1);

        centerPanel.add(jPanel1);

        jPanel3.add(centerPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        headerPanel.setLayout(new javax.swing.BoxLayout(headerPanel, javax.swing.BoxLayout.Y_AXIS));

        headerPanel.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("jLabel1");
        headerPanel.add(jLabel1);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-1007)/2, (screenSize.height-750)/2, 1007, 750);
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        master.selectNoBeams();
        master.toggleSpecificBeam(cand.getBeam().getName());
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        master.selectAllBeams();
        master.toggleSpecificBeam(cand.getBeam().getName());
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        master.toggleSpecificBeam(cand.getBeam().getName());
    }//GEN-LAST:event_jButton1ActionPerformed
    
    private void keyPressedListener(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyPressedListener
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_keyPressedListener
    
    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        cand.addComment("Candidate Declasified");
        class1button.setEnabled(true);
        class2button.setEnabled(true);
        class3button.setEnabled(true);
        noclassbutton.setEnabled(false);
        Thread task = new Thread(){
            public void run(){
                if(jToggleButton1.isSelected()) master.findHarmonics(cand,4);
                else master.findHarmonics(cand,5);
                master.replot();
            }
        };
        task.start();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jToggleButton1ActionPerformed
    
    private void noclassbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noclassbuttonActionPerformed
        //cand.setClass(-1);
        
        cand.addComment("Candidate Declasified");
        class1button.setEnabled(true);
        class2button.setEnabled(true);
        class3button.setEnabled(true);
        noclassbutton.setEnabled(false);
        Thread task = new Thread(){
            public void run(){
                master.findHarmonics(cand,-1);
                master.replot();
            }
        };
        task.start();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_noclassbuttonActionPerformed
    
    private void class2buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_class2buttonActionPerformed
        //cand.setClass(2);
        cand.setNPulses(1);
        class1button.setEnabled(true);
        class2button.setEnabled(false);
        class3button.setEnabled(true);
        noclassbutton.setEnabled(true);
        Thread task = new Thread(){
            public void run(){
                master.findHarmonics(cand,2);
                master.replot();
            }
        };
        task.start();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_class2buttonActionPerformed
    
    private void class3buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_class3buttonActionPerformed
        //cand.setClass(3);
        cand.setNPulses(1);
        class1button.setEnabled(true);
        class2button.setEnabled(false);
        class3button.setEnabled(true);
        noclassbutton.setEnabled(true);
        Thread task = new Thread(){
            public void run(){
                master.findHarmonics(cand,3);
                master.replot();
            }
        };
        task.start();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_class3buttonActionPerformed
    
    private void ClickHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ClickHandler
        if(evt.getButton() == evt.BUTTON1){
            this.setVisible(false);
            this.dispose();
        }
    }//GEN-LAST:event_ClickHandler
    
    private void class1buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_class1buttonActionPerformed
        //cand.setClass(1);
        cand.setNPulses(1);
        class1button.setEnabled(false);
        class2button.setEnabled(true);
        class3button.setEnabled(true);
        noclassbutton.setEnabled(true);
        Thread task = new Thread(){
            public void run(){
                master.findHarmonics(cand,1);
                master.replot();
            }
        };
        task.start();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_class1buttonActionPerformed
    
    /**
     * @param args the command line arguments
     */
/*    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestFrame().setVisible(true);
            }
        });
    }*/
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton class1button;
    private javax.swing.JButton class2button;
    private javax.swing.JButton class3button;
    private javax.swing.JTextArea commentHistory;
    private javax.swing.JPanel dmPanel;
    private javax.swing.JPanel freqPhasePanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton noclassbutton;
    private javax.swing.JPanel pdmHeaderPanel;
    private javax.swing.JPanel profPanel;
    private javax.swing.JPanel scorePanel;
    // End of variables declaration//GEN-END:variables
    
}
