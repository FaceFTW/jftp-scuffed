package net.sf.jftp.gui.framework;

import javax.swing.*;
import java.awt.*;

public class ProgressbarItem extends JPanel {

	private final net.sf.jftp.gui.base.dir.DirEntry d;
	private final JProgressBar bar = new JProgressBar();

	public ProgressbarItem(net.sf.jftp.gui.base.dir.DirEntry d) {
		this.d = d;
		this.setLayout(new BorderLayout(2, 2));

		this.add("Center", bar);

		bar.setStringPainted(true);
		bar.setString(d.file);

		this.setBackground(Color.WHITE);
		this.setSize(300, 25);
	}

	public void update(long size, long max, String lbl) {

		while (max > Integer.MAX_VALUE) {
			max = max / 1000;
			size = size / 1000;
		}

		if (max > 0 && max >= size) {
			bar.setMaximum((int) max);
			bar.setValue((int) size);
		} else {
			bar.setMaximum(99999999);
			bar.setValue(0);
		}

		bar.setString(lbl);

		//System.out.println(""+bar.getValue()+":"+bar.getMaximum()+" -> "+lbl);
	}

	public net.sf.jftp.gui.base.dir.DirEntry getDirEntry() {
		return d;
	}

	public void deselect() {
		this.setBackground(Color.WHITE);
		this.repaint();
	}

	public void select() {
		this.setBackground(Color.LIGHT_GRAY);
		this.repaint();
	}

	public Insets getInsets() {
		Insets in = super.getInsets();
		return new Insets(in.top + 3, in.left + 3, in.bottom + 3, in.right + 3);
	}

}
