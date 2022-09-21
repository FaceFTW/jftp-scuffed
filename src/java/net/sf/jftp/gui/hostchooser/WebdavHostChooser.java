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

import net.sf.jftp.JFtp;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HPasswordField;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.wrappers.WebdavConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class WebdavHostChooser extends HFrame implements ActionListener, WindowListener {
	public static HTextField host = new HTextField("URL:", "http://localhost", 35);
	public static HTextField user = new HTextField("Username:", "guest");

	//public static HTextField port = new HTextField("Port:","22");
	public static HPasswordField pass = new HPasswordField("Password:", "nopasswd");
	private final HPanel okP = new HPanel();
	private final HButton ok = new HButton("Connect");
	private ComponentListener listener = null;
	private boolean useLocal = false;

	public WebdavHostChooser(ComponentListener l, boolean local) {
		listener = l;
		useLocal = local;
		init();
	}

	public WebdavHostChooser(ComponentListener l) {
		listener = l;
		init();
	}

	public WebdavHostChooser() {
		init();
	}

	public void init() {
		//setSize(500, 200);
		setLocation(100, 150);
		setTitle("WebDAV Connection... (ALPHA STATE)");
		setBackground(okP.getBackground());

		host.setMinimumSize(new Dimension(500, 50));
		getContentPane().setLayout(new BorderLayout(5, 5));
		getContentPane().add("North", host);

		HPanel p = new HPanel();
		p.setLayout(new GridLayout(2, 2, 5, 3));

		//***MY CHANGES
        /*
        try {
                File f = new File(Settings.appHomeDir);
                f.mkdir();
                File f1 = new File(Settings.login);
                f1.createNewFile();
                File f2 = new File(Settings.login_def_sftp);
                f2.createNewFile();
                File f3 = new File(Settings.ls_out);
                f3.createNewFile();
                File f4 = new File(Settings.sortls_out);
                f4.createNewFile();
                File f5 = new File(Settings.sortsize_out);
                f5.createNewFile();
                File f6 = new File(Settings.permissions_out);
                f6.createNewFile();
        } catch (IOException ex) {
                ex.printStackTrace();
        }

        LoadSet l = new LoadSet();
        String login[] = l.loadSet(Settings.login_def_sftp);


        if (login[0] != null) {
                host.setText(login[0]);
                user.setText(login[1]);

        }
        */
        /*
        else {
                host.setText("localhost");
                user.setText("guest");

        }
        */
        /*
        if (Settings.getStorePasswords()) {
                if (login != null) {
                        pass.setText(login[2]);
                }

        } else
                pass.setText("");

        */
		//***end of my changes (for this section)
		//getContentPane().add(host);
		//getContentPane().add(new JLabel(" "));//port);
		//getContentPane()
		p.add(user);

		//getContentPane()
		p.add(pass);

		//getContentPane()
		p.add(new JLabel(""));

		//getContentPane()
		p.add(okP);

		okP.add(ok);

		getContentPane().add("South", p);

		ok.addActionListener(this);

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		pass.text.addActionListener(this);

		setModal(false);
		setVisible(false);
		addWindowListener(this);

		//novell webdav test site
		host.setText("http://www.planetpdf.com/planetpdf/webdavdemo/");
		user.setText("guest");
		pass.setText("guest");

		pack();
	}

	public void update() {
		setVisible(true);
		toFront();
		host.requestFocus();
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == ok) || (e.getSource() == pass.text)) {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			String htmp = host.getText().trim();
			String utmp = user.getText().trim();
			String ptmp = pass.getText();

			WebdavConnection con;

			if (useLocal) {
				con = new WebdavConnection(htmp, utmp, ptmp, (ConnectionListener) JFtp.localDir);
				JFtp.statusP.jftp.addLocalConnection("Webdav", con);
				con.chdir(htmp);
			} else {
				con = new WebdavConnection(htmp, utmp, ptmp, (ConnectionListener) JFtp.remoteDir);
				JFtp.statusP.jftp.addConnection("Webdav", con);
				con.chdir(htmp);
			}

			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			this.dispose();
			JFtp.mainFrame.setVisible(true);
			JFtp.mainFrame.toFront();

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

    /*
    public Insets getInsets()
    {
        Insets std = super.getInsets();

        return new Insets(std.top + 10, std.left + 10, std.bottom + 10,
                          std.right + 10);
    }
    */

	public void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}
}
