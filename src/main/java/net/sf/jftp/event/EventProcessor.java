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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class EventProcessor implements Runnable, Acceptor, FtpEventConstants, EventHandler {
	private static final Map<Integer, List<EventHandler>> table = new java.util.HashMap<>();
	private final Vector buffer;
	private boolean done;

	public EventProcessor(Vector b) {
		super();
		this.buffer = b;
		new Thread(this).start();
		addHandler(net.sf.jftp.event.FtpEventConstants.FTPShutdown, this);
	}

	public static void addHandler(int eventCode, EventHandler h) {
		Integer code = eventCode;
		List<EventHandler> handlers = table.get(code);

		if (null == handlers) {
			handlers = new ArrayList<EventHandler>();
			table.put(code, handlers);
		}

		handlers.add(h);
	}

	public void accept(Event e) {
		Integer code = e.eventCode();
		List<EventHandler> handlers = (table.get(code));

		if (null != handlers) {
			for (int i = 0, max = handlers.size(); i < max; i++) {
				((EventHandler) (handlers.get(i))).handle(e);
			}
		}
	}

	public boolean handle(Event e) {
		this.done = true;

		return true;
	}

	public void run() {
		while (!this.done) {
			if (0 != this.buffer.size()) {
				this.accept((Event) this.buffer.firstElement());
				this.buffer.removeElementAt(0);
			}
		}
	}
}
