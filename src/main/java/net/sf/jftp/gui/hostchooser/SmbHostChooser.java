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
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HInsetPanel;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HPasswordField;
import net.sf.jftp.gui.framework.HTextField;

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
	public static final HTextField host = new HTextField("URL:", "smb://localhost/");
	public static final HTextField user = new HTextField("Username:", "guest");
	public static final HPasswordField pass = new HPasswordField("Password:", "nopasswd");
	private final HButton ok = new HButton("Connect");
	public final HTextField domain = new HTextField("Domain:    ", "WORKGROUP");
	public final HTextField broadcast = new HTextField("Broadcast IP:    ", "AUTO");
	public final HTextField wins = new HTextField("WINS Server IP:    ", "NONE");

	public final JComboBox ip = new JComboBox();
	final JCheckBox lan = new JCheckBox("Browse LAN", true);
	private ComponentListener listener = null;
	private boolean useLocal = false;

	public SmbHostChooser(final ComponentListener l, final boolean local) {
		super();
		this.listener = l;
		this.useLocal = local;
		this.init();
	}

	public SmbHostChooser(final ComponentListener l) {
		super();
		this.listener = l;
		this.init();
	}

	public SmbHostChooser() {
		super();
		this.init();
	}

	public void init() {
		this.setPreferredSize(new Dimension(500, 320));
		this.setLocation(100, 150);
		this.setTitle("Smb Connection...");
		this.setBackground(new HPanel().getBackground());

		try {
			final File f = new File(net.sf.jftp.config.Settings.appHomeDir);
			f.mkdir();

			final File f1 = new File(net.sf.jftp.config.Settings.login);
			f1.createNewFile();

			final File f2 = new File(net.sf.jftp.config.Settings.login_def_smb);
			f2.createNewFile();
		} catch (final IOException ex) {
			ex.printStackTrace();
		}

		final String[] login = net.sf.jftp.config.LoadSet.loadSet(net.sf.jftp.config.Settings.login_def_smb);

		if ((null != login[0]) && (1 < login.length)) {
			net.sf.jftp.gui.hostchooser.SmbHostChooser.host.setText(login[0]);
			net.sf.jftp.gui.hostchooser.SmbHostChooser.user.setText(login[1]);
			this.domain.setText(login[5]);
		}


		if (net.sf.jftp.config.Settings.getStorePasswords()) {
			if ((null != login) && (2 < login.length) && (null != login[2])) {
				net.sf.jftp.gui.hostchooser.SmbHostChooser.pass.setText(login[2]);
			}
		} else {
			net.sf.jftp.gui.hostchooser.SmbHostChooser.pass.setText("");
		}

		this.ip.setEditable(true);

		final HInsetPanel root = new HInsetPanel();
		root.setLayout(new MigLayout());

		root.add(net.sf.jftp.gui.hostchooser.SmbHostChooser.host);
		root.add(this.lan, "wrap");
		root.add(net.sf.jftp.gui.hostchooser.SmbHostChooser.user);
		root.add(net.sf.jftp.gui.hostchooser.SmbHostChooser.pass, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(this.ip);
		root.add(this.domain, "wrap");

		root.add(this.broadcast);
		root.add(this.wins, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(new JLabel(" "));
		root.add(this.ok, "align right");

		this.ok.addActionListener(this);

		net.sf.jftp.gui.hostchooser.SmbHostChooser.host.setEnabled(!this.lan.isSelected());
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		this.lan.addActionListener(this);
		net.sf.jftp.gui.hostchooser.SmbHostChooser.pass.text.addActionListener(this);

		this.getContentPane().setLayout(new BorderLayout(3, 3));
		this.getContentPane().add("Center", root);

		final JTextArea t = new JTextArea() {
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
			final Enumeration e = NetworkInterface.getNetworkInterfaces();

			while (e.hasMoreElements()) {
				final Enumeration f = ((NetworkInterface) e.nextElement()).getInetAddresses();

				while (f.hasMoreElements()) {
					this.ip.addItem(((InetAddress) f.nextElement()).getHostAddress());
				}
			}
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error determining default network interface: " + ex);

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
			final String tmp = ((String) this.ip.getSelectedItem()).trim();
			final String x = tmp.substring(0, tmp.lastIndexOf(".") + 1) + "255";
			this.broadcast.setText(x);
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.out("Error (SMBHostChooser): " + ex);
		}
	}

	public void update() {
		this.fixLocation();
		this.setVisible(true);
		this.toFront();
		net.sf.jftp.gui.hostchooser.SmbHostChooser.user.requestFocus();
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == this.lan) {
			net.sf.jftp.gui.hostchooser.SmbHostChooser.host.setEnabled(!this.lan.isSelected());
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
		} else if ((e.getSource() == this.ok) || (e.getSource() == net.sf.jftp.gui.hostchooser.SmbHostChooser.pass.text)) {
			// Switch windows
			//this.setVisible(false);
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			final net.sf.jftp.net.wrappers.SmbConnection con = null;

			//System.out.println(jcifs.Config.getProperty("jcifs.smb.client.laddr"));
			final String tmp = ((String) this.ip.getSelectedItem()).trim();

			if (!tmp.isEmpty() && !tmp.equals("<default>")) {
				String x = tmp.trim().substring(0, tmp.lastIndexOf(".") + 1) + "255";
				final String bcast = this.broadcast.getText().trim();

				if (!bcast.equals("AUTO")) {
					x = bcast;
				}

				net.sf.jftp.system.logging.Log.debug("Setting LAN interface to: " + tmp + "/" + x);
				jcifs.Config.setProperty("jcifs.netbios.laddr", tmp);
				jcifs.Config.setProperty("jcifs.smb.client.laddr", tmp);
				jcifs.Config.setProperty("jcifs.netbios.baddr", x);

				final String y = this.wins.getText().trim();

				if (!y.equals("NONE")) {
					net.sf.jftp.system.logging.Log.debug("Setting WINS server IP to: " + y);
					jcifs.Config.setProperty("jcifs.netbios.wins", y);
				}
			}


			String htmp = net.sf.jftp.gui.hostchooser.SmbHostChooser.host.getText().trim();
			final String utmp = net.sf.jftp.gui.hostchooser.SmbHostChooser.user.getText().trim();
			final String ptmp = net.sf.jftp.gui.hostchooser.SmbHostChooser.pass.getText();
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
			final net.sf.jftp.config.SaveSet s = new net.sf.jftp.config.SaveSet(net.sf.jftp.config.Settings.login_def_smb, htmp, utmp, ptmp, "", "", dtmp);

			//*** Now make the function call to the methos for starting
			//connections
			final boolean status;
			final int potmp = 0; //*** port number: unlikely to be needed in the future

			status = net.sf.jftp.net.wrappers.StartConnection.startCon("SMB", htmp, utmp, ptmp, potmp, dtmp, this.useLocal);

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
			net.sf.jftp.JFtp.mainFrame.setVisible(true);
			net.sf.jftp.JFtp.mainFrame.toFront();

			if (null != this.listener) {
				this.listener.componentResized(new ComponentEvent(this, 0));
			}
		}
	}

	public void windowClosing(final WindowEvent e) {
		//System.exit(0);
		this.dispose();
	}

	public void windowClosed(final WindowEvent e) {
	}

	public void windowActivated(final WindowEvent e) {
	}

	public void windowDeactivated(final WindowEvent e) {
	}

	public void windowIconified(final WindowEvent e) {
	}

	public void windowDeiconified(final WindowEvent e) {
	}

	public void windowOpened(final WindowEvent e) {
	}

	public void pause(final int time) {
		try {
			Thread.sleep(time);
		} catch (final Exception ex) {
		}
	}
}
