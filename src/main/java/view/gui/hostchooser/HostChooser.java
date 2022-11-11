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
import view.gui.base.FtpHost;
import view.gui.framework.HButton;
import view.gui.framework.HFrame;
import view.gui.framework.HInsetPanel;
import view.gui.framework.HPanel;
import view.gui.framework.HPasswordField;
import view.gui.framework.HTextField;
import view.gui.tasks.HostList;
import model.net.FilesystemConnection;
import model.net.FtpConnection;
import model.net.FtpConstants;
import model.net.FtpURLConnection;
import model.net.wrappers.StartConnection;
import model.system.StringUtils;
import model.system.logging.Log;
import controller.util.I18nHelper;

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
	private final HTextField host = new HTextField(I18nHelper.getUIString("hostname"), "localhost        ");
	private final HTextField user = new HTextField(I18nHelper.getUIString("username"), "anonymous        ");
	//public static HTextField pass = new HTextField("Password:","none@nowhere.no");
	private final HPasswordField pass = new HPasswordField(I18nHelper.getUIString("password"), "none@nowhere.no");
	private final HTextField port = new HTextField(I18nHelper.getUIString("port"), "21");
	private final HTextField cwd = new HTextField(I18nHelper.getUIString("remote"), Settings.defaultDir);
	private final HTextField lcwd = new HTextField(I18nHelper.getUIString("local"), Settings.defaultWorkDir);
	private final HTextField dl = new HTextField(I18nHelper.getUIString("max.connections"), "3");
	private final HTextField crlf = new HTextField(I18nHelper.getUIString("override.server.newline"), "<default>");
	private final JCheckBox anonBox = new JCheckBox(I18nHelper.getUIString("use.anonymous.login"), false);
	private final JCheckBox listBox = new JCheckBox(I18nHelper.getUIString("list.compatibility.mode"), false);
	private final JCheckBox dirBox = new JCheckBox(I18nHelper.getUIString("use.default.directories"), Settings.getUseDefaultDir());
	private final JCheckBox modeBox = new JCheckBox(I18nHelper.getUIString("use.active.ftp.no.need.to"), false);
	private final JCheckBox threadBox = new JCheckBox(I18nHelper.getUIString("multiple.connections1"), false);
	private final HPanel okP = new HPanel();
	private final HButton ok = new HButton(I18nHelper.getUIString("connect"));
	private final HButton backMode = new HButton(I18nHelper.getUIString("yes"));
	private final HButton frontMode = new HButton(I18nHelper.getUIString("no"));
	private final HFrame h = new HFrame();
	private final HPanel listP = new HPanel();
	private final HButton list = new HButton(I18nHelper.getUIString("choose.from.or.edit.list"));
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
		this.setTitle(I18nHelper.getUIString("ftp.connection"));
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
			Log.debug(I18nHelper.getLogString("error.initializing.connection.values"));
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
			JLabel l1 = new JLabel(I18nHelper.getUIString("unix.lf.mac.mvs.cr.win.crlf"));
			l1.setFont(new Font("Dialog", Font.PLAIN, 10));
			JLabel l2 = new JLabel(I18nHelper.getUIString("don.t.change.this.unless.you.transfer.text.only"));
			l2.setFont(new Font("Dialog", Font.PLAIN, 10));
			x1.add("North", l1);
			x1.add(JFtp.SOUTH, l2);
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
		this.getContentPane().add(JFtp.CENTER, root);

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

			JFtp.statusP.jftp.addConnection(url, con);

			uc.connect();

			int response = uc.getLoginResponse();

			if (FtpConstants.LOGIN_OK == response) {
				this.dispose();

				if (null != this.listener) {
					this.listener.componentResized(new ComponentEvent(this, 0));
				}

				JFtp.mainFrame.setVisible(true);
				JFtp.mainFrame.toFront();
			} else {
				this.setTitle(I18nHelper.getUIString("wrong.password"));
				this.host.setText(uc.getHost());
				this.port.setText(Integer.toString(uc.getPort()));
				this.user.setText(uc.getUser());
				this.pass.setText(uc.getPass());
				this.setVisible(true);
				this.toFront();
				this.host.requestFocus();
			}
		} catch (IOException ex) {
			Log.debug(I18nHelper.getLogString("error"));
			ex.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.ok) || (e.getSource() == this.pass.text)) {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			FtpConnection con = null;
			JFtp.setHost(this.host.getText());

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
			JFtp.hostinfo.hostname = htmp;
			JFtp.hostinfo.username = utmp;
			JFtp.hostinfo.password = ptmp;
			JFtp.hostinfo.port = potmp;
			JFtp.hostinfo.type = "ftp";

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

			if (JFtp.localDir instanceof FilesystemConnection) {
				if (!JFtp.localDir.setPath(ltmp)) {
					if (!JFtp.localDir.setPath(System.getProperty("user.home"))) {
						JFtp.localDir.setPath("/");
					}
				}
			}

			int response = StartConnection.startFtpCon(htmp, utmp, ptmp, Integer.parseInt(potmp), dtmp, this.useLocal, this.crlf.getText().trim());

			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();

			JFtp.mainFrame.setVisible(true);
			JFtp.mainFrame.toFront();

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
			if (this.dirBox.isSelected()) {
				this.lcwd.setEnabled(false);
				this.cwd.setEnabled(false);
			} else {
				this.lcwd.setEnabled(true);
				this.cwd.setEnabled(true);
			}
		} else if (e.getSource() == this.anonBox) {
			if (this.anonBox.isSelected()) {
				this.user.setText("anonymous");
				this.pass.setText("no@no.no");
				this.user.setEnabled(false);
				this.pass.text.setEnabled(false);
			} else {
				this.user.setEnabled(true);
				this.pass.text.setEnabled(true);
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
		this.h.setTitle(I18nHelper.getUIString("connection.failed"));
		this.h.setLocation(150, 200);

		JTextArea text = new JTextArea();
		this.h.getContentPane().add(JFtp.CENTER, text);
		this.h.getContentPane().add(JFtp.SOUTH, p);
		text.setText(I18nHelper.getUIString("busy.server"));
		JFtp.logTextArea.setText("");
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

			JFtp.mainFrame.setVisible(false);

			while ((FtpConstants.OFFLINE == response) && (1 == this.mode)) {
				System.out.print(I18nHelper.getLogString("server.is.full.next.attempt.in"));

				int r = 5;

				for (final int i = 0; i < r; r--) {
					System.out.print(r + "-");

					try {
						Thread.sleep(1000);
					} catch (Exception ex) {
					}
				}

				System.out.println("0...");

				response = StartConnection.startFtpCon(htmp, utmp, ptmp, Integer.parseInt(potmp), dtmp, useLocal);
			}

			if (1 == this.mode) {
				JFtp.mainFrame.setVisible(true);
			} else {
				// Switch windows
				JFtp.mainFrame.setVisible(false);
				this.setVisible(true);
				this.toFront();

			}
		} else if ((FtpConstants.LOGIN_OK != response) || ((FtpConstants.OFFLINE == response) && (!Settings.reconnect))) {
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			//setFilesystemConnection();
			if (useLocal) {
				JFtp.statusP.jftp.closeCurrentLocalTab();
			} else {
				JFtp.statusP.jftp.closeCurrentTab();
			}

		}
	}

}
