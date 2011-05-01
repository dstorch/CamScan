package ocr;

import java.io.*;
import java.awt.Point;
import java.util.Iterator;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.w3c.tidy.Tidy;
import core.PageText;
import core.Position;

public class ocrManager {

	// absolute path to the tesseract executable
	public static String TESS_PATH = "/usr/local/bin/tesseract";
	
	private final static String CONFIG_FILE = "libraries/tesseract/config.txt";
	private final static String OUT_TXT_PATH = "libraries/tesseract/temp/out.txt";
	private final static String OUT_HTML_PATH = "libraries/tesseract/temp/out.html";
	private final static String OUT_XML_PATH = "libraries/tesseract/temp/out.xml";
	private final static String TEMP = "libraries/tesseract/temp/out2.xml";
	private final static String TEMP2 = "libraries/tesseract/temp/out3.xml";
	private final static String OUT_PATH = "libraries/tesseract/temp/out";


	/**
	 *
	 * @param image_file
	 * @return the text outputted by tesseract on the image_file
	 */
	private static String getOCRText(String image_file){

		// text of image_file
		String text = "";

		// string to execute tesseract command
		String arguments = TESS_PATH+" "+image_file+" "+OUT_PATH;
		System.out.println(arguments);

		try {
			Process p = Runtime.getRuntime().exec(arguments);

			// file handle so we can delete at the end of the process
			File out_file = new File(OUT_TXT_PATH);
			while(!out_file.canRead()){
				//System.err.println("file does not exit");
			}

			// read from out text file
			BufferedReader in = new BufferedReader(new FileReader(out_file));
			String line = "";

			while((line = in.readLine()) != null){
				text += line + "\n";
			}

			in.close();

			// delete txt file
			if(!out_file.delete()) System.err.println("File not deleted");

		} catch (IOException ex) {
			System.out.println("IOException!");
			ex.printStackTrace();

		}

		return text;
	}

	/**
	 *
	 * @param input_file
	 * @return
	 * @throws DocumentException
	 */
	private static PageText setPageText(String image_file, PageText pt){

		// string to execute tesseract command
		String arguments = TESS_PATH+" "+image_file+" "+OUT_PATH + " " + CONFIG_FILE;

		try {
			Process p = Runtime.getRuntime().exec(arguments);

			// file handle so we can delete at the end of the process
			File outTXT = new File(OUT_HTML_PATH);

			// create xml file to write to
			File outXMLFile = new File(OUT_XML_PATH);
			if (!outXMLFile.exists()) outXMLFile.createNewFile();

			// blocking statement to ensure tesseract has created html file
			while(!outTXT.canRead()){
				//System.err.println("file does not exist");
			}

			FileInputStream Fin = new FileInputStream(OUT_HTML_PATH);
			FileOutputStream Fout = new FileOutputStream(OUT_XML_PATH);

			// convert to xml file
			Tidy T = new Tidy();
			T.setXmlOut(true);
			T.parse(Fin, Fout);

			// delete first line of xml file so parser won't throw error **NEED TO RESOLVE THIS**
			File temp = new File(TEMP);

			BufferedReader br = new BufferedReader(new FileReader(outXMLFile));
			PrintWriter pw = new PrintWriter(new FileWriter(temp));

			String line = null;

			//Read from the original file and write to the new
			//unless content matches data to be removed.
			br.readLine(); 
			br.readLine(); 
			while ((line = br.readLine()) != null) {
				pw.println(line);
				pw.flush();
			}
			pw.close();
			br.close();

			//Delete the original file
			if (!outXMLFile.delete()) {
				System.out.println("Could not delete file");
			}
			
			System.out.println("python managers/ocr/cleanXML.py "+TEMP+" "+TEMP2);
			Runtime.getRuntime().exec("python managers/ocr/cleanXML.py "+TEMP+" "+TEMP2);
			
			File tempFile = new File(TEMP2);
			while(!tempFile.canRead()){
				System.err.println("file does not exist");
			}
			
			SAXReader reader = new SAXReader();
			org.dom4j.Document document = reader.read(new FileReader(TEMP2));
			Element root = document.getRootElement();
			PageText populatedPT = traverseTree(root, pt);

			// delete files
			if(!outTXT.delete()) System.err.println("File not deleted");
			if(!temp.delete()) System.err.println("File not deleted");

			return populatedPT;

		} catch (IOException ex) {
			System.out.println("IOException!");
			ex.printStackTrace();

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	} 

	/**
	 * Traverses XML tree and populates inputted PageText object
	 *
	 * @param element
	 * @param text
	 * @return PageText file
	 */
	private static PageText traverseTree(Element root, PageText text){
		Element body = root.element("body");
		for(Iterator g = body.elementIterator("div"); g.hasNext();){
			Element page = (Element) g.next();
			for(Iterator h = page.elementIterator("div"); h.hasNext();){
				Element block = (Element) h.next();
				for (Iterator i = block.elementIterator("p"); i.hasNext();) {
					Element paragraphs = (Element) i.next();
					for (Iterator j = paragraphs.elementIterator("span"); j.hasNext();) {
						Element line = (Element) j.next();
						for (Iterator k = line.elementIterator("span"); k.hasNext();){
							Element word = (Element) k.next();
							Attribute bbox = word.attribute("title");
							String wordText = word.getStringValue().trim();
							//System.out.println("Word: "+ wordText);
							Position pos = parseBBox(bbox.getStringValue(), wordText);
							text.addPosition(pos);
						}
					}
				}
			}
		}
		

		return text;
	}

	/**
	 * Parses string containing bounding box and creates Position object. Returns null if not bbox string
	 * @param bbox
	 * @param word
	 * @return 
	 */
	private static Position parseBBox(String bbox, String word){
		String[] bb = bbox.split(" ");
		if(bb[0].equals("bbox")){
			int minx = Integer.parseInt(bb[1]);
			int miny = Integer.parseInt(bb[2]);
			int maxx = Integer.parseInt(bb[3]);
			int maxy = Integer.parseInt(bb[4]);

			//System.out.println("Bounding Box: ("+minx+", "+miny+", "+maxx+", "+maxy+")");

			Point min = new Point(minx, miny);
			Point max = new Point(maxx, maxy);

			return new Position(min, max, word);
		}else return null;
	}


	/**
	 * 
	 * @param imageFile
	 * @return
	 * @throws DocumentException 
	 */

	public static PageText getPageText(String imageFile){
		String text = getOCRText(imageFile);
		PageText pt = new PageText(text);
		return setPageText(imageFile, pt);
	}

	public static void main(String[] args) {

		PageText pt = ocrManager.getPageText("../sample_page.tiff");
		
		System.out.println(pt.fullText());
		
		for (Position p : pt.positions()) {
			System.out.println(p.xmin()+" "+p.ymin()+" : "+p.word());
		}
		
	} 
}
