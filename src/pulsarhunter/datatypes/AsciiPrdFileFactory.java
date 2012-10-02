/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pulsarhunter.datatypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import pulsarhunter.Data;
import pulsarhunter.DataFactory;
import pulsarhunter.IncorrectDataTypeException;

/**
 *
 * @author kei041
 */
public class AsciiPrdFileFactory implements DataFactory {

    public AsciiPrdFileFactory() {
    }

    public Data createData(String filename) throws IncorrectDataTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Data loadData(String filename, int buffersize) throws IncorrectDataTypeException {
        if (filename.endsWith(".ascprd")) {
            try {
                return new AsciiPrdFile(new File(filename));
            } catch (FileNotFoundException e) {
                throw new IncorrectDataTypeException("File not found", e);
            } catch (IOException e) {
                throw new IncorrectDataTypeException("IO Exception", e);
            }
        } else {
            throw new IncorrectDataTypeException(".ascprd files must have correct extention");
        }
    }

    public String getName() {
        return "ASCIIPRDFILE";
    }
}
