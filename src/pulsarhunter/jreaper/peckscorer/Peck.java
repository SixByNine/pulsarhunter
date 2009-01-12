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
 * Peck.java
 *
 * Created on 25 August 2005, 14:53
 */

package pulsarhunter.jreaper.peckscorer;

import pulsarhunter.jreaper.JReaper;
import pulsarhunter.jreaper.Plugin;
/**
 *
 * @author mkeith
 */
public class Peck implements Plugin {

    
    PeckScorer pScorer;
    /** Creates a new instance of Peck */
    public Peck() {
    }
    
    public void init(JReaper jreaper) {
        pScorer = new PeckScorer();
       // jreaper.addCandScorer(pScorer);
      //  jreaper.setDefaultCandScorer(pScorer);


    }
    
    public String toString() {
        return this.getName();
        
    }
    
    public String getName() {
        return "Peck Scoring System";
    }
    
    public javax.swing.JMenu getMenu() {
        return null;
//        JMenu menu = new JMenu("Peck");
//        
//        JMenuItem item = new JMenuItem();
//        item.setText("Rescore All");
//        item.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                Thread task = new Thread(){
//                    public void run(){
//                        java.awt.EventQueue.invokeLater(new Runnable() {
//                            public void run() {
//                                Main.getInstance().lsplash.setText("Rescoring...");
//                                Main.getInstance().lsplash.setVisible(true);
//                            }
//                        });
//                        int i = 0;
//                        int j = 0;
//                        int total = 0;
//                        Cand[][] data = Main.getInstance().getMasterData();
//                        for(Cand[] candlist : data){
//                            total += candlist.length;
//                        }
//                        final int t = total;
//                        for(Cand[] candlist : data){
//                            pScorer.rescore(candlist);
//                            i++;
//                            j++;
//                            if(j > 100){
//                                j = 0;
//                                final int k = i;
//                                
//                                java.awt.EventQueue.invokeLater(new Runnable() {
//                                    public void run() {
//                                        Main.getInstance().lsplash.setText("Rescoring... "+Math.round(((double)k)/((double)t)*100)+"%");
//                                    }
//                                });
//                            }
//                        }
//                        java.awt.EventQueue.invokeLater(new Runnable() {
//                            public void run() {
//                                Main.getInstance().lsplash.setVisible(false);
//                            }
//                        });
//                    }
//                };
//                task.start();
//            }
//            
//        });
//        menu.add(item);
//        
//        item = new JMenuItem();
//        item.setText("Set as Default Scorer");
//        item.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                Main.getInstance().setDefaultCandScorer(pScorer);
//            }
//            
//        });
//        menu.add(item);
//        
//        item = new JMenuItem();
//        item.setText("Configure");
//        item.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//
//            }
//            
//        });
//        menu.add(item);
//        
//        return menu;
        
    }
    
    public String getInfo() {
        return "Peck Scoring System\nMichael Keith 2005";
    }
    
}
