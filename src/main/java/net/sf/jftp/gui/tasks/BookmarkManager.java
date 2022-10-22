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
package net.sf.jftp.gui.tasks;

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.I18nHelper;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


public class BookmarkManager extends JInternalFrame implements ActionListener {
	private final JTextArea info = new JTextArea(25, 50);
	private final JButton close = new JButton(I18nHelper.getUIString("close"));

	public BookmarkManager() {
		super(I18nHelper.getUIString("manage.bookmarks1"), true, true, true, true);
		this.setLocation(50, 50);
		this.setSize(600, 540);
		this.getContentPane().setLayout(new BorderLayout());

		this.load(Settings.bookmarks);

		JScrollPane jsp = new JScrollPane(this.info);
		this.getContentPane().add(I18nHelper.getUIString("center1"), jsp);

		HPanel closeP = new HPanel();
		closeP.setLayout(new FlowLayout(FlowLayout.CENTER));

		//closeP.add(close);
		javax.swing.JButton save = new javax.swing.JButton(I18nHelper.getUIString("save.and.close"));
		closeP.add(save);

		this.close.addActionListener(this);
		save.addActionListener(this);

		this.getContentPane().add(I18nHelper.getUIString("south1"), closeP);

		this.info.setCaretPosition(0);
		this.pack();
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.close) {
			this.dispose();
		} else {
			this.save(Settings.bookmarks);
			net.sf.jftp.JFtp.menuBar.loadBookmarks();
			this.dispose();
		}
	}

	private void setDefaultText() {
		this.info.setText("");
		this.info.append("# JFtp Bookmark Configuration file\n");
		this.info.append("#\n");
		this.info.append("# Syntax: protocol#host#user#password#port#dir/domain#local\n");
		this.info.append("#\n");
		this.info.append("# Note: not all values are used by every connection, but all fields must contain at least\n");
		this.info.append("# one character.\n");
		this.info.append("Use \"<%hidden%>\" for password fields you don't want to fill out.");
		this.info.append("#\n");
		this.info.append("# protocol: FTP, SFTP, SMB or NFS (uppercase)\n");
		this.info.append("# host: hostname or ip for ftp + sftp, valid url for smb + nfs  (\"(LAN)\" for smb lan browsing)\n");
		this.info.append("# user, password: the login data\n");
		this.info.append("# port: this must be a number (even if it is not used for smb+nfs, set it in the url for nfs)\n");
		this.info.append("# dir/domain: inital directory for the connection, domainname for smb\n");
		this.info.append("# local: \"true\" if connection should be opened in local tab, \"false\" otherwise\n");
		this.info.append("# directories must be included in <dir></dir> tags and can be ended" + " using a single\n# <enddir> tag");
		this.info.append("#\n");
		this.info.append("#\n");
		this.info.append("\n<dir>JFtp</dir>\n");
		this.info.append("FTP#upload.sourceforge.net#anonymous#j-ftp@sf.net#21#/incoming#false\n");
		this.info.append("<enddir>\n");
		this.info.append("\n");
		this.info.append("FTP#ftp.kernel.org#anonymous#j-ftp@sf.net#21#/pub/linux/kernel/v2.6#false\n");
		this.info.append("\n");
		this.info.append("SMB#(LAN)#guest#guest#-1#-#false\n\n");
	}

	private void load(String file) {
		String data = "";
		StringBuilder now = new StringBuilder();

		try {
			DataInput in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

			while (null != (data = in.readLine())) {
				now.append(data).append("\n");
			}
		} catch (IOException e) {
			Log.debug("No bookmarks.txt found, using defaults.");

			this.setDefaultText();

			return;
		}

		this.info.setText(now.toString());
	}

	private void save(String file) {
		try {
			PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));

			out.println(this.info.getText());
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.debug(e + " @BookmarkManager.save()");
		}
	}

	public Insets getInsets() {
		Insets std = super.getInsets();

		return new Insets(std.top + 5, std.left + 5, std.bottom + 5, std.right + 5);
	}
}
