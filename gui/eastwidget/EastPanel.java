package eastwidget;

import gui.MainPanel;

import javax.swing.JPanel;


/**
 * The East Panel of the application. It currently contains
 * the page explorer panel.
 * 
 * @author Stelios
 *
 */
@SuppressWarnings("serial")
public class EastPanel extends JPanel {
	
	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
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
	 * 
	 * @param Reference to the main panel
	 */
	public EastPanel(MainPanel mainPanel) {
		super();
		this.pageExpPanel = new PageExplorerPanel(mainPanel);
		this.add(pageExpPanel);
	}
	
	/****************************************
	 * 
	 * Getters
	 * 
	 ****************************************/
	
	/**
	 * Returns the page explorer panel.
	 */
	public PageExplorerPanel getPageExpPanel() {
		return this.pageExpPanel;
	}
}
