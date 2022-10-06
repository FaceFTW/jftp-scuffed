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

//NOTE TO SELF: use setModal here somewhere?
package net.sf.jftp.gui.tasks;

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HComboBox;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HPasswordField;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.net.wrappers.StartConnection;
import net.sf.jftp.system.StringUtils;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class AddBookmarks extends HFrame implements ActionListener, WindowListener {
	private static net.sf.jftp.JFtp jftp;
	//public JComboBox protocols = new JComboBox();
	private final HComboBox protocols = new HComboBox("Protocol:");
	private final HTextField host = new HTextField("Hostname:", "localhost");
	private final HTextField user = new HTextField("Username:", "anonymous");
	private final HPasswordField pass = new HPasswordField("Password:", "none@nowhere.no");
	private final HTextField port = new HTextField("Port:    ", "21");
	private final HTextField dirOrDom = new HTextField("Directory/Domain:    ", "");
	private final HComboBox isLocal = new HComboBox("Local Connection:");
	private final HButton add = new HButton("Add Bookmark");
	private final HButton addAndConnect = new HButton("Add Bookmark and Connect to Server");

	public AddBookmarks(ComponentListener l, net.sf.jftp.JFtp jftp) {
		super();

		AddBookmarks.jftp = jftp;
		this.init();
	}

	public AddBookmarks(net.sf.jftp.JFtp jftp) {
		super();
		AddBookmarks.jftp = jftp;
		this.init();
	}

	private void init() {
		this.setSize(650, 400);
		this.setLocation(50, 150);
		this.setTitle("Add Bookmarks...");

		//setBackground(okP.getBackground());
		this.getContentPane().setLayout(new GridLayout(8, 1));

		this.getContentPane().add(this.protocols);
		this.getContentPane().add(this.host);
		this.getContentPane().add(this.port);

		this.getContentPane().add(this.user);
		this.getContentPane().add(this.pass);

		this.getContentPane().add(this.dirOrDom);

		this.getContentPane().add(this.isLocal);

		Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 5, 20));

		this.getContentPane().add(buttonPanel);
		buttonPanel.add(this.add);
		buttonPanel.add(this.addAndConnect);


		this.add.addActionListener(this);
		this.addAndConnect.addActionListener(this);

		this.protocols.setEditable(false);
		this.protocols.addItem("FTP");
		this.protocols.addItem("SFTP");
		this.protocols.addItem("SMB");
		this.protocols.addItem("NFS");

		this.protocols.addActionListener(this);

		this.isLocal.setEditable(false);
		this.isLocal.addItem("No");
		this.isLocal.addItem("Yes");
	}

	public void update() {
		this.setVisible(true);
		this.toFront();
	}

	public void windowClosing(WindowEvent e) {
		this.dispose();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.add) {
			this.getData(false);
		} else if (e.getSource() == this.addAndConnect) {
			this.getData(true);
		}

		//FOR NOW: only other possible event is update of the protocol ComboBox
		else {
			String s = "";

			//s = (String) protocols.getSelectedItem()
			s = this.protocols.getSelectedItem().toString();
			System.out.println(s);

			//need to put information on what each protocol accepts
			//elsewhere to make it universally accessible to all routines
			//that need this info?
			switch (s) {
				case "FTP":
					this.port.setEnabled(true);
					this.user.setEnabled(true);
					this.pass.setEnabled(true);
					this.dirOrDom.setEnabled(true);
					break;
				case "SFTP":
					this.port.setEnabled(true);
					this.user.setEnabled(true);
					this.pass.setEnabled(true);
					this.dirOrDom.setEnabled(false);
					break;
				case "SMB":
					this.port.setEnabled(false);
					this.user.setEnabled(true);
					this.pass.setEnabled(true);
					this.dirOrDom.setEnabled(true);
					break;

				//can assume this is NFS for now
				default:
					this.port.setEnabled(false);
					this.user.setEnabled(true);
					this.pass.setEnabled(true);
					this.dirOrDom.setEnabled(false);
					break;
			}

		}

		//OK event
	}

	//actionPerformed
	private void getData(boolean andConnect) {
		String protocoltmp = "";

		protocoltmp = this.protocols.getSelectedItem().toString();

		System.out.println("test");

		//what happens next: get data on protocol, and based on the data
		//for the protocol, get what's needed for each field.
		//the data extraction from each field can actually be put in a
		//separate private method which could also be accessed if the
		//connection is to be initiated here
		//then, need a routine to open the bookmark file, then append it
		//to the end (add a blank line, then the data, separated by the
		//hash/sharp (#) comment) (and should it be here, or should only
		//one class know about it. Or maybe it can just refer to the config
		//file
		//don't need to get  all this, it depends on the protocol
		//***IMPORTANT NOTE!!!!: LAN BROWSING SHOULD BE SPEAPRTE PROTOCOL!
		//ALSO IMPORTANT: can refer to JFtp.hostinfo.hostname, for current
		//hostname, JFtp.hostinfo.type for prortocol, etc... refer to
		//code in HostChooser.java that says this, but might not be the
		//case for SFTP
		//FTP: get all of it
		//SFTP: All but dirOrDom? (SFTP doesn;t seem to support that at this time)
		//SMB: Don't get port
		//NFS: Don;t get port or dirOrDom
		//FOR NOW: just get all data

		String htmp = this.checkIfEmpty(StringUtils.cut(this.host.getText(), " "));
		String utmp = this.checkIfEmpty(StringUtils.cut(this.user.getText(), " "));
		String ptmp = this.checkIfEmpty(StringUtils.cut(this.pass.getText(), " "));
		String potmp = this.checkIfEmpty(StringUtils.cut(this.port.getText(), " "));

		String dirOrDomtmp = this.checkIfEmpty(StringUtils.cut(this.dirOrDom.getText(), " "));

		String local = "";

		int potmpint = new Integer(potmp);

		local = this.isLocal.getSelectedItem().toString();

		String localString = "";

		boolean localtmp = false;

		if (local.equals("true")) {
			localtmp = true;
			localString = "true";
		} else {
			localtmp = false;
			localString = "false";
		}

		String addedString = "";

		addedString += (protocoltmp + "#");
		addedString += (htmp + "#");
		addedString += (utmp + "#");

		addedString += (ptmp + "#");
		addedString += (potmp + "#");
		addedString += (dirOrDomtmp + "#");
		addedString += localString;

		//here, start the routine for appending the line to the bookmark
		//file (SHOULD IT BE IN A SEPARATE ROUTINE?)
		try {
			FileOutputStream fos;
			PrintStream out;

			fos = new FileOutputStream(Settings.bookmarks, true);
			out = new PrintStream(fos);

			//commented out, just for now
			out.println(addedString);
			out.println();

			//if it worked, update the menu
			net.sf.jftp.JFtp.menuBar.loadBookmarks();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (andConnect) {
			if (protocoltmp.equals("FTP")) {
				StartConnection.startFtpCon(htmp, utmp, ptmp, potmpint, dirOrDomtmp, localtmp);
			} else {
				StartConnection.startCon(protocoltmp, htmp, utmp, ptmp, potmpint, dirOrDomtmp, localtmp);
			}
		}

		this.dispose();

		net.sf.jftp.JFtp.mainFrame.setVisible(true);
		net.sf.jftp.JFtp.mainFrame.toFront();
	}

	private String checkIfEmpty(String value) {
		final String retVal = "0";

		if (value.isEmpty()) {
			return retVal;
		} else {
			return value;
		}
	}

}
