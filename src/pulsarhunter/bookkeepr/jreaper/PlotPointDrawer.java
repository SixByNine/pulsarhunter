/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pulsarhunter.bookkeepr.jreaper;

import java.awt.Graphics;
import pulsarhunter.jreaper.CandClass;
import pulsarhunter.jreaper.HarmonicType;

/**
 *
 * @author kei041
 */
public interface PlotPointDrawer {

    public void drawPoint(Graphics graphics, int x, int y, CandClass cl, HarmonicType htype,boolean possibleMatch, int zcolor, boolean viewed);
    public String getName();
}
