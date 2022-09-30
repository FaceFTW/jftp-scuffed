package net.sf.jftp.gui.base.dir;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ColoredCellRenderer extends DefaultTableCellRenderer {


	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


		if (isSelected) cell.setBackground(table.getSelectionBackground());
		else cell.setBackground(table.getBackground());

		((JComponent) cell).setBorder(new CompoundBorder(new EmptyBorder(new Insets(2, 4, 2, 4)), ((JComponent) cell).getBorder()));

		if (2 == column || 3 == column) {
			this.setHorizontalAlignment(JLabel.RIGHT);
		} else {
			this.setHorizontalAlignment(JLabel.LEFT);
		}

		if (3 == column) {
			final int x = ((DirEntry) value).getPermission();
			if (net.sf.jftp.gui.base.dir.DirEntry.R == x) {
				cell.setBackground(Color.WHITE);
				((JLabel) cell).setText("r-");
			} else if (net.sf.jftp.gui.base.dir.DirEntry.W == x) {
				cell.setBackground(new Color(230, 255, 230));
				((JLabel) cell).setText("rw");
			} else if (net.sf.jftp.gui.base.dir.DirEntry.DENIED == x) {
				cell.setBackground(new Color(255, 230, 230));
				((JLabel) cell).setText("--");
			}

		}


		return cell;

	}
}

