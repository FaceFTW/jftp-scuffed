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

import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HInsetPanel;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HPasswordField;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.gui.tasks.ExternalDisplayer;

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


public class NfsHostChooser extends HFrame implements ActionListener, WindowListener {
	public static final HTextField host = new HTextField("URL:", "nfs://localhost:v2m/tmp", 20);
	public static final HTextField user = new HTextField("Username:", "<anonymous>", 15);

	//public static HTextField pass = new HTextField("Password:","none@nowhere.no");
	public static final HPasswordField pass = new HPasswordField("Password:", "nopasswd");
	public static final HButton info = new HButton("Read me!");
	private final HPanel okP = new HPanel();
	private final HButton ok = new HButton("Connect");
	private ComponentListener listener = null;
	private boolean useLocal = false;

	public NfsHostChooser(ComponentListener l, boolean local) {
		listener = l;
		useLocal = local;
		this.init();
	}

	public NfsHostChooser(ComponentListener l) {
		listener = l;
		this.init();
	}

	public NfsHostChooser() {
		this.init();
	}

	public void init() {
		//setSize(600, 220);
		this.setLocation(100, 150);
		this.setTitle("NFS Connection...");
		this.setBackground(okP.getBackground());

		JPanel p = new JPanel();
		p.add(info);

		//*** MY ADDITIONS
		try {
			File f = new File(net.sf.jftp.config.Settings.appHomeDir);
			f.mkdir();

			File f1 = new File(net.sf.jftp.config.Settings.login);
			f1.createNewFile();

			File f2 = new File(net.sf.jftp.config.Settings.login_def_nfs);
			f2.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		net.sf.jftp.config.LoadSet l = new net.sf.jftp.config.LoadSet();
		String[] login = net.sf.jftp.config.LoadSet.loadSet(net.sf.jftp.config.Settings.login_def_nfs);

		if ((login[0] != null) && (login.length > 1)) {
			host.setText(login[0]);
			user.setText(login[1]);
		}

        /*
                host.setText("nfs://localhost:v2m/tmp");
                user.setText("guest");

        }
        */
		if (net.sf.jftp.config.Settings.getStorePasswords()) {
			if ((login[0] != null) && (login.length > 2) && (login[2] != null)) {
				pass.setText(login[2]);
			}
		} else {
			pass.setText("");
		}

		HInsetPanel root = new HInsetPanel();
		root.setLayout(new GridLayout(4, 2, 5, 3));

		root.add(host);
		root.add(p);
		root.add(user);
		root.add(pass);

		root.add(new JLabel(""));
		root.add(okP);

		okP.add(ok);

		this.getContentPane().setLayout(new BorderLayout(10, 10));
		this.getContentPane().add("Center", root);

		ok.addActionListener(this);
		info.addActionListener(this);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		pass.text.addActionListener(this);

		this.pack();
		this.setModal(false);
		this.setVisible(false);
		this.addWindowListener(this);
	}

	public void update() {
		this.fixLocation();
		this.setVisible(true);
		this.toFront();
		host.requestFocus();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == info) {
			java.net.URL url = ClassLoader.getSystemResource(net.sf.jftp.config.Settings.nfsinfo);

			if (url == null) {
				url = HImage.class.getResource("/" + net.sf.jftp.config.Settings.nfsinfo);
			}

			ExternalDisplayer d = new ExternalDisplayer(url);
		} else if ((e.getSource() == ok) || (e.getSource() == pass.text)) {
			// Switch windows
			//this.setVisible(false);
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			net.sf.jftp.net.wrappers.NfsConnection con = null;

			String htmp = host.getText().trim();
			String utmp = user.getText().trim();
			String ptmp = pass.getText();

			//*** MY ADDITIONS
			int potmp = 0; //*** just filler for the port number

			String userName = user.text.getText();

			//***
			try {
				boolean status;
				status = net.sf.jftp.net.wrappers.StartConnection.startCon("NFS", htmp, userName, ptmp, potmp, "", useLocal);

                /*

                con = new NfsConnection(htmp);
                //JFtp.remoteDir.setCon(con);
                //con.addConnectionListener(((ConnectionListener)JFtp.remoteDir));

                //JFtp.statusP.jftp.addConnection(htmp, con);

                if(!userName.equals("<anonymous>")) ((NfsConnection)con).login(utmp,ptmp);

                if(useLocal)
                {
                 con.setLocalPath("/");
                      JFtp.statusP.jftp.addLocalConnection(htmp, con);
                }
                else JFtp.statusP.jftp.addConnection(htmp, con);

                con.chdir(htmp);

                //con.setLocalPath(JFtp.localDir.getCon().getPWD());
                //con.addConnectionListener((ConnectionListener) JFtp.localDir);



                */
			} catch (Exception ex) {
				net.sf.jftp.system.logging.Log.debug("Could not create NfsConnection!");
			}

			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();
			net.sf.jftp.JFtp.mainFrame.setVisible(true);
			net.sf.jftp.JFtp.mainFrame.toFront();

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
