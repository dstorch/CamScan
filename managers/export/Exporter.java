package export;

import java.io.IOException;

import core.Document;

public interface Exporter {
	
	public void exportToPdf(Document document, String outfile) throws IOException;
	
	public void exportImages(Document document, String outdirectory) throws IOException;
	
	public void exportText(Document document, String outfile) throws IOException;
	
	public static class Factory {
		public static Exporter create() {
			return new ExportManager();
		}
	}
}
