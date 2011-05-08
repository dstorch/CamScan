package core;

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
	private boolean isViewMode;
	
	/**
	 * The working document when the event is recorded.
	 */
	private Document document;
	
	/**
	 * The working page when the event is recorded.
	 */
	private Page page;
	
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
	public Event(boolean isViewMode, Document document, Page page) {
		this.isViewMode = isViewMode;
		this.document = document;
		this.page = page;
	}
	
	/****************************************
	 * 
	 * Getters
	 * 
	 ****************************************/
	
	/**
	 * Returns true if the event was recording
	 * when CamScan was in view mode.
	 * 
	 * @return True if the event was recording
	 * when CamScan was in view mode.
	 */
	public boolean isViewMode() {
		return this.isViewMode;
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
}
