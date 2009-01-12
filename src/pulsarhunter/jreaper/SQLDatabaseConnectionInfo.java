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
 * SQLDatabaseConnectionInfo.java
 *
 * Created on 02 August 2006, 15:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pulsarhunter.jreaper;

/**
 *
 * @author mkeith
 */
public class SQLDatabaseConnectionInfo {
    private String url;
    private String username;
    private String password;
    /** Creates a new instance of SQLDatabaseConnectionInfo */
    public SQLDatabaseConnectionInfo(String url, String username, String password) {
        this.setUrl(url);
        this.setPassword(password);
        this.setUsername(username);
    }
    
    public String getUrl() {
        return url;
    }
    
    
    public String getUsername() {
        return username;
    }
    
    
    public String getPassword() {
        return password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    
}
