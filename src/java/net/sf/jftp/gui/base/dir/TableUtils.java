package net.sf.jftp.gui.base.dir;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;


public class TableUtils {
	
	
	/**
	 * Setzt die Breite der TableColumns.
	 * 
	 * Quelle: http://www.chka.de/swing/table/cell-sizes.html
	 * 
	 * @param table
	 */
	public static void calcColumnWidths(JTable table)
	{
	    JTableHeader header = table.getTableHeader();

	    TableCellRenderer defaultHeaderRenderer = null;

	    if (header != null)
	        defaultHeaderRenderer = header.getDefaultRenderer();

	    TableColumnModel columns = table.getColumnModel();
	    TableModel data = table.getModel();

	    int margin = columns.getColumnMargin(); // only JDK1.3

	    int rowCount = data.getRowCount();

	    int totalWidth = 0;

	    for (int i = columns.getColumnCount() - 1; i >= 0; --i)
	    {
	        TableColumn column = columns.getColumn(i);
	            
	        int columnIndex = column.getModelIndex();
	            
	        int width = -1; 

	        TableCellRenderer h = column.getHeaderRenderer();
	          
	        if (h == null)
	            h = defaultHeaderRenderer;
	            
	        if (h != null) // Not explicitly impossible
	        {
	            Component c = h.getTableCellRendererComponent
	                   (table, column.getHeaderValue(),
	                    false, false, -1, i);
	                    
	            width = c.getPreferredSize().width;
	        }
	       
	        for (int row = rowCount - 1; row >= 0; --row)
	        {
	            TableCellRenderer r = table.getCellRenderer(row, i);
	                 
	            Component c = r.getTableCellRendererComponent
	               (table,
	                data.getValueAt(row, columnIndex),
	                false, false, row, i);
	        
	                width = Math.max(width, c.getPreferredSize().width);
	        }

	        if (width >= 0)
	            column.setPreferredWidth(width + margin); // <1.3: without margin
	        else
	            ; // ???
	            
	        totalWidth += column.getPreferredWidth();
	    }

//	 only <1.3:   totalWidth += columns.getColumnCount() * columns.getColumnMargin();


	    /* If you like; This does not make sense for two many columns!
	    Dimension size = table.getPreferredScrollableViewportSize();

	    size.width = totalWidth;

	    table.setPreferredScrollableViewportSize(size);
	    */

	    // table.sizeColumnsToFit(-1); <1.3; possibly even table.revalidate()

	    // if (header != null)
	    //     header.repaint(); only makes sense when the header is visible (only <1.3)
	}
	
	public static void setFixedWidths(JTable table) {
	    JTableHeader header = table.getTableHeader();

	    TableCellRenderer defaultHeaderRenderer = null;

	    if (header != null)
	        defaultHeaderRenderer = header.getDefaultRenderer();

	    TableColumnModel columns = table.getColumnModel();
	    TableModel data = table.getModel();

	    int rowCount = data.getRowCount();

	    for (int i = 0; i<columns.getColumnCount(); i++)
	    {
	        TableColumn column = columns.getColumn(i);            
	        int columnIndex = column.getModelIndex();            
	        int width = -1; 

	        if(i == 0) {
	        	column.setPreferredWidth(20); 
	        	column.setMaxWidth(20);
	        }
	        else if(i == 1) {
	        	column.setMinWidth(100); 
	        	column.setPreferredWidth(400); 
	        }
	        else if(i == 2) {
	        	column.setMinWidth(60); 
	        	column.setPreferredWidth(80); 
	        	//column.setMaxWidth(90);
	        }
	        else if(i == 3) {
	        	column.setMinWidth(25); 
	        	column.setPreferredWidth(25); 
	        	//column.setMaxWidth(90);
	        }
	    }
	}
	
	/**
	 * Synchronisiert eine JList mit einem JTable.
	 * 
	 * Die Selections werden von dem Table auf die List kopiert.
	 * 
	 * @param list
	 * @param listTbl
	 */
	public static void copyTableSelectionsToJList(JList list, JTable listTbl) {

		list.setSelectedIndices(new int[0]);

		int rows = listTbl.getRowCount();
		Vector sel = new Vector();
		
		for(int i=0; i<rows; i++) {
			if(listTbl.getSelectionModel().isSelectedIndex(i)) {
				sel.add(new Integer(listTbl.convertRowIndexToModel(i)));
			}
		}
		
		int[] tmp = new int[sel.size()];
		for(int i=0; i<sel.size(); i++) tmp[i] = ((Integer)sel.get(i)).intValue();
		
		list.setSelectedIndices(tmp);
	}
		
	/**
	 * Generisches Modell erzeugen.
	 * 
	 * JList muss Vektoren von im JTable anzeigbaren Objekten enthalten.
	 * 
	 * @param l
	 * @return
	 */
	private static synchronized TableModel generateTableModel(JList l) {
		
	      TableModel dataModel = new MaterializedTableModel(l) {
	    	  
	          public Class getColumnClass(int columnIndex) {
	                if(columnIndex == 0) return ImageIcon.class;
	                else if(columnIndex == 3) return JLabel.class;
	                else return String.class;
	          }
	    	  
	          public int getColumnCount() { 
	        	  //return (list.getModel().getSize() > 0 ? ((Vector)list.getModel().getElementAt(0)).size() : 0);
	        	  return 4;
	          }
	          
	          public int getRowCount() { 
	        	  return list.getModel().getSize();
	          }
	          
	          public Object getValueAt(int row, int col) {

	        	  if(list.getModel().getSize() == 0) return ""+null;

	        	  DirEntry ret = (DirEntry) list.getModel().getElementAt(row);
	        	  
	        	  if(col == 0) return ret.getImageIcon();
	        	  else if(col == 1) return ret.toString();
	        	  else if(col == 2) {
	        		  String tmp = ""+ret.getFileSize();
	        		  String r = tmp.replaceAll(" >", "");
	        		  return r;
	        	  }
	        	  else if(col == 3) {
	        		  return ret;
	        	  }

	        	  //System.out.println(">>> "+ret.get(col)+" -> "+(ret.get(col) instanceof Status));

	        	  //return ret.size() > col ? ret.get(col) : "<ERROR>";
	        	  return ret;
	          }
	      };

	      return dataModel;
	}
	
	
	/**
	 * F?hrt Updates auf einen beliebigen JTable durch.
	 * 
	 * list muss hierzu vom Typ Vector<String> sein.
	 * 
	 * @param list
	 * @param listTbl
	 */
	public static void layoutTable(JList list, JTable listTbl) {
		layoutTable(list, listTbl, null);
	}
	
	/**
	 * F?hrt Updates auf einen beliebigen JTable durch.
	 * 
	 * list muss hierzu vom Typ Vector<String> sein.
	 */
	public static void layoutTable(JList list, JTable listTbl, Vector names) {
		    listTbl.setModel(generateTableModel(list));
		    
		    if(Settings.useFixedTableWidths) {
		    	setFixedWidths(listTbl);
		    }
		    else {
		    	calcColumnWidths(listTbl);
		    }
		    
			if(names != null) modifyTableHeader(listTbl.getTableHeader(), names);
			
			// 1.6+ only
			tryToEnableRowSorting(listTbl);
			
			//listTbl.doLayout();
	}
	

	public static void tryToEnableRowSorting(JTable listTbl) {

		TableRowSorter sorter = new TableRowSorter(listTbl.getModel());
		listTbl.setRowSorter(sorter);

	}
	
	/**
	 * Setzt den Header einer JTable
	 * @param head
	 * @param columnNames
	 */
	public static void modifyTableHeader(JTableHeader head, Vector columnNames) {
		
		TableColumnModel m = head.getColumnModel();
		
		if(m.getColumnCount() != columnNames.size()) {
			System.out.println("Column mismatch: "+m.getColumnCount()+"/"+columnNames.size());
			return;
		}
		
		for(int i=0; i<columnNames.size(); i++) {
			TableColumn c = m.getColumn(i);
			c.sizeWidthToFit();
			c.setHeaderValue(columnNames.get(i));
		}
	}
	
	/**
	 * Erzeugt einen Panel mit View und Header eines JTables.
	 * 
	 */
	public static JComponent makeTable(JTable table, JComponent cont) {
		JTableHeader header = table.getTableHeader();
		
		cont.setLayout(new BorderLayout());
		cont.add(header, BorderLayout.NORTH);
		cont.add(table, BorderLayout.CENTER);
		
		return cont;
	}
	
}
