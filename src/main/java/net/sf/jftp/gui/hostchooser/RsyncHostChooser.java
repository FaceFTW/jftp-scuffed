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
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HInsetPanel;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HPasswordField;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.wrappers.StartConnection;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class RsyncHostChooser extends HFrame implements ActionListener, WindowListener {


	public final HTextField host = new HTextField("Hostname:", "localhost        ");
	public final HTextField user = new HTextField("Username:", "anonymous        ");

	//public static HTextField pass = new HTextField("Password:","none@nowhere.no");
	public final HPasswordField pass = new HPasswordField("Password:", "none@nowhere.no");
	public final HTextField port = new HTextField("Port:    ", "21");
	public final HTextField cwd = new HTextField("Remote:  ", Settings.defaultDir);
	public final HTextField lcwd = new HTextField("Local:   ", Settings.defaultWorkDir);
	//	public HTextField dl = new HTextField("Max. connections:    ", "3");
	public HTextField crlf = new HTextField("Override server newline:    ", "<default>");

	private final HPanel okP = new HPanel();
	private final HButton ok = new HButton("Connect");
	private final HButton backMode = new HButton("Yes");
	private final HButton frontMode = new HButton("No");
	private final HFrame h = new HFrame();
	private final HPanel listP = new HPanel();
	//	private HButton list = new HButton("Choose from or edit list...");
	private ComponentListener listener = null;
	private final boolean ext = Settings.showNewlineOption;

	public RsyncHostChooser(ComponentListener l, boolean local) {
		super();
		this.listener = l;
		this.init();
	}

	public RsyncHostChooser(ComponentListener l) {
		super();
		this.listener = l;
		this.init();
	}

	public RsyncHostChooser() {
		super();
		this.init();
	}

	public void init() {


		this.setTitle("RSync Connection...");
		this.setBackground(this.okP.getBackground());

//		anonBox.setSelected(false);
		this.user.setEnabled(true);
		this.pass.text.setEnabled(true);

		try {
			LoadSet l = new LoadSet();
			String[] login = net.sf.jftp.config.LoadSet.loadSet(Settings.login_def);

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

		root.add(new JLabel(" "), "wrap");

		root.add(this.cwd, "wrap");
		root.add(this.lcwd, "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(new JLabel(" "), "wrap");

		root.add(this.listP);

		root.add(this.okP, "align right");
		this.okP.add(this.ok);
		this.ok.addActionListener(this);

		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		this.lcwd.setEnabled(true);
		this.cwd.setEnabled(true);
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
			System.out.println(this.lcwd.getText());
			System.out.println(this.cwd.getText());
			System.out.println(this.host.getText());
			System.out.println(this.user.getText());
			System.out.println(this.port.getText());
			System.out.println(this.pass.getText());

//			public HTextField host = new HTextField("Hostname:", "localhost        ");
//			public HTextField user = new HTextField("Username:", "anonymous        ");
//
//			//public static HTextField pass = new HTextField("Password:","none@nowhere.no");
//			public HPasswordField pass = new HPasswordField("Password:", "none@nowhere.no");
//			public HTextField port = new HTextField("Port:    ", "21");
//			public HTextField cwd = new HTextField("Remote:  ", Settings.defaultDir);
//			public HTextField lcwd = new HTextField("Local:   ", Settings.defaultWorkDir);

//			if (false) {
//				String source = sourcePath;
//				String destination = url + ":" + destinationPath;
//
//				RSync rsync;
//				rsync = new RSync().sshPass(rpass).source(source).destination(destination).verbose(true).rsh(sshBinary() + "-o StrictHostKeyChecking=no");
//
//				ConsoleOutputProcessOutput output = new ConsoleOutputProcessOutput();
//				output.monitor(rsync.builder());
//			}

//			FtpURLConnection uc = new FtpURLConnection(new java.net.URL(url));
//			FtpConnection con = uc.getFtpConnection();

//			JFtp.statusP.jftp.addConnection(url, con);

//			uc.connect();

//			int response = uc.getLoginResponse();

//			if (response != FtpConnection.LOGIN_OK) {
//				setTitle("Wrong password!");
//				host.setText(uc.getHost());
//				port.setText(Integer.toString(uc.getPort()));
//				user.setText(uc.getUser());
//				pass.setText(uc.getPass());
//				setVisible(true);
//				toFront();
//				host.requestFocus();
//			} else {
//				this.dispose();

//				if (listener != null) {
//					listener.componentResized(new ComponentEvent(this, 0));
//				}

			net.sf.jftp.JFtp.mainFrame.setVisible(true);
			net.sf.jftp.JFtp.mainFrame.toFront();
		} catch (Exception ex) {
			Log.debug("Error!");
			ex.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.ok) || (e.getSource() == this.pass.text)) {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

//			FtpConnection con = null;
			net.sf.jftp.JFtp.setHost(this.host.getText());

			String htmp = StringUtils.cut(this.host.getText(), " ");
			String utmp = StringUtils.cut(this.user.getText(), " ");
			String ptmp = StringUtils.cut(this.pass.getText(), " ");
			String potmp = StringUtils.cut(this.port.getText(), " ");

			/* All the information of the current server are stored in JFtp.HostInfo */
			net.sf.jftp.JFtp.hostinfo.hostname = htmp;
			net.sf.jftp.JFtp.hostinfo.username = utmp;
			net.sf.jftp.JFtp.hostinfo.password = ptmp;
			net.sf.jftp.JFtp.hostinfo.port = potmp;
			net.sf.jftp.JFtp.hostinfo.type = "rsync";

//		int x = Integer.parseInt(dl.getText().trim());

			Settings.save();

			String dtmp;
			String ltmp;

			dtmp = this.cwd.getText();
			ltmp = this.lcwd.getText();

			SaveSet s = new SaveSet(Settings.login_def, htmp, utmp, ptmp, potmp, dtmp, ltmp);

			if (net.sf.jftp.JFtp.localDir instanceof FilesystemConnection) {
				if (!net.sf.jftp.JFtp.localDir.setPath(ltmp)) {
					if (!net.sf.jftp.JFtp.localDir.setPath(System.getProperty("user.home"))) {
						net.sf.jftp.JFtp.localDir.setPath("/");
					}
				}
			}

			int response = StartConnection.startRsyncCon(htmp, utmp, ptmp, Integer.parseInt(potmp), dtmp, ltmp);

			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();

			net.sf.jftp.JFtp.mainFrame.setVisible(true);
			net.sf.jftp.JFtp.mainFrame.toFront();

			if (null != this.listener) {
				this.listener.componentResized(new ComponentEvent(this, 0));
//		} else if (e.getSource() == list) {
//			HostList hl = new HostList(this);
//			FtpHost selectedHost = hl.getFtpHost();
//
//			if (selectedHost == null) {
//				return;
//			}
//
//			host.setText(selectedHost.hostname);
//			pass.setText(selectedHost.password);
//			user.setText(selectedHost.username);
//			port.setText(selectedHost.port);
//
//			Settings.setProperty("jftp.useDefaultDir", true);
//			dirBox.setSelected(Settings.getUseDefaultDir());
//			lcwd.setEnabled(!dirBox.isSelected());
//			cwd.setEnabled(!dirBox.isSelected());
//		} else if (e.getSource() == dirBox) {
//			if (!dirBox.isSelected()) {
//				lcwd.setEnabled(true);
//				cwd.setEnabled(true);
//			} else {
//				lcwd.setEnabled(false);
//				cwd.setEnabled(false);
//			}
//		} else if (e.getSource() == anonBox) {
//			if (!anonBox.isSelected()) {
//				user.setEnabled(true);
//				pass.text.setEnabled(true);
//			} else {
//				user.setText("anonymous");
//				pass.setText("no@no.no");
//				user.setEnabled(false);
//				pass.text.setEnabled(false);
//			}
//		} else if (e.getSource() == threadBox) {
//			if (threadBox.isSelected()) {
//				dl.setEnabled(true);
//			} else {
//				dl.setEnabled(false);
//			}
//	} else if(e.getSource()==backMode)

//	{
//		mode = 1;
//		h.setVisible(false);
//		;
			} else if (e.getSource() == this.frontMode) {
				final int mode = 2;
				this.h.setVisible(false);
			}
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

	public void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}
}
