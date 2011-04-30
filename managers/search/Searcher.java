package search;

import java.io.IOException;
import java.util.List;
import core.Document;

public interface Searcher {
	public SearchResults getSearchResults(String query, Document workingDocument, List<Document> allDocuments);
	public static class Factory {
		public static Searcher create() throws IOException {
			return new SearchManager();
		}
	}
}
