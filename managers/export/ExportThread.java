package export;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import core.Document;
import core.Page;
import core.Parameters;


public class ExportThread extends Thread {

	private Document _document;
	private String _outfile;
	
	public ExportThread(Document document, String outfile) {
		_document = document;
		_outfile = outfile;
	}
	
	public void run() {
		
		BufferedReader reader = null;
		
		try {
			
			 // run OCR on each page---the bottleneck for pdf export!
	        for (Page p : _document.pages()) {
	            p.setOcrResults();
	        }
	
	        // serialize the document, to commit the ocr results to disk
	        _document.serialize();
	
	        String docPath = _document.pathname();
	        
			System.out.println("python "+Parameters.EXPORT_PATH+" "+docPath+" "+_outfile);
			Process process;
		
			process = Runtime.getRuntime().exec("python "+Parameters.EXPORT_PATH+" "+docPath+" "+_outfile);		
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
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
		
		// show an error message if an IOException is thrown
		catch (IOException e) {
			JOptionPane.showMessageDialog(Parameters.getFrame(),
					e.getMessage(),
					"Export Error", JOptionPane.ERROR_MESSAGE);
		}
		
		// make sure to clean up the buffered reader
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}
		
	}
	
}
