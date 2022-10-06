package net.sf.jftp.gui.base.dir;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

class ColoredCellRenderer extends DefaultTableCellRenderer {


	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


		if (isSelected) cell.setBackground(table.getSelectionBackground());
		else cell.setBackground(table.getBackground());

		((JComponent) cell).setBorder(new CompoundBorder(new EmptyBorder(new Insets(2, 4, 2, 4)), ((JComponent) cell).getBorder()));

		if (2 == column || 3 == column) {
			this.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		} else {
			this.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		}

		if (3 == column) {
			int x = ((DirEntry) value).getPermission();
			if (DirEntry.R == x) {
				cell.setBackground(Color.WHITE);
				((JLabel) cell).setText("r-");
			} else if (DirEntry.W == x) {
				cell.setBackground(new Color(230, 255, 230));
				((JLabel) cell).setText("rw");
			} else if (DirEntry.DENIED == x) {
				cell.setBackground(new Color(255, 230, 230));
				((JLabel) cell).setText("--");
			}

		}


		return cell;

	}
}

