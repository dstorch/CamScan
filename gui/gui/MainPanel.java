package gui;


import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dom4j.DocumentException;

import core.CoreManager;
import core.Document;
import core.Parameters;

import westwidget.WestPanel;

import centralwidget.CentralPanel;
import eastwidget.EastPanel;


/**
 * Sets up the GUI.
 * 
 * @author Stelios Anagnostopoulos (sanagnos)
 *
 */
public class MainPanel extends JPanel {
	
	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * The Central Panel.
	 */
	private CentralPanel centralPanel;

	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/

	/**
	 * Constructor.
	 * 
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public MainPanel() throws DocumentException, IOException {
		super();
		this.setLayout(new BorderLayout());
		
		Parameters.setCoreManager(new CoreManager());

		// Setup the central panel
		this.centralPanel = new CentralPanel();
		this.add(this.centralPanel, BorderLayout.CENTER);
		
		// Setup the east panel
		EastPanel eastPanel = new EastPanel(this);
		this.add(eastPanel, BorderLayout.EAST);

		// Setup the west panel
		WestPanel westPanel = new WestPanel(eastPanel.getPageExpPanel(), this.centralPanel);
		this.add(westPanel, BorderLayout.WEST);
	}
	
	/**
	 * To be called one the current page has changed.
	 * It updates the UI to show that page.
	 */
	public void updateCentralPanels() {
		this.centralPanel.updatePanels();
	}
}
