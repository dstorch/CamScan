package core;

import java.io.File;

import javax.swing.JFrame;

import search.SearchResults;
import westwidget.DocExplorerPanel;
import eastwidget.PageExplorerPanel;
import gui.MainPanel;

/*******************************************************************
 * Parameters
 *
 * Stores some global constants. Also stores a static global instance
 * of the CoreManager so that the core of the system can always
 * be accessed from the GUI.
 * 
 * @author dstorch, stelios
 * 
 *******************************************************************/

public class Parameters {

	
	/****************************************
	 * 
	 * Immutable Parameters
	 * 
	 ****************************************/
	
	/**
	 * name of the startup file
	 */
	public static final String STARTUP_FILE =".camscan_startup.xml";
	
	/**
	 * path of the python script to invoke for exportation
	 */
	public static final String EXPORT_PATH = "managers" + File.separator + "export" + File.separator + "jpg2pdf.py";
	
	/**
	 * path of the python script to invoke for exportation
	 */
	public static final String STOP_WORDS = "managers" + File.separator + "search" + File.separator + "stopwords.txt";

	/**
	 * path of the python script to invoke for exportation
	 */
	public static final String SEARCHER_PATH = "managers" + File.separator + "search" + File.separator + "grepper.py";
	
	/**
	 * Path of the Mac OS shell script which attempts to determine
	 * installation locations.
	 */
	public static final String AUTOCONFIGURE_MAC = "core/core/autoConfigureMac.sh";
	
	/**
	 * path of raw files
	 */
	public static final String RAW_DIRECTORY = "workspace" + File.separator + "raw";
	
	/**
	 * path of processed image files
	 */
	public static final String PROCESSED_DIRECTORY = "workspace" + File.separator + "processed";
	
	/**
	 * path of the document data
	 */
	public static final String DOC_DIRECTORY = "workspace" + File.separator + "docs";
	
	/**
	 * Max number of search hits to report in the working document
	 */
	public static final int RESULTS_INDOC = 4;
	
	/**
	 * Max number of search hits to report in all other documents
	 */
	public static final int RESULTS_ELSEWHERE = 10;
	
	/**
	 * Arrays defining file extension filters
	 */
	public static final String[] imgExtensions = {".jpeg", ".jpg", ".tiff", ".tif", ".png"};
	public static final String[] txtExtensions = {".txt", ".text"};
	public static final String[] pdfExtensions = {".pdf"};
	
	/****************************************
	 * 
	 * Mutable Parameters
	 * 
	 ****************************************/
	
	/**
	 * Reference to the global Core Manager instance.
	 */
	private static CoreManager coreManager;
	
	/**
	 * Reference to the global JFrame
	 */
	private static JFrame app;
	
	/**
	 * The Search Results from the latest search.
	 */
	private static SearchResults searchResults;

	/**
	 * Reference to the document explorer panel
	 */
	private static DocExplorerPanel docExpPanel;
	
	/**
	 * Reference to the page explorer panel
	 */
	private static PageExplorerPanel pageExpPanel;
	
	/**
	 * Reference to the main panel
	 */
	private static MainPanel mainPanel;

	/**
	 * Keeps track of whether the app is in edit mode.
	 */
	private static boolean isInEditMode;
	
	/****************************************
	 * 
	 * Getters for the Mutable Parameters
	 * 
	 ****************************************/
	
	/**
	 * Returns the Core Manager instance.
	 * 
	 * @return The Core Manager instance
	 */
	public static CoreManager getCoreManager() {
		return coreManager;
	}
	
	/**
	 * Use for access to the outermost Frame of the GUI
	 * 
	 * @return the global app frame
	 */
	public static JFrame getFrame() {
		return app;
	}
	
	/**
	 * Returns the search results from the latest search.
	 * 
	 * @return The search results from the latest search
	 */
	public static SearchResults getSearchResults() {
		return searchResults;
	}
	
	/**
	 * Returns the reference to the doc explorer panel.
	 * 
	 * @return The reference to the doc explorer panel
	 */
	public static DocExplorerPanel getDocExpPanel() {
		return docExpPanel;
	}
	
	/**
	 * Returns the reference to the page explorer panel.
	 * 
	 * @return The reference to the page explorer panel
	 */
	public static PageExplorerPanel getPageExpPanel() {
		return pageExpPanel;
	}
	
	/**
	 * Returns the reference to the main panel.
	 * 
	 * @return The reference to the main panel
	 */
	public static MainPanel getMainPanel() {
		return mainPanel;
	}
	
	/**
	 * Returns whether the app is in edit mode.
	 * 
	 * @return True if it is in edit mode; false if
	 * otherwise
	 */
	public static boolean isInEditMode() {
		return isInEditMode;
	}
	
	/****************************************
	 * 
	 * Setters for the Mutable Parameters
	 * 
	 ****************************************/
	
	/**
	 * Sets the Core Manager instance.
	 * Should only be called once.
	 * 
	 * @param cm The Core Manager instance to set
	 */
	public static void setCoreManager(CoreManager cm) {
		if (coreManager == null)
			coreManager = cm;
	}
	
	/**
	 * Sets the global JFrame instance.
	 * Should only be called once.
	 * 
	 * @param frame The JFrame instance to set
	 */
	public static void setApp(JFrame frame) {
		if (app == null)
			app = frame;
	}
	
	/**
	 * Sets the search results from the latest search.
	 * 
	 * @param sr The search results to set
	 */
	public static void setSearchResults(SearchResults sr) {
		searchResults = sr;
	}
	
	/**
	 * Sets the document explorer panel reference.
	 * 
	 * @param p The doc explorer panel references
	 */
	public static void setDocExplorerPanel(DocExplorerPanel p) {
		
		if (docExpPanel == null) 
			docExpPanel = p;
	}
	
	/**
	 * Sets the page explorer panel reference.
	 * 
	 * @param p The page explorer panel references
	 */
	public static void setPageExplorerPanel(PageExplorerPanel p) {
		
		if (pageExpPanel == null) 
			pageExpPanel = p;
	}
	
	/**
	 * Sets the main panel reference.
	 * 
	 * @param p The main panel references
	 */
	public static void setMainPanel(MainPanel p) {
		
		if (mainPanel == null) 
			mainPanel = p;
	}
	
	
	/**
	 * Sets whether the app is in edit mode.
	 * 
	 * @param b The boolean to set
	 */
	public static void setIsInEditMode(boolean b) {
		isInEditMode = b;
	}
	
	
	/****************************************
	 * 
	 * CamScan appearance
	 * 
	 ****************************************/
	
	public static final String LOGO = "libraries/icons/CamscanLogo.png";
	
	public static final String EDIT = "libraries/icons/pencil.png";
	
	public static final String VIEW = "libraries/icons/display.png";
	
	public static final String ZOOM_IN = "libraries/icons/zoomin.png";
	
	public static final String ZOOM_OUT = "libraries/icons/zoomout.png";
	
	public static final String DOWN_ARROW = "libraries/icons/downarrow.png";
	
	public static final String UP_ARROW = "libraries/icons/uparrow.jpg";
}
