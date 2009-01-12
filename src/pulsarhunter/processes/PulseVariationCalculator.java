/*
 * MultiProfilePlotter.java
 *
 * Created on July 30, 2007, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.processes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
public class PulseVariationCalculator implements PulsarHunterProcess{
    
    boolean clicked = false;
    boolean clicked2 = false;
    int pulseStartBin = 0;
    int pulseEndBin=2;
    
    int pulseStartBin2 = 0;
    int pulseEndBin2=0;
    
    MultiprofileFile infile;
    /** Creates a new instance of MultiProfilePlotter */
    public PulseVariationCalculator(MultiprofileFile infile) {
        this.infile = infile;
    }
    
    public void compute(int startBin, int endBin, int startInt, int endInt){
        
        if(endInt < 1)endInt=infile.getHeader().getNumberOfTimeStamps();
        if(endBin < 1)endBin=infile.getProfile(0,0,0).length;
        try {
            
            PrintWriter out = new PrintWriter(new FileWriter("pulse_power.asc"));
            
            double[][] profile=new double[endInt-startInt][endBin-startBin];
            for(int i = startInt; i < endInt; i++){
                
                double offPulseSum = 0;
                double onPulseSum =0;
                double onPulseSum2 =0;
                int offPulseCount=0;
                int onPulseCount=0;
                int onPulseCount2=0;
                
                double[] origprof = infile.getProfile(0,i,0).clone();
                if(endBin < 1)endBin=origprof.length;
                
                
//                double max = -Double.MAX_VALUE;
//                double min = Double.MAX_VALUE;
//
//
//                for(int b = startBin; b < endBin; b++){
//
//
//                    if(origprof[b] > max)max = origprof[b];
//                    if(origprof[b] < min)min = origprof[b];
//                }
//                for(int b = startBin; b < endBin; b++){
//                    origprof[b] = (origprof[b]-min)/(max-min);
//                }
                
                
                int count = 0;
                double sum = 0;
                double ssq = 1;
                for(int b = startBin; b < endBin; b++){
                    double val = origprof[b];
                    if((b >= pulseStartBin && b <= pulseEndBin) || (b >= pulseStartBin2 && b <= pulseEndBin2)){
                        
                    } else {
                        sum+=val;
                        ssq+=val*val;
                        count++;
                    }
                }
                
                
                double mean = sum/(double)count;
                double var = (ssq - sum*mean)/(double)(count);
                
                
                for(int b = startBin; b < endBin; b++){
                    origprof[b] = (origprof[b] - mean)/Math.sqrt(var);
                }
                
                
                
                for(int b = startBin; b < endBin; b++){
                    double val = origprof[b];
                    if(b >= pulseStartBin && b <= pulseEndBin){
                        onPulseSum+=val;
                        onPulseCount++;
                    } else if(b >= pulseStartBin2 && b <= pulseEndBin2) {
                         onPulseSum2+=val;
                        onPulseCount2++;
                    }else {
                        offPulseSum+=val;
                        offPulseCount++;
                    }
                }
                
                
                
                double onMean = onPulseSum/(double)onPulseCount;
                double onMean2 = onPulseSum2/(double)onPulseCount2;
                double offMean = offPulseSum/(double)offPulseCount;
                
                
                out.println(i+"\t"+onMean+"\t"+onMean2+"\t"+offMean);
                
            }
            
            System.out.println("Pulse Powers output to pulse_power.asc");
            
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void drawSubints( int startBin,  int endBin,  int startInt,  int endInt){
        
        
        
        
        if(endInt < 1)endInt=infile.getHeader().getNumberOfTimeStamps();
        if(endBin < 1)endBin=infile.getProfile(0,0,0).length;
        
        
        
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
        
        
        for(int i = startBin; i < endBin; i++){
            // xvals[i] = ((double)i)*this.infile.getHeader().getBinWidth()*1000.0;
            xvals[i] = ((double)i);
        }
        
        
        final MKPlot2D plot = new MKPlot2D(Colourmap.defaultGreyColmap);
        plot.setXLabel("Pulse Phase (bin)");
        
        
        plot.setYLabel("Observation Time (s)");
        plot.setValues(xvals,yvals,profile);
        
        final MKPlot plot2 = new MKPlot(Color.RED,Color.GREEN);
        plot2.setForceImage(true);
        plot2.setVals(xvals,sumprof);
        
        JFrame frame = new JFrame();
        frame.setSize(1024,768);
        final JPanel panel = new JPanel(){
            
            public void paint(Graphics g) {
                super.paint(g);
                
                Graphics2D g2 = (Graphics2D)g;
                
                plot.paintImage(g2,this.getHeight(),this.getWidth(),0,0);
                
            }
        };
        final JPanel panel2 = new JPanel(){
            
            public void paint(Graphics g) {
                super.paint(g);
                
                Graphics2D g2 = (Graphics2D)g;
                
                plot2.paintImage(g2,this.getHeight(),this.getWidth(),0,0);
                
                double sb = PulseVariationCalculator.this.pulseStartBin;
                double eb = PulseVariationCalculator.this.pulseEndBin;
                
                g2.setXORMode(Color.MAGENTA);
                
                int x = plot2.getXScreenPosn(sb);
                int y = plot2.getXScreenPosn(plot2.getXmin());
                int w = plot2.getXScreenPosn(eb)-plot2.getXScreenPosn(sb);
                //int h = plot2.getXScreenPosn(plot2.getXmax())-y;
                int h = this.getHeight()-(plot2.getMargin()[2] + plot2.getMargin()[3]);
                // System.out.println(sb+"\t"+eb);
                // System.out.println(x+"\t"+y+"\t"+w+"\t"+h+"\t");
                
                g2.drawRect(x,y,w,h);
                g2.setXORMode(Color.MAGENTA.brighter().brighter());
                g2.fillRect(x+1,y+1,w-1,h-1);
                if(PulseVariationCalculator.this.clicked){
                    g2.fillRect(0,0,10,10);
                }
                
                sb = PulseVariationCalculator.this.pulseStartBin2;
                eb = PulseVariationCalculator.this.pulseEndBin2;
                g2.setXORMode(Color.YELLOW);
                
                x = plot2.getXScreenPosn(sb);
                y = plot2.getXScreenPosn(plot2.getXmin());
                w = plot2.getXScreenPosn(eb)-plot2.getXScreenPosn(sb);
                //int h = plot2.getXScreenPosn(plot2.getXmax())-y;
                h = this.getHeight()-(plot2.getMargin()[2] + plot2.getMargin()[3]);
                // System.out.println(sb+"\t"+eb);
                // System.out.println(x+"\t"+y+"\t"+w+"\t"+h+"\t");
                
                g2.drawRect(x,y,w,h);
                g2.setXORMode(Color.YELLOW.brighter().brighter());
                g2.fillRect(x+1,y+1,w-1,h-1);
                
                if(PulseVariationCalculator.this.clicked2){
                    g2.fillRect(0,10,10,10);
                }
                
            }
        };
        
        final int sb=startBin;
        final int eb=endBin;
        final int si=startInt;
        final int ei=endInt;
        panel2.addMouseListener(new MouseListener(){
            public void mouseReleased(MouseEvent mouseEvent) {
            }
            
            public void mousePressed(MouseEvent mouseEvent) {
            }
            
            public void mouseExited(MouseEvent mouseEvent) {
            }
            
            public void mouseEntered(MouseEvent mouseEvent) {
            }
            
            public void mouseClicked(MouseEvent mouseEvent) {
                if(mouseEvent.getButton()==MouseEvent.BUTTON1){
                    if((mouseEvent.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK){
                        if(PulseVariationCalculator.this.clicked2){
                            PulseVariationCalculator.this.clicked2=false;
                            PulseVariationCalculator.this.pulseEndBin2 = (int)plot2.getXPlotPosn(mouseEvent.getX());
                            
                        } else {
                            PulseVariationCalculator.this.clicked2=true;
                            PulseVariationCalculator.this.pulseStartBin2 = (int)plot2.getXPlotPosn(mouseEvent.getX());
                        }
                    } else {
                        if(PulseVariationCalculator.this.clicked){
                            PulseVariationCalculator.this.clicked=false;
                            PulseVariationCalculator.this.pulseEndBin = (int)plot2.getXPlotPosn(mouseEvent.getX());
                            
                        } else {
                            PulseVariationCalculator.this.clicked=true;
                            PulseVariationCalculator.this.pulseStartBin = (int)plot2.getXPlotPosn(mouseEvent.getX());
                        }
                    }
                    panel2.repaint();
                }
                
                if(mouseEvent.getButton()==MouseEvent.BUTTON3){
                    PulseVariationCalculator.this.compute(sb,eb,si,ei);
                }
            }
            
        });
        
        
        frame.setLayout(new java.awt.GridLayout(2,1));
        //      frame.add(panel,BorderLayout.CENTER);
        //     frame.add(panel2,BorderLayout.CENTER);
        frame.add(panel);
        
        frame.add(panel2);
        //panel.setSize(1024,500);
        // panel.setMinimumSize(new Dimension(200,200));
        //panel2.setSize(1024,200);
        // panel2.setMinimumSize(new Dimension(200,200));
        
        frame.setBackground(Color.white);
        
        frame.validate();
        frame.setVisible(true);
        
        
        
        
    }
    
    public void run() {
        drawSubints(0,-1,0,-1);
        
    }
    
}
