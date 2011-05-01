package eastwidget;

import gui.MainPanel;

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

import core.Document;
import core.Page;
import core.Parameters;


/**
 * The page tree that will appear on the
 * page explorer panel.
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
	
	/**
	 * The list scroller.
	 */
	private JScrollPane listScroller;
	
	/**
	 * Reference to the Main Panel.
	 */
	private MainPanel mainPanel;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public PageExplorerPanel(MainPanel mainPanel) {
		
		this.mainPanel = mainPanel;
		this.pageList = new JList(this.getPageNames());
		
		this.pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.pageList.getSelectionModel().addListSelectionListener(new SelectionListener());
		this.pageList.setLayoutOrientation(JList.VERTICAL);
		
		this.listScroller = new JScrollPane(this.pageList);
		this.listScroller.setPreferredSize(new Dimension(150, 600));
		this.add(this.listScroller);
	}
	
	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/
	
	/**
	 * Updates the Page Panel. To be called
	 * when the working document has changed.
	 */
	public void update() {
		this.pageList.setListData(this.getPageNames());
		this.listScroller.revalidate();
		this.pageList.setSelectedIndex(0);
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
	private Vector<String> getPageNames() {
		Vector<String> pages = new Vector<String>();
		
		Document workingDoc = Parameters.getCoreManager().workingDocument();
		
		for (Page page : workingDoc.pages()) {
			pages.add(Integer.toString(page.order()));
		}
		
		return pages;
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
		    	
		    	// Get the current page and draw it on the panel.
		        String currPageName = (String) pageList.getSelectedValue();
		        if (currPageName != null) {
		        	Page currPage = Parameters.getCoreManager().getWorkingDocPageFromOrder(Integer.parseInt(currPageName));
		        	Parameters.setCurrPageImg(currPage.raw());
		        	mainPanel.updateCentralPanels();
		        }
		    }
		}
	}
}
