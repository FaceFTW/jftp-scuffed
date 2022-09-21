package net.sf.jftp.gui.base.dir;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ColoredCellRenderer extends DefaultTableCellRenderer {


    public Component getTableCellRendererComponent
            (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        //System.out.println("-> "+row+"/"+column + " ("+TableUtils.STATUS_COLUMN+"), "+value);
		/*
		if(column == TableUtils.STATUS_COLUMN)
		{
			if(value == null || ((String)value) == null) {
				cell.setBackground(Color.RED);
				return cell;
			}

			String status = (String) value;

			if(status.equals("0")) cell.setBackground(Color.BLUE);
			else if(status.equals("1")) cell.setBackground(Color.GREEN);
			else if(status.equals("2")) cell.setBackground(Color.YELLOW);
			else if(status.equals("3")) cell.setBackground(Color.YELLOW);
			else cell.setBackground(Color.RED);				
		}
		else {
		 */

        if (isSelected) cell.setBackground(table.getSelectionBackground());
        else cell.setBackground(table.getBackground());

        ((JComponent) cell).setBorder(
                new CompoundBorder(
                        new EmptyBorder(new Insets(2, 4, 2, 4)),
                        ((JComponent) cell).getBorder()));
        //}

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

