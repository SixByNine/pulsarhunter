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
package pulsarhunter.bookkeepr.jreaper;

import bookkeepr.xmlable.RawCandidateBasic;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;
import javax.swing.SwingUtilities;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;
import pulsarhunter.jreaper.CandClass;
import pulsarhunter.jreaper.HarmonicType;

/**
 *
 * @author  mkeith
 */
public class ClickableGraph extends Plot {

    private static String[] comments = new String[]{
        "--JReaper--", "--JReaper--", "--JReaper--", "--JReaper--", "--JReaper--", "--JReaper--",
        "Choose thy candidates", "I'm feeling lucky", "Pick a pulsar, any pulsar", "...It's full of stars", "My brain hurts...",
        "--JReaper--", "--JReaper--", "--JReaper--", "--JReaper--", "--JReaper--", "--JReaper--",
        "JReaper: the final fronteer", "JReaper: Time to visit the optician?", "JReaper: It works when I do it...",
        "JReaper: Now in relaxing blue"
    };
    private JReaper master;
    private MainView mainView;
    private PlotPointDrawer pointDrawer;
    private boolean inZmode = false;

    /** Creates new form ClickableGraph */
    public ClickableGraph(final RawCandidateBasic[] data, final PlotType plotType, JReaper master, MainView mainView, PlotPointDrawer pointDrawer) {
        super();
        _setPadding(0.02);

        initComponents();
        this.master = master;
        this.mainView = mainView;
        changeData(data, plotType, pointDrawer);

    }

    public void changeData(final RawCandidateBasic[] data, final PlotType plotType, final PlotPointDrawer pointDrawer) {
        //galactic = false;

        Runnable task = new Runnable() {

            public void run() {
                synchronized (ClickableGraph.this) {

                    // Create a sample plot.
                    ClickableGraph.this.pointDrawer = pointDrawer;
                    clear(true);
                    setGrid(false);


                    setTitle(comments[(int) (Math.random() * comments.length)]);
                    setYLabel(plotType.getYlabel());
                    setXLabel(plotType.getXlabel());
                    setMarksStyle("unviewed");
                    // Create the stripes in data form (arrays).
                    inZmode = plotType.hasZ();
                    for (int i = 0; i < data.length; i++) {
                        addPoint(0, plotType.getXval(data[i]), plotType.getYval(data[i]), false,(long)CandStatus.setZAxis(plotType.getZval(data[i]),data[i].getPlotStatus()));

                    //0x0000ClVi
                    }


                    mainView.rezoom(); // this was commented out, why?

                }
            }
        };

        if (EventQueue.isDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }

        repaint();
    }

    public void zoom(double lowx, double lowy, double highx, double highy) {
        mainView.zoom(lowx, highx, lowy, highy);
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

        marking =
                true;
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
            markx =
                    x;
            x =
                    xtmp;
        }

        final double xposn1 = getXPosn(x);
        final double xposn2 = getXPosn(markx);




        ClickableGraph.this.master.markAreaDud(xposn1, xposn2, unmark);

        //addPoint(1,xposn,yposn,false);
        //System.out.println("X: "+xposn+"  Y: "+yposn);
        //repaint();



        markdrawn =
                false;

        markx =
                markxn = -1;
    //}
    }

    private void drawMark(int x) {
        if (!marking) {
            return;
        }

        Graphics graphics = getGraphics();
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;        // Bound the rectangle so it doesn't go outside the box.

        }

        if (x > _lrx) {
            x = _lrx;
        }

        if (x < _ulx) {
            x = _ulx;
        // erase previous rectangle, if there was one.
        }

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
            markdrawn =
                    true;
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
        clicky =
                y;

        clicking =
                true;
    }

    private void clickDraw(int x, int y) {
        if (!clicking) {
            return;
        }

        Graphics graphics = getGraphics();
        // Ignore if there is no graphics object to draw on.
        if (graphics == null) {
            return;        // Bound the rectangle so it doesn't go outside the box.

        }

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
        // erase previous rectangle, if there was one.
        }

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
            clickyn =
                    y;

            int minx = Math.min(clickx, clickxn);
            int maxx = Math.max(clickx, clickxn);
            int miny = Math.min(clicky, clickyn);
            int maxy = Math.max(clicky, clickyn);
            graphics.setXORMode(Color.blue);
            graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
            clickdrawn =
                    true;
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
            clickx =
                    x;
            x =
                    xtmp;
        }

        if (y < clicky) {
            int ytmp = clicky;
            clicky =
                    y;
            y =
                    ytmp;
        }

        final double xposn1 = getXPosn(x);
        final double xposn2 = getXPosn(clickx);

        final double yposn1 = getYPosn(y);
        final double yposn2 = getYPosn(clicky);




        ClickableGraph.this.master.clickArea(xposn1, xposn2, yposn1, yposn2, clickSelected);

        //addPoint(1,xposn,yposn,false);
        //System.out.println("X: "+xposn+"  Y: "+yposn);
        //repaint();



        clickdrawn =
                false;

        clickx =
                clickxn = -1;
        clicky =
                clickyn = -1;

    }

    @Override
    protected void _drawPoint(Graphics graphics,
            int dataset, long xpos, long ypos,
            boolean clip,long id) {

        // If the point is not out of range, draw it.
        boolean pointinside = ypos <= _lry && ypos >= _uly &&
                xpos <= _lrx && xpos >= _ulx;
        if (!clip || pointinside) {
            int xposi = (int) xpos;
            int yposi = (int) ypos;

            boolean viewed = CandStatus.isViewed((int) id);
            CandClass cl = CandStatus.getCandClass((int) id);
            HarmonicType htype = CandStatus.getHarmonicType((int) id);
            boolean possible = CandStatus.getPossible((int)id);
            int zcol = CandStatus.getZAxis((int) id);
            if (!inZmode) {
                zcol = -1;
            }
            pointDrawer.drawPoint(graphics,xposi, yposi, cl, htype, possible, zcol, viewed);


        }
    }

    public void paint(Graphics g) {

        super.paint(g);
//        if (galactic) {
//            double width = this.master.getDataLibrary().getOptions().getDistmax() / 3.0;
//            for (Beam beam : beams) {
//                if (Arrays.binarySearch(ingoreBeams, beam.getName(), String.CASE_INSENSITIVE_ORDER) < 0) {
//                    g.setColor(Color.black);
//                    if (getXscale() > 100) {
//                        String[] elems = beam.getName().split("_");
//                        for (int i = 0; i <
//                                elems.length; i++) {
//                            g.drawString(elems[i], (int) getScreenX(beam.getCoord().getGl() - 0.12), (int) (getScreenY(beam.getCoord().getGb()) + 20 * i - 10));
//                        }
//
//                    }
//                    g.drawArc((int) getScreenX(beam.getCoord().getGl() - width / 2.0), (int) getScreenY(beam.getCoord().getGb() + width / 2.0), (int) (width * getXscale()), (int) (width * getYscale()), 0, 360);
//                } else {
//                    g.setColor(Color.lightGray);
//                    if (getXscale() > 100) {
//                        String[] elems = beam.getName().split("_");
//                        for (int i = 0; i <
//                                elems.length; i++) {
//                            g.drawString(elems[i], (int) getScreenX(beam.getCoord().getGl() - 0.12), (int) (getScreenY(beam.getCoord().getGb()) + 20 * i - 10));
//                        }
//
//                    }
//                    g.drawArc((int) getScreenX(beam.getCoord().getGl() - width / 2.0), (int) getScreenY(beam.getCoord().getGb() + width / 2.0), (int) (width * getXscale()), (int) (width * getYscale()), 0, 360);
//                }
//
//            }
//        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(183, 202, 237));
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
        setLayout(new java.awt.BorderLayout());
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

            }//addPoint(1,xposn,yposn,false);
//System.out.println("X: "+xposn+"  Y: "+yposn);
//repaint();

        }
    }//GEN-LAST:event_clickListener
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
