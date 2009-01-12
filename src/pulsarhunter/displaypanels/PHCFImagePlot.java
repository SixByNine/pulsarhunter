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
 * PHCFImagePlot.java
 *
 * Created on 01 February 2007, 12:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.displaypanels;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Formatter;
import pulsarhunter.Convert;
import pulsarhunter.datatypes.PHCSection;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.displaypanels.MKPlot.LineStyle;
import pulsarhunter.jreaper.Colourmap;

/**
 *
 * @author mkeith
 */
public class PHCFImagePlot {
    
    private Colourmap colourmap;
    private Color c1;
    private Color c2;
    
    /** Creates a new instance of PHCFImagePlot */
    public PHCFImagePlot(Colourmap colourmap, Color c1, Color c2) {
        this.colourmap = colourmap;
        this.c1 = c1;
        this.c2 = c2;
    }
    
    /**
     * Plot layout
     * 5% reserved for header
     *
     * Left side
     * 25% Sub bands
     * 40% Sub ints
     * 30% profile
     *
     * Right Side
     * 25% Pdm
     * 25% dmcurve
     * 25% accurve
     * 20% params
     *
     */
    public void draw(PulsarHunterCandidate phcf, BufferedImage img){
        int height = img.getHeight();
        int width = img.getWidth();
        Graphics2D g = (Graphics2D)img.getGraphics();
        
        g.setColor(Color.WHITE);
        g.fillRect(0,0,width,height);
        
        g.setColor(new Color(0,0,0));
        
//        StringBuffer line = new StringBuffer();
//        line.append("File: "+phcf.getFile().getName());
//        line.append("   RA:"+phcf.getHeader().getCoord().getRA().toString(false));
//        line.append("   Dec:"+phcf.getHeader().getCoord().getDec().toString(false));
//
//        line.append("   Gl:"+round(phcf.getHeader().getCoord().getGl(),100));
//        line.append("   Gb:"+round(phcf.getHeader().getCoord().getGb(),100));
//        line.append("   MJD:"+round(phcf.getHeader().getMjdStart(),100));
//
//        g.drawString(line.toString(),5,15);
//
//
//
//        line = new StringBuffer();
//
//        line.append("ObsFreq:"+round(phcf.getHeader().getFrequency(),10)+"MHz");
//        line.append("   Tobs:"+round(phcf.getHeader().getTobs(),1)+"s");
//        if(phcf.getInitialSec().getTsamp()>0)line.append("   Tsamp:"+(int)(phcf.getHeader().getTsamp())+"us");
//        line.append("   SourceID:"+phcf.getHeader().getSourceID());
//        line.append("   Telescope:"+phcf.getHeader().getTelescope().toString());
        
        
//        g.drawString(line.toString(),5,30);
        
        
        StringBuffer line = new StringBuffer();
        line.append("File: "+phcf.getFile().getName());
        line.append("   RA:"+phcf.getHeader().getCoord().getRA().toString(false));
        line.append("   Dec:"+phcf.getHeader().getCoord().getDec().toString(false));
        
        line.append("   Gl:"+round(phcf.getHeader().getCoord().getGl(),100));
        line.append("   Gb:"+round(phcf.getHeader().getCoord().getGb(),100));
        line.append("   MJD:"+round(phcf.getHeader().getMjdStart(),100));
        
        
        
        
        g.drawString(line.toString(),5,10);
        
        line = new StringBuffer();
        
        line.append("ObsFreq:"+round(phcf.getHeader().getFrequency(),10)+"MHz");
        line.append("   Tobs:"+round(phcf.getHeader().getTobs(),1)+"s");
        if(phcf.getInitialSec().getTsamp()>0)line.append("   Tsamp:"+(int)(phcf.getHeader().getTsamp())+"us");
        line.append("   SourceID:"+phcf.getHeader().getSourceID());
        line.append("   Telescope:"+phcf.getHeader().getTelescope().toString());
        
        
        g.drawString(line.toString(),5,20);
        
        line = new StringBuffer();
        if(phcf.getInitialSec().getExtraValue("SPECSNR")!=null){
            line.append("SpecSNR:"+phcf.getInitialSec().getExtraValue("SPECSNR"));
        }
        if(phcf.getInitialSec().getExtraValue("Recon")!=null||phcf.getInitialSec().getExtraValue("RECONSNR")!=null){
            if(phcf.getInitialSec().getExtraValue("RECONSNR")!=null){
                line.append("   ReconSNR:"+round(Double.parseDouble(phcf.getInitialSec().getExtraValue("RECONSNR")),100));
            }else {
                line.append("   ReconSNR:"+round(Double.parseDouble(phcf.getInitialSec().getExtraValue("Recon")),100));
            }
        }
        
        if(phcf.getInitialSec().getExtraValue("HFOLD")!=null){
            line.append("   H-Fold:"+phcf.getInitialSec().getExtraValue("HFOLD"));
        }
        
        if(phcf.getHeader().getExtraValue("ZAP")!=null){
            line.append("   Zap:"+phcf.getHeader().getExtraValue("ZAP"));
        }
        
        
        g.drawString(line.toString(),5,30);
        
        
        
        
        
        
        
        
        
        /****************
         *    SubBands   *
         ****************/
        {
            int x = 0;
            int y = (int)(height*0.05);
            int xSize = (int)(width*0.5);
            int ySize = (int)(height*0.25);
            Graphics2D graphics = (Graphics2D) g.create(x,y,xSize,ySize);
            
            PHCSection sec = phcf.getOptimisedSec();
            if(sec.getSubbands() != null){
                    double[][] map_d = Convert.wrapDoubleArr(Convert.rotateDoubleArray(sec.getSubbands()),1.5);

                //   int[][]    map_i = Convert.doubleArrToIntArr(map_d,0,255,1.5);
                
                MKPlot plot =  new PlotTwoDim("Sub-Bands","Bin Number","Sub-Band Number",map_d,colourmap);
                plot.paintImage(graphics,xSize,ySize);
            }
            
        }
        /****************
         *    Subints   *
         ****************/
        {
            int x = 0;
            int y = (int)(height*0.30);
            int xSize = (int)(width*0.5);
            int ySize = (int)(height*0.40);
            Graphics2D graphics = (Graphics2D) g.create(x,y,xSize,ySize);
            
            PHCSection sec = phcf.getOptimisedSec();
            
            if(sec.getSubints() != null){
                double[][] map_d = Convert.wrapDoubleArr(Convert.rotateDoubleArray(sec.getSubints()),1.5);
                //int[][]    map_i = Convert.doubleArrToIntArr(map_d,0,255,1.5);
                
                
                
                MKPlot plot =  new PlotTwoDim("Sub-Integrations","Bin Number","Sub-Int Number",map_d,colourmap);
                plot.paintImage(graphics,xSize,ySize);
            }
            
            
            
        }
        
        
        
        {
            /****************
             *   Profiles   *
             ****************/
            int x = 0;
            int y = (int)(height*0.70);
            int xSize = (int)(width*0.5);
            int ySize = (int)(height*0.30);
            Graphics2D graphics = (Graphics2D) g.create(x,y,xSize,ySize);
            
            PHCSection sec = phcf.getOptimisedSec();
            if(sec.getPulseProfile()!=null){
                int nbins = sec.getPulseProfile().length;
                double[] yaxis = Convert.wrapDoubleArr(sec.getPulseProfile(),1.5);
                double[] xaxis = new double[yaxis.length];
                for(int j = 0; j < xaxis.length; j++)xaxis[j] = (double)j/(double)sec.getPulseProfile().length;
                
                double[] xpoints = new double[0];
                double[] ypoints = new double[0];
                
                MKPlot plot =new PlotOneDim("Profile","Phase","",xaxis,yaxis,xpoints,ypoints,c2,c1);
                plot.paintImage(graphics,xSize,ySize);
            }
        }
        
        /****************
         * Period / PDM *
         ****************/
        {
            int x = (int)(width*0.5);
            int y = (int)(height*0.05);
            int xSize = (int)(width*0.5);
            int ySize = (int)(height*0.25);
            Graphics2D graphics = (Graphics2D) g.create(x,y,xSize,ySize);
            
            PHCSection sec = phcf.getOptimisedSec();
            if(sec.getSnrBlock() != null && sec.getSnrBlock().getPeriodIndex().length > 1){
                if(sec.getSnrBlock().getDmIndex().length > 1){
                    // We have PDM to draw...
                    
                    double[][] map_d  = sec.getSnrBlock().getPDmPlane(sec.getBestAccn(),sec.getBestJerk());
                    //  int[][] map_i = Convert.doubleArrToIntArr(map_d,0,255,1);
                    
                    double[] periodIdx = sec.getSnrBlock().getPeriodIndex();
                    
                    String pType;
                    double period  = 0;
                    if(sec.getSnrBlock().isBarrycenter()){
                        period = sec.getBestBaryPeriod();
                        pType="Bary ";
                    } else {
                        period = sec.getBestTopoPeriod();
                        pType="Topo ";
                    }
                    for(int j = 0; j < periodIdx.length; j++){
                        periodIdx[j] -= period;
                        periodIdx[j]*=1000.0;
                    }
                    
                    
                    double[] dmIdx = sec.getSnrBlock().getDmIndex();
                    
                    //  double dmStep = dmIdx[1] - dmIdx[0];
                    //  double pStep = (periodIdx[1] - periodIdx[0])*1000.0;
                    
                    
                    //pdmPanels[i] = new PlotTwoDim("Period-DM Plane","Period","DM",map_i,periodIdx[0]*1000.0,pStep,dmIdx[0],dmStep);
                    MKPlot plot = new PlotTwoDim("Period-DM Plane",pType+"Period Offset from "+(1000.0*period)+"ms","DM",periodIdx,dmIdx,map_d,colourmap);
                    plot.paintImage(graphics,xSize,ySize);
                }else{
                    // We have just Period to draw...
                    double[] xaxis = sec.getSnrBlock().getPeriodIndex();
                    double[] yaxis = sec.getSnrBlock().getPeriodCurve(sec.getBestDm(),sec.getBestAccn(),sec.getBestJerk());
                    double period;
                    String pType;
                    if(sec.getSnrBlock().isBarrycenter()){
                        period = sec.getBestBaryPeriod();
                        pType="Bary ";
                    } else {
                        period = sec.getBestTopoPeriod();
                        pType="Topo ";
                    }
                    double minV = sec.getBestSnr();
                    for(double v : yaxis){
                        if(v < minV)minV = v;
                    }
                    
                    
                    
                    double[] model = Convert.generatePeriodCurve(xaxis,period,sec.getBestWidth(),phcf.getHeader().getTobs());
                    
                    // Convert range to plot range.
                    for(int j = 0; j < model.length; j++){
                        model[j] = model[j] * (sec.getBestSnr()- minV) + minV ;
                    }
                    
                    
                    for(int j = 0; j < xaxis.length; j++){
                        xaxis[j] -= period;
                        xaxis[j] *= 1000.0;
                    }
                    
                    MKPlot plot = new PlotOneDim("Period Curve",pType+"Period Offset from "+(1000.0*period)+"ms","SNR",xaxis,model,xaxis,yaxis,c1,c2);
                    ((PlotOneDim)plot).setJoinDots(true);
                    ((PlotOneDim)plot).setLinestyle(LineStyle.JoinTheDots);
                    plot.paintImage(graphics,xSize,ySize);
                }
                
            } else {
                // there are no period info... Put a blank plot for now
                
            }
        }
        
        
        
        
        /****************
         *    DM Curve   *
         ****************/
        {
            
            int x = (int)(width*0.5);
            int y = (int)(height*0.30);
            int xSize = (int)(width*0.5);
            int ySize = (int)(height*0.25);
            Graphics2D graphics = (Graphics2D) g.create(x,y,xSize,ySize);
            
            PHCSection sec = phcf.getInitialSec();
            if(sec.getSnrBlock() != null && sec.getSnrBlock().getDmIndex().length > 1){
                
                double[] xaxis = sec.getSnrBlock().getDmIndex();
                
                double[] yaxis;
                if(sec.getSnrBlock().isBarrycenter()){
                    //yaxis = sec.getSnrBlock().getDmCurve(sec.getBestBaryPeriod(),sec.getBestAccn(),sec.getBestJerk());
                    yaxis = sec.getSnrBlock().getFlatDmCurve();
                } else {
                    // yaxis = sec.getSnrBlock().getDmCurve(sec.getBestTopoPeriod(),sec.getBestAccn(),sec.getBestJerk());
                    yaxis = sec.getSnrBlock().getFlatDmCurve();
                }
                MKPlot plot =  new PlotOneDim("DM Curve","DM","SNR",new double[0],new double[0],xaxis,yaxis,c1,c2);
                ((PlotOneDim)plot).setJoinDots(true);
                
                plot.paintImage(graphics,xSize,ySize);
                
            }
            
        }
        
        
        
        /****************
         *  Acc Curve   *
         ****************/
        {
            int x = (int)(width*0.5);
            int y = (int)(height*0.55);
            int xSize = (int)(width*0.5);
            int ySize = (int)(height*0.25);
            Graphics2D graphics = (Graphics2D) g.create(x,y,xSize,ySize);
            
            PHCSection sec = phcf.getInitialSec();
            if(sec.getSnrBlock() != null && sec.getSnrBlock().getAccnIndex().length > 1){
                if (sec.getSnrBlock().getJerkIndex().length > 1){
                    double[][] map_d;
                    if(sec.getSnrBlock().isBarrycenter()){
                        
                        map_d  = sec.getSnrBlock().getAccnJerkPlane(sec.getBestDm(),sec.getBestBaryPeriod());
                    } else {
                        
                        map_d  = sec.getSnrBlock().getAccnJerkPlane(sec.getBestDm(),sec.getBestTopoPeriod());
                    }
                    
                    //  int[][] map_i = Convert.doubleArrToIntArr(map_d,0,255,1);
                    
                    double[] acIndex = sec.getSnrBlock().getAccnIndex();
                    double[] jeIndex = sec.getSnrBlock().getJerkIndex();
                    
                    //  double dmStep = dmIdx[1] - dmIdx[0];
                    //  double pStep = (periodIdx[1] - periodIdx[0])*1000.0;
                    
                    
                    //pdmPanels[i] = new PlotTwoDim("Period-DM Plane","Period","DM",map_i,periodIdx[0]*1000.0,pStep,dmIdx[0],dmStep);
                    MKPlot2D plot = new PlotTwoDim("Accn-Jerk Plane","Accn (m/s/s)","Jerk (m/s/s/s)",acIndex,jeIndex,map_d,colourmap);
                    plot.paintImage(graphics,xSize,ySize);
                } else {
                    // pdot only
                    double[] xaxis = sec.getSnrBlock().getAccnIndex();
                    double[] yaxis;
                        /*
                         if(sec.getSnrBlock().isBarrycenter()){
                            yaxis = sec.getSnrBlock().getAccnCurve(sec.getBestDm(),sec.getBestBaryPeriod(),sec.getBestJerk());
                        } else {
                            yaxis = sec.getSnrBlock().getAccnCurve(sec.getBestDm(),sec.getBestTopoPeriod(),sec.getBestJerk());
                        }
                         */
                    yaxis = sec.getSnrBlock().getFlatAccCurve();
                    
                    MKPlot plot = new PlotOneDim("Accn Curve","Accn (m/s/s)","SNR",new double[0],new double[0],xaxis,yaxis,c1,c2);
                    ((PlotOneDim)plot).setJoinDots(true);
                    plot.paintImage(graphics,xSize,ySize);
                }
                
            } else  if (sec.getSnrBlock() != null && sec.getSnrBlock().getJerkIndex().length > 1){
                // pddot only
                double[] xaxis = sec.getSnrBlock().getJerkIndex();
                double[] yaxis;
                if(sec.getSnrBlock().isBarrycenter()){
                    yaxis = sec.getSnrBlock().getJerkCurve(sec.getBestDm(),sec.getBestBaryPeriod(),sec.getBestAccn());
                } else {
                    yaxis = sec.getSnrBlock().getJerkCurve(sec.getBestDm(),sec.getBestTopoPeriod(),sec.getBestAccn());
                }
                MKPlot plot = new PlotOneDim("Jerk Curve","Jerk (m/s/s/s)","SNR",new double[0],new double[0],xaxis,yaxis,c1,c2);
                ((PlotOneDim)plot).setJoinDots(true);
                plot.paintImage(graphics,xSize,ySize);
            } else {
                // There is no pdot or pddot curve...
                
            }
        }
        {
            
            
            int x = (int)(width*0.5);
            int y = (int)(height*0.80);
            int xSize = (int)(width*0.5);
            int ySize = (int)(height*0.20);
            Graphics2D graphics = (Graphics2D) g.create(x,y,xSize,ySize);
            int[] col = new int[]{2,82,162,242};
            int[] row = new int[]{10,23,33,46,56,69,79,89,99,109};
            
            //graphics.drawRect(0,0,xSize-1,ySize-1);
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            
            graphics.drawString("Initial",col[1],row[0]);
            
            graphics.drawString("Optimised",col[2],row[0]);
            
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("Bary Period",col[0],row[1]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(""+this.round(phcf.getHeader().getInitialBaryPeriod()*1000,1e9),col[1],row[1]);
            graphics.drawString(""+this.round(phcf.getHeader().getOptimisedBaryPeriod()*1000,1e9),col[2],row[1]);
            graphics.drawString("ms",col[3],row[1]);
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("Bary Freq",col[0],row[2]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(""+this.round(1.0/phcf.getHeader().getInitialBaryPeriod(),1e9),col[1],row[2]);
            graphics.drawString(""+this.round(1.0/phcf.getHeader().getOptimisedBaryPeriod(),1e9),col[2],row[2]);
            graphics.drawString("Hz",col[3],row[2]);
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("Topo Period",col[0],row[3]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(""+this.round(phcf.getHeader().getInitialTopoPeriod()*1000,1e9),col[1],row[3]);
            graphics.drawString(""+this.round(phcf.getHeader().getOptimisedTopoPeriod()*1000,1e9),col[2],row[3]);
            graphics.drawString("ms",col[3],row[3]);
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("Topo Freq",col[0],row[4]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(""+this.round(1.0/phcf.getHeader().getInitialTopoPeriod(),1e9),col[1],row[4]);
            graphics.drawString(""+this.round(1.0/phcf.getHeader().getOptimisedTopoPeriod(),1e9),col[2],row[4]);
            graphics.drawString("Hz",col[3],row[4]);
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("Accn",col[0],row[5]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(this.shortStringOf(phcf.getHeader().getInitialAccn()),col[1],row[5]);
            graphics.drawString(this.shortStringOf(phcf.getHeader().getOptimisedAccn()),col[2],row[5]);
            graphics.drawString("m/s/s",col[3],row[5]);
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("Jerk",col[0],row[6]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(this.shortStringOf(phcf.getHeader().getInitialJerk()),col[1],row[6]);
            graphics.drawString(this.shortStringOf(phcf.getHeader().getOptimisedJerk()),col[2],row[6]);
            graphics.drawString("m/s/s/s",col[3],row[6]);
            
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("DM",col[0],row[7]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(""+this.round(phcf.getHeader().getInitialDm(),100.0),col[1],row[7]);
            graphics.drawString(""+this.round(phcf.getHeader().getOptimizedDm(),100.0),col[2],row[7]);
            graphics.drawString("cm/pc",col[3],row[7]);
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("Width",col[0],row[8]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(""+this.round(phcf.getHeader().getInitialWidth(),100.0),col[1],row[8]);
            graphics.drawString(""+this.round(phcf.getHeader().getOptimizedWidth(),100.0),col[2],row[8]);
            graphics.drawString("periods",col[3],row[8]);
            
            graphics.setFont(new Font("Monospaced",Font.BOLD,10));
            graphics.drawString("SNR",col[0],row[9]);
            graphics.setFont(new Font("Monospaced",0,10));
            graphics.drawString(""+this.round(phcf.getHeader().getInitialSNR(),100.0),col[1],row[9]);
            graphics.drawString(""+this.round(phcf.getHeader().getOptimizedSNR(),100.0),col[2],row[9]);
            graphics.drawString("periods",col[3],row[9]);
            
        }
    }
    
    
    private String shortStringOf(double d){
        
        StringBuilder build = new StringBuilder();
        Formatter formater = new Formatter(build);
        if(d==0){
            build.append("0.00");
        }else if(Math.abs(d) > 1000){
            formater.format("%5.4e",d);
        } else if(Math.abs(d) < 0.01){
            formater.format("%5.4e",d);
        } else {
            formater.format("%3.4f",d);
        }
        
        return build.toString();
    }
    
    
    
    private String round(double d, double r){
        if(d==-1)return "N/A";
        else return Double.toString(((long)(d*r))/r);
    }
}
