package gui;


/**
 * Keeps track of important variables that multiple classes
 * in the GUI will need to use so as to avoid passing a certain
 * class as a parameter to the classes who will need to share
 * some values.
 * 
 * @author Stelios
 *
 */
public class ParamHolder {
	
	/**
	 * The workspace.
	 */
	private static String workspace;
	
	/**
	 * Gets the workspace absolute path.
	 * 
	 * @return The workspace absolute path
	 */
	public static String getWorkspace() {
		
		return workspace;
	}
	
	/**
	 * Sets the workspace absolute path.
	 * 
	 * @param w The workspace absolute path
	 */
	public static void setWorkspace(String w) {
		workspace = w;
	}
}
