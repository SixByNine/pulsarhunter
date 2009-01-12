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
 * StandardInputStreamGenerator.java
 *
 * Created on 31 August 2005, 15:07
 */

package pulsarhunter.jreaper.pmsurv;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author mkeith
 */
public class StandardInputStreamGenerator implements InputStreamGenerator{
    
    /** Creates a new instance of StandardInputStreamGenerator */
    public StandardInputStreamGenerator() {
    }
    
    
    public java.io.InputStream getInputStream(File file) throws IOException {
        return new java.io.FileInputStream(file);
    }
}
