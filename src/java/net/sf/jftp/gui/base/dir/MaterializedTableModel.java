package net.sf.jftp.gui.base.dir;

import javax.swing.JList;
import javax.swing.table.AbstractTableModel;

/**
 * Helperklasse für TableUtils.generate*Model()
 *
 */
public abstract class MaterializedTableModel extends AbstractTableModel {

	protected JList list;
	
	/**
	 * Speichert eine JList auf die später in einem TableModel zugegriffen werden kann.
	 * 
	 * @param list JList
	 */
	public MaterializedTableModel(JList list) {
		super();
		
		this.list = list;  
	}
	
}
 