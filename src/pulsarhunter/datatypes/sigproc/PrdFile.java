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
 * PrdFile.java
 *
 * Created on 15 January 2007, 11:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.datatypes.sigproc;

import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import pulsarhunter.Data;
import pulsarhunter.PulsarHunter;
import coordlib.Telescope;
import pulsarhunter.datatypes.BasicSearchResult;
import pulsarhunter.datatypes.BasicSearchResultData;

/**
 *
 * @author mkeith
 */
public class PrdFile extends BasicSearchResultData {

    private enum HeaderElems {

        SOURCEID, FREF, TSTART, TELESCOPE, RAJ, DECJ, TSAMP, PROGRAM, VERSION, HARM_FOLDS, COLS
    };
    
    public enum ColumnLabel {

        SNR_SPEC, SNR_RECON, PERIOD
    };
    Header header = new Header();
    ArrayList<Double> dmList = new ArrayList<Double>();
    ArrayList<Double> acList = new ArrayList<Double>();
    ArrayList<Double> adList = new ArrayList<Double>();

    /** Creates a new instance of PrdFile */
    public PrdFile(File file) throws FileNotFoundException, IOException {
        
        this.header.setBarryCentered(false);
        this.read(new BufferedReader(new FileReader(file)));

    }

    public String getDataType() {
        return "PRDFILE";
    }

    private void read(BufferedReader reader) throws IOException {
        PulsarHunter.out.print("Reading .prd file... ");
        PulsarHunter.out.flush();
        String line = reader.readLine();

        double tsamp = header.getTsamp();

        double dm = 0;
        double ac = 0;
        double ad = 0;
        //Date t1 = new Date();

        line_loop:
        while (line != null) {
            if (line.trim().equalsIgnoreCase("##BEGIN HEADER##")) {
                header.readHeader(reader);
                line = reader.readLine();
            }

            if (line.trim().startsWith("DM:")) {
                // we have the start of a new dm/ac/ad block


                String[] elems = line.trim().split("\\s+");
                dm = Double.parseDouble(elems[1]);
                ac = Double.parseDouble(elems[3]);
                ad = Double.parseDouble(elems[5]); // convert cm/s/s/s to m/s/s/s (or don't)

                if (elems.length > 6) {
                    tsamp = Double.parseDouble(elems[7]);
                }

                //      Scanner scanner = new Scanner(line);
                //      scanner.next();
                //      dm = scanner.nextDouble();
                //      scanner.next();
                //      ac = scanner.nextDouble();
                //      scanner.next();
                //      ad = scanner.nextDouble();

                int posn = Collections.binarySearch(dmList, dm);
                if (posn < 0) {
                    dmList.add(-posn - 1, dm);
                }

                posn = Collections.binarySearch(acList, ac);
                if (posn < 0) {
                    acList.add(-posn - 1, ac);
                }

                posn = Collections.binarySearch(adList, ad);
                if (posn < 0) {
                    adList.add(-posn - 1, ad);
                }

            } else {
                // Read more snrs/periods

                String[] elems = line.trim().split("\\s+");


                // Scanner scanner = new Scanner(line);
                int harmCounter = 0;
                int ncols = this.getHeader().getColumns().length;

                for (int i = 0; i < elems.length; i += ncols) {
                    int harmfold = this.getHeader().getHarmFolds()[harmCounter];

                    double snr = 0.0;
                    double reconSnr = 0.0;
                    double period = 0.0;
                    //Double.parseDouble(elems[i+1])/1000.0;

                    for (int j = 0; j < ncols; j++) {
                        ColumnLabel col = this.getHeader().getColumns()[j];
                        switch (col) {
                            case SNR_SPEC:
                                try {
                                    snr = Double.parseDouble(elems[i + j]);

                                } catch (NumberFormatException numberFormatException) {
                                    snr = 0;
                                    System.err.println("Bad line in prd file: " + line);
                                    System.err.println(numberFormatException.getLocalizedMessage());
                                    line = reader.readLine();
                                    continue line_loop;
                                }
                                break;
                            case SNR_RECON:
                                try {
                                    reconSnr = Double.parseDouble(elems[i + j]);

                                } catch (NumberFormatException numberFormatException) {
                                    reconSnr = 0;
                                    System.err.println("Bad line in prd file: " + line);
                                    System.err.println(numberFormatException.getLocalizedMessage());
                                    line = reader.readLine();
                                    continue line_loop;
                                }

                                break;
                            case PERIOD:
                                try {
                                    period = Double.parseDouble(elems[i + j]) / 1000.0;

                                } catch (NumberFormatException numberFormatException) {
                                    period = 0;
                                    System.err.println("Bad line in prd file: " + line);
                                    System.err.println(numberFormatException.getLocalizedMessage());
                                    line = reader.readLine();
                                    continue line_loop;
                                }


                                break;
                        }

                    }

                    if (Math.abs(snr) > 0.001) {

                        BasicSearchResult searchResult = new BasicSearchResult(period, dm);
                        searchResult.setSpectralSignalToNoise(snr);
                        searchResult.setReconstructedSignalToNoise(reconSnr);
                        searchResult.setAccn(ac);
                        searchResult.setJerk(ad);

                        searchResult.setTsamp(tsamp);

                        searchResult.setHarmfold(harmfold);
                        this.addSearchResult(searchResult);
                    }
                    harmCounter++;


                }

                //  while(scanner.hasNext()){
                //double snr = scanner.nextDouble();
                //double period = scanner.nextDouble()/1000.0;
                //  double pdot = Convert.accToPdot(period,ac)/2.0;
                //  double pddot = Convert.jerkToPddot(period,ad,pdot);

                /*    BasicSearchResult searchResult = new BasicSearchResult(period,dm);
                searchResult.setSpectralSignalToNoise(snr);
                searchResult.setPdot(pdot);
                searchResult.setPddot(pddot);
                searchResult.setHarmfold(harmfold);
                this.addSearchResult(searchResult);
                harmfold++;*/
                // }


            }
            line = reader.readLine();
        }
        // Date t2 = new Date();
        // System.out.println(t2.getTime()-t1.getTime());
        PulsarHunter.out.println("Done.");
    }

    public void release() {
        super.release();
    }

    public Header getHeader() {
        return header;
    }

    public void flush() throws IOException {
    }

    public double[] getDmIndex() {
        double[] arr = new double[dmList.size()];
        int off = 0;
        for (double d : dmList) {
            arr[off++] = d;
        }
        return arr;

    }

    public double[] getAdIndex() {
        double[] arr = new double[adList.size()];
        int off = 0;
        for (double d : adList) {
            arr[off++] = d;
        }
        return arr;
    }

    public double[] getAcIndex() {
        double[] arr = new double[acList.size()];
        int off = 0;
        for (double d : acList) {
            arr[off++] = d;
        }
        return arr;
    }

    public class Header extends Data.Header {

        private double tsamp = -1;
        private int[] harmFolds = new int[]{1, 2, 4, 8, 16};
        private ColumnLabel[] columns = new ColumnLabel[]{PrdFile.ColumnLabel.SNR_SPEC, PrdFile.ColumnLabel.PERIOD};

        void readHeader(BufferedReader reader) throws IOException {
            String line = reader.readLine();
            RA ra = new RA(0);
            Dec dec = new Dec(0);
            while (!line.equals("##END HEADER##")) {
                String[] elems = line.trim().split("\\s+", 3);
                String value = elems[2];
                HeaderElems key = HeaderElems.valueOf(elems[0]);

                // System.out.println(key);
                if (key != null) {
                    switch (key) {
                        case SOURCEID:
                            this.setSourceID(value);
                            break;
                        case FREF:
                            this.setFrequency(Double.parseDouble(value.split("\\s+")[0]));
                            break;
                        case TSTART:
                            this.setMjdStart(Double.parseDouble(value));
                            break;
                        case TELESCOPE:
                            if (value.equalsIgnoreCase("FAKE")) {
                                value = "UNKNOWN";
                            }
                            try {
                                this.setTelescope(Telescope.valueOf(value.toUpperCase()));
                            } catch (EnumConstantNotPresentException e) {
                                this.setTelescope(Telescope.UNKNOWN);
                            }
                            break;
                        case RAJ:
                            ra = new RA(value);
                            break;
                        case DECJ:
                            dec = new Dec(value);
                            break;
                        case TSAMP:
                            String[] elems2 = value.split("\\s+");
                            double factor = 1e-6;
                            if (elems2.length > 1) {
                                if (elems2[1].equals("us")) {
                                    factor = 1e-6;
                                }
                                if (elems2[1].equals("ms")) {
                                    factor = 1e-3;
                                }
                                if (elems2[1].equals("s")) {
                                    factor = 1.0;
                                }
                            }
                            this.setTsamp(Double.parseDouble(elems2[0]) * factor);
                            break;
                        case HARM_FOLDS:
                            String[] harmStr = value.split("\\s+");
                            harmFolds = new int[harmStr.length];
                            for (int i = 0; i < harmFolds.length; i++) {
                                harmFolds[i] = Integer.parseInt(harmStr[i]);
                            }
                            break;
                        case COLS:
                            String[] colStr = value.split("\\s+");
                            this.columns = new ColumnLabel[colStr.length];
                            for (int i = 0; i < colStr.length; i++) {
                                columns[i] = ColumnLabel.valueOf(colStr[i]);
                            }

                            break;
                        default:
                            break;





                    }
                }
                line = reader.readLine();
            }
            this.setCoord(new Coordinate(ra, dec));
        }

        public double getTsamp() {
            return tsamp;
        }

        public void setTsamp(double tsamp) {
            this.tsamp = tsamp;
        }

        public int[] getHarmFolds() {
            return harmFolds;
        }

        public void setHarmFolds(int[] harmFolds) {
            this.harmFolds = harmFolds;
        }

        public ColumnLabel[] getColumns() {
            return columns;
        }

        public void setColumns(ColumnLabel[] columns) {
            this.columns = columns;
        }
    }
}
