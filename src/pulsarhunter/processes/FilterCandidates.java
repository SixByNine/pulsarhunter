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
 * FilterCandidates.java
 *
 * Created on 15 January 2007, 15:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.processes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import pulsarhunter.BarryCenter;
import pulsarhunter.FrequencyFilter;
import pulsarhunter.PulsarHunter;
import pulsarhunter.PulsarHunterProcess;
import pulsarhunter.datatypes.BasicSearchResult;
import pulsarhunter.datatypes.BasicSearchResultData;
import pulsarhunter.datatypes.PHCSection;
import pulsarhunter.datatypes.PeriodSearchResultGroup;
import pulsarhunter.datatypes.PulsarHunterCandidate;
import pulsarhunter.datatypes.SNRBlock;
import pulsarhunter.datatypes.SearchResultComparator;
import pulsarhunter.jreaper.peckscorer.*;
import pulsarhunter.jreaper.Score;

/**
 *
 * @author mkeith
 */
public class FilterCandidates implements PulsarHunterProcess {

    private List<BasicSearchResult> rawSearchResults;
    private PeriodSearchResultGroup.SortField snrField;
    private double matchRangeFactor = 0;
    private String fileRoot;
    private BasicSearchResultData dataFile;
    private double snrMin = 0;
    private boolean useAccn = false;
    private boolean ignorePeriodLessThan4Tsamp = true;
    private boolean dumpHamonics = false;
    private boolean writesum = false;
    private int maxResults = Integer.MAX_VALUE;
    private double minProfileBins;
    private boolean nophcx = false;
    private boolean mjk_sigproc_fix = false;
    private FrequencyFilter[] filters = new FrequencyFilter[0];
    private PeckScorer scorer = new PeckScorer();
    boolean verbose = false;
    private boolean skipdm=false;

    /** Creates a new instance of FilterCandidates */
    public FilterCandidates(BasicSearchResultData dataFile, PeriodSearchResultGroup.SortField snrField, double matchRangeFactor, String fileRoot, double snrMin, int maxResults, boolean dumpHarmonics, double minProfileBins, boolean nophcx, boolean writesum, boolean mjk_sigproc_fix) {
        this.rawSearchResults = dataFile.getSearchResults();
        this.dataFile = dataFile;
        this.snrField = snrField;
        this.matchRangeFactor = matchRangeFactor;
        this.fileRoot = fileRoot;
        this.snrMin = snrMin;
        this.maxResults = maxResults;
        this.dumpHamonics = dumpHarmonics;
        this.writesum = writesum;
        this.minProfileBins = minProfileBins;
        this.nophcx = nophcx;
        this.mjk_sigproc_fix = mjk_sigproc_fix;
    }

    public void run() {


        SearchResultComparator comp = new SearchResultComparator<BasicSearchResult>(snrField);

        Collections.sort(rawSearchResults, Collections.reverseOrder(comp));

        ArrayList<PeriodSearchResultGroup<BasicSearchResult>> resultGroups = new ArrayList<PeriodSearchResultGroup<BasicSearchResult>>();

        PulsarHunter.out.print("FilterCandidates: Matching periods... ");
        PulsarHunter.out.flush();

        long[] tim = new long[4];
        Arrays.fill(tim, 0);
        Date start;
        Date end;
        int ignorecount = 0;
        int counter = 0;

        for (BasicSearchResult r : rawSearchResults) {
            if (verbose && (counter % 10000 == 0)) {
                System.out.printf("%d/%d\n", counter, rawSearchResults.size());
            }
            if (r.getTsamp() > 0 && this.ignorePeriodLessThan4Tsamp) {

                if (r.getPeriod() < r.getTsamp() * minProfileBins) {
                    ignorecount++;
                    continue;
                }

//                if (r.getPeriod() < 1.2e-6 * r.getDM() * minProfileBins) {
//                    ignorecount++;
//                    continue;
//                }

            }

            if (mjk_sigproc_fix) {
                double sp_snr = r.getSpectralSignalToNoise();
                double sp_freq = 1.0 / r.getPeriod();
                double fres = 1.0 / 540.0; //1.0/dataFile.getHeader().getTobs();
                double limit = 10;
                if (sp_freq < limit) {
                    sp_freq = limit;
                }
                double fmax = 0.5 / r.getTsamp();

                double A = 8000;
                double b = -12;
                if (r.getHarmfold() > 15) {
                    A = 401058635;
                    b = -18;
                } else if (r.getHarmfold() > 7) {
                    A = 7453033;
                    b = -16;
                } else if (r.getHarmfold() > 3) {
                    A = 307620;
                    b = -14;
                } else if (r.getHarmfold() > 1) {
                    A = 36720;
                    b = -13;
                }
                double p = (sp_freq / fres) * A * Math.pow(sp_snr, b);
//                r.setReconstructedSignalToNoise(r.getSpectralSignalToNoise());
                r.setSpectralSignalToNoise(Math.log(1.0 / p));
            }

            boolean inserted = false;
            double p1 = r.getPeriod();
            double p2 = 0;


            for (PeriodSearchResultGroup<BasicSearchResult> group : resultGroups) {
                p2 = group.getBestPeriod();
                if (Math.abs((p1 / p2) - 1.0) < matchRangeFactor) {
                    group.addSearchResult(r);
                    inserted = true;
                    break;
                }
            }



            if (!inserted) {
                PeriodSearchResultGroup<BasicSearchResult> group = new PeriodSearchResultGroup(snrField);
                group.addSearchResult(r);
                resultGroups.add(group);
            }
            counter++;

        }



        PulsarHunter.out.println("Found " + resultGroups.size() + " periods.");

        PulsarHunter.out.println("Done");
        PulsarHunter.out.println("FilterCandidates: Ignored " + ignorecount + " detections with P<4*tsamp");
        PulsarHunter.out.print("FilterCandidates: Matching harmonics... ");
        PulsarHunter.out.flush();



        PeriodSearchResultGroupComaprator groupComp = new PeriodSearchResultGroupComaprator(snrField, 0.0000001);
        Collections.sort(resultGroups, Collections.reverseOrder(groupComp));

        PrintStream harmout = null;
        if (this.dumpHamonics) {
            try {
                harmout = new PrintStream(new FileOutputStream(fileRoot + ".harms"));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        counter = 0;
        // for(PeriodSearchResultGroup g1 : (List<PeriodSearchResultGroup>)resultGroups.clone()){
        for (int i = 0; i < resultGroups.size(); i++) {
            if (verbose && (i % 100 == 0)) {
                System.out.printf("%d/%d\n", counter, resultGroups.size());
            }
            counter++;
            PeriodSearchResultGroup g1 = resultGroups.get(i);

            if (this.dumpHamonics) {
                harmout.printf("%f\t%f\t%f\n", g1.getBestPeriod() * 1000.0, g1.getBestSuspect().getSpectralSignalToNoise(), g1.getBestDM());
            }

            double period1 = g1.getBestPeriod();
            boolean first = false;
            for (PeriodSearchResultGroup g2 : (List<PeriodSearchResultGroup>) resultGroups.clone()) {
                // cycle through a clone of the groups to prevent modification exceptions...

                if (g2 == g1) {
                    first = true;
                    continue; // So we don't match ourself

                }
                if (!first) {
                    continue;
                }
                double period2 = g2.getBestPeriod();

                if ((this.skipdm || dmHarm(g1, g2, dataFile)) && isHarmonic(period1, period2, harmout)) {
                

                    if (this.dumpHamonics) {
                        harmout.printf("\t%f\t%f\n",
                                g2.getBestSuspect().getSpectralSignalToNoise(), g2.getBestDM());
                    }

                    g1.addHarmonic(g2);
                    resultGroups.remove(g2);
                }

            }

            if (this.dumpHamonics) {
                harmout.println("#############################\n");
            }
        }

        PulsarHunter.out.println("Done");



        if (this.dumpHamonics) {
            harmout.close();
        }




        double[] dmIndex = dataFile.getDmIndex();
        double[] acIndex = dataFile.getAcIndex();
        double[] adIndex = dataFile.getAdIndex();


        int n = 0;
        PulsarHunter.out.println("FilterCandidates: Writing Results");
        PulsarHunter.out.println("");
        PrintStream listFile = null;
        try {
            listFile = new PrintStream(new FileOutputStream(fileRoot + ".lis"));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        PulsarHunter.out.println("Spec SNR\tRecon SNR\tPeriod\t\tDM\t\tAccel\t\tJerk\t\t#Harm\tFold");
        // else PulsarHunter.out.println("SNR\t\tPeriod\t\tDM\t\tPdot\t\tPddot\t\t#Harm\tFold");
        PulsarHunter.out.println("============================================================================================================");

        groupComp = new PeriodSearchResultGroupComaprator(snrField, 0.0000001);
        Collections.sort(resultGroups, Collections.reverseOrder(groupComp));

        for (PeriodSearchResultGroup<BasicSearchResult> g : resultGroups) {
            n++;
            BasicSearchResult sr = g.getBestSuspect();
            double bestRecon = g.getBestRecon();
            boolean stop = (n > this.maxResults);
            switch (snrField) {
                case SPECTRAL_SNR:
                    if (sr.getSpectralSignalToNoise() < snrMin) {
                        stop = true;
                    }
                    break;
                case RECONSTRUCTED_SNR:
                    if (bestRecon < snrMin) {
                        stop = true;
                    }
                    break;
                case FOLD_SNR:
                    if (sr.getFoldSignalToNoise() < snrMin) {
                        stop = true;
                    }
                    break;
            }
            if (stop) {
                break;
            }
            boolean zap = false;
            char zapChar = ' ';
            String zapName = "";
            for (FrequencyFilter f : filters) {
                if (f.periodMatch(sr.getPeriod())) {
                    zap = true;
                    zapChar = '*';
                    zapName = f.getName();
                    f.setMatches(f.getMatches() + 1);
                    break;
                }
            }


            StringBuffer nStr = new StringBuffer(4);
            nStr.append(n);
            while (nStr.length() < 3) {
                nStr.insert(0, "0");
            }
            String fname;

            if (zap) {
                fname = fileRoot + "_" + nStr + "_zap.phcx.gz";
            } else {
                fname = fileRoot + "_" + nStr + ".phcx.gz";
            //System.out.printf("%f\t%f\t%f\t%f\t%e\t%d\t%d\n",sr.getSpectralSignalToNoise(),sr.getPeriod()*1000.0,sr.getDM(),Convert.pdotToAcc(sr.getPeriod(),sr.getAccn()),Convert.pddotToJerk(sr.getPeriod(),sr.getJerk(),sr.getAccn()),g.getHarmonics().size(),sr.getHarmfold());
            }

            double dopp = 1.0;
            if (BarryCenter.isAvaliable()) {

                BarryCenter bc = new BarryCenter(dataFile.getHeader().getMjdStart(),
                        dataFile.getHeader().getTelescope(),
                        dataFile.getHeader().getCoord().getRA().toDegrees(),
                        dataFile.getHeader().getCoord().getDec().toDegrees());

                dopp = bc.getDopplerFactor();

            }

            /*
             * Here we write out topocentric period, because
             * other tools expect topocentric values
             * independant of whether the input file was barycentred.
             */
            double convFactor = 1.0;
            if (dataFile.getHeader().isBarryCentered()) {
                convFactor = dopp;
            }


            System.out.printf("%f\t%f\t%f %c\t%f\t%e\t%e\t%d\t%d\n", sr.getSpectralSignalToNoise(), bestRecon, convFactor * sr.getPeriod() * 1000.0, zapChar, sr.getDM(), sr.getAccn(), sr.getJerk(), g.getHarmonics().size(), sr.getHarmfold());

            if (listFile != null) {
                listFile.printf("%s\t%f\t%12.8f\t%f\t%e\t%e\t%d\t%d\n", fname, sr.getSpectralSignalToNoise(), convFactor * sr.getPeriod() * 1000.0, sr.getDM(), sr.getAccn(), sr.getJerk(), g.getHarmonics().size(), sr.getHarmfold());
            }
            if (dmIndex.length == 0) {
                dmIndex = new double[]{sr.getDM()};
            }

            if (writesum) {
                File sumfile = new File(fileRoot + "_" + nStr + ".sum");
                try {
                    PrintStream sumout = new PrintStream(new FileOutputStream(sumfile));
                    sumout.println("\t" + fname);
                    sumout.printf("%f\t%f\t%f\t%d\n", sr.getPeriod(), sr.getDM(), sr.getSpectralSignalToNoise(), sr.getHarmfold());
                    HashMap<Double, Double> dmsnr = new HashMap<Double, Double>();
                    for (BasicSearchResult searchResult : g.getDmPdotPddotCube()) {
                        Double oldv = dmsnr.get((Double) searchResult.getDM());
                        if (oldv != null) {
                            if (oldv.doubleValue() < searchResult.getSpectralSignalToNoise()) {
                                dmsnr.remove(searchResult.getDM());
                                dmsnr.put(searchResult.getDM(), searchResult.getSpectralSignalToNoise());
                            }
                        } else {
                            dmsnr.put(searchResult.getDM(), searchResult.getSpectralSignalToNoise());
                        }
                    }
                    int v = 0;
                    ArrayList<Double> sorted = new ArrayList<Double>(dmsnr.keySet());
                    Collections.sort(sorted);
                    for (Double key : sorted) {
                        v++;
                        sumout.printf("\t%d\t%f\t%f\n", v, key.doubleValue(), dmsnr.get(key).doubleValue());

                    }

                    sumout.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

            if (!nophcx) {
                PHCSection sec = new PHCSection("FFT");

                sec.setBestDm(sr.getDM());
                sec.setBestAccn(sr.getAccn());
                sec.setBestJerk(sr.getJerk());
                sec.setBestSnr(sr.getSpectralSignalToNoise());
                sec.setExtraValue("RECONSNR", String.valueOf(bestRecon));
                sec.setExtraValue("SPECSNR", String.valueOf(sr.getSpectralSignalToNoise()));

                if (dataFile.getHeader().isBarryCentered()) {
                    sec.setBestBaryPeriod(sr.getPeriod());
                    sec.setBestTopoPeriod(sr.getPeriod() * dopp);
                } else {
                    sec.setBestBaryPeriod(sr.getPeriod() / dopp);
                    sec.setBestTopoPeriod(sr.getPeriod());

                }
                sec.setExtraValue("HFOLD", String.valueOf(sr.getHarmfold()));

//
//            double[] pdotIndex = new double[acIndex.length];
//            double[] pddotIndex = new double[adIndex.length];
//
//            if(pdotIndex.length == 0)pdotIndex = new double[]{sr.getAccn()};
//            else {
//
//                for(int i = 0; i < acIndex.length; i++){
//                    pdotIndex[i] = Convert.accToPdot(sr.getPeriod(),acIndex[i]);
//                }
//            }
//            if(pddotIndex.length == 0)pddotIndex = new double[]{sr.getJerk()};
//            else {
//
//                for(int i = 0; i < adIndex.length; i++){
//                    pddotIndex[i] = Convert.jerkToPddot(sr.getPeriod(),adIndex[i],0);
//                }
//            }
//


                SNRBlock block = new SNRBlock(dmIndex, new double[]{sr.getPeriod()}, acIndex, adIndex);

                block.setBarrycenter(dataFile.getHeader().isBarryCentered());

                for (BasicSearchResult searchResult : g.getDmPdotPddotCube()) {
                    if (searchResult.getSpectralSignalToNoise() > block.getPoint(searchResult.getDM(), sr.getPeriod(), searchResult.getAccn(), searchResult.getJerk())) {
                        block.addPoint(searchResult.getDM(), sr.getPeriod(), searchResult.getAccn(), searchResult.getJerk(), searchResult.getSpectralSignalToNoise());
                    }
                //    System.out.printf("\t%f\t%f\t%f\t%d\t%f\n",searchResult.getDM(),searchResult.getPdot(),searchResult.getPddot(),searchResult.getHarmfold(),searchResult.getSpectralSignalToNoise());
                }

                sec.setSnrBlock(block);


                PulsarHunterCandidate cand = new PulsarHunterCandidate(new File(fname));
                if (zap) {
                    cand.getHeader().setExtraValue("ZAP", zapName);
                }
                cand.addSection(sec);
                cand.getHeader().setCoord(dataFile.getHeader().getCoord());
                cand.getHeader().setBandwidth(dataFile.getHeader().getBandwidth());
                cand.getHeader().setFrequency(dataFile.getHeader().getFrequency());
                cand.getHeader().setMjdStart(dataFile.getHeader().getMjdStart());
                cand.getHeader().setSourceID(dataFile.getHeader().getSourceID());
                cand.getHeader().setTelescope(dataFile.getHeader().getTelescope());
                cand.getHeader().setTobs(dataFile.getHeader().getTobs());
                try {
                    cand.write();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        listFile.close();
        PulsarHunter.out.println("FilterCandidates: Done");

    }

    private boolean dmHarm(PeriodSearchResultGroup g1, PeriodSearchResultGroup g2, BasicSearchResultData datafile) {

        double snrCut = 0.75 * g1.getBestSuspect().getSpectralSignalToNoise();
        double minDM = 10000;
        double maxDM = 0;
        for (Object o : g1.getDmPdotPddotCube()) {
            BasicSearchResult r = (BasicSearchResult) o;
            if (r.getSpectralSignalToNoise() > snrCut) {
                if (r.getDM() < minDM) {
                    minDM = r.getDM();
                }
                if (r.getDM() > maxDM) {
                    maxDM = r.getDM();
                }
            }
        }
        minDM -= 1;
        maxDM += 1;
//        double p1 = g1.getBestPeriod();
//        double dm1 = g1.getBestDM();
//        double dm2 = g2.getBestDM();
//
//        double dmr = 100; //@TODO: Fix this!
//
//        // return (Math.abs(dm1 - dm2) < dmr);
//
//        double snr1 = g1.getSnrInDmRange(dm2, dmr);
//        double snr2 = g2.getSnrInDmRange(dm1, dmr);
////        if(isHarmonic(p1, g2.getBestPeriod(), null)){
////            System.out.printf("HH %f %f %f %f .. %f %f\n",p1,g2.getBestPeriod(),snr1,snr2, g1.getSnrInDmRange(dm1, dmr),g2.getSnrInDmRange(dm2, dmr));
////        }
//
//
//        if (snr1 < 0.9 * g1.getBestSuspect().getSpectralSignalToNoise()) {
//            return false;
//        }
//
//        if (snr2 < 0.9 * g2.getBestSuspect().getSpectralSignalToNoise()) {
//            return false;
//        }

        return (g2.getBestSuspect().getDM() > minDM && g1.getBestSuspect().getDM() < maxDM);

    }

    private boolean isHarmonic_pmminifind(double p1, double p2, PrintStream write) {

        double f1 = 1 / p2;
        double f2 = 1 / p1;


        if (f1 > f2) {
            double ratio = f1 / f2;

            if (ratio < 64) {
                if (Math.abs(ratio) - (int) (ratio + 0.5) < matchRangeFactor * (int) (ratio + 0.5)) {
                    write.printf("\t%f\t%f\tINT", p2 * 1000.0, (p1 / p2));
                    return true;
                } else {
                    for (int k = 1; k <
                            8; k++) {
                        if (Math.abs(ratio * k - (int) (k * ratio + 0.5)) < matchRangeFactor * (int) (ratio * k + 0.5)) {
                            write.printf("\t%f\t%f\tNonI %d", p2 * 1000.0, ratio, k);

                            return true;
                        }

                    }
                }
            }
        } else {
            double ratio = f2 / f1;
            if (ratio < 20) {
                if (Math.abs(ratio - 1) < matchRangeFactor) {
                    write.printf("\t%f\t%f\tH1", p2 * 1000.0, (p1 / p2));

                    return true;
                }

                for (int k = 1; k <
                        8; k++) {
                    if (Math.abs(ratio * k - (int) (k * ratio + 0.5)) < matchRangeFactor * (int) (ratio * k + 0.5)) {
                        write.printf("\t%f\t%f\tSUB", p2 * 1000.0, (p1 / p2));

                        return true;
                    }

                }

            }
        }
        return false;
    }
    private HashMap<String, Double> ratios = null;

    private boolean isHarmonic(double p1, double p2, PrintStream write) {

        if (ratios == null) {
            ratios = new HashMap<String, Double>();
            for (int i = 1; i <
                    64; i++) {
                ratios.put(String.valueOf(i), (double) i);
                if (verbose) {
                    System.out.print(i + ", ");
                }
                if (i % 8 == 0) {
                    if (verbose) {
                        System.out.println();
                    }
                }

            }
            if (verbose) {
                System.out.println();
            }
            for (int i = 3; i <
                    9; i++) {
                ratios.put(String.valueOf(i), (double) i);
                if (verbose) {
                    System.out.print("1/" + i + ", ");
                }
                if (i % 8 == 0) {
                    if (verbose) {
                        System.out.println();
                    }
                }

            }

            for (int top = 1; top <=
                    19; top++) {
                if (verbose) {
                    System.out.println();
                }
                for (int bottom = 1; bottom <
                        top; bottom++) {
                    double ratio = (double) top / (double) bottom;
                    boolean ok = true;
                    for (double r : ratios.values()) {
                        if (Math.abs(ratio - r) < 0.00001) {
                            ok = false;
                            break;

                        }


                    }
                    if (ok) {
                        ratios.put(top + "/" + bottom, ratio);
                        if (verbose) {
                            System.out.print(top + "/" + bottom + ", ");
                        }
                    }

                }
            }
            if (verbose) {
                System.out.println();
            }
            for (int bottom = 1; bottom <
                    8; bottom++) {
                if (verbose) {
                    System.out.println();
                }
                for (int top = 1; top <
                        bottom; top++) {
                    double ratio = (double) top / (double) bottom;
                    boolean ok = true;
                    for (double r : ratios.values()) {
                        if (Math.abs(ratio - r) < 0.00001) {
                            ok = false;
                            break;

                        }


                    }
                    if (ok) {
                        ratios.put(top + "/" + bottom, ratio);
                        if (verbose) {
                            System.out.print(top + "/" + bottom + ", ");
                        }
                    }

                }
            }
            if (verbose) {
                System.out.println();
            }


        }

        double f1 = 1.0 / p1;
        double f2 = 1.0 / p2;




        for (String k : ratios.keySet()) {
            if (isHarmonic(f1, f2, ratios.get(k))) {
                if (write != null) {
                    write.printf("\t%f\t%f\t%s", p2 * 1000.0, (f2 / f1), k);
                }

                return true;
            }

        }


        return false;
    }

    private boolean isHarmonic(double f1, double f2, double ratio) {
        if (Math.abs(f2 / f1 - ratio) < matchRangeFactor * ratio) {
            return true;
        } else {
            return false;
        }

    }

    private boolean OLDisHarmonic(double p1, double p2) {


        // Stop really large harmonics
        if (((p1 / p2) > 10) || ((p2 / p1) > 10)) {
            return false;
        }

        if (harmCalc(p1, p2)) {
            return true;
        } else if (harmCalc(p1, 3 * p2)) {
            return true;
        } else if (harmCalc(p1, 4 * p2)) {
            return true;
        } else if (harmCalc(p1, 5 * p2)) {
            return true;
        } else if (harmCalc(p1, 6 * p2)) {
            return true;
        } else if (harmCalc(p1, 7 * p2)) {
            return true;
        } else if (harmCalc(3 * p1, p2)) {
            return true;
        } else if (harmCalc(4 * p1, p2)) {
            return true;
        } else if (harmCalc(5 * p1, p2)) {
            return true;
        } else if (harmCalc(6 * p1, p2)) {
            return true;
        } else if (harmCalc(7 * p1, p2)) {
            return true;
        } else {
            return false;
        }

    }

    private boolean harmCalc(double p1, double p2) {
        //       System.out.println(p1+"\t"+p2+"\t"+((p2/p1) - (int)(p2/p1)));
        //     System.out.println(p1+"\t"+p2+"\t"+((p1/p2) - (int)(p1/p2)));
        //    System.out.println();
        if ((((p1 / p2) - (int) (p1 / p2)) < matchRangeFactor) || (-((p1 / p2) - (int) (p1 / p2) - 1.0) < matchRangeFactor)) {
            return true;
        } else if ((((p2 / p1) - (int) (p2 / p1)) < matchRangeFactor) || (-((p2 / p1) - (int) (p2 / p1) - 1.0) < matchRangeFactor)) {

            return true;
        } else {
            return false;
        }








    }

    /**
     * @return the skipdm
     */
    public boolean isSkipdm() {
        return skipdm;
    }

    /**
     * @param skipdm the skipdm to set
     */
    public void setSkipdm(boolean skipdm) {
        this.skipdm = skipdm;
    }

    private class PeriodSearchResultGroupComaprator implements Comparator<PeriodSearchResultGroup> {

        private double matchrange;
        PeriodSearchResultGroup.SortField sortField;

        public PeriodSearchResultGroupComaprator(PeriodSearchResultGroup.SortField sortField, double matchrange) {
            this.sortField = sortField;
            this.matchrange = 1.0 / matchrange;
        }

        public int compare(PeriodSearchResultGroup o1, PeriodSearchResultGroup o2) {
            switch (sortField) {
                case PERIOD:
                    return (int) ((o1.getBestPeriod() - o2.getBestPeriod()) * matchrange);
                case SPECTRAL_SNR:
                    return (int) ((o1.getBestSuspect().getSpectralSignalToNoise() - o2.getBestSuspect().getSpectralSignalToNoise()) * matchrange);
                case RECONSTRUCTED_SNR:
                    return (int) ((o1.getBestRecon() - o2.getBestRecon()) * matchrange);
                case FOLD_SNR:
                    return (int) ((o1.getBestSuspect().getFoldSignalToNoise() - o2.getBestSuspect().getFoldSignalToNoise()) * matchrange);
                default:
                    return 0;
            }

        }
    }

    public boolean isUseAccn() {
        return useAccn;
    }

    public void setUseAccn(boolean useAccn) {
        this.useAccn = useAccn;
    }

    public FrequencyFilter[] getFilters() {
        return filters;
    }

    public void setFilters(FrequencyFilter[] filters) {
        this.filters = filters;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        System.out.println("FILTERCANDIDATES: VERBOSE MODE");
        this.verbose = verbose;
    }
}


