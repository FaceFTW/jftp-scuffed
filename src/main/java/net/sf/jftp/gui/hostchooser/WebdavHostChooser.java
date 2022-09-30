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


public class WebdavHostChooser extends HFrame implements ActionListener, WindowListener {
	public static final HTextField host = new HTextField("URL:", "http://localhost", 35);
	public static final HTextField user = new HTextField("Username:", "guest");

	//public static HTextField port = new HTextField("Port:","22");
	public static final HPasswordField pass = new HPasswordField("Password:", "nopasswd");
	private final HPanel okP = new HPanel();
	private final HButton ok = new HButton("Connect");
	private ComponentListener listener = null;
	private boolean useLocal = false;

	public WebdavHostChooser(ComponentListener l, boolean local) {
		super();
		this.listener = l;
		this.useLocal = local;
		this.init();
	}

	public WebdavHostChooser(ComponentListener l) {
		super();
		this.listener = l;
		this.init();
	}

	public WebdavHostChooser() {
		super();
		this.init();
	}

	public void init() {
		//setSize(500, 200);
		this.setLocation(100, 150);
		this.setTitle("WebDAV Connection... (ALPHA STATE)");
		this.setBackground(this.okP.getBackground());

		host.setMinimumSize(new Dimension(500, 50));
		this.getContentPane().setLayout(new BorderLayout(5, 5));
		this.getContentPane().add("North", host);

		HPanel p = new HPanel();
		p.setLayout(new GridLayout(2, 2, 5, 3));


		p.add(user);
		p.add(pass);
		p.add(new JLabel(""));
		p.add(this.okP);

		this.okP.add(this.ok);

		this.getContentPane().add("South", p);

		this.ok.addActionListener(this);

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		pass.text.addActionListener(this);

		this.setModal(false);
		this.setVisible(false);
		this.addWindowListener(this);
		host.setText("http://www.planetpdf.com/planetpdf/webdavdemo/");
		user.setText("guest");
		pass.setText("guest");

		this.pack();
	}

	public void update() {
		this.setVisible(true);
		this.toFront();
		host.requestFocus();
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.ok) || (e.getSource() == pass.text)) {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

			String htmp = host.getText().trim();
			String utmp = user.getText().trim();
			String ptmp = pass.getText();

			net.sf.jftp.net.wrappers.WebdavConnection con;

			if (this.useLocal) {
				con = new net.sf.jftp.net.wrappers.WebdavConnection(htmp, utmp, ptmp, (net.sf.jftp.net.ConnectionListener) net.sf.jftp.JFtp.localDir);
				net.sf.jftp.JFtp.statusP.jftp.addLocalConnection("Webdav", con);
				con.chdir(htmp);
			} else {
				con = new net.sf.jftp.net.wrappers.WebdavConnection(htmp, utmp, ptmp, (net.sf.jftp.net.ConnectionListener) net.sf.jftp.JFtp.remoteDir);
				net.sf.jftp.JFtp.statusP.jftp.addConnection("Webdav", con);
				con.chdir(htmp);
			}

			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();
			net.sf.jftp.JFtp.mainFrame.setVisible(true);
			net.sf.jftp.JFtp.mainFrame.toFront();

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
