package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.dom4j.DocumentException;

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
		JMenuItem fromFileMenuItem = new JMenuItem("From File");
		fromFileMenuItem.addActionListener(new ImportFromFileListener());
		importMenu.add(fromFileMenuItem);
		
		JMenuItem fromFolderMenuItem = new JMenuItem("From Folder");
		fromFolderMenuItem.addActionListener(new ImportFromFolderListener());
		importMenu.add(fromFolderMenuItem);
		
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
		MainPanel mainPanel = new MainPanel();

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
			System.exit(0); // Exit the program
		}
	}
	
	/**
	 * The ActionListener class for the import from file
	 * menu item.
	 */
	private class ImportFromFileListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * The ActionListener class for the import from folder
	 * menu item.
	 */
	private class ImportFromFolderListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * The ActionListener class for the export as PDF
	 * menu item.
	 */
	private class ExportPDFListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * The ActionListener class for the export as images
	 * menu item.
	 */
	private class ExportImagesListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * The ActionListener class for the export as text
	 * menu item.
	 */
	private class ExportTextListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
		}
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
