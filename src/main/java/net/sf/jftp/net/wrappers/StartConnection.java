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

//TODO: Add SFTP port number here (convert potmp to a string and pass it
package net.sf.jftp.net.wrappers;

import net.sf.jftp.gui.tasks.LastConnections;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FtpConnection;


// This class is used to initiate connections of all types (FTP, SFTP, SMB, NFS
// are currently supported.) Any time the user tries to open a connection using
// any protocol, this class is the intermediary between the GUI and the actual
// connection establishing classes. This puts much common functionality into
// one method (so in creating this I did some code cleanup.)
public class StartConnection {
	public static FtpConnection con = null;
	public static String keyfile = null;

	public static void setSshKeyfile(final String file) {
		keyfile = file;
	}

	public static boolean startCon(final String protocol, final String htmp, final String utmp, final String ptmp, final int potmp, final String dtmp, final boolean useLocal) {

		String conType;

		final String[] searchValue = new String[net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		final String potmpString = Integer.toString(potmp);
		String useLocalString = "false";

		if (useLocal) {
			useLocalString = "true";
		}

		//*** FTP section: deprecated
		if (protocol.equals("FTP")) {
		} else if (protocol.equals("SMB")) {
			SmbConnection con = null;

			try {
				con = new SmbConnection(htmp, dtmp, utmp, ptmp, ((ConnectionListener) net.sf.jftp.JFtp.remoteDir));

				if (useLocal) {
					net.sf.jftp.JFtp.statusP.jftp.addLocalConnection(htmp, con);

					if (con.isConnected()) {
						net.sf.jftp.JFtp.localDir.setPath("");
						net.sf.jftp.JFtp.localDir.fresh();
					}
				} else {
					net.sf.jftp.JFtp.statusP.jftp.addConnection(htmp, con);
					if (con.isConnected()) {
						net.sf.jftp.JFtp.remoteDir.setPath("");
						net.sf.jftp.JFtp.remoteDir.fresh();
					}
				}

				searchValue[0] = "SMB";
				searchValue[1] = htmp;
				searchValue[2] = utmp;

				if (net.sf.jftp.config.Settings.getStorePasswords()) {
					searchValue[3] = ptmp;
				} else {
					searchValue[3] = "";
				}

				searchValue[4] = dtmp;
				searchValue[5] = useLocalString;
				searchValue[6] = LastConnections.SENTINEL;

				updateFileMenu(searchValue);

				return true;
			} catch (final Exception ex) {
				ex.printStackTrace();
				net.sf.jftp.system.logging.Log.debug("Could not create SMBConnection, does this distribution come with jcifs?");
			}
		}

		else {
			final NfsConnection con;

			//***
			boolean status = true;

			//***
			con = new NfsConnection(htmp);

			if (!utmp.equals("<anonymous>")) {
				status = con.login(utmp, ptmp);
			}

			if (useLocal) {
				con.setLocalPath("/");
				net.sf.jftp.JFtp.statusP.jftp.addLocalConnection(htmp, con);
			} else {
				net.sf.jftp.JFtp.statusP.jftp.addConnection(htmp, con);
			}

			con.chdir(htmp);

			searchValue[0] = "NFS";
			searchValue[1] = htmp;
			searchValue[2] = utmp;

			if (net.sf.jftp.config.Settings.getStorePasswords()) {
				searchValue[3] = ptmp;
			} else {
				searchValue[3] = "";
			}

			searchValue[4] = useLocalString;
			searchValue[5] = LastConnections.SENTINEL;
			updateFileMenu(searchValue);

			return status;
		}

		return true;
	}

	public static int startFtpCon(final String htmp, final String utmp, final String ptmp, final int potmp, final String dtmp, final boolean useLocal) {
		return startFtpCon(htmp, utmp, ptmp, potmp, dtmp, useLocal, null);
	}

	//startCon
	public static int startFtpCon(final String htmp, final String utmp, final String ptmp, final int potmp, final String dtmp, final boolean useLocal, final String crlf) {
		final boolean pasv = net.sf.jftp.config.Settings.getFtpPasvMode();
		final boolean threads = net.sf.jftp.config.Settings.getEnableMultiThreading();


		final String[] searchValue = new String[net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];


		con = new FtpConnection(htmp, potmp, dtmp, crlf);


		if (useLocal) {
			net.sf.jftp.JFtp.statusP.jftp.addLocalConnection(htmp, con);
		} else {
			net.sf.jftp.JFtp.statusP.jftp.addConnection(htmp, con);
		}

		final int response = con.login(utmp, ptmp);

		//boolean isConnected = false;
		if (response == FtpConnection.LOGIN_OK) {

			final String potmpString = Integer.toString(potmp);
			String useLocalString = "false";

			if (useLocal) {
				useLocalString = "true";
			}

			searchValue[0] = "FTP";
			searchValue[1] = htmp;
			searchValue[2] = utmp;

			if (net.sf.jftp.config.Settings.getStorePasswords()) {
				searchValue[3] = ptmp;
			} else {
				searchValue[3] = "";
			}

			searchValue[4] = potmpString;
			searchValue[5] = dtmp;
			searchValue[6] = useLocalString;

			searchValue[7] = LastConnections.SENTINEL;
			updateFileMenu(searchValue);
		}

		else {
			if (useLocal) {
				net.sf.jftp.JFtp.statusP.jftp.closeCurrentLocalTab();
			} else {
				net.sf.jftp.JFtp.statusP.jftp.closeCurrentTab();
			}
		}

		return response;
	}

	//startFtpCon
	private static void updateFileMenu(final String[] searchValue) {
		final int position;

		position = LastConnections.findString(searchValue, net.sf.jftp.JFtp.CAPACITY);

		String[][] newVals = new String[net.sf.jftp.JFtp.CAPACITY][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		if (position >= 0) {

			newVals = LastConnections.moveToFront(position, net.sf.jftp.JFtp.CAPACITY);
		} else {
			newVals = LastConnections.prepend(searchValue, net.sf.jftp.JFtp.CAPACITY, true);
		}
	}

	public static int startRsyncCon(final String htmp, final String utmp, final String ptmp, final int port, final String dtmp, final String ltmp) {
		final RsyncConnection con;

		//***
		final boolean status = true;

		//***
		con = new RsyncConnection(htmp, utmp, ptmp, port, dtmp, ltmp);
		con.transfer(ltmp, htmp, dtmp, utmp ,ptmp);

		return 0;
	}
}


//StartConnection
