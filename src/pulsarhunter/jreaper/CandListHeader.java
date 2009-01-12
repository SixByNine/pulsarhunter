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
 * CandListHeader.java
 *
 * Created on 01 November 2006, 14:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.jreaper;

import coordlib.Beam;
import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;
import coordlib.Telescope;
import java.io.IOException;
import java.io.PrintStream;
import org.xml.sax.Attributes;

import org.xml.sax.SAXException;
import pulsarhunter.Data;

/**
 *
 * @author mkeith
 */
public class CandListHeader extends Data.Header {
    private String name;
    //  Coordinate[] coverage;
    private Beam beam = new Beam("",new Coordinate());
    
    private boolean knownpsrSearched = false;
    private StringBuffer content = new StringBuffer();
    
    public enum CandListHeaderXMLTypes{CLHead,Name,Tobs,Telescope,Freq,Band,Coordinate,BeamId,KnownPSRSearched,Other};
    
    
    private int version;
    
    /** Creates a new instance of CandListHeader */
    public CandListHeader() {
    }
    
    public void write(PrintStream out) throws IOException{
        

        
        out.println("<CLHead version='"+CandList.VERSION+"'>");
        out.println("<Name>"+getName()+"</Name>");
        out.println("<Freq>"+getFrequency()+"</Freq>");
        out.println("<Band>"+getBandwidth()+"</Band>");
        out.println("<Tobs>"+getTobs()+"</Tobs>");
        out.println("<Telescope>"+getTelescope()+"</Telescope>");
        out.println("<Coordinate type=\"ra dec\" >"+this.getBeam().getCoord().getRA().toDegrees()+" "+this.getBeam().getCoord().getDec().toDegrees()+"</Coordinate>");
        
        out.println("<BeamId>"+this.getBeam().getName().trim()+"</BeamId>");
        if(this.isKnownpsrSearched())out.println("<KnownPSRSearched />");
        
        out.println("</CLHead>");
        
    }
    
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        
        
        content = new StringBuffer();
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        CandListHeaderXMLTypes type = CandListHeaderXMLTypes.valueOf(localName);
        
        switch(type){
            case Name:
                this.setName(content.toString());
                break;
                
            case Freq:
                this.setFrequency(Double.parseDouble(content.toString()));
                break;
            case Band:
                this.setBandwidth(Double.parseDouble(content.toString()));
                break;
            case Tobs:
                this.setTobs(Double.parseDouble(content.toString()));
                break;
            case Telescope:
                this.setTelescope(Telescope.valueOf(content.toString()));
                break;
            case Coordinate:
                String[] elems = content.toString().trim().split("\\s+");
                beam = new Beam(beam.getName(),new Coordinate(new RA(Double.parseDouble(elems[0])),new Dec(Double.parseDouble(elems[1]))));
                break;
                
            case BeamId:
                beam = new Beam(content.toString(),beam.getCoord());
                break;
            case KnownPSRSearched:
                this.setKnownpsrSearched(true);
                break;
                
        }
        content = new StringBuffer();
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.content.append(ch,start,length);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Beam getBeam() {
        return beam;
    }
    
    public void setBeam(Beam beam) {
        this.beam = beam;
    }
    
    public boolean isKnownpsrSearched() {
        return knownpsrSearched;
    }
    
    public void setKnownpsrSearched(boolean knownpsrSearched) {
        this.knownpsrSearched = knownpsrSearched;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    
    
    
    
}
