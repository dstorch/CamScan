package westwidget;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import centralwidget.CentralPanel;



import eastwidget.PageExplorerPanel;

/**
 * The West Panel of the application. It currently contains
 * the document explorer panel, the drag'n'drop panel, and the
 * search panel.
 * 
 * @author Stelios
 *
 */
public class WestPanel extends JPanel {

	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 * 
	 * @param pageExpPanel Reference to the page explorer
	 * panel
	 */
	public WestPanel(PageExplorerPanel pageExpPanel, CentralPanel centralPanel) {
		super();
		this.setLayout(new BorderLayout());
		
		// Setup the search panel
		SearchPanel searchPanel = new SearchPanel(centralPanel);
		this.add(searchPanel, BorderLayout.NORTH);
		
		// Setup the document explorer panel
		DocExplorerPanel docExpPanel = new DocExplorerPanel(pageExpPanel);
		this.add(docExpPanel, BorderLayout.CENTER);
	
		// Setup the drag'n'drop panel
		DDPanel ddPanel = new DDPanel(docExpPanel);
		this.add(ddPanel, BorderLayout.SOUTH);
	}
}
