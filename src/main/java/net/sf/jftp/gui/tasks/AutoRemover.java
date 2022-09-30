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

import java.io.File;


public class AutoRemover {
	public AutoRemover(final String file, final String type) {
		if (type.equals("local")) {
			final File f = new File(net.sf.jftp.JFtp.localDir.getPath() + file);

			if (f.isDirectory()) {
				net.sf.jftp.system.LocalIO.cleanLocalDir(file, net.sf.jftp.JFtp.localDir.getPath());
				f.delete();
			} else {
				f.delete();
			}
		}

		if (type.equals("remote")) {
			net.sf.jftp.JFtp.remoteDir.lock(false);

			net.sf.jftp.JFtp.remoteDir.getCon().removeFileOrDir(file);

			net.sf.jftp.JFtp.remoteDir.unlock(false);
		}
	}
}
