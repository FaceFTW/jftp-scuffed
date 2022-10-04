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
	private final java.util.Map<String, Method> methods = new java.util.HashMap<>();

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

	public void open(java.util.List<String> args) {
		System.out.println("***open");
		this.client.login(args.get(1));
	}

	public void disconnect(java.util.List<String> args) {
		System.out.println("***disconnect");
		this.client.disconnect();
	}

	public void cd(java.util.List<String> args) {
		System.out.println("***cd");
		this.client.cd(args.get(1));
	}

	public void pwd(java.util.List<String> args) {
		System.out.println("***pwd");

		String directory = this.client.pwd();
	}

	public void get(java.util.List<String> args) {
		System.out.println("***get");
		this.client.get(args.get(1));
	}

	public void put(java.util.List<String> args) {
		System.out.println("***put");
		this.client.put(args.get(1));
	}

	public void quit(java.util.List<String> args) {
		this.disconnect(args);
	}

	public boolean handle(Event e) {
		System.out.println(e.eventCode());
		System.out.println(((FtpEvent) e).eventMsg());

		StringTokenizer st = new StringTokenizer(((FtpEvent) e).eventMsg());
		java.util.List<String> list = new ArrayList<>();

		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}

		if (0 != list.size()) {
			String command = (String) list.get(0);
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
