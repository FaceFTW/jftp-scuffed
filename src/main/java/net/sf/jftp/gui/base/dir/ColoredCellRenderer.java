package net.sf.jftp.gui.base.dir;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ColoredCellRenderer extends DefaultTableCellRenderer {


	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


		if (isSelected) cell.setBackground(table.getSelectionBackground());
		else cell.setBackground(table.getBackground());

		((JComponent) cell).setBorder(new CompoundBorder(new EmptyBorder(new Insets(2, 4, 2, 4)), ((JComponent) cell).getBorder()));

		if (column == 2 || column == 3) {
			setHorizontalAlignment(JLabel.RIGHT);
		} else {
			setHorizontalAlignment(JLabel.LEFT);
		}

		if (column == 3) {
			int x = ((DirEntry) value).getPermission();
			if (x == DirEntry.R) {
				cell.setBackground(Color.WHITE);
				((JLabel) cell).setText("r-");
			} else if (x == DirEntry.W) {
				cell.setBackground(new Color(230, 255, 230));
				((JLabel) cell).setText("rw");
			} else if (x == DirEntry.DENIED) {
				cell.setBackground(new Color(255, 230, 230));
				((JLabel) cell).setText("--");
			}

		}


		return cell;

	}
}

