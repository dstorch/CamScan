package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

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
	
	private JFrame app;

	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/

	/**
	 * Constructor.
	 */
	public App() {

		/*
		 * Setup the JFrame
		 */
		super("CamScan");
		this.app = this;
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Setup the menu bar
		JMenuBar menuBar = new JMenuBar();

		// Setup the file menu
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		// Setup the menu items for the file menu
		JMenuItem quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.addActionListener(new QuitListener());
		fileMenu.add(quitMenuItem);

		// Setup the about menu
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		// Setup the menu items for the file menu
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new AboutListener());
		helpMenu.add(aboutMenuItem);

		// Assign the menu bar to this JFrame
		this.setJMenuBar(menuBar);

		// Instantiate the main panel
		MainPanel mainPanel = new MainPanel();

		// Add the panel to the frame
		this.add(mainPanel);
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
