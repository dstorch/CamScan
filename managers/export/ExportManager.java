package export;

import java.io.*;

import core.Document;
import core.Page;
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

	public void exportImages(Document document, String outdirectory) throws IOException {

		// produce the output directory, throwing an exception on failure
		File dir = new File(outdirectory);
		if (!dir.mkdir()) throw new IOException("Aborted export: could not make output directory!");
		
		// copy each of the image files inside the document
		for (Page p : document.pages()) {
		
		    InputStream in = new FileInputStream(p.processed());
		    try {
		    	
		    	// determine the name of the output file
		    	String[] pathpieces = p.processed().split("/");
		    	String outfile = outdirectory+"/"+pathpieces[pathpieces.length - 1];
		    	
			    OutputStream out = new FileOutputStream(outfile);
			    try {
				    // Transfer bytes from in to out
				    byte[] buf = new byte[1024];
				    int len;
				    while ((len = in.read(buf)) > 0) {
				        out.write(buf, 0, len);
				    }
			    } finally {
				    out.close();
			    }
		    } finally {
			    in.close();
		    }
	    
		}
		
	}

	public void exportText(Document document, String outfile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
		try {
			int i = 1;
			for (Page p : document.pages()) {
				writer.write("PAGE "+i+"\n");
				writer.write("==\n");
				writer.write(p.fullText());
				writer.write("\n\n");
				i++;
			}
		} finally {
			writer.flush();
			writer.close();
		}
	}
	
}
