package view.gui.base.dir;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

abstract class MaterializedTableModel extends AbstractTableModel {

	final JList list;

	public MaterializedTableModel(JList list) {
		super();

		this.list = list;
	}

}
 