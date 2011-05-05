package centralwidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import search.SearchHit;
import search.SearchResults;

import core.Page;
import core.Parameters;

/**
 * Displays the search results.
 * 
 * @author Stelios
 *
 */
public class SearchResultsPanel extends JPanel implements ActionListener {
	
	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/

	/**
	 * The list displaying the results from the
	 * working document.
	 */
	private JList upperList;
	
	/**
	 * The list displaying the results from the
	 * rest of the documents.
	 */
	private JList lowerList;
	
	/**
	 * Reference to the central panel.
	 */
	private CentralPanel centralPanel;
	
	/**
	 * The index of the result selection.
	 */
	private int selectedOrder;

	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public SearchResultsPanel(CentralPanel centralPanel) {
		super();
		this.centralPanel = centralPanel;
		this.setBackground(Color.LIGHT_GRAY);
		this.setBorder(new LineBorder(Color.GRAY));
		this.selectedOrder = -1;
		
		this.setLayout(new GridLayout(3,1));
		
		/*
		 * Add the upper panel.
		 */
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		this.add(upperPanel);
		
		// Add the info label
		JLabel upperPanelInfo = new JLabel("In the working document:");
		upperPanel.add(upperPanelInfo, BorderLayout.NORTH);
		
		// Setup the results list
		this.upperList = new JList();
		this.upperList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.upperList.getSelectionModel().addListSelectionListener(new UpperListSelectionListener());
		this.upperList.setLayoutOrientation(JList.VERTICAL);
		
		// Add the scroll pane with he list to the upper panel
		JScrollPane upperTextPane = new JScrollPane(this.upperList);
		upperTextPane.setBackground(Color.WHITE);
		upperPanel.add(upperTextPane);
		
		/*
		 * Add the lower panel.
		 */
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BorderLayout());
		this.add(lowerPanel);
		
		// Add the info label
		JLabel lowerPanelInfo = new JLabel("In the other documents:");
		lowerPanel.add(lowerPanelInfo, BorderLayout.NORTH);

		// Setup the results list
		this.lowerList = new JList();
		this.lowerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.lowerList.getSelectionModel().addListSelectionListener(new LowerListSelectionListener());
		this.lowerList.setLayoutOrientation(JList.VERTICAL);
		
		// Add the scroll pane with he list to the upper panel
		JScrollPane lowerTextPane = new JScrollPane(this.lowerList);
		lowerTextPane.setBackground(Color.WHITE);
		lowerPanel.add(lowerTextPane);
		
		/*
		 * Add the button panel. 
		 */
		JPanel buttonPanel = new JPanel();
		
		// Add the GoToPage button
		JButton goToPageButton = new JButton("Go to Result");
		goToPageButton.addActionListener(this);
		buttonPanel.add(goToPageButton);
		
		// Add the GoToViewMode button
		JButton goToViewModeButton = new JButton("Go to View Mode");
		goToViewModeButton.addActionListener(new ViewModeListener());
		buttonPanel.add(goToViewModeButton);
		
		// The the button panel to the search results panel
		this.add(buttonPanel);
	}
	
	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/
	
	/**
	 * Updates the search panel with the new results.
	 */
	public void updateSearchResults() {
		
		SearchResults results = Parameters.getSearchResults();
		
		Vector<String> upperResults = new Vector<String>();
		Vector<String> lowerResults = new Vector<String>();
		
		// The first time this is called, the search
		// method hasn't been called yet, and hence
		// there are no results to display.
		if (results == null) return;

		/*
		 * Get results from the working document.
		 */
		if (!results.inWorkingDoc().isEmpty()) {
			for (SearchHit searchHit : results.inWorkingDoc()) {
				upperResults.add("Page " + searchHit.link().order() + ": " + searchHit.snippet());
			}
		} else {
			upperResults.add("Your query did not return any results from the working document.");
		}
		
		this.upperList.setListData(upperResults);
		
		/*
		 * Get results from the other documents.
		 */
		if (!results.elsewhere().isEmpty()) {
			for (SearchHit searchHit : results.elsewhere()) {
				lowerResults.add("Document " + searchHit.link().getContainingDocument().name() + ", Page " + searchHit.link().order() + ": " + searchHit.snippet());
			}
		} else {
			lowerResults.add("Your query did not return any results from the other documents.");
		}
		
		this.lowerList.setListData(lowerResults);
		
		this.repaint();
	}
	
	/****************************************
	 * 
	 * Action Listeners
	 * 
	 ****************************************/
	
	/**
	 * This actionPerformed method is called when the
	 * user clicks on the "Go to Result" button.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (this.selectedOrder == -1) {
			JOptionPane.showMessageDialog(null,
				    "Please select a valid result to go to the appropriate page.",
				    "No Result Selected",
				    JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// Serialize the previous working page
    	if (Parameters.getCoreManager().getWorkingPage() != null) {
    		try {
				Parameters.getCoreManager().getWorkingPage().serialize();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    	}
    	
    	// Update the page panel to show the selected page
    	// selected.
    	this.centralPanel.setPageOrderInPageExpPanel(this.selectedOrder);
    	this.centralPanel.setSelectedDocInDocExpPanel(Parameters.getCoreManager().workingDocument().name());

    	Page currPage = Parameters.getCoreManager().getWorkingDocPageFromName(this.selectedOrder);

    	try {
    		Parameters.getCoreManager().setWorkingPageAndImage(currPage);
    	} catch (IOException e1) {
    		e1.printStackTrace();
    	}

    	this.centralPanel.switchToViewPanel();
    	this.centralPanel.updatePanels(false);
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
	private class UpperListSelectionListener implements ListSelectionListener {
		
		/**
		 * Called when a new selection has been made.
		 * 
		 * @param e
		 */
		public void valueChanged(ListSelectionEvent e) {
		    if (e.getValueIsAdjusting() == false) {

		    	String value = (String) upperList.getSelectedValue();
		    	if (upperList.getSelectedValue() == null || !value.contains("Page"))
		    		selectedOrder = -1;
		    	else
		    		selectedOrder = this.getSelectedOrder(value);
		    }
		}
		
		/**
		 * Returns the page number given the result.
		 * 
		 * @param searchResultText The result
		 * @return The page number (order)
		 */
		public int getSelectedOrder(String searchResultText) {
			
			if (searchResultText != null && searchResultText.contains("Page")) {
				int start = searchResultText.indexOf("e");
				int end = searchResultText.indexOf(":");
				return Integer.parseInt(searchResultText.substring(start + 2, end));
			} else {
				return -1;
			}
		}
	}
	
	/**
	 * The ActionListener class for selecting items
	 * on the doc list.
	 */
	private class LowerListSelectionListener implements ListSelectionListener {
		
		/**
		 * Called when a new selection has been made.
		 * 
		 * @param e
		 */
		public void valueChanged(ListSelectionEvent e) {

		    if (e.getValueIsAdjusting() == false) {

		    	String value = (String) lowerList.getSelectedValue();
		    
		    	if (lowerList.getSelectedValue() == null || !value.contains("Page")) {
		    		selectedOrder = -1;
		    	} else {
		    		selectedOrder = this.getSelectedOrder(value);
		    		
		    		try {
						Parameters.getCoreManager().setWorkingDocumentFromName(this.getSelectedDocumentName((String) lowerList.getSelectedValue()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		    	}
		    }
		}
		
		/**
		 * Returns the page number given the result.
		 * 
		 * @param searchResultText The result
		 * @return The page number (order)
		 */
		public int getSelectedOrder(String searchResultText) {
			
			if (searchResultText != null && searchResultText.contains("Page")) {
				int start = searchResultText.lastIndexOf("Page");
				int end = searchResultText.indexOf(":");
				return Integer.parseInt(searchResultText.substring(start + 5, end));
			} else {
				return -1;
			}
		}
		
		/**
		 * Returns the selected document name.
		 * 
		 * @param searchResultText The result
		 * @return The selected document name
		 */
		public String getSelectedDocumentName(String searchResultText) {
			
			if (searchResultText != null && searchResultText.contains("Page")) {
				int start = searchResultText.lastIndexOf("Document");
				int end = searchResultText.indexOf(",");
				return searchResultText.substring(start + 9, end);
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Action Listener implementation for the GoToViewMode
	 * button.
	 */
	private class ViewModeListener implements ActionListener { 

		/**
		 * Called when the GoToViewMode button has been pressed.
		 */
		public void actionPerformed(ActionEvent arg0) {
			centralPanel.switchToViewPanel();
		} 
	}
}
