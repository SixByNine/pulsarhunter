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
 * SigprocBandedTimeSeries.java
 *
 * Created on 03 November 2006, 08:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.datatypes.sigproc;

import coordlib.Coordinate;
import java.io.File;
import java.io.IOException;
import pulsarhunter.DataRecordType;
import coordlib.Telescope;
import pulsarhunter.datatypes.BulkReadable;
import pulsarhunter.datatypes.MultiChannelTimeSeries;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.datatypes.WritableMultiChannelTimeSeries;
import pulsarhunter.datatypes.WritableTimeSeries;

/**
 *
 * @author mkeith
 */
public class SigprocBandedTimeSeries extends MultiChannelTimeSeries implements WritableMultiChannelTimeSeries, BulkReadable<MultiChannelTimeSeries.Header> {

    private File subFile;
    private Header header;
    private SigprocTimeSeries fakeTS;
    private VirtualTimeSeries[] virtTS;

    public SigprocBandedTimeSeries(File subFile, int buffersize, boolean create) throws IOException {
        this.subFile = subFile;

        this.fakeTS = new SigprocTimeSeries(subFile, buffersize);


        if (!create) {
            this.makeHeaders();
        }
    }

    /** Creates a new instance of SigprocBandedTimeSeries */
    public SigprocBandedTimeSeries(File subFile, int buffersize) throws IOException {
        this(subFile, buffersize, false);


    }

    public TimeSeries getOnechannel(int channelNumber) {
        return virtTS[channelNumber];
    }

    public void release() {
    }

    public MultiChannelTimeSeries.Header getHeader() {
        return header;
    }

    public float[][] getDataAsFloats() {
        return null;
    }

    public void flush() throws IOException {
    }

    public String getDataType() {
        return "SIGPROCBANDEDTIMESERIES";
    }

    public SigprocHeader getSigprocHeader() {
        return header.getSigprocHeader();
    }

    public void copySigprocHeader(SigprocHeader h) {
        this.fakeTS.copySigprocHeader(h);
        this.makeHeaders();
    }

    public void read(long startPosn, float[] data) throws IOException {
        this.fakeTS.getBulkReadableInterface().read(startPosn, data);
    }

    public void read(long startPosn, byte[] data) throws IOException {
        this.fakeTS.getBulkReadableInterface().read(startPosn, data);
    }

    public void read(long startPosn, long[] data) throws IOException {
        this.fakeTS.getBulkReadableInterface().read(startPosn, data);
    }

    public void read(long startPosn, double[] data) throws IOException {
        this.fakeTS.getBulkReadableInterface().read(startPosn, data);
    }

    public void read(long startPosn, short[] data) throws IOException {
        this.fakeTS.getBulkReadableInterface().read(startPosn, data);
    }

    public void read(long startPosn, int[] data) throws IOException {
        this.fakeTS.getBulkReadableInterface().read(startPosn, data);
    }

    public BulkReadable getBulkReadableInterface() {
        if (this.fakeTS.getBulkReadableInterface() != null) {
            return this;
        } else {
            return null;
        }
    }

    public DataRecordType getDataRecordType() {
        return this.fakeTS.getBulkReadableInterface().getDataRecordType();
    }

    private void makeHeaders() {
        if (this.header == null) {
            this.header = new Header(fakeTS);
            virtTS = new VirtualTimeSeries[this.header.getNumChannel()];
            for (int i = 0; i < virtTS.length; i++) {
                virtTS[i] = new VirtualTimeSeries(fakeTS, i, virtTS.length);
            }
        }
    }

    public void writeBin(long bin, int channel, float value) {
        this.makeHeaders();
        this.virtTS[channel].writeBin(bin, value);
    }

    public void writeBins(long startBin, float[][] value) {
        this.makeHeaders();
        if (value.length != this.getHeader().getNumChannel()) {
            throw new IllegalArgumentException("Length of value array must be the same as the number of channels");
        }
        float[] singleFloatArr = new float[value.length * value[0].length];
        for (int c = 0; c < value.length; c++) {
            for (int b = 0; b < value[0].length; c++) {
                singleFloatArr[b * value.length + c] = value[c][b];
            }
        }
        long masterbin = startBin * this.getHeader().getNumChannel();
        this.fakeTS.writeBins(masterbin, singleFloatArr, 0, singleFloatArr.length);
    }

    public int getBufferSize() {
        return this.fakeTS.getBufferSize();
    }

    public void setBufferSize(int bufferSize) {
        this.fakeTS.setBufferSize(bufferSize);
    }

    private class Header extends MultiChannelTimeSeries.Header {

        private SigprocHeader header;
        private long npoints;

        private Header(SigprocTimeSeries fakeTS) {
            super();
            this.header = fakeTS.getSigprocHeader();
            this.npoints = ((subFile.length() - header.getHeaderLength()) / header.getNbits() * 8) / header.getNchans();

        }

        public double getTSamp() {
            return this.header.getTsamp();
        }

        public long getNPoints() {
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

        public boolean isChannelInterleaved() {
            return true;
        }
    }

    private class VirtualTimeSeries extends TimeSeries implements WritableTimeSeries {

        SigprocTimeSeries masterTS;
        int chanNum, nchans;
        TimeSeries.Header header;

        VirtualTimeSeries(SigprocTimeSeries masterTS, int chanNum, int nchans) {
            this.masterTS = masterTS;
            this.chanNum = chanNum;
            this.nchans = nchans;
            this.header = new TimeSeries.Header();
            header.setBandwidth(masterTS.getHeader().getBandwidth() / nchans);
            header.setFrequency(masterTS.getHeader().getFrequency() + chanNum * header.getBandwidth());
            header.setSourceID(masterTS.getHeader().getSourceID());
            header.setTSamp(masterTS.getHeader().getTSamp());
            header.setNPoints(masterTS.getHeader().getNPoints() / nchans);

            header.setMjdStart(masterTS.getHeader().getMjdStart());
            header.setTobs(header.getTSamp() * header.getNPoints());
        }

        public void release() {
            this.masterTS = null;
            this.header = null;
        }

        public TimeSeries.Header getHeader() {
            return header;
        }

        public float[] getDataAsFloats() {
            return null;
        }

        public float getBin(long bin) {
            //   System.out.println("vt: "+bin);
            long masterBin = bin * nchans + chanNum;

            return masterTS.getBin(masterBin);
        }

        public void flush() throws IOException {
            masterTS.flush();
        }

        public void writeBin(long bin, float value) {
            long masterBin = bin * nchans + chanNum;
            masterTS.writeBin(masterBin, value);
        }

        public void writeBins(long startBin, float[] value, int srcStart, int nbins) {
            throw new java.lang.NoSuchMethodError("This method should never have been called!");
        }

        public int getBufferSize() {
            return masterTS.getBufferSize();
        }

        public void setBufferSize(int bufferSize) {
            masterTS.setBufferSize(bufferSize);
        }
    }
}
