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

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.UIUtils;
import net.sf.jftp.net.wrappers.StartConnection;

import javax.swing.*;


//***
public class BookmarkItem extends JMenuItem {
	private String host = "localhost";
	private String user = "anonymous";
	private String pass = "j-ftp@sourceforge.net";
	private String protocol = "FTP";
	private int port = 21;
	private String dirOrDom = "/";
	private boolean useLocal = false;

	public BookmarkItem(final String host) {
		super(host);
		this.host = host;
	}

	public void setProtocol(final String proto) {
		protocol = proto;
		this.setLabel(proto + ": " + this.getLabel());
	}

	public void setDirectory(final String dir) {
		dirOrDom = dir;
	}

	public void setPort(final int p) {
		port = p;
	}

	public void setLocal(final boolean local) {
		useLocal = local;
	}

	public void setUserdata(final String u, final String p) {
		user = u;
		pass = p;
	}

	public void connect() {
		if (protocol.equals("FTP")) {
			if (pass.equals(Settings.hiddenPassword)) {
				pass = UIUtils.getPasswordFromUser(JFtp.statusP.jftp);
			}

			final int i = StartConnection.startFtpCon(host, user, pass, port, dirOrDom, useLocal);

			if (i < 0) {
				pass = Settings.hiddenPassword;
			}

		} else {
			if (pass.equals(Settings.hiddenPassword)) {
				pass = UIUtils.getPasswordFromUser(JFtp.statusP.jftp);
			}

			final boolean ok = StartConnection.startCon(protocol, host, user, pass, port, dirOrDom, useLocal);

			if (!ok) {
				pass = Settings.hiddenPassword;
			}
		}
	}
}
