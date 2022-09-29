package net.sf.jftp.gui.base.dir;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public abstract class MaterializedTableModel extends AbstractTableModel {

	protected final JList list;

	public MaterializedTableModel(JList list) {
		super();

		this.list = list;
	}

}
 