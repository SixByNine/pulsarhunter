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
 * PlotType.java
 *
 * Created on 24 May 2005, 21:50
 */
package pulsarhunter.bookkeepr.jreaper;

import bookkeepr.xmlable.RawCandidateBasic;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author mkeith
 */
public class PlotType {

    public enum axisType {

        FoldSNR, SpecSNR, ReconSNR, BaryPeriod, TopoPeriod, DM, Accel, Jerk, Score, Freq, MJD, RA, Dec
    };
    private axisType yAxis;
    private axisType xAxis;
    private axisType zAxis;
    double zmin, zmax;

    /** Creates a new instance of PlotType */
    public PlotType(axisType xAxis, axisType yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = null;
    }

    public PlotType(axisType xAxis, axisType yAxis, axisType zAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
    }

    public void calibrateZ(RawCandidateBasic[] cands, double maxval, double minval) {

        for (int i = 0; i < cands.length; i++) {
            double val = getVal(cands[i], zAxis);
            if (val < zmin) {
                zmin = val;
            }
            if (val > zmax) {
                zmax = val;
            }
        }

        if (zmin < minval) {
            zmin = minval;
        }
        if (zmax > maxval) {
            zmax = maxval;
        }
    }

    public void calibrateZ(RawCandidateBasic[] cands) {
        calibrateZ(cands, Double.MAX_VALUE, Double.MIN_VALUE);
    }

    public double getXval(RawCandidateBasic cand) {
        return getVal(cand, xAxis);
    }

    public double getYval(RawCandidateBasic cand) {
        return getVal(cand, yAxis);
    }

    public int getZval(RawCandidateBasic cand) {
        if (zAxis == null) {
            return -1;
        }
        int ans = (int) (255 * (getVal(cand, zAxis) - zmin) / (zmax - zmin));
        if (ans > 255) {
            ans = 255;
        }
        if (ans < 0) {
            ans = 0;
        }
        return ans;
    }

    public boolean hasZ() {
        return zAxis != null;
    }

    double getVal(RawCandidateBasic cand, axisType axis) {
        switch (axis) {
            case BaryPeriod:
                return cand.getBaryPeriod();
            case TopoPeriod:
                return cand.getTopoPeriod();
            case DM:
                return cand.getDm();
            case FoldSNR:
                return cand.getFoldSnr();
            case ReconSNR:
                return cand.getReconSnr();
            case SpecSNR:
                return cand.getSpecSnr();
            case Accel:
                return cand.getAccel();
            case Jerk:
                return cand.getJerk();
            case Score:
                return 0;
            case Freq:
                return 1000.0 / cand.getTopoPeriod();
            case RA:
                return 0;
            case Dec:
                return 0;
            case MJD:
                return cand.getMjd();
        }
        return 0;
    }

    public String getXlabel() {
        return getLabel(xAxis);
    }

    public String getYlabel() {
        return getLabel(yAxis);
    }

    String getLabel(axisType axis) {
        switch (axis) {
            case BaryPeriod:
                return "Barycentric Period";
            case TopoPeriod:
                return "Topocentric Period";
            case DM:
                return "Dispersion Measure";
            case FoldSNR:
                return "Folded Signal to Noise";
            case ReconSNR:
                return "Reconstructed Signal to Noise";
            case SpecSNR:
                return "Spectral Signal to Noise";
            case Jerk:
                return "Jerk";
            case Accel:
                return "Acceleration";
            case Score:
                return "Score";
            case RA:
                return "Right Asscention";
            case Dec:
                return "Declination";
            case MJD:
                return "Modified Julian Date";

        }

        return null;
    }

    public RawCandidateBasic getNearest(RawCandidateBasic[] cands, double x, double y, double xscale, double yscale) {
        double distmin = Double.MAX_VALUE;
        double curdist = 0.0;
        RawCandidateBasic curCand = null;
        for (int i = 0; i < cands.length; i++) {
            curdist = Math.pow(((getXval(cands[i]) - x) * xscale), 2.0) + Math.pow(((getYval(cands[i]) - y) * yscale), 2.0);
            if (curdist < distmin) {
                distmin = curdist;
                curCand = cands[i];
            }

        }
        return curCand;

    }

    public RawCandidateBasic[] zoom(RawCandidateBasic[] masterData, double lowx, double highx, double lowy, double highy) {
        ArrayList<RawCandidateBasic> cData = new ArrayList<RawCandidateBasic>();
        for (int j = 0; j < masterData.length; j++) {
            if (getXval(masterData[j]) < highx && getXval(masterData[j]) < lowx && getYval(masterData[j]) < highy && getYval(masterData[j]) < lowy) {
                cData.add(masterData[j]);
            }
        }
        RawCandidateBasic[] curData = new RawCandidateBasic[cData.size()];
        Iterator<RawCandidateBasic> itr = cData.iterator();
        int j = 0;
        while (itr.hasNext()) {
            curData[j] = (RawCandidateBasic) itr.next();
            j++;
        }
        return curData;
    }
}

