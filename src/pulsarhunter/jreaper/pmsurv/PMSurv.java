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
 * PMSurv.java
 *
 * Created on 27 May 2005, 10:57
 */

package pulsarhunter.jreaper.pmsurv;
import javax.swing.JMenu;
import pulsarhunter.jreaper.JReaper;
import pulsarhunter.jreaper.Main;
import pulsarhunter.jreaper.Plugin;

/**
 *
 * @author mkeith
 */
public class PMSurv implements Plugin {
    
    

    /** Creates a new instance of PMSurv */
    public PMSurv() {
    }
    
    public void init(JReaper jreaper){
//
//        jreaper.addCandReader(new phfile_CandidateReader(), ".ph");
//        jreaper.addCandReader(new aphfile_CandidateReader(), ".aph");
//        jreaper.addCandReader(new sphfile_CandidateReader(0), ".std.sph");
//        jreaper.addCandReader(new sphfile_CandidateReader(1), ".acc.sph");
//        jreaper.addCandReader(new sphfile_CandidateReader(2), ".lng.sph");
//        jreaper.addCandReader(new sphfile_CandidateReader(3), ".pdm.sph");

        jreaper.addDataLibraryType(new PMDataLibraryType(jreaper));
        
    }
    
    public String getName(){
        return "PMSurvPlugin";
    }
    public String getInfo(){
        return "PMSurv Plugin  - Michael Keith 2005" +
                "\nReads and displays susfind *.*ph files" ;

        
    }
    
    public JMenu getMenu(){
        JMenu menu = new JMenu();
        
        menu = null;
        
        
//        JMenuItem item = new JMenuItem();
//        item.setText("Create New DataLibrary");
//        item.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                java.awt.EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        new NewDataLibFrame().setVisible(true);
//                    }
//                });
//            }
//        });
//        menu.add(item);
//        
//        
//        
//        JMenuItem item2 = new JMenuItem();
//        item2.setText("Modify DataLibrary");
//        item2.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                //if((Main.getInstance().getDataLibrary() != null) && (Main.getInstance().getDataLibrary() instanceof pulsarsweep.plugins.PMSurvPlugin.PMDataLibrary)){
//                java.awt.EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        
//                        new ModifyDataLibFrame((PMDataLibrary)Main.getInstance().getDataLibrary(),Main.getInstance().getDataLibFile().getAbsolutePath()).setVisible(true);
//                        
//                    }
//                });
//                //} else Main.getInstance().log(Main.getInstance().getDataLibrary().getClass().getCanonicalName());
//            }
//        });
//        menu.add(item2);
//        
//        
//        DataLibrary dl = Main.getInstance().getDataLibrary();
//        
//        if(dl== null || ! (dl instanceof PMDataLibrary)){
//            // item2.setEnabled(false); // prevend modification of datalibraries when none are loaded.
//        } //else{
//        // item.setEnabled(false);  // Prevent creation of new Datalibraries when one is loaded. Can be removed when we can handle multi-datalibrary.
//        //}
//        
//        
//        if(!(Main.getInstance().getDataLibrary() instanceof PMDataLibrary)  || ((PMDataLibrary)Main.getInstance().getDataLibrary()).hasRemote()){
//            item2.setEnabled(false);
//        }
//        
//        item = new JMenuItem();
//        item.setText("Update Website");
//        item.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                java.awt.EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        DataLibrary dl = Main.getInstance().getDataLibrary();
//                        if(dl instanceof PMDataLibrary){
//                            new WebUpdater(dl).update();
//                        }
//                    }
//                });
//            }
//        });
//        menu.add(item);
//        
//        item = new JMenuItem();
//        item.setText("Sync with website");
//        item.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                java.awt.EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        DataLibrary dl = Main.getInstance().getDataLibrary();
//                        if(dl instanceof PMDataLibrary){
//                            ((PMDataLibrary)dl).webSync(Main.getInstance().getMasterData());
//                        }
//                    }
//                });
//            }
//        });
//        menu.add(item);
//        
//        JMenu peckMenu = new JMenu();
//        
//        item = new JMenuItem();
//        
//        
//        /*item = new JMenuItem();
//        item.setText("Rescore All");
//        item.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(ActionEvent e){
//                Thread task = new Thread(){
//                    public void run(){
//                        java.awt.EventQueue.invokeLater(new Runnable() {
//                            public void run() {
//                                Main.getInstance().lsplash.setText("Rescoring in progress...  Please Wait");
//                                Main.getInstance().lsplash.setVisible(true);
//                            }
//                        });
//                        CandScorer scorer = new PeckScorer();
//                        while(true){
//                            if(Main.getInstance().getLoadedCandLists().size() < 1) break;
//                            CandList cList = Main.getInstance().getLoadedCandLists().get(0);
//         
//                            for(Cand[] cs : cList.getCands()){
//                                for(Cand c : cs){
//                                    c.setScore(scorer.score(c));
//                                }
//                            }
//         
//                            Main.getInstance().unloadCandList(cList);
//                        }
//                        java.awt.EventQueue.invokeLater(new Runnable() {
//                            public void run() {
//         
//                                Main.getInstance().lsplash.setVisible(false);
//                            }
//                        });
//                    }
//                };
//                task.start();
//            };
//            //task.start();
//        });
//        peckMenu.add(item);
//         
//        peckMenu.setText("Peck Scoring");
//         
//        menu.add(peckMenu);
//         */
//        
//        
//        menu.setText("PMSurv");
        
        return menu;
    }
    
    
    public static boolean autoUpdateWebsite = true;
}
