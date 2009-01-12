/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pulsarhunter.bookkeepr.jreaper;

import pulsarhunter.jreaper.CandClass;
import pulsarhunter.jreaper.HarmonicType;

/**
 *
 * @author kei041
 */
public class CandStatus {

    
    private static int classMask = 0xFFFFF0FF;
    private static int possMask = 0xFFFFFF7F;
    private static int harmMask = 0xFFFF0FFF;
    private static int colourMask = 0xFF00FFFF;
    
    /*
     * Status
     * 
     * Translation of status code
     * 4        3        2        1
     * 12345678 12345678 12345678 12345678
     * xxxxxxxx UUUUUUUU HHHHCCCC PxxxxxxV
     * 
     * V = viewed                   0x00000001
     * C = cand class (0->16)       0x00000F00
     * H = harmonic type (0 -> 16)  0x0000F000
     * U = Colour        (0-255)    0x00FF0000
     * P = Possible match flag      0x00000080
     * x = not used
     */
    
    public static boolean isViewed(int status){
        return (status & 0x1) > 0;
    }
    public static int setViewed(boolean viewed, int status){
        if(viewed) return status | 0x1;
        else return status & 0xFFFFFFFE;
    }
    
    public static CandClass getCandClass(int status){
        return CandClass.fromIntClass((status & 0xF00)>>8);
    }
    
    public static int setCandClass(CandClass candClass, int status){
        status = status & classMask;
        return status | (candClass.getIntClass()<<8);
    }
    
    public static boolean getPossible(int status){
        return (status & 0x80) > 0;
    }
    
    public static int setPossible(boolean possible, int status){
        status = status & possMask;
        if(possible)status = status | 0x80;
        return status;
    }
    
     public static int getZAxis(int status){
        return (status & 0xFF0000)>>16;
    }
    
    public static int setZAxis(int z, int status){
        status = status & colourMask;
        if(z<0)return status;
       return  status | (z<<16);
    }
    
    
    public static HarmonicType getHarmonicType(int status){
        int typenum = (status & 0xF000)>>12;
        switch(typenum){
            case 0:
                return HarmonicType.None;
            case 1:
                return HarmonicType.Principal;
            case 2:
                return HarmonicType.Integer;
            case 3:
                return HarmonicType.SimpleNonInteger;
            case 4:
                return HarmonicType.ComplexNonInteger;
        }
        return HarmonicType.None;
    }
    public static int setHarmonicType(HarmonicType type, int status){
        int harmint = 0;
        status = status & harmMask;
        switch(type){
            
            case Principal:
                harmint = 1;
                break;
            case Integer:
                harmint = 2;
                break;
            case SimpleNonInteger:
                harmint = 3;
                break;
            case ComplexNonInteger:
                harmint = 4;
                break;
        }
        return status | (harmint << 12);
    }

    public static int clearCands(int plotStatus) {
        return plotStatus & classMask & possMask & harmMask;
    }
}
