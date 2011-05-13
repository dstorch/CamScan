package search;

/*******************************************************************
 * Term
 *
 * A "term" packages a word with its original position in the
 * OCR text. This is necessary in order to reconstruct a snippet
 * of text surrounding the matching search terms.
 * 
 * @author dstorch
 * 
 *******************************************************************/

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
