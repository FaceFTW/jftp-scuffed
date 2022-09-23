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
package net.sf.jftp.net;


/**
 * Classes that implement this interface are notified about various
 * events that may happen. Make sure to use myftpconnection.addConnectionListener(this) in
 * the class to get it working.
 */
public interface ConnectionListener {
	/**
	 * Called if the remote directory has changed by a chdir() or a finished upload for example.
	 */
	void updateRemoteDirectory(BasicConnection con);

	/**
	 * Called every n bytes, where n is defined by Settings
	 */
	void updateProgress(String file, String type, long bytes);

	void connectionInitialized(BasicConnection con);

	void connectionFailed(BasicConnection con, String why);

	void actionFinished(BasicConnection con);
}
