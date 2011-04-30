package search;

import java.util.List;

public interface SearchResults {

	public List<SearchHit> inWorkingDoc();
	public List<SearchHit> elsewhere();
	
}
