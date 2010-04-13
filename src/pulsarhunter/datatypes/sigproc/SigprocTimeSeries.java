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
 * SigprocTimeSeries.java
 *
 * Created on 28 September 2006, 10:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.datatypes.sigproc;

import coordlib.Coordinate;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import coordlib.Telescope;
import java.lang.reflect.Method;
import pulsarhunter.datatypes.TimeSeries;

/**
 *
 * @author mkeith
 */
public abstract class SigprocTimeSeries extends TimeSeries {

    SigprocTimeSeries.Header header;
    File timFile;

    public SigprocTimeSeries(File timFile) throws IOException{
        this.timFile = timFile;
        this.header = new SigprocTimeSeries.Header(timFile);
    }
    //   private FileInputStream in = null;
    private long currentFilePos = Long.MAX_VALUE;

    /** Creates a new instance of SigprocTimeSeries */
    public SigprocTimeSeries() throws IOException {
    }

    public TimeSeries.Header getHeader() {
        return this.header;
    }

    public SigprocHeader getSigprocHeader() {
        return header.getSigprocHeader();
    }

    public void copySigprocHeader(SigprocHeader h) {
        this.header.copySigprocHeader(h);
    }

    private class Header extends TimeSeries.Header {

        private SigprocHeader header;
        private long npoints = 0;
        private double bandwidth = 1;

        private Header(File file) throws IOException {
            super();
            header = new SigprocHeader(file);

        }

        public void write(File timfile) throws IOException {
            if (this.header.getNchans() < 1) {
                this.header.setData_type(2); //Time series (apparently)
            }
            if (this.header.getNchans() < 1) {
                this.header.setNchans(1);
            }
            this.header.setNbeams(1);
            this.header.setNbits(32);
            this.header.setIbeam(1);
            this.header.setNifs(1);
            if (this.header.getNchans() < 1) {
                this.header.setFoff(bandwidth);
            }
            this.header.write(timfile);
        }

        public double getTSamp() {
            return this.header.getTsamp();
        }

        public long getNPoints() {
            this.npoints = (timFile.length() - header.getHeaderLength()) / header.getNbits() * 8;
            return npoints;
        }

        public double getFrequency() {
            return this.header.getFch1();
        }

        public double getMjdStart() {
            return this.header.getTstart();
        }

        public String getSourceID() {
            return this.header.getSource_name();
        }

        public double getBandwidth() {

            return this.header.getFoff() * this.header.getNchans();
        }

        public double getTobs() {
            return this.getTSamp() * this.getNPoints();
        }

        public int getHeaderLength() {
            return header.getHeaderLength();
        }

        public Coordinate getCoord() {
            return header.getCoordinate();
        }

        public void setTelescope(Telescope telescope) {
            this.header.setTelescope(telescope);
        }

        public Telescope getTelescope() {
            return this.header.getTelescope();
        }

        public void setDm(double dm) {
            this.header.setRefdm(dm);
        }

        public double getDm() {
            return this.header.getRefdm();
        }

        public void setNumChannel(int numChannel) {
            this.header.setNchans(numChannel);
        }

        public int getNumChannel() {
            return this.header.getNchans();
        }

        SigprocHeader getSigprocHeader() {
            return header;
        }

        public void setSourceID(String sourceID) {

            this.header.setSource_name(sourceID);
        }

        public void setTobs(double obstime) {
        }

        public void setTSamp(double tSamp) {
            this.header.setTsamp(tSamp);
        }

        public void setMjdStart(double mjdStart) {
            this.header.setMjdobs(mjdStart);
            this.header.setTstart(mjdStart);
        }

        public void setFrequency(double frequency) {
            header.setFch1(frequency);
        }

        public void setBandwidth(double bandwidth) {
            this.bandwidth = bandwidth;
        }

        public void setCoord(Coordinate coord) {
            header.setCoordinate(coord);
        }

        public void setBarryCentered(boolean barryCentered) {
            if (barryCentered) {
                header.setBarycentric(1);
            } else {
                header.setBarycentric(0);
            }
        }

        public void setNPoints(long nPoints) {
        }

        public boolean isBarryCentered() {
            return header.getBarycentric() != 0;
        }

        public void copySigprocHeader(SigprocHeader h) {
            for (Method m : header.getClass().getDeclaredMethods()) {
                if (m.getName().startsWith("get")) {
                    Method setter = null;
                    try {
                        setter = header.getClass().getMethod(m.getName().replace("get", "set"), m.getReturnType());
                    } catch (SecurityException ex) {
                    } catch (NoSuchMethodException ex) {
                    }
                    if (setter != null) {
                        try {
                            setter.invoke(header, m.invoke(h));
                        } catch (IllegalArgumentException ex) {
                        } catch (IllegalAccessException ex) {
                        } catch (InvocationTargetException ex) {
                        }
                    }
                }
            }
        }
    }
}
