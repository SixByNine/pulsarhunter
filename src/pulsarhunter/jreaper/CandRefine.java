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
 * CandRefine.java
 *
 * Created on 25 May 2005, 09:52
 */
package pulsarhunter.jreaper;

import coordlib.Beam;
import coordlib.Coordinate;
import coordlib.CoordinateDistanceComparitor;
import coordlib.CoordinateDistanceComparitorGalactic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import pulsarhunter.jreaper.HarmonicType;

/**
 * A class for refining candidate lists. Allows for candidates to be filtered by removing low DM and low SNR candidates, as well as selecting particliar
 * @author mkeith
 */
public class CandRefine {

    private DataLibrary dataLibrary;
    private ArrayList<double[]> fracHarms = new ArrayList<double[]>();

    /**
     * Creates a new instance of CandRefine
     */
    public CandRefine(DataLibrary dataLibrary) {
        this.dataLibrary = dataLibrary;
        int fracHarmLimit = 32;

        for (int bottomfactor = 1; bottomfactor <= fracHarmLimit; bottomfactor++) {
            for (int topfactor = 1; topfactor < fracHarmLimit; topfactor++) {
                double factor = ((double) topfactor) / ((double) bottomfactor);
                boolean add = true;
                for (double[] arr : fracHarms) {
                    if (Math.abs(factor - arr[0]) < 0.001 * Math.min(arr[0], factor)) {
                        add = false;
                    }
                    if (add) {
                        fracHarms.add(new double[]{factor, topfactor, bottomfactor});
                    }
                }
            }
        }
    }

    public Cand[][] refine(Cand[][] masterData, Hashtable<PlotType.axisType, Double> minVals, Hashtable<PlotType.axisType, Double> maxVals, boolean[] dataSeries, String[] excludeBeams) {
        ArrayList[] cData = new ArrayList[masterData.length];
        Arrays.sort(excludeBeams, String.CASE_INSENSITIVE_ORDER);
        PlotType pt = new PlotType(null, null);
        while (dataSeries.length < masterData.length) {
            boolean[] newDataSeries = new boolean[masterData.length];
            int i = 0;
            while (i < dataSeries.length) {
                newDataSeries[i] = dataSeries[i];
                i++;
            }
            while (i < newDataSeries.length) {
                newDataSeries[i] = true;
                i++;
            }
            dataSeries = newDataSeries;
            newDataSeries = null;
        }
        for (int i = 0; i < masterData.length; i++) {
            cData[i] = new ArrayList();
            if (dataSeries[i]) {
                for (int j = 0; j < masterData[i].length; j++) {
//                    if(masterData[i][j].getSNR() > SNRlimit && masterData[i][j].getDM() > DMmin && Arrays.binarySearch(excludeBeams,masterData[i][j].getBeam().getName(),String.CASE_INSENSITIVE_ORDER) <0)
//                        cData[i].add(masterData[i][j]);
                    boolean add = true;
                    for (PlotType.axisType axisType : minVals.keySet()) {
                        if (pt.getVal(masterData[i][j], axisType) < minVals.get(axisType)) {
                            add = false;
                            break;
                        }
                    }
                    for (PlotType.axisType axisType : maxVals.keySet()) {
                        if (pt.getVal(masterData[i][j], axisType) > maxVals.get(axisType)) {
                            add = false;
                            break;
                        }
                    }
                    if (add && Arrays.binarySearch(excludeBeams, masterData[i][j].getBeam().getName(), String.CASE_INSENSITIVE_ORDER) < 0) {
                        cData[i].add(masterData[i][j]);
                    }


                }
            }
        }
        Cand[][] curData = new Cand[masterData.length][];
        for (int i = 0; i < masterData.length; i++) {
            curData[i] = new Cand[cData[i].size()];
            Iterator itr = cData[i].iterator();
            int j = 0;
            while (itr.hasNext()) {
                curData[i][j] = (Cand) itr.next();
                j++;
            }

        }
        return curData;
    }
//    public Cand[][] refine(Cand[][] masterData, double SNRlimit, double DMmin,boolean[] dataSeries,String[] excludeBeams){
//
//
//
//
////        ArrayList[] cData = new ArrayList[masterData.length];
////        Arrays.sort(excludeBeams,String.CASE_INSENSITIVE_ORDER);
////        while(dataSeries.length < masterData.length){
////            boolean[] newDataSeries = new boolean[masterData.length];
////            int i=0;
////            while(i<dataSeries.length){
////                newDataSeries[i] = dataSeries[i];
////                i++;
////            }
////            while(i<newDataSeries.length){
////                newDataSeries[i] = true;
////                i++;
////            }
////            dataSeries = newDataSeries;
////            newDataSeries = null;
////        }
////        for(int i =0;i<masterData.length;i++){
////            cData[i] = new ArrayList();
////            if(dataSeries[i]){
////                for(int j = 0;j<masterData[i].length;j++){
////                    if(masterData[i][j].getSNR() > SNRlimit && masterData[i][j].getDM() > DMmin && Arrays.binarySearch(excludeBeams,masterData[i][j].getBeam().getName(),String.CASE_INSENSITIVE_ORDER) <0)
////                        cData[i].add(masterData[i][j]);
////                }
////            }
////        }
////        Cand[][] curData = new Cand[masterData.length][];
////        for(int i = 0;i<masterData.length;i++){
////            curData[i] = new Cand[cData[i].size()];
////            Iterator itr = cData[i].iterator();
////            int j = 0;
////            while(itr.hasNext()){
////                curData[i][j] = (Cand)itr.next();
////                j++;
////            }
////
////        }
////        return curData;
//    }
    public void findHarmonics(final Cand[][] masterData, final double period, final String name, final Coordinate coord, final int candClass) {
        findHarmonics(masterData, period, name, coord, candClass, new String[0]);
    }

    public void findHarmonics(final Cand[][] masterData, final double period, final String name, final Coordinate coord, final int candClass, final String[] history) {

        final boolean ignoreCoordCheck = coord == null;

        final int intHarmLimit = 16;
        double gl = 0, gb = 0;

        if (!ignoreCoordCheck) {
            gl = coord.getGl();
            gb = coord.getGb();
        }
        //System.out.println(period);
        Beam beam;
//        CoordinateDistanceComparitor comp = new CoordinateDistanceComparitor();
        CoordinateDistanceComparitorGalactic comp = new CoordinateDistanceComparitorGalactic(0, 0);
        for (int i = 0; i < masterData.length; i++) {
            for (int j = 0; j < masterData[i].length; j++) {
                top:
                {
                    beam = masterData[i][j].getBeam();


//                    System.out.println("NAME: "+name);
//                    System.out.println("PCND: "+masterData[i][j].getPeriod());
//                    System.out.println("PPSR: "+period);
//                    System.out.println("PRAT: "+Math.abs(masterData[i][j].getPeriod() - period));
//                    System.out.println("DIST: "+comp.difference(beam.getCoord(),coord));
//                    System.out.println("ETA : "+getEta()*period);
//                    System.out.println("DMAX: "+getDistmax());

                    if (ignoreCoordCheck | candClass == 4 || candClass == 5 || comp.difference(gl, gb, beam.getCoord().getGl(), beam.getCoord().getGb()) < getDistmax()) {

                        if (Math.abs(masterData[i][j].getPeriod() - period) < getEta() * period) {

                            if (masterData[i][j].getNPulses() == 1) {



                                masterData[i][j].addDetection(new Detection(name, "Fundemental", candClass, HarmonicType.Principal, period));

                                if (dataLibrary instanceof WebDataLibrary) {
                                    WebDataLibrary dl = (WebDataLibrary) dataLibrary;
                                    dl.webUpdate(masterData[i][j]);

                                }
                                continue;
                            }
                        }
                        for (int intFactor = 2; intFactor < intHarmLimit; intFactor++) {
                            if (Math.abs(masterData[i][j].getPeriod() - period * intFactor) < getEta() * period * intFactor) {
                                if ((masterData[i][j].getNPulses() <= 0) || (masterData[i][j].getNPulses() == intFactor)) {

                                    masterData[i][j].addDetection(new Detection(name, intFactor + "th Harmonic", candClass, HarmonicType.Integer, period));

                                    break top;
                                }
                            }
                        }


//                        for (int bottomfactor = 1; bottomfactor <= fracHarmLimit; bottomfactor++) {
//                            for (int topfactor = 1; topfactor < fracHarmLimit; topfactor++) {
//                                factor = ((double) topfactor) / ((double) bottomfactor);
//                                //System.out.println(topfactor +"/"+bottomfactor+" "+factor+" "+ Math.abs(masterData[i][j].getPeriod()*factor - period));
//                                if (Math.abs(masterData[i][j].getPeriod() - period * factor) < getEta() * period * factor) {
//                                    if ((masterData[i][j].getNPulses() <= 0) || (masterData[i][j].getNPulses() == topfactor)) {
//
//                                        if (topfactor == 1) {
//                                            masterData[i][j].addDetection(new Detection(name, topfactor + "/" + bottomfactor + "th Harmonic", candClass, HarmonicType.SimpleNonInteger, period));
//                                        } else {
//                                            masterData[i][j].addDetection(new Detection(name, topfactor + "/" + bottomfactor + "th Harmonic", candClass, HarmonicType.ComplexNonInteger, period));
//                                        }
//                                        break top;
//                                    }
//                                }
//                            }
//                        }
                        for (double[] arr : fracHarms) {
                            if (Math.abs(masterData[i][j].getPeriod() - period * arr[0]) < getEta() * period * arr[0]) {

                                final int topfactor = (int) arr[1];
                                final int bottomfactor = (int) arr[2];
                                if ((masterData[i][j].getNPulses() <= 0) || (masterData[i][j].getNPulses() == topfactor)) {
                                    if (topfactor == 1) {
                                        masterData[i][j].addDetection(new Detection(name, topfactor + "/" + bottomfactor + "th Harmonic", candClass, HarmonicType.SimpleNonInteger, period));
                                    } else {
                                        masterData[i][j].addDetection(new Detection(name, topfactor + "/" + bottomfactor + "th Harmonic", candClass, HarmonicType.ComplexNonInteger, period));
                                    }
                                    break top;
                                }
                            }
                        }

                    }
                }
            }
        }

    }

    public double getEta() {
        return dataLibrary.getOptions().getEta();
    }

    public double getDistmax() {
        return dataLibrary.getOptions().getDistmax();
    }

    public DataLibrary getDataLibrary() {
        return dataLibrary;
    }
}


