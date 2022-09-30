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
package net.sf.jftp.gui.base.dir;

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.net.FtpConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.Hashtable;


public class DirEntry {
	public static final int R = FtpConnection.R;
	public static final int W = FtpConnection.W;
	public static final int DENIED = FtpConnection.DENIED;
	static final Object[] extensions = {new String[]{Settings.textFileImage, ".txt", ".doc", ".rtf"}, new String[]{Settings.htmlFileImage, ".htm", ".html"}, new String[]{Settings.zipFileImage, ".arj", ".bz", ".bz2", ".deb", ".jar", ".gz", ".rav", ".rpm", ".tar", ".tgz", ".zip", ".z", ".iso"}, new String[]{Settings.imageFileImage, "bmp", ".gif", ".jpg", ".png", ".xbm", ".xpm"}, new String[]{Settings.codeFileImage, ".c", ".cc", ".h", ".java"}, new String[]{Settings.audioFileImage, ".au", ".mid", ".midi", ".mp3", ".wav"}, new String[]{Settings.execFileImage, ".bat", ".csh", ".cgi", ".com", ".class", ".cmd", ".csh", ".dtksh", ".exe", ".ksh", ".pdksh", ".pl", ".sh", ".tcl", ".tksh", ".zsh"}, new String[]{Settings.presentationFileImage, ".ppt"}, new String[]{Settings.spreadsheetFileImage, ".xls"}, new String[]{Settings.videoFileImage, ".asf", ".avi", ".mpg", "mpeg", ".wmf"}};
	static final Hashtable extensionMap = new Hashtable();

	static {
		for (Object extension : extensions) {
			String[] temp = (String[]) extension;

			for (int j = 1; j < temp.length; j++) {
				extensionMap.put(temp[j], temp[0]);
			}
		}
	}

	private final JLabel c = new JLabel();
	public String file = "";
	public boolean selected = false;
	public ActionListener who = null;
	public boolean isFile = true;
	public Date date = null;
	//private boolean entered = false;
	private Image img;
	private boolean isDirectory = false;
	private long size = 0;
	private long transferred = 0;
	private boolean isLink = false;
	private int accessible = -1;
	private boolean noRender = false;

	public DirEntry(String file, ActionListener who) {
		super();
		this.file = file;
		this.who = who;
		this.setFile();
	}

	public void setFile() {
		String f = this.file;

		if ((f.contains("<")) && (f.contains(">"))) {
			f = this.file.substring(this.file.indexOf('<') + 1);
			f = f.substring(0, f.lastIndexOf('>'));
		}

		int lastIndex = f.lastIndexOf('.');
		String image = Settings.fileImage; // default

		if (-1 != lastIndex) {
			String ext = f.substring(lastIndex);
			String tmp = (String) extensionMap.get(ext.toLowerCase());

			if (null != tmp)
			{
				image = tmp;
			}
		}

		// else use the default
		this.img = HImage.getImage(this.c, image);

		if (null == this.img) {
			this.img = HImage.getImage(this.c, Settings.fileImage);
		}

		this.isFile = true;
		this.isDirectory = false;
	}

	public void setDirectory() {
		this.img = HImage.getImage(this.c, Settings.dirImage);
		this.isFile = false;
		this.isDirectory = true;
	}

	public void setNoRender() {
		this.noRender = true;
	}

	public boolean getNoRender() {
		return this.noRender;
	}

	public int getPermission() {
		return this.accessible;
	}

	public void setPermission(int what) {
		this.accessible = what;
	}

	public boolean isDirectory() {
		return this.isDirectory;
	}

	public boolean isFile() {
		return this.isFile;
	}

	public boolean isSelected() {
		return this.selected;
	}

	public void setSelected(boolean state) {
		this.selected = state;
	}

	public String toString() {
		return this.file;
	}

	public Image getImage() {
		return this.img;
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(this.img);
	}

	public String getDate() {
		if (null == this.date) {
			return "";
		}

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

		return df.format(this.date);
	}

	public void setDate(Date d) {
		this.date = d;
	}

	public String getFileSize() {
		if (this.isDirectory || (0 > this.size)) {
			return "          ";
		}

		long rsize = this.size;

		String type = "bs";

		if (1024 < rsize) {
			rsize = rsize / 1024;
			type = "kb";
		}

		if (1024 < rsize) {
			rsize = rsize / 1024;
			type = "mb";
		}

		if (1024 < rsize) {
			rsize = rsize / 1024;
			type = "gb";
		}

		StringBuilder x = new StringBuilder(Long.toString(rsize));

		while (4 > x.length()) {
			x.insert(0, " ");
		}

		return x + " " + type + " > ";
	}

	public void setFileSize(long s) {
		this.size = s;
	}

	public long getRawSize() {
		return this.size;
	}

	public void setLink() {
		this.img = HImage.getImage(this.c, Settings.linkImage);
		this.file = this.file.substring(0, this.file.lastIndexOf("###"));
		this.isLink = true;
	}

	public boolean isLink() {
		return this.isLink;
	}

	public long getTransferred() {
		return this.transferred;
	}

	public void setTransferred(long transferred) {
		this.transferred = transferred;
	}


}
