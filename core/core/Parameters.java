package core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import search.SearchResults;

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
	 * The regular expression used to find image files
	 */
	public static final String IMAGE_REGEX = ".png$|.jpg$|.tif$|.jpeg$";
	
	/**
	 * Max number of search hits to report in the working document
	 */
	public static final int RESULTS_INDOC = 4;
	
	/**
	 * Max number of search hits to report in all other documents
	 */
	public static final int RESULTS_ELSEWHERE = 10;
	
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
	 * The current page as a buffered image.
	 */
	private static BufferedImage currPageImg;
	
	/**
	 * The Search Results from the latest search.
	 */
	private static SearchResults searchResults;
	
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
	 * Returns the current page buffered image.
	 * 
	 * @return The current page buffered image
	 */
	public static BufferedImage getCurrPageImg() {
		return currPageImg;
	}
	
	/**
	 * Returns the search results from the latest search.
	 * 
	 * @return The search results from the latest search
	 */
	public static SearchResults getSearchResults() {
		return searchResults;
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
	 * Given the path to the raw page image, it
	 * sets the current page image as a buffered
	 * image.
	 * 
	 * @param raw The path to the raw image.
	 */
	public static void setCurrPageImg(String raw) {
		try {
			currPageImg = ImageIO.read(new File(raw));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the search results from the latest search.
	 * 
	 * @param sr The search results to set
	 */
	public static void setSearchResults(SearchResults sr) {
		searchResults = sr;
	}
}
