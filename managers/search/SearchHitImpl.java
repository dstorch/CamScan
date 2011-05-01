package search;

import core.Page;

public class SearchHitImpl implements SearchHit {

	private Page _link;
	private String _snippet;
	private float _score;
	
	public SearchHitImpl(Page link, String snippet, float score) {
		_link = link;
		_snippet = snippet;
		_score = score;
	}
	
	public Page link() {
		return _link;
	}

	public String snippet() {
		return _snippet;
	}
	
	public float score() {
		return _score;
	}

	public int compareTo(Object o) {
		SearchHit hit = (SearchHit) o;
		Float score = new Float(_score);
		return score.compareTo(hit.score()) * -1;
	}

	
	
}
