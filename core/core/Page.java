package core;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import ocr.OCRThread;
import ocr.ocrManager;
import org.dom4j.*;
import org.dom4j.io.*;
import search.*;
import vision.ConfigurationDictionary;
import vision.VisionManager;

public class Page {
	
	// used for searching the text of the page
	private static final int GREP_WINDOW = 10;
	
	// major attributes
	private PageText _text;
	private ConfigurationDictionary _config;
	private Corners _corners;
	
	// pathnames of files on disk
	private String _raw;
	private String _processed;
	private String _metafile;
	
	// reference to the containing document
	private Document _parentDoc;
	
	// the placement of the page relative to other
	// pages in the document
	private int _order;
	
	public Page(Document parent, int order) {
		_parentDoc = parent;
		_order = order;
		_config = new ConfigurationDictionary();
		_corners = new Corners();
		_text = new PageText();
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
	
	public BufferedImage getRawImgFromDisk() throws IOException {
		//return ImageIO.read(new File(raw()));
		return VisionManager.loadImage(raw());
	}
	
	public BufferedImage getProcessedImgFromDisk() throws IOException {
		//return ImageIO.read(new File(processed()));
		return VisionManager.loadImage(processed());
	}
	
    // sets corners and config file for the initial guesses of an imported document
    public void initGuesses() throws IOException {
    	System.out.println("Raw file: "+raw());

    	// read a buffered image from the disk
    	//BufferedImage buff = ImageIO.read(new File(raw()));
    	BufferedImage buff = VisionManager.loadImage(raw());
    	
    	// guess and set corners and configuration values of Page
    	setCorners(VisionManager.findCorners(buff));
    	setConfig(VisionManager.estimateConfigurationValues(buff));
    }
    
    // writes the current process image to workspace/processed
    public void writeProcessedImage() throws IOException {

    	// write out image as a TIFF file
    	VisionManager.writeTIFF(getRawImgFromDisk(), processed());
    	
    }
	
	public void setOcrResults() throws IOException {
		String[] fields = metafile().split("/");
		PageText text = ocrManager.getPageText(_raw, fields[fields.length-1]);
		synchronized (this) {
			_text = text;
			serialize();
		}
	}
	
	public void launchOcrThread() {
		OCRThread t = new OCRThread(this);
		t.start();
	}
	
	
	public void serialize() throws IOException {
		OutputFormat pretty = OutputFormat.createPrettyPrint();
		XMLWriter filewriter = new XMLWriter(new FileWriter(metafile()), pretty);
		
		try {
			org.dom4j.Document xmlDoc = DocumentHelper.createDocument();
			Element root = DocumentHelper.createElement("PAGE");
			xmlDoc.setRootElement(root);
			
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
	
	
	// DEBUGGING METHOD ONLY
	private void printWindowSet(Set<Term> windowSet) {
		System.out.print("set: "+_metafile+" ");
		for (Term t : windowSet) {
			System.out.print("["+t.word+","+t.pos+"] ");
		}
		System.out.println("");
	}

}
