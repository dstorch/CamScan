package search;

import java.util.List;

/*******************************************************************
 * SearchResults
 *
 * The interface by which the core accesses the SearchResults
 * 
 * @author dstorch
 * 
 *******************************************************************/

public interface SearchResults {

	public List<SearchHit> inWorkingDoc();
	public List<SearchHit> elsewhere();
	
}
