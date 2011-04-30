package eastwidget;

import java.awt.Dimension;
import java.io.File;
import java.io.FileFilter;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * The document tree that will appear on the
 * document explorer panel.
 * 
 * @author Stelios
 *
 */

public class PageExplorerPanel extends JPanel {

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * The document tree.
	 */
	private JList pageList;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public PageExplorerPanel() {
		
		this.pageList = new JList(this.getDocuments("dummydocs"));
		
		this.pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.pageList.getSelectionModel().addListSelectionListener(new SelectionListener());
		this.pageList.setLayoutOrientation(JList.VERTICAL);
		
		JScrollPane listScroller = new JScrollPane(this.pageList);
		listScroller.setPreferredSize(new Dimension(150, 600));
		this.add(listScroller);
	}
	
	/**
	 * Sets the page list.
	 * 
	 * @param pages
	 */
	public void setPageList(Vector<String> pages) {
		this.pageList.setListData(pages);
	}
	
	/****************************************
	 * 
	 * Private Methods
	 * 
	 ****************************************/
	
	/**
	 * Given the path to the workspace, it returns
	 * a vector of of all the Documents in that directory.
	 * 
	 * @param workspacePath The path to the workspace
	 */
	private Vector<String> getDocuments(String workspacePath) {
		Vector<String> docs = new Vector<String>();
		File dir = new File(workspacePath);
		
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		};
		
		File[] children = dir.listFiles(fileFilter);
		
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				
		        // Get filename of directory
				docs.add(children[i].getName());
		    }
		}
		
		return docs;
	}
	
	/****************************************
	 * 
	 * Private Classes
	 * 
	 ****************************************/

	/**
	 * The ActionListener class for selecting items
	 * on the doc list.
	 */
	private class SelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
		    if (e.getValueIsAdjusting() == false) {
		        String currDoc = (String) pageList.getSelectedValue();
		    }
		}
	}
}
