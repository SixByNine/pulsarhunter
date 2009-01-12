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
 * CandList.java
 *
 * Created on 26 May 2005, 15:35
 */

package pulsarhunter.jreaper;

import coordlib.Beam;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import coordlib.Coordinate;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import pulsarhunter.Data;
import coordlib.Telescope;
import java.util.Collections;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import pulsarhunter.jreaper.HarmonicType;


/**
 * A serializable object that contains a large array of {@link Cand}s.
 * @author mkeith
 */
public class CandList implements Data<CandListHeader>{
    public enum CandListXMLTypes{CandList,CLHead,CLBody,Name,Tobs,Telescope,Freq,Band,Coordinate,BeamId,KnownPSRSearched,Cand,
    CandName,CandType,Detection,Period,Dm,FoldSnr,SpecSnr,ReconSnr,
    Accel,Jerk,MJD,Score,CandFile,Viewed,Dud,Comment,Npulses
            ,Other};
            
            static final long serialVersionUID = 8017983461134107478L ;
            public static final int VERSION = 6;
            
            public static boolean IGNORE_ERRORS = false;
            
            Cand[][] cands;
            private CandListHeader header = new CandListHeader();
            private File file = null;
            // private String plotGenClass = "";
            private transient DataLibrary dataLibrary;
            /**
             * Creates a new instance of CandList
             * @param name The name for this CandList
             * @param cands An array of {@link Cand} that contains the new content for this CandList
             * @param coverage The Coordinates covered by this candlist.
             */
            public CandList(String name,Cand[][] cands,Beam beam) {
                this.cands = cands;
                this.getHeader().setName(name);
                this.getHeader().setBeam(beam);
                for(Cand[] cs : cands){
                    for(Cand c : cs){
                        c.setCandList(this);
                    }
                }
            }
            
            public String getDataType() {
                return null;
            }
            public CandList(CandListHeader header) {
                this.cands = null;
                this.header = header;
            }
            
            public CandList(BufferedReader in)throws IOException{
                this.read(in);
            }
            
            public void read(BufferedReader in)throws IOException{
                
                final ArrayList[] cArrLists = new ArrayList[5];
                
                for(int i = 0; i < 5; i++){
                    cArrLists[i] = new ArrayList();
                }
                try{
                    XMLReader XMLReaderparser = XMLReaderFactory.createXMLReader();
                    final CandList finalThis = this;
                    
                    XMLReaderparser.setContentHandler(new DefaultHandler(){
                        private Cand currentCand;
                        private int currentCandType;
                        private StringBuffer content = new StringBuffer();
                        private String scoreType = null;
                        private String scoreLabels = null;
                        private String candFileType = null;
                        // internal parser...
                        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                            CandListXMLTypes type = null;
                            try {
                                type = CandListXMLTypes.valueOf(localName);
                            } catch (IllegalArgumentException e)  {
                                type = CandListXMLTypes.Other;
                            }
                            
                            switch(type){
                                case Name:
                                case Freq:
                                case Band:
                                case Tobs:
                                case Telescope:
                                case Coordinate:
                                case BeamId:
                                case KnownPSRSearched:
                                    finalThis.getHeader().startElement(uri,localName,qName,attributes);
                                    break;
                                case Score:
                                    for(int i = 0; i < attributes.getLength(); i++){
                                        String aType = attributes.getLocalName(i);
                                        if(aType.equals("type")){
                                            scoreType = attributes.getValue(i);
                                        }
                                        if(aType.equals("labels")){
                                            scoreLabels = attributes.getValue(i);
                                        }
                                    }
                                    if(scoreType==null)scoreType="PECK_NEW_1";
                                    break;
                                    
                                case CandFile:
                                    for(int i = 0; i < attributes.getLength(); i++){
                                        String aType = attributes.getLocalName(i);
                                        if(aType.equals("java_class")){
                                            candFileType = attributes.getValue(i);
                                        }
                                    }
                                    break;
                                case Cand:
                                    currentCand = new Cand();
                                    break;
                                    
                                case Viewed:
                                    currentCand.setViewed(true);
                                    break;
                                case Dud:
                                    currentCand.setDud(true);
                                    break;
                                case Other:
                                    System.out.println("Unown tag: "+localName+" ignored...");
                                    break;
                            }
                            
                            content = new StringBuffer();
                        }
                        
                        public void endElement(String uri, String localName, String qName) throws SAXException {
                            CandListXMLTypes type=null;
                            try {
                                type = CandListXMLTypes.valueOf(localName);
                            } catch (IllegalArgumentException e)  {
                                type = CandListXMLTypes.Other;
                            }
                            
                            
                            
                            switch(type){
                                case Name:
                                case Freq:
                                case Band:
                                case Tobs:
                                case Telescope:
                                case Coordinate:
                                case BeamId:
                                case KnownPSRSearched:
                                    finalThis.getHeader().characters(content.toString().trim().toCharArray(),0,content.toString().trim().length());
                                    finalThis.getHeader().endElement(uri,localName,qName);
                                    break;
                                    
                                case CandName:
                                    break;
                                    
                                case CandType:
                                    currentCandType = Integer.parseInt(content.toString().trim());
                                    break;
                                    
                                case Detection:
                                    String[] elems = content.toString().trim().split("###");
                                    
                                    
                                    try {
                                        //    public Detection(String name,String harmonic, int candClass, HarmonicType harmType,double fundPeriod) {
                                        Detection d = new Detection(elems[0],elems[1],Integer.parseInt(elems[2]),HarmonicType.valueOf(elems[3]),Double.parseDouble(elems[4]));
                                        currentCand.addDetection(d);
                                    } catch (NumberFormatException ex) {
                                        System.err.println("Could not read detection from "+getName()+" cand="+currentCand.getName());
                                    } catch (ArrayIndexOutOfBoundsException ex){
                                        System.err.println("Could not read detection from "+getName()+" cand="+currentCand.getName());
                                    }
                                    break;
                                case Comment:
                                    currentCand.addComment(content.toString());
                                    break;
                                    
                                case Period:
                                    currentCand.setPeriod(Double.parseDouble(content.toString()));
                                    break;
                                case Dm:
                                    currentCand.setDM(Float.parseFloat(content.toString()));
                                    break;
                                case FoldSnr:
					float f = Float.parseFloat(content.toString());
					if(Float.isInfinite(f) && !CandList.IGNORE_ERRORS)throw new SAXException("Fold SNR is infinite!");
                                    currentCand.setFoldSNR(Float.parseFloat(content.toString()));
                                    break;
                                case ReconSnr:
                                    currentCand.setReconSNR(Float.parseFloat(content.toString()));
                                    break;
                                case SpecSnr:
                                    currentCand.setSpecSNR(Float.parseFloat(content.toString()));
                                    break;
                                case Accel:
                                    currentCand.setAccel(Double.parseDouble(content.toString()));
                                    break;
                                case Jerk:
                                    currentCand.setJerk(Double.parseDouble(content.toString()));
                                    break;
                                case MJD:
                                    currentCand.setMJD(Double.parseDouble(content.toString()));
                                    break;
                                case Score:
                                    if(scoreType.equals("PECK_OLD")){
                                        Score score = new Score();
                                        score.decode_peck_old(content.toString());
                                        currentCand.setScore(score);
                                    } else if(scoreType.equals("PECK_NEW_1")){
                                        Score score = new Score();
                                        score.decode_new_1(content.toString(),scoreLabels);
                                        currentCand.setScore(score);
                                    }
                                    
                                    scoreType=null;
                                    break;
                                case Npulses:
                                    currentCand.setNPulses(Integer.parseInt(content.toString().trim()));
                                    break;
                                case CandFile:
                                    CandidateFile candFile = null;
                                    try {
                                        Constructor cons = Class.forName(candFileType).getConstructor(String.class);
                                        candFile = (CandidateFile) cons.newInstance(content.toString());
                                    } catch (IllegalArgumentException ex) {
                                        ex.printStackTrace();
                                    } catch (IllegalAccessException ex) {
                                        ex.printStackTrace();
                                    } catch (InstantiationException ex) {
                                        ex.printStackTrace();
                                    } catch (InvocationTargetException ex) {
                                        ex.printStackTrace();
                                    }   catch (SecurityException ex) {
                                        ex.printStackTrace();
                                    } catch (ClassNotFoundException ex) {
                                        ex.printStackTrace();
                                    } catch (NoSuchMethodException ex) {
                                        ex.printStackTrace();
                                    }
                                    currentCand.setPhfile(candFile);
                                    candFileType = null;
                                    break;
                                    
                                case Cand:
                                    currentCand.setCandList(CandList.this);
                                    cArrLists[currentCandType].add(currentCand);
                                    currentCand = null;
                                    break;
                                    //Accel,Jerk,MJD,Score,CandFileName,CandFileClass,Viewed,Dud,
                            }
                            content =  new StringBuffer();
                        }
                        
                        public void characters(char[] ch, int start, int length) throws SAXException {
                            this.content.append(ch,start,length);
                        }
                    });
                    XMLReaderparser.parse(new InputSource(in));
                    
                } catch (SAXException ex) {
                    throw new IOException(ex.getMessage());
                }
                
                
                
                
                this.cands = new Cand[5][];
                for(int i = 0; i < cArrLists.length; i++ ){
                    this.cands[i] = new Cand[cArrLists[i].size()];
                    cArrLists[i].toArray(this.cands[i]);
                }
                
                
                
                
                
                
            }
            
            public void write(PrintStream out) throws IOException{
                
                
                out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                out.println("<CandList>");
                
                header.write(out);
                
                
                out.println("<CLBody version=\""+VERSION+"\">");
                for(int i = 0 ; i < cands.length; i++){
                    for(Cand c : cands[i]){
                        out.println(CandList.candToString(c,i));
                    }
                    
                }
                out.println("</CLBody>");
                out.println("</CandList>");
            }
            
            
            public static String candToString(Cand c,int candType) throws IOException{
                StringWriter sw = new StringWriter();
                PrintWriter out = new PrintWriter(sw);
                
                //            Cand,
                //CandName,CandType,Detection,Period,Dm,FoldSnr,SpecSnr,ReconSnr,
                //Accel,Jerk,MJD,Score,CandFile,Viewed,Dud,Comment,Npulses
                
                out.print("<Cand>");
                
                
                out.printf("<CandName>%s</CandName><CandType>%d</CandType>",c.getName(),candType);
                if(c.getDetectionList()!= null){
                    List<Detection> dl = c.getDetectionList();
                    Collections.reverse(dl);
                    for(Detection det : dl){
                        //    public Detection(String name,String harmonic, int candClass, HarmonicType harmType,double fundPeriod) {
                        out.printf("<Detection>%s###%s###%d###%s###%f</Detection>",det.getName(),det.getHarmonic(),det.getCandClass(),det.getHarmType().toString(),det.getFundPeriod());
                    }
                }
                out.printf("<Period>%10.5f</Period><Dm>%5.1f</Dm><FoldSnr>%5.2f</FoldSnr><ReconSnr>%5.2f</ReconSnr><SpecSnr>%5.2f</SpecSnr>",c.getPeriod(),c.getDM(),c.getFoldSNR(),c.getReconSNR(),c.getSpecSNR());
                
                
                out.printf("<Accel>%6.2f</Accel><Jerk>%6.2f</Jerk><MJD>%8.2f</MJD><Npulses>%d</Npulses>",c.getAccel(),c.getJerk(),c.getMJD(),c.getNPulses());
                
                Score score = c.getScoreObject();
                
                if(score != null){
                    String[] scoreBreak = score.getMachineBreakdown();
                    out.printf("<Score type='PECK_NEW_1' labels='"+scoreBreak[1]+"'>%s</Score>",scoreBreak[0]);
                }
                

                out.print("<CandFile java_class='"+c.getCandidateFile().getClass().getName()+"'>"
                        +c.getCandidateFile().getFile().getAbsolutePath()+"</CandFile>");
                
                
                if(c.beenViewed()){
                    out.print("<Viewed />");
                }
                
                if(c.isDud()){
                    out.print("<Dud />");
                }
                
                for(String s : c.getComments()){
                    out.print("<Comment>"+s+"</Comment>");
                }
                out.print("</Cand>");
                out.flush();
                
                return sw.toString();
            }
            
            /**
             * Returns the {@link Cand}s contained in this CandList
             * @return The candidates in this candlist
             */
            public Cand[][] getCands(){
                return cands;
            }
            /**
             * Returns the name associated with this candlist
             * @return The name of this CandList
             */
            public String getName(){
                return this.getHeader().getName();
            }
            
            /**
             * Returns the name of this CandList
             * @return The name of this candlist
             */
            public String toString(){
                return this.getName();
            }
            
            /**
             * The sky coverage of this CandList as an array of {@link Coordinates}
             * @return The sky coverage of this candlist
             */
            public Coordinate[] coverage(){
                return new Coordinate[]{this.getHeader().getBeam().getCoord()};
            }
            public Beam getBeam(){
                return this.getHeader().getBeam();
            }
            
            public void setDataLibrary(DataLibrary d){
                dataLibrary = d;
            }
            
            public DataLibrary getDataLibrary(){
                return dataLibrary;
            }
            
            public double getFch1() {
                return getHeader().getFrequency();
            }
            
            public void setFch1(double fch1) {
                getHeader().setFrequency(fch1);
            }
            
            public double getBand() {
                return this.getHeader().getBandwidth();
            }
            
            public void setBand(double band) {
                this.getHeader().setBandwidth(band);
            }
            
            public double getTobs() {
                return this.getHeader().getTobs();
            }
            
            public void setTobs(double tobs) {
                this.getHeader().setTobs(tobs);
            }
            
            public Telescope getTelescope() {
                return this.getHeader().getTelescope();
            }
            
            public void setTelescope(Telescope telescope) {
                this.getHeader().setTelescope(telescope);
            }
            
    /*public void setPlotGenClass(String plotGenClass) {
        this.plotGenClass = plotGenClass;
    }*/
            
            public void setBeam(Beam beam) {
                this.getHeader().setBeam(beam);
            }
            
            public CandListHeader getHeader() {
                return header;
            }
            
            public void release() {
                this.cands = null;
                // this.header = null;
                //  this.file = null;
                //this.dataLibrary = null;
                
            }
            
            public void flush() throws IOException {
                
            }
            
}
