package centralwidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import search.SearchHit;
import search.SearchResults;

import core.Parameters;

/**
 * Displays the search results.
 * 
 * @author Stelios
 *
 */
public class SearchResultsPanel extends JPanel {
	
	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	private JLabel upperPanelInfo;
	private JLabel workingDocSnippets;
	
	private JLabel lowerPanelInfo;
	private JLabel otherSnippets;

	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public SearchResultsPanel() {
		super();
		this.setBackground(Color.LIGHT_GRAY);
		this.setLayout(new GridLayout(2,1));
		
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		this.add(upperPanel);
		
		this.upperPanelInfo = new JLabel("In the working document:");
		upperPanel.add(this.upperPanelInfo, BorderLayout.NORTH);
		
		this.workingDocSnippets = new JLabel();
		upperPanel.add(this.workingDocSnippets, BorderLayout.CENTER);
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BorderLayout());
		this.add(lowerPanel);
		
		this.lowerPanelInfo = new JLabel("In the other documents:");
		lowerPanel.add(this.lowerPanelInfo, BorderLayout.NORTH);
		
		this.otherSnippets = new JLabel();
		lowerPanel.add(this.otherSnippets, BorderLayout.CENTER);
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
		
		String resultsText = "";
		SearchResults results = Parameters.getSearchResults();
		
		// The first time this is called, the search
		// method hasn't been called yet, and hence
		// there are no results to display.
		if (results == null) return;

		if (!results.inWorkingDoc().isEmpty()) {
			for (SearchHit searchHit : results.inWorkingDoc()) {
				resultsText += searchHit.snippet();
				resultsText += "\n";
			}
			this.workingDocSnippets.setText(resultsText);
		} else {
			this.workingDocSnippets.setText("Your query did not return any results from the working document.");
		}
		
		resultsText = "";
		
		if (!results.elsewhere().isEmpty()) {
			for (SearchHit searchHit : results.elsewhere()) {
				resultsText += searchHit.snippet();
			}
			this.otherSnippets.setText(resultsText);
		} else {
			this.otherSnippets.setText("No results found in the other documents.");
		}
		
		this.repaint();
	}
	
	/**
	 * The paintComponent method.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D brush = (Graphics2D) g;
	}
}
