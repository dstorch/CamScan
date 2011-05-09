package centralwidget;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;

import javax.swing.JPanel;

import core.Parameters;

import westwidget.WestPanel;

import eastwidget.EastPanel;

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
	
	/**
	 * Reference to the east panel.
	 */
	private EastPanel eastPanel;
	
	/**
	 * Reference to the west panel.
	 */
	private WestPanel westPanel;
	
	/****************************************
	 * 
	 * Constructors.
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public CentralPanel(EastPanel eastPanel) {
		super();
		this.setLayout(new BorderLayout());
		this.eastPanel = eastPanel;
		
		// Add the toolbar panel.
		this.toolbarPanel = new ToolbarPanel(this);
		this.add(this.toolbarPanel, BorderLayout.NORTH);
		
		// Setup and add the view panel.
		this.viewPanel = new ViewPanel();
//		this.viewPanel.setPreferredSize(this.viewPanel.getPreferredSize());
//		System.out.println(this.viewPanel.getPreferredSize());
//		JScrollPane viewScrollPane = new JScrollPane(this.viewPanel);
//		viewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		viewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//		viewScrollPane.getHorizontalScrollBar().addAdjustmentListener(new HorizontalScrollBarListener());
//		viewScrollPane.getVerticalScrollBar().setUnitIncrement(10);
//		this.add(viewScrollPane, BorderLayout.CENTER);
		this.add(viewPanel, BorderLayout.CENTER);
		
		// Setup the edit panel.
		this.editPanel = new EditPanel();
		
		// Setup the search results panel.
		this.searchResultsPanel = new SearchResultsPanel(this);
		this.searchResultsPanel.setSize(this.getSize());
		
		// Add the button panel.
		this.buttonPanel = new ButtonPanel(this.editPanel, this);
		this.buttonPanel.setComponentsVisible(false);
		this.add(this.buttonPanel, BorderLayout.SOUTH);
	}
	
	private class HorizontalScrollBarListener implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
			System.out.println(e.getValue());
			
		}
		
	}
	
	/**
	 * Returns the view panel.
	 * 
	 * @return The view panel
	 */
	public ViewPanel getViewPanel() {
		return this.viewPanel;
	}
	
	/**
	 * Returns the edit panel.
	 * 
	 * @return The edit panel
	 */
	public EditPanel getEditPanel() {
		return this.editPanel;
	}
	
	/****************************************
	 * 
	 * Setters
	 * 
	 ****************************************/
	
	/**
	 * Sets the west panel.
	 * 
	 * @param westPanel The west panel to set
	 */
	public void setWestPanel(WestPanel westPanel) {
		this.westPanel = westPanel;
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

		Parameters.getCoreManager().updateProcessedImage();
		
		this.toolbarPanel.selectViewRButton();
		
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
        this.toolbarPanel.showZoomButtons();
	}
	
	/**
	 * Switches to the edit panel.
	 */
	public void switchToEditPanel() {
	
		Parameters.getCoreManager().setProcessedImage(Parameters.getCoreManager().getRawImage());
		
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
                //this.toolbarPanel.hideZoomButtons();
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
                //this.toolbarPanel.hideZoomButtons();
	}
	
	/**
	 * Calls repaint() on the search, view and
	 * edit panels to draw the image once
	 * a new current image has been selected.
	 */
	public void updatePanels(boolean updateSearchPanel) {
		this.viewPanel.repaint();
		this.editPanel.updateCornersOnPanel();
		this.editPanel.repaint();
		
		if (updateSearchPanel) {
			this.searchResultsPanel.updateSearchResults();
			this.searchResultsPanel.repaint();
		}
	}
	
	/**
	 * Sets the order in the page explorer panel.
	 * 
	 * @param order The given order
	 */
	public void setPageOrderInPageExpPanel(int order) {
		this.eastPanel.getPageExpPanel().setPageOrder(order);
	}
	
	public void setSelectedDocInDocExpPanel(String docName) {
		this.westPanel.getDocExpPanel().setDocOrder(docName);
	}
}
