/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pulsarhunter.datatypes;

import java.io.IOException;
import pulsarhunter.DataRecordType;

/**
 *
 * @author kei041
 */
public class PsrXMLData extends MultiChannelTimeSeries  {

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[][] getDataAsFloats() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Header getHeader() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TimeSeries getOnechannel(int channelNumber) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void release() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

 

}
