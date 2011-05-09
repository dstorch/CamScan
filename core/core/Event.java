package core;

import search.SearchResults;

/**
 * Modes an event that the user makes. The most
 * recent of them are stored in a data structure,
 * so that users can revert their changes using
 * the "Back" and "Next" buttons.
 * 
 * @author Stelios
 *
 */
public class Event {
	
	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * Keeps track of whether CamScan is in view mode.
	 * If not, it will be in edit mode.
	 */
	private Mode mode;
	
	/**
	 * The working document when the event is recorded.
	 */
	private Document document;
	
	/**
	 * The working page when the event is recorded.
	 */
	private Page page;
	
	/**
	 * The search results object; null if this is
	 * not a search results page
	 */
	private SearchResults searchResults;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 * 
	 * @param isViewMode True if CamScan is in edit mode
	 * @param document The working document
	 * @param page The working page
	 */
	public Event(Mode mode, Document document, Page page, SearchResults results) {
		this.mode = mode;
		this.document = document;
		this.page = page;
		this.searchResults = results;
	}
	
	/****************************************
	 * 
	 * Getters
	 * 
	 ****************************************/
	
	/**
	 * Get the mode for this event object.
	 * 
	 * @return the Mode enum determining
	 * the view being displayed
	 */
	public Mode getMode() {
		return this.mode;
	}
	
	/**
	 * Returns the working document.
	 * 
	 * @return The working document
	 */
	public Document getDocument() {
		return this.document;
	}

	/**
	 * Returns the working page.
	 * 
	 * @return The working page
	 */
	public Page getPage() {
		return this.page;
	}
	
	/**
	 * @return the SearchResults object, null
	 * if this is not a search results page
	 * @return
	 */
	public SearchResults getSearchResults() {
		return this.searchResults;
	}
	
	/**
	 * Returns this object as a String
	 */
	@Override
	public String toString() {
		String output = "Event:\n";
		output += "\t"+this.mode+"\n";
		output += "\t"+this.page.metafile()+"\n";
		output += "\t"+this.document.name()+"\n";
		return output;
	}
}
