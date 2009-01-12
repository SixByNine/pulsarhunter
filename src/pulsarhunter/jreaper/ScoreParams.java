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
package pulsarhunter.jreaper;


    
    
    public class ScoreParams {    
    /** Creates a new instance of PeckScoreParams */
    private ScoreParams() {
    }
    
    private static double snrFactor = 0.0;
    private static double dmzerodiffFactor = 1.0;
    private static double dmcurveshapeFactor = 1;
    private static double widthFactor = 1;
    private static double freqVarianceFactor = 1;
    private static double profileShapeFactor = 1.0;
    private static double profileHighValueFactor = 1.0;
    private static double subintLinearFactor = 1;
    private static double subintGeneralFactor = 1;
    private static double additionalScoreFactor = 1;
    
    
    public static void setDmcurveshapeFactor(double aDmcurveshapeFactor) {
        dmcurveshapeFactor = aDmcurveshapeFactor;
        
    }
    
    public static double getWidthFactor() {
        return widthFactor;
    }
    
    public static void setWidthFactor(double aWidthFactor) {
        widthFactor = aWidthFactor;
    }
    
    public static double getFreqVarianceFactor() {
        return freqVarianceFactor;
    }
    
    public static void setFreqVarianceFactor(double aFreqVarianceFactor) {
        freqVarianceFactor = aFreqVarianceFactor;
    }
    
    public static double getProfileShapeFactor() {
        return profileShapeFactor;
    }
    
    public static void setProfileShapeFactor(double aProfileShapeFactor) {
        profileShapeFactor = aProfileShapeFactor;
    }
    
    public static double getProfileHighValueFactor() {
        return profileHighValueFactor;
    }
    
    public static void setProfileHighValueFactor(double aProfileHighValueFactor) {
        profileHighValueFactor = aProfileHighValueFactor;
    }
    
    public static double getSubintLinearFactor() {
        return subintLinearFactor;
    }
    
    public static void setSubintLinearFactor(double aSubintLinearFactor) {
        subintLinearFactor = aSubintLinearFactor;
    }
    
    public static double getSubintGeneralFactor() {
        return subintGeneralFactor;
    }
    
    public static void setSubintGeneralFactor(double aSubintGeneralFactor) {
        subintGeneralFactor = aSubintGeneralFactor;
    }
    
    public static double getAdditionalScoreFactor() {
        return additionalScoreFactor;
    }
    
    public static void setAdditionalScoreFactor(double aAdditionalScoreFactor) {
        additionalScoreFactor = aAdditionalScoreFactor;
    }
    
    public static double getSnrFactor() {
        return snrFactor;
    }
    
    public static void setSnrFactor(double aSnrFactor) {
        snrFactor = aSnrFactor;
    }
    
    public static double getDmzerodiffFactor() {
        return dmzerodiffFactor;
    }
    
    public static void setDmzerodiffFactor(double aDmzerodiffFactor) {
        dmzerodiffFactor = aDmzerodiffFactor;
    }
    
    public static double getDmcurveshapeFactor() {
        return dmcurveshapeFactor;
    }}
