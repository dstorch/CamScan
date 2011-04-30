package search;

import java.io.*;
import java.util.*;
import core.*;

public class SearchManager implements Searcher {
	
	private Stemmer _stemmer;
	private Set<String> _stopWords;
	
	public SearchManager() throws IOException {
		_stemmer = new Stemmer();
		_stopWords = new HashSet<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(Parameters.STOP_WORDS));
		
		String stopWord = "";
		while ((stopWord = reader.readLine()) != null) {
			_stopWords.add(stopWord.trim());
		}
	}
	
	
	public SearchResults getSearchResults(String query, Document workingDocument, List<Document> allDocuments) {
		List<Term> queryNew = sanitize(query);
		
		// query should be treated as a set of terms
		HashSet<Term> querySet = new HashSet<Term>();
		for (Term t : queryNew) {
			querySet.add(t);
		}
		
		// REMOVE WHEN READY!
		for (Term t : querySet) {
			System.out.println(t.word+" "+t.pos);
		}
		
		// find hits in the working document
		List<SearchHit> inWorkingDoc = workingDocument.search(querySet, this);
		
		// find hits everywhere else, and concatenate them all
		LinkedList<SearchHit> elsewhere = new LinkedList<SearchHit>();
		for (Document d : allDocuments) {
			if (!d.equals(workingDocument)) {
				elsewhere.addAll(d.search(querySet, this));
			}
		}
		
		// REMOVE WHEN READY!
		for (SearchHit hit : inWorkingDoc) {
			System.out.println(hit.snippet()+" "+hit.score());
		}
		
		return new SearchResultsImpl(inWorkingDoc, elsewhere);
	}
	
	public List<Term> sanitize(String text) {
		String textlower = text.toLowerCase();
		String[] textNew = textlower.split("[^a-z0-9]+");
		
		// remove stop words
		ArrayList<Term> noStopWords = new ArrayList<Term>();
		for (int i = 0; i < textNew.length; i++) {
			if (!_stopWords.contains(textNew[i])) {
				noStopWords.add(new Term(textNew[i], i));
			}
		}
		
		// apply Porter Stemming algorithm using Stemmer class
		ArrayList<Term> stemmed = new ArrayList<Term>();
		for (Term t : noStopWords) {
			stemmed.add(new Term(_stemmer.stemWord(t.word), t.pos));
		}
		
		return stemmed;
	}

}
