package search;

import java.util.List;

/*******************************************************************
 * SearchResultsImpl
 *
 * The struct-like class which implements the SeachResults object.
 * 
 * @author dstorch
 * 
 *******************************************************************/

public class SearchResultsImpl implements SearchResults {

	private List<SearchHit> _inWorkingDoc;
	private List<SearchHit> _elsewhere;
	
	public SearchResultsImpl(List<SearchHit> inWorkingDoc, List<SearchHit> elsewhere) {
		_inWorkingDoc = inWorkingDoc;
		_elsewhere = elsewhere;
	}
	
	public List<SearchHit> inWorkingDoc() {
		return _inWorkingDoc;
	}

	public List<SearchHit> elsewhere() {
		return _elsewhere;
	}

}
