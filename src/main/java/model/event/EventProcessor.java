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
package model.event;

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
		addHandler(FTPShutdown, this);
	}

	public static void addHandler(int eventCode, EventHandler h) {
		List<EventHandler> handlers = table.computeIfAbsent(eventCode, k -> new ArrayList<EventHandler>());

		handlers.add(h);
	}

	public void accept(Event e) {
		int code = e.eventCode();
		List<EventHandler> handlers = (table.get(code));

		if (null != handlers) {
			for (EventHandler handler : handlers) {
				handler.handle(e);
			}
		}
	}

	public boolean handle(Event e) {
		this.done = true;

		return true;
	}

	public void run() {
		while (!this.done) {
			if (!this.buffer.isEmpty()) {
				this.accept((Event) this.buffer.firstElement());
				this.buffer.removeElementAt(0);
			}
		}
	}
}
