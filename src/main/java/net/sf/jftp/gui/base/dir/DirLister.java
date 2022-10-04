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
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.system.LocalIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;


public class DirLister implements ActionListener {
	private final BasicConnection con;
	public boolean finished = false;
	private int length;
	private String[] files;
	private String[] sizes;
	private int[] perms;
	private boolean isDirectory = true;
	private String sortMode = null;
	private Date[] dates = null;

	public DirLister(BasicConnection con) {
		super();
		this.con = con;
		this.init();
	}

	public DirLister(BasicConnection con, String sortMode) {
		super();
		this.con = con;
		this.sortMode = sortMode;
		this.init();
	}

	public DirLister(BasicConnection con, String sortMode, boolean hide) {
		super();
		this.con = con;
		this.sortMode = sortMode;
		this.init();

		int cnt = this.files.length;

		if (hide) {
			for (int i = 0; i < this.files.length; i++) {
				if (this.files[i].startsWith(".") && !this.files[i].startsWith("..")) {
					this.files[i] = null;
					cnt--;
				}
			}

			String[] newFiles = new String[cnt];
			String[] newSizes = new String[cnt];
			int[] newPerms = new int[cnt];

			int idx = 0;
			for (int i = 0; i < this.files.length; i++) {
				if (null == this.files[i]) {
				} else {
					newFiles[idx] = this.files[i];
					newSizes[idx] = this.sizes[i];
					newPerms[idx] = this.perms[i];
					idx++;
				}
			}

			this.files = newFiles;
			this.sizes = newSizes;
			this.perms = newPerms;
			this.length = this.files.length;
		}
	}

	public void init() {
		try {
			String outfile = Settings.ls_out;

			this.con.list();
			this.files = this.con.sortLs();
			this.sizes = this.con.sortSize();

			this.length = this.files.length;
			this.perms = this.con.getPermissions();
			this.isDirectory = true;

			if (null != this.sortMode) {
				if (!this.sortMode.equals("Date")) {
					this.sortFirst();
				}

				this.sort(this.sortMode);
			} else if (null == this.sortMode) {
				this.sortFirst();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			this.isDirectory = false;
		}

		this.finished = true;
	}

	private void sort(String type) {
		Vector fv = new Vector();
		Vector sv = new Vector();
		Vector pv = new Vector();

		if (type.equals("Reverse")) {
			for (int i = 0; i < this.length; i++) {
				fv.add(this.files[i]);
				sv.add(this.sizes[i]);

				if (null != this.perms) {
					pv.add(this.perms[i]);
				}
			}

			fv.sort(java.util.Collections.reverseOrder());

			Object[] filesTmp = fv.toArray();
			Object[] sizesTmp = sv.toArray();
			Object[] permsTmp = null;

			if (null != this.perms) {
				permsTmp = pv.toArray();
			}

			for (int i = 0; i < this.length; i++) {
				this.files[i] = (String) filesTmp[i];
				this.sizes[i] = (String) sizesTmp[this.length - i - 1];

				if (null != this.perms) {
					this.perms[i] = (Integer) permsTmp[this.length - i - 1];
				}
			}
		} else if (type.startsWith("Size")) {
			int cnt = 0;
			Hashtable processed = new Hashtable();
			boolean reverse = type.endsWith("/Re");

			while (cnt < this.length) {
				int idx = 0;
				double current = 0;

				if (reverse) {
					current = Double.MAX_VALUE;
				}

				for (int i = 0; i < this.length; i++) {
					if (processed.containsKey("" + i)) {
						continue;
					}

					int si = Integer.parseInt(this.sizes[i]);

					if (!reverse && (si >= current)) {
						idx = i;
						current = si;
					} else if (reverse && (si <= current)) {
						idx = i;
						current = si;
					}
				}

				processed.put("" + idx, "" + this.sizes[idx]);
				fv.add(this.files[idx]);
				sv.add(this.sizes[idx]);

				if (null != this.perms) {
					pv.add(this.perms[idx]);
				}

				cnt++;
			}

			for (int i = 0; i < this.length; i++) {
				this.files[i] = (String) fv.elementAt(i);
				this.sizes[i] = (String) sv.elementAt(i);

				if (null != this.perms) {
					this.perms[i] = (Integer) pv.elementAt(i);
				}
			}
		} else if (type.equals("Date")) {
			String style = "ftp";

			//TODO: may be slow
			if (!(this.con instanceof FtpConnection) || (null == ((net.sf.jftp.net.FtpConnection) this.con).dateVector) || (1 > ((net.sf.jftp.net.FtpConnection) this.con).dateVector.size())) {
				if (!(this.con instanceof FilesystemConnection) || (null == ((net.sf.jftp.net.FilesystemConnection) this.con).dateVector) || (1 > ((net.sf.jftp.net.FilesystemConnection) this.con).dateVector.size())) {
				} else {
					style = "file";
				}
			}

			Object[] date = null;

			if (style.equals("ftp")) {
				date = ((FtpConnection) this.con).dateVector.toArray();

			} else {
				date = ((FilesystemConnection) this.con).dateVector.toArray();
			}

			for (int j = 0; j < date.length; j++) {
				for (int i = 0; i < date.length; i++) {
					Date x = (Date) date[i];

					if (i == (date.length - 1)) {
						break;
					}

					if (this.comp(x, (Date) date[i + 1])) {
						Date swp = (Date) date[i + 1];
						date[i + 1] = x;
						date[i] = swp;

						String s1 = this.files[i + 1];
						String s2 = this.files[i];
						this.files[i] = s1;
						this.files[i + 1] = s2;

						s1 = this.sizes[i + 1];
						s2 = this.sizes[i];
						this.sizes[i] = s1;
						this.sizes[i + 1] = s2;

						int s3 = this.perms[i + 1];
						int s4 = this.perms[i];
						this.perms[i] = s3;
						this.perms[i + 1] = s4;
					}
				}
			}

			this.dates = new Date[date.length];

			for (int i = 0; i < this.dates.length; i++) {
				this.dates[i] = (Date) date[i];
			}

		} else if (type.equals("Normal")) {
			// already done.
		}
	}

	private boolean comp(Date one, Date two) {
		Calendar c = new GregorianCalendar();
		c.clear();
		c.setTime(one);

		Calendar c2 = new GregorianCalendar();
		c2.clear();
		c2.setTime(two);

		return 0 < c.getTime().compareTo(c2.getTime());
	}

	public void sortFirst() {
		String[] tmpx = new String[this.length];

		for (int x = 0; x < this.length; x++) {
			if (null != this.perms) {
				tmpx[x] = this.files[x] + "@@@" + this.sizes[x] + "@@@" + this.perms[x];

			} else {
				tmpx[x] = this.files[x] + "@@@" + this.sizes[x];
			}
		}

		LocalIO.sortStrings(tmpx);

		for (int y = 0; y < this.length; y++) {
			this.files[y] = tmpx[y].substring(0, tmpx[y].indexOf("@@@"));

			String tmp = tmpx[y].substring(tmpx[y].indexOf("@@@") + 3);
			this.sizes[y] = tmp.substring(0, tmp.lastIndexOf("@@@"));

			if (null != this.perms) {
				this.perms[y] = Integer.parseInt(tmpx[y].substring(tmpx[y].lastIndexOf("@@@") + 3));
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
	}

	public boolean isOk() {
		return this.isDirectory;
	}

	public int getLength() {
		return this.length;
	}

	public String[] list() {
		return this.files;
	}

	public String[] sList() {
		return this.sizes;
	}

	public int[] getPermissions() {
		return this.perms;
	}

	public Date[] getDates() {
		return this.dates;
	}
}
