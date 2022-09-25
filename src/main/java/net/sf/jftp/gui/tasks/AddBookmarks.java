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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class AddBookmarks extends net.sf.jftp.gui.framework.HFrame implements ActionListener, WindowListener {
	private static net.sf.jftp.JFtp jftp;
	private final net.sf.jftp.gui.framework.HButton add = new net.sf.jftp.gui.framework.HButton("Add Bookmark");
	private final net.sf.jftp.gui.framework.HButton addAndConnect = new net.sf.jftp.gui.framework.HButton("Add Bookmark and Connect to Server");

	//public JComboBox protocols = new JComboBox();
	public final net.sf.jftp.gui.framework.HComboBox protocols = new net.sf.jftp.gui.framework.HComboBox("Protocol:");
	public final net.sf.jftp.gui.framework.HTextField host = new net.sf.jftp.gui.framework.HTextField("Hostname:", "localhost");
	public final net.sf.jftp.gui.framework.HTextField user = new net.sf.jftp.gui.framework.HTextField("Username:", "anonymous");
	public final net.sf.jftp.gui.framework.HPasswordField pass = new net.sf.jftp.gui.framework.HPasswordField("Password:", "none@nowhere.no");
	public final net.sf.jftp.gui.framework.HTextField port = new net.sf.jftp.gui.framework.HTextField("Port:    ", "21");
	public final net.sf.jftp.gui.framework.HTextField dirOrDom = new net.sf.jftp.gui.framework.HTextField("Directory/Domain:    ", "");

	//public JComboBox isLocal = new JComboBox();
	public final net.sf.jftp.gui.framework.HComboBox isLocal = new net.sf.jftp.gui.framework.HComboBox("Local Connection:");

	//private ActionListener protocolListener = new ActionListener();
	//private FlowLayout fl = new FlowLayout(FlowLayout.RIGHT, 10, 5);
	public AddBookmarks(ComponentListener l, net.sf.jftp.JFtp jftp) {
		//listener = l;
		AddBookmarks.jftp = jftp;
		init();
	}

	public AddBookmarks(net.sf.jftp.JFtp jftp) {
		AddBookmarks.jftp = jftp;
		init();
	}

	public void init() {
		setSize(650, 400);
		setLocation(50, 150);
		setTitle("Add Bookmarks...");

		//setBackground(okP.getBackground());
		getContentPane().setLayout(new GridLayout(8, 1));

		getContentPane().add(protocols);
		getContentPane().add(host);
		getContentPane().add(port);

		getContentPane().add(user);
		getContentPane().add(pass);

		getContentPane().add(dirOrDom);

		getContentPane().add(isLocal);

		//getContentPane().add(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		//getContentPane().add(fl);
		//fl.add(add);
		Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 5, 20));

		getContentPane().add(buttonPanel);
		buttonPanel.add(add);
		buttonPanel.add(addAndConnect);

        /*
        getContentPane().add(add);
        getContentPane().add(addAndConnect);
        */
		add.addActionListener(this);
		addAndConnect.addActionListener(this);

		//port.setEnabled(false);
		protocols.setEditable(false);
		protocols.addItem("FTP");
		protocols.addItem("SFTP");
		protocols.addItem("SMB");
		protocols.addItem("NFS");

		protocols.addActionListener(this);

		//protocols.addActionListener();
		isLocal.setEditable(false);
		isLocal.addItem("No");
		isLocal.addItem("Yes");
	}

	public void update() {
		setVisible(true);
		toFront();
	}

	public void windowClosing(WindowEvent e) {
		//System.exit(0);
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
		if (e.getSource() == add) {
			getData(false);
		} else if (e.getSource() == addAndConnect) {
			getData(true);
		}

		//FOR NOW: only other possible event is update of the protocol ComboBox
		else {
			String s = "";

			//s = (String) protocols.getSelectedItem()
			s = protocols.getSelectedItem().toString();
			System.out.println(s);

			//need to put information on what each protocol accepts
			//elsewhere to make it universally accessible to all routines
			//that need this info?
			if (s.equals("FTP")) {
				port.setEnabled(true);
				user.setEnabled(true);
				pass.setEnabled(true);
				dirOrDom.setEnabled(true);
			} else if (s.equals("SFTP")) {
				port.setEnabled(true);
				user.setEnabled(true);
				pass.setEnabled(true);
				dirOrDom.setEnabled(false);
			} else if (s.equals("SMB")) {
				port.setEnabled(false);
				user.setEnabled(true);
				pass.setEnabled(true);
				dirOrDom.setEnabled(true);
			}

			//can assume this is NFS for now
			else {
				port.setEnabled(false);
				user.setEnabled(true);
				pass.setEnabled(true);
				dirOrDom.setEnabled(false);
			}

			//System.out.println("yes!");
		}

		//OK event
	}

	//actionPerformed
	private void getData(boolean andConnect) {
		String protocoltmp = "";

		//here, get the selected protocol
		//s = (String) protocols.getSelectedItem()
		protocoltmp = protocols.getSelectedItem().toString();

		//System.out.println(s);
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

        /*
        String htmp = StringUtils.cut(host.getText(), " ");
        String utmp = StringUtils.cut(user.getText(), " ");
        String ptmp = StringUtils.cut(pass.getText(), " ");
        String potmp = StringUtils.cut(port.getText(), " ");

        String dirOrDomtmp = StringUtils.cut(dirOrDom.getText(), " ");
        */
		String htmp = checkIfEmpty(net.sf.jftp.system.StringUtils.cut(host.getText(), " "));
		String utmp = checkIfEmpty(net.sf.jftp.system.StringUtils.cut(user.getText(), " "));
		String ptmp = checkIfEmpty(net.sf.jftp.system.StringUtils.cut(pass.getText(), " "));
		String potmp = checkIfEmpty(net.sf.jftp.system.StringUtils.cut(port.getText(), " "));

		String dirOrDomtmp = checkIfEmpty(net.sf.jftp.system.StringUtils.cut(dirOrDom.getText(), " "));

		String local = "";

		Integer potmpInt = new Integer(potmp);
		int potmpint = potmpInt.intValue();

		local = isLocal.getSelectedItem().toString();

		String localString = "";

		boolean localtmp = false;

		if (local.equals("true")) {
			localtmp = true;
			localString = "true";
		} else {
			localtmp = false;
			localString = "false";
		}

		//first, build the string which is to be appended to end
		//of bookmark file
		// protocol#host#user#password#port#dir/domain#local
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

			fos = new FileOutputStream(net.sf.jftp.config.Settings.bookmarks, true);
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
				net.sf.jftp.net.wrappers.StartConnection.startFtpCon(htmp, utmp, ptmp, potmpint, dirOrDomtmp, localtmp);
			} else {
				net.sf.jftp.net.wrappers.StartConnection.startCon(protocoltmp, htmp, utmp, ptmp, potmpint, dirOrDomtmp, localtmp);
			}
		}

		//else {
		//}
		this.dispose();

		//this.setVisible(false);
		net.sf.jftp.JFtp.mainFrame.setVisible(true);
		net.sf.jftp.JFtp.mainFrame.toFront();
	}

	//getData
	private String checkIfEmpty(String value) {
		String retVal = "0";

		if (value.equals("")) {
			return retVal;
		} else {
			return value;
		}
	}

	//checkIfEmpty
}
