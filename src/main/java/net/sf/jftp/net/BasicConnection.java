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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


/**
 * Interface for all connection types
 */
public interface BasicConnection {
	boolean hasUploaded = false;

	void sendRawCommand(String cmd);

	//public boolean login(String user, String pass);
	void disconnect();

	boolean isConnected();

	String getPWD();

	boolean cdup();

	boolean mkdir(String dirName);

	void list() throws IOException;

	boolean chdir(String p);

	boolean chdirNoRefresh(String p);

	String getLocalPath();

	boolean setLocalPath(String newPath);

	String[] sortLs();

	String[] sortSize();

	Date[] sortDates();

	int[] getPermissions();

	int handleDownload(String file);

	int handleUpload(String file);

	int download(String file);

	int upload(String file);

	int upload(String file, InputStream in);

	InputStream getDownloadInputStream(String file);

	int removeFileOrDir(String file);

	void addConnectionListener(ConnectionListener listener);

	void setConnectionListeners(java.util.List<ConnectionListener> listeners);

	boolean rename(String from, String to);
}
