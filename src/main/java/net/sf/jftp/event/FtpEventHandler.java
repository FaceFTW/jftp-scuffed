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
		net.sf.jftp.event.FtpEventHandler.commands = new ArrayList();
		net.sf.jftp.event.FtpEventHandler.commands.add("account");
		net.sf.jftp.event.FtpEventHandler.commands.add("append");
		net.sf.jftp.event.FtpEventHandler.commands.add("ascii");
		net.sf.jftp.event.FtpEventHandler.commands.add("bell");
		net.sf.jftp.event.FtpEventHandler.commands.add("bye");
		net.sf.jftp.event.FtpEventHandler.commands.add("cd");
		net.sf.jftp.event.FtpEventHandler.commands.add("cdup");
		net.sf.jftp.event.FtpEventHandler.commands.add("chmod");
		net.sf.jftp.event.FtpEventHandler.commands.add("close");
		net.sf.jftp.event.FtpEventHandler.commands.add("cr"); // unsupported
		net.sf.jftp.event.FtpEventHandler.commands.add("delete");
		net.sf.jftp.event.FtpEventHandler.commands.add("dir");
		net.sf.jftp.event.FtpEventHandler.commands.add("disconnect");
		net.sf.jftp.event.FtpEventHandler.commands.add("form"); // unsupported
		net.sf.jftp.event.FtpEventHandler.commands.add("get");
		net.sf.jftp.event.FtpEventHandler.commands.add("glob"); // unsupported
		net.sf.jftp.event.FtpEventHandler.commands.add("hash");
		net.sf.jftp.event.FtpEventHandler.commands.add("idle");
		net.sf.jftp.event.FtpEventHandler.commands.add("image");
		net.sf.jftp.event.FtpEventHandler.commands.add("lcd");
		net.sf.jftp.event.FtpEventHandler.commands.add("mdelete");
		net.sf.jftp.event.FtpEventHandler.commands.add("mdir");
		net.sf.jftp.event.FtpEventHandler.commands.add("mget");
		net.sf.jftp.event.FtpEventHandler.commands.add("mkdir");
		net.sf.jftp.event.FtpEventHandler.commands.add("mls");
		net.sf.jftp.event.FtpEventHandler.commands.add("mode");
		net.sf.jftp.event.FtpEventHandler.commands.add("modtime");
		net.sf.jftp.event.FtpEventHandler.commands.add("mput");
		net.sf.jftp.event.FtpEventHandler.commands.add("newer");
		net.sf.jftp.event.FtpEventHandler.commands.add("nlist");
		net.sf.jftp.event.FtpEventHandler.commands.add("open");
		net.sf.jftp.event.FtpEventHandler.commands.add("passive");
		net.sf.jftp.event.FtpEventHandler.commands.add("put");
		net.sf.jftp.event.FtpEventHandler.commands.add("pwd");
		net.sf.jftp.event.FtpEventHandler.commands.add("quote");
		net.sf.jftp.event.FtpEventHandler.commands.add("recv");
		net.sf.jftp.event.FtpEventHandler.commands.add("reget");
		net.sf.jftp.event.FtpEventHandler.commands.add("rstatus");
		net.sf.jftp.event.FtpEventHandler.commands.add("rhelp");
		net.sf.jftp.event.FtpEventHandler.commands.add("rename");
		net.sf.jftp.event.FtpEventHandler.commands.add("reset");
		net.sf.jftp.event.FtpEventHandler.commands.add("restart");
		net.sf.jftp.event.FtpEventHandler.commands.add("rmdir");
		net.sf.jftp.event.FtpEventHandler.commands.add("runique");
		net.sf.jftp.event.FtpEventHandler.commands.add("send");
		net.sf.jftp.event.FtpEventHandler.commands.add("sendport");
		net.sf.jftp.event.FtpEventHandler.commands.add("site");
		net.sf.jftp.event.FtpEventHandler.commands.add("size");
		net.sf.jftp.event.FtpEventHandler.commands.add("status");
		net.sf.jftp.event.FtpEventHandler.commands.add("struct");
		net.sf.jftp.event.FtpEventHandler.commands.add("system");
		net.sf.jftp.event.FtpEventHandler.commands.add("sunique");
		net.sf.jftp.event.FtpEventHandler.commands.add("type");
		net.sf.jftp.event.FtpEventHandler.commands.add("user");
	}

	private final FtpClient client;
	private final Hashtable methods = new Hashtable();

	public FtpEventHandler() {
		this.client = new FtpClient();

		final Method[] m = this.getClass().getDeclaredMethods();
		String methodName = null;

		for (final java.lang.reflect.Method method : m) {
			methodName = method.getName();

			if (net.sf.jftp.event.FtpEventHandler.commands.contains(methodName)) {
				this.methods.put(methodName, method);
			}
		}
	}

	public void open(final Vector args) {
		System.out.println("***open");
		this.client.login((String) args.elementAt(1));
	}

	public void disconnect(final Vector args) {
		System.out.println("***disconnect");
		this.client.disconnect();
	}

	public void cd(final Vector args) {
		System.out.println("***cd");
		this.client.cd((String) args.elementAt(1));
	}

	public void pwd(final Vector args) {
		System.out.println("***pwd");

		final String directory = this.client.pwd();
	}

	public void get(final Vector args) {
		System.out.println("***get");
		this.client.get((String) args.elementAt(1));
	}

	public void put(final Vector args) {
		System.out.println("***put");
		this.client.put((String) args.elementAt(1));
	}

	public void quit(final Vector args) {
		this.disconnect(args);
	}

	public boolean handle(final Event e) {
		System.out.println(e.eventCode());
		System.out.println(((FtpEvent) e).eventMsg());

		final StringTokenizer st = new StringTokenizer(((FtpEvent) e).eventMsg());
		final Vector list = new Vector();

		while (st.hasMoreTokens()) {
			list.addElement(st.nextToken());
		}

		if (list.size() != 0) {
			final String command = (String) list.elementAt(0);
			final Method o = (Method) this.methods.get(command.toLowerCase());

			if (o != null) {
				try {
					o.invoke(this, list);
				} catch (final Exception ex) {
				}
			}
		}

		return true;
	}
}
