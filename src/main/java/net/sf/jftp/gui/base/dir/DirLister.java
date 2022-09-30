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

	public DirLister(final BasicConnection con)
	{
		this.con = con;
		this.init();
	}

	public DirLister(final BasicConnection con, final String sortMode)
	{
		this.con = con;
		this.sortMode = sortMode;
		this.init();
	}

	public DirLister(final BasicConnection con, final String sortMode, final boolean hide)
	{
		this.con = con;
		this.sortMode = sortMode;
		this.init();

		int cnt = files.length;

		if (hide) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].startsWith(".") && !files[i].startsWith("..")) {
					files[i] = null;
					cnt--;
				}
			}

			final String[] newFiles = new String[cnt];
			final String[] newSizes = new String[cnt];
			final int[] newPerms = new int[cnt];

			int idx = 0;
			for (int i = 0; i < files.length; i++) {
				if (files[i] == null) {
					continue;
				} else {
					newFiles[idx] = files[i];
					newSizes[idx] = sizes[i];
					newPerms[idx] = perms[i];
					idx++;
				}
			}

			files = newFiles;
			sizes = newSizes;
			perms = newPerms;
			length = files.length;
		}
	}

	public void init() {
		try {
			final String outfile = Settings.ls_out;

			con.list();
			files = con.sortLs();
			sizes = con.sortSize();

			length = files.length;
			perms = con.getPermissions();
			isDirectory = true;

			if (sortMode != null) {
				if (!sortMode.equals("Date")) {
					this.sortFirst();
				}

				this.sort(sortMode);
			} else if (sortMode == null) {
				this.sortFirst();
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			isDirectory = false;
		}

		finished = true;
	}

	private void sort(final String type) {
		final Vector fv = new Vector();
		final Vector sv = new Vector();
		final Vector pv = new Vector();

		if (type.equals("Reverse")) {
			for (int i = 0; i < length; i++) {
				fv.add(files[i]);
				sv.add(sizes[i]);

				if (perms != null) {
					pv.add(perms[i]);
				}
			}

			fv.sort(java.util.Collections.reverseOrder());

			final Object[] filesTmp = fv.toArray();
			final Object[] sizesTmp = sv.toArray();
			Object[] permsTmp = null;

			if (perms != null) {
				permsTmp = pv.toArray();
			}

			for (int i = 0; i < length; i++) {
				files[i] = (String) filesTmp[i];
				sizes[i] = (String) sizesTmp[length - i - 1];

				if (perms != null) {
					perms[i] = (Integer) permsTmp[length - i - 1];
				}
			}
		} else if (type.startsWith("Size")) {
			int cnt = 0;
			final Hashtable processed = new Hashtable();
			final boolean reverse = type.endsWith("/Re");

			while (cnt < length) {
				int idx = 0;
				double current = 0;

				if (reverse) {
					current = Double.MAX_VALUE;
				}

				for (int i = 0; i < length; i++) {
					if (processed.containsKey("" + i)) {
						continue;
					}

					final int si = Integer.parseInt(sizes[i]);

					if (!reverse && (si >= current)) {
						idx = i;
						current = si;
					} else if (reverse && (si <= current)) {
						idx = i;
						current = si;
					}
				}

				processed.put("" + idx, "" + sizes[idx]);
				fv.add(files[idx]);
				sv.add(sizes[idx]);

				if (perms != null) {
					pv.add(perms[idx]);
				}

				cnt++;
			}

			for (int i = 0; i < length; i++) {
				files[i] = (String) fv.elementAt(i);
				sizes[i] = (String) sv.elementAt(i);

				if (perms != null) {
					perms[i] = (Integer) pv.elementAt(i);
				}
			}
		} else if (type.equals("Date")) {
			String style = "ftp";

			//TODO: may be slow
			if (!(con instanceof FtpConnection) || (((FtpConnection) con).dateVector == null) || (((FtpConnection) con).dateVector.size() < 1)) {
				if (!(con instanceof FilesystemConnection) || (((FilesystemConnection) con).dateVector == null) || (((FilesystemConnection) con).dateVector.size() < 1)) {
				} else {
					style = "file";
				}
			}

			Object[] date = null;

			if (style.equals("ftp")) {
				date = ((FtpConnection) con).dateVector.toArray();

			} else {
				date = ((FilesystemConnection) con).dateVector.toArray();
			}

			for (int j = 0; j < date.length; j++) {
				for (int i = 0; i < date.length; i++) {
					final Date x = (Date) date[i];

					if (i == (date.length - 1)) {
						break;
					}

					if (this.comp(x, (Date) date[i + 1])) {
						final Date swp = (Date) date[i + 1];
						date[i + 1] = x;
						date[i] = swp;

						String s1 = files[i + 1];
						String s2 = files[i];
						files[i] = s1;
						files[i + 1] = s2;

						s1 = sizes[i + 1];
						s2 = sizes[i];
						sizes[i] = s1;
						sizes[i + 1] = s2;

						final int s3 = perms[i + 1];
						final int s4 = perms[i];
						perms[i] = s3;
						perms[i + 1] = s4;
					}
				}
			}

			dates = new Date[date.length];

			for (int i = 0; i < dates.length; i++) {
				dates[i] = (Date) date[i];
			}

		} else if (type.equals("Normal")) {
			// already done.
		}
	}

	private boolean comp(final Date one, final Date two) {
		final Calendar c = new GregorianCalendar();
		c.clear();
		c.setTime(one);

		final Calendar c2 = new GregorianCalendar();
		c2.clear();
		c2.setTime(two);

		return c.getTime().compareTo(c2.getTime()) > 0;
	}

	public void sortFirst() {
		final String[] tmpx = new String[length];

		for (int x = 0; x < length; x++) {
			if (perms != null) {
				tmpx[x] = files[x] + "@@@" + sizes[x] + "@@@" + perms[x];

			} else {
				tmpx[x] = files[x] + "@@@" + sizes[x];
			}
		}

		LocalIO.sortStrings(tmpx);

		for (int y = 0; y < length; y++) {
			files[y] = tmpx[y].substring(0, tmpx[y].indexOf("@@@"));

			final String tmp = tmpx[y].substring(tmpx[y].indexOf("@@@") + 3);
			sizes[y] = tmp.substring(0, tmp.lastIndexOf("@@@"));

			if (perms != null) {
				perms[y] = Integer.parseInt(tmpx[y].substring(tmpx[y].lastIndexOf("@@@") + 3));
			}
		}
	}

	public void actionPerformed(final ActionEvent e) {
	}

	public boolean isOk() {
		return isDirectory;
	}

	public int getLength() {
		return length;
	}

	public String[] list() {
		return files;
	}

	public String[] sList() {
		return sizes;
	}

	public int[] getPermissions() {
		return perms;
	}

	public Date[] getDates() {
		return dates;
	}
}
