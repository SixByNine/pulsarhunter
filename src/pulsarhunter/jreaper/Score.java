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
 * Score.java
 *
 * Created on 02 July 2005, 17:40
 */

package pulsarhunter.jreaper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;

/**
 *
 * @author mkeith
 */
public class Score{
    
    public enum ScoreType {
        DMZ("DM Zero Difference    "),
        DMC("DM Curve Shape        "),
        WID("Width                 "),
        PRS("Profile Shape         "),
        FRQ("Frequency Variance    "),
        PHV("Frac of Profile > 25% "),
        SPC("Sub Ints(Period Curve)"),
        SIH("Sub ints (Hough)      "),
        ACC("Acceleration Curve    "),
        NDM("New DM Curve Score    ");
        
        private String description;
        ScoreType(String description){
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private Cand cand;
    Hashtable<ScoreType,Float> scores = new Hashtable<ScoreType,Float>();
    private double score = -1.0;
    
    public Score(){
        
    }
    
    public Score(float score, String scoreV,String scoreLabels){
        this.setScore(score);
        this.decode_new_1(scoreV,scoreLabels);
    }
    
    public void decode_new_1(String scoreV,String scoreLabels){
        this.scores.clear();
        String[] scoreElems = scoreV.trim().split("\\s+");
        String[] labelElems = scoreLabels.trim().split("\\s+");
        
        if(scoreElems.length != labelElems.length){
            System.err.println("Cannot read score, each element must be labeled");
        }
        
        for(int i = 0; i < scoreElems.length; i++){
            ScoreType key;
            try {
                key = ScoreType.valueOf(labelElems[i]);
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown score type "+labelElems[i]+" ignored");
                continue;
            }
            float v = extractNewScore(scoreElems[i]);
            this.scores.put(key,v);
        }
        
    }
    
    public float extractNewScore(String sc){
        if(sc.equals("XXX")) return -1;
        else return (float)Integer.parseInt(sc,16)/2000.0f;
    }
    
    public void decode_peck_old(String scoreV){
        this.scores.clear();
        char[] chars = scoreV.toCharArray();
        if(chars.length == 9){
            this.scores.put(ScoreType.DMZ,this.fromChar(chars[0])/10.0f);
            this.scores.put(ScoreType.DMC,this.fromChar(chars[1])/10.0f);
            this.scores.put(ScoreType.WID,this.fromChar(chars[2])/10.0f);
            this.scores.put(ScoreType.FRQ,this.fromChar(chars[3])/10.0f);
            this.scores.put(ScoreType.FRQ,this.fromChar(chars[4])/10.0f);
            this.scores.put(ScoreType.PRS,this.fromChar(chars[5])/10.0f);
            this.scores.put(ScoreType.PHV,this.fromChar(chars[6])/10.0f);
            this.scores.put(ScoreType.SPC,this.fromChar(chars[7])/10.0f);
        }
        
        if(chars.length == 18){
            this.scores.put(ScoreType.DMZ,this.fromChars(new char[]{chars[0],chars[1]})/100.0f);
            this.scores.put(ScoreType.DMC,this.fromChars(new char[]{chars[2],chars[3]})/100.0f);
            this.scores.put(ScoreType.WID,this.fromChars(new char[]{chars[4],chars[5]})/100.0f);
            this.scores.put(ScoreType.FRQ,this.fromChars(new char[]{chars[6],chars[7]})/100.0f);
            this.scores.put(ScoreType.PRS,this.fromChars(new char[]{chars[8],chars[9]})/100.0f);
            this.scores.put(ScoreType.PHV,this.fromChars(new char[]{chars[10],chars[11]})/100.0f);
            this.scores.put(ScoreType.SPC,this.fromChars(new char[]{chars[12],chars[13]})/100.0f);
        }
        
        
    }
    
    public void recalcScore(){
        this.score = -1.0;
    }
    
    public double getScore(){
        if(score < 0){
            double num = 0;
            double compScore = 0;
            Hashtable<ScoreType, Double> scoreFactorTable;
            try {
                scoreFactorTable = this.cand.getCandList().getDataLibrary().getOptions().getScoreFactors();
            } catch (NullPointerException e) {
                scoreFactorTable = new Hashtable<ScoreType, Double> ();
            }
            for(ScoreType type : this.scores.keySet()){
                double scElem = this.scores.get(type);
                if(scElem < 0)continue;
                Double object_factor = scoreFactorTable.get(type);
                double scFactor = 1.0;
                if(object_factor != null)scFactor = object_factor.doubleValue();
                
                num+= scFactor;
                compScore += scElem*scFactor;
                
            }
            
            score = compScore /= (double)num;
            
        }
        return score;
    }
    public String getBreakdown(){
        getScore();
        StringWriter buf = new StringWriter();
        PrintWriter out = new PrintWriter(buf);
        Hashtable<ScoreType, Double> scoreFactorTable;
        try {
            scoreFactorTable = this.cand.getCandList().getDataLibrary().getOptions().getScoreFactors();
        } catch (NullPointerException e) {
            scoreFactorTable = new Hashtable<ScoreType, Double> ();
        }
        for(ScoreType type : this.scores.keySet()){
            
            double scElem = this.scores.get(type);
            if(scElem < 0)continue;
            Double object_factor = scoreFactorTable.get(type);
            double scFactor = 1.0;
            if(object_factor != null)scFactor = object_factor.doubleValue();
            out.printf("%s: %3.2f x %3.2f = %3.2f\n",type.getDescription(),scElem,scFactor,scElem*scFactor);
            
            
        }
        out.printf("%s:             = %3.2f\n","Total                 ",this.getScore());
        
        return buf.toString();
        
    }
    
    
    
    
    public String[] getMachineBreakdown(){
        StringWriter sw = new StringWriter(12);
        PrintWriter out = new PrintWriter(sw);
        
        StringWriter swT = new StringWriter(12);
        PrintWriter outT = new PrintWriter(swT);
        
        for(ScoreType type : this.scores.keySet()){
            double scElem = this.scores.get(type);
            if(!Double.isNaN(this.getScoreElement(type))){
                out.printf("%03X ",(int)(this.getScoreElement(type)*2000.0));
                outT.print(type+" ");
            }
            
        }
        
        return new String[]{sw.toString(),swT.toString()};
    }
    
    
    private char[] toChars(int val){
        //if there is no data return N s
        if(val < 0)return new char[]{'N','N'};
        int tens = (int)(val/10);
        int units = val - 10*tens;
        return new char[]{toChar(tens),toChar(units)};
        
    }
    
    private char toChar(int val){
        char result = Integer.toString(val).charAt(0);
        if(val > 9)result = 'X';
        if(val < 0)result = 'N';
        return result;
    }
    private int fromChar(char val){
        int result = -1;
        if(val == 'X'){
            result = 10;
            
        } else if(val == 'N'){
            result = -1;
        } else {
            result = Character.digit(val,10);
        }
        return result;
    }
    
    
    private int fromChars(char[] val){
        // No data -> return -1
        if(val[0] == 'N')return -1;
        
        int mult = (int)(Math.pow(10,val.length-1));
        int result = 0;
        for(char c : val){
            result +=  fromChar(c)*mult;
            mult/=10;
        }
        
        return result;
    }
    
    
    public void updateScore(){
        this.setScore(-1);
        this.getScore();
    }
    
    public double getScoreElement(ScoreType type){
        if(this.scores.get(type)==null)return Double.NaN;
        else {
            double d =  this.scores.get(type);
            if(d < 0)return Double.NaN;
            else return d;
        }
    }
    
    
    public void setScoreElement(ScoreType type,double val){
        this.scores.remove(type);
        this.scores.put(type,(float)val);
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public Cand getCand() {
        return cand;
    }
    
    public void setCand(Cand cand) {
        this.cand = cand;
    }
    
    
}

