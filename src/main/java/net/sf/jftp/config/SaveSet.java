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
package net.sf.jftp.config;

import java.io.FileOutputStream;
import java.io.PrintStream;


public class SaveSet {
	private PrintStream out = null;

	public SaveSet(String file, String host, String user, String pass, String name, String port) {
		super();
		try {
			FileOutputStream fos;
			this.out = new PrintStream((fos = new FileOutputStream(file)));
			this.out.println(host);
			this.out.println(user);

			if (Settings.getStorePasswords()) {
				this.savePW(pass, this.out);
			} else {
				this.out.println();
			}

			this.out.println(name);
			this.out.println(port);
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public SaveSet(String file, String host, String user, String pass, String port, String cwd, String lcwd) {
		super();
		try {
			this.out = new PrintStream(new FileOutputStream(file));
			this.out.println(host);
			this.out.println(user);

			if (Settings.getStorePasswords()) {
				this.savePW(pass, this.out);
			} else {
				this.out.println();
			}

			this.out.println(port);
			this.out.println(cwd);
			this.out.println(lcwd);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//***
	//***this is for saving advanced option data
	//***ideally, we'd have it set up so that common code in
	//***all of these constructors is put in a private method
	//*** file: the file name
	//*** lsCMD: the FTP LIST command to be saved
	public SaveSet(String file, String lsCmd) {
		super();
		try {
			this.out = new PrintStream(new FileOutputStream(file));
			this.out.println(lsCmd);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void savePW(String pass, PrintStream out) throws Exception {
		String coded = Crypto.Encrypt(pass);
		if ("".equals(coded)) {
			// we failed to encrypt for some reason, so lets just save it
			out.println(pass);
		} else {
			out.println(coded);
		}
	}
}
