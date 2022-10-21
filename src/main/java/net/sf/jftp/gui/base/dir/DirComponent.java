package net.sf.jftp.gui.base.dir;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class DirComponent extends DirPanel implements ListSelectionListener {

	private static final ResourceBundle uiResources = ResourceBundle.getBundle("UIText");
	public final JTable table = new JTable();
	DirPanel target;

	public DirComponent() {
		super();

		this.table.setDefaultRenderer(Object.class, new ColoredCellRenderer());
		this.table.getSelectionModel().addListSelectionListener(this);

		this.table.setRowSelectionAllowed(true);
		this.table.setColumnSelectionAllowed(false);
	}

	public void update() {
		List<String> colNames = new ArrayList<>();
		colNames.add("");
		colNames.add(uiResources.getString("name"));
		colNames.add(uiResources.getString("size"));
		colNames.add("##");

		TableUtils.layoutTable(this.jl, this.table, colNames);
	}

	/**
	 * This manages the selections
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			TableUtils.copyTableSelectionsToJList(this.jl, this.table);

			int index = this.jl.getSelectedIndex() - 1;

			if ((0 > index) || (null == this.dirEntry) || (this.dirEntry.length < index) || (null == this.dirEntry[index])) {
			} else { // -------------------- local --------------------------

				String tgt = this.jl.getSelectedValue().toString();

				for (int i = 0; i < this.dirEntry.length; i++) {
					this.dirEntry[i].setSelected(this.jl.isSelectedIndex(i + 1));
				}
			}
		}
	}


}
