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
 * PulsarFile_ph.java
 *
 * Created on 24 May 2005, 16:11
 */

package pulsarhunter.jreaper.pmsurv;
import com.bbn.openmap.dataAccess.shape.input.LittleEndianInputStream;
import coordlib.Beam;
import coordlib.Coordinate;
import coordlib.Dec;
import coordlib.RA;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFrame;

import pulsarhunter.jreaper.Cand;
import pulsarhunter.jreaper.CandidateFile;
import pulsarhunter.jreaper.Main;
import pulsarhunter.jreaper.gui.MainView;



/**
 *
 * @author mkeith
 */
public class PulsarFile_sph extends PulsarCandFile  {
    
    private String plad1;
    private String plad2;
    private String plad3;
    private String plad4;
    

    
    /** Creates a new instance of PulsarFile_ph */
    public PulsarFile_sph(String phFile) {
        super(new File(phFile));
    }
    
    public PulsarFile_sph(File phFile) {
        super(phFile);
    }
    
     public void precache() {
            read();
    }
    
    public void read(){
        if(read) return;
        try{
            
            
            
            LittleEndianInputStream instream =this.getInputStream();
            instream.skipBytes(4);  //Fortran Buffer
            tsmp = instream.readLEDouble();
            nbin = instream.readLEInt();
            nbine = instream.readLEInt();
            nchan = instream.readLEInt();
            nband = instream.readLEInt();
            npsub = instream.readLEInt();
            dmc = instream.readLEFloat();
            pc = instream.readLEDouble();
            instream.skipBytes(8); //Fortran Buffer
            plhd1 = instream.readString(120);
            plhd2 = instream.readString(120);
            plhd3 = instream.readString(120);
            plhd9 = instream.readString(120);
            instream.skipBytes(8);  //Fortran Buffer
            
            
            nsub = instream.readLEInt();
            rms = instream.readLEFloat();
            rmss = instream.readLEFloat();
            snrmax = instream.readLEFloat();
            kwmax=instream.readLEInt();
            ppmax = instream.readLEDouble();
            ppmaxe = instream.readLEFloat();
            dmmax = instream.readLEFloat();
            dmmaxe = instream.readLEFloat();
            pa = instream.readLEFloat();
            pb = instream.readLEFloat();
            dma = instream.readLEFloat();
            dmb = instream.readLEFloat();
            plhd8 = instream.readString(120);
            
            
            ndm = 2*nband-1;
            nprd = 2*nsub-1;
            
            nn=ndm*nprd;
            
            instream.skipBytes(8);  //Fortran Buffer
            pdma = new float[nn];
            for (int n=0;n<nn;n++)
                pdma[n]=instream.readLEFloat();
            
            instream.skipBytes(8);  //Fortran Buffer
            nn=nsub*nbin;
            wrk = new float[nn];
            for (int j=0;j<nn;j++)
                wrk[j]=instream.readLEFloat();
            instream.skipBytes(8);  //Fortran Buffer
            
            
            
            nn=nband*nbin;
            frph = new float[nn];
            for (int j=0;j<nn;j++)
                frph[j]=instream.readLEFloat();
            instream.skipBytes(8);  //Fortran Buffer
            
            ymin = instream.readLEFloat();
            ymax = instream.readLEFloat();
            
            plhd4 = instream.readString(60);
            plhd4a = instream.readString(60);
            plhd5 = instream.readString(60);
            plhd5a = instream.readString(60);
            plhd6 = instream.readString(60);
            plhd7 = instream.readString(60);
            plad1 = instream.readString(60);
            plad2 = instream.readString(60);
            plad3 = instream.readString(60);
            plad4 = instream.readString(60);
            instream.skipBytes(8);  //Fortran Buffer
            
            
            prmax = new float[nbin];
            for (int j=0;j<nbin;j++)
                prmax[j]=instream.readLEFloat();
            
            instream.skipBytes(8);  //Fortran Buffer
            
            ndms = instream.readLEInt();
            instream.skipBytes(8);  //Fortran Buffer
            
            snlist = new float[ndms];
            for (int j=0;j<ndms;j++)
                snlist[j] = instream.readLEFloat();
            
            instream.skipBytes(8);  //Fortran Buffer
            instream.close();
        } catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("Cannot Read file "+file.getName());
        }
        read = true;
    }
    
    
    
    public double getAccel(){
        read();
        String[] dmstring = plad3.split(":");
        try{
            return Double.parseDouble(dmstring[1].split("ph")[0]);
        }catch(NumberFormatException e){
            System.err.println("Problem reading Accel from "+file.getName() +"\nGenerating fake Accel.");
            return 1000;
        }
    }
    
    public double getJerk(){
        read();
        String[] dmstring = plad4.split(":");
        return Double.parseDouble(dmstring[1].split("ph")[0]);
    }

    public String[] getPDMParams() {
        String[] retValue;
        
        retValue = super.getPDMParams();
        
        String[] newRet = new String[retValue.length+4];
        newRet[0] = plad2;
        newRet[1] = plad3;
        newRet[2] = plad4;
        newRet[3] = "---";
        
        for(int i = 0; i < retValue.length;i++){
            newRet[i+4] = retValue[i];
        }
        
        return newRet;
    }
    
    
    
    public double getReconSnr() {
       read();
        String[] snrStr = plhd3.split(":");
         if(snrStr.length >2){
            return Double.parseDouble(snrStr[3].trim().split("\\s+")[0]);
        } else {
            return 0.0;
        }
    }
    
    
}
