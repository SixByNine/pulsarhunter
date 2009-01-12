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
 * InitialFrame.java
 *
 * Created on 27 May 2005, 12:13
 */

package pulsarhunter.jreaper.gui;

import coordlib.Coordinate;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import pulsarhunter.Pair;
import pulsarhunter.jreaper.CandList;
import pulsarhunter.jreaper.DataLibrary;
import pulsarhunter.jreaper.JReaper;

/**
 *
 * @author  mkeith
 */
public class CandListChooseFrame extends javax.swing.JFrame {
    private JReaper jreaper;
    private DataLibrary dataLibrary;
    
    /** Creates new form InitialFrame */
    public CandListChooseFrame(JReaper jreaper,DataLibrary dl) {
        this.jreaper = jreaper;
        this.dataLibrary = dl;
        initComponents();
        Image splashimage;
        try{
            java.net.URL imageurl = this.getClass().getResource("jreaperlogo4.0.png");
            
            if(imageurl!=null){
                splashimage = ImageIO.read(imageurl);
                //splashimage = ImageIO.read(new File("jreaperlogo3.0.png"));
                JPanel iPanel = new ImagePanel(splashimage);
                iPanel.setBackground(new java.awt.Color(0, 0, 0));
                jPanel_logo.add(iPanel,java.awt.BorderLayout.CENTER);
            }
            
        } catch (IOException e){
            e.printStackTrace();
        }
        this.setTitle(JReaper.WINDOWTITLE);
        this.jTextField_dataLibraryRoot.setText(this.dataLibrary.getRootPath().getPath());
        
        
        updateDisplay();
        
        repaint();
    }
    
    
    private void updateDisplay(){
        
        String searchString = this.jTextField1.getText();
        boolean searchByPosition = this.jCheckBox1.isSelected();
        String positionSearch = this.jTextField2.getText();
        double distance = 1.5;
        try {
            
            distance = Double.parseDouble(this.jTextField3.getText());
        } catch (NumberFormatException ex) {
            this.jTextField3.setText("1.5");
        }
        
        
        List<Pair<String,String>> clNames = null;
        
        if(searchByPosition){
            clNames = this.dataLibrary.searchCandListsNear(searchString,new Coordinate().generateNew(positionSearch,""),distance);
        } else {
            clNames = this.dataLibrary.searchCandLists(searchString);
        }
        
        final Hashtable<String,ClGroup> groupTable = new Hashtable<String,ClGroup>();
        
        
        for(Pair<String,String> names : clNames){
            String gName = names.getA();
            String cName = names.getB();
            ClGroup g = groupTable.get(gName);
            if(g==null){
                g = new ClGroup();
                g.setName(gName);
                
                groupTable.put(gName,g);
            }
            
            
            Cl cl = new Cl();
            cl.setName(cName);
            cl.setGroup(g);
            g.getContents().add(cl);
        }
        
        
        
        java.awt.EventQueue.invokeLater(new Runnable(){
            public void run(){
                final ClGroup[] arr = groupTable.values().toArray(new ClGroup[0]);
                Arrays.sort(arr,new Comparator<ClGroup>(){
                    public int compare(CandListChooseFrame.ClGroup o1, CandListChooseFrame.ClGroup o2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(),o2.getName());
                    }
                    
                });
                
                jList_Lists.setModel(new javax.swing.AbstractListModel() {
                    public int getSize() { return 0; }
                    public Object getElementAt(int i) { return null; }
                });
                
                jList_Groups.setModel(new javax.swing.AbstractListModel() {
                    public int getSize() { return arr.length; }
                    public Object getElementAt(int i) { return arr[i]; }
                });
                
                
                final Pair[] arr2 = jreaper.getLoadedCandLists().toArray(new Pair[0]);
                Arrays.sort((Pair[])arr2,new Comparator<Pair<String,CandList>>(){
                    public int compare(Pair<String, CandList> o1, Pair<String, CandList> o2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getA(),o2.getA());
                    }
                    
                });
                jList_Loaded.setModel(new javax.swing.AbstractListModel() {
                    public int getSize() { return arr2.length; }
                    public Object getElementAt(int i) { return arr2[i]; }
                });
                
                jButton_load.setEnabled(true);
                jList_Lists.setEnabled(true);
                jList_Loaded.setEnabled(true);
                jButton_unload.setEnabled(true);
                jList_Groups.setEnabled(true);
                jButton_continue.setEnabled(true);
                CandListChooseFrame.this.repaint();
            }
        });
        
    }
    
    private class ClGroup{
        private String name;
        private ArrayList<Cl> contents = new ArrayList<Cl>();
        
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public ArrayList<Cl> getContents() {
            return contents;
        }
        public String toString(){
            return this.name+" ("+this.contents.size()+")";
        }
    }
    
    private class Cl{
        private String name;
        private ClGroup group;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public ClGroup getGroup() {
            return group;
        }
        
        public void setGroup(ClGroup group) {
            this.group = group;
        }
        public String toString(){
            return name;
        }
        
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel_logo = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton_continue = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jTextField2 = new javax.swing.JTextField();
        jButton_search = new javax.swing.JButton();
        jButton_clearSearch = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton_load = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton_unload = new javax.swing.JButton();
        jScrollPane_Loaded = new javax.swing.JScrollPane();
        jList_Loaded = new javax.swing.JList();
        jScrollPane_Lists = new javax.swing.JScrollPane();
        jList_Lists = new javax.swing.JList();
        jScrollPane_Groups = new javax.swing.JScrollPane();
        jList_Groups = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jTextField_dataLibraryRoot = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jTextField_groupName = new javax.swing.JTextField();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel_dlName = new javax.swing.JLabel();
        jButton_optionsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("JReaper: 2.2");
        setBackground(new java.awt.Color(0, 0, 0));
        setForeground(java.awt.Color.white);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel_logo.setLayout(new java.awt.BorderLayout());

        jPanel_logo.setBackground(new java.awt.Color(255, 249, 230));
        jPanel_logo.setMaximumSize(new java.awt.Dimension(800, 100));
        jPanel_logo.setMinimumSize(new java.awt.Dimension(800, 100));
        jPanel_logo.setPreferredSize(new java.awt.Dimension(800, 100));
        getContentPane().add(jPanel_logo, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.GridLayout(1, 0));

        jPanel1.setBackground(new java.awt.Color(255, 249, 230));
        jPanel1.setForeground(new java.awt.Color(29, 37, 33));
        jPanel1.setPreferredSize(new java.awt.Dimension(225, 30));
        jButton3.setText("Quit");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton3);

        jButton_continue.setText("Continue to Plot");
        jButton_continue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_continueActionPerformed(evt);
            }
        });

        jPanel1.add(jButton_continue);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 249, 230));
        jPanel3.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 180, 20));

        jLabel1.setText("Search");
        jPanel3.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, 50, -1));

        jCheckBox1.setBackground(new java.awt.Color(255, 249, 230));
        jCheckBox1.setText("Filter By Position");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jPanel3.add(jCheckBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 130, -1));

        jTextField2.setEnabled(false);
        jPanel3.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 30, 170, 20));

        jButton_search.setText("Search");
        jButton_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_searchActionPerformed(evt);
            }
        });

        jPanel3.add(jButton_search, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 130, 20));

        jButton_clearSearch.setText("Clear Search");
        jButton_clearSearch.setMaximumSize(new java.awt.Dimension(65, 23));
        jButton_clearSearch.setMinimumSize(new java.awt.Dimension(65, 23));
        jButton_clearSearch.setPreferredSize(new java.awt.Dimension(65, 23));
        jButton_clearSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_clearSearchActionPerformed(evt);
            }
        });

        jPanel3.add(jButton_clearSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 60, 130, 20));

        jLabel2.setText("Select Groups");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        jLabel3.setText("Select Candidate Lists");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 90, -1, -1));

        jButton_load.setText("Load Selected");
        jButton_load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_loadActionPerformed(evt);
            }
        });

        jPanel3.add(jButton_load, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 150, 130, -1));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("---->");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 180, 130, -1));

        jLabel5.setText("Loaded Candidate Lists");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 90, -1, -1));

        jLabel6.setText("Max Seperation:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 30, -1, -1));

        jTextField3.setText("1.5");
        jTextField3.setEnabled(false);
        jPanel3.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 30, 50, -1));

        jButton_unload.setText("Unload selected");
        jButton_unload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_unloadActionPerformed(evt);
            }
        });

        jPanel3.add(jButton_unload, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 320, 180, -1));

        jScrollPane_Loaded.setViewportView(jList_Loaded);

        jPanel3.add(jScrollPane_Loaded, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 110, 180, 210));

        jScrollPane_Lists.setViewportView(jList_Lists);

        jPanel3.add(jScrollPane_Lists, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 110, 190, 230));

        jList_Groups.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList_GroupsValueChanged(evt);
            }
        });

        jScrollPane_Groups.setViewportView(jList_Groups);

        jPanel3.add(jScrollPane_Groups, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 190, 230));

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 249, 230));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Add More Candidate Lists"));
        jTextField_dataLibraryRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_dataLibraryRootActionPerformed(evt);
            }
        });

        jPanel2.add(jTextField_dataLibraryRoot, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 20, 200, -1));

        jLabel8.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel8.setText("Load from dir:");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 20, 90, -1));

        jButton4.setText("Browse");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel2.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 20, 80, -1));

        jButton1.setFont(new java.awt.Font("Dialog", 1, 10));
        jButton1.setText("Keep Cache and Add New Cand Lists");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 250, -1));

        jButton2.setFont(new java.awt.Font("Dialog", 1, 10));
        jButton2.setText("Delete Cache and Reload All Cand Lists");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 250, -1));

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel7.setText("Group Name:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 50, 90, -1));

        jTextField_groupName.setText("default");
        jTextField_groupName.setEnabled(false);
        jPanel2.add(jTextField_groupName, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 50, 200, -1));

        jCheckBox2.setBackground(new java.awt.Color(255, 249, 230));
        jCheckBox2.setFont(new java.awt.Font("Dialog", 1, 10));
        jCheckBox2.setSelected(true);
        jCheckBox2.setText("Use First Level Dirs as Group Name");
        jCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jPanel2.add(jCheckBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 50, -1, -1));

        jPanel3.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 350, 760, 80));

        jLabel_dlName.setText("Data Library:");
        jPanel3.add(jLabel_dlName, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, 270, -1));

        jButton_optionsButton.setText("Options");
        jButton_optionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_optionsButtonActionPerformed(evt);
            }
        });

        jPanel3.add(jButton_optionsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 0, -1, 20));

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-800)/2, (screenSize.height-600)/2, 800, 600);
    }// </editor-fold>//GEN-END:initComponents
    
    private void jTextField_dataLibraryRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_dataLibraryRootActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jTextField_dataLibraryRootActionPerformed
    
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        
        Runnable runnable = new Runnable(){
            public void run(){
                final JFileChooser jfc = new JFileChooser(CandListChooseFrame.this.jTextField_dataLibraryRoot.getText());
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(jfc.showOpenDialog(CandListChooseFrame.this)==JFileChooser.APPROVE_OPTION){
                    java.awt.EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            CandListChooseFrame.this.jTextField_dataLibraryRoot.setText(jfc.getSelectedFile().getAbsolutePath());
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
        
    }//GEN-LAST:event_jButton4ActionPerformed
    
    private void jButton_optionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_optionsButtonActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable(){
            public void run(){
                new DataLibraryOptionsFrame(dataLibrary).setVisible(true);
            }
        });
    }//GEN-LAST:event_jButton_optionsButtonActionPerformed
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Runnable runnable = new Runnable(){
            public void run(){
                jreaper.close();
            }
        };
        new Thread(runnable).start();
    }//GEN-LAST:event_formWindowClosing
    
    private void jButton_continueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_continueActionPerformed
        
        Runnable runnable = new Runnable(){
            public void run(){
                jreaper.goToPlot();
            }
        };
        new Thread(runnable).start();
        
    }//GEN-LAST:event_jButton_continueActionPerformed
    
    private void jButton_unloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_unloadActionPerformed
        final Object[] arr = this.jList_Loaded.getSelectedValues();
        this.jList_Groups.setEnabled(false);
        this.jButton_load.setEnabled(false);
        this.jList_Lists.setEnabled(false);
        this.jList_Loaded.setEnabled(false);
        this.jButton_unload.setEnabled(false);
        this.jButton_continue.setEnabled(false);
        final LoadingSplash lsplash = new LoadingSplash();
        
        Runnable runnable = new Runnable(){
            public void run(){
                int i = 0;
                
                for(Object o : arr){
                    Pair<String,CandList> cl = (Pair<String,CandList>)o;
                    final int count = i++;
                    final String gName = cl.getA();
                    final CandList cList = cl.getB();
                    java.awt.EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            lsplash.setProgress("Unloading "+gName+": "+cList.getName(),count,arr.length);
                            lsplash.setVisible(true);
                        }
                    });
                    jreaper.closeCandList(gName,cList);
                    
                    
                    
                }
                java.awt.EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        lsplash.setVisible(false);
                        lsplash.dispose();
                    }
                });
                CandListChooseFrame.this.updateDisplay();
            }
        };
        new Thread(runnable).start();
    }//GEN-LAST:event_jButton_unloadActionPerformed
    
    private void jButton_loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_loadActionPerformed
        
        final Object[] arr = this.jList_Lists.getSelectedValues();
        this.jList_Groups.setEnabled(false);
        this.jButton_load.setEnabled(false);
        this.jList_Lists.setEnabled(false);
        this.jList_Loaded.setEnabled(false);
        this.jButton_unload.setEnabled(false);
        this.jButton_continue.setEnabled(false);
        Runnable runnable = new Runnable(){
            public void run(){
                final LoadingSplash lsplash = new LoadingSplash();
                int i = 0;
                for(Object o : arr){
                    Cl cl = (Cl)o;
                    final int count = i++;
                    final String gName = cl.getGroup().getName();
                    final String cName = cl.getName();
                    
                    java.awt.EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            lsplash.setProgress("Loading "+gName+": "+cName,count,arr.length);
                            lsplash.setVisible(true);
                        }
                    });
                    
                    jreaper.loadCandList(gName,cName);
                    
                    
                    
                }
                java.awt.EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        lsplash.setVisible(false);
                        lsplash.dispose();
                    }
                });
                CandListChooseFrame.this.updateDisplay();
            }
        };
        new Thread(runnable).start();
        
    }//GEN-LAST:event_jButton_loadActionPerformed
    
    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        this.jTextField2.setEnabled(this.jCheckBox1.isSelected());
        this.jTextField3.setEnabled(this.jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1ActionPerformed
    
    private void jButton_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_searchActionPerformed
        updateDisplay();
    }//GEN-LAST:event_jButton_searchActionPerformed
    
    private void jButton_clearSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_clearSearchActionPerformed
        this.jTextField1.setText("");
        this.jCheckBox1.setSelected(false);
        updateDisplay();
    }//GEN-LAST:event_jButton_clearSearchActionPerformed
    
    private void jList_GroupsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList_GroupsValueChanged
        
        
        final Object[] selectedGroups = this.jList_Groups.getSelectedValues();
        
        
        java.awt.EventQueue.invokeLater(new Runnable(){
            public void run(){
                
                ArrayList<Cl> aL = new ArrayList<Cl> ();
                
                for(Object o : selectedGroups){
                    ClGroup clg = (ClGroup)o;
                    aL.addAll(clg.getContents());
                    
                }
                
                
                
                final Cl[] arr = aL.toArray(new Cl[0]);
                
                Arrays.sort(arr,new Comparator<Cl>(){
                    public int compare(CandListChooseFrame.Cl o1, CandListChooseFrame.Cl o2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.name,o2.name);
                    }
                    
                });
                
                jList_Lists.setModel(new javax.swing.AbstractListModel() {
                    public int getSize() { return arr.length; }
                    public Object getElementAt(int i) { return arr[i]; }
                });
                
                int[] select = new int[arr.length];
                for(int i = 0; i < arr.length; i++){
                    select[i] = i;
                }
                
                jList_Lists.setSelectedIndices(select);
                
                jList_Lists.setEnabled(true);
                CandListChooseFrame.this.repaint();
                
            }
        });
        
    }//GEN-LAST:event_jList_GroupsValueChanged
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.jList_Groups.setEnabled(false);
        this.jList_Lists.setEnabled(false);
        //    this.jList_Loaded.setEnabled(false);
        this.dataLibrary.clearAll();
        final String groupName;
        if(!this.jCheckBox2.isSelected()) groupName = this.jTextField_groupName.getText();
        else groupName = null;
        final File loadFile = new File(this.jTextField_dataLibraryRoot.getText());
        final DataLibrary dataLibrary = this.dataLibrary;
        Runnable runnable = new Runnable(){
            public void run(){
                dataLibrary.importClistsFromDir(loadFile,groupName);
                CandListChooseFrame.this.updateDisplay();
            }
        };
        new Thread(runnable).start();
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        this.jTextField_groupName.setEnabled(!this.jCheckBox2.isSelected());
    }//GEN-LAST:event_jCheckBox2ActionPerformed
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.jList_Groups.setEnabled(false);
        this.jList_Lists.setEnabled(false);
        this.jList_Loaded.setEnabled(false);
        final String groupName;
        if(!this.jCheckBox2.isSelected()) groupName = this.jTextField_groupName.getText();
        else groupName = null;
        final File loadFile = new File(this.jTextField_dataLibraryRoot.getText());
        final DataLibrary dataLibrary = this.dataLibrary;
        Runnable runnable = new Runnable(){
            public void run(){
                dataLibrary.importClistsFromDir(loadFile,groupName);
                CandListChooseFrame.this.updateDisplay();
            }
        };
        new Thread(runnable).start();
    }//GEN-LAST:event_jButton1ActionPerformed
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Runnable runnable = new Runnable(){
            public void run(){
                jreaper.close();
            }
        };
        new Thread(runnable).start();
        
    }//GEN-LAST:event_jButton3ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton_clearSearch;
    private javax.swing.JButton jButton_continue;
    private javax.swing.JButton jButton_load;
    private javax.swing.JButton jButton_optionsButton;
    private javax.swing.JButton jButton_search;
    private javax.swing.JButton jButton_unload;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel_dlName;
    private javax.swing.JList jList_Groups;
    private javax.swing.JList jList_Lists;
    private javax.swing.JList jList_Loaded;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel_logo;
    private javax.swing.JScrollPane jScrollPane_Groups;
    private javax.swing.JScrollPane jScrollPane_Lists;
    private javax.swing.JScrollPane jScrollPane_Loaded;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField_dataLibraryRoot;
    private javax.swing.JTextField jTextField_groupName;
    // End of variables declaration//GEN-END:variables
    
}
