package core;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import ocr.OCRThread;
import ocr.ocrManager;
import org.dom4j.*;
import org.dom4j.io.*;
import search.*;
import vision.ConfigurationDictionary;
import vision.VisionManager;

/*******************************************************************
 * Page
 *
 * In addition to Document, the most important class implementing
 * the Document model. An instance of this class keeps the metadata
 * for every page in every document of the library.
 * 
 * @author dstorch, micalle
 * 
 *******************************************************************/

@SuppressWarnings("rawtypes")
public class Page implements Comparable{

	/*******************************************************************
	 * 
	 * INSTANCE VARIABLES
	 * 
	 *******************************************************************/
	
	// used for searching the text of the page
	private static final int GREP_WINDOW = 10;

	// major attributes
	private PageText _text;
	private ConfigurationDictionary _config;
	private Corners _corners;

	// name of Page
	private String _name;

	// pathnames of files on disk
	private String _raw;
	private String _processed;
	private String _metafile;

	// reference to the containing document
	private Document _parentDoc;

	// the placement of the page relative to other
	// pages in the document
	private int _order;
	
	// whether the text found by OCR is
	// based on the current processed image
	private boolean _ocrUpToDate;

	/*******************************************************************
	 * 
	 * CONSTRUCTOR
	 * 
	 *******************************************************************/
	
	public Page(Document parent, int order, String name) {
		_parentDoc = parent;
		_order = order;
		_name = name;
		_config = new ConfigurationDictionary();
		_corners = new Corners();
		_text = new PageText();
	}
	
	/*******************************************************************
	 * 
	 * GETTERS
	 * 
	 *******************************************************************/

	public String name(){
		return _name;
	}

	public int order() {
		return _order;
	}
	public String raw() {
		return _raw;
	}
	public String processed() {
		return _processed;
	}
	public String metafile() {
		return _metafile;
	}
	public Corners corners() {
		return _corners;
	}
	public PageText pageText() {
		return _text;
	}
	public String fullText() {
		return _text.fullText();
	}
	public ConfigurationDictionary config() {
		return _config;
	}
	public Document getContainingDocument() {
		return _parentDoc;
	}
	
	/*******************************************************************
	 * 
	 * SETTERS
	 * 
	 *******************************************************************/
	
	public void setName(String name) {
		_name = name;
	}
	public void setOrder(int order) {
		_order = order;
	}
	public void setRawFile(String path) {
		_raw = path;
	}
	public void setProcessedFile(String path) {
		_processed = path;
	}
	public void setMetafile(String path) {
		_metafile = path;
	}
	public void setCorners(Corners c) {
		_corners = c;
	}
	public void setPageText(PageText pt) {
		_text = pt;
	}
	public void setConfig(ConfigurationDictionary c) {
		_config = c;
	}
	public void setContainingDocument(Document parent) {
		_parentDoc = parent;
	}
	public void ocrUpToDate() {
		_ocrUpToDate = true;
	}
	public void ocrNeedsRevision() {
		_ocrUpToDate = false;
	}
	public boolean getOcrUpToDate() {
		return _ocrUpToDate;
	}
	
	/*******************************************************************
	 * 
	 * PUBLIC METHODS
	 * 
	 *******************************************************************/

	/**
	 * This getter does not simply return a reference to an instance
	 * variable, but does image IO. Namely, it loads an image
	 * from disk.
	 * 
	 * @return the image as a BufferedImage
	 * @throws IOException
	 */
	public BufferedImage getRawImgFromDisk() throws IOException {
		return VisionManager.loadImage(raw());
	}

	/**
	 * Loads the processed image from disk and returns a reference
	 * to the resulting image object.
	 * 
	 * @return the BufferedImage object loaded from disk
	 * @throws IOException
	 */
	public BufferedImage getProcessedImgFromDisk() throws IOException {
		return VisionManager.loadImage(processed());
	}


	/**
	 * Does corner finding and initializes the corners of the page
	 * as the corners guessed by the VisionManager. Also estimates
	 * initial configuration values for the appearance of the image.
	 * 
	 * @throws IOException
	 */
	public void initGuesses() throws IOException {

		// read a buffered image from the disk
		BufferedImage buff = VisionManager.loadImage(raw());

		// guess and set corners and configuration values of Page
		setCorners(VisionManager.findCorners(buff));
		setConfig(VisionManager.estimateConfigurationValues(buff));
	}

	/**
	 * Renames this page.
	 * 
	 * @param newName - the new name, a String
	 * @throws IOException
	 */
	public void rename(String newName) throws IOException {

		// change the name of the metadata and processed files
		String newMet = metafile().substring(0, (metafile().length()-(name().length()+4)))+newName+".xml";
		String newPro = Parameters.PROCESSED_DIRECTORY+File.separator+newName+".tiff";

		File oldMeta = new File(metafile());
		File newMeta = new File(newMet);

                File oldProcessed = new File(processed());
                File newProcessed = new File(newPro);
		if (!oldMeta.renameTo(newMeta)) throw new IOException("Could not rename Page (metadata file)!");
                if (!oldProcessed.renameTo(newProcessed)) throw new IOException("Could not rename Page (processed file)!");

		// reset instance variables
		setName(newName);
		setMetafile(newMet);
		setProcessedFile(newPro);
		
                serialize();
                _parentDoc.serialize();
	}

	/**
	 * Writes the processed image to the disk. Called whenever the
	 * processed image has been changed, and the copy on disk needs
	 * to be updated.
	 * 
	 * @throws IOException
	 */
	public void writeProcessedImage() throws IOException {
		VisionManager.outputToFile(getRawImgFromDisk(), processed(), this.corners(), this.config());
	}
	
	/**
	 * Writes the processed image as a TIFF file. This is necessary
	 * in order for the image to be compatible with Tesseract,
	 * and is therefore used by the OCR manager.
	 * 
	 * @throws IOException
	 */
	public void writeProcessedImageTIFF() throws IOException {
		BufferedImage rerendered = VisionManager.rerenderImage(getRawImgFromDisk(), this.corners(), this.config());
		VisionManager.writeTIFF(rerendered, processed());
	}

	/**
	 * Performs OCR on this page object, and sets the result
	 * as its PageText object.
	 * 
	 * @throws IOException
	 */
	public void setOcrResults() throws IOException {
		String[] fields = metafile().split(SystemConfiguration.PATH_REGEX);
		PageText text = ocrManager.getPageText(_processed, fields[fields.length-1]);
		synchronized (this) {
			_text = text;
			serialize();
		}
	}

	/**
	 * Launches a thread which calls setOcrResults in order
	 * to update the OCR text.
	 */
	public void launchOcrThread() {
		OCRThread t = new OCRThread(this);
		t.start();
	}


	/**
	 * Writes the instance variables of this Page to disk,
	 * according to the CamScan XML spec.
	 * 
	 * @throws IOException
	 */
	public void serialize() throws IOException {
		OutputFormat pretty = OutputFormat.createPrettyPrint();
		XMLWriter filewriter = new XMLWriter(new FileWriter(metafile()), pretty);

		try {
			org.dom4j.Document xmlDoc = DocumentHelper.createDocument();
			Element root = DocumentHelper.createElement("PAGE");
			xmlDoc.setRootElement(root);

			root.addAttribute("name", name());


			Element image = DocumentHelper.createElement("IMG");
			image.addAttribute("path", raw());
			image.addAttribute("processed", processed());
			root.add(image);

			corners().serialize(root);
			pageText().serialize(root);
			config().serialize(root);

			filewriter.write(xmlDoc);
		} finally {
			filewriter.close();
		}

	}

	/**
	 * The function by which the searchs results within a page
	 * are obtained. Called from the Document class's search
	 * function.
	 * 
	 * @param query - a String, the search query
	 * @param searcher - the Searcher object performing the search
	 * @return a list of SearchHit objects within this page.
	 */
	public List<SearchHit> search(Set<Term> query, Searcher searcher) {
		
		LinkedList<SearchHit> hits = new LinkedList<SearchHit>();
		String fullText = fullText();
		
		List<Term> fullTextTerms = searcher.sanitize(fullText);

		// build the initial "grepping window"
		HashSet<Term> windowSet = new HashSet<Term>();
		for (int i = 0; i < GREP_WINDOW; i++) {
			if (i < fullTextTerms.size()) {
				windowSet.add(fullTextTerms.get(i));
			}
		}

		boolean resultInWindow = false;
		float scoreInWindow = 0;
		SearchHit lastHit = null;
		for (int i = GREP_WINDOW; i < fullTextTerms.size(); i++) {
			// increment the "grepping window"
			windowSet.remove(fullTextTerms.get(i - GREP_WINDOW));
			windowSet.add(fullTextTerms.get(i));

			// determine the amount of intersection between the grepping
			// window and the query set
			int score = 0;
			int position = 0;
			for (Term t : windowSet) {
				if (query.contains(t)) {
					score++;
					position = t.pos;
				}
			}

			// if there is an intersection, find the snippet and
			// create a new search hit
			if (!resultInWindow) {
				if (score > 0) {
					resultInWindow = true;
					scoreInWindow = score;
					String snippet = getSearchSnippet(windowSet, fullText, position);
					lastHit = SearchHit.Factory.create(this, snippet, score);
				}
			} else if (resultInWindow) {
				if (score > scoreInWindow) {
					String snippet = getSearchSnippet(windowSet, fullText, position);
					lastHit = SearchHit.Factory.create(this, snippet, score);
					scoreInWindow = score;
				} else if (score < scoreInWindow) {
					scoreInWindow = 0;
					resultInWindow = false;
					hits.add(lastHit);
				}
			}
		}

		// if you exit the loop and there is still a hit, then add it
		if (resultInWindow) hits.add(lastHit);
		
		return hits;
	}

	


	/**
	 *  deletes image file in the workspace/raw directory
	 */
	public void deleteRawFile(){
		File raw = new File(raw());
		if(!raw.delete()) System.out.println("RAW file not deleted!!");
	}

	/**
	 *  deletes image file in the workspace/processed directory
	 */
	public void deleteProcessedFile(){
		File processed = new File(processed());
		if(!processed.delete()) System.out.println("PROCESSED file not deleted!!");
	}

	/**
	 * deletes metadata file
	 */
	public void deleteMetadataFile(){
		File meta = new File(metafile());
		System.out.println("Metafile: "+ metafile());
		if(!meta.delete()) System.out.println("******METADTA file not deleted!!");
	}


	public int compareTo(Object t) {
		if(order()< ((Page) t).order()) return -1;
		else if (order() == ((Page) t).order()) return 0;
		else return 1;
	}

	public boolean equals(Page p){
		return (name() == p.name());
	}
	
	/*******************************************************************
	 * 
	 * PRIVATE METHODS
	 * 
	 *******************************************************************/


	/**
	 * Given a set of terms matching the search query, extracts an appropriate
	 * snippet of text, which the terms that matched the query approximately centered.
	 * 
	 * @param grepWindow - the set of terms that matched the query
	 * @param fullText - the complete OCR text of the page
	 * @param midpoint - an integer giving the term position of the term in the middle
	 * 	of the "grep window"
	 * 
	 * @return a String giving a snippet of text that matched the search
	 */
	private String getSearchSnippet(Set<Term> grepWindow, String fullText, int midPosition) {

		// collapse the full text into an array of strings
		String[] fullTextArr = 	fullText.split("[^a-zA-Z0-9.,/-]+");

		// get the minimum position out of the terms
		int minPosition = Integer.MAX_VALUE;
		for (Term t : grepWindow) {
			if (t.pos < minPosition) {
				minPosition = t.pos;
			}
		}

		// get the snippet
		LinkedList<String> snippetList = new LinkedList<String>();
		for (int i = (midPosition - GREP_WINDOW); i < (midPosition + GREP_WINDOW); i++) {
			if (i < fullTextArr.length && i > 0) snippetList.add(fullTextArr[i]);
		}

		// get the snippet from the list
		String snippet = "";
		for (String s : snippetList) {
			snippet += s + " ";
		}

		return snippet.trim();
	}

}
