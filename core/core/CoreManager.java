package core;

import java.io.*;
import java.util.*;

import ocr.OCRThread;
import ocr.ocrManager;
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
		
		// keep track of whether something went wrong, and throw a warning
		// if necessary
		boolean throwWarning = false;
		
		for (Iterator i = root.elementIterator("WORKINGDOC"); i.hasNext();) {
			Element workingdoc = (Element) i.next();
			String workingStr = workingdoc.attribute("value").getStringValue();
			setWorkingDocumentFromName(workingStr);
		}
		
		for (Iterator i = root.elementIterator("WORKINGPAGE"); i.hasNext();) {
			Element workingPage = (Element) i.next();
			String workingStr = workingPage.attribute("value").getStringValue();
			setWorkingPage(workingStr);
		}
		
		for (Iterator i = root.elementIterator("TESSERACT"); i.hasNext();) {
			Element tesseractEl = (Element) i.next();
			String tessPath = tesseractEl.attribute("path").getStringValue();
			ocrManager.TESS_PATH = tessPath;
		}
		
		
		for (Iterator i = root.elementIterator("DOCLIST"); i.hasNext();) {
			Element docList = (Element) i.next();
			for (Iterator j = docList.elementIterator("DOC"); j.hasNext();) {
				Element singleDoc = (Element) j.next();
				String docStr = singleDoc.attribute("value").getStringValue();
				
				// get the new document by parsing XML
				Document newDoc = _xmlReader.parseDocument(docStr);
				
				if (newDoc != null) {
					_allDocuments.add(newDoc);
				} else {
					throwWarning = true;
				}
			}
		}
		
		// if a problem has occurred, throw a file not found exception
		// so that the GUI can display a warning message
		if (throwWarning) throw new FileNotFoundException("Some of your files could not be located!");

	}
	
	public Document workingDocument() {
		return _workingDocument;
	}
	
	// called before the application exits
	public void shutdown() throws IOException {
		writeStartupFile();
	}
	
	// writes the startup file to disk based on the list of all documents
	public void writeStartupFile() throws IOException {
		OutputFormat pretty = OutputFormat.createPrettyPrint();
		XMLWriter filewriter = new XMLWriter(new FileWriter(Parameters.STARTUP_FILE), pretty);
		
		try {
			org.dom4j.Document xmlDoc = DocumentHelper.createDocument();
			Element root = DocumentHelper.createElement("STARTUP");
			xmlDoc.setRootElement(root);
			
			// tesseract pathname
			Element tesseract = DocumentHelper.createElement("TESSERACT");
			tesseract.addAttribute("path", ocrManager.TESS_PATH);
			root.add(tesseract);
			
			if (_workingDocument != null) {
				Element workingdoc = DocumentHelper.createElement("WORKINGDOC");
				workingdoc.addAttribute("value", _workingDocument.pathname());
				root.add(workingdoc);
			}
			
			
			Element docList = DocumentHelper.createElement("DOCLIST");
			root.add(docList);
			
			for (Document doc : _allDocuments) {
				Element docEl = DocumentHelper.createElement("DOC");
				docEl.addAttribute("value", doc.pathname());
				docList.add(docEl);
			}
			
			filewriter.write(xmlDoc);
		} finally {
			filewriter.close();
		}
	}
	
	public void setWorkingPage(String path) {
		// TODO: implement this method!
	}
	
	// when a working document is "closed" it is serialized
	// to the disk
	public void closeWorkingDocument() throws IOException {
		if (_workingDocument != null) {
			_workingDocument.serialize();
		}
	}
	
	public void renameDocument(String docName, String newName) throws IOException {
		for (Document d : _allDocuments) {
			if (docName.equals(d.name())) {
				renameDocument(d, newName);
			}
		}
	}
	
	public void renameDocument(Document d, String newName) throws IOException {
		d.rename(newName);
		d.serialize();
		writeStartupFile();
	}
	
	public void deleteDocument(String docName) throws IOException {
		Document toDelete = null;
		for (Document d : _allDocuments) {
			if (docName.equals(d.name())) {
				toDelete = d;
			}
		}
		
		deleteDocument(toDelete);
	}
	
	public void deleteDocument(Document d) throws IOException {
		d.delete();
		_allDocuments.remove(d);
		d = null;
		
		// make sure that all references to the document are
		// deleted (so that it gets garbage collected, and will
		// not get serialized)
		if (_workingDocument != null) {
			if (_workingDocument.equals(d)) _workingDocument = null;
		}
		
		writeStartupFile();
	}
	
	// Called after an import in order to establish a new
	// document object, if the user imports an entire folder
	public Document createDocumentFromFolder(File sourceLocation) throws IOException {
		
		if (sourceLocation.isFile()) {
			return createDocumentFromFile(sourceLocation);
		}
		
		// put this document in workspace/docs by default
		String name = sourceLocation.getName();
		String directory = Parameters.DOC_DIRECTORY + "/" + name;
		File dirFile = new File(directory);
		if (!dirFile.mkdir()) throw new IOException("Import aborted: problem making new document directory!");
		String pathname = directory + "/" + "doc.xml";
		Document newDoc = new Document(name, pathname);
		
		File targetLocation = new File(Parameters.RAW_DIRECTORY);
		recursiveImageCopy(sourceLocation, targetLocation, newDoc, 1);
		
		// add the new document to the list of documents
		_allDocuments.add(newDoc);
		
		// update data for the new document on the disk
		newDoc.serialize();
		writeStartupFile();
		return newDoc;
	}
	
	// recursively copies all image files to the workspace/
	private void recursiveImageCopy(File sourceLocation, File targetLocation, Document d, int order)
		throws IOException {
		
		System.out.println("recurring");
		
		if (sourceLocation.isDirectory()) {

            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                recursiveImageCopy(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]), d, ++order);
            }
        } else {
            
        	// get the file extension
        	String filename = sourceLocation.getName();
        	String[] extensionArr = filename.split("[.]");
        	String extension = "";
        	if (extensionArr.length > 0) {
        		extension = extensionArr[extensionArr.length-1];
        	}
        	
        	if (extension.equals("tiff")) {
        		
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
	           
	            // get the image file name without a ".tiff" extension
	            String imageFile = sourceLocation.getName();
	            String noExt = imageFile.substring(0,imageFile.length()-5);
	            
	            // construct the page and add it to the document
	            Page p = new Page(d, order);
	            p.setRawFile(targetLocation.getPath());
	            p.setProcessedFile(Parameters.PROCESSED_DIRECTORY+"/"+sourceLocation.getName());
	            p.setMetafile(Parameters.DOC_DIRECTORY+"/"+d.name()+"/" + noExt+".xml");
	            d.addPage(p); 
	            
	            // do OCR!
	            p.launchOcrThread();
	            
        	}

        }
	}
	

	// called when the user imports a single photograph
	// as a document
	public Document createDocumentFromFile(File sourceLocation) throws IOException {
		
		// put this document in workspace/docs by default
        // get the image file name without a ".tiff" extension
        String imageFile = sourceLocation.getName();
        String noExt = imageFile.substring(0,imageFile.length()-5);
		String directory = Parameters.DOC_DIRECTORY + "/" + noExt;
		File dirFile = new File(directory);
		if (!dirFile.mkdir()) throw new IOException("Import aborted: problem making new document directory!");
		String pathname = directory + "/" + "doc.xml";
		Document newDoc = new Document(noExt, pathname);
	
		File targetLocation = new File(Parameters.RAW_DIRECTORY + "/" + sourceLocation.getName());
		recursiveImageCopy(sourceLocation, targetLocation, newDoc, 1);
		
		// add the document to the global list of documents
		_allDocuments.add(newDoc);
		
		// write the XML for the new document to disk
		newDoc.serialize();
		writeStartupFile();
		
		return newDoc;
	}
	
	// write a document out as a multipage pdf
	public void exportToPdf(String pathname, String outfile) throws IOException {
		
		// get the document based on name
		Document doc = null;
		for (Document d : _allDocuments) {
			if (pathname.equals(d.pathname())) {
				doc = d;
			}
		}
		
		if (doc == null) throw new IOException("Problem exporting document!");
		
		// run OCR on each page---the bottleneck for pdf export!
		for (Page p : doc.pages()) {
			p.setOcrResults();
		}
		
		// serialize the document, to commit the ocr results to disk
		doc.serialize();
		
		_exporter.exportToPdf(pathname, outfile);
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
		System.out.println("In the working page: ");
		for (SearchHit hit : results.inWorkingDoc()) {
			System.out.println(hit.snippet()+" "+hit.score());
		}
		System.out.println("In all other pages: ");
		for (SearchHit hit : results.elsewhere()) {
			System.out.println(hit.snippet()+" "+hit.score());
		}
		
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
		//core.createDocumentFromFile(new File("tests/xml/testDocument/hamlet_1.tiff"));
		core.createDocumentFromFile(new File("../sample2.tiff"));
		//core.setWorkingDocumentFromName("sample_page");
		core.exportToPdf("workspace/docs/sample2/doc.xml", "../foo.pdf");
		//core.exportText(core.workingDocument(), "../document.txt");
		//core.exportImages(core.workingDocument(), "../copiedDoc");
		//core.search("political situation");
		//core.renameDocument("sample2", "sample_page_2");
		//core.deleteDocument("sample_page_2");
		core.closeWorkingDocument();
		core.shutdown();
	}
	
}
