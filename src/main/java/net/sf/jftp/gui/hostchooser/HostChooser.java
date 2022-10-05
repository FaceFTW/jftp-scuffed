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
import net.sf.jftp.config.LoadSet;
import net.sf.jftp.config.SaveSet;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.FtpHost;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HInsetPanel;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HPasswordField;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.gui.tasks.HostList;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.FtpConstants;
import net.sf.jftp.net.FtpURLConnection;
import net.sf.jftp.net.wrappers.StartConnection;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;


public class HostChooser extends HFrame implements ActionListener, WindowListener {
	private final HTextField host = new HTextField("Hostname:", "localhost        ");
	private final HTextField user = new HTextField("Username:", "anonymous        ");
	//public static HTextField pass = new HTextField("Password:","none@nowhere.no");
	private final HPasswordField pass = new HPasswordField("Password:", "none@nowhere.no");
	private final HTextField port = new HTextField("Port:    ", "21");
	private final HTextField cwd = new HTextField("Remote:  ", Settings.defaultDir);
	private final HTextField lcwd = new HTextField("Local:   ", Settings.defaultWorkDir);
	private final HTextField dl = new HTextField("Max. connections:    ", "3");
	private final HTextField crlf = new HTextField("Override server newline:    ", "<default>");
	private final JCheckBox anonBox = new JCheckBox("Use anonymous login", false);
	private final JCheckBox listBox = new JCheckBox("LIST compatibility mode", false);
	private final JCheckBox dirBox = new JCheckBox("Use default directories", Settings.getUseDefaultDir());
	private final JCheckBox modeBox = new JCheckBox("Use active Ftp (no need to)", false);
	private final JCheckBox threadBox = new JCheckBox("Multiple connections", false);
	private final HPanel okP = new HPanel();
	private final HButton ok = new HButton("Connect");
	private final HButton backMode = new HButton("Yes");
	private final HButton frontMode = new HButton("No");
	private final HFrame h = new HFrame();
	private final HPanel listP = new HPanel();
	private final HButton list = new HButton("Choose from or edit list...");
	private final boolean ext = Settings.showNewlineOption;
	private ComponentListener listener;
	private int mode;
	private boolean useLocal;

	public HostChooser(ComponentListener l, boolean local) {
		super();
		this.listener = l;
		this.useLocal = local;
		this.init();
	}

	public HostChooser(ComponentListener l) {
		super();
		this.listener = l;
		this.init();
	}

	public HostChooser() {
		super();
		this.init();
	}

	private void init() {
		this.setTitle("Ftp Connection...");
		this.setBackground(this.okP.getBackground());

		this.anonBox.setSelected(false);
		this.user.setEnabled(true);
		this.pass.text.setEnabled(true);

		try {
			LoadSet l = new LoadSet();
			String[] login = LoadSet.loadSet(Settings.login_def);

			if ((null != login) && (null != login[0])) {
				this.host.setText(login[0]);
				this.user.setText(login[1]);

				if (null != login[3]) {
					this.port.setText(login[3]);
				}

				if (null != login[4]) {
					this.cwd.setText(login[4]);
				}

				if (null != login[5]) {
					this.lcwd.setText(login[5]);
				}
			}

			if (Settings.getStorePasswords()) {
				if (null != login && null != login[2]) {
					this.pass.setText(login[2]);
				}
			} else {
				this.pass.setText("");
			}
		} catch (Exception ex) {
			Log.debug("Error initializing connection values!");
			//ex.printStackTrace();
		}

		HInsetPanel root = new HInsetPanel();
		root.setLayout(new MigLayout());

		root.add(this.host);
		root.add(this.port, "wrap");
		root.add(this.user);
		root.add(this.pass, "wrap");
		root.add(this.anonBox, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(this.dirBox, "wrap");
		root.add(this.lcwd);
		root.add(this.cwd, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(this.listBox);
		root.add(this.modeBox, "wrap");
		root.add(this.threadBox);
		root.add(this.dl, "wrap");

		root.add(new JLabel(" "), "wrap");

		if (this.ext) {
			root.add(this.crlf);

			JPanel x1 = new JPanel();
			x1.setLayout(new BorderLayout(2, 2));
			JLabel l1 = new JLabel("Unix: LF, Mac/MVS: CR, Win: CRLF");
			l1.setFont(new Font("Dialog", Font.PLAIN, 10));
			JLabel l2 = new JLabel("Don't change this unless you transfer text only");
			l2.setFont(new Font("Dialog", Font.PLAIN, 10));
			x1.add("North", l1);
			x1.add("South", l2);
			root.add(x1, "wrap");
		}

		this.modeBox.setSelected(!Settings.getFtpPasvMode());
		this.threadBox.setSelected(Settings.getEnableMultiThreading());
		this.dirBox.setSelected(Settings.getUseDefaultDir());
		this.anonBox.addActionListener(this);
		this.threadBox.addActionListener(this);

		root.add(this.listP);
		this.listP.add(this.list);
		this.list.addActionListener(this);

		root.add(this.okP, "align right");
		this.okP.add(this.ok);
		this.ok.addActionListener(this);

		this.dirBox.addActionListener(this);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		this.lcwd.setEnabled(!this.dirBox.isSelected());
		this.cwd.setEnabled(!this.dirBox.isSelected());
		this.pass.text.addActionListener(this);

		this.getContentPane().setLayout(new BorderLayout(10, 10));
		this.getContentPane().add("Center", root);

		this.pack();
		this.setModal(false);
		this.setVisible(false);

		this.addWindowListener(this);
		this.prepareBackgroundMessage();
	}

	public void update() {
		this.fixLocation();
		this.setVisible(true);
		this.toFront();
		this.host.requestFocus();
	}

	public void update(String url) {
		try {
			FtpURLConnection uc = new FtpURLConnection(new java.net.URL(url));
			FtpConnection con = uc.getFtpConnection();

			net.sf.jftp.JFtp.statusP.jftp.addConnection(url, con);

			uc.connect();

			int response = uc.getLoginResponse();

			if (FtpConstants.LOGIN_OK != response) {
				this.setTitle("Wrong password!");
				this.host.setText(uc.getHost());
				this.port.setText(Integer.toString(uc.getPort()));
				this.user.setText(uc.getUser());
				this.pass.setText(uc.getPass());
				this.setVisible(true);
				this.toFront();
				this.host.requestFocus();
			} else {
				this.dispose();

				if (null != this.listener) {
					this.listener.componentResized(new ComponentEvent(this, 0));
				}

				net.sf.jftp.JFtp.mainFrame.setVisible(true);
				net.sf.jftp.JFtp.mainFrame.toFront();
			}
		} catch (IOException ex) {
			Log.debug("Error!");
			ex.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.ok) || (e.getSource() == this.pass.text)) {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			FtpConnection con = null;
			net.sf.jftp.JFtp.setHost(this.host.getText());

			String htmp = StringUtils.cut(this.host.getText(), " ");
			String utmp = StringUtils.cut(this.user.getText(), " ");
			String ptmp = StringUtils.cut(this.pass.getText(), " ");
			String potmp = StringUtils.cut(this.port.getText(), " ");

			Settings.setProperty("jftp.ftpPasvMode", !this.modeBox.isSelected());
			Settings.setProperty("jftp.enableMultiThreading", this.threadBox.isSelected());
			Settings.setProperty("jftp.useDefaultDir", this.dirBox.isSelected());

			if (this.listBox.isSelected()) {
				FtpConnection.LIST = "LIST";
			} else {
				FtpConnection.LIST = "LIST -laL";
			}

			/* All the information of the current server are stored in JFtp.HostInfo */
			net.sf.jftp.JFtp.hostinfo.hostname = htmp;
			net.sf.jftp.JFtp.hostinfo.username = utmp;
			net.sf.jftp.JFtp.hostinfo.password = ptmp;
			net.sf.jftp.JFtp.hostinfo.port = potmp;
			net.sf.jftp.JFtp.hostinfo.type = "ftp";

			boolean pasv = Settings.getFtpPasvMode();
			boolean threads = Settings.getEnableMultiThreading();

			Settings.maxConnections = Integer.parseInt(this.dl.getText().trim());

			Settings.save();

			String dtmp;
			String ltmp;

			if (this.dirBox.isSelected()) {
				dtmp = Settings.defaultDir;
				ltmp = Settings.defaultWorkDir;
			} else {
				dtmp = this.cwd.getText();
				ltmp = this.lcwd.getText();
			}

			SaveSet s = new SaveSet(Settings.login_def, htmp, utmp, ptmp, potmp, dtmp, ltmp);

			if (net.sf.jftp.JFtp.localDir instanceof FilesystemConnection) {
				if (!net.sf.jftp.JFtp.localDir.setPath(ltmp)) {
					if (!net.sf.jftp.JFtp.localDir.setPath(System.getProperty("user.home"))) {
						net.sf.jftp.JFtp.localDir.setPath("/");
					}
				}
			}

			int response = StartConnection.startFtpCon(htmp, utmp, ptmp, Integer.parseInt(potmp), dtmp, this.useLocal, this.crlf.getText().trim());

			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();

			net.sf.jftp.JFtp.mainFrame.setVisible(true);
			net.sf.jftp.JFtp.mainFrame.toFront();

			if (null != this.listener) {
				this.listener.componentResized(new ComponentEvent(this, 0));
			}

		} else if (e.getSource() == this.list) {
			HostList hl = new HostList(this);
			FtpHost selectedHost = hl.getFtpHost();

			if (null == selectedHost) {
				return;
			}

			this.host.setText(selectedHost.hostname);
			this.pass.setText(selectedHost.password);
			this.user.setText(selectedHost.username);
			this.port.setText(selectedHost.port);

			Settings.setProperty("jftp.useDefaultDir", true);
			this.dirBox.setSelected(Settings.getUseDefaultDir());
			this.lcwd.setEnabled(!this.dirBox.isSelected());
			this.cwd.setEnabled(!this.dirBox.isSelected());
		} else if (e.getSource() == this.dirBox) {
			if (!this.dirBox.isSelected()) {
				this.lcwd.setEnabled(true);
				this.cwd.setEnabled(true);
			} else {
				this.lcwd.setEnabled(false);
				this.cwd.setEnabled(false);
			}
		} else if (e.getSource() == this.anonBox) {
			if (!this.anonBox.isSelected()) {
				this.user.setEnabled(true);
				this.pass.text.setEnabled(true);
			} else {
				this.user.setText("anonymous");
				this.pass.setText("no@no.no");
				this.user.setEnabled(false);
				this.pass.text.setEnabled(false);
			}
		} else if (e.getSource() == this.threadBox) {
			this.dl.setEnabled(this.threadBox.isSelected());
		} else if (e.getSource() == this.backMode) {
			this.mode = 1;
			this.h.setVisible(false);
		} else if (e.getSource() == this.frontMode) {
			this.mode = 2;
			this.h.setVisible(false);
		}
	}

	private void prepareBackgroundMessage() {
		HPanel p = new HPanel();
		p.add(this.backMode);
		p.add(this.frontMode);
		p.setLayout(new FlowLayout(FlowLayout.CENTER));

		this.backMode.addActionListener(this);
		this.frontMode.addActionListener(this);

		this.h.getContentPane().setLayout(new BorderLayout(10, 10));
		this.h.setTitle("Connection failed!");
		this.h.setLocation(150, 200);

		JTextArea text = new JTextArea();
		this.h.getContentPane().add("Center", text);
		this.h.getContentPane().add("South", p);
		text.setText(" ---------------- Output -----------------\n\n" + "The server is busy at the moment.\n\n" + "Do you want JFtp to go to disappear and try to login\n" + "continuously?\n\n" + "(It will show up again when it has initiated a connection)\n\n");
		net.sf.jftp.JFtp.log.setText("");
		text.setEditable(false);
		this.h.pack();
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

	private void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}

	private void tryFtpAgain(int response, String htmp, String ptmp, String utmp, String potmp, String dtmp, boolean useLocal) {
		//*** FOR TESTING PURPOSES
		//System.out.println(htmp + " " + ptmp + " " + utmp);
		//System.out.println(potmp + " " + dtmp);
		//***
		if ((FtpConstants.OFFLINE == response) && Settings.reconnect) {
			//FtpConnection con;
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			this.h.setVisible(true);

			while (0 == this.mode) {
				this.pause(10);
			}

			net.sf.jftp.JFtp.mainFrame.setVisible(false);

			while ((FtpConstants.OFFLINE == response) && (1 == this.mode)) {
				System.out.print("Server is full, next attempt in ");

				int r = 5;

				for (final int i = 0; i < r; r--) {
					System.out.print("" + r + "-");

					try {
						Thread.sleep(1000);
					} catch (Exception ex) {
					}
				}

				System.out.println("0...");

				response = StartConnection.startFtpCon(htmp, utmp, ptmp, Integer.parseInt(potmp), dtmp, useLocal);
			}

			if (1 == this.mode) {
				net.sf.jftp.JFtp.mainFrame.setVisible(true);
			} else {
				// Switch windows
				net.sf.jftp.JFtp.mainFrame.setVisible(false);
				this.setVisible(true);
				this.toFront();

			}
		} else if ((FtpConstants.LOGIN_OK != response) || ((FtpConstants.OFFLINE == response) && (!Settings.reconnect))) {
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			//setFilesystemConnection();
			if (useLocal) {
				net.sf.jftp.JFtp.statusP.jftp.closeCurrentLocalTab();
			} else {
				net.sf.jftp.JFtp.statusP.jftp.closeCurrentTab();
			}

		}
	}

}
