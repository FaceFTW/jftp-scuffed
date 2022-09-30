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
package net.sf.jftp.net.wrappers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;


public class Sftp2URLConnection extends URLConnection {
	private Sftp2Connection connection = null;
	private String username = "user";
	private String password = "none@no.no";
	private boolean loginFlag = false;

	public Sftp2URLConnection(final URL u) {
		super(u);

		final int port = u.getPort() > 0 ? u.getPort() : 22;
		this.connection = new Sftp2Connection(u.getHost(), "" + port, null);

		final String userInfo = u.getUserInfo();

		if (userInfo != null) {
			final int index = userInfo.indexOf(":");

			if (index != -1) {
				this.username = userInfo.substring(0, index);
				this.password = userInfo.substring(index + 1);
			}
		}

		net.sf.jftp.system.logging.Log.debug("Connecting...");
	}

	public void connect() throws IOException {
		this.loginFlag = this.connection.login(this.username, this.password);

		if (!this.loginFlag) {
			return;
		}

		this.connection.chdir(this.url.getPath());
	}

	public Sftp2Connection getSftp2Connection() {
		return this.connection;
	}

	public String getUser() {
		return this.username;
	}

	public String getPass() {
		return this.password;
	}

	public String getHost() {
		return this.url.getHost();
	}

	public int getPort() {
		final int ret = this.url.getPort();

		if (ret <= 0) {
			return 22;
		} else {
			return ret;
		}
	}

	public boolean loginSucceeded() {
		return this.loginFlag;
	}

}
