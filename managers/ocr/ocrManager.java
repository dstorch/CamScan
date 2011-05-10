package ocr;

import java.io.*;
import java.awt.Point;
import core.PageText;
import core.Position;

public class ocrManager {

	// absolute path to the tesseract executable
	public static String TESS_PATH = "C:\\Program Files (x86)\\Tesseract-OCR\\tesseract.exe";

	// path to the python script for processing the tesseract output
	private final static String EXTRACTBB_PATH = "managers\\ocr\\extractbb.py";
	
	// pathnames used for tesseract temp file and config file
	private final static String CONFIG_FILE = "libraries\\tesseract\\config.txt";
	private final static String OUT_PATH = "workspace\\temp";
	
	
	/**
	 * 
	 * @param imageFile
	 * @return
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static PageText getPageText(String imageFile, String outname) throws IOException{
		
		// delete the outfile if it exists
		File outFile = new File(OUT_PATH+"/"+outname+".html");
		if (outFile.exists()) outFile.delete();
		
		// run tesseract
		String arguments = TESS_PATH+" "+imageFile+" "+OUT_PATH+"/"+outname+" " + CONFIG_FILE;
		Runtime.getRuntime().exec(arguments);
		
		// block until the file is created
		while (!outFile.canRead()) {}

		// now run python script for extracting data
		String command = "python "+EXTRACTBB_PATH+" "+OUT_PATH+"/"+outname+".html";
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
	 * @param args
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException{
                System.out.println("Starting Test...");

		PageText pt = ocrManager.getPageText("../sample_page.tiff", "sample_page");
		
		System.out.println(pt.fullText());

		for (Position p : pt.positions()) {
			System.out.println(p.xmin()+" "+p.ymin()+" : "+p.word());
		}

	} 
}
