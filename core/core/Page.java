package core;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import search.*;

public class Page {
	
	private static final int GREP_WINDOW = 10;
	
	private PageText _text;
	private Config _config;
	private Corners _corners;
	private String _raw;
	private String _processed;
	private String _metafile;
	private int _order;
	
	public Page(int order) {
		_order = order;
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
	public Config config() {
		return _config;
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
	public void setConfig(Config c) {
		_config = c;
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

	public List<SearchHit> search(String[] query) {
		LinkedList<SearchHit> hits = new LinkedList<SearchHit>();
		String fullText = fullText();
		
		String[] fullTextTerms = SearchManager.sanitizeFullText(fullText);
		
		HashSet<String> windowSet = new HashSet<String>();
		for (int i = 0; i < GREP_WINDOW; i++) {
			windowSet.add(fullTextTerms[i]);
		}
		
		for (int i = GREP_WINDOW; i < fullTextTerms.length; i++) {
			
		}
		
		return null;
	}

}
