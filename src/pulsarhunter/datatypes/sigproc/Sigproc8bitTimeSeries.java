/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pulsarhunter.datatypes.sigproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import pulsarhunter.DataRecordType;
import pulsarhunter.PulsarHunter;
import pulsarhunter.datatypes.TimeSeries;
import pulsarhunter.recipies.RecipeParser;

/**
 *
 * @author kei041
 */
public class Sigproc8bitTimeSeries extends SigprocTimeSeries {

    private int bufferSize;
    private ByteBuffer bb;
    private RandomAccessFile raStream;
    private long currentFilePos = Long.MAX_VALUE;

    public Sigproc8bitTimeSeries(File timFile, int bufferSize) throws IOException {
        super(timFile);
        this.timFile = timFile;

        if (timFile.length() > 0) {
            long fileSize = timFile.length() - getSigprocHeader().getHeaderLength();
            // System.out.println("hl:"+header.getHeaderLength());
            if (bufferSize > fileSize) {
                bufferSize = (int) fileSize;
            }
        }

        this.bufferSize = (int) (RecipeParser.getMem(bufferSize));

    }


    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }


    public TimeSeries.Header getHeader() {
        return this.header;
    }

    public float[] getDataAsFloats() {
        return null;
    }

    public void release() {
        try {
            if (this.raStream != null) {
                this.raStream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        this.header = null;
        this.bb = null;

        this.raStream = null;
        this.timFile = null;
    }

    public void flush() throws IOException {
    }

    private void checkBuffers(final long bin, int nbins, final boolean reading) throws FileNotFoundException, IOException {
        int bufs = bufferSize;

        if (nbins > bufs) {
            throw new IOException("Trying to read/write more bins than the buffer size!");
        }

        if (bb == null || raStream == null) {

            raStream = new RandomAccessFile(this.timFile, "rw");

            bufs = (int) (RecipeParser.getMem(bufs));
            bb = null;
            try {
                bb = raStream.getChannel().map(MapMode.READ_WRITE, ((long) getSigprocHeader().getHeaderLength() + bin), bufs);
            } catch (IOException ex) {

                bufferSize /= 2.0;

                ex.printStackTrace();
                PulsarHunter.out.println("Reduced Buffer Size... " + bufferSize);
                checkBuffers(bin, nbins, reading);
                return;

            }
            bb.order(ByteOrder.nativeOrder());

            currentFilePos = bin;
        //System.out.println(fb.order().toString() + " Native "+ByteOrder.nativeOrder().toString());
        //  System.out.println("N");
        }
        if (bin + nbins >= currentFilePos + bb.capacity()) {

            int addj = 0;

            if (reading && getSigprocHeader().getHeaderLength() + bin + this.bufferSize > this.timFile.length()) {
                addj = (int) ((long) (getSigprocHeader().getHeaderLength() + bin + this.bufferSize) - this.timFile.length());
            }

            // System.out.println(this.timFile.getName()+">> ("+addj+")");
            //           int bufs = (int)(RecipeParser.getMem(bufferSize*4)/4);
            bufs = (int) (RecipeParser.getMem(bufs));
            bb = null;
            try {

                bb = raStream.getChannel().map(MapMode.READ_WRITE, getSigprocHeader().getHeaderLength() + bin - addj, bufs);
            } catch (IOException ex) {
                bufferSize /= 2.0;
                ex.printStackTrace();
                PulsarHunter.out.println("Reduced Buffer Size... " + bufferSize);
                checkBuffers(bin, nbins, reading);
                return;

            }

            bb.order(ByteOrder.nativeOrder());
            bb.position(addj);
            currentFilePos = bin;

        }
        if (nbins > 1 && bin == currentFilePos + bb.position()) {
            return;
        }

        if (bin > currentFilePos + bb.position()) {
            //  System.out.println(">");
            while (bin != currentFilePos + bb.position()) {
                bb.get();

            }

        }

        if (bin < bb.position()) {


            //System.out.println("<<");
            raStream.close();
            raStream = new RandomAccessFile(this.timFile, "rw");

            // System.out.println(((long)header.getHeaderLength()+bin*4L)+"\t"+this.bufferSize*4+"\t"+timFile.length());
            int addj = 0;
            if (getSigprocHeader().getHeaderLength() + bin + this.bufferSize > this.timFile.length()) {
//                int bufs = (int)(RecipeParser.getMem(bufferSize*4)/4);
                addj = (int) ((long) (getSigprocHeader().getHeaderLength() + bin + bufs) - this.timFile.length());
            }
            ByteBuffer bb = null;
            try {
                bb = raStream.getChannel().map(MapMode.READ_WRITE, getSigprocHeader().getHeaderLength() + bin - addj, this.bufferSize);
            } catch (IOException ex) {
                bufferSize /= 2.0;
                PulsarHunter.out.println("Reduced Buffer Size... " + bufferSize);
                checkBuffers(bin, nbins, reading);
                return;
            }

            bb.order(ByteOrder.nativeOrder());

            currentFilePos = bin;

        }
    }

    public float getBin(long bin) {
        float f = -1.0f;

        try {


            this.checkBuffers(bin, 0, true);

            //System.out.println("ts: " + bin);
            if (bin == currentFilePos + bb.position()) {
                try {
                    f = (float) bb.get();

                } catch (java.nio.BufferUnderflowException e) {
                    System.out.println("Ran over end of file");
                    System.out.println("bin:" + bin + "\ncfp:" + currentFilePos + "\npos:" + bb.position() + "\ncap:" + bb.capacity() + "\nbfs:" + this.bufferSize);
                    System.exit(-2);
                }
            } else {
                // System.out.println("<.");
                try {
                    f = (float) bb.get((int) (bin - bb.position()));


                } catch (java.nio.BufferUnderflowException e) {
                    System.out.println("Ran over end of file");
                    System.out.println("bin:" + bin + "\ncfp:" + currentFilePos + "\npos:" + bb.position() + "\ncap:" + bb.capacity() + "\nbfs:" + this.bufferSize);
                    System.exit(-2);
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println(this.timFile.getName());
            System.out.println("Bin:" + bin);
            ex.printStackTrace();
            System.exit(-1);
        }
        return f;
    }

    public DataRecordType getDataRecordType() {
        return DataRecordType.FLOAT_FLOAT;
    }

    public String getDataType() {
        return "SIGPROCTIMESERIES";
    }
}
