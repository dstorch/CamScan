package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

/*******************************************************************
 * SystemConfiguraion
 *
 * Keeps system-specific configuration, including the pathnames to
 * CamScan dependencies. The static variables inside this class should
 * be set on startup based on the contents of .camscan_startup.xml.
 * If there is no .camscan_startup.xml found, then autoconfiguration
 * is attempted.
 * 
 * @author dstorch
 * 
 *******************************************************************/

public class SystemConfiguration {
	
	/**
	 * Whether to use openCV or to defer to Java's ImageIO
	 * functionality. Useful for development, or when
	 * working on a machine without openCV installed.
	 */
	public static boolean OPENCV_ENABLED = false;
	
	/**
	 * Whether tesseract and OCR functionality is
	 * enabled.
	 */
	public static boolean OCR_ENABLED = true;
	
	/**
	 * Whether python, reportlab, and PIL are installed--
	 * and therefore whether image exporting is enabled.
	 */
	public static boolean EXPORT_ENABLED = true;
	
	/**
	 * The regular expression to use for splitting up pathnames.
	 */
	public static String PATH_REGEX = "/";
	
	/**
	 * The path to tesseract executable.
	 */
	public static String TESS_PATH = "";
	
	/**
	 * The path to python. Generally python will be added to the
	 * system PATH, but include the whole path to be safe.
	 */
	public static String PYTHON_PATH = "";

	/**
	 * This method attemps to autoconfigure by determining the
	 * operating system and looking for the installation of
	 * python and tesseract.
	 */
	public static void autoconfigure() {
		String osName = System.getProperty("os.name");
		if (osName.equals("Mac OS X")) autoconfigureMac();
		else {
			JOptionPane.showMessageDialog(Parameters.getFrame(),
					"Automatic system configuration failed!",
					"Startup Warning", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * Attempt an automatic system configuration for
	 * Mac OS X. Gets path names via the "which" utility.
	 */
	public static void autoconfigureMac() {
		
		// set the regular expression for splitting pathnames
		PATH_REGEX = "/";
		
		// set up the tesseract path
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(Parameters.AUTOCONFIGURE_MAC);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			// set tesseract path
			String result = reader.readLine();
			result = result.trim();
			TESS_PATH = result;
			OCR_ENABLED = true;
			
			result = reader.readLine();
			result = result.trim();
			System.out.println(result);
			PYTHON_PATH = result;
			EXPORT_ENABLED = true;
		}
		
		// problem finding path: alert the user
		catch (IOException e) {
			JOptionPane.showMessageDialog(Parameters.getFrame(),
					"Tesseract could not be located! Install Tesseract and set your Tesseract path in Preferences.",
					"Startup Warning", JOptionPane.WARNING_MESSAGE);
		}
		
		// close the reader if necessary
		finally {
			try {
				reader.close();
			} catch (IOException e) {
			} catch (NullPointerException e) {}
			
		}
	}

}
