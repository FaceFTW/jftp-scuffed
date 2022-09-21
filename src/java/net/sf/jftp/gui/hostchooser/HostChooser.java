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
import net.sf.jftp.net.FtpURLConnection;
import net.sf.jftp.net.wrappers.StartConnection;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;


public class HostChooser extends HFrame implements ActionListener, WindowListener {
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
	public HTextField host = new HTextField("Hostname:", "localhost        ");
	public HTextField user = new HTextField("Username:", "anonymous        ");
	//public static HTextField pass = new HTextField("Password:","none@nowhere.no");
	public HPasswordField pass = new HPasswordField("Password:", "none@nowhere.no");
	public HTextField port = new HTextField("Port:    ", "21");
	public HTextField cwd = new HTextField("Remote:  ", Settings.defaultDir);
	public HTextField lcwd = new HTextField("Local:   ", Settings.defaultWorkDir);
	public HTextField dl = new HTextField("Max. connections:    ", "3");
	public HTextField crlf = new HTextField("Override server newline:    ", "<default>");
	private ComponentListener listener = null;
	private int mode = 0;
	private boolean useLocal = false;

	public HostChooser(ComponentListener l, boolean local) {
		listener = l;
		useLocal = local;
		init();
	}

	public HostChooser(ComponentListener l) {
		listener = l;
		init();
	}

	public HostChooser() {
		init();
	}

	public void init() {
		setTitle("Ftp Connection...");
		setBackground(okP.getBackground());

		anonBox.setSelected(false);
		user.setEnabled(true);
		pass.text.setEnabled(true);

		try {
			LoadSet l = new LoadSet();
			String[] login = LoadSet.loadSet(Settings.login_def);

			if ((login != null) && (login[0] != null)) {
				host.setText(login[0]);
				user.setText(login[1]);

				if (login[3] != null) {
					port.setText(login[3]);
				}

				if (login[4] != null) {
					cwd.setText(login[4]);
				}

				if (login[5] != null) {
					lcwd.setText(login[5]);
				}
			}

			if (Settings.getStorePasswords()) {
				if (login != null && login[2] != null) {
					pass.setText(login[2]);
				}
			} else {
				pass.setText("");
			}
		} catch (Exception ex) {
			Log.debug("Error initializing connection values!");
			//ex.printStackTrace();
		}

		HInsetPanel root = new HInsetPanel();
		root.setLayout(new MigLayout());

		root.add(host);
		root.add(port, "wrap");
		root.add(user);
		root.add(pass, "wrap");
		root.add(anonBox, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(dirBox, "wrap");
		root.add(lcwd);
		root.add(cwd, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(listBox);
		root.add(modeBox, "wrap");
		root.add(threadBox);
		root.add(dl, "wrap");

		root.add(new JLabel(" "), "wrap");

		if (ext) {
			root.add(crlf);

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

		modeBox.setSelected(!Settings.getFtpPasvMode());
		threadBox.setSelected(Settings.getEnableMultiThreading());
		dirBox.setSelected(Settings.getUseDefaultDir());
		anonBox.addActionListener(this);
		threadBox.addActionListener(this);

		root.add(listP);
		listP.add(list);
		list.addActionListener(this);

		root.add(okP, "align right");
		okP.add(ok);
		ok.addActionListener(this);

		dirBox.addActionListener(this);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		lcwd.setEnabled(!dirBox.isSelected());
		cwd.setEnabled(!dirBox.isSelected());
		pass.text.addActionListener(this);

		getContentPane().setLayout(new BorderLayout(10, 10));
		getContentPane().add("Center", root);

		pack();
		setModal(false);
		setVisible(false);

		addWindowListener(this);
		prepareBackgroundMessage();
	}

	public void update() {
		fixLocation();
		setVisible(true);
		toFront();
		host.requestFocus();
	}

	public void update(String url) {
		try {
			FtpURLConnection uc = new FtpURLConnection(new java.net.URL(url));
			FtpConnection con = uc.getFtpConnection();

			JFtp.statusP.jftp.addConnection(url, con);

			uc.connect();

			int response = uc.getLoginResponse();

			if (response != FtpConnection.LOGIN_OK) {
				setTitle("Wrong password!");
				host.setText(uc.getHost());
				port.setText(Integer.toString(uc.getPort()));
				user.setText(uc.getUser());
				pass.setText(uc.getPass());
				setVisible(true);
				toFront();
				host.requestFocus();
			} else {
				this.dispose();

				if (listener != null) {
					listener.componentResized(new ComponentEvent(this, 0));
				}

				JFtp.mainFrame.setVisible(true);
				JFtp.mainFrame.toFront();
			}
		} catch (IOException ex) {
			Log.debug("Error!");
			ex.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == ok) || (e.getSource() == pass.text)) {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			FtpConnection con = null;
			JFtp.setHost(host.getText());

			String htmp = StringUtils.cut(host.getText(), " ");
			String utmp = StringUtils.cut(user.getText(), " ");
			String ptmp = StringUtils.cut(pass.getText(), " ");
			String potmp = StringUtils.cut(port.getText(), " ");

			Settings.setProperty("jftp.ftpPasvMode", !modeBox.isSelected());
			Settings.setProperty("jftp.enableMultiThreading", threadBox.isSelected());
			Settings.setProperty("jftp.useDefaultDir", dirBox.isSelected());

			if (listBox.isSelected()) {
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

			int x = Integer.parseInt(dl.getText().trim());
			Settings.maxConnections = x;

			Settings.save();

			String dtmp;
			String ltmp;

			if (dirBox.isSelected()) {
				dtmp = Settings.defaultDir;
				ltmp = Settings.defaultWorkDir;
			} else {
				dtmp = cwd.getText();
				ltmp = lcwd.getText();
			}

			SaveSet s = new SaveSet(Settings.login_def, htmp, utmp, ptmp, potmp, dtmp, ltmp);

			if (JFtp.localDir instanceof FilesystemConnection) {
				if (!JFtp.localDir.setPath(ltmp)) {
					if (!JFtp.localDir.setPath(System.getProperty("user.home"))) {
						JFtp.localDir.setPath("/");
					}
				}
			}

			int response = StartConnection.startFtpCon(htmp, utmp, ptmp, Integer.parseInt(potmp), dtmp, useLocal, crlf.getText().trim());

			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();

			JFtp.mainFrame.setVisible(true);
			JFtp.mainFrame.toFront();

			if (listener != null) {
				listener.componentResized(new ComponentEvent(this, 0));
			}

		} else if (e.getSource() == list) {
			HostList hl = new HostList(this);
			FtpHost selectedHost = hl.getFtpHost();

			if (selectedHost == null) {
				return;
			}

			host.setText(selectedHost.hostname);
			pass.setText(selectedHost.password);
			user.setText(selectedHost.username);
			port.setText(selectedHost.port);

			Settings.setProperty("jftp.useDefaultDir", true);
			dirBox.setSelected(Settings.getUseDefaultDir());
			lcwd.setEnabled(!dirBox.isSelected());
			cwd.setEnabled(!dirBox.isSelected());
		} else if (e.getSource() == dirBox) {
			if (!dirBox.isSelected()) {
				lcwd.setEnabled(true);
				cwd.setEnabled(true);
			} else {
				lcwd.setEnabled(false);
				cwd.setEnabled(false);
			}
		} else if (e.getSource() == anonBox) {
			if (!anonBox.isSelected()) {
				user.setEnabled(true);
				pass.text.setEnabled(true);
			} else {
				user.setText("anonymous");
				pass.setText("no@no.no");
				user.setEnabled(false);
				pass.text.setEnabled(false);
			}
		} else if (e.getSource() == threadBox) {
			dl.setEnabled(threadBox.isSelected());
		} else if (e.getSource() == backMode) {
			mode = 1;
			h.setVisible(false);
		} else if (e.getSource() == frontMode) {
			mode = 2;
			h.setVisible(false);
		}
	}

	private void prepareBackgroundMessage() {
		HPanel p = new HPanel();
		p.add(backMode);
		p.add(frontMode);
		p.setLayout(new FlowLayout(FlowLayout.CENTER));

		backMode.addActionListener(this);
		frontMode.addActionListener(this);

		h.getContentPane().setLayout(new BorderLayout(10, 10));
		h.setTitle("Connection failed!");
		h.setLocation(150, 200);

		JTextArea text = new JTextArea();
		h.getContentPane().add("Center", text);
		h.getContentPane().add("South", p);
		text.setText(" ---------------- Output -----------------\n\n" + "The server is busy at the moment.\n\n" + "Do you want JFtp to go to disappear and try to login\n" + "continuously?\n\n" + "(It will show up again when it has initiated a connection)\n\n");
		JFtp.log.setText("");
		text.setEditable(false);
		h.pack();
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

	private void tryFtpAgain(int response, String htmp, String ptmp, String utmp, String potmp, String dtmp, boolean useLocal) {
		//*** FOR TESTING PURPOSES
		//System.out.println(htmp + " " + ptmp + " " + utmp);
		//System.out.println(potmp + " " + dtmp);
		//***
		if ((response == FtpConnection.OFFLINE) && Settings.reconnect) {
			//FtpConnection con;
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			h.setVisible(true);

			while (mode == 0) {
				pause(10);
			}

			JFtp.mainFrame.setVisible(false);

			while ((response == FtpConnection.OFFLINE) && (mode == 1)) {
				System.out.print("Server is full, next attempt in ");

				int r = 5;

				for (int i = 0; i < r; r--) {
					System.out.print("" + r + "-");

					try {
						Thread.sleep(1000);
					} catch (Exception ex) {
					}
				}

				System.out.println("0...");

				response = StartConnection.startFtpCon(htmp, utmp, ptmp, Integer.parseInt(potmp), dtmp, useLocal);
			}

			if (mode == 1) {
				JFtp.mainFrame.setVisible(true);
			} else {
				// Switch windows
				JFtp.mainFrame.setVisible(false);
				this.setVisible(true);
				this.toFront();

			}
		} else if ((response != FtpConnection.LOGIN_OK) || ((response == FtpConnection.OFFLINE) && (!Settings.reconnect))) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			//setFilesystemConnection();
			if (useLocal) {
				JFtp.statusP.jftp.closeCurrentLocalTab();
			} else {
				JFtp.statusP.jftp.closeCurrentTab();
			}

			//this.setVisible(true);
			//this.toFront();
		}
	}

}
