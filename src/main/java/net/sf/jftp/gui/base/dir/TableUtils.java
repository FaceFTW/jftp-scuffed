package net.sf.jftp.gui.base.dir;

import net.sf.jftp.config.Settings;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Vector;


public class TableUtils {

	public static void calcColumnWidths(final JTable table) {
		final JTableHeader header = table.getTableHeader();

		TableCellRenderer defaultHeaderRenderer = null;

		if (null != header) defaultHeaderRenderer = header.getDefaultRenderer();

		final TableColumnModel columns = table.getColumnModel();
		final TableModel data = table.getModel();

		final int margin = columns.getColumnMargin();

		final int rowCount = data.getRowCount();

		int totalWidth = 0;

		for (int i = columns.getColumnCount() - 1; 0 <= i; --i) {
			final TableColumn column = columns.getColumn(i);

			final int columnIndex = column.getModelIndex();

			int width = -1;

			TableCellRenderer h = column.getHeaderRenderer();

			if (null == h) h = defaultHeaderRenderer;

			if (null != h)
			{
				final Component c = h.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, -1, i);

				width = c.getPreferredSize().width;
			}

			for (int row = rowCount - 1; 0 <= row; --row) {
				final TableCellRenderer r = table.getCellRenderer(row, i);

				final Component c = r.getTableCellRendererComponent(table, data.getValueAt(row, columnIndex), false, false, row, i);

				width = Math.max(width, c.getPreferredSize().width);
			}

			if (0 <= width) column.setPreferredWidth(width + margin);

			totalWidth += column.getPreferredWidth();
		}

}

	public static void setFixedWidths(final JTable table) {
		final JTableHeader header = table.getTableHeader();

		TableCellRenderer defaultHeaderRenderer = null;

		if (null != header) defaultHeaderRenderer = header.getDefaultRenderer();

		final TableColumnModel columns = table.getColumnModel();
		final TableModel data = table.getModel();

		final int rowCount = data.getRowCount();

		for (int i = 0; i < columns.getColumnCount(); i++) {
			final TableColumn column = columns.getColumn(i);
			final int columnIndex = column.getModelIndex();
			final int width = -1;

			if (0 == i) {
				column.setPreferredWidth(20);
				column.setMaxWidth(20);
			} else if (1 == i) {
				column.setMinWidth(100);
				column.setPreferredWidth(400);
			} else if (2 == i) {
				column.setMinWidth(60);
				column.setPreferredWidth(80);

			} else if (3 == i) {
				column.setMinWidth(25);
				column.setPreferredWidth(25);
			}
		}
	}

	public static void copyTableSelectionsToJList(final JList list, final JTable listTbl) {

		list.setSelectedIndices(new int[0]);

		final int rows = listTbl.getRowCount();
		final Vector sel = new Vector();

		for (int i = 0; i < rows; i++) {
			if (listTbl.getSelectionModel().isSelectedIndex(i)) {
				sel.add(listTbl.convertRowIndexToModel(i));
			}
		}

		final int[] tmp = new int[sel.size()];
		for (int i = 0; i < sel.size(); i++) tmp[i] = (Integer) sel.get(i);

		list.setSelectedIndices(tmp);
	}


	private static synchronized TableModel generateTableModel(final JList l) {

		return new net.sf.jftp.gui.base.dir.MaterializedTableModel(l) {

			public Class getColumnClass(final int columnIndex) {
				if (0 == columnIndex) return javax.swing.ImageIcon.class;
				else if (3 == columnIndex) return javax.swing.JLabel.class;
				else return String.class;
			}

			public int getColumnCount() {
				return 4;
			}

			public int getRowCount() {
				return this.list.getModel().getSize();
			}

			public Object getValueAt(final int row, final int col) {

				if (0 == this.list.getModel().getSize()) return "" + null;

				final net.sf.jftp.gui.base.dir.DirEntry ret = (net.sf.jftp.gui.base.dir.DirEntry) this.list.getModel().getElementAt(row);

				if (0 == col) return ret.getImageIcon();
				else if (1 == col) return ret.toString();
				else if (2 == col) {
					final String tmp = "" + ret.getFileSize();
					return tmp.replaceAll(" >", "");
				} else if (3 == col) {
					return ret;
				}
return ret;
			}
		};
	}


	public static void layoutTable(final JList list, final JTable listTbl) {
		net.sf.jftp.gui.base.dir.TableUtils.layoutTable(list, listTbl, null);
	}

	public static void layoutTable(final JList list, final JTable listTbl, final Vector<String> names) {
		listTbl.setModel(net.sf.jftp.gui.base.dir.TableUtils.generateTableModel(list));

		if (Settings.useFixedTableWidths) {
			net.sf.jftp.gui.base.dir.TableUtils.setFixedWidths(listTbl);
		} else {
			net.sf.jftp.gui.base.dir.TableUtils.calcColumnWidths(listTbl);
		}

		if (null != names) net.sf.jftp.gui.base.dir.TableUtils.modifyTableHeader(listTbl.getTableHeader(), names);

		net.sf.jftp.gui.base.dir.TableUtils.tryToEnableRowSorting(listTbl);

	}


	public static void tryToEnableRowSorting(final JTable listTbl) {

		final TableRowSorter sorter = new TableRowSorter(listTbl.getModel());
		listTbl.setRowSorter(sorter);

	}

	public static void modifyTableHeader(final JTableHeader head, final Vector columnNames) {

		final TableColumnModel m = head.getColumnModel();

		if (m.getColumnCount() != columnNames.size()) {
			System.out.println("Column mismatch: " + m.getColumnCount() + "/" + columnNames.size());
			return;
		}

		for (int i = 0; i < columnNames.size(); i++) {
			final TableColumn c = m.getColumn(i);
			c.sizeWidthToFit();
			c.setHeaderValue(columnNames.get(i));
		}
	}

	public static JComponent makeTable(final JTable table, final JComponent cont) {
		final JTableHeader header = table.getTableHeader();

		cont.setLayout(new BorderLayout());
		cont.add(header, BorderLayout.NORTH);
		cont.add(table, BorderLayout.CENTER);

		return cont;
	}

}
