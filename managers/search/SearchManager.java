package search;

import java.io.*;
import java.util.*;

import org.dom4j.DocumentException;

import core.*;

/*******************************************************************
 * SearchManager
 *
 * Implements the Searcher's public interface.
 * 
 * @author dstorch
 * 
 *******************************************************************/

public class SearchManager implements Searcher {
	
	private Stemmer _stemmer;
	private Set<String> _stopWords;
	
	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public SearchManager() throws IOException {
		_stemmer = new Stemmer();
		_stopWords = new HashSet<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(Parameters.STOP_WORDS));
		
		String stopWord = "";
		while ((stopWord = reader.readLine()) != null) {
			_stopWords.add(stopWord.trim());
		}
	}
	
	/**
	 * The top-level search method.
	 * 
	 * @param query - the String passed as the search query by the user
	 * @param workingDocument - the Document which the user was viewing when s/he clicked search
	 * @param allDocuments - the list of all documents in the library
	 * 
	 * @return a SearchResults object resulting from the search
	 */
	@SuppressWarnings("unused")
	public SearchResults getSearchResults(String query, Document workingDocument, List<Document> allDocuments) {
		List<Term> queryNew = sanitize(query);
		
		// query should be treated as a set of terms
		HashSet<Term> querySet = new HashSet<Term>();
		for (Term t : queryNew) {
			querySet.add(t);
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
		
		// add search hits to a priority queue
		PriorityQueue<SearchHit> pq = new PriorityQueue<SearchHit>();
		pq.addAll(inWorkingDoc);
		
		// get the top results by repeatedly dequeueing from the priority queue
		LinkedList<SearchHit> topInWorkingDoc = new LinkedList<SearchHit>();
		int i = 0;
		for (SearchHit hit : inWorkingDoc) {
			if (i < Parameters.RESULTS_INDOC) {
				topInWorkingDoc.add(pq.poll());
				i++;
			} else {
				break;
			}
		}
		
		// clear the priority queue, and repeat the process for
		// hits outside of the working page
		pq.clear();
		pq.addAll(elsewhere);
		
		// get the top results
		LinkedList<SearchHit> topElsewhere = new LinkedList<SearchHit>();
		i = 0;
		for (SearchHit hit : elsewhere) {
			if (i < Parameters.RESULTS_ELSEWHERE) {
				topElsewhere.add(pq.poll());
				i++;
			} else {
				break;
			}
		}
		
		return new SearchResultsImpl(topInWorkingDoc, topElsewhere);
	}
	
	
	/**
	 * Removes stop words and applies Porter stemming.
	 * This makes the search results more flexible, and less
	 * dependent on small, unimportant words and part of speech.
	 */
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

	
	
	public void test() throws FileNotFoundException, DocumentException{
		SearchResults results;
		XMLReader r = new XMLReader();
		Document doc = r.parseDocument("tests/search-tests/searchtest1");
		String term = "death march";
		
		results = getSearchResults(term, doc, new LinkedList<Document>());
		System.out.println("Searched for: " + term);
		for(SearchHit snip: results.inWorkingDoc()){
			System.out.println(snip.snippet());
		}
		doc = r.parseDocument("tests/search-tests/searchtest2");
		term = "prosecute";
		results = getSearchResults(term, doc, new LinkedList<Document>());
		System.out.println("Searched for: " + term);
		for(SearchHit snip: results.inWorkingDoc()){
			System.out.println(snip.snippet());
		}
		doc = r.parseDocument("tests/search-tests/searchtest3");
		term = "pale as death";
		results = getSearchResults(term, doc, new LinkedList<Document>());
		System.out.println("Searched for: " + term);
		for(SearchHit snip: results.inWorkingDoc()){
			System.out.println(snip.snippet());
		}
	}
}
