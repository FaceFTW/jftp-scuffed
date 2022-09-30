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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.net;


public class FtpClient {
	private String name = "ftp";
	private String password = "ftp@sourceforge.net";
	private FtpConnection connection = null;

	public FtpClient() {
	}

	public void login(final String host) {
		this.connection = new FtpConnection(host);
		this.connection.login(this.name, this.password);
	}

	public void setUsername(final String s) {
		this.name = s;
	}

	public void setPassword(final String s) {
		this.password = s;
	}

	public void disconnect() {
		if (null != this.connection) {
			this.connection.disconnect();
		}
	}

	public void cd(final String s) {
		if (null != this.connection) {
			this.connection.chdir(s);
		}
	}

	public String pwd() {
		if (null != this.connection) {
			return this.connection.getPWD();
		} else {
			return "";
		}
	}

	public void get(final String file) {
		if (null != this.connection) {
			this.connection.handleDownload(file);
		}
	}

	public void put(final String file) {
		if (null != this.connection) {
			this.connection.handleUpload(file);
		}
	}
}
