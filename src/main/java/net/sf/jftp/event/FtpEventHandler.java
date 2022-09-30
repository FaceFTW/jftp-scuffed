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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.event;

import net.sf.jftp.net.FtpClient;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;


public class FtpEventHandler implements EventHandler {
	private static ArrayList commands = null;

	static {
		commands = new ArrayList();
		commands.add("account");
		commands.add("append");
		commands.add("ascii");
		commands.add("bell");
		commands.add("bye");
		commands.add("cd");
		commands.add("cdup");
		commands.add("chmod");
		commands.add("close");
		commands.add("cr"); // unsupported
		commands.add("delete");
		commands.add("dir");
		commands.add("disconnect");
		commands.add("form"); // unsupported
		commands.add("get");
		commands.add("glob"); // unsupported
		commands.add("hash");
		commands.add("idle");
		commands.add("image");
		commands.add("lcd");
		commands.add("mdelete");
		commands.add("mdir");
		commands.add("mget");
		commands.add("mkdir");
		commands.add("mls");
		commands.add("mode");
		commands.add("modtime");
		commands.add("mput");
		commands.add("newer");
		commands.add("nlist");
		commands.add("open");
		commands.add("passive");
		commands.add("put");
		commands.add("pwd");
		commands.add("quote");
		commands.add("recv");
		commands.add("reget");
		commands.add("rstatus");
		commands.add("rhelp");
		commands.add("rename");
		commands.add("reset");
		commands.add("restart");
		commands.add("rmdir");
		commands.add("runique");
		commands.add("send");
		commands.add("sendport");
		commands.add("site");
		commands.add("size");
		commands.add("status");
		commands.add("struct");
		commands.add("system");
		commands.add("sunique");
		commands.add("type");
		commands.add("user");
	}

	private final FtpClient client;
	private final Hashtable methods = new Hashtable();

	public FtpEventHandler() {
		super();
		this.client = new FtpClient();

		Method[] m = this.getClass().getDeclaredMethods();
		String methodName = null;

		for (java.lang.reflect.Method method : m) {
			methodName = method.getName();

			if (commands.contains(methodName)) {
				this.methods.put(methodName, method);
			}
		}
	}

	public void open(Vector args) {
		System.out.println("***open");
		this.client.login((String) args.elementAt(1));
	}

	public void disconnect(Vector args) {
		System.out.println("***disconnect");
		this.client.disconnect();
	}

	public void cd(Vector args) {
		System.out.println("***cd");
		this.client.cd((String) args.elementAt(1));
	}

	public void pwd(Vector args) {
		System.out.println("***pwd");

		String directory = this.client.pwd();
	}

	public void get(Vector args) {
		System.out.println("***get");
		this.client.get((String) args.elementAt(1));
	}

	public void put(Vector args) {
		System.out.println("***put");
		this.client.put((String) args.elementAt(1));
	}

	public void quit(Vector args) {
		this.disconnect(args);
	}

	public boolean handle(Event e) {
		System.out.println(e.eventCode());
		System.out.println(((FtpEvent) e).eventMsg());

		StringTokenizer st = new StringTokenizer(((FtpEvent) e).eventMsg());
		Vector list = new Vector();

		while (st.hasMoreTokens()) {
			list.addElement(st.nextToken());
		}

		if (0 != list.size()) {
			String command = (String) list.elementAt(0);
			Method o = (Method) this.methods.get(command.toLowerCase());

			if (null != o) {
				try {
					o.invoke(this, list);
				} catch (Exception ex) {
				}
			}
		}

		return true;
	}
}
