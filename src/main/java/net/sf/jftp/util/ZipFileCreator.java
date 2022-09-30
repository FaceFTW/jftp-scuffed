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
package net.sf.jftp.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StreamTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipFileCreator {
	private final ZipOutputStream z;

	public ZipFileCreator(String[] files, String path, String name) throws Exception {
		super();
		this.z = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path + name)));
		this.perform(files, path, "");
		this.z.finish();
		this.z.flush();
		this.z.close();
	}

	private void perform(String[] files, String path, String offset) {
		byte[] buf = new byte[4096];

		for (int i = 0; i < files.length; i++) {
			try {
				File f = new File(path + offset + files[i]);
				BufferedInputStream in = null;

				if (f.exists() && !f.isDirectory()) {
					in = new BufferedInputStream(new FileInputStream(path + offset + files[i]));
				} else if (f.exists()) {
					if (!files[i].endsWith("/")) {
						files[i] = files[i] + "/";
					}

					this.perform(f.list(), path, offset + files[i]);
				}

				ZipEntry tmp = new ZipEntry(offset + files[i]);
				this.z.putNextEntry(tmp);

				int len = 0;

				while ((null != in) && (java.io.StreamTokenizer.TT_EOF != len)) {
					len = in.read(buf);

					if (java.io.StreamTokenizer.TT_EOF == len) {
						break;
					}

					this.z.write(buf, 0, len);
				}

				this.z.closeEntry();
			} catch (Exception ex) {
				net.sf.jftp.system.logging.Log.debug("Skipping a file (no permission?)");
			}
		}
	}
}
