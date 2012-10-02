/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pulsarhunter.datatypes;

import coordlib.Telescope;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import pulsarhunter.Data.Header;
import pulsarhunter.PulsarHunter;

/**
 *
 * @author kei041
 */
public class AsciiPrdFile extends BasicSearchResultData {

    ArrayList<Double> dmList = new ArrayList<Double>();
    ArrayList<Double> acList = new ArrayList<Double>();
    ArrayList<Double> adList = new ArrayList<Double>();
    Header header = new Header();

    public AsciiPrdFile(File file) throws FileNotFoundException, IOException {
        this.header.setTelescope(Telescope.PARKES);
        this.read(file);
    }

    public String getDataType() {
        return "ASCIIPRDFILE";
    }

    public void flush() throws IOException {
    }

    private void read(File file) throws FileNotFoundException, IOException {
        PulsarHunter.out.print("Reading .ascprd file... ");
        PulsarHunter.out.flush();

        this.adList.add(0.0);
        BufferedReader reader = new BufferedReader(new FileReader(file));


        String line = reader.readLine();
        while (line != null) {
            StringTokenizer tok = new StringTokenizer(line);
            if (tok.countTokens() == 5) {
                double sn = Double.parseDouble(tok.nextToken());
                double p_ms = Double.parseDouble(tok.nextToken());
                double dm = Double.parseDouble(tok.nextToken());
                double ac = Double.parseDouble(tok.nextToken());
                int hfold = Integer.parseInt(tok.nextToken());
                if (!this.dmList.contains(dm)){
                    this.dmList.add(dm);
                }
                if (!this.acList.contains(ac)){
                    this.acList.add(ac);
                }
                BasicSearchResult bsr = new BasicSearchResult(p_ms / 1000.0, dm);
                bsr.setAccn(ac);
                bsr.setJerk(0);
                bsr.setHarmfold(hfold);
                bsr.setSpectralSignalToNoise(sn);
                this.addSearchResult(bsr);
            }
            line = reader.readLine();
        }

        reader.close();
    }

    public Header getHeader() {
        return this.header;
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
}
