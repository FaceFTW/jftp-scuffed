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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 *
 */
public class FtpURLConnection extends URLConnection {
    private FtpConnection connection = null;
    private String username = "ftp";
    private String password = "none@no.no";
    private int loginFlag = 0;

    public FtpURLConnection(URL u) {
        super(u);

        int port = u.getPort() > 0 ? u.getPort() : 21;
        connection = new FtpConnection(u.getHost(), port, Settings.defaultDir);

        String userInfo = u.getUserInfo();

        if (userInfo != null) {
            int index = userInfo.indexOf(":");

            if (index != -1) {
                username = userInfo.substring(0, index);
                password = userInfo.substring(index + 1);
            }
        }

        Log.debug("Connecting...");
    }

    public static void main(String[] args) {
        try {
            URLConnection uc = new FtpURLConnection(new URL("ftp://ftp:pass@localhost/pub"));
            uc.connect();
        } catch (IOException ioe) {
        }
    }

    public void connect() throws IOException {
        loginFlag = connection.login(username, password);

        if (loginFlag != FtpConnection.LOGIN_OK) {
            return;
        }

        //System.out.println(url.getPath());
        connection.chdir(url.getPath());
    }

    public FtpConnection getFtpConnection() {
        return connection;
    }

    public InputStream getInputStream() throws IOException {
        return null;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
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
            return 21;
        } else {
            return ret;
        }
    }

    public int getLoginResponse() {
        return loginFlag;
    }

    public boolean loginSucceeded() {
        return loginFlag == FtpConnection.LOGIN_OK;
    }
}
