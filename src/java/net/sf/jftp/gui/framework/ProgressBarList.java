package net.sf.jftp.gui.framework;

import net.sf.jftp.gui.base.dir.DirEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProgressBarList extends JPanel {

	private int index = -1;

	public ProgressBarList() {
		setLayout(new GridLayout(0, 1));

		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Component c = getComponentAt(e.getX(), e.getY());

				deselectAll();

				for (int i = 0; i < getComponentCount(); i++) {
					if (getComponent(i) instanceof ProgressbarItem) {
						ProgressbarItem item = (ProgressbarItem) getComponent(i);

						if (item == c) {
							item.select();
							index = i;
						}
					}
				}

				if (c instanceof ProgressbarItem) {

				}
			}
		});
	}

	public void setListData(DirEntry[] items) {

		removeAll();
		//System.out.println("\n\n--------------------\n");

		for (int i = 0; i < items.length; i++) {
			ProgressbarItem p = new ProgressbarItem(items[i]);
			p.update((int) items[i].getTransferred() / 1024, (int) items[i].getRawSize() / 1024, items[i].file);

			//System.out.println("add: "+items[i].file+" -> "+items[i].getTransferred()+"/"+items[i].getRawSize());

			add(p);
		}

		while (getComponentCount() < 10) {
			add(new JLabel(" "));
		}

		revalidate();
		setSelectedIndex(index);
	}

	public ProgressbarItem getSelectedValue() {
		return (ProgressbarItem) getComponent(index);
	}

	public int getSelectedIndex() {
		return index;
	}

	public void setSelectedIndex(int idx) {

		deselectAll();

		index = idx;
		if (index >= 0 && getComponentCount() > index && getComponent(index) instanceof ProgressbarItem) {
			((ProgressbarItem) getComponent(index)).select();
		}
	}

	private void deselectAll() {
		for (int i = 0; i < getComponentCount(); i++) {
			if (getComponent(i) instanceof ProgressbarItem) {
				((ProgressbarItem) getComponent(i)).deselect();
			}
		}
	}

	private String strip(String in) {
		String tmp;
		if (in.indexOf("<") >= 0) {
			in = in.substring(in.lastIndexOf("<") + 1);
			in = in.substring(0, in.lastIndexOf(">"));
		}

		return in;
	}

	public void setTransferred(String file, long bytes, String message, long max) {
		boolean ok = false;

		//System.out.println(file+":"+bytes+":"+max);

		for (int i = 0; i < getComponentCount() && !ok; i++) {
			if (getComponent(i) instanceof ProgressbarItem) {
				ProgressbarItem item = ((ProgressbarItem) getComponent(i));
				String f = strip(item.getDirEntry().file);

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
