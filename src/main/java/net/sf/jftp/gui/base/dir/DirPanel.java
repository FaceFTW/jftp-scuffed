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

import net.sf.jftp.gui.base.DownloadList;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.net.BasicConnection;

import javax.swing.*;


public class DirPanel extends HPanel implements Dir {
	public int length;
	public String[] files;
	public DirEntry[] dirEntry;
	public String type;
	public long oldtime;
	public DownloadList dList;
	public BasicConnection con;
	public String path = "./";
	public JList jl = new JList();

	DirPanel() {
		super();
	}

	public DirPanel(String path) {
		super();
		this.path = path;
	}

	public DirPanel(String path, String type) {
		super();
		this.path = path;
		this.type = type;
	}

	public boolean setPath(String path) {
		this.path = path;

		return true;
	}

	public String getPath() {
		return this.path;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDownloadList(DownloadList d) {
		this.dList = d;
	}

	public BasicConnection getCon() {
		return this.con;
	}

	public void setCon(BasicConnection con) {
		this.con = con;
	}

	public void fresh() {
	}

	public void actionPerformed(Object target, String msg) {
	}

	public void lock(boolean isNotification) {
	}

	public void unlock(boolean isNotification) {
	}
}
