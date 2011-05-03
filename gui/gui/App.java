package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.dom4j.DocumentException;

import core.Document;
import core.Parameters;

/**
 * The top-level class that contains the
 * main line.
 * 
 * @author Stelios
 *
 */
public class App extends JFrame {

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * Reference to the App class.
	 */
	private JFrame app;
	

	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/

	/**
	 * Constructor.
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public App() throws DocumentException, IOException {

		/*
		 *  Setup the JFrame
		 */
		super("CamScan");
		this.setLayout(new BorderLayout());
		this.app = this;
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Setup the menu bar
		JMenuBar menuBar = new JMenuBar();

		/*
		 * The File Menu
		 */
		
		// Setup the file menu
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		// Setup the menu items for the file menu
		JMenuItem quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.addActionListener(new QuitListener());
		fileMenu.add(quitMenuItem);
		
		/*
		 * The Import Menu
		 */
		
		// Setup the import menu
		JMenu importMenu = new JMenu("Import");
		menuBar.add(importMenu);
		
		// Setup the menu items for the import menu
		JMenuItem fromFileMenuItem = new JMenuItem("From File or Folder");
		fromFileMenuItem.addActionListener(new ImportListener());
		importMenu.add(fromFileMenuItem);
		
		
		/*
		 * The Export Menu
		 */
		
		// Setup the export menu
		JMenu exportMenu = new JMenu("Export");
		menuBar.add(exportMenu);
		
		// Setup the menu items for the export menu
		JMenuItem pdfMenuItem = new JMenuItem("PDF");
		pdfMenuItem.addActionListener(new ExportPDFListener());
		exportMenu.add(pdfMenuItem);
		
		JMenuItem imagesMenuItem = new JMenuItem("Images");
		imagesMenuItem.addActionListener(new ExportImagesListener());
		exportMenu.add(imagesMenuItem);
		
		JMenuItem textMenuItem = new JMenuItem("Extracted Text");
		textMenuItem.addActionListener(new ExportTextListener());
		exportMenu.add(textMenuItem);

		/*
		 * The About Menu
		 */
		
		// Setup the about menu
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		// Setup the menu items for the file menu
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new AboutListener());
		helpMenu.add(aboutMenuItem);

		/*
		 * Setup the JFrame
		 */
		
		// Assign the menu bar to this JFrame
		this.setJMenuBar(menuBar);

		// Instantiate the main panel
		MainPanel mainPanel = null;
		try {
			mainPanel = new MainPanel();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(app, e.getMessage(),
					"Startup Warning", JOptionPane.WARNING_MESSAGE);
		}

		// Add the panel to the frame
		this.add(mainPanel, BorderLayout.CENTER);
		this.pack();
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
	}

	/****************************************
	 * 
	 * Mainline
	 * 
	 ****************************************/

	/**
	 * The main-line.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new App();
	}

	/****************************************
	 * 
	 * Private Classes
	 * 
	 ****************************************/

	/**
	 * The ActionListener class for the quit menu item.
	 */
	private class QuitListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			try {
				Parameters.getCoreManager().shutdown();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0); // Exit the program
		}
	}
	
	/**
	 * The ActionListener class for the import from folder
	 * menu item.
	 */
	private class ImportListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {

			File file = getUserSelectionForImport();
			
			// if cancelled, then break out of this method
			if (file == null) return;
			
			try {
				if (file.isDirectory()) {
					System.out.println(file.getAbsolutePath());
					Parameters.getCoreManager().createDocumentFromFolder(file);
				} else if (file.isFile()) {
					Parameters.getCoreManager().createDocumentFromFile(file);
				} else {
					throw new IOException("Unrecognized object selected");
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(app, e.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * The ActionListener class for the export as PDF
	 * menu item.
	 */
	private class ExportPDFListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			
			File folder = getUserFileForExport(Parameters.pdfExtensions);
			
			// if cancelled, then break out of this method
			if (folder == null) return;
			
			Document workingDocument = Parameters.getCoreManager().workingDocument();
			if (workingDocument == null) return;
			
			String workingPath = workingDocument.pathname();
			
			// build the path for the output file
			String outpath = folder.getPath();
			String lowerpath = outpath.toLowerCase();
			if (!lowerpath.endsWith(".pdf")) outpath += ".pdf";
			
			try {
				Parameters.getCoreManager().exportToPdf(workingPath, outpath);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(app, e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}
	}
	

	/**
	 * The ActionListener class for the export as images
	 * menu item.
	 */
	private class ExportImagesListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {

			File folder = getUserDirectoryForExport();
			
			// if cancelled, then break out of this method
			if (folder == null) return;
			
			Document workingDocument = Parameters.getCoreManager().workingDocument();
			if (workingDocument == null) return;
			
			// get the path for the output file
			String outpath = folder.getPath();
			String lowerpath = outpath.toLowerCase();
			if (!lowerpath.endsWith(".txt") && !lowerpath.endsWith(".text") )
				outpath += ".txt";
			
			try {
				Parameters.getCoreManager().exportImages(workingDocument, outpath);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(app, e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * The ActionListener class for the export as text
	 * menu item.
	 */
	private class ExportTextListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			File folder = getUserFileForExport(Parameters.txtExtensions);
			
			Document workingDocument = Parameters.getCoreManager().workingDocument();
			if (workingDocument == null) return;
			
			String outpath = folder.getPath();
			
			try {
				Parameters.getCoreManager().exportText(workingDocument, outpath);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(app, e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	
	private File getUserDirectoryForExport() {

		JFileChooser fc = new JFileChooser();
		int status = fc.showSaveDialog(app);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else if (status == JFileChooser.CANCEL_OPTION) {
			return null;
		} else if (status == JFileChooser.ERROR_OPTION) {
			return null;
		}
		return null;
	}
	
	/**
	 * This method defines the interface by which the user selects
	 * directories for import and export.
	 * @see getUserSelection()
	 * 
	 * @return a File representing the directory selected by the user
	 */
	private File getUserFileForExport(String[] extensions) {

		FileFilter ff = new ExtensionFileFilter(extensions);
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(ff);
		int status = fc.showSaveDialog(app);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else if (status == JFileChooser.CANCEL_OPTION) {
			return null;
		} else if (status == JFileChooser.ERROR_OPTION) {
			return null;
		}
		return null;
	}
	
	/**
	 * This method defines the interface by which the user selects
	 * files OR directories for import and export.
	 * @see getUserDirectory()
	 * 
	 * @return a File representing the directory selected by the user
	 */
	private File getUserSelectionForImport() {

		FileFilter ff = new ExtensionFileFilter(Parameters.imgExtensions);
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(ff);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int status = fc.showOpenDialog(app);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else if (status == JFileChooser.CANCEL_OPTION) {
			return null;
		} else if (status == JFileChooser.ERROR_OPTION) {
			return null;
		}
		return null;
	}
	
	/**
	 * The ActionListener class for the about menu item.
	 */
	private class AboutListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			JOptionPane.showMessageDialog(app,
					"<html> CamScan, version 0.1 <br><br> " +
					"Copyright 2011. All rights reserved. <br><br>" +
					"The program is provided AS IS with NO WARRANTY OF ANY KIND, <br>" +
					"INCLUDING THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS <br>" +
					"FOR A PARTICULAR PURPOSE.</html>",
					"About CamScan",
					JOptionPane.PLAIN_MESSAGE);
		}	
	}
}
