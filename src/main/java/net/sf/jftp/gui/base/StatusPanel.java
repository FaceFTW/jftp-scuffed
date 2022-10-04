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
package net.sf.jftp.gui.base;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.GUIDefaults;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.hostchooser.HostChooser;
import net.sf.jftp.gui.hostchooser.NfsHostChooser;
import net.sf.jftp.gui.hostchooser.SftpHostChooser;
import net.sf.jftp.gui.hostchooser.SmbHostChooser;
import net.sf.jftp.gui.hostchooser.WebdavHostChooser;
import net.sf.jftp.gui.tasks.HttpBrowser;
import net.sf.jftp.gui.tasks.NativeHttpBrowser;
import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.wrappers.HttpTransfer;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class StatusPanel extends HPanel implements ActionListener {
	public static final StatusCanvas status = new StatusCanvas();
	public final HImageButton close = new HImageButton(Settings.closeImage, "close", "Close active tab...", this);
	public final JFtp jftp;
	private final JTextField address = new JTextField("http://www.xkcd.com", 30);

	public StatusPanel(JFtp jftp) {
		super();
		this.jftp = jftp;
		this.setLayout(new BorderLayout());

		JToolBar bar = new JToolBar();

		Insets in = bar.getMargin();
		bar.setMargin(new Insets(in.top + 2, in.left + 4, in.bottom + 2, in.right + 4));

		net.sf.jftp.gui.framework.HImageButton newcon = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.hostImage, "newcon", "Add FTP Connection...", this);
		bar.add(newcon);
		newcon.setSize(24, 24);
		newcon.setToolTipText("New FTP Connection...");
		bar.add(new JLabel(" "));

		net.sf.jftp.gui.framework.HImageButton smbcon = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.openImage, "smbcon", "Add SMB Connection...", this);
		bar.add(smbcon);
		smbcon.setSize(24, 24);
		smbcon.setToolTipText("New SMB Connection...");
		bar.add(new JLabel(" "));

		net.sf.jftp.gui.framework.HImageButton sftpcon = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.sftpImage, "sftpcon", "Add SFTP Connection...", this);
		bar.add(sftpcon);
		sftpcon.setSize(24, 24);
		sftpcon.setToolTipText("New SFTP Connection...");
		bar.add(new JLabel(" "));

		net.sf.jftp.gui.framework.HImageButton nfscon = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.nfsImage, "nfscon", "Add NFS Connection...", this);
		bar.add(nfscon);
		nfscon.setSize(24, 24);
		nfscon.setToolTipText("New NFS Connection...");
		bar.add(new JLabel(" "));

		net.sf.jftp.gui.framework.HImageButton webdavcon = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.webdavImage, "webdavcon", "Add WebDAV Connection...", this);
		if (Settings.enableWebDav) bar.add(webdavcon);
		webdavcon.setSize(24, 24);
		webdavcon.setToolTipText("New WebDAV Connection...");
		bar.add(new JLabel("   "));

		bar.add(this.close);
		this.close.setSize(24, 24);
		this.close.setToolTipText("Close Active Remote tab...");
		bar.add(new JLabel("    "));

		this.address.addActionListener(this);
		bar.add(new JLabel("URL: "));
		bar.add(this.address);
		bar.add(new JLabel(" "));
		net.sf.jftp.gui.framework.HImageButton go = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.refreshImage, "go", "Download URL now...", this);
		bar.add(go);


		go.setToolTipText("Download URL Now...");


		bar.add(new JLabel("    "));

		this.add("North", bar);

		this.validate();
		this.setFont(GUIDefaults.menuFont);
		this.setVisible(true);
	}

	public void status(String msg) {
		status.setText(msg);
	}

	public String getHost() {
		return status.getHost();
	}

	public void setHost(String host) {
		status.setHost(host);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("go") || (e.getSource() == this.address)) {
			Vector listeners = new Vector();
			listeners.add(JFtp.localDir);

			String url = this.address.getText().trim();

			this.startTransfer(url, JFtp.localDir.getPath(), listeners, JFtp.getConnectionHandler());
		} else if (e.getActionCommand().equals("smbcon")) {

			SmbHostChooser hc = new SmbHostChooser();
			hc.toFront();

			hc.update();
		} else if (e.getActionCommand().equals("sftpcon")) {

			SftpHostChooser hc = new SftpHostChooser();
			hc.toFront();

			hc.update();
		} else if (e.getActionCommand().equals("nfscon")) {
			NfsHostChooser hc = new NfsHostChooser();
			hc.toFront();

			hc.update();
		} else if (e.getActionCommand().equals("webdavcon")) {

			WebdavHostChooser hc = new WebdavHostChooser();
			hc.toFront();
			hc.update();
		} else if (e.getActionCommand().equals("close")) {
			this.jftp.closeCurrentTab();
		} else if (e.getActionCommand().equals("newcon") && (!JFtp.uiBlocked)) {

			HostChooser hc = new HostChooser();
			hc.toFront();
			hc.update();
		}
	}

	public void startTransfer(String url, String localPath, Vector listeners, ConnectionHandler handler) {
		if (url.startsWith("ftp://") && (url.endsWith("/") || (10 > url.lastIndexOf('/')))) {
			JFtp.safeDisconnect();

			HostChooser hc = new HostChooser();
			hc.update(url);
		} else if (url.startsWith("http://") && (url.endsWith("/") || (10 > url.lastIndexOf('/')))) {
			try {
				NativeHttpBrowser.main(new String[]{url});
			} catch (Throwable ex) {
				ex.printStackTrace();
				Log.debug("Native browser intialization failed, using JContentPane...");

				HttpBrowser h = new HttpBrowser(url);
				JFtp.desktop.add(h, new Integer(Integer.MAX_VALUE - 10));
			}
		} else {
			HttpTransfer t = new HttpTransfer(url, localPath, listeners, handler);
		}
	}

	public Insets getInsets() {
		Insets in = super.getInsets();

		return new Insets(in.top, in.left, in.bottom, in.right);
	}
}
