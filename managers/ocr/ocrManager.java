package ocr;

import java.io.*;
import java.awt.Point;
import core.PageText;
import core.Position;
import core.SystemConfiguration;

/*******************************************************************
 * ocrManager
 *
 * The Java interface for executing Tesseract and reading in the
 * OCR results.
 * 
 * @author mmicalle
 * 
 *******************************************************************/

public class ocrManager {

	// path to the python script for processing the tesseract output
	private final static String EXTRACTBB_PATH = "managers" + File.separator + "ocr" + File.separator + "extractbb.py";

	// pathnames used for tesseract temp file and config file
	private final static String CONFIG_FILE = "libraries" + File.separator + "tesseract" + File.separator + "config.txt";
	private final static String OUT_PATH = "workspace" + File.separator + "temp";


	/**
	 * 
	 * @param imageFile
	 * @return
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static PageText getPageText(String imageFile, String outname) throws IOException{

		// delete the outfile if it exists
		File outFile = new File(OUT_PATH+File.separator+outname+".html");
		if (outFile.exists()) outFile.delete();

		// run tesseract
		String arguments = SystemConfiguration.TESS_PATH+" "+imageFile+" "+OUT_PATH+File.separator+outname+" " + CONFIG_FILE;
		System.out.println(arguments);
		Runtime.getRuntime().exec(arguments);

		// block until the file is created
		while (!outFile.canRead()) {}

		// now run python script for extracting data
		String command = SystemConfiguration.PYTHON_PATH+" "+EXTRACTBB_PATH+" "
		+OUT_PATH+File.separator+outname+".html";
		Process process = Runtime.getRuntime().exec(command);

		// use a reader to read text from the standard output stream of the processs
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String fullText = "";

		String line = "";
		PageText pt = new PageText();
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split(" ");
			fullText += fields[0] + " ";

			if (fields[1].equals("bbox")) {
				int minx = Integer.parseInt(fields[2]);
				int miny = Integer.parseInt(fields[3]);
				int maxx = Integer.parseInt(fields[4]);
				int maxy = Integer.parseInt(fields[5]);

				Point min = new Point(minx, miny);
				Point max = new Point(maxx, maxy);

				// add the position to the PageText object
				Position position = new Position(min, max, fields[0]);

				// hold the mutex when the PageText object is updated
				synchronized (pt) {
					pt.addPosition(position);
				}
			}

		}

		// set the full text attribute of the PageText,
		// making sure that it is thread safe
		synchronized (pt) {
			pt.setFullText(fullText.trim());
		}

		return pt;
	}

	/**
	 * Main method---useful for independently testing
	 * the OCR module!
	 * 
	 * This main method is executed from the test_ocr.sh
	 * shell script as part of the unit test suite.
	 * 
	 * @param args -
	 * 		args[0] should be the path to tesseract
	 * 		args[1] should be the path to python
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{

		SystemConfiguration.TESS_PATH = args[0];
		SystemConfiguration.PYTHON_PATH = args[1];

		// print an opening test message
		System.out.println("OCR UNIT TESTS\n");
		
		// set up a series of tests
		String[] tests = {"sample_page.tiff", "mexican_war_text.tiff", "sample2.tiff"};
		String[] outNames = {"sample_page", "mexican_war_text", "sample2"};

		// keep track of whether the tests pass
		boolean passed = true;
		
		// loop through the test cases
		for (int i = 0; i < tests.length; i++) {

			System.out.println("TEST PAGE "+(i+1));
			System.out.println("===");
			
			// run the ocr
			String path = "tests" + File.separator + "ocr" + File.separator + tests[i];
			PageText pt = ocrManager.getPageText(path, outNames[i]);

			// print the extracted text
			System.out.println(pt.fullText());
			for (Position p : pt.positions()) {
				System.out.println(p.xmin()+" "+p.ymin()+" "+p.xmax()+" "+p.ymax()+" : "+p.word());
			}
			
			// add newlines after each test
			System.out.print("\n\n");
		}
		
		if (passed) System.out.println("All OCR tests passed!");
	} 
}
