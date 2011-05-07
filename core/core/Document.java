package core;

import java.io.*;
import java.util.*;
import org.dom4j.*;
import org.dom4j.io.*;
import search.*;

public class Document {

	private SortedSet _pages;
	private String _name;
	private String _pathname;
	
	public Document() {
		_pages = new TreeSet<Page>();
	}
	
	public Document(String name, String pathname) {
		_pages = new TreeSet<Page>();
		_name = name;
		_pathname = pathname;
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
	public SortedSet<Page> pages() {
		return _pages;
	}
	
	// i.e. workspace/docs/mydoc/doc.xml
	public String pathname() {
		return _pathname;
	}
	
	public void rename(String newName) throws IOException {
		
		// change the name of the directory on disk
		String newPath = Parameters.DOC_DIRECTORY+"/"+newName;
		String oldPath = pathname().substring(0, pathname().length()-8);
		
		
		File oldDir = new File(oldPath);
		File newDir = new File(newPath);
		System.out.println("old directory: "+oldDir.getPath());
		System.out.println("new directory: "+newDir.getPath());
		if (!oldDir.renameTo(newDir)) throw new IOException("Could not rename document!");
		
		// set instance variables
		setName(newName);
		setPathName(newDir.getPath()+"/doc.xml");
		
		// set instance variables of the contained page objects
		for (Page p : pages()) {
			
			// get the name of the metafile (not the complete pathname)
			String oldMetafile = p.metafile();			
			String[] pathfields = oldMetafile.split("/");
			String name = pathfields[pathfields.length-1];
			
			// set path name variable for each page metafile
			p.setMetafile(Parameters.DOC_DIRECTORY+"/"+newName+"/"+name);
		}

		
	}
	
	// WARNING: recursively deletes all directory contents
	public boolean deleteDir(File dir) {
	    
		// if directory, then recur on children
		if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            
	            // short circuit if recursive deletion fails
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}
	
	public void delete() throws IOException {
		File docDirectory = new File(Parameters.DOC_DIRECTORY+"/"+name());
		if (!deleteDir(docDirectory)) throw new IOException("Problem deleting the document!");

                // delete all image files in raw directory (AND PROCESSED DIRECTORY?????)
                for (Page p : pages()) {
                    p.deleteRawFile();
                }
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
	
	public List<SearchHit> search(Set<Term> query, Searcher searcher) {
		LinkedList<SearchHit> hits = new LinkedList<SearchHit>();
		for (Page p : pages()) {
			hits.addAll(p.search(query, searcher));
		}
		return hits;
	}
        
        public boolean equals(Document d){
            return (d.name().equals(this.name()));
        }

        // deletes Page from list and all references
        public void deletePage(Page p){
            int index = p.order();
            
        }
	
}
