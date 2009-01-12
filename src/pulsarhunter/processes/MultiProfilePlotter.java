/*
 * MultiProfilePlotter.java
 *
 * Created on July 30, 2007, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.datatypes.MultiprofileFile;
import pulsarhunter.displaypanels.MKPlot;
import pulsarhunter.displaypanels.MKPlot2D;
import pulsarhunter.jreaper.Colourmap;

/**
 *
 * @author mkeith
 */
public class MultiProfilePlotter implements PulsarHunterProcess{
    
    
    MultiprofileFile infile;
    /** Creates a new instance of MultiProfilePlotter */
    public MultiProfilePlotter(MultiprofileFile infile) {
        this.infile = infile;
    }
    
    public void drawSubints(int startBin, int endBin, int startInt, int endInt){
        
        JPanel panel = new JPanel();
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(panel,BorderLayout.CENTER);
        frame.setSize(1024,768);
        frame.setBackground(Color.white);
        
        
        if(endInt < 1)endInt=infile.getHeader().getNumberOfTimeStamps();
        if(endBin < 1)endBin=infile.getProfile(0,0,0).length;
        
        
        MKPlot2D plot = new MKPlot2D(Colourmap.defaultGreyColmap);
        double[] sumprof = new double[endBin-startBin];
        double[] xvals = new double[endBin-startBin];
        double[] yvals = new double[endInt-startInt];
        
        double[][] profile=new double[endBin-startBin][endInt-startInt];
        for(int i = startInt; i < endInt; i++){
            double max = -Double.MAX_VALUE;
            double min = Double.MAX_VALUE;
            double[] origprof = infile.getProfile(0,i,0);
            if(endBin < 1)endBin=origprof.length;
            for(int b = 0; b < profile.length; b++){
                profile[b][i] = origprof[b+startBin];
                sumprof[b] += profile[b][i];
                if(profile[b][i] > max)max = profile[b][i];
                if(profile[b][i] < min)min = profile[b][i];
            }
            for(int b = 0; b < profile.length; b++){
                profile[b][i] = (profile[b][i]-min)/(max-min);
            }
            
        }
        
        
        for(int i = startInt; i < endInt; i++){
            yvals[i]=i*infile.getHeader().getTobs()/infile.getHeader().getNumberOfTimeStamps();
        }
        System.out.println(infile.getHeader().getTobs());
        
        for(int i = startBin; i < endBin; i++){
            xvals[i] = ((double)i)*this.infile.getHeader().getBinWidth()*1000.0;
        }
        plot.setXLabel("Pulse Phase (ms)");
        
        System.out.println(xvals.length+" "+yvals.length);
        
        frame.validate();
        frame.setVisible(true);
        
        plot.setYLabel("Observation Time (s)");
        plot.setValues(xvals,yvals,profile);
        plot.paintImage((Graphics2D)panel.getGraphics(),panel.getHeight()-200,panel.getWidth(),0,0);
        
        MKPlot plot2 = new MKPlot(Color.RED,Color.GREEN);
        plot2.setVals(xvals,sumprof);
        plot2.paintImage((Graphics2D)panel.getGraphics(),200,panel.getWidth(),0,panel.getHeight()-200);
        
        
    }
    
    public void run() {
        drawSubints(0,-1,0,-1);
        
    }
    
}
