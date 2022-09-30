package net.sf.jftp.gui.framework;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProgressBarList extends JPanel {

	private int index = -1;

	public ProgressBarList() {
		this.setLayout(new GridLayout(0, 1));

		this.addMouseListener(new MouseAdapter() {
			public void mouseReleased(final MouseEvent e) {
				final Component c = net.sf.jftp.gui.framework.ProgressBarList.this.getComponentAt(e.getX(), e.getY());

				net.sf.jftp.gui.framework.ProgressBarList.this.deselectAll();

				for (int i = 0; i < net.sf.jftp.gui.framework.ProgressBarList.this.getComponentCount(); i++) {
					if (net.sf.jftp.gui.framework.ProgressBarList.this.getComponent(i) instanceof ProgressbarItem) {
						final ProgressbarItem item = (ProgressbarItem) net.sf.jftp.gui.framework.ProgressBarList.this.getComponent(i);

						if (item == c) {
							item.select();
							net.sf.jftp.gui.framework.ProgressBarList.this.index = i;
						}
					}
				}

				if (c instanceof ProgressbarItem) {

				}
			}
		});
	}

	public void setListData(final net.sf.jftp.gui.base.dir.DirEntry[] items) {

		this.removeAll();
		//System.out.println("\n\n--------------------\n");

		for (final net.sf.jftp.gui.base.dir.DirEntry item : items) {
			final net.sf.jftp.gui.framework.ProgressbarItem p = new net.sf.jftp.gui.framework.ProgressbarItem(item);
			p.update((int) item.getTransferred() / 1024, (int) item.getRawSize() / 1024, item.file);

			//System.out.println("add: "+items[i].file+" -> "+items[i].getTransferred()+"/"+items[i].getRawSize());

			this.add(p);
		}

		while (this.getComponentCount() < 10) {
			this.add(new JLabel(" "));
		}

		this.revalidate();
		this.setSelectedIndex(this.index);
	}

	public ProgressbarItem getSelectedValue() {
		return (ProgressbarItem) this.getComponent(this.index);
	}

	public int getSelectedIndex() {
		return this.index;
	}

	public void setSelectedIndex(final int idx) {

		this.deselectAll();

		this.index = idx;
		if (this.index >= 0 && this.getComponentCount() > this.index && this.getComponent(this.index) instanceof ProgressbarItem) {
			((ProgressbarItem) this.getComponent(this.index)).select();
		}
	}

	private void deselectAll() {
		for (int i = 0; i < this.getComponentCount(); i++) {
			if (this.getComponent(i) instanceof ProgressbarItem) {
				((ProgressbarItem) this.getComponent(i)).deselect();
			}
		}
	}

	private String strip(String in) {
		String tmp;
		if (in.contains("<")) {
			in = in.substring(in.lastIndexOf("<") + 1);
			in = in.substring(0, in.lastIndexOf(">"));
		}

		return in;
	}

	public void setTransferred(final String file, final long bytes, final String message, final long max) {
		boolean ok = false;

		//System.out.println(file+":"+bytes+":"+max);

		for (int i = 0; i < this.getComponentCount() && !ok; i++) {
			if (this.getComponent(i) instanceof ProgressbarItem) {
				final ProgressbarItem item = ((ProgressbarItem) this.getComponent(i));
				final String f = this.strip(item.getDirEntry().file);

				if (f.equals(file)) {
					item.update(bytes, max, message);
					ok = true;
				}
			} else {
				ok = false;
			}
		}
	}

}
