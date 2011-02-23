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
 * PeriodSearchResultGroup.java
 *
 * Created on 15 January 2007, 13:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mkeith
 */
public class PeriodSearchResultGroup<R extends BasicSearchResult> {

    public enum SortField {

        SPECTRAL_SNR, RECONSTRUCTED_SNR, FOLD_SNR, PERIOD, DM, PDOT, PDDOT, DM_PDOT_PDDOT
    }
    private SortField masterSortField;
    private SortField sortedBy = null;
    private ArrayList<R> results = new ArrayList<R>();
    private R best = null;
    private double bestRecon = 0.0;
    private ArrayList<PeriodSearchResultGroup<R>> harmonics = new ArrayList<PeriodSearchResultGroup<R>>();

    /** Creates a new instance of PeriodSearchResultGroup */
    public PeriodSearchResultGroup(SortField masterSortField) {
        //this.setMasterSortField(masterSortField);

        //Always sort by Spectral!

        this.setMasterSortField(SortField.SPECTRAL_SNR);
    }

    public synchronized void addSearchResult(R searchResult) {


        sortedBy = null;
        // list is no longer sorted.

        SearchResultComparator comp = new SearchResultComparator<R>(masterSortField);
        if (searchResult.getReconstructedSignalToNoise() > this.bestRecon) {
            this.bestRecon = searchResult.getReconstructedSignalToNoise();
        }
        if (best == null || comp.compare(best, searchResult) < 0) {
            best = searchResult;
        }

        results.add(searchResult);
    }

    public synchronized R getBestSuspect() {
        /* if(best == null){
        this.sort(this.getMasterSortField());
        this.best = results.get(0);
        }*/



        return best;
    }

    public void test() {
        /* if(best == null){
        this.sort(this.getMasterSortField());
        this.best = results.get(0);
        }*/

        System.out.println("Best A: " + best.getSpectralSignalToNoise() + "\t" + best.getReconstructedSignalToNoise());

        this.sort(SortField.SPECTRAL_SNR);
        System.out.println("Best S: " + results.get(0).getSpectralSignalToNoise() + "\t" + results.get(0).getReconstructedSignalToNoise());


        this.sort(SortField.RECONSTRUCTED_SNR);
        System.out.println("Best R: " + results.get(0).getSpectralSignalToNoise() + "\t" + results.get(0).getReconstructedSignalToNoise());



    }

    public synchronized double getBestPeriod() {
        return this.getBestSuspect().getPeriod();
    }

    public synchronized double getBestDM() {
        return this.getBestSuspect().getDM();
    }

    public synchronized double getBestPdot() {
        return this.getBestSuspect().getAccn();
    }

    public synchronized double getBestPddot() {
        return this.getBestSuspect().getJerk();
    }

    public synchronized double getSnrInDmRange(double dm, double range) {
        double max = 0;
        for (R result : this.results) {
            if (Math.abs(result.getDM() - dm) < range && result.getSpectralSignalToNoise() > max) {
                max = result.getSpectralSignalToNoise();

            }
        }
        return max;
    }

    public synchronized void sort(SortField sortField) {
        if (sortField == this.sortedBy) {
            return;
        }
        SearchResultComparator comp = new SearchResultComparator<R>(sortField);

        Collections.sort(results, Collections.reverseOrder(comp));
        this.sortedBy = sortField;


    }

    public synchronized void sortRev(SortField sortField) {
        if (sortField == this.sortedBy) {
            return;
        }
        SearchResultComparator comp = new SearchResultComparator<R>(sortField);

        Collections.sort(results, (comp));
        this.sortedBy = sortField;


    }

    public synchronized SortField getMasterSortField() {
        return masterSortField;
    }

    public synchronized void setMasterSortField(SortField masterSortField) {
        this.masterSortField = masterSortField;
    }

    public synchronized List<PeriodSearchResultGroup<R>> getHarmonics() {
        return (List<PeriodSearchResultGroup<R>>) (this.harmonics.clone());
    }

    public synchronized void addHarmonic(PeriodSearchResultGroup<R> harmonic) {
        this.harmonics.add(harmonic);

    }

    public synchronized List<R> getDmPdotPddotCube() {
        this.sortRev(SortField.SPECTRAL_SNR);
        this.sort(SortField.DM_PDOT_PDDOT);
        return (List<R>) (this.results.clone());
    }

    public double getBestRecon() {
        return this.bestRecon;
    }
}
