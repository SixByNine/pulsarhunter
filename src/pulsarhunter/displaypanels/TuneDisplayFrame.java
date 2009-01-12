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
 * TuneDisplayFrame.java
 *
 * Created on 13 March 2007, 16:31
 */

package pulsarhunter.displaypanels;

import java.awt.BorderLayout;
import java.util.Arrays;
import pulsarhunter.Convert;
import pulsarhunter.displaypanels.MKPlot.LineStyle;
import pulsarhunter.jreaper.Colourmap;
import pulsarhunter.processes.folding.PeriodTuneFold;
import pulsarhunter.processes.folding.PeriodTuneFoldParams;

/**
 *
 * @author  mkeith
 */
public class TuneDisplayFrame extends javax.swing.JFrame implements ClickNotifyable{
    private double[][][] bandedSints;
    private double[][][] bandedSints_orig;
    private boolean[] ignoreSints;
    private boolean[] ignoreSbands;
    private PeriodTuneFoldParams params;
    private boolean done = false;
    /** Creates new form TuneDisplayFrame */
    public TuneDisplayFrame(double[][][] bandedSints, PeriodTuneFoldParams params) {
        initComponents();
        bandedSints_orig = new double[bandedSints.length][bandedSints[0].length][bandedSints[0][0].length];
        for(int i = 0; i < bandedSints.length; i++){
            for(int j = 0; j < bandedSints[0].length; j++){
                for(int k = 0; k < bandedSints[0][0].length; k++){
                    bandedSints_orig[i][j][k] = bandedSints[i][j][k];
                }
            }
        }
        this.bandedSints = bandedSints;
        this.params = params;
        
        this.ignoreSints = new boolean[bandedSints[0].length];
        Arrays.fill(ignoreSints,false);
        this.ignoreSbands = new boolean[bandedSints.length];
        Arrays.fill(ignoreSbands,false);
        makePlots();
    }
    
    
    private void makePlots(){
        Thread thread = new Thread(){
            public void run(){
                final double[][] map_d = Convert.rotateDoubleArray(PeriodTuneFold.dedisperseSints(0,bandedSints));
                java.awt.EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        TuneDisplayFrame.this.jPanel1.setVisible(false);
                        TuneDisplayFrame.this.jPanel1.removeAll();
                        PlotTwoDim sintplot = new PlotTwoDim("Sub-Integrations","Bin Number","Sub-Int Number",map_d,Colourmap.gray_quadratic);
                        sintplot.addClickNotifyable(TuneDisplayFrame.this);
                        TuneDisplayFrame.this.jPanel1.add(sintplot,BorderLayout.CENTER);
                        TuneDisplayFrame.this.jPanel1.validate();
                        TuneDisplayFrame.this.jPanel1.setVisible(true);
                    }
                });
                
                final double[] prof = new double[bandedSints[0][0].length];
                Arrays.fill(prof,0.0);
                final double[] xaxis = new double[bandedSints[0][0].length];
                final double[][] sbands = new double[bandedSints.length][bandedSints[0][0].length];
                for(int i = 0; i < sbands.length; i++)Arrays.fill(sbands[i],0);
                
                for(int i =0; i < sbands.length; i++){
                    for(int dd = 0; dd < sbands[0].length; dd++){
                        for(int s = 0; s < bandedSints[i].length; s++){
                            sbands[i][dd] += bandedSints[i][s][dd];
                            prof[dd] += bandedSints[i][s][dd];
                            xaxis[dd] = (double)dd/prof.length;
                        }
                    }
                }
                java.awt.EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        TuneDisplayFrame.this.jPanel2.setVisible(false);
                        TuneDisplayFrame.this.jPanel2.removeAll();
                        PlotTwoDim sintplot = new PlotTwoDim("Sub-Bands","Bin Number","Sub-Bands Number",Convert.rotateDoubleArray(sbands),Colourmap.gray_quadratic);
                        sintplot.addClickNotifyable(TuneDisplayFrame.this);
                        TuneDisplayFrame.this.jPanel2.add(sintplot,BorderLayout.CENTER);
                        TuneDisplayFrame.this.jPanel2.validate();
                        TuneDisplayFrame.this.jPanel2.setVisible(true);
                        
                        TuneDisplayFrame.this.jPanel3.setVisible(false);
                        TuneDisplayFrame.this.jPanel3.removeAll();
                        PlotOneDim pp = new PlotOneDim("Profile","Phase","Power",xaxis,prof);
                        pp.setLinestyle(LineStyle.Histogram);
                        TuneDisplayFrame.this.jPanel3.add(pp,BorderLayout.CENTER);
                        TuneDisplayFrame.this.jPanel3.validate();
                        TuneDisplayFrame.this.jPanel3.setVisible(true);
                    }
                });
                
                
                
//                java.awt.EventQueue.invokeLater(new Runnable(){
//                    public void run(){
//                        TuneDisplayFrame.this.jPanel3.setVisible(false);
//                        TuneDisplayFrame.this.jPanel3.removeAll();
//                        PlotOneDim sintplot = new PlotOneDim("Profile","Phase","Power",xaxis,prof);
//                        TuneDisplayFrame.this.jPanel3.add(sintplot,BorderLayout.CENTER);
//                        TuneDisplayFrame.this.jPanel3.validate();
//                        TuneDisplayFrame.this.jPanel3.setVisible(true);
//                    }
//                });
                
            }
            
        };
        thread.start();
    }
    
    private void recalcBandedSints(){
        Thread thread = new Thread(){
            public void run(){
                for(int i = 0; i < bandedSints.length; i++){
                    for(int j = 0; j < bandedSints[0].length; j++){
                        for(int k = 0; k < bandedSints[0][0].length; k++){
                            if(!ignoreSints[j] && !ignoreSbands[i]){
                                bandedSints[i][j][k] = bandedSints_orig[i][j][k];
                            } else {
                                bandedSints[i][j][k] = 0.0;
                            }
                        }
                    }
                }
            }
        };
        thread.start();
    }
    public boolean waitToTerminate(){
        while(!done){
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        System.out.print("Ignored Bands:");
        for(int i = 0 ; i < ignoreSbands.length; i++){
            if(ignoreSbands[i])System.out.print(i+",");
        }
        System.out.println();
        System.out.print("Ignored Subints:");
        for(int i = 0 ; i < ignoreSints.length; i++){
            if(ignoreSints[i])System.out.print(i+",");
        }
        System.out.println();
        return true;
    }
    
    public void dispose() {
        super.dispose();
        this.done = true;
    }
    
    public void clicked(double x, double y, Object clickedItem) {
        if(clickedItem.toString().equals("Sub-Integrations")){
            this.ignoreSints[(int)y] = !this.ignoreSints[(int)y];
        } else if(clickedItem.toString().equals("Sub-Bands")){
            this.ignoreSbands[(int)y] = !this.ignoreSbands[(int)y];
        }
        recalcBandedSints();
        makePlots();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridLayout(2, 2));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jPanel1.setLayout(new java.awt.BorderLayout());

        getContentPane().add(jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        getContentPane().add(jPanel2);

        jPanel3.setLayout(new java.awt.BorderLayout());

        getContentPane().add(jPanel3);

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        jButton1.setText("Kill Next Peak");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel4.add(jButton1);

        getContentPane().add(jPanel4);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-800)/2, (screenSize.height-600)/2, 800, 600);
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        double max = -Double.MAX_VALUE;
        int killint = 0;
        for(int i = 0; i < bandedSints.length; i++){
            for(int j = 0; j < bandedSints[0].length; j++){
                for(int k = 0; k < bandedSints[0][0].length; k++){
                    if(bandedSints[i][j][k] > max){
                        max =   bandedSints[i][j][k];
                        killint = j;
                    }
                }
            }
        }
        ignoreSints[killint] = true;
        recalcBandedSints();
        makePlots();
    }//GEN-LAST:event_jButton1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
    
}
