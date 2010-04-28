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
package pulsarhunter.datatypes.presto;

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
import java.util.regex.Pattern;
import pulsarhunter.datatypes.BasicSearchResult;
import pulsarhunter.datatypes.BasicSearchResultData;

/**
 *
 * @author mkeith
 */
public class AccelSearchOutput extends BasicSearchResultData {

    Header header;
    ArrayList<Double> dmList = new ArrayList<Double>();
    ArrayList<Double> acList = new ArrayList<Double>();
    ArrayList<Double> adList = new ArrayList<Double>();

    /** Creates a new instance of AccelSearchOutput */
    public AccelSearchOutput(File file) throws FileNotFoundException, IOException {
        this.read(new BufferedReader(new FileReader(file)));
    }

    public String getDataType() {
        return "PRESTOACCELRES";
    }
    private static Pattern brak = Pattern.compile("\\(.*\\)");
    private static Pattern exp = Pattern.compile("x10\\^");

    private String fixString(String in) {
        String out = exp.matcher(brak.matcher(in).replaceAll("")).replaceAll("e");
//        System.out.println(out+"\t"+in);
        return out;

    }

    private void read(BufferedReader reader) throws IOException {
        PulsarHunter.out.print("Reading PRESTO accel list file... ");
        PulsarHunter.out.flush();


        acList.add(0, Double.valueOf(0));
        adList.add(0, Double.valueOf(0));
        String infile = reader.readLine();
        while (infile != null) {

            BufferedReader reader2 = new BufferedReader(new FileReader(infile));

            // read the header, which is at the end!

            String line = reader2.readLine();


            // first read the human header off the top...
            while (line != null && !line.startsWith("-------")) {
                line = reader2.readLine();
            }
            line = reader2.readLine();

            double dm = 0;
            double tsamp = 0;
            ArrayList<BasicSearchResult> tmplist = new ArrayList<BasicSearchResult>();
            while (line != null) {


                String[] elems = line.trim().split("\\s+");
                if (elems.length < 11) {
                    // we have finished this bit of the file
                    break;
                }


                int harmfold = Integer.parseInt(elems[4]);

                double snr = Double.parseDouble(elems[1]);
                double reconSnr = Double.parseDouble(elems[3]);
                double ac = -Double.parseDouble(fixString(elems[10]));
                double ad = 0;
                double period = Double.parseDouble(fixString(elems[5])) / 1000.0;



                if (Math.abs(snr) > 0.001) {

                    BasicSearchResult searchResult = new BasicSearchResult(period, dm);
                    searchResult.setSpectralSignalToNoise(snr);
                    searchResult.setReconstructedSignalToNoise(reconSnr);
                    searchResult.setAccn(ac);
                    searchResult.setJerk(ad);

                    searchResult.setTsamp(tsamp);

                    searchResult.setHarmfold(harmfold);
                    tmplist.add(searchResult);


                }
                line = reader2.readLine();
            }

            // now skip to the 'inf' content at the bottom...
            while (line != null && !line.startsWith("-------")) {
                line = reader2.readLine();
            }
            line = reader2.readLine();
            while (line != null && line.length() > 10) {
                line = reader2.readLine();
            }
            line = reader2.readLine();
            // now at start of header
            try {
                header = new AccelSearchOutput.Header(reader2);

                dm = header.dm;
                int posn = Collections.binarySearch(dmList, dm);
                if (posn < 0) {
                    dmList.add(-posn - 1, dm);
                }
                for (BasicSearchResult searchResult : tmplist) {
                    searchResult.setDM(header.dm);
                    searchResult.setTsamp(header.getTSamp());
                    this.addSearchResult(searchResult);
                }
            } catch (Exception e) {
                System.err.println("BAD file:"+ infile);
                e.printStackTrace();
            }
            // go to the next file
            infile = reader.readLine();

        }

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

        String[] bands = new String[]{"Radio", "IR", "Optical", "UV", "X-ray", "Gamma"};
        String[] scopes = new String[]{"None (Artificial Data Set)", "Arecibo", "Parkes", "VLA", "MMT", "Las Campanas 2.5m", "Mt. Hopkins 48in", "Other"};        //PrestoParams
        double ra_s;                /* Right ascension seconds (J2000)       */

        double dec_s;               /* Declination seconds (J2000)           */

        double N;                   /* Number of bins in the time series     */

        double dt;                  /* Width of each time series bin (sec)   */

        double fov;                 /* Diameter of Beam or FOV in arcsec     */

        double mjd_f;               /* Epoch of observation (MJD) frac part  */

        double dm;                  /* Radio -- Dispersion Measure (cm-3 pc) */

        double freq;                /* Radio -- Low chan central freq (Mhz)  */

        double freqband;            /* Radio -- Total Bandwidth (Mhz)        */

        double chan_wid;            /* Radio -- Channel Bandwidth (Mhz)      */

        double wavelen;             /* IR,Opt,UV -- central wavelength (nm)  */

        double waveband;            /* IR,Opt,UV -- bandpass (nm)            */

        double energy;              /* x-ray,gamma -- central energy (kev)   */

        double energyband;          /* x-ray,gamma -- energy bandpass (kev)  */

        double[] onoff = new double[40]; /* Bin number pairs where obs is "on"    */

        int num_chan;               /* Radio -- Number Channels              */

        int mjd_i;                  /* Epoch of observation (MJD) int part   */

        int ra_h;                   /* Right ascension hours (J2000)         */

        int ra_m;                   /* Right ascension minutes (J2000)       */

        int dec_d;                  /* Declination degrees (J2000)           */

        int dec_m;                  /* Declination minutes (J2000)           */

        int bary;                   /* Barycentered?  1=yes, 0=no            */

        int numonoff;               /* The number of onoff pairs in the data */

        String notes;            /* Any additional notes                  */

        String name;             /* Data file name without suffix         */

        String object;           /* Object being observed                 */

        String instrument;       /* Instrument used                       */

        String observer;         /* Observer[s] for the data set          */

        String analyzer;         /* Who analyzed the data                 */

        String telescope;         /* Telescope used                        */

        String band;              /* Type of observation (EM band)         */

        String filt;               /* IR,Opt,UV -- Photometric Filter       */

        boolean negdec;

        private String stripline(String line) {
            String[] elems = line.split("=\\s");
            if (elems.length > 1) {
                return elems[1].trim();
            } else {
                return "";
            }
        }

        private Header(BufferedReader reader) {
            super();
            try {

                String line = reader.readLine();
                this.name = stripline(line);

                line = reader.readLine();
                this.telescope = stripline(line);

                if (!name.equals(scopes[0])) {

                    line = reader.readLine();
                    this.instrument = stripline(line);

                    line = reader.readLine();
                    this.object = stripline(line);

                    line = reader.readLine();
                    String raString = stripline(line);

                    String[] elems = raString.split("\\:");
                    this.ra_h = Integer.parseInt(elems[0].trim());
                    this.ra_m = Integer.parseInt(elems[1].trim());
                    this.ra_s = Double.parseDouble(elems[2].trim());

                    line = reader.readLine();
                    String deString = stripline(line);
                    elems = deString.split("\\:");
                    negdec = elems[0].trim().charAt(0) == '-';

                    this.dec_d = Integer.parseInt(elems[0].trim());
                    this.dec_m = Integer.parseInt(elems[1].trim());
                    this.dec_s = Double.parseDouble(elems[2].trim());


                    line = reader.readLine();
                    this.observer = stripline(line);

                    line = reader.readLine();
                    String mjdString = stripline(line);
                    elems = mjdString.split("\\.");
                    this.mjd_i = Integer.parseInt(elems[0]);
                    this.mjd_f = Double.parseDouble("0." + elems[1]);

                    line = reader.readLine();
                    this.bary = Integer.parseInt(stripline(line));

                } else {
                    this.object = "fake pulsar";
                }


                line = reader.readLine();
                this.N = Double.parseDouble(line.split("=\\s")[1]);

                line = reader.readLine();
                this.dt = Double.parseDouble(line.split("=\\s")[1]);


                line = reader.readLine();
                this.numonoff = Integer.parseInt(stripline(line));

                if (this.numonoff > 0) {
                    int ii = 0;
                    do {
                        line = reader.readLine();
                        String datLine = line.split("=\\s")[1];
                        String[] elems = datLine.split(",");
                        this.onoff[ii] = Double.parseDouble(elems[0]);
                        this.onoff[ii + 1] = Double.parseDouble(elems[1]);
                        ii += 2;

                    } while ((this.onoff[ii - 1] < (this.N - 1)) && (ii < onoff.length));
                    this.numonoff = ii / 2;
                    if (this.numonoff > this.onoff.length) {
                        throw new IOException("There are two many OnOff values. Max: " + onoff.length);
                    }
                }

                if (!name.equals(scopes[0])) {
                    line = reader.readLine();
                    this.band = stripline(line);

                    if (this.band.equals(this.bands[0])) {
                        line = reader.readLine();
                        this.fov = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.dm = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.freq = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.freqband = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.num_chan = Integer.parseInt(stripline(line));
                        line = reader.readLine();
                        this.chan_wid = Double.parseDouble(line.split("=\\s")[1]);
                    } else if (this.band.equals(this.bands[4]) || this.band.equals(this.bands[5])) {
                        line = reader.readLine();
                        this.fov = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.energy = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.energyband = Double.parseDouble(line.split("=\\s")[1]);
                    } else {
                        line = reader.readLine();
                        this.filt = stripline(line);
                        line = reader.readLine();
                        this.fov = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.wavelen = Double.parseDouble(line.split("=\\s")[1]);
                        line = reader.readLine();
                        this.waveband = Double.parseDouble(line.split("=\\s")[1]);
                    }

                }

                line = reader.readLine();
                this.analyzer = stripline(line);
                line = reader.readLine();
                line = reader.readLine();
                this.notes = line.trim();


                reader.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void setSourceID(String sourceID) {
            this.object = sourceID;
        }

        public void setNPoints(long nPoints) {
            this.N = nPoints;
        }

        public void setFrequency(double frequency) {
            this.freq = frequency;
        }

        public void setTobs(double obstime) {
        }

        public void setBandwidth(double bandwidth) {
            this.freqband = bandwidth;
        }

        public void setTSamp(double tSamp) {
            this.dt = tSamp;
        }

        public void setMjdStart(double mjdStart) {
            this.mjd_i = (int) mjdStart;
            this.mjd_f = mjdStart - this.mjd_i;
        }

        public double getTSamp() {
            return this.dt;

        }

        public double getTobs() {
            return this.dt * this.N;

        }

        public double getFrequency() {
            return this.freq;
        }

        public long getNPoints() {
            return (long) this.N;

        }

        public double getBandwidth() {
            return this.freqband;
        }

        public double getMjdStart() {
            return this.mjd_i + this.mjd_f;
        }

        public String getSourceID() {
            return this.object;
        }

        @Override
        public Coordinate getCoord() {
            return new Coordinate(new RA(ra_h, ra_m, ra_s), new Dec(dec_d, ra_m, ra_s, negdec));
        }

        @Override
        public Telescope getTelescope() {
            Telescope tel = null;
            try {
                tel = Telescope.valueOf(telescope.toUpperCase());
            } catch (Exception e) {
                tel = Telescope.UNKNOWN;
            }

            return tel;
        }
    }
}
