package core;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.*;
import org.dom4j.io.*;
import search.*;
import vision.*;
import export.*;
import java.awt.image.BufferedImage;
import java.awt.Point;
import javax.swing.JOptionPane;

/*******************************************************************
 * CoreManager
 *
 * The main delegator class for calling out to the modules.
 * Also stores the working Page and the working Document, as
 * well as the list of all documents stored in the library.
 * 
 * @author dstorch, sbirch, mmicalle, stelios
 * 
 *******************************************************************/

@SuppressWarnings("rawtypes")
public class CoreManager {

	private Exporter _exporter;
	private Searcher _searcher;
	private XMLReader _xmlReader;
	private List<Document> _allDocuments;

	/**
	 * Instance variables keeping track of the
	 * current display.
	 */
	private Document _workingDocument;
	private Page _workingPage;
	private BufferedImage _rawImage;
	private BufferedImage _processedImage;

	/**
	 * Constructor
	 * Sets some instance variables and calls the startup
	 * function.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public CoreManager() throws DocumentException, IOException {
		_xmlReader = new XMLReader();
		_exporter = Exporter.Factory.create();
		_searcher = Searcher.Factory.create();
		_allDocuments = new LinkedList<Document>();
		startup();
	}

	/*******************************************************************
	 * 
	 * GETTERS
	 * 
	 *******************************************************************/

	public List<Document> getDocuments() {
		return _allDocuments;
	}

	public Document workingDocument() {
		return _workingDocument;
	}

	public Page getWorkingPage() {
		return _workingPage;
	}

	public BufferedImage getRawImage() {
		return _rawImage;
	}

	public BufferedImage getProcessedImage() {
		return _processedImage;
	}

	/**
	 * Given a document name, scans the list of documents
	 * for one with a matching name. Returns null if no
	 * match was found.
	 * 
	 * @param docName - the name of the document to get
	 * @return the Document object to return
	 */
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


	/**
	 * Given a document and a page number, retrieves
	 * the corresponding page.
	 * 
	 * @param d - the Document
	 * @param o - the page number, an int
	 * 
	 * @return the corresponding page of that number
	 */
	public Page getPageFromOrder(Document d, int o){
		for (Page p : d.pages()) {
			if(p.order() == o) return p;
		}
		return null;
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


	/*******************************************************************
	 * 
	 * SETTERS
	 * 
	 *******************************************************************/

	/**
	 * Given the name of a document, it sets its
	 * instance of Document as the working
	 * document.
	 *
	 * @param docName The name of the document
	 * @throws IOException 
	 */
	public void setWorkingDocumentFromName(String docName) throws IOException {
		Document doc = getDocFromName(docName);
		_workingDocument = doc;
		setWorkingPageAndImage(doc.pages().first());
	}

	public void setWorkingDocument(Document doc) {
		_workingDocument = doc;
	}

	public void setProcessedImage(BufferedImage img) {
		_processedImage = img;
	}

	/**
	 * Sets the working page by parsing the XML on disk.
	 * 
	 * @param path - the path to the metafile for the page
	 * @param order - the page number
	 * @param name - the name of the Page
	 * 
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 */
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

	/**
	 * Given a page, set it as the working page and set
	 * the working image by reading it in from the disk.
	 * 
	 * @param page - the Page to set as the working Page
	 * @throws IOException
	 */
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


	/*******************************************************************
	 * 
	 * STARTUP AND SHUTDOWN
	 * 
	 *******************************************************************/

	/**
	 * Called once on startup. Reads the state of the application
	 * from .camscan_startup.xml.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
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
			String[] fields = workingStr.split(SystemConfiguration.PATH_REGEX);

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

	/**
	 * Called once during a normal shutdown of the application.
	 * 
	 * @throws IOException
	 */
	public void shutdown() throws IOException {
		writeStartupFile();
	}

	/**
	 * Writes the startup XML so that on the next launch
	 * of the application, the state will be preserved.
	 * 
	 * @throws IOException
	 */
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

	/*******************************************************************
	 * 
	 * DOCUMENT MANIPULATION
	 * 
	 *******************************************************************/

	/**
	 * One of the renaming functions. Given the name of a document,
	 * finds that document in the list, and calls the version of
	 * the function below in order to complete the rename.
	 * 
	 * @param docName - the old name of the document
	 * @param newName - the new name of the document
	 */
	public void renameDocument(String docName, String newName) throws IOException {
		for (Document d : _allDocuments) {
			if (docName.equals(d.name())) {
				renameDocument(d, newName);
			}
		}
	}

	/**
	 * Given a Document, renames it to the given name. Called by the overloaded
	 * form of this method (see above).
	 * 
	 * @param d - the Document to rename
	 * @param newName - the new name of the document
	 * 
	 * @throws IOException
	 */
	private void renameDocument(Document d, String newName) throws IOException {
		d.rename(newName);
		d.serialize();
		writeStartupFile();
		setWorkingDocumentFromName(newName);
	}

	/**
	 * Given a Document and a Page number, renames the corresponding
	 * page.
	 * 
	 * @param d - the Document to rename
	 * @param order - the page number in the document of the page to rename
	 * @param newName - the new name
	 * 
	 * @throws IOException
	 */
	public void renamePage(Document d, int order, String newName) throws IOException{
		Page p = getPageFromOrder(d,order);
		for(Page page : d.pages()){
			if(page.equals(p)) page.rename(newName);
		}
	}

	/**
	 * Given the name of a document, deletes it by calling the
	 * overloaded version of the function below.
	 * 
	 * @param docName - the name of the document to delete
	 * 
	 * @throws IOException
	 */
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

	/**
	 * Given a Document and a page number in that document, deletes
	 * the indicated Page completely from the workspace.
	 * 
	 * @param d - the Document containing the Page to delete
	 * @param order - the page number of the Page to delete
	 * 
	 * @throws IOException
	 */
	public void deletePage(Document d, int order) throws IOException{
		Page p = getPageFromOrder(d, order);
		deletePage(d, p);
	}

	/**
	 * Overloaded version of teh deletePage() function above.
	 * Given a Document and a Page, deletes the corresponding
	 * page.
	 * 
	 * @param d - the Document containing the Page to delete
	 * @param p - the Page to delete
	 * 
	 * @throws IOException
	 */
	private void deletePage(Document d, Page p) throws IOException{
		d.deletePage(p);
		// if the last page is deleted, delete the document as well
		if(d.pages().size()==0) deleteDocument(d);
	}

	/**
	 * Given a Document and a Page in that Document, moves
	 * the page so that it is positioned at the specified
	 * page number.
	 * 
	 * Called by drag and drop reordering.
	 * 
	 * @param d - the Document containing the page to reorder
	 * @param p - the Page to reorder
	 * @param newOrder - the page number to which Page p will be moved
	 * 
	 * @throws IOException
	 */
	public void reorderPage(Document d, ArrayList<String> newOrders) throws IOException{
		d.reorderPage(newOrders);
	}

        /**
         * Merges all documents in List by calling overloaded version that takes in only two Strings
         *
         * @param docs -- all the documents needed to merged together; the document
         *          takes the name of the first String in the list
         * @throws IOException
         */
        public void mergeDocuments(ArrayList<String> docs) throws IOException{
            String docName = docs.get(0);

            for(int i = 1; i<docs.size(); i++){
                mergeDocuments(docName, docs.get(i));
            }

        }

	/**
	 * Implements combining the pages from two documents
	 * into a single document.
	 * 
	 * Calls the overloaded version of the function which takes
	 * Documents and not Strings naming documents.
	 * 
	 * @param d1 - a string naming the first document to merge
	 * @param d2 - a string naming the second document to merge
	 * @throws IOException
	 */
	private void mergeDocuments(String d1, String d2) throws IOException{
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


	/**
	 * Given two documents, combines them into a single document
	 * by taking the pages of the two originals.
	 * 
	 * @param d1 - one Document to merge
	 * @param d2 - the second Document to merge
	 * @throws IOException
	 */
	private void mergeDocuments(Document d1, Document d2) throws IOException {

		String doc1 = d1.name();
		int numPages = d1.pages().size();

		String docPath = Parameters.DOC_DIRECTORY+File.separator+doc1+File.separator;

		for (Page p : d2.pages()) {
			// extract name of file and append to path of document 1 to get new path
			String[] s = p.metafile().split("\\\\");
			//String newMetaPath = docPath + s[s.length - 1];
                        String newMetaPath = docPath + p.name()+".xml";

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
		d2.deleteOnlyDirectory();

		// remove second Document from global list
		_allDocuments.remove(d2);

                writeStartupFile();

	}

	/*******************************************************************
	 * 
	 * IMPORT AND EXPORT
	 * 
	 *******************************************************************/

	/**
	 * Given the location of a folder of images to import,
	 * copies the raw images into the workspace, and creates
	 * a Document object with the required attributes.
	 * 
	 * Runs corner finding and OCR automatically on the imported
	 * images.
	 * 
	 * Calls importPages() to do most of the work.
	 * 
	 * @param sourceLocation - the directory containing the images to import
	 */
	public Document createDocumentFromFolder(File sourceLocation) throws IOException {

		if (sourceLocation.isFile()) {
			return createDocumentFromFile(sourceLocation);
		}

		// put this document in workspace/docs by default
		String name = sourceLocation.getName();
		String directory = Parameters.DOC_DIRECTORY + File.separator + name;
		File dirFile = new File(directory);
		
		// handle the error case by displaying a warning, and then proceding to delete
		if (!dirFile.mkdir()) {
			
			int result = JOptionPane.showConfirmDialog(Parameters.getFrame(),
										  "You already have a document by that name. Do you want to overwrite?",
										  "Import Document",
										  JOptionPane.YES_NO_OPTION	);
			
			// if overwriting
			if (result == JOptionPane.YES_OPTION) {
				IOFunctions.deleteDir(dirFile);
				dirFile.mkdir();
			}
			
			// otherwise do nothing
			else {
				return null;
			}
		}
		
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

	/**
	 * The import function for importing a single image file.
	 * 
	 * @param sourceLocation - the file to import
	 * 
	 * @return the Document instance which is created as an
	 * internal representation of the imported image
	 * 
	 * @throws IOException
	 */
	public Document createDocumentFromFile(File sourceLocation) throws IOException {

		// put this document in workspace/docs by default
		// get the image file name without a ".tiff" extension
		String imageFile = sourceLocation.getName();
		String noExt = removeExtension(imageFile);
		String directory = Parameters.DOC_DIRECTORY + File.separator + noExt;
		File dirFile = new File(directory);
		
		// handle the error case by displaying a warning, and then proceding to delete
		if (!dirFile.mkdir()) {
			
			int result = JOptionPane.showConfirmDialog(Parameters.getFrame(),
										  "You already have a document by that name. Do you want to overwrite?",
										  "Import Document",
										  JOptionPane.YES_NO_OPTION	);
			
			// if overwriting
			if (result == JOptionPane.YES_OPTION) {
				IOFunctions.deleteDir(dirFile);
				dirFile.mkdir();
			}
			
			// otherwise do nothing
			else {
				return null;
			}
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


	/**
	 * Recursively imports images in the selected folder.
	 * 
	 * @param sourceLocation - the root folder of the subdirectory to import
	 * @param targetLocation - the location to which all image files should be copied
	 * @param d - the document instance that the imported images belong to
	 * @param order - keeps track of the page number as it recurs
	 * 
	 * @throws IOException
	 */
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
				p.launchOcrThread();
			}
		}
	}


	/**
	 * The GUI calls this function in order to implement PDF export.
	 * Just calls the underlying function of the Exporter.
	 * 
	 * @param pathname
	 * @param outfile
	 * @throws IOException
	 */
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

	/**
	 * Exports a document by copying the images out to a new folder.
	 * 
	 * @param document - the Document to copy
	 * @param outdirectory - a String giving the directory to copy to
	 * 
	 * @throws IOException
	 */
	public void exportImages(Document document, String outdirectory) throws IOException {

		for (Page page : document.pages()) {
			//TODO
			_processedImage = VisionManager.rerenderImage(getRawImage(), page.corners(), page.config());
			page.writeProcessedImage();
		}

		_exporter.exportImages(document, outdirectory);
	}

	/**
	 * Exports the text extracted from the document by OCR.
	 * 
	 * @param document - the document to export
	 * @param outfile - the pathname specifying the outfile
	 * 
	 * @throws IOException
	 */
	public void exportText(Document document, String outfile) throws IOException {
		_exporter.exportText(document, outfile);
	}


	/*******************************************************************
	 * 
	 * SEARCH
	 * 
	 *******************************************************************/

	public SearchResults search(String query) {
		return _searcher.getSearchResults(query, _workingDocument, _allDocuments);
	}


	/*******************************************************************
	 * 
	 * VISION
	 * 
	 *******************************************************************/

	/**
	 * Calls the rerender function of the VisionManager in order to apply
	 * changes made by the user.
	 * 
	 * The GUI should call this function when switching back into view mode.
	 */
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

	/**
	 * Implements horizontal and vertical flipping by
	 * calling into the VisionManager.
	 * 
	 * @param isVertical
	 */
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

	/**
	 * Given a temperature (centered around 0, e.g. -50 to 50), adjust the image.
	 * 
	 * @param temperature
	 */
	public void changeTemperature(int temperature){
		try {
			Parameters.getCoreManager().getWorkingPage().config().setKey(new ConfigurationValue(ConfigurationValue.ValueType.ColorTemperature, temperature));
		} catch (InvalidTypingException e) {
			JOptionPane.showMessageDialog(Parameters.getFrame(),
					"ConfigurationValue threw an InvalidTypingException! Won't be able to change the temperature.",
					"Startup Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void boostConstrast() {
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
		_workingPage.launchOcrThread();
		splitProduct.launchOcrThread();

		Parameters.getPageExpPanel().update();
		this.setProcessedImage(this.getRawImage());
		Parameters.getCoreManager().updateProcessedImageWithRawDimensions();

	}

	/**
	 * Convenience string manipulation function. Given a file name,
	 * returns that name with the extension removed. That is,
	 * "image.jpg" will return "image".
	 * 
	 * @param file - a filename
	 * @return the filename String with the file extension removed.
	 * 
	 * @see getExtension()
	 */
	private String removeExtension(String file) {
		String[] pieces = file.split("[.]");

		String result = "";
		for (int i = 0; i < pieces.length-1; i++) {
			result += pieces[i];
		}

		return result;
	}

	/**
	 * Convenience string manipulation function. Given a file name,
	 * returns the file extension. That is,
	 * "image.jpg" will return "jpg".
	 * 
	 * @param file - a filename
	 * @return the file extension of the filename string passed in.
	 * 
	 * @see removeExtension()
	 */
	private String getExtension(String file) {
		String[] pieces = file.split("[.]");
		return pieces[pieces.length-1];
	}

	/**
	 * Called every time entering Edit Mode or Configuration Dictionary is changed
	 */
	public void getEditImageTransform() {
		_processedImage = VisionManager.imageGlobalTransforms(_rawImage,
				Parameters.getCoreManager().getWorkingPage().config());
		this.updateProcessedImageWithRawDimensions();
	}


}
