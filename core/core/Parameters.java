package core;

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
	
	/****************************************
	 * 
	 * Mutable Parameters
	 * 
	 ****************************************/
	
	/**
	 * Reference to the global Core Manager instance.
	 */
	private static CoreManager coreManager;
	
	/****************************************
	 * 
	 * Getters for the Immutable Parameters
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
	
	/****************************************
	 * 
	 * Setters for the Immutable Parameters
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
}
