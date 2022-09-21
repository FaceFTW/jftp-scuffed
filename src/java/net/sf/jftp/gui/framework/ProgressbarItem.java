package net.sf.jftp.gui.framework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.sf.jftp.gui.base.dir.DirEntry;

public class ProgressbarItem extends JPanel {

	private final DirEntry d;
	private final JProgressBar bar = new JProgressBar();
	
	public ProgressbarItem(DirEntry d) {
		this.d = d;
		setLayout(new BorderLayout(2,2));
		
		add("Center", bar);
		
		bar.setStringPainted(true);
		bar.setString(d.file);
		
		setBackground(Color.WHITE);
		setSize(300,25);
	}
	
	public void update(long size, long max, String lbl) {
	
		while(max > Integer.MAX_VALUE) {
			max = max / 1000;
			size = size / 1000;
		}
		
		if(max > 0 && max >= size) {
			bar.setMaximum((int) max);
			bar.setValue((int) size);
		}
		else {
			bar.setMaximum(99999999);
			bar.setValue(0);
		}
		
		bar.setString(lbl);	
				
		//System.out.println(""+bar.getValue()+":"+bar.getMaximum()+" -> "+lbl);
	}
	
	public DirEntry getDirEntry() {
		return d;
	}
	
	public void deselect() {
		setBackground(Color.WHITE);
		repaint();
	}
	
	public void select() {
		setBackground(Color.LIGHT_GRAY);
		repaint();
	}
	
	public Insets getInsets() {
        Insets in = super.getInsets();
        return new Insets(in.top+3,in.left+3,in.bottom+3,in.right+3);
	}
	
}
