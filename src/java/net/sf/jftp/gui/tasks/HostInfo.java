/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.gui.tasks;


/* Incapsulate all the information of the current connection */
public class HostInfo {
    public String type;

    // parameter used for ftp
    public String hostname;
    public String username;
    public String password;
    public String port;

    // local path + filename
    // public String local;
    // remote path + filename
    // public String remote;
    // used only for smb
    public String localip;
    public String domain;

    public HostInfo() {
    }
}
