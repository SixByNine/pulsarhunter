/*
 * Test.java
 *
 * Created on 18 October 2007, 19:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import pulsarhunter.datatypes.PulsarHunterCandidate;

/**
 *
 * @author Mike Keith
 */
public class Test {
    
    
    public static void main(String[] args){
        try {
            
            System.out.println("testing...");
            
            PulsarHunterCandidate phcf = new PulsarHunterCandidate(new File("test.phcf"));
            
            phcf.read();
            
            phcf.writeXML(new GZIPOutputStream(new FileOutputStream("test.phcx.gz")));
            phcf.writeOldPHCF(new GZIPOutputStream(new FileOutputStream("test.phcf.gz")));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
        
        
    }
}
