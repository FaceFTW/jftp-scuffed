package net.sf.jftp.gui.base.dir;

import net.sf.jftp.config.Settings;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;


public class TableUtils {

	public static void calcColumnWidths(JTable table) {
		JTableHeader header = table.getTableHeader();

		TableCellRenderer defaultHeaderRenderer = null;

		if (null != header) defaultHeaderRenderer = header.getDefaultRenderer();

		TableColumnModel columns = table.getColumnModel();
		TableModel data = table.getModel();

		int margin = columns.getColumnMargin();

		int rowCount = data.getRowCount();

		int totalWidth = 0;

		for (int i = columns.getColumnCount() - 1; 0 <= i; --i) {
			TableColumn column = columns.getColumn(i);

			int columnIndex = column.getModelIndex();

			int width = -1;

			TableCellRenderer h = column.getHeaderRenderer();

			if (null == h) h = defaultHeaderRenderer;

			if (null != h) {
				Component c = h.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, -1, i);

				width = c.getPreferredSize().width;
			}

			for (int row = rowCount - 1; 0 <= row; --row) {
				TableCellRenderer r = table.getCellRenderer(row, i);

				Component c = r.getTableCellRendererComponent(table, data.getValueAt(row, columnIndex), false, false, row, i);

				width = Math.max(width, c.getPreferredSize().width);
			}

			if (0 <= width) column.setPreferredWidth(width + margin);

			totalWidth += column.getPreferredWidth();
		}

	}

	public static void setFixedWidths(JTable table) {
		JTableHeader header = table.getTableHeader();

		TableCellRenderer defaultHeaderRenderer = null;

		if (null != header) defaultHeaderRenderer = header.getDefaultRenderer();

		TableColumnModel columns = table.getColumnModel();
		TableModel data = table.getModel();

		int rowCount = data.getRowCount();

		for (int i = 0; i < columns.getColumnCount(); i++) {
			TableColumn column = columns.getColumn(i);
			int columnIndex = column.getModelIndex();
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

	public static void copyTableSelectionsToJList(JList list, JTable listTbl) {

		list.setSelectedIndices(new int[0]);

		int rows = listTbl.getRowCount();
		List<Integer> sel = new ArrayList<>();

		for (int i = 0; i < rows; i++) {
			if (listTbl.getSelectionModel().isSelectedIndex(i)) {
				sel.add(listTbl.convertRowIndexToModel(i));
			}
		}

		int[] tmp = new int[sel.size()];
		for (int i = 0; i < sel.size(); i++) tmp[i] = (Integer) sel.get(i);

		list.setSelectedIndices(tmp);
	}


	private static synchronized TableModel generateTableModel(JList l) {

		return new MaterializedTableModel(l) {

			public Class getColumnClass(int columnIndex) {
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

			public Object getValueAt(int row, int col) {

				if (0 == this.list.getModel().getSize()) return "" + null;

				DirEntry ret = (DirEntry) this.list.getModel().getElementAt(row);

				if (0 == col) return ret.getImageIcon();
				else if (1 == col) return ret.toString();
				else if (2 == col) {
					String tmp = "" + ret.getFileSize();
					return tmp.replaceAll(" >", "");
				} else if (3 == col) {
					return ret;
				}
				return ret;
			}
		};
	}


	public static void layoutTable(JList list, JTable listTbl) {
		layoutTable(list, listTbl, null);
	}

	public static void layoutTable(JList list, JTable listTbl, List<String> names) {
		listTbl.setModel(generateTableModel(list));

		if (Settings.useFixedTableWidths) {
			setFixedWidths(listTbl);
		} else {
			calcColumnWidths(listTbl);
		}

		if (null != names) modifyTableHeader(listTbl.getTableHeader(), names);

		tryToEnableRowSorting(listTbl);

	}


	public static void tryToEnableRowSorting(JTable listTbl) {

		TableRowSorter sorter = new TableRowSorter(listTbl.getModel());
		listTbl.setRowSorter(sorter);

	}

	public static void modifyTableHeader(JTableHeader head, List<String> columnNames) {

		TableColumnModel m = head.getColumnModel();

		if (m.getColumnCount() != columnNames.size()) {
			System.out.println("Column mismatch: " + m.getColumnCount() + "/" + columnNames.size());
			return;
		}

		for (int i = 0; i < columnNames.size(); i++) {
			TableColumn c = m.getColumn(i);
			c.sizeWidthToFit();
			c.setHeaderValue(columnNames.get(i));
		}
	}

	public static JComponent makeTable(JTable table, JComponent cont) {
		JTableHeader header = table.getTableHeader();

		cont.setLayout(new BorderLayout());
		cont.add(header, BorderLayout.NORTH);
		cont.add(table, BorderLayout.CENTER);

		return cont;
	}

}
