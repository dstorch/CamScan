package centralwidget;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * The Central Panel of this application.
 * The pages and the search results are displayed here.
 * 
 * @author Stelios
 *
 */
public class CentralPanel extends JPanel {

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * The view panel.
	 */
	private ViewPanel viewPanel;
	
	/**
	 * The edit panel.
	 */
	private EditPanel editPanel;
	
	/**
	 * The button panel.
	 */
	private ButtonPanel buttonPanel;
	
	/**
	 * The search results panel.
	 */
	private SearchResultsPanel searchResultsPanel;
	
	/**
	 * The toolbar panel.
	 */
	private ToolbarPanel toolbarPanel;
	
	/****************************************
	 * 
	 * Constructors.
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public CentralPanel() {
		super();
		this.setLayout(new BorderLayout());
		
		// Add the toolbar panel.
		this.toolbarPanel = new ToolbarPanel(this);
		this.add(this.toolbarPanel, BorderLayout.NORTH);
		
		// Setup and add the view panel.
		this.viewPanel = new ViewPanel(); 
		this.add(this.viewPanel, BorderLayout.CENTER);
		
		// Setup the edit panel.
		this.editPanel = new EditPanel();
		
		// Setup the search results panel.
		this.searchResultsPanel = new SearchResultsPanel();
		
		// Add the button panel.
		this.buttonPanel = new ButtonPanel();
		this.buttonPanel.setComponentsVisible(false);
		this.add(this.buttonPanel, BorderLayout.SOUTH);
	}
	
	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/
	
	/**
	 * Switches to the view panel.
	 */
	public void switchToViewPanel() {
		
		if (this.editPanel.getParent() != null) {
			this.editPanel.setVisible(false);
			this.remove(this.editPanel);
		}
		
		if (this.searchResultsPanel.getParent() != null) {
			this.searchResultsPanel.setVisible(false);
			this.remove(this.searchResultsPanel);
		}

		this.add(this.viewPanel, BorderLayout.CENTER);
		this.viewPanel.setVisible(true);
		this.buttonPanel.setComponentsVisible(false);
	}
	
	/**
	 * Switches to the edit panel.
	 */
	public void switchToEditPanel() {
		
		if (this.viewPanel.getParent() != null) {
			this.viewPanel.setVisible(false);
			this.remove(this.viewPanel);
		}
		
		if (this.searchResultsPanel.getParent() != null) {
			this.searchResultsPanel.setVisible(false);
			this.remove(this.searchResultsPanel);
		}

		this.add(this.editPanel, BorderLayout.CENTER);
		this.editPanel.setVisible(true);
		this.buttonPanel.setComponentsVisible(true);
	}
	
	/**
	 * Switches to the search results panel.
	 */
	public void switchToSearchResultsPanel() {
		
		this.toolbarPanel.unselectModeButtons();

		if (this.viewPanel.getParent() != null) {
			this.viewPanel.setVisible(false);
			this.remove(this.viewPanel);
		}
		
		if (this.editPanel.getParent() != null) {
			this.editPanel.setVisible(false);
			this.remove(this.editPanel);
		}

		this.add(this.searchResultsPanel, BorderLayout.CENTER);
		this.searchResultsPanel.setVisible(true);
		this.buttonPanel.setComponentsVisible(false);
	}
	
	/**
	 * Calls repaint() on the search, view and
	 * edit panels to draw the image once
	 * a new current image has been selected.
	 */
	public void updatePanels() {
		this.viewPanel.repaint();
		this.editPanel.updateCornersOnPanel();
		this.editPanel.repaint();
		this.searchResultsPanel.updateSearchResults();
		this.searchResultsPanel.repaint();
	}
}
