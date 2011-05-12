package search;

import core.Page;

/*******************************************************************
 * SearchHit
 *
 * The interface by which the core of the system accesses search
 * hits.
 * 
 * @author dstorch
 * 
 *******************************************************************/

@SuppressWarnings("rawtypes")
public interface SearchHit extends Comparable {

	public Page link();
	public String snippet();
	public float score();
	
	public static class Factory {
		public static SearchHit create(Page link, String snippet, float score) {
			return new SearchHitImpl(link, snippet, score);
		}
	}
	
}
