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
 * SubTimeSeries.java
 *
 * Created on 27 September 2006, 10:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.IOException;
import pulsarhunter.*;

/**
 *
 * @author mkeith
 */
public class SubMultiChannelTimeSeries extends MultiChannelTimeSeries{
    
    private MultiChannelTimeSeries masterTS;
    private long startBin;
    private long endBin;
    private MultiChannelTimeSeries.Header header;
    /** Creates a new instance of SubTimeSeries */
    public SubMultiChannelTimeSeries(MultiChannelTimeSeries masterTS, long startBin, long endBin) {
        this.masterTS = masterTS;
        this.startBin = startBin;
        this.endBin = endBin;
        this.header = new MultiChannelTimeSeries.Header();
        header.setBandwidth(masterTS.getHeader().getBandwidth());
        header.setFrequency(masterTS.getHeader().getFrequency());
        header.setSourceID(masterTS.getHeader().getSourceID());
        header.setTSamp(masterTS.getHeader().getTSamp());
        header.setNPoints(endBin - startBin);
        
        header.setMjdStart(masterTS.getHeader().getMjdStart() + Convert.secToMJD(startBin*header.getTSamp()));
        header.setTobs(header.getTSamp()*header.getNPoints());
        header.setNumChannel(masterTS.getHeader().getNumChannel());
        header.setInterleaved(masterTS.getHeader().isChannelInterleaved());
    }
    
    
    public MultiChannelTimeSeries.Header getHeader() {
        return header;
    }
    
    
    public String getDataType() {
       return null;
    }
    
    public void release() {
        this.masterTS = null;
        this.header = null;
        
    }
    
    public void flush() throws IOException {
        masterTS.flush();
    }
    
    public TimeSeries getOnechannel(int channelNumber) {
        return masterTS.getOnechannel(channelNumber).subData(startBin,endBin);
    }
    
    
    
    public float[][] getDataAsFloats() {
        return null;
    }
    
    
    public BulkReadable getBulkReadableInterface() {
        BulkReadable br = masterTS.getBulkReadableInterface();
        if(br==null)return null;
        else{
            return new SubBulkReadable(br,(int)startBin,(int)endBin,this.masterTS.getHeader().getNumChannel(),header);
        }
    }
    
    
    private class SubBulkReadable implements BulkReadable<MultiChannelTimeSeries.Header>{
        private BulkReadable master;
        private long start;
        private long end;
        private MultiChannelTimeSeries.Header header;
        private int nchans;
        
        SubBulkReadable(BulkReadable master,long start,long end,int nchans,MultiChannelTimeSeries.Header header){
            this.master = master;
            this.start = start;
            this.end = end;
            this.header = header;
            this.nchans = nchans;
        }
        
    public String getDataType() {
       return null;
    }
        public void read(long startPosn, float[] data) throws IOException {
            master.read(startPosn+start*nchans,data);
        }
        
        public void read(long startPosn, byte[] data) throws IOException {
            master.read(startPosn+start*nchans,data);
        }
        
        public void read(long startPosn, long[] data)  throws IOException{
            master.read(startPosn+start*nchans,data);
        }
        
        public void read(long startPosn, double[] data) throws IOException {
            master.read(startPosn+start*nchans,data);
        }
        
        public void read(long startPosn, short[] data)  throws IOException{
            master.read(startPosn+start*nchans,data);
        }
        
        public void read(long startPosn, int[] data) throws IOException {
            master.read(startPosn+start*nchans,data);
        }
        
        public void release() {
            this.master = null;
            this.header = null;
        }
        
        public MultiChannelTimeSeries.Header getHeader() {
            return header;
        }
        
        public DataRecordType getDataRecordType() {
            return master.getDataRecordType();
        }
        
        public void flush() throws IOException {
            
        }
        
        
    }
    
    
}
