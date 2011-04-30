package core;

import java.io.*;
import java.util.*;
import org.dom4j.*;
import org.dom4j.io.*;
import search.*;

public class Document {

	private List<Page> _pages;
	private String _name;
	private String _pathname;
	
	public Document() {
		_pages = new LinkedList<Page>();
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public void setPathName(String pathname) {
		_pathname = pathname;
	}
	
	public void addPage(Page p) {
		_pages.add(p);
	}
	public String name() {
		return _name;
	}
	public List<Page> pages() {
		return _pages;
	}
	private String pathname() {
		return _pathname;
	}

	public void serialize() throws IOException {
		
		OutputFormat pretty = OutputFormat.createPrettyPrint();
		XMLWriter filewriter = new XMLWriter(new FileWriter(pathname()), pretty);
		
		try {
			org.dom4j.Document xmlDoc = DocumentHelper.createDocument();
			Element root = DocumentHelper.createElement("DOCUMENT");
			xmlDoc.setRootElement(root);
			
			root.addAttribute("name", name());
			
			Element pages = DocumentHelper.createElement("PAGES");
			root.add(pages);
			
			for (Page p : pages()) {
				p.serialize();
				Element pageEl = DocumentHelper.createElement("PAGE");
				Integer order = new Integer(p.order());
				pageEl.addAttribute("order", order.toString());
				pageEl.addAttribute("metafile", p.metafile());
				pages.add(pageEl);
			}
			
			filewriter.write(xmlDoc);
		} finally {
			filewriter.close();
		}
		
	}
	
	public List<SearchHit> search(String[] query) {
		LinkedList<SearchHit> hits = new LinkedList<SearchHit>();
		for (Page p : pages()) {
			hits.addAll(p.search(query));
		}
		return null;
	}
	
}