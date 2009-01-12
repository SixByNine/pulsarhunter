/*
 * Options.java
 *
 * Created on 09 October 2007, 21:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.jreaper;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import pulsarhunter.Pair;
import pulsarhunter.jreaper.Score.ScoreType;
import pulsarhunter.jreaper.Colourmap;

/**
 *
 * @author Mike Keith
 */
public class Options {
    
    
    public enum OptionsXMLTypes{Options,KnownPsrFile,KnownPsrFormat,ColorMap,Eta,Distmax,ScoreFactor ,RecheckKnownPSRs,ZapFile,ZapFileRoot,Other};
    
    private String knownPsrFile = "./knownpulsars.list";
    private String knownPsrFormat = "NAME PERIOD X RA X DEC X";
    private Colourmap grayColourMap = Colourmap.defaultGreyColmap;
    private Colourmap ZColorMap = Colourmap.defaultZColmap;
    private double eta = 0.001;
    private double distmax = 0.5;
    private Colourmap stdColmap = Colourmap.defaultSTDColmap;
    private Hashtable<ScoreType,Double> scoreFactors = new Hashtable<ScoreType,Double>();
    private boolean alwaysCheckForKnownPSRs = false;
    private boolean zfe = false;
    private ArrayList<Pair<String,Boolean>> zapFiles = new ArrayList<Pair<String,Boolean>>();
    private String zapFileRoot = "./";
    
    private StringBuffer content = new StringBuffer();
    /** Creates a new instance of Options */
    public Options() {
    }
    
    public void write(PrintStream out){
        out.println("<Options>");
        out.println("<KnownPsrFile>"+knownPsrFile+"</KnownPsrFile>");
        out.println("<KnownPsrFormat>"+knownPsrFormat+"</KnownPsrFormat>");
        out.println("<Eta>"+eta+"</Eta>");
        out.println("<Distmax>"+distmax+"</Distmax>");
        for(ScoreType type : this.scoreFactors.keySet()){
            out.println("<ScoreFactor type=\""+type+"\">"+this.scoreFactors.get(type)+"</ScoreFactor>");
        }
        
        out.println("<Colormap type='zaxis' name='"+ZColorMap.toString()+"'>");
        for(Color c : ZColorMap.getCols()){
            out.printf("%06X ",c.getRGB());
        }
        out.println("</Colormap>");
        out.println("<Colormap type='gray' name='"+grayColourMap.toString()+"'>");
        for(int[] a : grayColourMap.getGCols()){
            for(int i : a){
                out.printf("%02X ",i);
            }
        }
        out.println("</Colormap>");
        out.println("<Colormap type='std' name='"+stdColmap.toString()+"'>");
        for(Color c : stdColmap.getCols()){
            out.printf("%06X ",c.getRGB());
        }
        out.println("</Colormap>");
        if(isAlwaysCheckForKnownPSRs())out.println("<RecheckKnownPSRs />");
        ArrayList<String> zapWritten = new ArrayList<String>();
        for(Pair<String,Boolean> pair : zapFiles){
            if(!zapWritten.contains(pair.getA())){
                out.println("<ZapFile enabled='"+pair.getB()+"'>"+pair.getA()+"</ZapFile>");
                zapWritten.add(pair.getA());
            }
        }
        out.println("<ZapFileRoot>"+zapFileRoot+"</ZapFileRoot>");
        out.println("</Options>");
    }
    
    private String colourmapType = null;
    private String colourmapName = null;
    private String scoreFactorType = null;
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        OptionsXMLTypes type  = null;
        try {
            type = OptionsXMLTypes.valueOf(localName);
        } catch (IllegalArgumentException e)  {
            type = OptionsXMLTypes.Other;
        }
        
        
        
        
        switch(type){
            
            case RecheckKnownPSRs:
                this.setAlwaysCheckForKnownPSRs(true);
                break;
                
                
            case ColorMap:
                for(int i = 0; i < attributes.getLength(); i++){
                    String aType = attributes.getLocalName(i);
                    if(aType.equals("type")){
                        this.colourmapType = attributes.getValue(i);
                    }
                    if(aType.equals("name")){
                        this.colourmapName = attributes.getValue(i);
                    }
                }
                
                break;
            case ScoreFactor:
                for(int i = 0; i < attributes.getLength(); i++){
                    String aType = attributes.getLocalName(i);
                    if(aType.equals("type")){
                        this.scoreFactorType = attributes.getValue(i);
                    }
                }
                break;
            case ZapFile:
                for(int i = 0; i < attributes.getLength(); i++){
                    String aType = attributes.getLocalName(i);
                    if(aType.equals("enabled")){
                        this.zfe = Boolean.parseBoolean(attributes.getValue(i));
                    }
                }
                break;
            default:
                break;
        }
        content = new StringBuffer();
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        OptionsXMLTypes type  = null;
        try {
            type = OptionsXMLTypes.valueOf(localName);
        } catch (IllegalArgumentException e)  {
            type = OptionsXMLTypes.Other;
        }
        
        switch(type){
            case Options:
                break;
            case KnownPsrFile:
                this.knownPsrFile = content.toString().trim();
                break;
            case KnownPsrFormat:
                this.knownPsrFormat = content.toString().trim();
                break;
            case ColorMap:
                if(this.colourmapType.equals("std")){
                    String[] elems = content.toString().trim().split("\\s+");
                    
                    Color[] carr = new Color[elems.length];
                    
                    for(int i = 0; i < elems.length; i++){
                        carr[i] = new Color(Integer.parseInt(elems[i],16));
                    }
                    
                    this.stdColmap = new Colourmap(carr,colourmapName);
                    
                } else if(this.colourmapType.equals("zaxis")){
                    String[] elems = content.toString().trim().split("\\s+");
                    
                    Color[] carr = new Color[elems.length];
                    
                    for(int i = 0; i < elems.length; i++){
                        carr[i] = new Color(Integer.parseInt(elems[i],16));
                    }
                    
                    this.ZColorMap = new Colourmap(carr,colourmapName);
                } else if(this.colourmapType.equals("gray")){
                    String[] elems = content.toString().trim().split("\\s+");
                    int [][] carr = new int[3][(int)(elems.length/3)];
                    int j = 0;
                    int k = 0;
                    for(int i = 0; i < elems.length; i++){
                        if(j > 2){
                            j = 0;
                            k++;
                        } else j++;
                        carr[j][k] = Integer.parseInt(elems[i],16);
                    }
                    this.grayColourMap = new Colourmap(carr,colourmapName);
                }
                break;
            case Eta:
                this.eta = Double.parseDouble(content.toString());
                break;
            case Distmax:
                this.distmax = Double.parseDouble(content.toString());
                break;
            case ScoreFactor:
                ScoreType key;
                
                try {
                    
                    key = ScoreType.valueOf(this.scoreFactorType);
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown score type "+scoreFactorType+" ignored");
                    break;
                }
                this.scoreFactors.put(key,Double.parseDouble(content.toString()));
                break;
            case ZapFile:
                
                this.getZapFiles().add(new Pair(this.content.toString(),zfe));
                zfe = false;
                break;
            case ZapFileRoot:
                this.zapFileRoot = this.content.toString();
                break;
                
        }
        
        
        content = new StringBuffer();
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.content.append(ch,start,length);
    }
    
    
    
    
    public String getKnownPsrFile() {
        return knownPsrFile;
    }
    
    public void setKnownPsrFile(String knownPsrFile) {
        this.knownPsrFile = knownPsrFile;
    }
    
    public String getKnownPsrFormat() {
        return knownPsrFormat;
    }
    
    public void setKnownPsrFormat(String knownPsrFormat) {
        this.knownPsrFormat = knownPsrFormat;
    }
    
    public Colourmap getGrayColourMap() {
        return grayColourMap;
    }
    
    public void setGrayColourMap(Colourmap grayColourMap) {
        this.grayColourMap = grayColourMap;
    }
    
    public Colourmap getZColorMap() {
        return ZColorMap;
    }
    
    public void setZColorMap(Colourmap ZColorMap) {
        this.ZColorMap = ZColorMap;
    }
    
    public double getEta() {
        return eta;
    }
    
    public void setEta(double eta) {
        this.eta = eta;
    }
    
    public double getDistmax() {
        return distmax;
    }
    
    public void setDistmax(double distmax) {
        this.distmax = distmax;
    }
    
    public Colourmap getStdColmap() {
        return stdColmap;
    }
    
    public void setStdColmap(Colourmap stdColmap) {
        this.stdColmap = stdColmap;
    }
    
    public Hashtable<ScoreType, Double> getScoreFactors() {
        return scoreFactors;
    }
    
    public boolean isAlwaysCheckForKnownPSRs() {
        return alwaysCheckForKnownPSRs;
    }
    
    public void setAlwaysCheckForKnownPSRs(boolean alwaysCheckForKnownPSRs) {
        this.alwaysCheckForKnownPSRs = alwaysCheckForKnownPSRs;
    }
    
    public ArrayList<Pair<String, Boolean>> getZapFiles() {
        return zapFiles;
    }
    
    public String getZapFileRoot() {
        return zapFileRoot;
    }
    
    public void setZapFileRoot(String zapFileRoot) {
        this.zapFileRoot = zapFileRoot;
    }
    
    
}
