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
import java.awt.image.BufferedImage;
import java.awt.Point;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

@SuppressWarnings("rawtypes")
public class CoreManager {

	private Exporter _exporter;
	private Searcher _searcher;
	private XMLReader _xmlReader;
	private List<Document> _allDocuments;

	// instance variables telling the GUI which page to display
	private Document _workingDocument;
	private Page _workingPage;
	private BufferedImage _rawImage;
	private BufferedImage _processedImage;

	public CoreManager() throws DocumentException, IOException {
		_xmlReader = new XMLReader();
		_exporter = Exporter.Factory.create();
		_searcher = Searcher.Factory.create();
		_allDocuments = new LinkedList<Document>();
		startup();
	}

	public List<Document> getDocuments() {
		return _allDocuments;
	}


	// called from the constructor when the application launches
	public void startup() throws DocumentException, IOException {
		SAXReader reader = new SAXReader();
		
		// try to read the startup file. If no file is found, then
		// treat this as the first launch of the application
		org.dom4j.Document document = null;
		try {
			document = reader.read(new FileReader(Parameters.STARTUP_FILE));
		} catch (FileNotFoundException e) {
			SystemConfiguration.autoconfigure();
			return;
		}
		
		
		Element root = document.getRootElement();

		// keep track of whether something went wrong, and throw a warning
		// if necessary
		boolean throwWarning = false;
		
		// read in the path of the tesseract executable
		for (Iterator i = root.elementIterator("TESSERACT"); i.hasNext();) {
			Element tesseractEl = (Element) i.next();
			String tessPath = tesseractEl.attribute("path").getStringValue();
			SystemConfiguration.TESS_PATH = tessPath;
		}
		
		// read in the path of the python executable
		for (Iterator i = root.elementIterator("PYTHON"); i.hasNext();) {
			Element pythonEl = (Element) i.next();
			String pythonPath = pythonEl.attribute("path").getStringValue();
			SystemConfiguration.PYTHON_PATH = pythonPath;
		}	

		for (Iterator i = root.elementIterator("WORKINGPAGE"); i.hasNext();) {
			Element workingPage = (Element) i.next();
			String workingStr = workingPage.attribute("value").getStringValue();
			String order = workingPage.attribute("order").getStringValue();
			String name = workingPage.attribute("name").getStringValue();
			setWorkingPage(workingStr, Integer.parseInt(order), name);
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

		for (Iterator i = root.elementIterator("WORKINGDOC"); i.hasNext();) {
			Element workingdoc = (Element) i.next();
			String workingStr = workingdoc.attribute("value").getStringValue();
			String[] fields = workingStr.split("\\\\");

			if (fields.length > 1) {
				String name = fields[fields.length - 2];
				setWorkingDocumentFromName(name);
			}	

		}

		// if a problem has occurred, throw a file not found exception
		// so that the GUI can display a warning message
		if (throwWarning) {
			JOptionPane.showMessageDialog(Parameters.getFrame(), "Some of your files could not be located!",
					"Startup Warning", JOptionPane.WARNING_MESSAGE);
		}

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
			tesseract.addAttribute("path", SystemConfiguration.TESS_PATH);
			root.add(tesseract);

			// python pathname
			Element python = DocumentHelper.createElement("PYTHON");
			python.addAttribute("path", SystemConfiguration.PYTHON_PATH);
			root.add(python);

			if (_workingDocument != null) {
				Element workingdoc = DocumentHelper.createElement("WORKINGDOC");
				workingdoc.addAttribute("value", _workingDocument.pathname());
				root.add(workingdoc);
			}

			if (_workingPage != null) {
				Element workingdoc = DocumentHelper.createElement("WORKINGPAGE");
				workingdoc.addAttribute("value", _workingPage.metafile());
				workingdoc.addAttribute("order", new Integer(_workingPage.order()).toString());
				workingdoc.addAttribute("name", _workingPage.name());
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

	public void setWorkingPage(String path, int order, String name) throws FileNotFoundException, DocumentException {
		try {
			_workingPage = _xmlReader.parsePage(path, order, _workingDocument, name);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(Parameters.getFrame(),
					"Some of your data might be lost. Did CamScan shut down correctly?",
					"Startup Warning", JOptionPane.WARNING_MESSAGE);
		} catch (DocumentException e) {
			JOptionPane.showMessageDialog(Parameters.getFrame(),
					"Your CamScan data may have been corrupted.",
					"Startup Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void setWorkingPageAndImage(Page page) throws IOException {

		if (_workingPage == page){
			return;
		}

		_workingPage = page;
		_rawImage = page.getRawImgFromDisk();

		if (Parameters.isInEditMode())
			this.updateProcessedImageWithRawDimensions();
		else
			this.updateProcessedImage();
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

	private void renameDocument(Document d, String newName) throws IOException {
		d.rename(newName);
		d.serialize();
		writeStartupFile();
		setWorkingDocumentFromName(newName);
	}

	public void renamePage(Document d, int order, String newName) throws IOException{
		Page p = getPageFromOrder(d,order);
		for(Page page : d.pages()){
			if(page.equals(p)) page.rename(newName);
		}
	}


	// returns null if there isn't a document with name
	private Document getDocFromName(String docName){

		String[] fields = docName.split("\\\\");
		if (fields.length > 1) {

		}

		Document doc = null;
		for (Document d : _allDocuments) {

			if (docName.equals(d.name())) {
				doc = d;
			}
		}

		return doc;
	}

	public void deleteDocument(String docName) throws IOException {
		Document toDelete = getDocFromName(docName);
		deleteDocument(toDelete);
	}

	private void deleteDocument(Document d) throws IOException {

		// make sure that all references to the document are
		// deleted (so that it gets garbage collected, and will
		// not get serialized)
		if (_workingDocument != null) {
			if (_workingDocument.equals(d)){
				if(_allDocuments.size()>1){
					Document first = _allDocuments.get(0);
					setWorkingDocument(first);
					setWorkingPageAndImage(first.pages().first());
				}else{ // there are no Documents
					System.out.println("IN ELSE!!!");
				_workingDocument = null;
				_workingPage = null;
				_rawImage = null;
				_processedImage = null;
				}
			}

		}

		d.delete();
		_allDocuments.remove(d);
		d = null;


		writeStartupFile();
	}

	public Page getPageFromOrder(Document d, int o){
		for (Page p : d.pages()) {
			if(p.order() == o) return p;
		}
		return null;
	}

	public void deletePage(Document d, int order) throws IOException{
		Page p = getPageFromOrder(d, order);
		deletePage(d, p);
	}

	private void deletePage(Document d, Page p) throws IOException{
		d.deletePage(p);
		// if the last page is deleted, delete the document as well
		if(d.pages().size()==0) deleteDocument(d);
	}

	public void reorderPage(Document d, Page p, int newOrder) throws IOException{
		if(p.order()!=newOrder) d.reorderPage(p, newOrder);
	}


	public void mergeDocuments(String d1, String d2) throws IOException{
		Document toMerge1 = null;
		Document toMerge2 = null;
		for (Document d : _allDocuments) {
			if (d1.equals(d.name())) {
				toMerge1 = d;
			}else if(d2.equals(d.name())){
				toMerge2 = d;
			}
		}

		mergeDocuments(toMerge1, toMerge2);
	}


	// Merges two inputted documents (appends pages of d2 to end of d1)
	private void mergeDocuments(Document d1, Document d2) throws IOException {

		String doc1 = d1.name();
		int numPages = d1.pages().size();

		String docPath = Parameters.DOC_DIRECTORY+File.separator+doc1+File.separator;

		for (Page p : d2.pages()) {
			// extract name of file and append to path of document 1 to get new path
			String[] s = p.metafile().split("\\\\");
			String newMetaPath = docPath + s[s.length - 1];

			File oldFile = new File(p.metafile());
			File newFile = new File(newMetaPath);

			boolean success = oldFile.renameTo(newFile);
			if (!success) {
				System.err.println("***********"+oldFile + " not moved to " + newFile);
			}

			// update order int of page
			p.setOrder(numPages+p.order());
			// update metafile path
			p.setMetafile(newMetaPath);
		}

		// update list of Pages in d1
		d1.pages().addAll(d2.pages());
		d1.serialize();

		// delete directory of second Document
		d2.delete();

		// remove second Document from global list
		_allDocuments.remove(d2);

	}

	// Called after an import in order to establish a new
	// document object, if the user imports an entire folder
	public Document createDocumentFromFolder(File sourceLocation) throws IOException {

		if (sourceLocation.isFile()) {
			return createDocumentFromFile(sourceLocation);
		}

		// put this document in workspace/docs by default
		String name = sourceLocation.getName();
		String directory = Parameters.DOC_DIRECTORY + File.separator + name;
		File dirFile = new File(directory);
		if (!dirFile.mkdir()) throw new IOException("Import aborted: problem making new document directory!");
		String pathname = directory + File.separator + "doc.xml";
		Document newDoc = new Document(name, pathname);

		File targetLocation = new File(Parameters.RAW_DIRECTORY);
		importPages(sourceLocation, targetLocation, newDoc, 0);

		// add the new document to the list of documents
		if (newDoc.pages().isEmpty()) {
			JOptionPane.showMessageDialog(Parameters.getFrame(), "There are no image files in that folder!",
					"Import Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		_allDocuments.add(newDoc);

		_workingDocument = newDoc;

		// update data for the new document on the disk
		newDoc.serialize();
		writeStartupFile();
		return newDoc;
	}

	// recursively copies all image files to the workspace/
	private void importPages(File sourceLocation, File targetLocation, Document d, int order)

	throws IOException {

		if (sourceLocation.isDirectory()) {

			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				importPages(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]), d, ++order);
			}
		} else {

			String filename = sourceLocation.getName();

			boolean validExt = false;
			for (int i = 0; i<Parameters.imgExtensions.length;i++) {
				if (filename.toLowerCase().endsWith(Parameters.imgExtensions[i])) {
					validExt = true;
					break;
				}
			}

			if (validExt) {

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
				int lastIndex = imageFile.lastIndexOf(".");
				String noExt = imageFile.substring(0, lastIndex);

				// construct the page and add it to the document
				Page p = new Page(d, order, noExt);

				// set pathname attributes of the page
				p.setRawFile(targetLocation.getPath());
				p.setProcessedFile(Parameters.PROCESSED_DIRECTORY + File.separator + sourceLocation.getName());
				p.setMetafile(Parameters.DOC_DIRECTORY + File.separator + d.name() + File.separator + noExt + ".xml");

				// guess initial configuration values
				p.initGuesses();
				d.addPage(p);

				// do OCR!
				launchOcrThread(p);
			}
		}
	}

	public void launchOcrThread(Page page) {
		OCRThread t = new OCRThread(page);
		t.start();
	}

	// called when the user imports a single photograph
	// as a document
	public Document createDocumentFromFile(File sourceLocation) throws IOException {

		// put this document in workspace/docs by default
		// get the image file name without a ".tiff" extension
		String imageFile = sourceLocation.getName();
		String noExt = removeExtension(imageFile);
		String directory = Parameters.DOC_DIRECTORY + File.separator + noExt;
		File dirFile = new File(directory);
		if (!dirFile.mkdir()) {
			throw new IOException("Import aborted: problem making new document directory!");
		}
		String pathname = directory + File.separator + "doc.xml";
		Document newDoc = new Document(noExt, pathname);

		File targetLocation = new File(Parameters.RAW_DIRECTORY + File.separator + sourceLocation.getName());
		importPages(sourceLocation, targetLocation, newDoc, 1);

		// add the document to the global list of documents
		_allDocuments.add(newDoc);

		_workingDocument = newDoc;

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

		if (doc == null) {
			throw new IOException("Problem exporting document!");
		}

		_exporter.exportToPdf(doc, outfile);
	}

	// write a directory of image files
	public void exportImages(Document document, String outdirectory) throws IOException {

		for (Page page : document.pages()) {
			//TODO
			_processedImage = VisionManager.rerenderImage(getRawImage(), page.corners(), page.config());
			page.writeProcessedImage();
		}

		_exporter.exportImages(document, outdirectory);
	}

	// write a text file containing the document text
	public void exportText(Document document, String outfile) throws IOException {
		_exporter.exportText(document, outfile);
	}

	public SearchResults search(String query) {
		return _searcher.getSearchResults(query, _workingDocument, _allDocuments);
	}

	/**
	 * Given the name of a document, it sets its
	 * instance of Document as the working
	 * document.
	 *
	 * @param docName The name of the document
	 * @throws IOException 
	 */
	public void setWorkingDocumentFromName(String docName) throws IOException {
		System.out.println("Doc Name: "+docName);
		Document doc = getDocFromName(docName);
		_workingDocument = doc;
		setWorkingPageAndImage(doc.pages().first());
	}

	public void setWorkingDocument(Document doc) {
		_workingDocument = doc;
	}

	public Page getWorkingPage() {
		return _workingPage;
	}

	public BufferedImage getRawImage() {
		return _rawImage;
	}

	public void setProcessedImage(BufferedImage img) {
		_processedImage = img;
	}

	public BufferedImage getProcessedImage() {
		return _processedImage;
	}

	/**
	 * Given an order, it returns the page of the given
	 * order from the working document.
	 *
	 * @param order The order of the page to fetch
	 * @return The page with the given order of the
	 * working document.
	 */
	public Page getWorkingDocPageFromName(int order) {

		for (Page page : _workingDocument.pages()) {
			if (order == page.order()) {
				return page;
			}
		}

		return null;
	}

	// called when changing from edit mode to view mode
	// uses changes made in edit mode and rerenders the image
	public void updateProcessedImage() {
		Page curr = getWorkingPage();
		BufferedImage img = getRawImage();
		if (curr != null && img != null) {
			getWorkingPage().ocrNeedsRevision();
			_processedImage = VisionManager.rerenderImage(getRawImage(), curr.corners(), curr.config());
		}
	}

	public void updateProcessedImageWithRawDimensions() {
		Page curr = getWorkingPage();
		BufferedImage img = getRawImage();
		if (curr != null && img != null) {

			Corners originalCorners = new Corners(new Point(0, 0), 
					new Point(getRawImage().getWidth(), 0), 
					new Point(0, getRawImage().getHeight()), 
					new Point(getRawImage().getWidth(), getRawImage().getHeight()));

			//			_processedImage = VisionManager.rerenderImage(getRawImage(), VisionManager.findCorners(getRawImage()), curr.config());
			_processedImage = VisionManager.rerenderImage(getRawImage(), originalCorners, curr.config());
		}
	}

	public void flipImage(boolean isVertical) {

		if (isVertical){
			try {
				ConfigurationValue configVal = this.getWorkingPage().config().getKey(ConfigurationValue.ValueType.FlipVertical);
				Parameters.getCoreManager().getWorkingPage().config().setKey(new ConfigurationValue(ConfigurationValue.ValueType.FlipVertical, !(Boolean) configVal.value()));
			} catch (InvalidTypingException e) {
				e.printStackTrace();
			}
		}else{
			try {
				ConfigurationValue configVal = this.getWorkingPage().config().getKey(ConfigurationValue.ValueType.FlipHorizontal);
				Parameters.getCoreManager().getWorkingPage().config().setKey(new ConfigurationValue(ConfigurationValue.ValueType.FlipHorizontal, !(Boolean) configVal.value()));
			} catch (InvalidTypingException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Given a temperature (centered around 0, e.g. -50 to 50), adjust the image.
	 */
	public void changeTemperature(int temperature){
		try {
			Parameters.getCoreManager().getWorkingPage().config().setKey(new ConfigurationValue(ConfigurationValue.ValueType.ColorTemperature, temperature));
		} catch (InvalidTypingException e) {
			System.out.println("ConfigurationValue threw an InvalidTypingException! Won't be able to change the temperature.");
		}
	}

	public void boostConstrast(boolean boost) {
		ConfigurationValue configVal = this.getWorkingPage().config().getKey(ConfigurationValue.ValueType.ContrastBoost);

		try {
			Parameters.getCoreManager().getWorkingPage().config().setKey(new ConfigurationValue(ConfigurationValue.ValueType.ContrastBoost, !(Boolean) configVal.value()));
		} catch (InvalidTypingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given two Corners objects giving corner locations in image
	 * coordinates, converts a single Page object into two, and
	 * updates all Document attributes accordingly.
	 * 
	 * @param box1 - one of the bounding box on which to do the split
	 * @param box2 - the second bounding box on which to do the split
	 */
	public void applySplit(Corners box1, Corners box2) {

		Page p = this._workingPage;

		int order = _workingPage.order();
		String processed = _workingPage.processed();
		String metafile = _workingPage.metafile();

		_workingPage.setCorners(box1);

		// construct the page and add it to the document
		Page splitProduct = new Page(_workingDocument, ++order, p.name()+"_split");
		splitProduct.setCorners(box2);

		// rename the processed file for the split product
		String newName = removeExtension(processed) + "_split." + getExtension(processed);
		splitProduct.setProcessedFile(newName);

		// rename the metafile for the split product
		String newMetafile = removeExtension(metafile) + "_split.xml";
		splitProduct.setMetafile(newMetafile);
		splitProduct.setConfig(_workingPage.config().getCopy());
		splitProduct.setRawFile(_workingPage.raw());

		_workingDocument.addPage(splitProduct);

		// attempt to serialize
		try {
			_workingDocument.serialize();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// do OCR!
		launchOcrThread(_workingPage);
		launchOcrThread(splitProduct);

		Parameters.getPageExpPanel().update();
		this.setProcessedImage(this.getRawImage());
		Parameters.getCoreManager().updateProcessedImageWithRawDimensions();

	}

	private String removeExtension(String file) {
		String[] pieces = file.split("[.]");

		String result = "";
		for (int i = 0; i < pieces.length-1; i++) {
			result += pieces[i];
		}

		return result;
	}

	private String getExtension(String file) {
		System.out.println(file);
		String[] pieces = file.split("[.]");
		return pieces[pieces.length-1];
	}

	// Called every time entering Edit Mode or Configuration Dictionary is changed
	public void getEditImageTransform() {
		_processedImage = VisionManager.imageGlobalTransforms(_rawImage,
				Parameters.getCoreManager().getWorkingPage().config());
	}



	//
	//    // called when user tries to place corner; tries to make a better point given the user's guess
	//    public Point snapCorner(Point pt) {
	//        return VisionManager.snapCorner(Parameters.getCurrPageImg(), pt);
	//    }

}
