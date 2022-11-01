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
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.I18nHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


public class FtpEventHandler implements EventHandler {
	private static List<String> commands = Arrays.asList(new String[]{"account", "append", "ascii", "bell", "bye", "cd", "cdup", "chmod", "close", "cr", // unsupported
			"delete", "dir", "disconnect", "form", // unsupported
			"get", "glob", // unsupported
			"hash", "idle", "image", "lcd", "mdelete", "mdir", "mget", "mkdir", "mls", "mode", "modtime", "mput", "newer", "nlist", "open", "passive", "put", "pwd", "quote", "recv", "reget", "rstatus", "rhelp", "rename", "reset", "restart", "rmdir", "runique", "send", "sendport", "site", "size", "status", "struct", "system", "sunique", "type", "user"});

	private final FtpClient client;
	private final java.util.Map<String, Method> methods = new HashMap<>();

	public FtpEventHandler() {
		super();
		this.client = new FtpClient();

		Method[] m = this.getClass().getDeclaredMethods();
		String methodName = null;

		for (Method method : m) {
			methodName = method.getName();

			if (commands.contains(methodName)) {
				this.methods.put(methodName, method);
			}
		}
	}

	public void open(List<String> args) {
		System.out.println("***open");
		this.client.login(args.get(1));
	}

	private void disconnect(List<String> args) {
		System.out.println("***disconnect");
		this.client.disconnect();
	}

	public void cd(List<String> args) {
		System.out.println("***cd");
		this.client.cd(args.get(1));
	}

	public void pwd(List<String> args) {
		System.out.println("***pwd");

		String directory = this.client.pwd();
	}

	public void get(List<String> args) {
		System.out.println("***get");
		this.client.get(args.get(1));
	}

	public void put(List<String> args) {
		System.out.println("***put");
		this.client.put(args.get(1));
	}

	public void quit(List<String> args) {
		this.disconnect(args);
	}

	public boolean handle(Event e) {
		System.out.println(e.eventCode());
		System.out.println(((FtpEvent) e).eventMsg());

		StringTokenizer st = new StringTokenizer(((FtpEvent) e).eventMsg());
		List<String> list = new ArrayList<>();

		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}

		if (!list.isEmpty()) {
			String command = list.get(0);
			Method o = this.methods.get(command.toLowerCase());

			if (null != o) {
				try {
					o.invoke(this, list);
				} catch (IllegalAccessException e1) {
					Log.debug(I18nHelper.getLogString("ftpeventhandler.illegalaccessexception.in.invoke.in.handle"));
				} catch (IllegalArgumentException e1) {
					Log.debug(I18nHelper.getLogString("ftpeventhandler.illegalargumentexception.in.invoke.in.handle"));
				} catch (InvocationTargetException e1) {
					Log.debug(I18nHelper.getLogString("ftpeventhandler.invocationtargetexception.in.invoke.in.handle"));
				} catch (RuntimeException ex) {
					Log.debug(I18nHelper.getLogString("ftpeventhandler.runtimeexception.in.invoke.in.handle"));
				}
			}
		}

		return true;
	}
}
