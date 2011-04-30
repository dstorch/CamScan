package search;

public class Term {
	
	public String word;
	public int pos;
	
	public Term(String word, int position) {
		this.word = word;
		this.pos = position;
	}
	
	@Override
	public boolean equals(Object o) {
		Term other = (Term) o;
		return other.word.equals(this.word);
	}
	
	@Override
	public int hashCode() {
		return this.word.hashCode();
	}

}
