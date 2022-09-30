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
package net.sf.jftp.gui.base;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.ProgressBarList;
import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.Transfer;
import net.sf.jftp.net.wrappers.HttpTransfer;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.UpdateDaemon;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;


public class DownloadList extends HPanel implements ActionListener {
	private final ProgressBarList list = new ProgressBarList();
	private final JScrollPane scroll;
	public final Hashtable sizeCache = new Hashtable();
	private Hashtable downloads = new Hashtable();
	private long oldtime = 0;

	public DownloadList() {
		this.setLayout(new BorderLayout());

		final net.sf.jftp.gui.framework.HImageButton resume = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.resumeImage, "resume", "Resume selected transfer...", this);
		resume.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.resumeImage2)));
		resume.setRolloverEnabled(true);
		final net.sf.jftp.gui.framework.HImageButton pause = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.pauseImage, "pause", "Pause selected transfer...", this);
		pause.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.pauseImage2)));
		pause.setRolloverEnabled(true);
		final net.sf.jftp.gui.framework.HImageButton clear = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.clearImage, "clear", "Remove old/stalled items from output...", this);
		clear.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.clearImage2)));
		clear.setRolloverEnabled(true);
		final net.sf.jftp.gui.framework.HImageButton cancel = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.deleteImage, "delete", "Cancel selected transfer...", this);
		cancel.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.deleteImage2)));
		cancel.setRolloverEnabled(true);

		final HPanel cmdP = new HPanel();

		cmdP.add(clear);

		cmdP.add(new JLabel("   "));

		cmdP.add(resume);
		cmdP.add(pause);

		cmdP.add(new JLabel("   "));

		cmdP.add(cancel);

		clear.setSize(24, 24);
		cancel.setSize(24, 24);
		resume.setSize(24, 24);
		pause.setSize(24, 24);

		clear.setToolTipText("Remove old/stalled items from output...");

		resume.setToolTipText("Resume selected transfer...");
		pause.setToolTipText("Pause selected transfer...");
		cancel.setToolTipText("Cancel selected transfer...");

		this.scroll = new JScrollPane(this.list);
		this.add("South", cmdP);
		this.add("Center", this.scroll);
	}

	public void fresh() {
		this.downloads = new Hashtable();
		this.updateArea();
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("delete")) {
			this.deleteCon();
		} else if (e.getActionCommand().equals("clear") || (e.getSource() == AppMenuBar.clearItems)) {
			this.fresh();
		} else if (e.getActionCommand().equals("pause")) {
			this.pauseCon();
		} else if (e.getActionCommand().equals("resume")) {
			this.resumeCon();
		}
	}

	private void deleteCon() {
		try {
			String cmd = this.getActiveItem();

			if (cmd == null) {
				return;
			}

			if ((cmd.contains(net.sf.jftp.net.Transfer.QUEUED)) || (cmd.contains(net.sf.jftp.net.Transfer.PAUSED))) {
				cmd = this.getFile(cmd);

				try {
					final Transfer d = JFtp.getConnectionHandler().getConnections().get(cmd);

					if (d == null) {
						return;
					}

					d.work = false;
					d.pause = false;

				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			} else {
				cmd = this.getFile(cmd);

				final ConnectionHandler h = JFtp.getConnectionHandler();

				final Object o = h.getConnections().get(cmd);

				Log.out("aborting  transfer: " + cmd);
				Log.out("connection handler present: " + h + ", pool size: " + h.getConnections().size());

				if (o instanceof HttpTransfer) {
					final Transfer d = (Transfer) o;
					d.work = false;
					this.updateList(cmd, DataConnection.FAILED, -1, -1);

					return;
				} else {
					final Transfer d = (Transfer) o;

					final DataConnection con = d.getDataConnection();
					con.getCon().work = false;

					try {
						con.sock.close();

					} catch (final Exception ex) {
						ex.printStackTrace();
					}
				}

				LocalIO.pause(500);
				this.updateList(this.getRawFile(this.getActiveItem()), DataConnection.FAILED, -1, -1);
			}
		} catch (final Exception ex) {
			Log.debug("Action is not supported for this connection.");
			ex.printStackTrace();
		}
	}

	private void pauseCon() {
		try {
			String cmd = this.getActiveItem();

			if (cmd == null) {
				return;
			}

			if ((cmd.contains(net.sf.jftp.net.DataConnection.GET)) || (cmd.contains(net.sf.jftp.net.DataConnection.PUT))) {
				cmd = this.getFile(cmd);

				final Object o = JFtp.getConnectionHandler().getConnections().get(cmd);

				if (o == null) {
					return;
				}

				final Transfer d = (Transfer) o;
				d.pause = true;

				final DataConnection con = d.getDataConnection();

				try {
					con.sock.close();
				} catch (final Exception ex) {
					ex.printStackTrace();
				}

				d.prepare();
			}
		} catch (final Exception ex) {
			Log.debug("Action is not supported for this connection.");
		}
	}

	private void resumeCon() {
		try {
			String cmd = this.getActiveItem();

			if (cmd == null) {
				return;
			}

			if ((cmd.contains(net.sf.jftp.net.Transfer.PAUSED)) || (cmd.contains(net.sf.jftp.net.Transfer.QUEUED))) {
				cmd = this.getFile(cmd);

				try {
					final Object o = JFtp.getConnectionHandler().getConnections().get(cmd);

					if (o == null) {
						return;
					}

					final Transfer d = (Transfer) o;
					d.work = true;
					d.pause = false;
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (final Exception ex) {
			Log.debug("Action is not supported for this connection.");
		}
	}

	private String getActiveItem() {
		final String tmp = this.list.getSelectedValue().getDirEntry().toString();

		if (tmp == null) {
			return "";
		} else {
			return tmp;
		}
	}

	public synchronized void updateList(final String file, String type, final long bytes, long size) {
		String message = type + ": <" + file + "> ";

		if (!this.safeUpdate()) {
			if (!type.startsWith(DataConnection.DFINISHED) && !type.startsWith(DataConnection.FINISHED) && !type.startsWith(DataConnection.FAILED) && !type.startsWith(Transfer.PAUSED) && !type.startsWith(Transfer.REMOVED)) {
				return;
			}
		}

		// directory
		int count = 0;

		if (type.startsWith(DataConnection.GETDIR) || type.startsWith(DataConnection.PUTDIR) || type.startsWith(DataConnection.DFINISHED)) {
			//System.out.println(type);
			final String tmp = type.substring(type.indexOf(":") + 1);
			type = type.substring(0, type.indexOf(":"));
			count = Integer.parseInt(tmp);
			message = type + ": <" + file + "> ";
		}

		if (type.equals(DataConnection.GET)) {
			final net.sf.jftp.gui.base.dir.DirEntry[] e = ((net.sf.jftp.gui.base.dir.DirPanel) JFtp.remoteDir).dirEntry;

			for (final net.sf.jftp.gui.base.dir.DirEntry dirEntry : e) {
				if (dirEntry.file.equals(file)) {
					size = dirEntry.getRawSize();
				}
			}
		}

		final String tmp;
		final long s = size / 1024;

		if (s > 0) {
			tmp = Long.toString(s);
		} else {
			tmp = "?";
		}

		if (type.equals(DataConnection.GET) || type.equals(DataConnection.PUT)) {
			message = message + (bytes / 1024) + " / " + tmp + " kb";
			this.list.setTransferred(file, (bytes / 1024), message, s);
		} else if (type.equals(DataConnection.GETDIR) || type.equals(DataConnection.PUTDIR)) {
			message = message + (bytes / 1024) + " kb of file #" + count;
			this.list.setTransferred(file, (bytes / 1024), message, -1);
		} else if (type.startsWith(DataConnection.DFINISHED)) {
			message = message + " " + count + " files.";
		}

		if (type.equals(DataConnection.FINISHED) || type.startsWith(DataConnection.DFINISHED)) {
			try {
				JFtp.getConnectionHandler().removeConnection(file);
			} catch (final Exception ex) {
			}

			UpdateDaemon.updateCall();
		} else if (type.equals(DataConnection.FAILED)) {
			UpdateDaemon.updateCall();
		}

		net.sf.jftp.gui.base.dir.DirEntry d = null;
		if (this.downloads.containsKey(message)) {
			d = (net.sf.jftp.gui.base.dir.DirEntry) this.downloads.get(message);
		} else {
			d = new net.sf.jftp.gui.base.dir.DirEntry(message, null);
			d.setNoRender();
			if (this.getFile(tmp).endsWith("/")) {
				d.setDirectory();
			}
			d.setFileSize(size);
		}

		d.setTransferred(bytes);

		this.downloads.put(file, d);

		this.updateArea();
	}

	private synchronized net.sf.jftp.gui.base.dir.DirEntry[] toArray() {
		final net.sf.jftp.gui.base.dir.DirEntry[] f = new net.sf.jftp.gui.base.dir.DirEntry[this.downloads.size()];
		int i = 0;

		final Enumeration k = this.downloads.elements();

		while (k.hasMoreElements()) {
			final Object o = k.nextElement();
			if (o instanceof net.sf.jftp.gui.base.dir.DirEntry) {
				f[i] = (net.sf.jftp.gui.base.dir.DirEntry) o;
			} else {

				final String tmp = (String) o;
				final net.sf.jftp.gui.base.dir.DirEntry d = new net.sf.jftp.gui.base.dir.DirEntry(tmp, null);

				if (this.getFile(tmp).endsWith("/")) {
					d.setDirectory();
				}

				d.setNoRender();
				f[i] = d;
			}

			i++;
		}

		return f;
	}

	private synchronized void updateArea() {

		final int idx = this.list.getSelectedIndex();

		final net.sf.jftp.gui.base.dir.DirEntry[] f = this.toArray();

		this.list.setListData(f);

		if ((f.length == 1) && (idx < 0)) {
			this.list.setSelectedIndex(0);
		} else {
			this.list.setSelectedIndex(idx);
		}

		this.revalidate();
		this.scroll.revalidate();
		this.repaint();
	}

	private String getFile(final String msg) {
		String f = msg;

		if (msg.contains("<") && msg.contains(">")) {
			f = msg.substring(msg.indexOf("<") + 1);
			f = f.substring(0, f.lastIndexOf(">"));
		}

		return this.getRealName(f);
	}

	private String getRealName(final String file) {
		try {
			final Enumeration e = JFtp.getConnectionHandler().getConnections().keys();

			while (e.hasMoreElements()) {
				final String tmp = (String) e.nextElement();

				if (tmp.endsWith(file)) {
					return tmp;
				}
			}
		} catch (final Exception ex) {
		}

		return file;
	}

	private String getRawFile(final String msg) {
		String f = msg.substring(msg.indexOf("<") + 1);
		f = f.substring(0, f.lastIndexOf(">"));

		return f;
	}

	private boolean safeUpdate() {
		final long time = System.currentTimeMillis();

		if ((time - this.oldtime) < Settings.refreshDelay) {
			return false;
		}

		this.oldtime = time;

		return true;
	}
}
