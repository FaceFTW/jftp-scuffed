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
package view.gui.hostchooser;

import net.miginfocom.swing.MigLayout;
import view.JFtp;
import controller.config.LoadSet;
import controller.config.SaveSet;
import controller.config.Settings;
import view.gui.framework.HButton;
import view.gui.framework.HFrame;
import view.gui.framework.HInsetPanel;
import view.gui.framework.HPanel;
import view.gui.framework.HPasswordField;
import view.gui.framework.HTextField;
import model.net.wrappers.SmbConnection;
import model.net.wrappers.StartConnection;
import model.system.logging.Log;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;


public class SmbHostChooser extends HFrame implements ActionListener, WindowListener {
	private static final HTextField host = new HTextField("URL:", "smb://localhost/");
	private static final HTextField user = new HTextField("Username:", "guest");
	private static final HPasswordField pass = new HPasswordField("Password:", "nopasswd");
	private final HTextField domain = new HTextField("Domain:    ", "WORKGROUP");
	private final HTextField broadcast = new HTextField("Broadcast IP:    ", "AUTO");
	private final HTextField wins = new HTextField("WINS Server IP:    ", "NONE");
	private final JComboBox ip = new JComboBox();
	private final JCheckBox lan = new JCheckBox("Browse LAN", true);
	private final HButton ok = new HButton("Connect");
	private ComponentListener listener;
	private boolean useLocal;

	public SmbHostChooser(ComponentListener l, boolean local) {
		super();
		this.listener = l;
		this.useLocal = local;
		this.init();
	}

	public SmbHostChooser(ComponentListener l) {
		super();
		this.listener = l;
		this.init();
	}

	public SmbHostChooser() {
		super();
		this.init();
	}

	private void init() {
		this.setPreferredSize(new Dimension(500, 320));
		this.setLocation(100, 150);
		this.setTitle("Smb Connection...");
		this.setBackground(new HPanel().getBackground());

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

		if ((null != login[0]) && (1 < login.length)) {
			host.setText(login[0]);
			user.setText(login[1]);
			this.domain.setText(login[5]);
		}


		if (Settings.getStorePasswords()) {
			if ((null != login) && (2 < login.length) && (null != login[2])) {
				pass.setText(login[2]);
			}
		} else {
			pass.setText("");
		}

		this.ip.setEditable(true);

		HInsetPanel root = new HInsetPanel();
		root.setLayout(new MigLayout());

		root.add(host);
		root.add(this.lan, "wrap");
		root.add(user);
		root.add(pass, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(this.ip);
		root.add(this.domain, "wrap");

		root.add(this.broadcast);
		root.add(this.wins, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(new JLabel(" "));
		root.add(this.ok, "align right");

		this.ok.addActionListener(this);

		host.setEnabled(!this.lan.isSelected());
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		this.lan.addActionListener(this);
		pass.text.addActionListener(this);

		this.getContentPane().setLayout(new BorderLayout(3, 3));
		this.getContentPane().add(JFtp.CENTER, root);

		JTextArea t = new JTextArea() {
			public Insets getInsets() {
				return new Insets(7, 7, 7, 7);
			}
		};
		t.setLineWrap(true);
		t.setText("Note: Please use URL format \"smb://host/\"");

		this.getContentPane().add("North", t);

		this.setModal(false);
		this.setVisible(false);
		this.addWindowListener(this);

		this.ip.addItem("<default>");

		try {
			java.util.Enumeration<java.net.NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				java.util.Enumeration<java.net.InetAddress> f = e.nextElement().getInetAddresses();

				while (f.hasMoreElements()) {
					this.ip.addItem(f.nextElement().getHostAddress());
				}
			}
		} catch (Exception ex) {
			Log.debug("Error determining default network interface: " + ex);

			//ex.printStackTrace();
		}

		//setBCast();
		this.domain.setEnabled(false);
		this.broadcast.setEnabled(false);
		this.wins.setEnabled(false);

		this.ip.addActionListener(this);

		this.pack();
	}

	private void setBCast() {
		try {
			String tmp = ((String) this.ip.getSelectedItem()).trim();
			String x = tmp.substring(0, tmp.lastIndexOf('.') + 1) + "255";
			this.broadcast.setText(x);
		} catch (Exception ex) {
			Log.out("Error (SMBHostChooser): " + ex);
		}
	}

	public void update() {
		this.fixLocation();
		this.setVisible(true);
		this.toFront();
		user.requestFocus();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.lan) {
			host.setEnabled(!this.lan.isSelected());
		} else if (e.getSource() == this.ip) {
			if (this.ip.getSelectedItem().equals("<default>")) {
				this.domain.setEnabled(false);
				this.broadcast.setEnabled(false);
				this.wins.setEnabled(false);
			} else {
				this.domain.setEnabled(true);
				this.broadcast.setEnabled(true);
				this.wins.setEnabled(true);
				this.setBCast();
			}
		} else if ((e.getSource() == this.ok) || (e.getSource() == pass.text)) {
			// Switch windows
			//this.setVisible(false);
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			SmbConnection con = null;

			//System.out.println(jcifs.Config.getProperty("jcifs.smb.client.laddr"));
			String tmp = ((String) this.ip.getSelectedItem()).trim();

			if (!tmp.isEmpty() && !tmp.equals("<default>")) {
				String x = tmp.trim().substring(0, tmp.lastIndexOf('.') + 1) + "255";
				String bcast = this.broadcast.getText().trim();

				if (!bcast.equals("AUTO")) {
					x = bcast;
				}

				Log.debug("Setting LAN interface to: " + tmp + "/" + x);
				jcifs.Config.setProperty("jcifs.netbios.laddr", tmp);
				jcifs.Config.setProperty("jcifs.smb.client.laddr", tmp);
				jcifs.Config.setProperty("jcifs.netbios.baddr", x);

				String y = this.wins.getText().trim();

				if (!y.equals("NONE")) {
					Log.debug("Setting WINS server IP to: " + y);
					jcifs.Config.setProperty("jcifs.netbios.wins", y);
				}
			}


			String htmp = host.getText().trim();
			String utmp = user.getText().trim();
			String ptmp = pass.getText();
			String dtmp = this.domain.getText().trim();

			//***
			//if(dtmp.equals("")) dtmp = null;
			if (dtmp.isEmpty()) {
				dtmp = "NONE";
			}

			//if(lan.isSelected()) htmp = null;
			if (this.lan.isSelected()) {
				htmp = "(LAN)";
			}

			//***save the set of selected data
			SaveSet s = new SaveSet(Settings.login_def_smb, htmp, utmp, ptmp, "", "", dtmp);

			//*** Now make the function call to the methos for starting
			//connections
			boolean status;
			final int potmp = 0; //*** port number: unlikely to be needed in the future

			status = StartConnection.startCon("SMB", htmp, utmp, ptmp, potmp, dtmp, this.useLocal);

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
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();
			JFtp.mainFrame.setVisible(true);
			JFtp.mainFrame.toFront();

			if (null != this.listener) {
				this.listener.componentResized(new ComponentEvent(this, 0));
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
