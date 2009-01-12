/*
 * ExceptionHandler.java
 *
 * Created on 10 October 2007, 11:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.jreaper;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 *
 * @author Mike Keith
 */
public class ExceptionHandler implements UncaughtExceptionHandler{
    
    /** Creates a new instance of ExceptionHandler */
    public ExceptionHandler() {
    }

    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }
    
}
