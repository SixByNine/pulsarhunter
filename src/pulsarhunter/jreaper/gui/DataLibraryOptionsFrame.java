/*
 * DataLibraryOptionsFrame.java
 *
 * Created on 12 October 2007, 10:48
 */

package pulsarhunter.jreaper.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import pulsarhunter.Pair;
import pulsarhunter.datatypes.ZapFile;
import pulsarhunter.displaypanels.ZapFileDisplayFrame;
import pulsarhunter.jreaper.DataLibrary;
import pulsarhunter.jreaper.Options;
import pulsarhunter.jreaper.Score.ScoreType;

/**
 *
 * @author  mkeith
 */
public class DataLibraryOptionsFrame extends javax.swing.JFrame {
    private DataLibrary dl;
    private Options opt;
    private Hashtable<ScoreType,JTextField> scoreFields = new Hashtable<ScoreType,JTextField>();
    private ArrayList<DudListItemPanel> listItems = new ArrayList<DudListItemPanel>();
    
    private Color[] mainViewCols = new Color[4];
    
    /** Creates new form DataLibraryOptionsFrame */
    public DataLibraryOptionsFrame(DataLibrary dl) {
        initComponents();
        this.dl = dl;
        this.opt = dl.getOptions();
        
        // set eta and distmax
        this.jTextField_distMax.setText(Double.toString(opt.getDistmax()));
        this.jTextField_eta.setText(Double.toString(opt.getEta()));
        this.jCheckBox_recheckKPSR.setSelected(opt.isAlwaysCheckForKnownPSRs());
        
        // set the number of rows to match the number of scores.
        jPanel_scorePanel.setLayout(new java.awt.GridLayout(ScoreType.values().length, 2));
        // add boxes for each type
        Hashtable<ScoreType,Double> scoreFactors = opt.getScoreFactors();
        for(ScoreType type : ScoreType.values()){
            Double d = scoreFactors.get(type);
            JTextField field = new JTextField();
            
            if(d == null){
                field.setText("Not Set (i.e. 1.0)");
            } else {
                field.setText(d.toString());
            }
            this.scoreFields.put(type,field);
            this.jPanel_scorePanel.add(new JLabel(type.getDescription()));
            this.jPanel_scorePanel.add(field);
        }
        
        
        
        // Colour the buttons nicely...
        
        mainViewCols[0] = this.opt.getStdColmap().getCols()[0];
        mainViewCols[1] = this.opt.getStdColmap().getCols()[1];
        mainViewCols[2] = this.opt.getStdColmap().getCols()[2];
        mainViewCols[3] = this.opt.getStdColmap().getCols()[3];
        
        // Dud lists
        
        
        this.jTextField_dudPath.setText(new File(this.opt.getZapFileRoot()).getAbsolutePath());
        
        for(Pair<String,Boolean> pair : opt.getZapFiles()){
            DudListItemPanel panel = new DudListItemPanel(pair.getA(),this,pair.getB());
            listItems.add(panel);
            this.jPanel_dudLists.add(panel);
        }
        
        
        
        
        
        recolourButtons();
        
        
    }
    
    private void recolourButtons(){
        this.jButton_SearchType0.setBackground(mainViewCols[0]);
        this.jButton_SearchType0.setIcon(new ImageIcon(this.getButtonImage(mainViewCols[0])));
        this.jButton_SearchType1.setBackground(mainViewCols[1]);
        this.jButton_SearchType1.setIcon(new ImageIcon(this.getButtonImage(mainViewCols[1])));
        this.jButton_SearchType2.setBackground(mainViewCols[2]);
        this.jButton_SearchType2.setIcon(new ImageIcon(this.getButtonImage(mainViewCols[2])));
        this.jButton_SearchType3.setBackground(mainViewCols[3]);
        this.jButton_SearchType3.setIcon(new ImageIcon(this.getButtonImage(mainViewCols[3])));
    }
    
    private Image getButtonImage(Color c){
        Image img = new BufferedImage(15,15,BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.setColor(Color.WHITE);
        g.drawRect(0,0,15,15);
        g.setColor(c);
        g.fillRect(1,1,14,14);
        img.flush();
        return img;
    }
    
    
    public void removeDudList(DudListItemPanel panel){
        this.jPanel_dudLists.remove(panel);
        this.listItems.remove(panel);
        this.repaint();
    }
    
    public void editDudList(DudListItemPanel panel){
        File file = new File(this.opt.getZapFileRoot()+panel.getListName()+".zapfile");
        try {
            
            
            ZapFile zf = new ZapFile(file);
            if(file.exists())zf.read();
            new ZapFileDisplayFrame(zf).setVisible(true);
        } catch (IOException ex) {
            System.err.println("Cannot read file for dud list: "+file.getAbsolutePath());
        }
        
        
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel2 = new javax.swing.JPanel();
        jButton_cancel = new javax.swing.JButton();
        jButton_save = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButton_SearchType0 = new javax.swing.JButton();
        jButton_SearchType1 = new javax.swing.JButton();
        jButton_SearchType2 = new javax.swing.JButton();
        jButton_SearchType3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jTextField_eta = new javax.swing.JTextField();
        jTextField_distMax = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jCheckBox_recheckKPSR = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jPanel_scorePanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel_dudLists = new javax.swing.JPanel();
        jTextField_newdud = new javax.swing.JTextField();
        jButton_addDud = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextField_dudPath = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Configure Options");
        setBackground(new java.awt.Color(255, 249, 230));
        setResizable(false);
        jPanel2.setLayout(new java.awt.GridLayout(1, 2));

        jPanel2.setBackground(new java.awt.Color(255, 249, 230));
        jButton_cancel.setText("Cancel");
        jButton_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_cancelActionPerformed(evt);
            }
        });

        jPanel2.add(jButton_cancel);

        jButton_save.setText("Save");
        jButton_save.setFocusCycleRoot(true);
        jButton_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_saveActionPerformed(evt);
            }
        });

        jPanel2.add(jButton_save);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.setBackground(new java.awt.Color(255, 249, 230));
        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 249, 230));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Colours"));
        jPanel3.setOpaque(false);
        jButton_SearchType0.setBackground(new java.awt.Color(153, 0, 153));
        jButton_SearchType0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SearchType0ActionPerformed(evt);
            }
        });

        jButton_SearchType1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SearchType1ActionPerformed(evt);
            }
        });

        jButton_SearchType2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SearchType2ActionPerformed(evt);
            }
        });

        jButton_SearchType3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_SearchType3ActionPerformed(evt);
            }
        });

        jLabel1.setText("Std Search (Type 0)");

        jLabel2.setText("Acc Search (Type 1)");

        jLabel3.setText("Long Search (Type 2)");

        jLabel4.setText("Alt Search (Type 3)");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jButton_SearchType0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jButton_SearchType2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jButton_SearchType1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton_SearchType3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jLabel4))
                .addContainerGap(132, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton_SearchType0)
                    .add(jButton_SearchType2)
                    .add(jLabel1)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton_SearchType1)
                    .add(jLabel2)
                    .add(jButton_SearchType3)
                    .add(jLabel4))
                .addContainerGap(141, Short.MAX_VALUE))
        );
        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 520, 240));

        jPanel4.setBackground(new java.awt.Color(255, 249, 230));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Known PSR Searching"));
        jTextField_eta.setText("jTextField1");

        jTextField_distMax.setText("jTextField2");

        jLabel5.setText("eta (matches periods if [diff < period*eta])");

        jLabel6.setText("Max Seperation to match (degrees)");

        jCheckBox_recheckKPSR.setText("Check for Known PSRs even if CandList is already searched");
        jCheckBox_recheckKPSR.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox_recheckKPSR.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextField_distMax)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextField_eta, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel5)
                            .add(jLabel6)))
                    .add(jCheckBox_recheckKPSR))
                .addContainerGap(117, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextField_eta, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jTextField_distMax, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox_recheckKPSR)
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 520, 210));

        jTabbedPane1.addTab("KnownPSRs and Colours", jPanel1);

        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel5.setBackground(new java.awt.Color(255, 249, 230));
        jPanel_scorePanel.setLayout(new java.awt.GridLayout(1, 2));

        jPanel_scorePanel.setBackground(new java.awt.Color(255, 249, 230));
        jPanel_scorePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Scoring"));
        jPanel5.add(jPanel_scorePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 540, 510));

        jTabbedPane1.addTab("Scoring", jPanel5);

        jPanel6.setBackground(new java.awt.Color(255, 249, 230));
        jLabel7.setText("Dud Lists Associated with this DataLibrary:");

        jPanel_dudLists.setLayout(new javax.swing.BoxLayout(jPanel_dudLists, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane1.setViewportView(jPanel_dudLists);

        jButton_addDud.setText("Add/Create");
        jButton_addDud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_addDudActionPerformed(evt);
            }
        });

        jLabel8.setText("Add or create new:");

        jLabel9.setText("Location of lists:");

        jTextField_dudPath.setEditable(false);
        jTextField_dudPath.setText("jTextField2");
        jTextField_dudPath.setEnabled(false);
        jTextField_dudPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_dudPathActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
                    .add(jLabel7)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel8)
                            .add(jLabel9))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jPanel6Layout.createSequentialGroup()
                                .add(jTextField_newdud, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 241, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton_addDud))
                            .add(jTextField_dudPath))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 370, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(jTextField_newdud, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton_addDud))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jTextField_dudPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jTabbedPane1.addTab("Dud Lists", jPanel6);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-575)/2, (screenSize.height-605)/2, 575, 605);
    }// </editor-fold>//GEN-END:initComponents
    
    private void jTextField_dudPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_dudPathActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jTextField_dudPathActionPerformed
    
    private void jButton_addDudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addDudActionPerformed
        File file = new File(this.opt.getZapFileRoot()+this.jTextField_newdud.getText().trim()+".zapfile");
        if(!file.exists()){
            ZapFile zf = new ZapFile(file);
            zf.setTitle(this.jTextField_newdud.getText().trim());
            try {
                zf.write();
            } catch (IOException ex) {
                System.err.println("Cannot save file for dud list: "+file.getAbsolutePath());
                return;
            }
        }
        DudListItemPanel panel = new DudListItemPanel(this.jTextField_newdud.getText().trim(),this,true);
        listItems.add(panel);
        this.jPanel_dudLists.add(panel);
        this.repaint();
    }//GEN-LAST:event_jButton_addDudActionPerformed
    
    private void jButton_SearchType3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SearchType3ActionPerformed
        Color newCol = JColorChooser.showDialog(this, "Choose a Colour", this.mainViewCols[3]);
        if(newCol != null){
            this.mainViewCols[3] = newCol;
            recolourButtons();
        }
    }//GEN-LAST:event_jButton_SearchType3ActionPerformed
    
    private void jButton_SearchType1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SearchType1ActionPerformed
        Color newCol = JColorChooser.showDialog(this, "Choose a Colour", this.mainViewCols[1]);
        if(newCol != null){
            this.mainViewCols[1] = newCol;
            recolourButtons();
        }
    }//GEN-LAST:event_jButton_SearchType1ActionPerformed
    
    private void jButton_SearchType2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SearchType2ActionPerformed
        Color newCol = JColorChooser.showDialog(this, "Choose a Colour", this.mainViewCols[2]);
        if(newCol != null){
            this.mainViewCols[2] = newCol;
            recolourButtons();
        }
    }//GEN-LAST:event_jButton_SearchType2ActionPerformed
    
    private void jButton_SearchType0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_SearchType0ActionPerformed
        Color newCol = JColorChooser.showDialog(this, "Choose a Colour", this.mainViewCols[0]);
        if(newCol != null){
            this.mainViewCols[0] = newCol;
            recolourButtons();
        }
        
    }//GEN-LAST:event_jButton_SearchType0ActionPerformed
    
    private void jButton_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_saveActionPerformed
        
        double d;
        // do eta and distMax
        
        d = Double.parseDouble(this.jTextField_eta.getText());
        if(!Double.isNaN(d)){
            this.opt.setEta(d);
        }
        d = Double.parseDouble(this.jTextField_distMax.getText());
        if(!Double.isNaN(d)){
            this.opt.setDistmax(d);
        }
        
        this.opt.setAlwaysCheckForKnownPSRs(this.jCheckBox_recheckKPSR.isSelected());
        
        
        // Do scores...
        for(ScoreType type : ScoreType.values()){
            JTextField field = this.scoreFields.get(type);
            try {
                d = Double.parseDouble(field.getText());
                this.opt.getScoreFactors().remove(type);
                this.opt.getScoreFactors().put(type,d);
            } catch (NumberFormatException ex) {
                this.opt.getScoreFactors().remove(type);
            }
        }
        
        // Do Colours
        
        
        for(int i = 0; i < this.mainViewCols.length; i++){
            this.opt.getStdColmap().getCols()[i] = mainViewCols[i];
            this.opt.getStdColmap().getCols()[i+5] = mainViewCols[i];
            
            
        }
        
        // Do dud lists
        
        opt.getZapFiles().clear();
        for(DudListItemPanel panel : this.listItems){
            opt.getZapFiles().add(new Pair<String,Boolean>(panel.getListName(),panel.isListEnabled()));
        }
        
        // Done, so quit!
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButton_saveActionPerformed
    
    
    
    private void jButton_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_cancelActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButton_cancelActionPerformed
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_SearchType0;
    private javax.swing.JButton jButton_SearchType1;
    private javax.swing.JButton jButton_SearchType2;
    private javax.swing.JButton jButton_SearchType3;
    private javax.swing.JButton jButton_addDud;
    private javax.swing.JButton jButton_cancel;
    private javax.swing.JButton jButton_save;
    private javax.swing.JCheckBox jCheckBox_recheckKPSR;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel_dudLists;
    private javax.swing.JPanel jPanel_scorePanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField_distMax;
    private javax.swing.JTextField jTextField_dudPath;
    private javax.swing.JTextField jTextField_eta;
    private javax.swing.JTextField jTextField_newdud;
    // End of variables declaration//GEN-END:variables
    
}
