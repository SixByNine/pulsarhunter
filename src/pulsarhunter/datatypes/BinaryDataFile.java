/*
 * BinaryDataFile.java
 *
 * Created on February 24, 2008, 11:11 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

/**
 *
 * @author mkeith
 */
public class BinaryDataFile {
    
    int headerLength = 0;
    int blockHeaderLength = 0;
    int blockSize = 0;
    
    DataType dataType;
    
    public enum DataType{OneBit(1,false),TwoBit(2,false),FourBit(4,false)
    ,EightBit(8,false),SixteenBit(16,false),ThirtytwoBit(32,false)
    ,ThirtytwoBitFloat(32,true),SixtyfourBit(64,false),SixtyfourBitFloat(64,true);
    
    
    private int nbits;
    private boolean floating;
    DataType(int nbints, boolean floating){
        this.nbits = nbits;
        this.floating = floating;
    }
    
    };
    
    /** Creates a new instance of BinaryDataFile */
    public BinaryDataFile(DataType dataType) {
        this.dataType = dataType;
    }
    
    
    
}
