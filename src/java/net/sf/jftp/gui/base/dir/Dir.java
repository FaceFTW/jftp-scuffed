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
import net.sf.jftp.net.BasicConnection;


public interface Dir {
	void setDownloadList(DownloadList list);

	BasicConnection getCon();

	void setCon(BasicConnection con);

	void actionPerformed(Object target, String message);

	String getPath();

	boolean setPath(String path);

	String getType();

	void setType(String type);

	void fresh();

	void lock(boolean isNotification);

	void unlock(boolean isNotification);
}
