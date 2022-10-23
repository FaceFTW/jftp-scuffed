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

import net.sf.jftp.config.Settings;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.I18nHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 *
 */
public class FtpURLConnection extends URLConnection {
	private FtpConnection connection;
	private String username = "ftp";
	private String password = "none@no.no";
	private int loginFlag;

	public FtpURLConnection(URL u) {
		super(u);

		int port = 0 < u.getPort() ? u.getPort() : 21;
		this.connection = new FtpConnection(u.getHost(), port, Settings.defaultDir);

		String userInfo = u.getUserInfo();

		if (null != userInfo) {
			int index = userInfo.indexOf(':');

			if (-1 != index) {
				this.username = userInfo.substring(0, index);
				this.password = userInfo.substring(index + 1);
			}
		}

		Log.debug(I18nHelper.getLogString("connecting"));
	}

	public static void main(String[] args) {
		try {
			URLConnection uc = new FtpURLConnection(new URL("ftp://ftp:pass@localhost/pub"));
			uc.connect();
		} catch (IOException ioe) {
		}
	}

	public void connect() throws IOException {
		this.loginFlag = this.connection.login(this.username, this.password);

		if (FtpConstants.LOGIN_OK != this.loginFlag) {
			return;
		}

		//System.out.println(url.getPath());
		this.connection.chdir(this.url.getPath());
	}

	public FtpConnection getFtpConnection() {
		return this.connection;
	}

	public InputStream getInputStream() throws IOException {
		return null;
	}

	public OutputStream getOutputStream() throws IOException {
		return null;
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
		int ret = this.url.getPort();

		if (0 >= ret) {
			return 21;
		} else {
			return ret;
		}
	}

	public int getLoginResponse() {
		return this.loginFlag;
	}

	public boolean loginSucceeded() {
		return FtpConstants.LOGIN_OK == this.loginFlag;
	}
}
