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
package pulsarhunter.datatypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;


public class PHCSection{

    
    private String name;
    private double[][] subints = null;
    private double[][] subbands = null;
    private double[] pulseProfile = null;
    private SNRBlock snrBlock;
    
    
    private double bestTopoPeriod = -1;
    private double bestBaryPeriod = -1;
    private double bestDm = 0;
    private double bestAccn = 0;
    private double bestJerk = 0;
    private double bestSnr = 0;
    private double bestWidth = -1;
    private double tsamp = -1;
    
    private Hashtable<String,String> extraValues = new Hashtable<String,String>();
    
    public PHCSection(String name){
        this.name = name;
    }
    
    public double[][] getSubints() {
        if(subints==null)return null;
        double[][] result = new double[subints.length][];
        for(int i = 0; i < subints.length; i++){
            result[i] = new double[subints[i].length];
            System.arraycopy(subints[i],0,result[i],0,subints[i].length);
        }
        return result;
    }
    
    public void setSubints(double[][] subints) {
        this.subints = subints;
    }
    
    public double[][] getSubbands() {
        if(subbands==null)return null;
        double[][] result = new double[subbands.length][];
        for(int i = 0; i < subbands.length; i++){
            result[i] = new double[subbands[i].length];
            System.arraycopy(subbands[i],0,result[i],0,subbands[i].length);
        }
        return result;
    }
    
    public void setSubbands(double[][] subbands) {
        this.subbands = subbands;
    }
    
    public Hashtable<String, String> getExtraValues() {
        return extraValues;
    }
    
    public void setExtraValues(Hashtable<String, String> extraValues) {
        this.extraValues = extraValues;
    }
    
    /**
     * Returns the Extravalue specified by the key, or null if it is not set.
     * @param key The field to return the value of
     * @return The value, or null
     * @see  getExtraValueSafe
     */
    public String getExtraValue(String key){
        return this.getExtraValues().get(key);
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
    public String getExtraValueSafe(String key){
        if(this.getExtraValues().containsKey(key)) return this.getExtraValues().get(key);
        else return "";
    }
    
    public void setExtraValue(String key, String value){
        this.getExtraValues().put(key,value);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    void readExtraField(DataInputStream in) throws IOException{
        in.readInt();
        int keySize = in.readInt();
        int valueSize = in.readInt();
        String key = PulsarHunterCandidate.readASCII(in,keySize);
        String value = PulsarHunterCandidate.readASCII(in,valueSize);
        
        this.getExtraValues().put(key,value);
        
    }
    
    void writeExtraField(DataOutputStream out, String key) throws IOException{
        PulsarHunterCandidate.writeASCII(out,"XTRA");
        
        String value = this.getExtraValues().get(key);
        
        
        out.writeInt(key.length()+value.length());
        out.writeInt(key.length());
        out.writeInt(value.length());
        
        PulsarHunterCandidate.writeASCII(out,key);
        PulsarHunterCandidate.writeASCII(out,value);
        
    }
    
    public SNRBlock getSnrBlock() {
        return snrBlock;
    }
    
    public void setSnrBlock(SNRBlock snrBlock) {
        this.snrBlock = snrBlock;
    }
    
    public double[] getPulseProfile() {
        return pulseProfile;
    }
    
    public void setPulseProfile(double[] pulseProfile) {
        this.pulseProfile = pulseProfile;
    }
    
    public double getBestDm() {
        return bestDm;
    }
    
    public void setBestDm(double bestDm) {
        this.bestDm = bestDm;
    }
    
    
    public double getBestSnr() {
        return bestSnr;
    }
    
    public void setBestSnr(double bestSnr) {
        this.bestSnr = bestSnr;
    }
    
    public double getBestTopoPeriod() {
        return bestTopoPeriod;
    }
    
    public void setBestTopoPeriod(double bestTopoPeriod) {
        this.bestTopoPeriod = bestTopoPeriod;
    }
    
    public double getBestBaryPeriod() {
        return bestBaryPeriod;
    }
    
    public void setBestBaryPeriod(double bestBaryPeriod) {
        this.bestBaryPeriod = bestBaryPeriod;
    }

    public double getBestWidth() {
        return bestWidth;
    }

    public void setBestWidth(double bestWidth) {
        this.bestWidth = bestWidth;
    }

    public double getTsamp() {
        return tsamp;
    }

    public void setTsamp(double tsamp) {
        this.tsamp = tsamp;
    }

    public double getBestAccn() {
        return bestAccn;
    }

    public void setBestAccn(double bestAccn) {
        this.bestAccn = bestAccn;
    }

    public double getBestJerk() {
        return bestJerk;
    }

    public void setBestJerk(double bestJerk) {
        this.bestJerk = bestJerk;
    }

    
    
}
