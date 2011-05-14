package core;

import java.io.*;
import java.util.*;
import org.dom4j.*;
import org.dom4j.io.*;
import search.*;

/*******************************************************************
 * Document
 *
 * Represents all metadata about a scanned document. This class
 * does not contain the images themselves, because that would
 * require too much memory---the images are kept on disk.
 * 
 * @author dstorch, mmicalle
 * 
 *******************************************************************/

public class Document {

	/*******************************************************************
	 * 
	 * PRIVATE INSTANCE VARIABLES
	 * 
	 *******************************************************************/
	
	private SortedSet<Page> _pages;
	private String _name;
	private String _pathname;

	/*******************************************************************
	 * 
	 * CONSTRUCTORS
	 * 
	 *******************************************************************/
	
	public Document() {
		_pages = new TreeSet<Page>();
	}

	public Document(String name, String pathname) {
		_pages = new TreeSet<Page>();
		_name = name;
		_pathname = pathname;
	}
	
	/*******************************************************************
	 * 
	 * GETTERS
	 * 
	 *******************************************************************/
	
	public String name() {
		return _name;
	}
	public SortedSet<Page> pages() {
		return _pages;
	}

	// i.e. workspace/docs/mydoc/doc.xml
	public String pathname() {
		return _pathname;
	}

	
	/*******************************************************************
	 * 
	 * SETTERS
	 * 
	 *******************************************************************/

	public void setName(String name) {
		_name = name;
	}
	public void setPathName(String pathname) {
		_pathname = pathname;
	}

	public void addPage(Page p) {
		_pages.add(p);
	}
	
	/*******************************************************************
	 * 
	 * PUBLIC METHODS
	 * 
	 *******************************************************************/
	
	public boolean equals(Document d){
		return (d.name().equals(this.name()));
	}
	
	/**
	 * Called from the rename method of the CoreManager, this
	 * method changes the name of this document. After changing
	 * the name, the document is re-serialized so that the
	 * changes are reflected on disk.
	 * 
	 * @param newName - the new name to assign to the document
	 * @throws IOException
	 */
	public void rename(String newName) throws IOException {

		// change the name of the directory on disk
		String newPath = Parameters.DOC_DIRECTORY+File.separator+newName;
		String oldPath = pathname().substring(0, pathname().length()-8);


		File oldDir = new File(oldPath);
		File newDir = new File(newPath);
		if (!oldDir.renameTo(newDir)) throw new IOException("Could not rename document!");

		// set instance variables
		setName(newName);
		setPathName(newDir.getPath()+File.separator+"doc.xml");

		// move metadata files of page objects into correct directory
		for (Page p : pages()) {

			// get the name of the metafile (not the complete pathname)
			String oldMetafile = p.metafile();			
			String[] pathfields = oldMetafile.split(File.separator);
			String name = pathfields[pathfields.length-1];

			// set path name variable for each page metafile
			p.setMetafile(Parameters.DOC_DIRECTORY+File.separator+newName+File.separator+name);
		}


	}

	/**
	 * Deletes this page.
	 * 
	 * @throws IOException
	 */
	public void delete() throws IOException {
		File docDirectory = new File(Parameters.DOC_DIRECTORY+File.separator+name());
		if (!IOFunctions.deleteDir(docDirectory)) throw new IOException("Problem deleting the document!");


		// delete all image files in raw directory (AND PROCESSED DIRECTORY?????)
		for (Page p : pages()) {
			p.deleteRawFile();
			p.deleteProcessedFile();
		}

	}

        /**
         * Deletes documents directory -- used in merging
         * @throws IOException
         */
        public void deleteOnlyDirectory() throws IOException{
            File docDirectory = new File(Parameters.DOC_DIRECTORY+File.separator+name());
            if (!IOFunctions.deleteDir(docDirectory)) throw new IOException("Problem deleting the document!");
        }

	/**
	 * Creates a new dom4j document, and writes it to disk.
	 * The resulting XML contains all of the instance variables
	 * from inside this document.
	 */
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
				pageEl.addAttribute("name", p.name());
				pages.add(pageEl);
			}

			filewriter.write(xmlDoc);
		} finally {
			filewriter.close();
		}

	}

	/**
	 * The primary function for document search, called by
	 * the searching module.
	 * 
	 * @param query - a String, the search query
	 * @param searcher - the Searcher object which is performing
	 * the searching
	 * 
	 * @return a list of SearchHit objects resulting from searching
	 * this document with the given query
	 */
	public List<SearchHit> search(Set<Term> query, Searcher searcher) {
		LinkedList<SearchHit> hits = new LinkedList<SearchHit>();
		for (Page p : pages()) {
			hits.addAll(p.search(query, searcher));
		}
		return hits;
	}

	/**
	 * Deletes the Page from disk and eliminates all references
	 * to that page.
	 * 
	 * @param p - the Page to delete
	 * @throws IOException
	 */
	public void deletePage(Page p) throws IOException{
		int index = p.order();
		_pages.remove(p);
		// update orders of all pages below deleted page
		for (Page page : pages()) {
			if(page.order()>index) page.setOrder(page.order() - 1);
		}
		p.deleteMetadataFile();
		p.deleteProcessedFile();
		p.deleteRawFile(); 
		serialize();
	}

	/**
	 * Change the order of pages.
	 * 
	 * @param p - the Page to reorder
	 * @param newOrder - the new page number
	 * @throws IOException
	 */
	public void reorderPage(Page p, int newOrder) throws IOException{
		int oldOrder = p.order();

		if(oldOrder<newOrder){ // moving a Page down
			for (Page page : pages()) {
				if((page.order()<=newOrder)&&(page.order()>oldOrder)){
					page.setOrder(page.order() + 1);
				}
			}
		}else{ // moving a Page up
			for (Page page : pages()) {
				if((page.order()>=newOrder)&&(page.order()<oldOrder)){
					page.setOrder(page.order() + 1);
				}
			}
		}

		p.setOrder(newOrder);

		updateList();
		serialize();

	}
	
	/*******************************************************************
	 * 
	 * PRIVATE METHODS
	 * 
	 *******************************************************************/

	/**
	 * Update the list of pages to make sure that it stays sorted.
	 */
	private void updateList(){
		TreeSet<Page> temp = new TreeSet<Page>();
		for (Page page : pages()) {
			temp.add(page);
		}
		_pages = temp;
	}

}
