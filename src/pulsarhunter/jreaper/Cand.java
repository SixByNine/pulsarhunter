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
 * Cand.java
 *
 * Created on 24 May 2005, 21:47
 */

package pulsarhunter.jreaper;

import coordlib.Beam;
import coordlib.Dec;
import coordlib.RA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import pulsarhunter.jreaper.HarmonicType;
import pulsarhunter.jreaper.pmsurv.PulsarCandFile;





/**
 *
 * @author mkeith
 */
public class Cand{
    static final long serialVersionUID = 1000L;
    
    private final static short VERSION = 1;
    
    private  ArrayList<Detection> detectionList = null;
    private float period=0;
    private float foldSNR=0;
    private float specSNR=0;
    private float reconSNR=0;
    private float DM=0;
    private CandidateFile phfile;
    private ArrayList<String> comments = new ArrayList<String>();
    private String lastComment = "";
    private boolean viewed = false;
    //private transient Cand harmonicof;
    private  HarmonicType harmonicType = HarmonicType.None;
    private float accel=0;
    private float jerk=0;
    private Score score;
    private double MJD=0;
    private boolean dud = false;
    
    private short nPulses = 0;
    
    private CandList candList;
;
    
    public Cand(){
        
    }
    
    /** Creates a new instance of Cand */
    public Cand(CandidateFile phfile,double period,float SNR,float DM,double accel,double jerk,Score score,double MJD) {
        this.period = (float)period;
        this.setFoldSNR(SNR);
        this.DM = DM;
        this.phfile = phfile;
        this.accel = (float)accel;
        this.jerk = (float)jerk;
        this.score = score;
        this.MJD = (float)MJD;
    }
    
    public void setScore(Score score){
        this.score = score;
        score.setCand(this);
    }
    
    public double getSNR(){
        return getFoldSNR();
    }
    
    public double getPeriod(){
        return period;
    }
    
    public double getDM(){
        return DM;
    }
    
    public String getUniqueIdentifier(){
        
        
        StringBuilder build = new StringBuilder();
        Formatter formater = new Formatter(build);
        
        
        
        formater.format("%010.7f",this.getPeriod());
        
        formater.format("%07.2f",this.getDM());
        
        formater.format("%07.2f",this.getSNR());
        build.append(this.getBeam().getName());
        

        String s = build.toString();
        if(s.length() > 50)s = s.substring(0,50);
        return s;
    }
    
    
    
    private String round(double d, double r){
        if(d==-1)return "N/A";
        else return Double.toString(((long)(d*r))/r);
    }
    
    public CandidateFile getCandidateFile(){
        return phfile;
    }
    
    public Beam getBeam(){
        return candList.getBeam();
    }
    
    public int getCandClass(){
        if(dud) return 4;
        if(detectionList == null) return -2;
        return detectionList.get(0).getCandClass();
        
    }
    public String getComment(){
       return lastComment;
    }
    
    public void addComment(String comment){
        this.comments.add(comment);
        this.lastComment = comment;
    }
    
    public void setViewed(boolean viewed){
        this.viewed = viewed;
    }
    
    public boolean beenViewed(){
        return viewed;
    }
    private String name = null;
    public String getName(){
        if(detectionList == null) return phfile.getUniqueIdentifier();
        else return detectionList.get(0).getName();
    }
    
    
    public String getCommentHistory(){
        StringBuffer buf = new StringBuffer();
        for(String s : this.comments){
            buf.append(s);
        }
        return buf.toString();
    }
    
    public boolean equals(Object o){
        if(o instanceof Cand){
            return ((Cand) o).getBeam() == this.getBeam() && ((Cand) o).getPeriod() == period && ((Cand) o).getSNR() == getFoldSNR();
        }
        return false;
    }
    
    public double getAccel(){
        return accel;
    }
    
    public double getJerk(){
        return jerk;
    }
    
    public void recalcScore(){
        this.score.recalcScore();
    }
    
    public double getScore(){
        if(score == null) return 0;
        else return score.getScore();
    }
    
    public Score getScoreObject(){
        return this.score;
    }
    
    public String getScoreBreakdown(){
        if(score == null) return "Candidate Not Scored";
        else return score.getBreakdown();
    }
    
    public double getMJD(){
        return MJD;
    }
    
    public RA getRA(){
        return this.getBeam().getCoord().getRA();
    }
    
    public Dec getDec(){
        return this.getBeam().getCoord().getDec();
    }
    
    
    public HarmonicType getHarmonicType() {
        if(detectionList == null) return HarmonicType.None;
        return detectionList.get(0).getHarmType();
    }
    
    
    
    public void addDetection(Detection detection) {
        if(detection.getCandClass()==4){
            dud = true;
            return;
        } else {
            if(detection.getCandClass()==5){
                dud = false;
                return;
            }
            
        }
        if(detectionList == null) detectionList = new ArrayList<Detection>();
       // if(!detectionList.contains(detection))
         detectionList.remove(detection);   
            detectionList.add(detection);

        Collections.sort(detectionList,new DetectionComparator(detection));
    }
    
    public java.util.List<Detection> getDetectionList() {
        if(detectionList==null)return null;
        else return (java.util.List<Detection>)detectionList.clone();
    }
    
    
    public CandList getCandList(){
        return candList;
    }
    
    public void clearDetectionList(){
        this.detectionList = null;
    }
    
    public void setCandList(CandList candList) {
        this.candList = candList;
    }
    
    public Cand deepClone() {
        Cand c = new Cand(phfile.deepClone(), period, getFoldSNR(), DM, accel, jerk,score,MJD);
        c.setCandList(candList);
        c.setViewed(viewed);
        return c;
    }
    
    
    public void setPeriod(double period) {
        this.period = (float)period;
    }
    
    public void setSNR(float SNR) {
        this.setFoldSNR(SNR);
    }
    
    public void setDM(float DM) {
        this.DM = DM;
    }
    
    public CandidateFile getPhfile() {
        return phfile;
    }
    
    public void setPhfile(CandidateFile phfile) {
        this.phfile = phfile;
    }
    
    
    public void setAccel(double accel) {
        this.accel = (float)accel;
    }
    
    public void setJerk(double jerk) {
        this.jerk = (float)jerk;
    }
    
    public void setMJD(double MJD) {
        this.MJD = MJD;
    }
    
    public int getNPulses() {
        if(this.nPulses < 1){
            //  System.out.println("Looking for pulses");
            if(this.phfile instanceof PulsarCandFile){
                this.nPulses = (short)((PulsarCandFile)this.phfile).getNPulses();
                //   System.out.println("Got "+this.nPulses+" Pulses");
            }
        }
        return nPulses;
    }
    
    public void setNPulses(int nPulses) {
        
        this.nPulses = (short)nPulses;
    }
    
    
    public float getFrequency(){
        
        return  (float) candList.getFch1();
        
    }
    public float getBandwidth(){
        return (float)candList.getBand();
    }
    
    public float getFoldSNR() {
        return foldSNR;
    }
    
    public void setFoldSNR(float foldSNR) {
        this.foldSNR = foldSNR;
    }
    
    public float getSpecSNR() {
        return specSNR;
    }
    
    public void setSpecSNR(float specSNR) {
        this.specSNR = specSNR;
    }
    
    public float getReconSNR() {
        return reconSNR;
    }
    
    public void setReconSNR(float reconSNR) {
        this.reconSNR = reconSNR;
    }
    
    public int hashCode() {
        
        return this.getUniqueIdentifier().hashCode();
    }

    public boolean isDud() {
        return dud;
    }

    public void setDud(boolean dud) {
       
        this.dud = dud;
    }

    public ArrayList<String> getComments() {
        return comments;
    }
    
    
}

