package core;

import java.io.*;
import java.util.*;
import org.dom4j.*;
import org.dom4j.io.*;
import search.*;
import vision.*;
import export.*;

@SuppressWarnings("rawtypes")
public class CoreManager {

	private Exporter _exporter;
	private Searcher _searcher;
	private VisionManager _vision;
	
	private Document _workingDocument;
	private XMLReader _xmlReader;
	private List<Document> _allDocuments;
	
	public CoreManager() throws DocumentException, IOException {
		_xmlReader = new XMLReader();
		_exporter = Exporter.Factory.create();
		_searcher = Searcher.Factory.create();
		_vision = new VisionManager();
		_allDocuments = new LinkedList<Document>();
		startup();
	}
	
	public List<Document> getDocuments() {
		return _allDocuments;
	}
	
	// called from the constructor when the application launches
	public void startup() throws FileNotFoundException, DocumentException {
		SAXReader reader = new SAXReader();
		org.dom4j.Document document = reader.read(new FileReader(Parameters.STARTUP_FILE));
		Element root = document.getRootElement();
		
		for (Iterator i = root.elementIterator("WORKINGDOC"); i.hasNext();) {
			Element workingdoc = (Element) i.next();
			String workingStr = workingdoc.attribute("value").getStringValue();
			setWorkingDocument(workingStr);
		}
		
		for (Iterator i = root.elementIterator("WORKINGPAGE"); i.hasNext();) {
			Element workingPage = (Element) i.next();
			String workingStr = workingPage.attribute("value").getStringValue();
			setWorkingPage(workingStr);
		}
		
		
		for (Iterator i = root.elementIterator("DOCLIST"); i.hasNext();) {
			Element docList = (Element) i.next();
			for (Iterator j = root.elementIterator("DOC"); j.hasNext();) {
				Element singleDoc = (Element) i.next();
				String docStr = singleDoc.attribute("value").getStringValue();
				_allDocuments.add(_xmlReader.parseDocument(docStr));
			}
		}
		

	}
	
	private String workingDocName() {
		return _workingDocument.name();
	}
	
	// called before the application exits
	public void shutdown() throws IOException {
		OutputFormat pretty = OutputFormat.createPrettyPrint();
		XMLWriter filewriter = new XMLWriter(new FileWriter(Parameters.STARTUP_FILE), pretty);
		
		try {
			org.dom4j.Document xmlDoc = DocumentHelper.createDocument();
			Element root = DocumentHelper.createElement("STARTUP");
			xmlDoc.setRootElement(root);
			
			if (_workingDocument != null) {
				Element workingdoc = DocumentHelper.createElement("WORKINGDOC");
				workingdoc.addAttribute("value", workingdoc.getName());
				root.add(workingdoc);
			}
			
			
			Element docList = DocumentHelper.createElement("DOCLIST");
			root.add(docList);
			
			for (Document doc : _allDocuments) {
				Element docEl = DocumentHelper.createElement("DOC");
				docEl.addAttribute("value", doc.name());
				docList.add(docEl);
			}
			
			filewriter.write(xmlDoc);
		} finally {
			filewriter.close();
		}
	}
	
	public Document setWorkingDocument(String path) throws FileNotFoundException, DocumentException {
		_workingDocument =  _xmlReader.parseDocument(path);
		return _workingDocument;
	}
	
	public void setWorkingPage(String path) {
		// TODO: implement this method!
	}
	
	// when a working document is "closed" it is serialized
	// to the disk
	public void closeWorkingDocument() throws IOException {
		_workingDocument.serialize();
		_workingDocument = null;
	}
	
	// Called after an import in order to establish a new
	// document object
	public Document createDocument() {
		return null;
	}
	
	public void export(String docpath, String outfile) throws IOException {
		_exporter.export(docpath, outfile);
	}
	
	public SearchResults search(String query) {
		return _searcher.getSearchResults(query, _workingDocument, _allDocuments);
	}
	
	// not the main method for the application,
	// just used for testing the core and integrating
	// components independent of the GUI
	public static void main(String[] args) throws DocumentException, IOException {
		CoreManager core = new CoreManager();
		core.setWorkingDocument("tests/xml/testDocument/doc.xml");
		core.export("tests/xml/testDocument/doc.xml", "foo.pdf");
		core.search("foo");
		core.closeWorkingDocument();
		core.shutdown();
	}
	
}
