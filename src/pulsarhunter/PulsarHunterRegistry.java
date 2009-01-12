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
 * PulsarHunterRegistry.java
 *
 * Created on 25 October 2006, 11:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author mkeith
 */
public class PulsarHunterRegistry {
    
    /*private static PulsarHunterRegistry instance = null;
    public static PulsarHunterRegistry getInstance(){
        if(instance == null){
            instance = new PulsarHunterRegistry();
        }
        return instance;
    }*/
    
    private Hashtable<String,DataFactory>  dataFactories = new Hashtable<String,DataFactory>();
    private Hashtable<String,ProcessFactory>  processFactories = new Hashtable<String,ProcessFactory>();
    private Hashtable<String,String>  switches = new Hashtable<String,String>();
    private final GlobalOptions options = new GlobalOptions();
    
    /** Creates a new instance of PulsarHunterRegistry */
    public PulsarHunterRegistry() {
    }
    
    public DataFactory getDataFactory(String name){
        return dataFactories.get(name.toUpperCase());
    }
    public List<String> getDataFactoryList(){
        return new ArrayList<String>(dataFactories.keySet());
    }
    public void addDataFactory(DataFactory d){
        dataFactories.put(d.getName().toUpperCase(), d);
    }
    
    
    public ProcessFactory getProcessFactory(String name){
        return processFactories.get(name.toUpperCase());
    }
    public List<String> getProcessFactory(){
        return new ArrayList<String>(processFactories.keySet());
    }
    public void addProcessFactory(ProcessFactory d){
        processFactories.put(d.getName().toUpperCase(), d);
    }
    
    public String getSwitches(String key){
        return this.switches.get((key.toUpperCase()));
    }
    
    public void addSwitches(String key, String switches){
        this.switches.put(key.toUpperCase(),switches);
    }

    public GlobalOptions getOptions() {
        return options;
    }

    
}
