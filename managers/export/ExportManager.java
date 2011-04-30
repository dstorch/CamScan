package export;

import java.io.*;
import core.Parameters;

public class ExportManager implements Exporter {


	
	public void exportToPdf(String document, String outfile) throws IOException {
		Process process = Runtime.getRuntime().exec("python "+Parameters.EXPORT_PATH+" "+document+" "+outfile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		String status = reader.readLine();
		status = status.trim();
		
		if (status.equals("ERROR")) {
			String message = reader.readLine();
			throw new IOException(message);
		} else if (!status.equals("OK")) {
			throw new IOException("Unknown export problem!");
		}
		
		reader.close();
	}
	
}
