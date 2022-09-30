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

import java.util.Hashtable;
import java.util.Vector;


public class EventProcessor implements Runnable, Acceptor, FtpEventConstants, EventHandler {
	private static final Hashtable table = new Hashtable();
	private final Vector buffer;
	private boolean done = false;

	public EventProcessor(final Vector b) {
		super();
		this.buffer = b;
		new Thread(this).start();
		net.sf.jftp.event.EventProcessor.addHandler(net.sf.jftp.event.FtpEventConstants.FTPShutdown, this);
	}

	public static void addHandler(final int eventCode, final EventHandler h) {
		final Integer code = eventCode;
		Vector handlers = (Vector) (net.sf.jftp.event.EventProcessor.table.get(code));

		if (null == handlers) {
			handlers = new Vector();
			net.sf.jftp.event.EventProcessor.table.put(code, handlers);
		}

		handlers.addElement(h);
	}

	public void accept(final Event e) {
		final Integer code = e.eventCode();
		final Vector handlers = (Vector) (net.sf.jftp.event.EventProcessor.table.get(code));

		if (null != handlers) {
			for (int i = 0, max = handlers.size(); i < max; i++) {
				((EventHandler) (handlers.elementAt(i))).handle(e);
			}
		}
	}

	public boolean handle(final Event e) {
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
