package centralwidget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
	
	private JLabel snippets;

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
		
		this.snippets = new JLabel();
		this.add(this.snippets);
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
		
		String resultsText = null;
		SearchResults results = Parameters.getSearchResults();
		
		// The first time this is called, the search
		// method hasn't been called yet, and hence
		// there are no results to display.
		if (results == null) return;

		if (!results.inWorkingDoc().isEmpty()) {
			for (SearchHit searchHit : results.inWorkingDoc()) {
				resultsText += searchHit.snippet();
			}
			this.snippets.setText(resultsText);
		} else {
			this.snippets.setText("Your query did not return any results");
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
