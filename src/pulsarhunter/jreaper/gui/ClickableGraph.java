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
 * ClickableGraph.java
 *
 * Created on 24 May 2005, 20:08
 */
package pulsarhunter.jreaper.gui;

import coordlib.Beam;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;

import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.DataLibrary;

import pulsarhunter.jreaper.KnownPSR;
import pulsarhunter.jreaper.Main;
import pulsarhunter.jreaper.PlotType;
import coordlib.Coordinate;

/**
 *
 * @author  mkeith
 */
public class ClickableGraph extends Plot {

    MainView master;

    /** Creates new form ClickableGraph */
    public ClickableGraph(final Cand[][] data, final PlotType plotType, MainView master) {
        super();
        _setPadding(0.02);

        initComponents();
        this.master = master;
        changeData(data, plotType, true,null);

    }

    public void changeData(final Cand[][] data, final PlotType plotType, final boolean showKnown, final Cand last) {
        galactic = false;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                synchronized (this) {

                    // Create a sample plot.

                    clear(true);
                    setGrid(false);


                    setTitle("Main View");
                    setYLabel(plotType.getYlabel());
                    setXLabel(plotType.getXlabel());

                    // Create the stripes in data form (arrays).
                    setMarksStyle("unviewed");

                    if (plotType.hasZ()) {
                        PlotBox.changeColor(master.getDataLibrary().getOptions().getZColorMap().getCols());
                        for (int i = 0; i < 256; i++) {
                            setMarksStyle("viewed", i + 256);
                        }
                        for (int j = 0; j < data.length; j++) {
                            for (int i = 0; i < data[j].length; i++) {
                                if (data[j][i].getCandClass() >= 0) {
                                    if (master.show(data[j][i].getHarmonicType())) {
                                        if (data[j][i].beenViewed() || data[j][i].getCandClass() == 4) {
                                            addPoint(511 - plotType.getZval(data[j][i]), plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                        } else {
                                            addPoint(255 - plotType.getZval(data[j][i]), plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                        }
                                    }
                                } else {
                                    if (data[j][i].beenViewed()) {
                                        addPoint(511 - plotType.getZval(data[j][i]), plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                    } else {
                                        addPoint(255 - plotType.getZval(data[j][i]), plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                    }
                                }
                            }
                        }
                    } else {
                        setMarksStyle("viewed", 5);
                        setMarksStyle("viewed", 6);
                        setMarksStyle("viewed", 7);
                        setMarksStyle("viewed", 8);
                        setMarksStyle("viewed", 14);

                        setMarksStyle("halo", 10);
                        setMarksStyle("halo", 11);
                        setMarksStyle("halo", 12);
                        setMarksStyle("halo", 13);
                        setMarksStyle("halo", 16);


                        PlotBox.changeColor(master.getDataLibrary().getOptions().getStdColmap().getCols());
                        for (int j = 0; j < data.length; j++) {
                            for (int i = 0; i < data[j].length; i++) {
                                if (data[j][i].getCandClass() >= 0) {

                                    if (data[j][i].getCandClass() == 4) {
                                        if (data[j][i].beenViewed()) {
                                            addPoint(14, plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                        } else {
                                            addPoint(15, plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                        }
                                    } else {
                                        if (master.show(data[j][i].getHarmonicType())) {
                                            if (master.mark(data[j][i].getHarmonicType())) {
                                                if (data[j][i].beenViewed()) {
                                                    addPoint(5 + j, plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                                } else {
                                                    addPoint(j, plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                                }

                                                addPoint(10 + data[j][i].getCandClass(), plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                            } else {
                                                if (data[j][i].beenViewed()) {
                                                    addPoint(5 + j, plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                                } else {
                                                    addPoint(j, plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (data[j][i].beenViewed()) {
                                        addPoint(5 + j, plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                    } else {
                                        addPoint(j, plotType.getXval(data[j][i]), plotType.getYval(data[j][i]), false);
                                    }
                                }

                            }

                        }
                        if(last!=null){
                            addPoint(16,plotType.getXval(last),plotType.getYval(last),false);
                        }
                    }
                    master.rezoom(); // this was commented out, why?
                }
            }
        });


        repaint();
    }
    private boolean galactic = false;
    private List<Beam> beams = null;
    private String[] ingoreBeams = null;

    public void galacticPlot(final List<Beam> beams, final List<KnownPSR> knownPulsars, final Coordinate mark, final String[] ingoreBeams, final Cand[][] data) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                synchronized (ClickableGraph.this) {
                    PlotBox.changeColor(new Color[]{Color.red, Color.white});
                    galactic = true;
                    ClickableGraph.this.beams = beams;
                    ClickableGraph.this.ingoreBeams = ingoreBeams;
                    Arrays.sort(ingoreBeams, String.CASE_INSENSITIVE_ORDER);
                    clear(true);
                    setGrid(false);
                    setMarksStyle("dots");
                    for (Beam beam : beams) {
                        if (Arrays.binarySearch(ingoreBeams, beam.getName(), String.CASE_INSENSITIVE_ORDER) < 0) {
                            drawBeam(beam, (ClickableGraph.this.master.getDataLibrary().getOptions().getDistmax()) / 10.0);

                        }
                    }
                    master.rezoom();
                    for (KnownPSR psr : knownPulsars) {
                        //PlotBox.changeColor(Colourmap.getCurrentSTDcolormap().getCols());
                        double gb = psr.getPosition().getGb();
                        double gl = psr.getPosition().getGl();
                        //addPoint(0,psr.getPosition().getRA().toDegrees(),psr.getPosition().getDec().toDegrees(),false);
                        addPoint(0, psr.getPosition().getGl(), psr.getPosition().getGb(), false);
                    }
                    for (int j = 0; j < data.length; j++) {
                        for (int i = 0; i < data[j].length; i++) {
                            if (data[j][i].getCandClass() >= 0 && data[j][i].getCandClass() < 4) {
                                Coordinate coord = new Coordinate(data[j][i].getRA(), data[j][i].getDec());
                                addPoint(10 + data[j][i].getCandClass(), coord.getGl(), coord.getGb(), false);
                            }
                        }
                    }
                }
            }
        });
    }

    private void drawBeam(Beam beam, double width) {
        Coordinate coord = beam.getCoord();
        double gb = coord.getGb();
        double gl = coord.getGl();
        //double gl = coord.getRA().toDegrees();
        //double gb = coord.getDec().toDegrees();

        setMarksStyle("points", 1);


        addPoint(1, gl + width, gb + width, false);
        addPoint(1, gl, gb + width * 1.25, true);
        addPoint(1, gl - width, gb + width, true);
        addPoint(1, gl - width, gb - width, true);
        addPoint(1, gl, gb - width * 1.25, true);
        addPoint(1, gl + width, gb - width, true);
        addPoint(1, gl + width, gb + width, true);



    }

    public void zoomN(double lowx, double lowy, double highx, double highy) {
        master.zoom(lowx, highx, lowy, highy);
        super.zoom(lowx, lowy, highx, highy);
        setXRange(lowx, highx);
        setYRange(lowy, highy);
        repaint();
    }

    public void zoom(double lowx, double lowy, double highx, double highy) {
        master.zoomU(lowx, highx, lowy, highy);
        super.zoom(lowx, lowy, highx, highy);
        setXRange(lowx, highx);
        setYRange(lowy, highy);
        repaint();
    }
    private int markx = -1;
    private boolean marking = false;
    private int markxn = -1;
    private boolean markdrawn = false;
    private boolean unmark = false;

    private void markStart(int x) {

        if (x > _lrx) {
            x = _lrx;
        }
        if (x < _ulx) {
            x = _ulx;
        }
        markx = x;

        marking = true;
    }

    private void mark(int x) {
        marking = false;

        Graphics graphics = getGraphics();
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;
        }

        boolean handled = false;
        if (markdrawn == true) {
            if (markxn != -1) {
                // erase previous rectangle.
                int minx = Math.min(markx, markxn);
                int maxx = Math.max(markx, markxn);
                graphics.setXORMode(getBoxColor());
                graphics.drawRect(minx, _uly + 1, maxx - minx, _lry - 34);
                graphics.setPaintMode();
            }
        }



        if (x < markx) {
            int xtmp = markx;
            markx = x;
            x = xtmp;
        }


        final double xposn1 = getXPosn(x);
        final double xposn2 = getXPosn(markx);




        ClickableGraph.this.master.markAreaDud(xposn1, xposn2, unmark);

        //addPoint(1,xposn,yposn,false);
        //System.out.println("X: "+xposn+"  Y: "+yposn);
        //repaint();



        markdrawn = false;

        markx = markxn = -1;
        //}
    }

    private void drawMark(int x) {
        if (!marking) {
            return;
        }

        Graphics graphics = getGraphics();
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;
        }

        // Bound the rectangle so it doesn't go outside the box.
        if (x > _lrx) {
            x = _lrx;
        }
        if (x < _ulx) {
            x = _ulx;
        }
        // erase previous rectangle, if there was one.
        if (markx != -1) {

            // Erase the previous box if necessary.
            if ((markxn != -1) && (markdrawn == true)) {
                int minx = Math.min(markx, markxn);
                int maxx = Math.max(markx, markxn);

                graphics.setXORMode(PlotBox.getBoxColor());
                graphics.drawRect(minx, _uly + 1, maxx - minx, _lry - 34);
            }
            // Draw a new box if necessary.
            //if (x > markx) {
            markxn = x;

            int minx = Math.min(markx, markxn);
            int maxx = Math.max(markx, markxn);
            graphics.setXORMode(PlotBox.getBoxColor());
            graphics.drawRect(minx, _uly + 1, maxx - minx, _lry - 34);
            markdrawn = true;
            return;
            //} else markdrawn = false;
        }

    }
    private int clickx = -1;
    private int clicky = -1;
    private boolean clicking = false;
    private int clickxn = -1;
    private int clickyn = -1;
    private boolean clickdrawn = false;
    private boolean clickSelected = false;

    private void clickStart(int x, int y) {
        System.out.println(x + "," + y);
        if (y > _lry) {
            y = _lry;
        }
        if (y < _uly) {
            y = _uly;
        }
        if (x > _lrx) {
            x = _lrx;
        }
        if (x < _ulx) {
            x = _ulx;
        }
        clickx = x;
        clicky = y;

        clicking = true;
    }

    private void clickDraw(int x, int y) {
        if (!clicking) {
            return;
        }

        Graphics graphics = getGraphics();
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;
        }

        // Bound the rectangle so it doesn't go outside the box.
        if (y > _lry) {
            y = _lry;
        }
        if (y < _uly) {
            y = _uly;
        }
        if (x > _lrx) {
            x = _lrx;
        }
        if (x < _ulx) {
            x = _ulx;
        }
        // erase previous rectangle, if there was one.
        if (clickx != -1 || clicky != -1) {

            // Erase the previous box if necessary.
            if ((clickxn != -1 || clickyn != -1) && (clickdrawn == true)) {
                int minx = Math.min(clickx, clickxn);
                int maxx = Math.max(clickx, clickxn);
                int miny = Math.min(clicky, clickyn);
                int maxy = Math.max(clicky, clickyn);

                graphics.setXORMode(Color.blue);
                graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
            }
            // Draw a new box if necessary.
            //if (x > markx) {
            clickxn = x;
            clickyn = y;

            int minx = Math.min(clickx, clickxn);
            int maxx = Math.max(clickx, clickxn);
            int miny = Math.min(clicky, clickyn);
            int maxy = Math.max(clicky, clickyn);
            graphics.setXORMode(Color.blue);
            graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
            clickdrawn = true;
            return;
            //} else markdrawn = false;
        }
    }

    private void clickArea(int x, int y) {
        clicking = false;

        Graphics graphics = getGraphics();
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;
        }

        boolean handled = false;
        if (clickdrawn == true) {
            if (clickxn != -1 || clickyn != -1) {
                // erase previous rectangle.
                int minx = Math.min(clickx, clickxn);
                int maxx = Math.max(clickx, clickxn);

                int miny = Math.min(clicky, clickyn);
                int maxy = Math.max(clicky, clickyn);

                graphics.setXORMode(Color.blue);
                graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                graphics.setPaintMode();
            }
        }



        if (x < clickx) {
            int xtmp = clickx;
            clickx = x;
            x = xtmp;
        }
        if (y < clicky) {
            int ytmp = clicky;
            clicky = y;
            y = ytmp;
        }


        final double xposn1 = getXPosn(x);
        final double xposn2 = getXPosn(clickx);

        final double yposn1 = getYPosn(y);
        final double yposn2 = getYPosn(clicky);




        ClickableGraph.this.master.clickArea(xposn1, xposn2, yposn1, yposn2, clickSelected);

        //addPoint(1,xposn,yposn,false);
        //System.out.println("X: "+xposn+"  Y: "+yposn);
        //repaint();



        clickdrawn = false;

        clickx = clickxn = -1;
        clicky = clickyn = -1;

    }

    public void paint(Graphics g) {

        super.paint(g);
        if (galactic) {
            double width = this.master.getDataLibrary().getOptions().getDistmax() / 3.0;
            for (Beam beam : beams) {
                if (Arrays.binarySearch(ingoreBeams, beam.getName(), String.CASE_INSENSITIVE_ORDER) < 0) {
                    g.setColor(Color.black);
                    if (getXscale() > 100) {
                        String[] elems = beam.getName().split("_");
                        for (int i = 0; i < elems.length; i++) {
                            g.drawString(elems[i], (int) getScreenX(beam.getCoord().getGl() - 0.12), (int) (getScreenY(beam.getCoord().getGb()) + 20 * i - 10));
                        }
                    }
                    g.drawArc((int) getScreenX(beam.getCoord().getGl() - width / 2.0), (int) getScreenY(beam.getCoord().getGb() + width / 2.0), (int) (width * getXscale()), (int) (width * getYscale()), 0, 360);
                } else {
                    g.setColor(Color.lightGray);
                    if (getXscale() > 100) {
                        String[] elems = beam.getName().split("_");
                        for (int i = 0; i < elems.length; i++) {
                            g.drawString(elems[i], (int) getScreenX(beam.getCoord().getGl() - 0.12), (int) (getScreenY(beam.getCoord().getGb()) + 20 * i - 10));
                        }
                    }
                    g.drawArc((int) getScreenX(beam.getCoord().getGl() - width / 2.0), (int) getScreenY(beam.getCoord().getGb() + width / 2.0), (int) (width * getXscale()), (int) (width * getYscale()), 0, 360);
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

        setBackground(new java.awt.Color(255, 249, 230));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                dragListener(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clickListener(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                markListener(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                markReleasedListener(evt);
            }
        });

    }// </editor-fold>//GEN-END:initComponents

    private void dragListener(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dragListener
        //System.out.println("D"+evt.getButton());

        this.drawMark(evt.getX());
        this.clickDraw(evt.getX(), evt.getY());
    }//GEN-LAST:event_dragListener

    private void markReleasedListener(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_markReleasedListener
        //System.out.println("R"+evt.getButton());

        if (evt.getButton() == evt.BUTTON2) {
            unmark = false;
            int onmask = evt.CTRL_DOWN_MASK;
            int offmask = evt.SHIFT_DOWN_MASK;
            if ((evt.getModifiersEx() & (onmask | offmask)) == onmask) {
                unmark = true;
            }


            this.mark(evt.getX());

        }

        if (evt.getButton() == evt.BUTTON1) {

            clickSelected = false;
            int onmask = evt.SHIFT_DOWN_MASK | evt.ALT_DOWN_MASK;
            int offmask = evt.CTRL_DOWN_MASK;
            if ((evt.getModifiersEx() & (onmask | offmask)) == onmask) {
                clickSelected = true;
            }
            if (clicking) {
                this.clickArea(evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_markReleasedListener

    private void markListener(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_markListener
        //System.out.println("P"+evt.getButton());
        if (evt.getButton() == evt.BUTTON2) {
            this.markStart(evt.getX());
        }

        if (evt.getButton() == evt.BUTTON1) {
            int onmask = evt.SHIFT_DOWN_MASK;
            int offmask = evt.CTRL_DOWN_MASK;
            if ((evt.getModifiersEx() & (onmask | offmask)) == onmask) {
                this.clickStart(evt.getX(), evt.getY());
            }

        }
    }//GEN-LAST:event_markListener

    /*
    public class ZoomListener implements MouseListener {
    public void mouseClicked(MouseEvent event) {
    requestFocus();
    }
    public void mouseEntered(MouseEvent event) {
    }
    public void mouseExited(MouseEvent event) {
    }
    public void mousePressed(MouseEvent event) {
    // http://developer.java.sun.com/developer/bugParade/bugs/4072703.html
    // BUTTON3_MASK still not set for MOUSE_PRESSED events
    // suggests:
    // Workaround
    //   Assume that a press event with no modifiers must be button 1.
    //   This has the serious drawback that it is impossible to be sure
    //   that button 1 hasn't been pressed along with one of the other
    //   buttons.
    // This problem affects Netscape 4.61 under Digital Unix and
    // 4.51 under Solaris
    if ((event.getModifiers() & event.BUTTON3_MASK) != 0 ||
    event.getModifiers() == 0) {
    PlotBox.this._zoomStart(event.getX(), event.getY());
    }
    }
    public void mouseReleased(MouseEvent event) {
    if ((event.getModifiers() & event.BUTTON3_MASK) != 0 ||
    event.getModifiers() == 0) {
    PlotBox.this._zoom(event.getX(), event.getY());
    }
    }
    }

    public class DragListener implements MouseMotionListener {
    public void mouseDragged(MouseEvent event) {
    // NOTE: Due to a bug in JDK 1.1.7B, the BUTTON3_MASK does
    // not work on mouse drags.  It does work on MouseListener
    // methods, so those methods set a variable _zooming that
    // is used by _zoomBox to determine whether to draw a box.
    // if ((event.getModifiers() & event.BUTTON3_MASK)!= 0) {
    PlotBox.this._zoomBox(event.getX(), event.getY());
    // }
    }
    public void mouseMoved(MouseEvent event) {
    }
    }
     */
    private void clickListener(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clickListener

        if (evt.getButton() == evt.BUTTON1) {
            Point p = evt.getPoint();
            Rectangle frame = this.getBounds();
            double[] xrange = getXRange();
            double[] yrange = getYRange();

            //final double xposn = (p.getX()-35)/(frame.getWidth()-68)*(xrange[1]-xrange[0]) + xrange[0];
            //final double yposn = ((frame.getHeight() - p.getY()-35)/(frame.getHeight()-68))*(yrange[1]-yrange[0]) + yrange[0];
            final double xposn = getXPosn(p.getX());
            final double yposn = getYPosn(p.getY());

            //addPoint(19,xposn,yposn,false);
            int onmask = evt.CTRL_DOWN_MASK;
            int offmask = evt.SHIFT_DOWN_MASK;
            if ((evt.getModifiersEx() & (onmask | offmask)) == onmask) {
                Runnable task = new Runnable() {

                    public void run() {
                        master.clickOnHold(xposn, yposn);
                    }
                };
                new Thread(task).start();
            }

            offmask = evt.SHIFT_DOWN_MASK | evt.CTRL_DOWN_MASK;

            if (evt.getModifiersEx() == 0) {

                Runnable task = new Runnable() {

                    public void run() {
                        master.clickOn(xposn, yposn);
                    }
                };
                new Thread(task).start();

            }

            //addPoint(1,xposn,yposn,false);
            //System.out.println("X: "+xposn+"  Y: "+yposn);
            //repaint();
        }
    }//GEN-LAST:event_clickListener
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
