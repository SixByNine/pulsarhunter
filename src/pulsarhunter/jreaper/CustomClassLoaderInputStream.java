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
 * CustomClassLoaderInputStream.java
 *
 * Created on 27 May 2005, 14:01
 */

package pulsarhunter.jreaper;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 *
 * @author mkeith
 */
public class CustomClassLoaderInputStream extends ObjectInputStream{
    
    
    ClassLoader cl;
    /** Creates a new instance of CustomClassLoaderInputStream */
    public CustomClassLoaderInputStream(ClassLoader cl,InputStream instream) throws IOException {
        super(instream);
        this.cl = cl;
    }
    
    
    protected Class<?> resolveClass(ObjectStreamClass desc)
	throws IOException, ClassNotFoundException
    {
	String name = desc.getName();
	return Class.forName(name, false, cl);
	
    }
    
}
