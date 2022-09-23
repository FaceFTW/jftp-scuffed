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

	public Sftp2URLConnection(URL u) {
		super(u);

		int port = u.getPort() > 0 ? u.getPort() : 22;
		connection = new Sftp2Connection(u.getHost(), "" + port, null);

		String userInfo = u.getUserInfo();

		if (userInfo != null) {
			int index = userInfo.indexOf(":");

			if (index != -1) {
				username = userInfo.substring(0, index);
				password = userInfo.substring(index + 1);
			}
		}

		net.sf.jftp.system.logging.Log.debug("Connecting...");
	}

	public void connect() throws IOException {
		loginFlag = connection.login(username, password);

		if (!loginFlag) {
			return;
		}

		connection.chdir(url.getPath());
	}

	public Sftp2Connection getSftp2Connection() {
		return connection;
	}

	public String getUser() {
		return username;
	}

	public String getPass() {
		return password;
	}

	public String getHost() {
		return url.getHost();
	}

	public int getPort() {
		int ret = url.getPort();

		if (ret <= 0) {
			return 22;
		} else {
			return ret;
		}
	}

	public boolean loginSucceeded() {
		return loginFlag;
	}

}
