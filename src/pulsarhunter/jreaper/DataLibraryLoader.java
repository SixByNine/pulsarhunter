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
 * DataLibraryLoader.java
 *
 * Created on 31 October 2006, 09:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.jreaper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author mkeith
 */
public class DataLibraryLoader extends DefaultHandler{
    
    public enum DataLibraryXMLTypes{DataLibrary,RootPath,CandLists,CandList,CLHead,Name,Tobs,Telescope,Freq,Band,Coordinate,BeamId,Group
            ,Options,KnownPsrFile,KnownPsrFormat,ColorMap,Eta,Distmax,RecheckKnownPSRs,ScoreFactor,ZapFile,ZapFileRoot
            ,Other};
            private DataLibrary dl = null;
            private Hashtable<String,StringBuffer> content = new Hashtable<String,StringBuffer>();
            private Stack<String> tagStack = new Stack<String>();
            private Hashtable<String,CandListHeader> clGroup = null;
            private Hashtable<String,File> clGroupData = null;
            private String clGroupName = null;
            private String clFile = null;
            
            private CandListHeader clHead = null;
            
            
            /** Creates a new instance of DataLibraryLoader */
            private DataLibraryLoader() {
            }
            
            
            public static DataLibrary load(BufferedReader in) throws IOException{
                
                try{
                    XMLReader XMLReaderparser = XMLReaderFactory.createXMLReader();
                    DataLibraryLoader dll = new DataLibraryLoader();
                    XMLReaderparser.setContentHandler(dll);
                    XMLReaderparser.parse(new InputSource(in));
                    return dll.dl;
                } catch (SAXException ex) {
                    System.err.println("XML parsing exception reading datalibrary");
                    throw new RuntimeException(ex);
                }
                
//
//        Constructor con = Class.forName(dlClass).getConstructor(BufferedReader.class);
//
//        dl = (DataLibrary) con.newInstance(in);
//
                
                
                
            }
            
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                
                super.startElement(uri, localName, qName, attributes);
                
                
                this.content.put(localName,new StringBuffer());
                this.tagStack.push(localName);
                DataLibraryXMLTypes type = null;
                try {
                    type = DataLibraryXMLTypes.valueOf(localName);
                } catch (IllegalArgumentException e) {
                }
                if(type == null) type = DataLibraryXMLTypes.Other;
                
                switch(type){
                    
                    case DataLibrary:
                        String dlClass = null;
                        
                        for(int i = 0; i < attributes.getLength(); i++){
                            String aType = attributes.getLocalName(i);
                            if(aType.equals("java_class")){
                                dlClass = attributes.getValue(i);
                            }
                        }
                        
                        if(dlClass == null){
                            throw new SAXException("No class specified in the Datalibrary element");
                        }
                        
                        
                        
                        try {
                            Constructor con;
                            con = Class.forName(dlClass).getConstructor();
                            dl = (DataLibrary) con.newInstance();
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        } catch (InvocationTargetException ex) {
                            ex.printStackTrace();
                        } catch (InstantiationException ex) {
                            ex.printStackTrace();
                        } catch (IllegalAccessException ex) {
                            ex.printStackTrace();
                        } catch (SecurityException ex) {
                            ex.printStackTrace();
                        } catch (NoSuchMethodException ex) {
                            ex.printStackTrace();
                        } catch (ClassNotFoundException ex) {
                            throw new SAXException("Invalid class ("+dlClass+") specified in the Datalibrary element");
                        }
                        
                        break;
                        
                    case RootPath:
                        if(dl==null)throw new SAXException("Badly formated XML document, no Datalibrary tag");
                        
                        
                        break;
                        
                    case CandLists:
                        
                        if(this.clGroup!=null){
                            throw new SAXException("Cannot have nested Candidate list groups!");
                        }
                        
                        for(int i = 0; i < attributes.getLength(); i++){
                            String aType = attributes.getLocalName(i);
                            if(aType.equals("group_name")){
                                this.clGroupName = attributes.getValue(i);
                            }
                        }
                        this.clGroup = new Hashtable<String,CandListHeader>();
                        this.clGroupData = new Hashtable<String,File>();
                        
                        if(clGroupName == null){
                            clGroupName = "default";
                        }
                        
                        break;
                        
                    case CandList:
                        clHead = new CandListHeader();
                        for(int i = 0; i < attributes.getLength(); i++){
                            String aType = attributes.getLocalName(i);
                            if(aType.equals("file")){
                                this.clFile = attributes.getValue(i);
                            }
                        }
                        break;
                        
                    case CLHead:
                    case Name:
                    case Tobs:
                    case Telescope:
                    case Coordinate:
                        
                    case Freq:
                    case Band:
                    case BeamId:
                        clHead.startElement(uri,localName,qName,attributes);
                        break;
                        
                        
                    case Options:
                    case KnownPsrFile:
                    case KnownPsrFormat:
                    case ColorMap:
                    case Eta:
                    case Distmax:
                    case RecheckKnownPSRs:
                    case ScoreFactor:
                    case ZapFile:
                    case ZapFileRoot:
                        dl.getOptions().startElement(uri,localName,qName,attributes);
                        break;
                    case Other:
                        
                        dl.startElement(uri,localName,qName,attributes);
                        
                        break;
                        
                    default:
                        break;
                }
                
                
            }
            
            public void endElement(String uri, String localName, String qName) throws SAXException {
                super.endElement(uri, localName, qName);
                String openTag = tagStack.pop();
                if(!openTag.equals(localName)){
                    throw new SAXException("Opening tag ("+openTag+") closed by non matching tag ("+localName+")");
                }
                
                String contents = this.content.remove(localName).toString().trim();
                DataLibraryXMLTypes type = null;
                try {
                    type = DataLibraryXMLTypes.valueOf(localName);
                } catch (IllegalArgumentException e)  {
                }
                if(type == null) type = DataLibraryXMLTypes.Other;
                
                switch(type){
                    case RootPath:
                        
                        this.dl.setRootPath(new File(contents));
                        
                        break;
                    case CandLists:
                        
                        this.dl.setCandListGroup(this.clGroupName,this.clGroup,this.clGroupData);
                        
                        this.clGroup=null;
                        this.clGroupData=null;
                        this.clGroupName = null;
                        break;
                        
                    case CandList:
                        this.clGroup.put(clHead.getName(),clHead);
                        this.clGroupData.put(clHead.getName(),new File(clFile));
                        this.clHead = null;
                        break;
                        
                    case CLHead:
                    case Name:
                    case Tobs:
                    case Telescope:
                    case Coordinate:
                    case Freq:
                    case Band:
                    case BeamId:
                        clHead.characters(contents.toCharArray(),0,contents.length());
                        clHead.endElement(uri,localName,qName);
                        break;
                        
                        
                    case Options:
                    case KnownPsrFile:
                    case KnownPsrFormat:
                    case ColorMap:
                    case Eta:
                    case Distmax:
                    case RecheckKnownPSRs:
                    case ScoreFactor:
                    case ZapFile:
                    case ZapFileRoot:
                        dl.getOptions().characters(contents.toCharArray(),0,contents.length());
                        dl.getOptions().endElement(uri,localName,qName);
                        
                        break;
                        
                    case Other:
                        
                        dl.characters(contents.toCharArray(),0,contents.length());
                        dl.endElement(uri,localName,qName);
                        break;
                        
                    default:
                        break;
                        
                }
                
                
            }
            
            public void characters(char[] ch, int start, int length) throws SAXException {
                String localName = this.tagStack.peek();
                this.content.get(localName).append(ch,start,length);
            }
            
            
            
}
