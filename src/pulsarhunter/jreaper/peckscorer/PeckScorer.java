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
 * PeckScorer.java
 *
 * Created on 25 August 2005, 14:04
 */

package pulsarhunter.jreaper.peckscorer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import pulsarhunter.Convert;
import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.CandScorer;
import pulsarhunter.jreaper.Score;
import pulsarhunter.jreaper.Score.ScoreType;


/**
 *
 * @author mkeith
 */
public class PeckScorer implements CandScorer{
    
    // Hough variables
    HoughTransform houghTransform =  new HoughTransform();
    double[] a1s = new double[101];
    double[] a2s = new double[101];
    /** Creates a new instance of PeckScorer */
    public PeckScorer() {
        
        //double[] a1s = new double[]{-2.0,-1.75,-1.5,-1.25,-1.0,-0.75,-0.5,-0.25,0,0.25,0.5,0.75,1,1.25,1.5,1.75,2};
        
        
        // set up hough stuff
        double a1min = -3;
        double a1max = 3;
        double a1Step = (a1max - a1min)/a1s.length;
        double v = a1min;
        for(int i = 0; i < a1s.length; i++){
            a1s[i] = v;
            v += a1Step;
        }
        
        
        double a2min = -3;
        double a2max = 3;
        double a2Step = (a2max - a2min)/a2s.length;
        v = a2min;
        for(int i = 0; i < a2s.length; i++){
            a2s[i] = v;
            v += a2Step;
        }
        
    }
    
    public void rescore(Cand[] cands){
        if(cands==null) return;
        for(Cand c : cands){
            Cand clone = c.deepClone();
            Score s = this.score(clone);
            c.setScore(s);
        }
    }
    
    
    public Score score(Cand cand) {
        Score score = new Score();
        if(cand.getCandidateFile() instanceof PeckScoreableCandFile){
            
            PeckScoreableCandFile candFile = (PeckScoreableCandFile)cand.getCandidateFile();
            
            
            
            // --- The scores:
            
            float dmzerodiff = -1.0f;       // Done // The difference between the dm at zero and the candidate DM.
            float dmcurveshape = -1.0f;     // Done // A measure of the DM curve shape
            float width = -1.0f;            // ???? // The pulse width
            float freqVariance = -1.0f;     // Done // The variance between frequency chanels in the candidate
            float profileShape = -1.0f;     // NotD // A measure of the shape of the profile
            float profileHighValue = -1.0f; // Done //
            float subintLinear = -1.0f;     // NotD // Measures the liniarty of the subints. Only good for solitary pulsars.
            float subintGeneral = -1.0f;    // NotD // Measures the overall consistancy of the subints. Also checks that most the data is negitive.
            float additionalScore = -1.0f;
            float newdmcurveshape = -1.0f;
            // ---
            
            
            
            // 1: Do dm curve scoring if we have a dm curve
            
            
            if(candFile.hasDMCurve()){
                
                float[][] dmcurv = candFile.getDMCurve();
                if(dmcurv != null && dmcurv[0].length > 6){
                    
                    float maxdm = 0;
                    int mdmposn = 0;
                    
                    for(int i = 0 ; i < dmcurv[1].length; i++){
                        float f = dmcurv[1][i];
                        if(f > maxdm){
                            maxdm = f;              // find the DM peak (peak value)
                            mdmposn = i;            // find the DM peak (location)
                        }
                    }
                    
                    double bestDmValue = dmcurv[0][mdmposn];

                    
                    // We are going to average the first 5 values to create a 'zero dm' value
                    
                    int count = 0;
                    float tot = 0;
                    
                    for(int i = 0; i< 5 ; i++){
                        if(dmcurv[1][i]>0){
                            count ++;
                            tot += dmcurv[1][i];
                        }
                    }
                    
                    float zeroval = 0;
                    
                    if(count != 0){
                        zeroval = tot/count;    // average over the 'zero dm values'
                    }
                    
                    // ---
                    dmzerodiff = (maxdm - zeroval);     // subtract the peak value from  the zero dm value.
                    
                    
                    width = candFile.getWidth();
                    
                    float freq = cand.getFrequency();
                    
                    float band = cand.getBandwidth();
                    
                    
                    dmcurveshape = this.getDMCurveScore(dmcurv[0], dmcurv[1], bestDmValue, cand.getPeriod()/1000.0, width ,freq,band);
                    newdmcurveshape = this.getNewDMCurveScore(dmcurv[0], dmcurv[1], bestDmValue, cand.getPeriod()/1000.0, width ,freq,band,cand);
                    dmzerodiff = this.getDMCurveScore(dmcurv[0], dmcurv[1],  0, cand.getPeriod()/1000.0, width, freq, band);
//                    if(dmzerodiff > dmcurveshape){
//                        cand.setDM(0);
//                    }
                    dmzerodiff = 1.0f - dmzerodiff*dmzerodiff;
                    
                    
                    // ---
                    
                    
                }
            }   // END of DMcurve Measurements
            
            
            // 2: Analyse the profile...
            if(candFile.hasProfile()){
                
                float[] pArray =candFile.getProfile();
                if(pArray != null){
                    float max = Float.MIN_VALUE;
                    float min = Float.MAX_VALUE;
                    int peakLocation = -1;
                    
                    for(int i = 0; i < pArray.length; i++ ){
                        if(pArray[i] > max){
                            max = pArray[i];                    // update the max value
                            peakLocation = i;                   // find the bin number of the peak value...
                        }
                        if(pArray[i] < min) min = pArray[i];    // update the min value
                    }
                    
                    int highcount = 0;
                    int lowcount = 0;
                    int medcount = 0;
                    float scaledVal = 0;
                    for(int i = 0; i < pArray.length; i++ ){
                        scaledVal = (pArray[i] - min) / (max - min);  // Scale to 1
                        if(scaledVal > 0.60) highcount++;                   // count how many are in the upper quartile
                        else if(scaledVal < 0.25) lowcount++;               // lower quartile
                        else medcount++;                                    // the rest...
                    }
                    //Main.getInstance().log("scaledVal "+scaledVal + "Highcount:" + highcount + " LowCount: "+lowcount + " MedCount "+ medcount);
                    // ---
                    profileHighValue = 1.0f - ((float)highcount / (float)(pArray.length));
                    
                    // ---
                }
            }
                /*
                 * Now look for subints stuff
                 */
            
            /**
             * Prefer PeriodCurve over Hough!
             *
             */
            if(candFile.hasPeriodCurve()){
                double[][] curve = candFile.getPeriodCurve();
                double[] xaxis = curve[0];
                double[] dmcurv = curve[1];
                
                double sc = 0.0;
                int tot = 0;
                
                double max = 0.0f;
                int maxPos = 0;
                for(int i = 0;i<dmcurv.length;i++){
                    if(dmcurv[i]> max){
                        maxPos = i;
                        max = dmcurv[i];
                    }
                }
                
                double[] model = Convert.generatePeriodCurve(xaxis,xaxis[maxPos], candFile.getWidth(),candFile.getTobs());
                
                max = 0.0f;
                double min = Double.MAX_VALUE;
                for(int i = 0;i<dmcurv.length;i++){
                    if(dmcurv[i] > max) max = dmcurv[i];
                    if(dmcurv[i] < min) min = dmcurv[i];
                }
                double dmcurvValue;
                for(int i = 0;i<dmcurv.length && i<model.length;i++){
                    dmcurvValue = (dmcurv[i]-min)/(max-min);
//                    System.out.println(xaxis[i]+" "+dmcurvValue+" "+model[i]);
                    if(model[i] > 0.4){
                        sc += Math.pow(dmcurvValue-model[i],2);
                        tot ++;
                    }
                }
                
//                System.out.println("Raw period score:"+sc+" with "+tot);
                if(sc < 0.01) sc = 0.01;
                sc =  Math.sqrt(sc / tot);
//                System.out.println("Sqrt:"+sc);
                if(sc < 0.0) sc = 0.0;
                if(sc > 1.0) sc = 1.0;
                subintLinear =  (float) (1.0f - sc);
                
                
                // see if we can get a AccnCurve
                if(candFile.hasAccnCurve()){
                    
                }
                
            } else {
                
                // We might already have a  hough plane!
                if(candFile.hasHoughPlane()){
                    
                    double[][] houghplot = candFile.getHoughPlane();
                    
                    double dmax = 0;
                    // int maxX = -1;
                    //int maxY = -1;
                    //double sum = 0;
                    // double sumsquare = 0;
                    for(int i = 0; i < houghplot.length; i++){
                        for(int j = 0; j < houghplot[i].length; j++){
                            //if(output[i][j] < 8)continue;
                            // sum += output[i][j];
                            //sumsquare += output[i][j]*output[i][j];
                            if(houghplot[i][j] > dmax){
                                dmax = houghplot[i][j];
                                //maxX = i;
                                // maxY = j;
                            }
                        }
                    }
                    
                    int count = 0;
                    for(int j = 0; j < houghplot[0].length; j++){
                        
                        int val = 0;
                        for(int i = 0; i < houghplot.length; i++){
                            if(houghplot[i][j] > dmax*0.7) val +=1;
                        }
                        if(val > 0)count +=val;
                    }
                    
                    //System.out.println(count);
                    subintGeneral = (float)(20 - count)/17.0f;
                    
                    if(subintGeneral > 1)subintGeneral = 1;
                    if(subintGeneral < 0)subintGeneral = 0;
                    
                    
                    
                    
                    
                    
                    
                } else if(candFile.hasSubints()){ // Else see if we can make it
                    float[][] subints = candFile.getSubints();
                    // Need to sort this
                    
                    float max = Float.MIN_VALUE;
                    float min = Float.MAX_VALUE;
                    for(int i=0;i<subints.length;i++){
                        for(int j=0;j<subints[i].length;j++){
                            if(subints[i][j]<min)min = subints[i][j];
                            if(subints[i][j]>max)max = subints[i][j];
                        }
                    }
                    
                /*
                 * Hough Transform!
                 */
                    
                    
                    
                    int cRes = 101;
                    double threshold = 0.7;
                    
                    double[][] xys = new double[subints.length][subints[0].length];
                    for(int i=0;i<subints.length;i++){
                        for(int j=0;j<subints[i].length;j++){
                            xys[i][j] = (subints[i][j] - min) / (max - min);
                        }
                    }
                    
                    
                    
                    
                    
                    double[][] output = houghTransform.linearTransform(xys, a1s, cRes, threshold);
                    
                    
                    
                    
                    double dmax = 0;
                    // int maxX = -1;
                    //int maxY = -1;
                    //double sum = 0;
                    // double sumsquare = 0;
                    for(int i = 0; i < output.length; i++){
                        for(int j = 0; j < output[i].length; j++){
                            //if(output[i][j] < 8)continue;
                            // sum += output[i][j];
                            //sumsquare += output[i][j]*output[i][j];
                            if(output[i][j] > dmax){
                                dmax = output[i][j];
                                //maxX = i;
                                // maxY = j;
                            }
                        }
                    }
                    
                    int count = 0;
                    for(int j = 0; j < output[0].length; j++){
                        
                        int val = 0;
                        for(int i = 0; i < output.length; i++){
                            if(output[i][j] > dmax*0.7) val +=1;
                        }
                        if(val > 0)count +=val;
                    }
                    
                    subintGeneral = (float)(20 - count)/17.0f;
                    
                    if(subintGeneral > 1)subintGeneral = 1;
                    if(subintGeneral < 0)subintGeneral = 0;
                    
                    
                    
                    
                }
            }
            
            
            
            
            
            
            
            
            
            
            
            if(candFile.hasFrequencyChannels()){
                float[][] subbands = candFile.getFrequencyChanels();
                
                
                
                /*
                if(subbands != null){
                    int np = 5;
                    double[][] peakposns = this.getSubintPeaks(subbands,np);
                    //double[][] noiseposns = this.getRandomPeakData(subints.length, subints[0].length,np);
                    double[] x1 = new double[subbands.length*np];
                    double[] y1 = new double[subbands.length*np];
                    //double[] x2 = new double[subints.length*np];
                    //double[] y2 = new double[subints.length*np];
                 
                    for(int i = 0;i<subbands.length; i++){
                        for(int j = 0;j<np; j++){
                            x1[i*np + j] = i + Math.random();
                            y1[i*np + j] = peakposns[i][j];
                            //  x2[i*np + j] = i;
                            //  y2[i*np + j] = noiseposns[i][j];
                        }
                    }
                    StatisticalResult s1 = new KSTest().ks2d1s(x1, y1, new KS2DFunction(subbands.length, subbands[0].length));
                    //StatisticalResult s2 = new KSTest().ks2d2s(x1, y1,x2,y2);
                    //StatisticalResult s3 = new KSTest().ks2d1s(x2, y2, new KS2DFunction(subints.length, subints[0].length));
                    freqVariance = (float)(1 - s1.prob);
                    if(freqVariance > 1)freqVariance = 1;
                    if(freqVariance < 0)freqVariance = 0;
                 
                }
                 */
            }
            
            
            
            // Is there any more frequency Chanel stuff???
            
            
            
            score.setScoreElement(ScoreType.PHV,profileHighValue);
            score.setScoreElement(ScoreType.DMC,dmcurveshape);
            score.setScoreElement(ScoreType.SIH,subintGeneral);
            score.setScoreElement(ScoreType.SPC,subintLinear);
            score.setScoreElement(ScoreType.FRQ,freqVariance);
            score.setScoreElement(ScoreType.WID,width);
            score.setScoreElement(ScoreType.DMZ,dmzerodiff);
            score.setScoreElement(ScoreType.NDM,newdmcurveshape);
            
            
            
            
            
            
            
            
        } else {
            System.err.println("Cannot Score this candidate");
        }
        
        return score;
    }
    
    
// ----------------- Scoring Funcitons ------------------
    
    
    
    
    
    
    
    
    
    
    private final double kdm = 8.3e6;
    public float getDMCurveScore( float[] dmidex,float[] dmcurv,double centerDM,double period,double width,float frequency,float bandwidth){
        // Generate theoretical DM Curve
//        float[] model = new float[dmidex.length];  // create result array
//        float max = 0.0f;
//        for(int i = 0;i<model.length;i++){
//            // Generate time delay
//            double weff = Math.sqrt(Math.pow(width,2) + Math.pow( (kdm * Math.abs(centerDM-dmidex[i]) * (bandwidth/Math.pow(frequency,3)) ) ,2) );
//
//
//            if(weff > period) model[i] = 0;    // if the time delay is longer than period, we will not see the pulse
//            else model[i] = (float) Math.sqrt((period - weff) / weff);  // otherwise the snr is reduced as shown.
//
//
//            if(model[i] > max)max = model[i];  // store peak value so far, so we can re-range the results from 0 to 1.
//
//        }
//        for(int i = 0;i<model.length;i++){
//            model[i] = model[i] / max;        // re-range results from 0 to 1.
//        }
        
        double[] xaxis = new double[dmidex.length];
        for(int i = 0; i < dmidex.length; i++){
            xaxis[i] = dmidex[i];
        }
        
        double[] model = Convert.generateDmCurve(xaxis,period,width,centerDM,bandwidth,frequency);
        
        
        // now compare with the real dm curve.
        double score = 0.0;
        int tot = 1;
        float max = 0.0f;
        float min = 0.0f;
        for(int i = 0;i<dmcurv.length;i++){
            if(dmcurv[i] > max) max = dmcurv[i];
            if(dmcurv[i] < min) min = dmcurv[i];
        }
        float dmcurvValue;
        for(int i = 0;i<dmcurv.length && i<model.length;i++){
            dmcurvValue = (dmcurv[i]-min)/(max-min);
//            System.out.println(xaxis[i]+"\t"+dmcurvValue+"\t"+model[i]);
            if(dmcurvValue > 0.2){
                score += Math.pow(dmcurvValue -model[i],2);
                tot ++;
            }
        }
//        System.out.println("Raw dm score:"+score);
        
        if(score < 0.01) score = 0.01;
        score =  Math.sqrt(score / tot);
        if(score < 0.0) score = 0.0;
        if(score > 1.0) score = 1.0;
        // We should test if we have enough data points to be statisticaly sound
        if(tot < 4 && score < 0.3)score = 0.3f;
        
        return (float) (1.0f - score);
    }
    
    public float getNewDMCurveScore( float[] dmidex,float[] dmcurv,double centerDM,double period,double width,float frequency,float bandwidth,Cand cand){
        
        double[] xaxis = new double[dmidex.length];
        for(int i = 0; i < dmidex.length; i++){
            xaxis[i] = dmidex[i];
        }
        int tot = 0;
        double sum = 0;
        
        float max = 0.0f;
        float min = 0.0f;
        for(int i = 0;i<dmcurv.length;i++){
            if(dmcurv[i] > max) max = dmcurv[i];
            if(dmcurv[i] < min) min = dmcurv[i];
            sum += dmcurv[i];
            tot++;
        }
        double mean = sum / tot;
        
        //float dmcurvValue;

        double dmlimit = max*0.8;
        
        double maxScore = Double.MAX_VALUE;
        double bestDm = 0;
        double bestWidth = 0;
        for(double widthV = width/1.5 ; widthV < width*1.5; widthV+=(width/5.0)){
  //          for(int v = 0; v < xaxis.length; v++){
    //            if(dmcurv[v] < dmlimit)continue;
                
    //            max = dmcurv[v];
                
                double dmV = centerDM;
                
                double[] model = Convert.generateDmCurve(xaxis,period,width,dmV,bandwidth,frequency);
                
                
                // now compare with the real dm curve.
                double score = 0.0;
                
                

                
                
                tot = 0;
                
                for(int i = 0;i<dmcurv.length && i<model.length;i++){
                    double dmcurvValue = (dmcurv[i]-min)/(max-min);

                    if(dmcurv[i] > mean){
                        score += Math.pow(dmcurvValue - model[i],2);
                        tot++;
                    }
                }

                score /= tot;
            //    System.out.println("DM: "+dmV+"\tWidth: "+widthV+"\t SCORE: "+score+"\t"+tot);
                
                //if(tot==0)score = 0;
                //else score = tot/score;
                
                //System.out.println(dmV+" "+score);
                
                //if(tot == 1) score /= 1.5;
                
                
                
                
                if(score < maxScore){
                    maxScore = score;
                    bestDm = dmV;
                    bestWidth = widthV;
                }
         //   }
        }
        
       // System.out.println("\nDM: "+bestDm+"\tWidth: "+bestWidth+"\t SCORE: "+maxScore+"");
        
        maxScore = (1.0 / maxScore)/10.0;
      // System.out.println("MaxScore: "+maxScore);
        
        //  System.out.println("DM: "+bestDm);
        if(maxScore > 1.0) maxScore = 1.0;
        
        
        return (float) (maxScore);
    }
    
    
    private float getFreqScore(float[][] freqBands){
        int numpoints = 3;
        int[][] maxValPosns = new int[freqBands.length][numpoints];
        float[][] maxVals = new float[freqBands.length][numpoints];
        
        for(int i = 0;i<freqBands.length;i++){              // Each band
            
            for(int j = 0;j < freqBands[i].length;j++){     // Each bin
                
                for(int n = 0; n < maxVals[i].length;n++){
                    if(freqBands[i][j] > maxVals[i][n]){
                        for(int k = maxVals[i].length-1-n;k > 0;k--){
                            maxVals[i][k] = maxVals[i][k-1];
                            maxValPosns[i][k] = maxValPosns[i][k-1];
                        }
                        maxVals[i][n] = freqBands[i][j];
                        maxValPosns[i][n] = j;
                        break;
                    }
                }
            }
        }
        
        // Now find the most likely Signal...
        
        
        
        
        
        return 0.0f;
        
    }
    
    
    
// -------------------------------------------------------
    
    
    
    
    
    
    public String getName() {
        return "PeckPlugin";
    }
    
    public String getDescription() {
        return "Peck Scorer";
    }
    
    private Method checkedDeclaredMethod(Class cClass,Type returnType,String name, Class... methodParams){
        try{
            Method m = cClass.getDeclaredMethod(name, methodParams);
            if(m.getReturnType().equals(returnType)){
                return m;
            } else return null;
        } catch(NoSuchMethodException e){
            return null;
        }
        
    }
    
    private Object checkedInvoke(Method method, Object obj, Object... params){
        try{
            return method.invoke(obj, params);
        } catch (IllegalAccessException e){
            e.printStackTrace();
            return null;
        } catch(InvocationTargetException e){
            e.printStackTrace();
            return null;
        }
    }
    
    
    public  double gausianNoise(double random){
        double oneoverroottwopi = 0.3989422804014326;
        return oneoverroottwopi*Math.exp(-random*random/2.0);
    }
    
    
    public  double[][] getSubintPeaks(float[][] data,int n){
        double[][] result = new double[data.length][];
        
        for(int i = 0; i<data.length;i++){
            double[] best = new double[n];
            Arrays.fill(best,-Float.MAX_VALUE);
            double[] bestYvals = new double[n];
            Arrays.fill(bestYvals,-1);
            for(int j = 0; j<data[i].length;j++){
                float val = data[i][j];
                for(int k = 0; k<best.length;k++){
                    
                    if(val > best[k]){
                        for(int m = best.length-1;m>k;m--){
                            best[m] = best[m-1];
                            bestYvals[m] = bestYvals[m-1];
                        }
                        best[k] = val;
                        bestYvals[k] = j + Math.random();
                        break;
                    }
                    
                }
                result[i] = bestYvals;
            }
        }
        return result;
    }
    
    
    private double[][] getRandomPeakData(int x,int y,int n){
        float[][] randData = new float[x][y];
        
        for(int i = 0;i<x; i++){
            for(int j = 0;j<y; j++){
                double rand = Math.random();
                randData[i][j] = (float)(gausianNoise(rand)*100);
            }
        }
        return getSubintPeaks(getRandData( x, y),n);
    }
    
    private float[][] getRandData(int x,int y){
        float[][] randData = new float[x][y];
        
        for(int i = 0;i<x; i++){
            for(int j = 0;j<y; j++){
                double rand = Math.random();
                randData[i][j] = (float)(gausianNoise(rand)*100);
            }
        }
        return randData;
    }
    
    
    
    
}
