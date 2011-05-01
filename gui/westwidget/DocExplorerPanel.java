package westwidget;

import java.awt.Dimension;
import java.io.File;
import java.io.FileFilter;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import centralwidget.CentralPanel;

import core.Document;
import core.Page;
import core.Parameters;

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
	
	/**
	 * The list scroller.
	 */
	private JScrollPane listScroller;
	
	/**
	 * Reference to the page explorer panel.
	 */
	private PageExplorerPanel pageExpPanel;
	
	/**
	 * Reference to the Central Panel.
	 */
	private CentralPanel centralPanel;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public DocExplorerPanel(PageExplorerPanel pageExpPanel, CentralPanel centralPanel) {
		super();
		this.centralPanel = centralPanel;
		
		this.pageExpPanel = pageExpPanel;
		
		this.docList = new JList(this.getDocumentNames());
		
		this.docList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.docList.getSelectionModel().addListSelectionListener(new SelectionListener());
		this.docList.setLayoutOrientation(JList.VERTICAL);
		this.docList.setSelectedIndex(0);
		
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
		this.docList.setListData(this.getDocumentNames());
		this.listScroller.revalidate();
	}
	
	/****************************************
	 * 
	 * Private Methods
	 * 
	 ****************************************/
	
	/**
	 * Returns a vector of of all the document names
	 * in the workspace.
	 * 
	 * @return Vector of all the document names in the
	 * workspace
	 */
	private Vector<String> getDocumentNames() {
		Vector<String> docs = new Vector<String>();
		
		for (Document doc : Parameters.getCoreManager().getDocuments()) {
			docs.add(doc.name());
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
				
				// Get the current selection and set it as the working document.
				String currDocName = (String) docList.getSelectedValue();   
				Parameters.getCoreManager().setWorkingDocumentFromName(currDocName);
				
				// Update the page explorer panel with the pages of the
				// new working document.
				pageExpPanel.update();

				// Get the very first page and display its image on the
				// central panel.
				Page currPage = Parameters.getCoreManager().workingDocument().pages().get(0);
				Parameters.setCurrPageImg(currPage.raw());
				centralPanel.updatePanels();
			}
		}
	}
}
