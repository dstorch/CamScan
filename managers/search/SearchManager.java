package search;

import java.io.*;
import java.util.*;
import core.*;

public class SearchManager implements Searcher {
	
	
	public SearchResults getSearchResults(String query, Document workingDocument, List<Document> allDocuments) {
		String[] queryNew = sanitizeQuery(query);
		
		List<SearchHit> inWorkingDoc = workingDocument.search(queryNew);
		
		LinkedList<SearchHit> elsewhere = new LinkedList<SearchHit>();
		for (Document d : allDocuments) {
			if (!d.equals(workingDocument)) {
				elsewhere.addAll(d.search(queryNew));
			}
		}
		
		return new SearchResultsImpl(inWorkingDoc, elsewhere);
	}
	
	
	public static void main(String[] args) throws IOException {
		Searcher s = Searcher.Factory.create();
		
	}
	
	private String[] sanitizeQuery(String query) {
		String qlower = query.toLowerCase();
		String[] queryNew = qlower.split("[^a-z0-9]+");
		return queryNew;
	}
	
	public static String[] sanitizeFullText(String fullText) {
		String fullLower = fullText.toLowerCase();
		String[] fullNew = fullLower.split("[^a-z0-9]+");
		return fullNew;
	}
	

}
