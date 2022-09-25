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

	public static void setSshKeyfile(String file) {
		keyfile = file;
	}

	//data sent to startCon: protocol: (ie. FTP, SFTP, etc.)
	//                       htmp: hostname
	//                       utmp: username
	//                       ptmp: password
	//	                 potmp: port
	//			 dtmp: domain
	// (null data is sent if it is ever not applicable)
	//maybe it should just take an array of strings instead? (What's
	//stored in the array can then be determined by reading the 1st
	//entry, which is the protocol name)
	public static boolean startCon(String protocol, String htmp, String utmp, String ptmp, int potmp, String dtmp, boolean useLocal) {
		// I may need a series of If statements following this one
		// declaring con for each of the possible types of connections
		//FtpConnection con = null;
		// ie.: if (protocol=="SFTP) SftpConnection con = null;
		//NfsConnection con = null;
		String conType; // this is for error msgs: may not be needed

		String[] searchValue = new String[net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		Integer potmpInt = potmp;

		String potmpString = potmpInt.toString();
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

				//JFtp.statusP.jftp.addConnection(htmp, con);
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

				//JFtp.remoteDir.setCon(con);
				//con.setLocalPath(JFtp.localDir.getCon().getPWD());
				//con.addConnectionListener((ConnectionListener) JFtp.localDir);
				//con.addConnectionListener((ConnectionListener) JFtp.remoteDir);
				//JFtp.remoteDir.fresh();
				//BUGFIX
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

				//searchValue = "SMB " + htmp + " " + utmp + " " +
				//      ptmp + " " + dtmp + " " + useLocalString;
				updateFileMenu(searchValue);

				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				net.sf.jftp.system.logging.Log.debug("Could not create SMBConnection, does this distribution come with jcifs?");
			}
		}

		//can assume any other connection is NFS for now
		else {
			NfsConnection con;

			//***
			boolean status = true;

			//***
			con = new NfsConnection(htmp);

			//JFtp.remoteDir.setCon(con);
			//con.addConnectionListener(((ConnectionListener)JFtp.remoteDir));
			//JFtp.statusP.jftp.addConnection(htmp, con);
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

			//con.setLocalPath(JFtp.localDir.getCon().getPWD());
			//con.addConnectionListener((ConnectionListener) JFtp.localDir);
			//BUGFIX
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

			//searchValue = "NFS " + htmp + " " + utmp + " " +
			//              ptmp + " " + useLocalString;
            /*
            searchValue = "NFS " + htmp + " " + utmp + " " +
                          ptmp + " " + potmpString + " " + dtmp
                          + " " + useLocalString;
            */
			updateFileMenu(searchValue);

			return status;
		}

        /*
        //Common functionality goes here

        try {
                //if statement here, select connection type (or should
                // a superclass be used?)
                con = new NfsConnection(htmp);

                JFtp.statusP.jftp.addConnection(htmp, con);
        } catch (Exception ex) {
                Log.debug("Could not create " + protocol + "Connection!");
        }

        */

		//return true only if all has executed as expected
		return true;
	}

	public static int startFtpCon(String htmp, String utmp, String ptmp, int potmp, String dtmp, boolean useLocal) {
		return startFtpCon(htmp, utmp, ptmp, potmp, dtmp, useLocal, null);
	}

	//startCon
	public static int startFtpCon(String htmp, String utmp, String ptmp, int potmp, String dtmp, boolean useLocal, String crlf) {
		boolean pasv = net.sf.jftp.config.Settings.getFtpPasvMode();
		boolean threads = net.sf.jftp.config.Settings.getEnableMultiThreading();

		//BUGFIX: this is now an array
		//String searchValue = new String("");
		String[] searchValue = new String[net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		//BELOW IS GUI-related stuff... should put in separate
		//method in GUI dir?
        /*
        if(!pasv && threads)
        { // only passive ftp threading works
            //Settings.setProperty("jftp.enableMultiThreading", false);

            JDialog j = new JDialog();
            j.setTitle("Warning");
            j.setLocation(150, 150);
            j.setSize(450, 100);
            j.getContentPane().add(new JLabel(" Multithreading in active mode is EXPERIMENTAL"));
            j.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            j.setModal(true);
            j.show();
        }
        */
		//***also need to get max connections!!
		con = new FtpConnection(htmp, potmp, dtmp, crlf);

		//con.addConnectionListener((ConnectionListener) JFtp.localDir);
		//con.addConnectionListener((ConnectionListener) JFtp.remoteDir);
		//JFtp.remoteDir.setCon(con);
		if (useLocal) {
			net.sf.jftp.JFtp.statusP.jftp.addLocalConnection(htmp, con);
		} else {
			net.sf.jftp.JFtp.statusP.jftp.addConnection(htmp, con);
		}

		//System.out.println("**" + htmp + potmp + dtmp);
		//System.out.println("**" + utmp + ptmp);
		int response = con.login(utmp, ptmp);

		//boolean isConnected = false;
		if (response == FtpConnection.LOGIN_OK) {
			//System.out.println(htmp + " " + potmp + " " + dtmp);

            /*
            if (useLocal)
                    JFtp.statusP.jftp.addLocalConnection(
                            htmp, con);
            else
                    JFtp.statusP.jftp.addConnection(htmp, con);

             */

			//String searchValue = new String("");
			Integer potmpInt = potmp;

			String potmpString = potmpInt.toString();
			String useLocalString = "false";

			if (useLocal) {
				useLocalString = "true";
			}

			//BUGFIX
            /*
            searchValue = "FTP " + htmp + " " + utmp + " " +
                          ptmp + " " + potmpString + " " + dtmp
                          + " " + useLocalString;
            */
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

		//*** JUST FOR NOW: if not connected, take out the tabs
		else {
			if (useLocal) {
				net.sf.jftp.JFtp.statusP.jftp.closeCurrentLocalTab();
			} else {
				net.sf.jftp.JFtp.statusP.jftp.closeCurrentTab();
			}
		}

		return response; //*** code that indicates successful connection
	}

	//startFtpCon
	private static void updateFileMenu(String[] searchValue) {
		int position;

		position = LastConnections.findString(searchValue, net.sf.jftp.JFtp.CAPACITY);

		//bugfix: now a 2D array
		String[][] newVals = new String[net.sf.jftp.JFtp.CAPACITY][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		if (position >= 0) {
			//System.out.println(JFtp.CAPACITY);
			//System.out.println("IT WAS FOUND");
			//System.out.println(position);
			newVals = LastConnections.moveToFront(position, net.sf.jftp.JFtp.CAPACITY);
		} else {
			//System.out.println("IT WASN'T FOUND");
			newVals = LastConnections.prepend(searchValue, net.sf.jftp.JFtp.CAPACITY, true);
		}
	}

	public static int startRsyncCon(String htmp, String utmp, String ptmp, int port, String dtmp, String ltmp) {
		RsyncConnection con;

		//***
		boolean status = true;

		//***
		con = new RsyncConnection(htmp, utmp, ptmp, port, dtmp, ltmp);
//		con.start();
		return 0;
	}
}


//StartConnection
