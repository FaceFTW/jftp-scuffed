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
package net.sf.jftp.gui.hostchooser;

import net.miginfocom.swing.MigLayout;
import net.sf.jftp.JFtp;
import net.sf.jftp.config.LoadSet;
import net.sf.jftp.config.SaveSet;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HInsetPanel;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HPasswordField;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.net.wrappers.SmbConnection;
import net.sf.jftp.net.wrappers.StartConnection;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class SmbHostChooser extends HFrame implements ActionListener, WindowListener {
	public static HTextField host = new HTextField("URL:", "smb://localhost/");
	public static HTextField user = new HTextField("Username:", "guest");
	public static HPasswordField pass = new HPasswordField("Password:", "nopasswd");
	private final HButton ok = new HButton("Connect");
	public HTextField domain = new HTextField("Domain:    ", "WORKGROUP");
	public HTextField broadcast = new HTextField("Broadcast IP:    ", "AUTO");
	public HTextField wins = new HTextField("WINS Server IP:    ", "NONE");

	public JComboBox ip = new JComboBox();
	JCheckBox lan = new JCheckBox("Browse LAN", true);
	private ComponentListener listener = null;
	private boolean useLocal = false;

	public SmbHostChooser(ComponentListener l, boolean local) {
		listener = l;
		useLocal = local;
		init();
	}

	public SmbHostChooser(ComponentListener l) {
		listener = l;
		init();
	}

	public SmbHostChooser() {
		init();
	}

	public void init() {
		setPreferredSize(new Dimension(500, 320));
		setLocation(100, 150);
		setTitle("Smb Connection...");
		setBackground(new HPanel().getBackground());

		try {
			File f = new File(Settings.appHomeDir);
			f.mkdir();

			File f1 = new File(Settings.login);
			f1.createNewFile();

			File f2 = new File(Settings.login_def_smb);
			f2.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		String[] login = LoadSet.loadSet(Settings.login_def_smb);

		if ((login[0] != null) && (login.length > 1)) {
			host.setText(login[0]);
			user.setText(login[1]);
			domain.setText(login[5]);
		}


		if (Settings.getStorePasswords()) {
			if ((login != null) && (login.length > 2) && (login[2] != null)) {
				pass.setText(login[2]);
			}
		} else {
			pass.setText("");
		}

		ip.setEditable(true);

		HInsetPanel root = new HInsetPanel();
		root.setLayout(new MigLayout());

		root.add(host);
		root.add(lan, "wrap");
		root.add(user);
		root.add(pass, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(ip);
		root.add(domain, "wrap");

		root.add(broadcast);
		root.add(wins, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(new JLabel(" "));
		root.add(ok, "align right");

		ok.addActionListener(this);

		host.setEnabled(!lan.isSelected());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		lan.addActionListener(this);
		pass.text.addActionListener(this);

		getContentPane().setLayout(new BorderLayout(3, 3));
		getContentPane().add("Center", root);

		JTextArea t = new JTextArea() {
			public Insets getInsets() {
				return new Insets(7, 7, 7, 7);
			}
		};
		t.setLineWrap(true);
		t.setText("Note: Please use URL format \"smb://host/\"");

		getContentPane().add("North", t);

		setModal(false);
		setVisible(false);
		addWindowListener(this);

		ip.addItem("<default>");

		try {
			Enumeration e = NetworkInterface.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				Enumeration f = ((NetworkInterface) e.nextElement()).getInetAddresses();

				while (f.hasMoreElements()) {
					ip.addItem(((InetAddress) f.nextElement()).getHostAddress());
				}
			}
		} catch (Exception ex) {
			Log.debug("Error determining default network interface: " + ex);

			//ex.printStackTrace();
		}

		//setBCast();
		domain.setEnabled(false);
		broadcast.setEnabled(false);
		wins.setEnabled(false);

		ip.addActionListener(this);

		pack();
	}

	private void setBCast() {
		try {
			String tmp = ((String) ip.getSelectedItem()).trim();
			String x = tmp.substring(0, tmp.lastIndexOf(".") + 1) + "255";
			broadcast.setText(x);
		} catch (Exception ex) {
			Log.out("Error (SMBHostChooser): " + ex);
		}
	}

	public void update() {
		fixLocation();
		setVisible(true);
		toFront();
		user.requestFocus();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == lan) {
			host.setEnabled(!lan.isSelected());
		} else if (e.getSource() == ip) {
			if (ip.getSelectedItem().equals("<default>")) {
				domain.setEnabled(false);
				broadcast.setEnabled(false);
				wins.setEnabled(false);
			} else {
				domain.setEnabled(true);
				broadcast.setEnabled(true);
				wins.setEnabled(true);
				setBCast();
			}
		} else if ((e.getSource() == ok) || (e.getSource() == pass.text)) {
			// Switch windows
			//this.setVisible(false);
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			SmbConnection con = null;

			//System.out.println(jcifs.Config.getProperty("jcifs.smb.client.laddr"));
			String tmp = ((String) ip.getSelectedItem()).trim();

			if (!tmp.equals("") && !tmp.equals("<default>")) {
				String x = tmp.trim().substring(0, tmp.lastIndexOf(".") + 1) + "255";
				String bcast = broadcast.getText().trim();

				if (!bcast.equals("AUTO")) {
					x = bcast;
				}

				Log.debug("Setting LAN interface to: " + tmp + "/" + x);
				jcifs.Config.setProperty("jcifs.netbios.laddr", tmp);
				jcifs.Config.setProperty("jcifs.smb.client.laddr", tmp);
				jcifs.Config.setProperty("jcifs.netbios.baddr", x);

				String y = wins.getText().trim();

				if (!y.equals("NONE")) {
					Log.debug("Setting WINS server IP to: " + y);
					jcifs.Config.setProperty("jcifs.netbios.wins", y);
				}
			}

			//System.out.println(jcifs.Config.getProperty("jcifs.smb.client.laddr"));
			//JFtp.setHost(host.getText());
			String htmp = host.getText().trim();
			String utmp = user.getText().trim();
			String ptmp = pass.getText();
			String dtmp = domain.getText().trim();

			//***
			//if(dtmp.equals("")) dtmp = null;
			if (dtmp.equals("")) {
				dtmp = "NONE";
			}

			//if(lan.isSelected()) htmp = null;
			if (lan.isSelected()) {
				htmp = "(LAN)";
			}

			//***save the set of selected data
			SaveSet s = new SaveSet(Settings.login_def_smb, htmp, utmp, ptmp, "", "", dtmp);

			//*** Now make the function call to the methos for starting
			//connections
			boolean status;
			int potmp = 0; //*** port number: unlikely to be needed in the future

			status = StartConnection.startCon("SMB", htmp, utmp, ptmp, potmp, dtmp, useLocal);

            /*
            try
            {
             con = new SmbConnection(htmp,dtmp,utmp,ptmp, ((ConnectionListener)JFtp.remoteDir));

             //JFtp.statusP.jftp.addConnection(htmp, con);
             if(useLocal)
             {
                     JFtp.statusP.jftp.addLocalConnection(htmp, con);
                JFtp.localDir.fresh();
             }
             else
             {
                      JFtp.statusP.jftp.addConnection(htmp, con);
                JFtp.remoteDir.fresh();
             }

            //JFtp.remoteDir.setCon(con);
            //con.setLocalPath(JFtp.localDir.getCon().getPWD());
            //con.addConnectionListener((ConnectionListener) JFtp.localDir);
            //con.addConnectionListener((ConnectionListener) JFtp.remoteDir);
            //JFtp.remoteDir.fresh();
            }
            catch(Exception ex)
            {
                    Log.debug("Could not create SMBConnection, does this distribution come with jcifs?");
            } */
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();
			JFtp.mainFrame.setVisible(true);
			JFtp.mainFrame.toFront();

			if (listener != null) {
				listener.componentResized(new ComponentEvent(this, 0));
			}
		}
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

	public void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}
}
