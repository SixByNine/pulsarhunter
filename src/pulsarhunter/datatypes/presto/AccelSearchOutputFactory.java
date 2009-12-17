/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pulsarhunter.datatypes.presto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pulsarhunter.Data;
import pulsarhunter.DataFactory;
import pulsarhunter.IncorrectDataTypeException;

/**
 *
 * @author kei041
 */
public class AccelSearchOutputFactory implements DataFactory {

    public Data loadData(String filename, int bufSize) throws IncorrectDataTypeException {
        File f = new File(filename);

System.out.println("TEST1");
        if (!f.getName().endsWith(".accelsearch")) {
            throw new IncorrectDataTypeException("Error trying to read AccelSearchOutput file " + f.getName() + ". It MUST end with .accelsearch");
        }
        try {
            System.out.println("TEST2");
            return new AccelSearchOutput(f);
        } catch (FileNotFoundException ex) {
            throw new IncorrectDataTypeException("AccelSearchOutputFactory: File " + filename + "does not exist!");
        } catch (IOException ex) {
            throw new IncorrectDataTypeException("AccelSearchOutputFactory: File " + filename + " could not be read!");
        }


    }

    public String getName() {
        return "PRESTOACCELRES";
    }

    public Data createData(String filename) throws IncorrectDataTypeException {
        throw new IncorrectDataTypeException("createData not supported on " + this.getName());
    }
}
