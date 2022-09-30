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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

public class SftpHostChooser extends HFrame implements ActionListener, WindowListener, ChangeListener {
	private final HButton ok = new HButton("Connect");
	private final HButton keyfile = new HButton("Choose Key File");
	public final HTextField host = new HTextField("Host:", "localhost");
	public final HTextField user = new HTextField("Username:", "guest");
	public final HTextField port = new HTextField("Port:", "22");
	public final HPasswordField pass = new HPasswordField("Password/Phrase:", "nopasswd");
	public final JComboBox enc = new JComboBox();
	public final JComboBox cs = new JComboBox();
	public final JComboBox keys = new JComboBox();
	public final JLabel encL = new JLabel("Preferred Encryption");
	public final JLabel csL = new JLabel("Preferred Message Authentication");
	public final JLabel keysL = new JLabel("Preferred Public Key");
	public final JLabel keyfileL = new JLabel("None, maybe look in ~/.ssh");
	private ComponentListener listener = null;
	private boolean useLocal = false;
	private String keyfileName = null;

	public SftpHostChooser(ComponentListener l, boolean local) {
		listener = l;
		useLocal = local;
		this.init();
	}

	public SftpHostChooser(ComponentListener l) {
		listener = l;
		this.init();
	}

	public SftpHostChooser() {
		this.init();
	}

	public void init() {
		this.setLocation(100, 150);
		this.setTitle("Sftp Connection...");
		this.setBackground(new HPanel().getBackground());

		try {
			File f = new File(net.sf.jftp.config.Settings.appHomeDir);
			f.mkdir();

			File f1 = new File(net.sf.jftp.config.Settings.login);
			f1.createNewFile();

			File f2 = new File(net.sf.jftp.config.Settings.login_def_sftp);
			f2.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		String[] login = net.sf.jftp.config.LoadSet.loadSet(net.sf.jftp.config.Settings.login_def_sftp);

		if ((login[0] != null) && (login.length > 1)) {
			host.setText(login[0]);
			user.setText(login[1]);

			if (login.length > 3) {
				port.setText(login[3]);
			}
		}

		if (net.sf.jftp.config.Settings.getStorePasswords()) {
			if ((login != null) && (login.length > 2) && (login[2] != null)) {
				pass.setText(login[2]);
			}
		} else {
			pass.setText("");
		}


		enc.addItem("3des-cbc");
		enc.addItem("blowfish-cbc");

		cs.addItem("hmac-sha1");
		cs.addItem("hmac-sha1-96");
		cs.addItem("hmac-md5");
		cs.addItem("hmac-md5-96");

		keys.addItem("ssh-rsa");
		keys.addItem("ssh-dss");

		HInsetPanel root = new HInsetPanel();
		root.setLayout(new MigLayout());
		root.add(host);
		root.add(port, "wrap");
		root.add(user);
		root.add(pass, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(encL);
		root.add(enc, "wrap");
		root.add(csL);
		root.add(cs, "wrap");
		root.add(keysL);
		root.add(keys);

		root.add(new JLabel(" "), "wrap");

		root.add(keyfileL);
		root.add(keyfile, "wrap");
		keyfileL.setForeground(Color.DARK_GRAY);
		keyfileL.setFont(new Font("serif", Font.ITALIC, 11));

		root.add(new JLabel(" "), "wrap");

		root.add(new JLabel(" "));
		root.add(ok, "align right");

		this.getContentPane().setLayout(new BorderLayout(10, 10));
		this.getContentPane().add("Center", root);

		ok.addActionListener(this);
		keyfile.addActionListener(this);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		pass.text.addActionListener(this);

		this.pack();
		this.setModal(false);
		this.setVisible(false);
		this.addWindowListener(this);
	}

	public void stateChanged(ChangeEvent e) {
		enc.setEnabled(false);
		cs.setEnabled(false);
		keys.setEnabled(false);
	}

	public void update() {
		this.fixLocation();
		this.setVisible(true);
		this.toFront();
		host.requestFocus();
	}

	public void update(String url) {
		try {
			url = url.replace("sftp://", "ftp://"); //sftp is not a valid protocol here, but we still get the parser to work

			net.sf.jftp.net.wrappers.Sftp2URLConnection uc = new net.sf.jftp.net.wrappers.Sftp2URLConnection(new java.net.URL(url));
			net.sf.jftp.net.wrappers.Sftp2Connection con = uc.getSftp2Connection();
			net.sf.jftp.JFtp.statusP.jftp.addConnection(url, con);

			uc.connect();

			if (!uc.loginSucceeded()) {
				this.setTitle("Wrong password!");
				host.setText(uc.getHost());
				port.setText(Integer.toString(uc.getPort()));
				user.setText(uc.getUser());
				pass.setText(uc.getPass());
				this.setVisible(true);
				this.toFront();
				host.requestFocus();
			} else {
				this.dispose();

				if (listener != null) {
					listener.componentResized(new ComponentEvent(this, 0));
				}

				net.sf.jftp.JFtp.mainFrame.setVisible(true);
				net.sf.jftp.JFtp.mainFrame.toFront();
			}
		} catch (IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error!");
			ex.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == ok) || (e.getSource() == pass.text)) {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			String htmp = host.getText().trim();
			String utmp = user.getText().trim();
			String ptmp = pass.getText();

			int potmp = 22;

			try {
				potmp = Integer.parseInt(port.getText());
			} catch (Exception ex) {
				net.sf.jftp.system.logging.Log.debug("Error: Not a number!");
			}

			try {
				boolean status;

				net.sf.jftp.config.SaveSet s = new net.sf.jftp.config.SaveSet(net.sf.jftp.config.Settings.login_def_sftp, htmp, utmp, ptmp, "" + potmp, "null", "null");

				net.sf.jftp.net.wrappers.Sftp2Connection con2 = new net.sf.jftp.net.wrappers.Sftp2Connection(htmp, "" + potmp, keyfileName);

				if (con2.login(utmp, ptmp)) {
					if (useLocal) {
						net.sf.jftp.JFtp.statusP.jftp.addLocalConnection(htmp, con2);
					} else {
						net.sf.jftp.JFtp.statusP.jftp.addConnection(htmp, con2);
					}

					if (con2.chdir(con2.getPWD()) || con2.chdir("/")) {
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				net.sf.jftp.system.logging.Log.debug("Could not create SftpConnection, does this distribution come with j2ssh?");
			}


			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();
			net.sf.jftp.JFtp.mainFrame.setVisible(true);
			net.sf.jftp.JFtp.mainFrame.toFront();

			if (listener != null) {
				listener.componentResized(new ComponentEvent(this, 0));
			}
		} else if (e.getSource() == keyfile) {
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				keyfileName = chooser.getSelectedFile().getPath();

				if (keyfileName != null) {
					keyfileL.setText("(File present)");
				}
			} else {
				keyfileName = null;

				if (keyfileName != null) {
					keyfileL.setText("(No File)");
				}
			}
		}
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

	public void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}
}
