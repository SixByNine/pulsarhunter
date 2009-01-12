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
 * ConfigureTest.java
 *
 * Created on 03 August 2006, 09:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package pulsarhunter.jreaper;

/**
 *
 * @author mkeith
 */
public class ConfigureTest {

    public static void main(String args[]) {
        System.out.println("-----------------");
        //System.out.println("PulsarHunter Configure");


        System.out.println("Checking required Java libraries are installed");
        boolean error = false;
        try {
            System.out.print("AbsoluteLayout.jar");
            System.out.flush();
            Class absLayoutClass = Class.forName("org.netbeans.lib.awtextra.AbsoluteLayout");
            System.out.println(" - OK!");
        } catch (ClassNotFoundException e) {
            error = true;
            System.out.println();
            error("A required library is missing. Please locate 'AbsoluteLayout.jar' and place it in the lib dir");
        }

        try {
            System.out.print("CoordLib.jar");
            System.out.flush();
            Class absLayoutClass = Class.forName("coordlib.Coordinate");
            try {
                Class convertClass = Class.forName("pulsarhunter.Convert");
            } catch (ClassNotFoundException e) {
                error = true;
                System.out.println();
                error("A required library is missing. 'CoordLib.jar' Exists, but is not up-to-date. Please get a new version!");
            }

            System.out.println(" - OK!");
        } catch (ClassNotFoundException e) {
            error = true;
            System.out.println();
            error("A required library is missing. Please locate 'CoordLib.jar' and place it in the lib dir");
        }


        try {
            System.out.print("swing-layout-1.0.jar");
            System.out.flush();
            Class absLayoutClass = Class.forName("org.jdesktop.layout.GroupLayout");
            System.out.println(" - OK!");
        } catch (ClassNotFoundException e) {
            error = true;
            System.out.println();
            error("A required library is missing. Please locate 'swing-layout-1.0.jar' and place it in the lib dir");
        }

        try {
            System.out.print("BookKeeprXml.jar");
            System.out.flush();
            Class xmlClass = Class.forName("bookkeepr.xml.XMLAble");
            System.out.println(" - OK!");
        } catch (ClassNotFoundException e) {
            error = true;
            System.out.println();
            error("A required library is missing. Please locate 'BookKeeprXml.jar' and place it in the lib dir");
        }

        try {
            System.out.print("EPN.jar");
            System.out.flush();
            Class absLayoutClass = Class.forName("epn.EPNFile");
            System.out.println(" - OK!");
        } catch (ClassNotFoundException e) {
            error = true;
            System.out.println();
            error("A required library is missing. Please locate 'EPN.jar' and place it in the lib dir");
        }


        System.out.println("");

        try {
            System.out.print("Testing Jreaper v" + JReaper.VERSION);
            System.out.flush();
            System.out.println(" - OK!");
        } catch (java.lang.InternalError err) {
            err.printStackTrace();
            error("Cannot activate graphical display... Check your DISPLAY variable, etc.");
        }


        if (error) {
            System.exit(10);
        }
        System.out.println("\n\nTests finished... OK!\nExiting.");
        System.exit(0);

    }

    public static void error(String messg) {
        System.out.println("\n=======\nERROR!");
        System.out.println(messg);
        System.out.println("\n=======");
    //System.exit(10);
    }
}
