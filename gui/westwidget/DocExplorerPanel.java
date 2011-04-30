package westwidget;

import java.awt.Dimension;
import java.io.File;
import java.io.FileFilter;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import eastwidget.PageExplorerPanel;
import gui.ParamHolder;



/**
 * The document tree that will appear on the
 * document explorer panel.
 * 
 * @author Stelios
 *
 */

public class DocExplorerPanel extends JPanel {

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * The document tree.
	 */
	private JList docList;
	
	private JScrollPane listScroller;
	
	/**
	 * Reference to the page explorer panel.
	 */
	private PageExplorerPanel pageExpPanel;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public DocExplorerPanel(PageExplorerPanel pageExpPanel) {
		super();
		
		this.pageExpPanel = pageExpPanel;
		
		this.docList = new JList(/*this.getDocuments(ParamHolder.getWorkspace())*/);
		
		this.docList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.docList.getSelectionModel().addListSelectionListener(new SelectionListener());
		this.docList.setLayoutOrientation(JList.VERTICAL);
		
		this.listScroller = new JScrollPane(this.docList);
		this.listScroller.setPreferredSize(new Dimension(150, 600));
		this.add(this.listScroller);
	}
	
	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/
	
	/**
	 * Updates the Document Panel. To be called
	 * when files have been added or removed in the workspace.
	 */
	public void update() {
		this.docList.setListData(this.getDocuments(ParamHolder.getWorkspace()));
		this.listScroller.revalidate();
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
		        String currDocName = (String) docList.getSelectedValue();   
		        //pageExpPanel.setPageList(pages);
		    }
		}
	}
}
