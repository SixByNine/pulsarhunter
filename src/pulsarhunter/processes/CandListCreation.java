/*
 * CandListCreation.java
 *
 * Created on 11 April 2007, 14:07
 *
 *
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
package pulsarhunter.processes;

import coordlib.Beam;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import pulsarhunter.PulsarHunter;
import pulsarhunter.PulsarHunterProcess;
import coordlib.Telescope;
import java.util.Arrays;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.CandList;
import pulsarhunter.jreaper.CandScorer;
import pulsarhunter.jreaper.Score;
import pulsarhunter.jreaper.peckscorer.PeckScorer;
import pulsarhunter.jreaper.pmsurv.PulsarCandFile;
import pulsarhunter.jreaper.pmsurv.PulsarFile_aph;
import pulsarhunter.jreaper.pmsurv.PulsarFile_ph;
import pulsarhunter.jreaper.pmsurv.PulsarFile_sph;
import pulsarhunter.jreaper.pmsurv.aphfile_CandidateReader;
import pulsarhunter.jreaper.pmsurv.phfile_CandidateReader;
import pulsarhunter.jreaper.pmsurv.sphfile_CandidateReader;

/**
 *
 * @author mkeith
 */
public class CandListCreation implements PulsarHunterProcess {

    private final File[] loadFromDirs;
    private final String resultsRootRoot;
    private final String name;
    private final boolean reswd;

    /** Creates a new instance of CandListCreation */
    public CandListCreation(File[] loadFromDirs, String resultsRootRoot, String name, boolean reswd) {
        this.loadFromDirs = loadFromDirs;
        this.resultsRootRoot = resultsRootRoot;
        this.name = name;
        this.reswd = reswd;
    }

    public void run() {
        for (int ff = 0; ff < loadFromDirs.length; ff++) {

            File loadFromDir = loadFromDirs[ff];
            String resultsRoot;
            if (reswd) {
                try {
                    resultsRoot = new File(resultsRootRoot + loadFromDir.getPath()).getCanonicalPath();
                } catch (IOException ex) {
                    resultsRoot = resultsRootRoot + loadFromDir.getPath();
                }
            } else {
                resultsRoot = resultsRootRoot;
            }

            ArrayList<Cand> cands0 = new ArrayList<Cand>();
            ArrayList<Cand> cands1 = new ArrayList<Cand>();
            ArrayList<Cand> cands2 = new ArrayList<Cand>();
            ArrayList<Cand> cands3 = new ArrayList<Cand>();
            CandScorer scorer = new PeckScorer();
            Telescope telescope = Telescope.UNKNOWN;
            double freq = -1;
            double band = -1;
            double tobs = -1;
            Beam beam = null;
            double complete = 0;
            int totalTrials = loadFromDir.listFiles().length;
            double completeEta = 30.0 * 1.0 / (double) totalTrials;
            CandList dummyClist = null;
            PulsarHunter.out.println("Dir2Candist - Reading Candidates");
            PulsarHunter.out.println("Dir2Candist - JReaper Path: " + resultsRoot + "");
            PulsarHunter.out.println("Dir2Candist - |0%                        100%|");
            PulsarHunter.out.print("Dir2Candist - [");

            File[] files = loadFromDir.listFiles();
            Arrays.sort(files);

            for (File file : files) {
                complete += completeEta;
                while (complete > 1) {
                    PulsarHunter.out.print(".");
                    PulsarHunter.out.flush();
                    complete -= 1.0;
                }


                String filename = file.getName();
                String[] elems = filename.split("\\.");
                String ext = elems[elems.length - 1];


                Cand c = null;

                if (ext.equals("phcf") || ext.equals("phcx") || filename.endsWith(".phcx.gz")) {
                    PulsarHunterCandidate phcf = new PulsarHunterCandidate(file);
                    try {
                        phcf.read();
                        c = phcf.extractJReaperCand();

                        if (c == null) {
                            continue;
                        }
                        if (beam == null) {
                            beam = new Beam(phcf.getHeader().getSourceID(), phcf.getHeader().getCoord());
                        }
                        if (tobs < 0) {
                            tobs = phcf.getHeader().getTobs();
                        }
                        if (freq < 0) {
                            freq = phcf.getHeader().getFrequency();
                        }
                        if (band < 0) {
                            band = phcf.getHeader().getBandwidth();
                        }
                        if (dummyClist == null) {
                            dummyClist = new CandList(name, new Cand[5][0], beam);
                            dummyClist.setFch1(freq);
                            dummyClist.setBand(band);
                        }
                        c.setCandList(dummyClist);

                        Score score = new PeckScorer().score(c);

                        c.setScore(score);


                        // make up a new phcf with the altered path

                        phcf = new PulsarHunterCandidate(resultsRoot + File.separator + file.getName());
                        c.setPhfile(phcf);

                        cands3.add(c);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }

                if (ext.equals("ph")) {
                    phfile_CandidateReader candReader = new phfile_CandidateReader();
                    c = candReader.getFromFile(file, scorer);
                    if (c == null) {
                        continue;
                    }
                    if (beam == null) {
                        beam = c.getBeam();
                    }
                    if (tobs < 0) {
                        tobs = ((PulsarCandFile) c.getPhfile()).getTobs();
                        freq = 1374;
                        band = 288;
                        telescope = Telescope.PARKES;
                    }
                    c.setPhfile(new PulsarFile_ph(resultsRoot + File.separator + file.getName()));
                    cands0.add(c);
                }
                if (ext.equals("aph")) {
                    aphfile_CandidateReader candReader = new aphfile_CandidateReader();
                    c = candReader.getFromFile(file, scorer);
                    if (c == null) {
                        continue;
                    }
                    if (beam == null) {
                        beam = c.getBeam();
                    }
                    if (tobs < 0) {
                        tobs = ((PulsarCandFile) c.getPhfile()).getTobs();
                        freq = 1374;
                        band = 288;
                        telescope = Telescope.PARKES;
                    }
                    c.setPhfile(new PulsarFile_aph(resultsRoot + File.separator + file.getName()));
                    cands1.add(c);
                }
                if (ext.equals("sph")) {

                    if (elems[elems.length - 2].equals("std")) {
                        sphfile_CandidateReader candReader = new sphfile_CandidateReader(0);
                        c = candReader.getFromFile(file, scorer);
                        if (c == null) {
                            continue;
                        }
                        if (beam == null) {
                            beam = c.getBeam();
                        }
                        if (tobs < 0) {
                            tobs = ((PulsarCandFile) c.getPhfile()).getTobs();
                            freq = 1374;
                            band = 288;
                            telescope = Telescope.PARKES;
                        }
                        c.setPhfile(new PulsarFile_sph(resultsRoot + File.separator + file.getName()));
                        cands0.add(c);
                    }
                    if (elems[elems.length - 2].equals("acc")) {
                        sphfile_CandidateReader candReader = new sphfile_CandidateReader(1);
                        c = candReader.getFromFile(file, scorer);
                        if (c == null) {
                            continue;
                        }
                        if (beam == null) {
                            beam = c.getBeam();
                        }
                        if (tobs < 0) {
                            tobs = ((PulsarCandFile) c.getPhfile()).getTobs();
                            freq = 1374;
                            band = 288;
                            telescope = Telescope.PARKES;
                        }
                        c.setPhfile(new PulsarFile_sph(resultsRoot + File.separator + file.getName()));
                        cands1.add(c);
                    }
                    if (elems[elems.length - 2].equals("lng")) {
                        sphfile_CandidateReader candReader = new sphfile_CandidateReader(2);
                        c = candReader.getFromFile(file, scorer);
                        if (c == null) {
                            continue;
                        }
                        if (beam == null) {
                            beam = c.getBeam();
                        }
                        if (tobs < 0) {
                            tobs = ((PulsarCandFile) c.getPhfile()).getTobs();
                            freq = 1374;
                            band = 288;
                            telescope = Telescope.PARKES;
                        }
                        c.setPhfile(new PulsarFile_sph(resultsRoot + File.separator + file.getName()));
                        cands2.add(c);
                    }
                    if (elems[elems.length - 2].equals("pdm")) {
                        sphfile_CandidateReader candReader = new sphfile_CandidateReader(3);
                        c = candReader.getFromFile(file, scorer);
                        if (c == null) {
                            continue;
                        }
                        if (beam == null) {
                            beam = c.getBeam();
                        }
                        if (tobs < 0) {
                            tobs = ((PulsarCandFile) c.getPhfile()).getTobs();
                            freq = 1374;
                            band = 288;
                            telescope = Telescope.PARKES;
                        }
                        c.setPhfile(new PulsarFile_sph(resultsRoot + File.separator + file.getName()));
                        cands3.add(c);
                    }


                }
            }
            PulsarHunter.out.println("] done.");
            if (beam == null) {
                PulsarHunter.out.println("Dir2Candist - Empty Clist, ignoring.");
                continue;
            }
            PulsarHunter.out.println("Dir2Candist - Writing clist");
            Cand[][] cands = new Cand[5][0];
            cands[0] = cands0.toArray(cands[0]);
            cands[1] = cands1.toArray(cands[1]);
            cands[2] = cands2.toArray(cands[2]);
            cands[3] = cands3.toArray(cands[3]);
            cands[4] = new Cand[0];




            String localname = name;
            if (name == null) {
                localname = beam.getName().trim();
            }

            beam = new Beam(localname, beam.getCoord());


            CandList clist = new CandList(localname, cands, beam);
            clist.setFch1(freq);
            clist.setBand(band);
            clist.setTelescope(telescope);
            clist.setTobs(tobs);
            try {
                PrintStream out = new PrintStream(new java.util.zip.GZIPOutputStream(new FileOutputStream(localname + ".clist.gz")));
                clist.write(out);
                out.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            PulsarHunter.out.println("Dir2Candist - Done.");
        }
    }
}
