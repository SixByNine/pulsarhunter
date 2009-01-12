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
 * ZapFile.java
 *
 * Created on 27 February 2007, 09:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import pulsarhunter.Data;
import pulsarhunter.FrequencyFilter;

/**
 *
 * @author mkeith
 */
public class ZapFile implements Data{
    
    private ArrayList<FrequencyFilter> filters = new ArrayList<FrequencyFilter>();
    private File file;
    private String title="New Zap File";
    /** Creates a new instance of ZapFile */
    public ZapFile(File file){
        this.file = file;
    }
    public void read()throws IOException {
        read(file);
    }
    public void read(File file)throws IOException {
        filters = new ArrayList<FrequencyFilter>();
        this.file = file;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        if(!line.equals("##PULSARHUNTER ZAP FILE##"))throw new IOException("Not a pulsarhunter zap file!");
        
        while(line!=null && !line.equals("##BEGIN ZAP LIST##")){
            String[] elems = line.trim().split("=",2);
            if(elems.length==2){
                String value = elems[1];
                String key = elems[0];
                
                if(key.equalsIgnoreCase("TITLE")){
                    this.title = value;
                }
            }
            line = reader.readLine();
        }
                    line = reader.readLine();
        while(line!=null){
            String[] elems = line.split("\\s+",4);
            FrequencyFilter f = new FrequencyFilter(Double.parseDouble(elems[0]),Double.parseDouble(elems[1]),Integer.parseInt(elems[2]));
            if(elems.length > 2)f.setName(elems[3]);
            getFilters().add(f);
            
            line = reader.readLine();
        }
        reader.close();
    }
    public void write() throws IOException {
        write(file);
    }
    public void write(File file)throws IOException {
        this.file = file;
        PrintStream out = new PrintStream(new FileOutputStream(file));
        out.println("##PULSARHUNTER ZAP FILE##");
        out.println("TITLE="+getTitle());
        out.println("##BEGIN ZAP LIST##");
        for(FrequencyFilter f : filters){
            out.printf("%f\t%f\t%d\t%s\n",f.getStart(),f.getEnd(),f.getMatches(),f.getName());
        }
        out.close();
    }
    
    public String getDataType() {
       return "ZAPFILE";
    }
    public void release() {
        try {
            flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.filters = null;
    }
    
    public Header getHeader() {
        return null;
    }
    
    public void flush() throws IOException {
        this.write(file);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public ArrayList<FrequencyFilter> getFilters() {
        return filters;
    }
    
    public void addFilters(ArrayList<FrequencyFilter> newFilters) {
        this.filters.addAll(newFilters);
    }
    
    
    public void addFilter(FrequencyFilter newFilter) {
        this.filters.add(newFilter);
    }
    
    public void clearFilters() {
        this.filters = new ArrayList<FrequencyFilter>();
    }
    
}
