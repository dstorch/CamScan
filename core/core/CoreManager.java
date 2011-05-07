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
    private BufferedImage _workingImage;
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
			String order = workingPage.attribute("order").getStringValue();
			setWorkingPage(workingStr, Integer.parseInt(order));
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
			tesseract.addAttribute("path", ocrManager.TESS_PATH);
			root.add(tesseract);
			
			if (_workingDocument != null) {
				Element workingdoc = DocumentHelper.createElement("WORKINGDOC");
				workingdoc.addAttribute("value", _workingDocument.pathname());
				root.add(workingdoc);
			}
			
			if (_workingPage != null) {
				Element workingdoc = DocumentHelper.createElement("WORKINGPAGE");
				workingdoc.addAttribute("value", _workingPage.metafile());
				workingdoc.addAttribute("order", new Integer(_workingPage.order()).toString());
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
	
	public void setWorkingPage(String path, int order) throws FileNotFoundException, DocumentException {
		try {
			_workingPage = _xmlReader.parsePage(path, order, _workingDocument);
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
		_workingPage = page;
		_workingImage = page.getRawImgFromDisk();
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
	
	public void deleteDocument(String docName) throws IOException {
		Document toDelete = null;
		for (Document d : _allDocuments) {
			if (docName.equals(d.name())) {
				toDelete = d;
			}
		}
		
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
                                setWorkingPageAndImage(first.pages().get(0));
                            }else{ // there are no Documents
                                _workingDocument = null;
                                _workingPage = null;
                                _workingImage = null;
                            }
                        }
		}

                d.delete();
		_allDocuments.remove(d);
		d = null;
		

		writeStartupFile();
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

        String docPath = Parameters.DOC_DIRECTORY+"/"+doc1+"/";

        for (Page p : d2.pages()) {
            // extract name of file and append to path of document 1 to get new path
            String[] s = p.metafile().split("/");
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
		String directory = Parameters.DOC_DIRECTORY + "/" + name;
		File dirFile = new File(directory);
		if (!dirFile.mkdir()) throw new IOException("Import aborted: problem making new document directory!");
		String pathname = directory + "/" + "doc.xml";
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
            	if (filename.endsWith(Parameters.imgExtensions[i])) validExt = true;
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
                Page p = new Page(d, order);

                // set pathname attributes of the page
                p.setRawFile(targetLocation.getPath());
                p.setProcessedFile(Parameters.PROCESSED_DIRECTORY + "/" + sourceLocation.getName());
                p.setMetafile(Parameters.DOC_DIRECTORY + "/" + d.name() + "/" + noExt + ".xml");

                // guess initial configuration values
                //p.initGuesses();
                d.addPage(p);

                // do OCR!
                launchOcrThread(p);
            }

        }

    }

    private void launchOcrThread(Page page) {
        OCRThread t = new OCRThread(page);
        t.start();
    }

    // called when the user imports a single photograph
    // as a document
    public Document createDocumentFromFile(File sourceLocation) throws IOException {

        // put this document in workspace/docs by default
        // get the image file name without a ".tiff" extension
        String imageFile = sourceLocation.getName();
        String noExt = imageFile.substring(0, imageFile.length() - 5);
        String directory = Parameters.DOC_DIRECTORY + "/" + noExt;
        File dirFile = new File(directory);
        if (!dirFile.mkdir()) {
            throw new IOException("Import aborted: problem making new document directory!");
        }
        String pathname = directory + "/" + "doc.xml";
        Document newDoc = new Document(noExt, pathname);

        File targetLocation = new File(Parameters.RAW_DIRECTORY + "/" + sourceLocation.getName());
        importPages(sourceLocation, targetLocation, newDoc, 0);

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
            System.out.println(hit.snippet() + " " + hit.score());
        }
        System.out.println("In all other pages: ");
        for (SearchHit hit : results.elsewhere()) {
            System.out.println(hit.snippet() + " " + hit.score());
        }

        return results;
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

        for (Document doc : _allDocuments) {
            if (docName.equals(doc.name())) {
                _workingDocument = doc;
                setWorkingPageAndImage(doc.pages().get(0));
            }
        }
    }
    
    public void setWorkingDocument(Document doc) {
    	_workingDocument = doc;
    }

    public Page getWorkingPage() {
		return _workingPage;
	}
    
    public BufferedImage getWorkingImage() {
    	return _workingImage;
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
    public void updateWorkingImage() {
        Page curr = getWorkingPage();
        BufferedImage img = getWorkingImage();
        if (curr != null && img != null) {
        	_processedImage = VisionManager.rerenderImage(getWorkingImage(), curr.corners(), curr.config());
        }
    }
//
//	// called when user tries to place corner; tries to make a better point given the user's guess
//    // writes the current process image to workspace/processed (as Tiff file)
//    public void writeProcessedTiff() {
//        String[] s = Parameters.getCoreManager().getWorkingPage().metafile().split("/");
//        String path = "workspace/processed/" + s[s.length - 1] + ".tiff";
//
//        VisionManager.writeTIFF(Parameters.getCurrPageImg(), path);
//    }
//
//    // writes the current process image to workspace/processed (as PNG file)?
//    public void writeProcessedFile() throws IOException {
//        String[] s = Parameters.getCoreManager().getWorkingPage().metafile().split("/");
//        String path = "workspace/processed/" + s[s.length - 1] + ".png";
//
//        Page curr = Parameters.getCoreManager().getWorkingPage();
//        VisionManager.outputToFile(Parameters.getCurrPageImg(), path, curr.corners(), curr.config());
//    }
//
//    // Called every time entering Edit Mode or Configuration Dictionary is changed
//    public void getEditImageTransform() {
//        Parameters.setCurrPageImg(VisionManager.imageGlobalTransforms(Parameters.getCurrPageImg(),
//        		Parameters.getCoreManager().getWorkingPage().config()));
//    }
//
//    // sets corners and config file for the initial guesses of an imported document
//    private void initGuesses(Document d) throws IOException {
//        for (Page p : d.pages()) {
//            BufferedImage buff = ImageIO.read(new File(p.raw()));
//            // guess and set corners and configuration values of Page
//            p.setCorners(VisionManager.findCorners(buff));
//            p.setConfig(VisionManager.estimateConfigurationValues(buff));
//        }
//    }
//
//    // called when user tries to place corner; tries to make a better point given the user's guess
//    public Point snapCorner(Point pt) {
//        return VisionManager.snapCorner(Parameters.getCurrPageImg(), pt);
//    }

    // not the main method for the application,
    // just used for testing the core and integrating
    // components independent of the GUI
    public static void main(String[] args) throws DocumentException, IOException {
        CoreManager core = new CoreManager();
        core.createDocumentFromFile(new File("tests/images/1col-300.tiff"));
        core.createDocumentFromFile(new File("tests/images/mexican_war_text.jpg"));
        //core.createDocumentFromFile(new File("../sample2.tiff"));
        //core.setWorkingDocumentFromName("sample_page");
        //core.exportToPdf("workspace/docs/sample2/doc.xml", "../foo.pdf");
        //core.exportText(core.workingDocument(), "../document.txt");
        //core.exportImages(core.workingDocument(), "../copiedDoc");
        //core.search("political situation");
        //core.renameDocument("sample2", "sample_page_2");
        //core.deleteDocument("sample_page_2");
        //core.closeWorkingDocument();
        //core.shutdown();
    }
}
