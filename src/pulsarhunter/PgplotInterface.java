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
 * PgplotInterface.java
 *
 * Created on 02 November 2006, 13:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter;

/**
 *
 * @author mkeith
 */
public class PgplotInterface {

    private boolean active = true;

    /** Creates a new instance of PgplotInterface */
    private PgplotInterface() {
    }
    // Because of pgplot, only one plot can be made at a time
    private static PgplotInterface currentPlotter = null;

    public synchronized static PgplotInterface getPlotter() {
        if (currentPlotter == null) {
            return new PgplotInterface();
        } else {
            return null;
        }
    }

    public void jpgopen(String device) {
        PgplotInterface.pgopen(device);
    }

    public void jpgclose() {
        PgplotInterface.pgclose();
        PgplotInterface.currentPlotter = null;
        this.active = false;
    }

    public float[] jpgqwin() {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        float[] ret = new float[4];
        pgqwin(ret);
        return ret;
    }

    public void jpgsch(float val) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgsch(val);
    }

    public void jpgsvp(float xleft, float xright, float ybot, float ytop) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgsvp(xleft, xright, ybot, ytop);
    }

    public void jpgswin(float xleft, float xright, float ybot, float ytop) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgswin(xleft, xright, ybot, ytop);
    }

    public void jpgmove(float x, float y) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgmove(x, y);
    }

    public void jpgdraw(float x, float y) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgdraw(x, y);
    }

    public void jpglab(String x, String y, String top) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pglab(x, y, top);
    }

    public void jpggray(float[] a, int idim, int jdim, int i1, int i2, int j1, int j2, float fg, float bg, float[] tr) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pggray(a, idim, jdim, i1, i2, j1, j2, fg, bg, tr);
    }

    public void jpgbox(String xopt, float xtic, int nxsub, String yopt, float ytic, int nysub) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgbox(xopt, xtic, nxsub, yopt, ytic, nysub);
    }

    public void quickgray(float[] dat, int nx, int ny, int nxx) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.quikgray(dat, nx, ny, nxx);
    }

    public void jpgtext(String text, float x, float y) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgtext(text, x, y);
    }

    public void jpgsci(int ci) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgsci(ci);
    }

    public void jpgshls(int ci, float h, float s, float l) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgshls(ci, h, s, l);
    }

    public void jpgcirc(float x, float y, float r) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgcirc(x, y, r);
    }

    public void jpgsfs(int fs) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgsfs(fs);
    }

    public void jpgscf(int fs) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgscf(fs);
    }

    public void jpgslw(int lw) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgslw(lw);
    }

    public void jpgsls(int ls) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgsls(ls);
    }

    public void jpgptxt(float x, float y, float angle, float fjust, String text) {
        if (!active) {
            throw new RuntimeException("Cannot plot using this plotter as it is no longer active.");
        }
        PgplotInterface.pgtxt(x, y, angle, fjust, text);
    }
    private static boolean avaliable = false;

    public static boolean isAvaliable() {
        return avaliable;
    }
    /*
     *  Try and load the libary on class load... Otherwise we set avaliable to false.
     *
     */


    static {

        try {

            PulsarHunter.loadLibrary("jpgplot");





            avaliable = true;
        } catch (java.lang.UnsatisfiedLinkError err) {
            System.err.println(err.getMessage());
            err.printStackTrace();

            // err.printStackTrace();


            avaliable = false;
        }
    }

    private static native void pgopen(String device);

    private static native void pgclose();

    private static native void pgtext(String text, float x, float y);

    private static native void pgsch(float val);

    private static native void pgscf(int val);

    private static native void pgsvp(float xleft, float xright, float ybot, float ytop);

    private static native void pgswin(float xleft, float xright, float ybot, float ytop);

    private static native void pgqwin(float[] coords);

    private static native void pgmove(float x, float y);

    private static native void pgdraw(float x, float y);

    private static native void pglab(String x, String y, String top);

    private static native void pggray(float[] a, int idim, int jdim, int i1, int i2, int j1, int j2, float fg, float bg, float[] tr);

    private static native void pgbox(String xopt, float xtic, int nxsub, String yopt, float ytic, int nysub);

    private static native void quikgray(float[] dat, int nx, int ny, int nxx);

    private static native void pgshls(int ci, float h, float s, float l);

    private static native void pgsci(int ci);

    private static native void pgcirc(float x, float y, float r);

    private static native void pgsfs(int ci);

    private static native void pgslw(int ci);

    private static native void pgsls(int ci);

    private static native void pgtxt(float x, float y, float angle, float fjust, String text);
}

