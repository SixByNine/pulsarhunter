/*
 * Test.java
 *
 * Created on 18 October 2007, 19:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;

/**
 *
 * @author Mike Keith
 */
public class Test {
    
    
    public static void main(String[] args){
//        try {
//
//            System.out.println("testing...");
//
//            PulsarHunterCandidate phcf = new PulsarHunterCandidate(new File("test.phcf"));
//
//            phcf.read();
//
//            phcf.writeXML(new GZIPOutputStream(new FileOutputStream("test.phcx.gz")));
//            phcf.writeOldPHCF(new GZIPOutputStream(new FileOutputStream("test.phcf.gz")));
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
        
        Coordinate coord= new Coordinate(new RA(113.07083333), new Dec(-32.105));

        System.out.println(coord.getGl()+", "+coord.getGb());

        System.out.println(coord.getRA().toDegrees()+", "+coord.getDec().toDegrees());



        coord= new Coordinate(new RA("07:32:17.0"), new Dec("-32:06:18.8"));

        System.out.println(coord.getGl()+", "+coord.getGb());

        
    }
}
