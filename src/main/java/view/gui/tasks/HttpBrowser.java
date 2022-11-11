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
package view.gui.tasks;

import view.JFtp;
import model.system.logging.Log;
import controller.util.I18nHelper;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.Vector;


public class HttpBrowser extends JInternalFrame implements HyperlinkListener {
	public HttpBrowser(String url) {
		super(I18nHelper.getUIString("http.browser"), true, true, true, true);

		try {
			this.setTitle(url);

			JEditorPane pane = new JEditorPane(url);
			pane.setEditable(false);
			pane.addHyperlinkListener(this);

			if (!pane.getEditorKit().getContentType().equals("text/html") && !pane.getEditorKit().getContentType().equals("text/rtf")) {
				if (!pane.getEditorKit().getContentType().equals("text/plain")) {
					Log.debug(I18nHelper.getLogString("could.not.display.url"));

					return;
				}

				pane.setEditable(false);
				pane.addHyperlinkListener(this);
			}

			JScrollPane jsp = new JScrollPane(pane);

			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(JFtp.CENTER, jsp);

			this.setLocation(50, 50);
			this.setSize(800, 500);
			this.show();
			this.requestFocus();
		} catch (Exception ex) {
			Log.debug(MessageFormat.format(I18nHelper.getLogString("error.fetching.url.0"), ex));
			ex.printStackTrace();
		}
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) e.getSource();

			if (e instanceof HTMLFrameHyperlinkEvent) {
				HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
				HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			} else {
				try {
					String url = e.getURL().toString();
					String tmp = url.substring(url.lastIndexOf('/'));

					Vector listeners = new Vector();
					listeners.add(JFtp.localDir);

					if (!url.endsWith(".htm") && !url.endsWith(".html") && (tmp.contains("."))) {
						JFtp.statusP.startTransfer(url, JFtp.localDir.getPath(), listeners, JFtp.getConnectionHandler());
					} else {
						pane.setPage(e.getURL());
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
}
