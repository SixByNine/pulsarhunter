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
 * SigprocDataFactory.java
 *
 * Created on 25 October 2006, 14:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.datatypes.sigproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import pulsarhunter.Data;
import pulsarhunter.DataFactory;
import pulsarhunter.IncorrectDataTypeException;

/**
 *
 * @author mkeith
 */
public class SigprocDataFactory implements DataFactory {

    public enum SigprocDataType {

        SigprocTimeSeries, SigprocBandedTimeSeries, BestSumFile, PrdFile
    };
    /** Creates a new instance of SigprocDataFactory */
    private SigprocDataType type;

    public SigprocDataFactory(SigprocDataType type) {
        this.type = type;
    }

    public Data loadData(String filename, int bufSize) throws IncorrectDataTypeException {
        Data data = null;

        File file = new File(filename);

        if (file.exists()) {
            if (type == SigprocDataType.BestSumFile) {
                return loadSumFile(file);
            }
            if (type == SigprocDataType.PrdFile) {
                return loadPrdFile(file);
            }
            if (file.getName().endsWith(".prd")) {
                throw new IncorrectDataTypeException("SigprocDataFactory: Looks like a .prd file");
            }

            SigprocHeader head = null;
            try {
                head = new SigprocHeader(file);
            } catch (IOException e) {

                throw new IncorrectDataTypeException("SigprocDataFactory: Cannot parse header of specified file");
            } catch (Exception e) {

                throw new IncorrectDataTypeException("SigprocDataFactory: Cannot parse header of specified file");
            }

            try {
                if (head.getNbits() != 32) {
                    if (head.getNbits() == 8) {
                        // Use the new 8-bit reader.
                        System.err.println("\n\nWarning: Using prototype 8-bit sigproc file reader. Bugs may exist!");
                        if (head.getNchans() > 1) {
                            if (type == SigprocDataType.SigprocTimeSeries) {
                                throw new IncorrectDataTypeException("SigprocDataFactory: This should be read as type SIGPROC-8BIT-TIMESERIES.");
                            }
                            data = new Sigproc8bitBandedTimeSeries(file, bufSize);
                        } else {
                            if (type == SigprocDataType.SigprocBandedTimeSeries) {
                                throw new IncorrectDataTypeException("SigprocDataFactory: This should be read as type SIGPROC-8BIT-BANDEDTIMESERIES.");
                            }
                            data = new Sigproc8bitTimeSeries(file, bufSize);

                        }
                    } else {
                        throw new IncorrectDataTypeException("SigprocDataFactory: Currently PulsarHunter can only read 32 bit float sigproc data.");
                    }

                } else {
                    // head.setNchans(1);
                    //System.out.println(head.getNchans());
                    if (head.getNchans() > 1) {
                        if (type == SigprocDataType.SigprocTimeSeries) {
                            throw new IncorrectDataTypeException("SigprocDataFactory: This should be read as type SIGPROCTIMESERIES.");
                        }
                        data = new SigprocBandedTimeSeries(file, bufSize);
                    } else {
                        if (type == SigprocDataType.SigprocBandedTimeSeries) {
                            throw new IncorrectDataTypeException("SigprocDataFactory: This should be read as type SIGPROCBANDEDTIMESERIES.");
                        }
                        data = new Sigproc32bitTimeSeries(file, bufSize);

                    }
                }
            } catch (IOException ex) {
                throw new IncorrectDataTypeException("SigprocDataFactory: Cannot read body of specified file", ex);
            }

        } else {
            throw new IncorrectDataTypeException("SigprocDataFactory: File " + filename + "does not exist!");
        }

        return data;

    }

    public String getName() {
        return type.toString().toUpperCase();
    }

    public Data createData(String filename) throws IncorrectDataTypeException {
        Data data = null;
        File file = new File(filename);
        file.delete();
        switch (this.type) {
            case SigprocTimeSeries:

                try {
                    data = new Sigproc32bitTimeSeries(file, 1024);
                } catch (IOException ex) {
                    throw new IncorrectDataTypeException("IO Error trying to create file " + this.getName(), ex);
                }
                return data;
            case SigprocBandedTimeSeries:
                try {
                    data = new SigprocBandedTimeSeries(file, 1024, true);
                } catch (IOException ex) {
                    throw new IncorrectDataTypeException("IO Error trying to create file " + this.getName(), ex);
                }
                return data;
            default:
                throw new IncorrectDataTypeException("createData not supported on " + this.getName());
        }


    }

    private Data loadSumFile(File f) throws IncorrectDataTypeException {
        if (!f.getName().endsWith(".sum")) {
            throw new IncorrectDataTypeException("Error trying to read SumFile " + f.getName() + ". It MUST end with .sum");
        }
        BestSumFile data = new BestSumFile(f);
        try {
            data.read();
        } catch (IOException ex) {
            throw new IncorrectDataTypeException("Error trying to read BestSumFile " + f.getName(), ex);
        }
        return data;

    }

    private Data loadPrdFile(File f) throws IncorrectDataTypeException {
        if (!f.getName().endsWith(".prd")) {
            throw new IncorrectDataTypeException("Error trying to read PrdFile " + f.getName() + ". It MUST end with .prd");
        }
        PrdFile data = null;
        try {
            data = new PrdFile(f);
        } catch (FileNotFoundException ex) {
            throw new IncorrectDataTypeException("Error trying to read PrdFile (File does not exist) " + f.getName(), ex);
        } catch (IOException ex) {
            throw new IncorrectDataTypeException("Error trying to read PrdFile " + f.getName(), ex);
        }
        return data;
    }
}
