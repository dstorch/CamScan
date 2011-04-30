package eastwidget;

import javax.swing.JPanel;


/**
 * The East Panel of the application. It currently contains
 * the page explorer panel.
 * 
 * @author Stelios
 *
 */
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
	
	public EastPanel() {
		super();
		
		this.pageExpPanel = new PageExplorerPanel();
		this.add(pageExpPanel);
	}
	
	/****************************************
	 * 
	 * Getters
	 * 
	 ****************************************/
	
	public PageExplorerPanel getPageExpPanel() {
		return this.pageExpPanel;
	}
}
