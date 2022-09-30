package net.sf.jftp.gui.base.dir;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Vector;


public class DirComponent extends DirPanel implements ListSelectionListener {

	public final JTable table = new JTable();
	DirPanel target;

	public DirComponent() {

		table.setDefaultRenderer(Object.class, new ColoredCellRenderer());
		table.getSelectionModel().addListSelectionListener(this);

		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
	}

	public void update() {
		final Vector<String> colNames = new Vector<String>();
		colNames.add("");
		colNames.add("Name");
		colNames.add("Size");
		colNames.add("##");

		TableUtils.layoutTable(jl, table, colNames);
	}

	/**
	 * This manages the selections
	 */
	public void valueChanged(final ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			TableUtils.copyTableSelectionsToJList(jl, table);

			final int index = jl.getSelectedIndex() - 1;

			if ((index < 0) || (dirEntry == null) || (dirEntry.length < index) || (dirEntry[index] == null)) {
			} else { // -------------------- local --------------------------

				final String tgt = jl.getSelectedValue().toString();

				for (int i = 0; i < dirEntry.length; i++) {
					dirEntry[i].setSelected(jl.isSelectedIndex(i + 1));
				}
			}
		}
	}


}
