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
 * OptimisedSuspectResult.java
 *
 * Created on 01 November 2006, 15:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.datatypes;

import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import pulsarhunter.Data;
import coordlib.Telescope;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import pulsarhunter.Convert;
import pulsarhunter.PulsarHunter;
import pulsarhunter.displaypanels.PHCFImagePlot;
import pulsarhunter.displaypanels.PHCFPlot;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.CandidateFile;
import pulsarhunter.jreaper.gui.CandidateDisplayFrame;
import pulsarhunter.jreaper.gui.MainView;
import pulsarhunter.jreaper.peckscorer.PeckScoreableCandFile;

/**
 *
 *
 * @author mkeith
 */
public class PulsarHunterCandidate implements Data, CandidateFile, PeckScoreableCandFile {

    private Header header;
    private File file;
    private Hashtable<String, PHCSection> sections = new Hashtable<String, PHCSection>();
    private ArrayList<String> keys = new ArrayList<String>();
    private PHCSection initialSec = null;
    private PHCSection optimisedSec = null;
    private String altPath = null;
    private boolean readOnDemand = false;

    private enum PHCXMLTypes {

        phcf, head, Telescope, SourceID, Coordinate, RA, Dec, Epoch, CentreFreq,
        BandWidth, MjdStart, ObservationLength,
        body, Section, BestValues, TopoPeriod, BaryPeriod, Dm, Accn, Jerk, Snr, Width,
        SampleRate, SubIntegrations, SubBands, Profile,
        SnrBlock, PeriodIndex, DmIndex, AccnIndex, JerkIndex, DataBlock, Extra, SecExtra,
        Other
    };

    public String getDataType() {
        return "PHCF";
    }

    /** Creates a new instance of PulsarHunterCandidate */
    public PulsarHunterCandidate(File file) {
        this.header = new Header(this);
        this.file = file;
    }

    public PulsarHunterCandidate(String string) {
        this.header = new Header(this);
        this.file = new File(string);
        this.setReadOnDemand(true);
    }

    public void release() {
        this.readOnDemand = true;
        this.header = new Header(this);
        this.initialSec = null;
        this.optimisedSec = null;
        this.sections = new Hashtable<String, PHCSection>();
        this.keys = new ArrayList<String>();
    }

    public Header getHeader() {
        readIfRequired();
        return this.header;
    }

    public void addSection(PHCSection sec) {
        readIfRequired();
//        keys.remove(sec.getName());
//        this.sections.remove(sec.getName());
        this.removeSection(sec.getName());
        this.sections.put(sec.getName(), sec);
        keys.add(sec.getName());
        if (this.getInitialSec() == null) {
            this.initialSec = sec;
        }
        this.optimisedSec = sec;
    }

    public void removeSection(String secKey) {
        readIfRequired();
        this.sections.remove(secKey);
        keys.remove(secKey);
    }

    public List<String> listSections() {
        readIfRequired();
        //return new ArrayList(sections.keySet());
        return keys;
    }

    public PHCSection getSection(String key) {
        readIfRequired();
        return this.sections.get(key);
    }

    public void read() throws IOException {
        InputStream in = new FileInputStream(file);
        if (file.getName().endsWith(".gz")) {
            in = new java.util.zip.GZIPInputStream(in);
        }

        if (file.getName().endsWith("phcf") || file.getName().endsWith("phcf.gz")) {
            this.readOldPHCF(new BufferedInputStream(in));
        } else {
            this.readXML(new BufferedInputStream(in));
        }

    }

    public void precache() {
        try {
            read();
        } catch (IOException ex) {
        }
    }

    public void readXML(BufferedInputStream inStream) throws IOException {
        try {

            XMLReader XMLReaderparser = XMLReaderFactory.createXMLReader();
            final PulsarHunterCandidate finalThis = this;



            XMLReaderparser.setContentHandler(new DefaultHandler() {

                private RA ra = null;
                private Dec dec = null;
                private String units = "";
                private String format = "";
                private String extraKey = "";
                private PHCSection sec = null;
                private int nSub;
                private int nVals;
                private int nBins;
                private double max;
                private double min;
                private double[] periodIdx;
                private double[] dmIdx;
                private double[] accnIdx;
                private double[] jerkIdx;
                private double[][][][] block;
                private StringBuffer content = new StringBuffer();

                public void characters(char[] ch, int start, int length) throws SAXException {
                    this.content.append(ch, start, length);
                }

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    PHCXMLTypes type = null;
                    try {
                        type = PHCXMLTypes.valueOf(localName);
                    } catch (IllegalArgumentException e) {
                        type = PHCXMLTypes.Other;
                    }



                    max = min = nSub = nVals = nBins = 0;

                    String name = "Unnamed";
                    for (int i = 0; i < attributes.getLength(); i++) {

                        String key = attributes.getLocalName(i);
                            if (key.equals("units")) {
                                units = attributes.getValue(i);
                            } else if (key.equals("nVals")) {
                                nVals = Integer.parseInt(attributes.getValue(i).trim());
                            } else if (key.equals("nBins")) {
                                nBins = Integer.parseInt(attributes.getValue(i).trim());
                            } else if (key.equals("nSub")) {
                                nSub = Integer.parseInt(attributes.getValue(i).trim());
                            } else if (key.equals("format")) {
                                format = attributes.getValue(i);
                            } else if (key.equals("name")) {
                                name = attributes.getValue(i);
                            } else if (key.equals("min")) {
                                min = Double.parseDouble(attributes.getValue(i));
                            } else if (key.equals("max")) {
                                max = Double.parseDouble(attributes.getValue(i));
                            } else if (key.equals("key")) {
                                extraKey = attributes.getValue(i);
                            }
                    }



                    switch (type) {
                        case Section:
                            sec = new PHCSection(name);
                            break;


                        case Other:
                        default:
                            break;
                    }

                    content = new StringBuffer();
                }
                //                   phcf,head,Telescope,SourceID,Coordinate,RA,Dec,Epoch,CentreFreq,
                //                   Bandwidth,MjdStart,ObservationLength,
                //                   body,Section,BestValues,TopoPeriod,BaryPeriod,Dm,Accn,Jerk,Snr,Width,
                //                   SampleRate,SubIntegrations,SubBands,Profile,
                //                   SnrBlock,PeriodIndex,DmIndex,AccnIndex,JerkIndex,DataBlock,

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    PHCXMLTypes type = null;
                    try {
                        type = PHCXMLTypes.valueOf(localName);
                    } catch (IllegalArgumentException e) {
                        type = PHCXMLTypes.Other;
                    }

                    double fact = 1.0;
                    double nmax;
                    int i, j, k, l;
                    int digitsPerSamp = 2;
                    try {
                        switch (type) {
                            case SecExtra:
                            case Extra:

                                if (sec == null) {
                                    header.setExtraValue(extraKey, content.toString());
                                } else {
                                    sec.setExtraValue(extraKey, content.toString());
                                }

                                break;
                            case Telescope:
                                Telescope tel;
                                try {
                                    tel = Telescope.valueOf(content.toString());
                                } catch (IllegalArgumentException e) {
                                    tel = Telescope.UNKNOWN;
                                }
                                header.setTelescope(tel);
                                break;
                            case SourceID:
                                header.setSourceID(content.toString());
                                break;
                            case Coordinate:
                                header.setCoord(new Coordinate(ra, dec));
                                break;
                            case RA:
                                this.ra = new RA(Double.parseDouble(content.toString()));
                                break;
                            case Dec:
                                this.dec = new Dec(Double.parseDouble(content.toString()));
                                break;
                            case Epoch:
                                if (!content.toString().trim().equals("J2000")) {
                                    System.err.println("WARNING: Overwriding epoch from " + content.toString().trim() + " to J2000");
                                    System.err.println("WARNING: No suport for epoch " + content.toString().trim());
                                }
                                break;
                            case CentreFreq:
                                header.setFrequency(Double.parseDouble(content.toString()));
                                break;
                            case BandWidth:
                                header.setBandwidth(Double.parseDouble(content.toString()));
                                break;
                            case MjdStart:
                                header.setMjdStart(Double.parseDouble(content.toString()));
                                break;
                            case ObservationLength:

                                if (units.equalsIgnoreCase("seconds")) {
                                    fact = 1.0;
                                } else if (units.equalsIgnoreCase("minutes")) {
                                    fact = 60.0;
                                } else if (units.equalsIgnoreCase("hours")) {
                                    fact = 3600.0;
                                } else if (units.equalsIgnoreCase("days")) {
                                    fact = 3600.0 * 24;
                                } else if (units.equalsIgnoreCase("years")) {
                                    fact = 3600.0 * 24.0 * 365.0;
                                } else {
                                    System.err.println("WARNING: Unknown temporal units: " + units + " Assumings seconds");
                                }
                                header.setTobs(Double.parseDouble(content.toString()) * fact);
                                break;



                            // Section stuff
                            case Section:
                                addSection(sec);
                                optimisedSec = sec;
                                break;


                            // Section header
                            case TopoPeriod:
                                fact = 1.0;
                                if (units.equalsIgnoreCase("seconds")) {
                                    fact = 1.0;
                                } else if (units.equalsIgnoreCase("milliseconds")) {
                                    fact = 0.001;
                                } else {
                                    System.err.println("WARNING: Unknown temporal units: " + units + " Assumings seconds");
                                }
                                sec.setBestTopoPeriod(Double.parseDouble(content.toString()) * fact);
                                break;

                            case BaryPeriod:
                                fact = 1.0;
                                if (units.equalsIgnoreCase("seconds")) {
                                    fact = 1.0;
                                } else if (units.equalsIgnoreCase("milliseconds")) {
                                    fact = 0.001;
                                } else {
                                    System.err.println("WARNING: Unknown temporal units: " + units + " Assumings seconds");
                                }
                                sec.setBestBaryPeriod(Double.parseDouble(content.toString()) * fact);
                                break;
                            case Dm:
                                sec.setBestDm(Double.parseDouble(content.toString()));
                                break;

                            case Accn:
                                fact = 1.0;
                                if (units.equalsIgnoreCase("m/s/s")) {
                                    fact = 1.0;
                                } else {
                                    System.err.println("WARNING: Unknown accn units: " + units + " Assumings seconds");
                                }
                                sec.setBestAccn(Double.parseDouble(content.toString()) * fact);
                                break;
                            case Jerk:
                                fact = 1.0;
                                if (units.equalsIgnoreCase("m/s/s/s")) {
                                    fact = 1.0;
                                } else {
                                    System.err.println("WARNING: Unknown jerk units: " + units + " Assumings seconds");
                                }
                                sec.setBestJerk(Double.parseDouble(content.toString()) * fact);
                                break;

                            case Snr:
                                sec.setBestSnr(Double.parseDouble(content.toString()));
                                break;
                            case Width:
                                sec.setBestWidth(Double.parseDouble(content.toString()));
                                break;

                            case SampleRate:
                                sec.setTsamp(Double.parseDouble(content.toString()));
                                break;


                            // section body:
                            case SubIntegrations:
                                double[][] subints = new double[nSub][nBins];
                                k = 0;
                                nmax = Math.pow(16, digitsPerSamp);


                                for (i = 0; i < nSub; i++) {
                                    for (j = 0; j < nBins; j++) {
                                        int intV = 0;
                                        int posn = (int) nmax;
                                        for (int n = digitsPerSamp - 1; n >= 0; n--) {
                                            char c = content.charAt(k++);
                                            posn /= 16;
                                            while (!Character.isLetterOrDigit(c)) {
                                                c = content.charAt(k++);
                                            }
                                            intV += Character.digit(c, 16) * posn;

                                        }
                                        subints[i][j] = (((double) intV) / (nmax - 1)) * (max - min) + min;
                                    }
                                }



                                sec.setSubints(subints);

                                break;
                            case SubBands:
                                double[][] subbands = new double[nSub][nBins];

                                k = 0;
                                nmax = Math.pow(16, digitsPerSamp);


                                for (i = 0; i < nSub; i++) {
                                    for (j = 0; j < nBins; j++) {
                                        int intV = 0;
                                        int posn = (int) nmax;
                                        for (int n = digitsPerSamp - 1; n >= 0; n--) {
                                            posn /= 16;
                                            char c = content.charAt(k++);
                                            while (!Character.isLetterOrDigit(c)) {
                                                c = content.charAt(k++);
                                            }
                                            intV += Character.digit(c, 16) * posn;

                                        }
                                        subbands[i][j] = (((double) intV) / (nmax - 1)) * (max - min) + min;
                                    }
                                }
                                if (sec.getSubints() != null && nBins != sec.getSubints()[0].length) {
                                    // catch the case where the nbins and nsub are the wrong way round.
                                    sec.setSubbands(Convert.rotateDoubleArray(subbands));
                                } else {
                                    sec.setSubbands(subbands);
                                }
                                break;

                            case Profile:
                                double[] prof = new double[nBins];
                                k = 0;
                                nmax = Math.pow(16, digitsPerSamp);



                                for (j = 0; j < nBins; j++) {
                                    int intV = 0;
                                    int posn = (int) nmax;
                                    for (int n = digitsPerSamp - 1; n >= 0; n--) {
                                        posn /= 16;
                                        char c = content.charAt(k++);
                                        while (!Character.isLetterOrDigit(c)) {
                                            c = content.charAt(k++);
                                        }
                                        intV += Character.digit(c, 16) * posn;

                                    }

                                    prof[j] = (((double) intV) / (nmax - 1)) * (max - min) + min;
                                }

                                sec.setPulseProfile(prof);
                                break;


                            // The SNR block...

                            case PeriodIndex:
                                 {
                                    periodIdx = new double[nVals];
                                    String[] elems = content.toString().trim().split("\\s+");
                                    for (j = 0; j < nVals; j++) {
                                        periodIdx[j] = Double.parseDouble(elems[j]);
                                    }
                                }
                                break;

                            case DmIndex:
                                 {
                                    dmIdx = new double[nVals];
                                    String[] elems = content.toString().trim().split("\\s+");
                                    for (j = 0; j < nVals; j++) {
                                        dmIdx[j] = Double.parseDouble(elems[j]);
                                    }
                                }
                                break;

                            case AccnIndex:
                                 {
                                    accnIdx = new double[nVals];
                                    String[] elems = content.toString().trim().split("\\s+");
                                    for (j = 0; j < nVals; j++) {
                                        accnIdx[j] = Double.parseDouble(elems[j]);
                                    }
                                }
                                break;

                            case JerkIndex:
                                 {
                                    jerkIdx = new double[nVals];
                                    String[] elems = content.toString().trim().split("\\s+");
                                    for (j = 0; j < nVals; j++) {
                                        jerkIdx[j] = Double.parseDouble(elems[j]);
                                    }
                                }
                                break;

                            case DataBlock:
                                 {
                                    block = new double[dmIdx.length][periodIdx.length][accnIdx.length][jerkIdx.length];
                                    k = 0;
                                    nmax = Math.pow(16, digitsPerSamp);

                                    for (i = 0; i < dmIdx.length; i++) {
                                        for (j = 0; j < periodIdx.length; j++) {
                                            for (int m = 0; m < accnIdx.length; m++) {
                                                for (l = 0; l < jerkIdx.length; l++) {

                                                    int intV = 0;
                                                    int posn = (int) nmax;
                                                    for (int n = digitsPerSamp - 1; n >= 0; n--) {
                                                        posn /= 16;
                                                        char c = content.charAt(k++);
                                                        while (!Character.isLetterOrDigit(c)) {
                                                            c = content.charAt(k++);
                                                        }
                                                        intV += Character.digit(c, 16) * posn;

                                                    }
                                                    block[i][j][m][l] = (((double) intV) / (nmax - 1)) * (max - min) + min;
                                                }
                                            }
                                        }
                                    }
                                }
                                break;

                            case SnrBlock:
                                SNRBlock snrBlock = new SNRBlock(dmIdx, periodIdx, accnIdx, jerkIdx, block);
                                sec.setSnrBlock(snrBlock);
                                break;

                            case Other:
                            default:
                                break;
                        }
                    } catch (NumberFormatException ex) {
                        PulsarHunter.out.println("WARNING: Malformed number found in phcx file!\n" + ex.getMessage());
                    }
                    content = new StringBuffer();
                }

                public void endDocument() throws SAXException {
                }

                @Override
                public void error(SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void warning(SAXParseException e) throws SAXException {
                    throw e;
                }
            });
            XMLReaderparser.parse(new InputSource(inStream));
        } catch (SAXException ex) {
            throw new IOException("XML Parsing Exception: " + ex.getMessage());
        }catch (Exception ex) {
            throw new IOException("Exception occured reading phcx file "+this.getFile().getName()+" : " + ex.getMessage());
            
        }


    }

    public void readOldPHCF(BufferedInputStream inStream) throws IOException {
        DataInputStream in = new DataInputStream(inStream);

        String title = readASCII(in, 4);
        if (!title.equals("PHNT")) {
            throw new IOException("Provided file " + file.getName() + " is not a Pulsarhunter file");
        }
        String type = readASCII(in, 4);
        if (!type.equals("PHCF")) {
            throw new IOException("Provided file " + file.getName() + " is not a valid PHCF (PulsarHunter Candidate File) file");
        }

        this.header.read(in);

        PHCSection currentSection = null;

        while (in.available() >= 8) {

            String specifier = readASCII(in, 4);

            // System.out.println(specifier);

            if (specifier.equals("BEGN")) {
                currentSection = this.readPHCSection(in);
                this.addSection(currentSection);
                continue;
            }

            if (specifier.equals("SUBI")) {
                currentSection.setSubints(this.readDoubleArray2d(in));
                continue;
            }

            if (specifier.equals("SUBB")) {
                currentSection.setSubbands(this.readDoubleArray2d(in));
                continue;
            }


            if (specifier.equals("PROF")) {
                currentSection.setPulseProfile(this.readDoubleArray(in));
                continue;
            }

            if (specifier.equals("BLOK")) {
                currentSection.setSnrBlock(this.readSnrBlock(in));
                continue;
            }

            if (specifier.equals("XTRA")) {
                currentSection.readExtraField(in);
                continue;
            }


            // We don't match any known headers!

            int skip = in.readInt();
            in.skipBytes(skip);

        }
        this.optimisedSec = currentSection;

    }

    public void write() throws IOException {
        OutputStream out = new FileOutputStream(file);
        if (file.getName().endsWith(".gz")) {
            out = new java.util.zip.GZIPOutputStream(out);
        }
        if (file.getName().endsWith("phcf") || file.getName().endsWith("phcf.gz")) {
            // Old type file...
            this.writeOldPHCF(out);
        } else {
            // New type file...
            this.writeXML(out);
        }

    }

    public void writeXML(OutputStream outStream) throws IOException {
        PrintStream out = new PrintStream(outStream);
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        out.println("<phcf>");
        out.println("<head>");
        this.header.writeXML(out);
        out.println("</head>");

        out.println("<body>");

        for (String key : this.listSections()) {
            PHCSection sec = this.sections.get(key);

            out.println("<Section name='" + key + "'>");

            // Section Header...

            out.print("<BestValues>");
            out.printf("\n\t<TopoPeriod units='seconds'>%16.14f</TopoPeriod>", sec.getBestTopoPeriod());
            out.printf("\n\t<BaryPeriod units='seconds'>%16.14f</BaryPeriod>", sec.getBestBaryPeriod());
            out.printf("\n\t<Dm>%f</Dm>", sec.getBestDm());
            out.printf("\n\t<Accn units='m/s/s'>%f</Accn>", sec.getBestAccn());
            out.printf("\n\t<Jerk units='m/s/s/s'>%f</Jerk>", sec.getBestJerk());
            out.printf("\n\t<Snr>%f</Snr>", sec.getBestSnr());
            out.printf("\n\t<Width>%f</Width>", sec.getBestWidth());
            out.println("\n</BestValues>");

            out.printf("<SampleRate>%f</SampleRate>\n", sec.getTsamp());

            // Section Contents
            {
                double[][] subints = sec.getSubints();
                if (subints != null) {

                    double max = -Double.MAX_VALUE;
                    double min = Double.MAX_VALUE;
                    for (int i = 0; i < subints.length; i++) {
                        for (int j = 0; j < subints[0].length; j++) {
                            if (subints[i][j] > max) {
                                max = subints[i][j];
                            }
                            if (subints[i][j] < min) {
                                min = subints[i][j];
                            }
                        }
                    }
                    out.printf("<SubIntegrations nBins='%d' nSub='%d' format='02X' min='" + min + "' max='" + max + "'>\n", subints[0].length, subints.length);
                    int count = 0;
                    for (int i = 0; i < subints.length; i++) {
                        for (int j = 0; j < subints[0].length; j++) {
                            int val = (int) ((subints[i][j] - min) / (max - min) * 255);
                            out.printf("%02X", val);
                            count++;
                            if (count == 40) {
                                count = 0;
                                out.println();
                            }
                        }
                    }
                    out.println("</SubIntegrations>");
                }
            }

            {
                double[][] subBands = sec.getSubbands();
                if (subBands != null) {

                    double max = -Double.MAX_VALUE;
                    double min = Double.MAX_VALUE;
                    for (int i = 0; i < subBands.length; i++) {
                        for (int j = 0; j < subBands[0].length; j++) {
                            if (subBands[i][j] > max) {
                                max = subBands[i][j];
                            }
                            if (subBands[i][j] < min) {
                                min = subBands[i][j];
                            }
                        }
                    }
                    out.printf("<SubBands nBins='%d' nSub='%d' format='02X' min='" + min + "' max='" + max + "'>\n", subBands[0].length, subBands.length);
                    int count = 0;
                    for (int i = 0; i < subBands.length; i++) {
                        for (int j = 0; j < subBands[0].length; j++) {
                            int val = (int) ((subBands[i][j] - min) / (max - min) * 255);
                            out.printf("%02X", val);
                            count++;
                            if (count == 40) {
                                count = 0;
                                out.println();
                            }
                        }

                    }
                    out.println("</SubBands>");
                }
            }

            {
                double[] prof = sec.getPulseProfile();
                if (prof != null) {

                    double max = -Double.MAX_VALUE;
                    double min = Double.MAX_VALUE;
                    for (int i = 0; i < prof.length; i++) {

                        if (prof[i] > max) {
                            max = prof[i];
                        }
                        if (prof[i] < min) {
                            min = prof[i];
                        }

                    }
                    out.printf("<Profile nBins='%d' format='02X' min='" + min + "' max='" + max + "'>\n", prof.length);
                    int count = 0;
                    for (int i = 0; i < prof.length; i++) {

                        int val = (int) ((prof[i] - min) / (max - min) * 255);
                        out.printf("%02X", val);
                        count++;
                        if (count == 40) {
                            count = 0;
                            out.println();
                        }
                    }

                    out.println("</Profile>");
                }
            }

            {
                SNRBlock snrBlock = sec.getSnrBlock();
                if (snrBlock != null) {
                    out.println("<SnrBlock>");
                    if (snrBlock.isBarrycenter()) {
                        out.println("<BaryCentred />");
                    }

                    // The index
                    {
                        double[] index = snrBlock.getPeriodIndex();
                        out.printf("<PeriodIndex nVals='%d' format='6.12f '>\n", index.length);
                        for (int i = 0; i < index.length; i++) {
                            out.printf("%6.12f\n", index[i]);
                        }
                        out.println("</PeriodIndex>");
                    }
                    {
                        double[] index = snrBlock.getDmIndex();
                        out.printf("<DmIndex nVals='%d' format='2.4f '>\n", index.length);
                        for (int i = 0; i < index.length; i++) {
                            out.printf("%2.4f\n", index[i]);
                        }
                        out.println("</DmIndex>");
                    }
                    {
                        double[] index = snrBlock.getAccnIndex();
                        out.printf("<AccnIndex nVals='%d' format='2.4f '>\n", index.length);
                        for (int i = 0; i < index.length; i++) {
                            out.printf("%2.4e\n", index[i]);
                        }
                        out.println("</AccnIndex>");
                    }
                    {
                        double[] index = snrBlock.getJerkIndex();
                        out.printf("<JerkIndex nVals='%d' format='2.4e '>\n", index.length);
                        for (int i = 0; i < index.length; i++) {
                            out.printf("%2.4e\n", index[i]);
                        }
                        out.println("</JerkIndex>");
                    }

                    // The data block!

                    {
                        double[][][][] block = snrBlock.getBlock();
                        double max = -Double.MAX_VALUE;
                        double min = Double.MAX_VALUE;
                        for (int i = 0; i < block.length; i++) {
                            for (int j = 0; j < block[0].length; j++) {
                                for (int k = 0; k < block[0][0].length; k++) {
                                    for (int l = 0; l < block[0][0][0].length; l++) {

                                        if (block[i][j][k][l] > max) {
                                            max = block[i][j][k][l];
                                        }
                                        if (block[i][j][k][l] < min) {
                                            min = block[i][j][k][l];
                                        }
                                    }
                                }
                            }
                        }

                        out.printf("<DataBlock format='02X' max='%f' min='%f'>\n", max, min);
                        int count = 0;
                        for (int i = 0; i < block.length; i++) {
                            for (int j = 0; j < block[0].length; j++) {
                                for (int k = 0; k < block[0][0].length; k++) {
                                    for (int l = 0; l < block[0][0][0].length; l++) {
                                        int val = (int) ((block[i][j][k][l] - min) / (max - min) * 255);

                                        out.printf("%02X", val);
                                        count++;
                                        if (count == 40) {
                                            count = 0;
                                            out.println();
                                        }
                                    }
                                }

                            }
                        }
                        out.println("</DataBlock>");
                    }




                    out.println("</SnrBlock>");
                }
            }

            for (String xkey : sec.getExtraValues().keySet()) {
                //sec.writeExtraField(out,xkey);
                out.println("<SecExtra key='" + xkey + "'>" + sec.getExtraValueSafe(xkey) + "</SecExtra>");
            }

            out.println("</Section>");



        /*
        this.writePHCSection(out,sec);
        this.writeDoubleArray(sec.getSubints(), "SUBI",out);
        this.writeDoubleArray(sec.getSubbands(), "SUBB",out);
        if(sec.getSnrBlock()!=null)this.writeSnrBlock(out,sec.getSnrBlock(),"BLOK");
        this.writeDoubleArray(sec.getPulseProfile(), "PROF",out);
        for(String xkey : sec.getExtraValues().keySet()){
        sec.writeExtraField(out,xkey);
        }
         */


        }



        out.println("</body>");
        out.println("</phcf>");
        out.flush();
        out.close();

    }

    public void writeOldPHCF(OutputStream stream) throws IOException {

        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(stream));
        writeASCII(out, "PHNT");
        writeASCII(out, "PHCF");
        this.header.writeOldPHCF(out);

        for (String key : this.listSections()) {
            PHCSection sec = this.sections.get(key);

            this.writePHCSection(out, sec);

            this.writeDoubleArray(sec.getSubints(), "SUBI", out);
            this.writeDoubleArray(sec.getSubbands(), "SUBB", out);

            if (sec.getSnrBlock() != null) {
                this.writeSnrBlock(out, sec.getSnrBlock(), "BLOK");
            }

            this.writeDoubleArray(sec.getPulseProfile(), "PROF", out);

            for (String xkey : sec.getExtraValues().keySet()) {
                sec.writeExtraField(out, xkey);
            }

        }
        out.flush();
        out.close();
    }

    public PHCSection readPHCSection(DataInputStream in) throws IOException {
        in.readInt();
        int namesize = in.readInt();
        String name = this.readASCII(in, namesize);

        PHCSection result = new PHCSection(name);


        result.setBestTopoPeriod(in.readDouble());
        result.setBestBaryPeriod(in.readDouble());
        result.setBestDm(in.readDouble());
        result.setBestAccn(in.readDouble());
        result.setBestJerk(in.readDouble());
        result.setBestSnr(in.readDouble());
        result.setBestWidth(in.readDouble());
        result.setTsamp(in.readDouble());


        return result;

    }

    public void writePHCSection(DataOutputStream out, PHCSection sec) throws IOException {
        this.writeASCII(out, "BEGN");
        int size = sec.getName().length() + 7 * 8;
        out.writeInt(size);
        out.writeInt(sec.getName().length());
        this.writeASCII(out, sec.getName());

        out.writeDouble(sec.getBestTopoPeriod());
        out.writeDouble(sec.getBestBaryPeriod());
        out.writeDouble(sec.getBestDm());
        out.writeDouble(sec.getBestAccn());
        out.writeDouble(sec.getBestJerk());
        out.writeDouble(sec.getBestSnr());
        out.writeDouble(sec.getBestWidth());
        out.writeDouble(sec.getTsamp());

    }

    public void flush() throws IOException {
        this.write();
    }

    public Cand extractJReaperCand() {
        return this.extractJReaperCand(null);
    }

    public Cand extractJReaperCand(String resultsDir) {
        readIfRequired();
        //    public Cand(CandidateFile phfile,double period,float SNR,float DM,double accel,double jerk,Score score,double MJD) {
        this.altPath = resultsDir;

        Cand c = new Cand(this, this.getHeader().getOptimisedTopoPeriod() * 1000.0,
                (float) (this.getHeader().getOptimizedSNR()), (float) (this.getHeader().getOptimizedDm()),
                this.getHeader().getOptimisedAccn(), this.getHeader().getOptimisedJerk(), null, this.getHeader().getMjdStart());
        c.setNPulses(this.getNPulses());
        c.setSpecSNR((float) this.getHeader().getInitialSNR());
        if (this.getInitialSec().getExtraValue("SPECSNR") != null) {
            c.setSpecSNR(Float.parseFloat(this.getInitialSec().getExtraValueSafe("SPECSNR").trim()));
        }

        if (this.getInitialSec().getExtraValue("Recon") != null || this.getInitialSec().getExtraValue("RECONSNR") != null) {
            if (this.getInitialSec().getExtraValue("RECONSNR") != null) {
                c.setReconSNR(Float.parseFloat(this.getInitialSec().getExtraValueSafe("RECONSNR").trim()));
            } else {
                c.setReconSNR(Float.parseFloat(this.getInitialSec().getExtraValueSafe("Recon").trim()));
            }
        }
        //if(this.getInitialSec().getExtraValue("Recon")!=null)c.setReconSNR(Float.parseFloat(this.getInitialSec().getExtraValueSafe("Recon").trim()));
        if (this.getHeader().getExtraValue("ZAP") != null) {
            c.setDud(true);
        }
        return c;

    }

    public int getNPulses() {

        double[] profile = this.getOptimisedSec().getPulseProfile();
        if (profile == null) {
            return -1;
        }
        double max = 0;
        double sum = 0;
        int mPos = 1;
        int nBin = profile.length;
        for (int i = 0; i < nBin; i++) {
            double f = profile[i];
            if (f > max) {
                max = f;
                mPos = i;
            }
            sum += f;
        }
        double average = sum / nBin;
        double threashold = (max - average) / 2.5f + average;
        /*boolean onpulse = false;
        int numP = 0;
        for(int i = 0; i < profile.length; i++){
        if(onpulse){
        if(profile[i] < threashold){
        onpulse = false;
        //System.out.println("off:"+((double)i/(double)profile.length));
        }
        } else {
        if(profile[i] > threashold){
        numP ++;
        onpulse = true;
        //System.out.println("on:"+((double)i/(double)profile.length));
        }
        }
        }*/
        int numP;
        for (numP = 32; numP > 1; numP--) {
            boolean good = true;
            double spacing = (double) nBin / (double) numP;
            for (int i = 1; i <= numP; i++) {
                int bin = (int) (spacing * i) + mPos;
                while (bin >= nBin) {
                    bin -= nBin;
                }
                if (profile[bin] > threashold) {
                } else {
                    good = false;
                    break;
                }
            }
            if (good) {  // IE we see all the pulses...

                break;
            }
        }


        return numP;

    }

    private static void writeDoubleArray(double[][][][] array, String specifier, DataOutputStream out) throws IOException {
        if (array != null) {
            if (array.length > 0) {
                int size = array.length * array[0].length * array[0][0].length * array[0][0][0].length * 4 + 4 * 8;

                writeASCII(out, specifier);
                out.writeInt(size);
                out.writeInt(array.length);
                out.writeInt(array[0].length);
                out.writeInt(array[0][0].length);
                out.writeInt(array[0][0][0].length);

                for (double[][][] d1 : array) {
                    for (double[][] d2 : d1) {
                        for (double[] d3 : d2) {
                            for (double d : d3) {
                                out.writeFloat((float) d);
                            }
                        }
                    }
                }

            }
        }
    }

    private static void writeDoubleArray(double[][] array, String specifier, DataOutputStream out) throws IOException {
        if (array != null) {
            if (array.length > 0) {
                int size = array.length * array[0].length * 4 + 2 * 8;

                writeASCII(out, specifier);
                out.writeInt(size);
                out.writeInt(array.length);
                out.writeInt(array[0].length);

                for (double[] dArr : array) {
                    for (double d : dArr) {
                        out.writeFloat((float) d);
                    }
                }

            }
        }
    }

    private static double[][] readDoubleArray2d(DataInputStream in) throws IOException {

        in.readInt();
        int xSize = in.readInt();
        int ySize = in.readInt();

        double[][] result = new double[xSize][ySize];

        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                try {
                    result[i][j] = in.readFloat();
                } catch (IOException ex) {
                    System.err.print("i:" + i + " j:" + j);
                }
            }

        }
        return result;
    }

    private static double[][][][] readDoubleArray4d(DataInputStream in) throws IOException {

        in.readInt();
        int xSize = in.readInt();
        int ySize = in.readInt();
        int zSize = in.readInt();
        int wSize = in.readInt();



        double[][][][] result = new double[xSize][ySize][zSize][wSize];

        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                for (int k = 0; k < zSize; k++) {
                    for (int l = 0; l < wSize; l++) {

                        try {
                            result[i][j][k][l] = in.readFloat();
                        } catch (IOException ex) {
                            //  System.err.print("i:"+i+" j:"+j);
                        }
                    }
                }
            }

        }
        return result;
    }

    private static double[] readDoubleArray(DataInputStream in) throws IOException {

        in.readInt();
        int xSize = in.readInt();


        double[] result = new double[xSize];

        for (int i = 0; i < xSize; i++) {

            result[i] = in.readFloat();


        }
        return result;
    }

    private static void writeDoubleArray(double[] array, String specifier, DataOutputStream out) throws IOException {
        if (array != null) {
            if (array.length > 0) {
                int size = array.length * 4 + 8;

                writeASCII(out, specifier);
                out.writeInt(size);
                out.writeInt(array.length);



                for (double d : array) {
                    out.writeFloat((float) d);
                }


            }
        }
    }

    public static void writeASCII(DataOutputStream out, String text) throws IOException {

        for (char c : text.toCharArray()) {
            int i = (int) c;
            if (i > 255) {
                i = 0;
            }
            out.writeByte(i);
        }

    }

    public static String readASCII(DataInputStream in, int npoints) throws IOException {

        char[] carr = new char[npoints];
        for (int i = 0; i < npoints; i++) {
            carr[i] = (char) in.readByte();
        }

        return new String(carr);

    }

    public SNRBlock readSnrBlock(DataInputStream in) throws IOException {

        in.readInt();
        byte b = in.readByte();

        String field = readASCII(in, 4);
        double[] dmIndex = readDoubleArray(in);
        field = readASCII(in, 4);
        double[] periodIndex = readDoubleArray(in);
        field = readASCII(in, 4);
        double[] pdotIndex = readDoubleArray(in);
        field = readASCII(in, 4);
        double[] pddotIndex = readDoubleArray(in);




        field = readASCII(in, 4);

        double[][][][] blockData = readDoubleArray4d(in);

        SNRBlock block = new SNRBlock(dmIndex, periodIndex, pdotIndex, pddotIndex, blockData);
        if (b == 1) {
            block.setBarrycenter(true);
        }

        return block;
    }

    public void writeSnrBlock(DataOutputStream out, SNRBlock block, String specifier) throws IOException {
        writeASCII(out, "BLOK");
        out.writeInt(1);
        if (block.isBarrycenter()) {
            out.writeByte(1);
        } else {
            out.writeByte(0);
        }
        writeDoubleArray(block.getDmIndex(), "DMIX", out);
        writeDoubleArray(block.getPeriodIndex(), "P-IX", out);
        writeDoubleArray(block.getAccnIndex(), "PDIX", out);
        writeDoubleArray(block.getJerkIndex(), "DDIX", out);
        writeDoubleArray(block.getBlock(), "BDAT", out);


    }

    public class Header extends Data.Header {

        /*     private double initialTopoPeriod=-1;
        private double initialBaryPeriod=-1;
        private double initialPdot=0;
        private double initialPddot=-1;
        private double initialDm=-1;
        private double initialWidth=-1;
        private double initialSNR=-1;
        private double optimisedTopoPeriod=-1;
        private double optimisedBaryPeriod=-1;
        private double optimisedPdot=-1;
        private double optimisedPddot=-1;
        private double optimizedDm=-1;
        private double optimizedWidth=-1;
        private double optimizedSNR=-1;
        private double tsamp = -1;
         */
        private PulsarHunterCandidate cand;

        public Header(PulsarHunterCandidate cand) {
            this.cand = cand;
        }
        private Hashtable<String, String> extraValues = new Hashtable<String, String>();

        public void writeXML(PrintStream out) throws IOException {
            out.println("\t<Telescope>" + super.getTelescope().toString() + "</Telescope>");
            out.println("\t<SourceID>" + super.getSourceID() + "</SourceID>");
            out.println("\t<Coordinate>\n\t\t<RA units='degrees'>" + super.getCoord().getRA().toDegrees() + "</RA>\n\t\t<Dec units='degrees'>" + super.getCoord().getDec().toDegrees() + "</Dec>\n\t\t<Epoch>J2000</Epoch>\n\t</Coordinate>");
            out.println("\t<CentreFreq units='MHz'>" + super.getFrequency() + "</CentreFreq>");
            out.println("\t<BandWidth units='MHz'>" + super.getBandwidth() + "</BandWidth>");
            out.println("\t<MjdStart>" + super.getMjdStart() + "</MjdStart>");
            out.println("\t<ObservationLength units='seconds'>" + super.getTobs() + "</ObservationLength>");

            for (String key : this.extraValues.keySet()) {
                out.println("\t<Extra key='" + key + "'>" + this.extraValues.get(key) + "</Extra>");
            }
        }

        public void writeOldPHCF(DataOutputStream out) throws IOException {

            writeASCII(out, "HDR_");

            String sid = super.getSourceID();
            if (sid == null) {
                sid = "NOT_AVALIABLE";
            }
            int size = 7 * 8 + sid.length(); //168 + sid.length();


            out.writeInt(size); //Number of bytes

            out.writeInt(sid.length());
            writeASCII(out, sid);

            out.writeDouble(super.getCoord().getRA().toDegrees());
            out.writeDouble(super.getCoord().getDec().toDegrees());
            out.writeDouble(super.getFrequency());
            out.writeDouble(super.getBandwidth());
            out.writeDouble(super.getMjdStart());
            out.writeDouble(super.getTobs());
            out.writeShort((short) super.getTelescope().getId());


            /*  out.writeDouble(initialTopoPeriod);
            out.writeDouble(initialBaryPeriod);
            out.writeDouble(getInitialPdot());
            out.writeDouble(getInitialPddot());
            out.writeDouble(initialDm);
            out.writeDouble(initialWidth);
            out.writeDouble(initialSNR);
            out.writeDouble(optimisedTopoPeriod);
            out.writeDouble(optimisedBaryPeriod);
            out.writeDouble(getOptimisedPdot());
            out.writeDouble(getOptimisedPddot());
            out.writeDouble(optimizedDm);
            out.writeDouble(optimizedWidth);
            out.writeDouble(optimizedSNR);
            out.writeDouble(getTsamp());*/

            for (String key : this.extraValues.keySet()) {
                this.writeExtraField(out, key);
            }

            writeASCII(out, "ENDH");


        }

        public void read(DataInputStream in) throws IOException {

            String s = readASCII(in, 4);
            if (s.equals("HDR_")) {
                int size = in.readInt();

                int srcIDsize = in.readInt();

                this.setSourceID(readASCII(in, srcIDsize));

                double ra = in.readDouble();
                double dec = in.readDouble();

                this.setCoord(new Coordinate(new RA(ra), new Dec(dec)));

                this.setFrequency(in.readDouble());
                this.setBandwidth(in.readDouble());
                this.setMjdStart(in.readDouble());
                this.setTobs(in.readDouble());
                this.setTelescope(Telescope.getFromID(in.readShort()));



                /*   initialTopoPeriod = in.readDouble();
                initialBaryPeriod = in.readDouble();
                setInitialPdot(in.readDouble());
                this.setInitialPddot(in.readDouble());
                initialDm = in.readDouble();
                initialWidth = in.readDouble();
                initialSNR = in.readDouble();
                optimisedTopoPeriod = in.readDouble();
                optimisedBaryPeriod = in.readDouble();
                setOptimisedPdot(in.readDouble());
                this.setOptimisedPddot(in.readDouble());
                optimizedDm = in.readDouble();
                optimizedWidth = in.readDouble();
                optimizedSNR = in.readDouble();
                setTsamp((in.readDouble()));
                 */
                s = readASCII(in, 4);
                if (s.equals("HDRX")) {
                    this.readExtraField(in);
                }

                while (!s.equals("ENDH")) {
                    size = in.readInt();
                    in.skipBytes(size);
                    s = readASCII(in, 4);
                }

            } else {
                throw new IOException("Malformed Header in OSRF file");
            }
        }

        /**
         * Returns the Extravalue specified by the key, or null if it is not set.
         * @param key The field to return the value of
         * @return The value, or null
         * @see  getExtraValueSafe
         */
        public String getExtraValue(String key) {
            return this.extraValues.get(key);
        }

        /**
         * Reutrns the value of the specified extra field, but will never return a null.
         *
         * The return value for unspecified keys is "", but this is not required by subclasses
         * and may be changed in future versions (however it is intended that it be some sensible
         * value, i.e. N/A)
         * @param key The field to return the value of
         * @return The value requested, or some default value.
         * @see getExtraValue
         */
        public String getExtraValueSafe(String key) {
            if (this.extraValues.containsKey(key)) {
                return this.extraValues.get(key);
            } else {
                return "";
            }
        }

        public void setExtraValue(String key, String value) {
            this.extraValues.put(key, value);
        }

        private void readExtraField(DataInputStream in) throws IOException {
            in.readInt();
            int keySize = in.readInt();
            int valueSize = in.readInt();
            String key = PulsarHunterCandidate.readASCII(in, keySize);
            String value = PulsarHunterCandidate.readASCII(in, valueSize);

            this.extraValues.put(key, value);

        }

        private void writeExtraField(DataOutputStream out, String key) throws IOException {
            PulsarHunterCandidate.writeASCII(out, "XTRA");

            String value = this.extraValues.get(key);


            out.writeInt(key.length() + value.length());
            out.writeInt(key.length());
            out.writeInt(value.length());

            PulsarHunterCandidate.writeASCII(out, key);
            PulsarHunterCandidate.writeASCII(out, value);

        }

        public double getOptimizedSNR() {
            // return optimizedSNR;
            return cand.getOptimisedSec().getBestSnr();
        }

//        public void setOptimizedSNR(double optimizedSNR) {
//           this.optimizedSNR = optimizedSNR;
//        }
        public double getInitialTopoPeriod() {
//            return initialTopoPeriod;
            return cand.getInitialSec().getBestTopoPeriod();
        }

//        public void setInitialTopoPeriod(double initialTopoPeriod) {
//            this.initialTopoPeriod = initialTopoPeriod;
//        }
        public double getInitialBaryPeriod() {
            // return initialBaryPeriod;
            return cand.getInitialSec().getBestBaryPeriod();
        }

//        public void setInitialBaryPeriod(double initialBaryPeriod) {
//            this.initialBaryPeriod = initialBaryPeriod;
//        }
        public double getInitialDm() {
            //return initialDm;
            return cand.getInitialSec().getBestDm();
        }
//
//        public void setInitialDm(double initialDm) {
//            this.initialDm = initialDm;
//        }

        public double getInitialWidth() {
            //return initialWidth;
            return cand.getInitialSec().getBestWidth();
        }

//        public void setInitialWidth(double initialWidth) {
//            this.initialWidth = initialWidth;
//        }
        public double getInitialSNR() {
            // return initialSNR;
            return cand.getInitialSec().getBestSnr();
        }

//        public void setInitialSNR(double initialSNR) {
//            this.initialSNR = initialSNR;
//        }
        public double getOptimisedTopoPeriod() {
            // return optimisedTopoPeriod;
            return cand.getOptimisedSec().getBestTopoPeriod();
        }

//        public void setOptimisedTopoPeriod(double optimisedTopoPeriod) {
//            this.optimisedTopoPeriod = optimisedTopoPeriod;
//        }
        public double getOptimisedBaryPeriod() {
            //   return optimisedBaryPeriod;
            return cand.getOptimisedSec().getBestBaryPeriod();
        }

//        public void setOptimisedBaryPeriod(double optimisedBaryPeriod) {
//            this.optimisedBaryPeriod = optimisedBaryPeriod;
//        }
        public double getOptimizedDm() {
            // if(this.optimizedDm==-1)return this.initialDm;
            // else return optimizedDm;
            return cand.getOptimisedSec().getBestDm();
        }

//        public void setOptimizedDm(double optimizedDm) {
//            this.optimizedDm = optimizedDm;
//        }
        public double getOptimizedWidth() {
            // return optimizedWidth;
            return cand.getOptimisedSec().getBestWidth();
        }

//        public void setOptimizedWidth(double optimizedWidth) {
//            this.optimizedWidth = optimizedWidth;
//        }
        public double getOptimisedAccn() {
            //  return optimisedPdot;
            return cand.getOptimisedSec().getBestAccn();
        }

//        public void setOptimisedPdot(double optimisedPdot) {
//            this.optimisedPdot = optimisedPdot;
//        }
        public double getInitialAccn() {
            return cand.getInitialSec().getBestAccn();
        }

//        public void setInitialPdot(double initialPdot) {
//            this.initialPdot = initialPdot;
//        }
        public double getInitialJerk() {
            // return initialPddot;
            return cand.getInitialSec().getBestJerk();
        }

//        public void setInitialPddot(double initialPddot) {
//            this.initialPddot = initialPddot;
//        }
        public double getOptimisedJerk() {
            //return optimisedPddot;
            return cand.getOptimisedSec().getBestJerk();
        }

//        public void setOptimisedPddot(double optimisedPddot) {
//            this.optimisedPddot = optimisedPddot;
//        }
        public double getTsamp() {
            // return tsamp;
            return cand.getInitialSec().getTsamp();
        }
//        public void setTsamp(double tsamp) {
//            this.tsamp = tsamp;
//        }
        }

    public void setHeader(Header header) {

        this.header = header;
    }

    public File getFile() {
        if (this.altPath == null) {
            return file;
        } else {
            return new File(altPath + File.separator + file.getName());
        }
    }

    public JFrame getCandDisplayFrame(Cand c, MainView main) {
        readIfRequired();
        return new CandidateDisplayFrame(main, c, new PHCFPlot(this, c.getCandList().getDataLibrary().getOptions().getGrayColourMap(), Color.RED, Color.GREEN));
    }

    public String getName() {
        readIfRequired();
        return this.file.getName();
    }

    public CandidateFile deepClone() {
        readIfRequired();
        return new PulsarHunterCandidate(file);

    }

    public boolean hasSubints() {
        readIfRequired();
        return this.getOptimisedSec().getSubints() != null && this.getOptimisedSec().getSubints().length > 0;
    }

    public boolean hasProfile() {
        readIfRequired();
        return this.getOptimisedSec().getPulseProfile() != null && this.getOptimisedSec().getPulseProfile().length > 0;
    }

    public boolean hasHoughPlane() {
        return false;
//        readIfRequired();
//        return this.getOptimisedSec().getSnrBlock() != null;

    }

    public boolean hasFrequencyChannels() {
        readIfRequired();
        return this.getOptimisedSec().getSubbands() != null && this.getOptimisedSec().getSubbands().length > 0;
    }

    public boolean hasDMCurve() {
        readIfRequired();
        return this.getInitialSec().getSnrBlock() != null;
    }

    public boolean hasAditionalScore() {
        readIfRequired();
        return false;
    }

    public float getWidth() {
        readIfRequired();
        return (float) (this.header.getOptimizedWidth());
    }

    public float[] getProfile() {
        readIfRequired();
        return Convert.toFloatArr(this.getOptimisedSec().getPulseProfile(), 1.0);
    }

    public double[][] getHoughPlane() {
        return null;
//        readIfRequired();
//        return this.getOptimisedSec().getSnrBlock().getPDmPlane(this.getOptimisedSec().getBestAccn(),this.getOptimisedSec().getBestJerk());

    }

    public float[][] getFrequencyChanels() {
        readIfRequired();
        return Convert.toFloatArr(this.getOptimisedSec().getSubbands());

    }

    public float[][] getDMCurve() {
        readIfRequired();
        double period = this.getOptimisedSec().getBestTopoPeriod();
        if (this.getOptimisedSec().getSnrBlock().isBarrycenter()) {
            period = this.getOptimisedSec().getBestBaryPeriod();
        }

        //double[] dmc = this.getInitialSec().getSnrBlock().getDmCurve(period,this.getInitialSec().getBestAccn(),this.getInitialSec().getBestJerk());
        double[] dmc = this.getInitialSec().getSnrBlock().getFlatDmCurve();
        double[] dmi = this.getInitialSec().getSnrBlock().getDmIndex();
        float[][] result = new float[2][dmc.length];
        for (int i = 0; i < dmc.length; i++) {
            result[0][i] = (float) dmi[i];
            result[1][i] = (float) dmc[i];
        }
        return result;
    }

    public float getAdditionalScore() {
        readIfRequired();
        return 0;
    }

    public float[][] getSubints() {
        readIfRequired();
        if (this.getOptimisedSec().getSubints().length > 0) {
            return Convert.toFloatArr(this.getOptimisedSec().getSubints());
        } else {
            return null;
        }
    }

    public String getUniqueIdentifier() {
//        readIfRequired();
        return this.getFile().getName();
    }

    public PHCSection getInitialSec() {
        readIfRequired();
        return initialSec;
    }

    public PHCSection getOptimisedSec() {
        readIfRequired();
        return optimisedSec;
    }

    public File createImage(Cand c, String path) {

        readIfRequired();
        File imgFile = new File(path + File.separator + this.file.getName() + ".png");
        if (!imgFile.exists()) {
            try {
                // test
                BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
                Graphics g = img.getGraphics();
                g.setColor(new Color(255, 255, 255, 0));

                g.fillRect(0, 0, img.getWidth(), img.getHeight());

                PHCFImagePlot imgPlot = new PHCFImagePlot(c.getCandList().getDataLibrary().getOptions().getGrayColourMap(), Color.RED, Color.GREEN);
                imgPlot.draw(this, img);
                ImageIO.write(img, "png", imgFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                imgFile = null;
            }
        }

        return imgFile;
    }

    public boolean isReadOnDemand() {
        return readOnDemand;
    }

    public void setReadOnDemand(boolean readOnDemand) {
        this.readOnDemand = readOnDemand;
    }

    public void readIfRequired() {
        if (readOnDemand) {
            try {
                this.readOnDemand = false;
                this.read();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean hasPeriodCurve() {
        readIfRequired();
        return this.getOptimisedSec().getSnrBlock() != null && this.getOptimisedSec().getSnrBlock().getPeriodIndex().length > 1;
    }

    public boolean hasAccnCurve() {
        readIfRequired();
        return this.getInitialSec().getSnrBlock() != null && this.getInitialSec().getSnrBlock().getAccnIndex().length > 1;
    }

    public double[][] getPeriodCurve() {
        double[][] result = new double[2][this.getOptimisedSec().getSnrBlock().getPeriodIndex().length];
        System.arraycopy(this.getOptimisedSec().getSnrBlock().getPeriodIndex(), 0, result[0], 0, result[0].length);
        System.arraycopy(this.getOptimisedSec().getSnrBlock().getPeriodCurve(this.getOptimisedSec().getBestDm(), this.getOptimisedSec().getBestAccn(), this.getOptimisedSec().getBestJerk()), 0, result[1], 0, result[1].length);
        return result;

    }

    public double[][] getAccnCurve() {
        double[][] result = new double[2][this.getInitialSec().getSnrBlock().getAccnIndex().length];
        System.arraycopy(this.getInitialSec().getSnrBlock().getAccnIndex(), 0, result[0], 0, result[0].length);
        double period;
        if (this.getInitialSec().getSnrBlock().isBarrycenter()) {
            period = this.getInitialSec().getBestBaryPeriod();
        } else {
            period = this.getInitialSec().getBestTopoPeriod();
        }
        //System.arraycopy(this.getInitialSec().getSnrBlock().getAccnCurve(this.getInitialSec().getBestDm(),period,this.getInitialSec().getBestJerk()),0,result[1],0,result[0].length);
        System.arraycopy(this.getInitialSec().getSnrBlock().getFlatAccCurve(), 0, result[1], 0, result[0].length);
        return result;
    }

    public double getTobs() {
        return this.getHeader().getTobs();
    }
}


