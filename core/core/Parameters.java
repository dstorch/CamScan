package core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import search.SearchResults;
import westwidget.DocExplorerPanel;
import eastwidget.PageExplorerPanel;

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
	public static final String EXPORT_PATH = "managers/export/jpg2pdf.py";
	
	/**
	 * path of the python script to invoke for exportation
	 */
	public static final String STOP_WORDS = "managers/search/stopwords.txt";

	/**
	 * path of the python script to invoke for exportation
	 */
	public static final String SEARCHER_PATH = "managers/search/grepper.py";
	
	/**
	 * path of raw files
	 */
	public static final String RAW_DIRECTORY = "workspace/raw";
	
	/**
	 * path of processed image files
	 */
	public static final String PROCESSED_DIRECTORY = "workspace/processed";
	
	/**
	 * path of the document data
	 */
	public static final String DOC_DIRECTORY = "workspace/docs";
	
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
	
	
	/****************************************
	 * 
	 * CamScan appearance
	 * 
	 ****************************************/
	
	public static final String LOGO = "libraries/icons/CamscanLogo.png";
}
