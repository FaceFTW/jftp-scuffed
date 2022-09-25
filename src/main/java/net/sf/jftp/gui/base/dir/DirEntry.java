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
	// to check file permissions
	public static final int R = FtpConnection.R;
	public static final int W = FtpConnection.W;
	public static final int DENIED = FtpConnection.DENIED;
	final static Object[] extensions = {new String[]{Settings.textFileImage, ".txt", ".doc", ".rtf"}, new String[]{Settings.htmlFileImage, ".htm", ".html"}, new String[]{Settings.zipFileImage, ".arj", ".bz", ".bz2", ".deb", ".jar", ".gz", ".rav", ".rpm", ".tar", ".tgz", ".zip", ".z", ".iso"}, new String[]{Settings.imageFileImage, "bmp", ".gif", ".jpg", ".png", ".xbm", ".xpm"}, new String[]{Settings.codeFileImage, ".c", ".cc", ".h", ".java"}, new String[]{Settings.audioFileImage, ".au", ".mid", ".midi", ".mp3", ".wav"}, new String[]{Settings.execFileImage, ".bat", ".csh", ".cgi", ".com", ".class", ".cmd", ".csh", ".dtksh", ".exe", ".ksh", ".pdksh", ".pl", ".sh", ".tcl", ".tksh", ".zsh"}, new String[]{Settings.presentationFileImage, ".ppt"}, new String[]{Settings.spreadsheetFileImage, ".xls"}, new String[]{Settings.videoFileImage, ".asf", ".avi", ".mpg", "mpeg", ".wmf"}};
	static Hashtable extensionMap = new Hashtable();

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
		this.file = file;
		this.who = who;
		setFile();
	}

	public void setFile() {
		String f = file;

		if ((f.indexOf("<") >= 0) && (f.indexOf(">") >= 0)) {
			f = file.substring(file.indexOf("<") + 1);
			f = f.substring(0, f.lastIndexOf(">"));
		}

		int lastIndex = f.lastIndexOf(".");
		String image = Settings.fileImage; // default

		if (lastIndex != -1) {
			String ext = f.substring(lastIndex);
			String tmp = (String) extensionMap.get(ext.toLowerCase());

			if (tmp != null) // we found an extension, let's use it's image
			{
				image = tmp;
			}
		}

		// else use the default
		img = HImage.getImage(c, image);

		if (img == null) {
			img = HImage.getImage(c, Settings.fileImage);
		}

		isFile = true;
		isDirectory = false;
	}

	public void setDirectory() {
		img = HImage.getImage(c, Settings.dirImage);
		isFile = false;
		isDirectory = true;
	}

	public void setNoRender() {
		noRender = true;
	}

	public boolean getNoRender() {
		return noRender;
	}

	public int getPermission() {
		return accessible;
	}

	public void setPermission(int what) {
		accessible = what;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public boolean isFile() {
		return isFile;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean state) {
		selected = state;
	}

	public String toString() {
		return file;
	}

	public Image getImage() {
		return img;
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(img);
	}

	public String getDate() {
		if (date == null) {
			return "";
		}

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

		return df.format(date);
	}

	public void setDate(Date d) {
		date = d;
	}

	public String getFileSize() {
		if (isDirectory() || (size < 0)) {
			return "          ";
		}

		long rsize = size;

		String type = "bs";

		if (rsize > 1024) {
			rsize = rsize / 1024;
			type = "kb";
		}

		if (rsize > 1024) {
			rsize = rsize / 1024;
			type = "mb";
		}

		if (rsize > 1024) {
			rsize = rsize / 1024;
			type = "gb";
		}

		String x = Long.toString(rsize);

		while (x.length() < 4) {
			x = " " + x;
		}

		return x + " " + type + " > ";
	}

	public void setFileSize(long s) {
		size = s;
	}

	public long getRawSize() {
		return size;
	}

	public void setLink() {
		img = HImage.getImage(c, Settings.linkImage);
		file = file.substring(0, file.lastIndexOf("###"));
		isLink = true;
	}

	public boolean isLink() {
		return isLink;
	}

	public long getTransferred() {
		return transferred;
	}

	public void setTransferred(long transferred) {
		this.transferred = transferred;
	}


}
