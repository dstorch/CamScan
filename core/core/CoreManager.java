package core;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

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
			for (Iterator j = docList.elementIterator("DOC"); j.hasNext();) {
				Element singleDoc = (Element) j.next();
				String docStr = singleDoc.attribute("value").getStringValue();
				_allDocuments.add(_xmlReader.parseDocument(docStr));
			}
		}
		

	}
	
	private String workingDocName() {
		return _workingDocument.name();
	}
	
	public Document workingDocument() {
		return _workingDocument;
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
	// document object, if the user imports an entire folder
	public Document createDocumentFromFolder(File sourceLocation) throws IOException {
		
		// put this document in workspace/docs by default
		String name = sourceLocation.getName();
		String pathname = Parameters.DOC_DIRECTORY + "/" + name;
		Document newDoc = new Document(name, pathname);
	
		File targetLocation = new File(Parameters.RAW_DIRECTORY);
		recursiveImageCopy(sourceLocation, targetLocation, newDoc);
		
		// write the XML for the new document to disk
		newDoc.serialize();
		
		return newDoc;
	}
	
	// recursively copies all image files to the workspace/
	private void recursiveImageCopy(File sourceLocation, File targetLocation, Document d) throws IOException {
		
		if (sourceLocation.isDirectory()) {

            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                recursiveImageCopy(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]), d);
            }
        } else {
            
        	String filename = sourceLocation.getName();
        	
        	if (Pattern.matches(Parameters.IMAGE_REGEX, filename)) {
        		
        		// copy the image into the workspace
	            InputStream in = new FileInputStream(sourceLocation);
	            try {
	            	OutputStream out = new FileOutputStream(targetLocation);
	            	try {
			         
			            // Copy the bits from instream to outstream
			            byte[] buf = new byte[1024];
			            int len;
			            while ((len = in.read(buf)) > 0) {
			                out.write(buf, 0, len);
			            }
	            	} finally {
	            		out.close();
	            	}
	            } finally {
	            	 in.close();
	            }
	           
	            // construct the page and add it to the document
	            Page p = new Page(d, -1);
	            p.setRawFile(targetLocation.getPath());
	            p.setProcessedFile(Parameters.PROCESSED_DIRECTORY+"/"+sourceLocation.getName());
	            p.setMetafile(d.name() + "/" + sourceLocation.getName());
	            p.setOcrResults();
	            
        	}

        }
		
	}
	
	// called when the user imports a single photograph
	// as a document
	public Document createDocumentFromFile() {
		return null;
	}
	
	// write a document out as a multipage pdf
	public void exportToPdf(String docpath, String outfile) throws IOException {
		_exporter.exportToPdf(docpath, outfile);
	}
	
	// write a directory of image files
	public void exportImages(Document document, String outdirectory) throws IOException {
		_exporter.exportImages(document, outdirectory);
	}
	
	// write a text file containing the document text
	public void exportText(Document document, String outfile) throws IOException {
		_exporter.exportText(document, outfile);
	}
	
	public SearchResults search(String query) {
		SearchResults results = _searcher.getSearchResults(query, _workingDocument, _allDocuments);
		
		// REMOVE WHEN READY
		//System.out.println("In the working page: ");
		//for (SearchHit hit : results.inWorkingDoc()) {
		//	System.out.println(hit.snippet()+" "+hit.score());
		//}
		//System.out.println("In all other pages: ");
		//for (SearchHit hit : results.elsewhere()) {
		//	System.out.println(hit.snippet()+" "+hit.score());
		//}
		
		return results;
	}
	
	/**
	 * Given the name of a document, it sets its
	 * instance of Document as the working
	 * document.
	 * 
	 * @param docName The name of the document
	 */
	public void setWorkingDocumentFromName(String docName) {
		
		for (Document doc : _allDocuments) {
			if (docName.equals(doc.name())) {
				_workingDocument = doc;
			}
		}
	}
	
	/**
	 * Given an order, it returns the page of the given 
	 * order from the working document.
	 * 
	 * @param order The order of the page to fetch
	 * @return The page with the given order of the
	 * working document.
	 */
	public Page getWorkingDocPageFromOrder(int order) {
		
		for (Page page : _workingDocument.pages()) {
			if (order == page.order()) {
				return page;
			}
		}
		
		return null;
	}
	
	// not the main method for the application,
	// just used for testing the core and integrating
	// components independent of the GUI
	public static void main(String[] args) throws DocumentException, IOException {
		CoreManager core = new CoreManager();
		core.setWorkingDocument("tests/xml/testDocument/doc.xml");
		core.exportToPdf("tests/xml/testDocument/doc.xml", "foo.pdf");
		core.exportText(core.workingDocument(), "../document.txt");
		core.exportImages(core.workingDocument(), "../copiedDoc");
		core.search("Benjamin Franklin almanac");
		core.closeWorkingDocument();
		core.shutdown();
	}
	
}
