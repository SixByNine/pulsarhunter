/*
 * Phcf2Phcx.java
 *
 * Created on 19 October 2007, 15:07
 *
 *
Copyright (C) 2005-2007 Michael Keith

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
package pulsarhunter;

import bookkeepr.xml.XMLReader;
import bookkeepr.xml.XMLWriter;
import bookkeepr.xmlable.RawCandidate;
import bookkeepr.xmlable.RawCandidateSection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.xml.sax.SAXException;
import pulsarhunter.datatypes.PHCSection;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.jreaper.Cand;

/**
 *
 * @author mkeith
 */
public class Phcx2RawCandidate {

    public static void main(String[] args) {

        for (String s : args) {
            try {

                File file = new File(s);

                File outFile = null;

                if (s.endsWith(".phcx") || s.endsWith(".phcf")) {
                    outFile = new File(s.substring(0, s.length() - 5) + ".xml.gz");
                } else if (s.endsWith(".phcx.gz") || s.endsWith(".phcf.gz")) {
                    outFile = new File(s.substring(0, s.length() - 8) + ".xml.gz");
                }

                if (outFile == null) {
                    System.err.println("File " + s + " doesn't seem to be a phcx file!");
                } else {
                    System.out.println(s + " -> " + outFile.toString());
                    PulsarHunterCandidate phcf = new PulsarHunterCandidate(file);
                    phcf.read();

                    Cand cand = phcf.extractJReaperCand();

                    RawCandidate rawCandidate = new RawCandidate();

                    rawCandidate.setBandwidth((float) phcf.getHeader().getBandwidth());
                    rawCandidate.setCentreFreq((float) phcf.getHeader().getFrequency());
                    rawCandidate.setCoordinate(phcf.getHeader().getCoord());
                    rawCandidate.setMjdStart(phcf.getHeader().getMjdStart());

                    rawCandidate.setReconstructedSnr(cand.getReconSNR());
                    rawCandidate.setSourceId(phcf.getHeader().getSourceID());
                    rawCandidate.setSpectralSnr(cand.getSpecSNR());
                    rawCandidate.setTelescope(phcf.getHeader().getTelescope().toString());
                    rawCandidate.setTobs(phcf.getTobs());
                    long unixtime_ms = (long) (86400000.0 * (phcf.getHeader().getMjdStart() - 40587.0));
                    rawCandidate.setUtc(new Date(unixtime_ms));

                    for (String key : phcf.listSections()) {
                        PHCSection pSec = phcf.getSection(key);
                        RawCandidateSection rSec = new RawCandidateSection();
                        rSec.setBestAccn((float)pSec.getBestAccn());
                        if(pSec.getBestBaryPeriod()!=-1)rSec.setBestBaryPeriod((float)pSec.getBestBaryPeriod());
                        if(pSec.getBestDm()!=-1)rSec.setBestDm((float)pSec.getBestDm());
                        rSec.setBestJerk((float)pSec.getBestJerk());
                        if(pSec.getBestSnr()!=-1)rSec.setBestSnr((float)pSec.getBestSnr());
                        if(pSec.getBestTopoPeriod()!=-1)rSec.setBestTopoPeriod((float)pSec.getBestTopoPeriod());
                        if(pSec.getBestWidth()!=-1)rSec.setBestWidth((float)pSec.getBestWidth());
                        rSec.setName(pSec.getName());
                        rSec.setProfile(pSec.getPulseProfile());
                        rSec.setSubBands(pSec.getSubbands());
                        rSec.setSubIntegrations(pSec.getSubints());
                        rSec.setSnrBlock(pSec.getSnrBlock());
                        rawCandidate.addRawCandidateSection(rSec);
                    }



                    GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(outFile));
                    XMLWriter.write(out, rawCandidate);
                    out.close();
//                    
//                    GZIPInputStream in = new GZIPInputStream(new FileInputStream(outFile));
//                    try {
//                        XMLReader.read(in);
//                    } catch (SAXException ex) {
//                        Logger.getLogger(Phcx2RawCandidate.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    in.close();
                    
                }
                
                


            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }


    }
}
