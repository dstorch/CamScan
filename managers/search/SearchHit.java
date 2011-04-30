package search;

import core.Page;

public interface SearchHit {

	public Page link();
	public String snippet();
	public float score();
	
	public static class Factory {
		public static SearchHit create(Page link, String snippet, float score) {
			return new SearchHitImpl(link, snippet, score);
		}
	}
	
}
