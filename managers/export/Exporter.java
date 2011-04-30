package export;

import java.io.IOException;

public interface Exporter {
	public void exportToPdf(String pathname, String outfile) throws IOException;
	
	public static class Factory {
		public static Exporter create() {
			return new ExportManager();
		}
	}
}
