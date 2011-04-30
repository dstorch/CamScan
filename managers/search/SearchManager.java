package search;

import java.util.*;
import core.*;

public class SearchManager implements Searcher {
	
	private Stemmer _stemmer;
	
	public SearchManager() {
		_stemmer = new Stemmer();
	}
	
	
	public SearchResults getSearchResults(String query, Document workingDocument, List<Document> allDocuments) {
		String[] queryNew = sanitize(query);
		
		List<SearchHit> inWorkingDoc = workingDocument.search(queryNew);
		
		LinkedList<SearchHit> elsewhere = new LinkedList<SearchHit>();
		for (Document d : allDocuments) {
			if (!d.equals(workingDocument)) {
				elsewhere.addAll(d.search(queryNew));
			}
		}
		
		return new SearchResultsImpl(inWorkingDoc, elsewhere);
	}
	
	private String[] sanitize(String text) {
		String textlower = text.toLowerCase();
		String[] textNew = textlower.split("[^a-z0-9]+");
		
		// remove stop words
		
		// apply Porter Stemming algorithm using Stemmer class
		for (int i = 0; i < textNew.length; i++) {
			textNew[i] = _stemmer.stemWord(textNew[i]);
		}
		
		return textNew;
	}
	
	public static String[] sanitizeFullText(String fullText) {
		String fullLower = fullText.toLowerCase();
		String[] fullNew = fullLower.split("[^a-z0-9]+");
		return fullNew;
	}
	

}
