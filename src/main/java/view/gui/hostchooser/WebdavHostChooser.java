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

import view.JFtp;
import view.gui.framework.HButton;
import view.gui.framework.HFrame;
import view.gui.framework.HPanel;
import view.gui.framework.HPasswordField;
import view.gui.framework.HTextField;
import model.net.ConnectionListener;
import model.net.wrappers.WebdavConnection;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class WebdavHostChooser extends HFrame implements ActionListener, WindowListener {
	private static final HTextField host = new HTextField("URL:", "http://localhost", 35);
	private static final HTextField user = new HTextField("Username:", "guest");

	//public static HTextField port = new HTextField("Port:","22");
	private static final HPasswordField pass = new HPasswordField("Password:", "nopasswd");
	private final HPanel okP = new HPanel();
	private final HButton ok = new HButton("Connect");
	private ComponentListener listener;
	private boolean useLocal;

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

	private void init() {
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

		this.getContentPane().add(JFtp.SOUTH, p);

		this.ok.addActionListener(this);

		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

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

			WebdavConnection con;

			if (this.useLocal) {
				con = new WebdavConnection(htmp, utmp, ptmp, (ConnectionListener) JFtp.localDir);
				JFtp.statusP.jftp.addLocalConnection("Webdav", con);
				con.chdir(htmp);
			} else {
				con = new WebdavConnection(htmp, utmp, ptmp, (ConnectionListener) JFtp.remoteDir);
				JFtp.statusP.jftp.addConnection("Webdav", con);
				con.chdir(htmp);
			}

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
