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
	
//		// Setup and display the file chooser
//		JFileChooser fileChooser = new JFileChooser();
//		fileChooser.setDialogTitle("Select Workspace");
//		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		fileChooser.setAcceptAllFileFilterUsed(false);
//		int returnVal = fileChooser.showOpenDialog(this);
//
//		// Handle the case where the user closes the file chooser
//		// without choosing the workspace
//		while (returnVal != JFileChooser.APPROVE_OPTION) {
//			JOptionPane.showMessageDialog(this, "Please select a workspace.", "No Workspace Selected", JOptionPane.WARNING_MESSAGE);
//			returnVal = fileChooser.showOpenDialog(this);
//		}
//		
//		// Once the user selects the workspace, its absolute
//		// path is stored
//		File file = fileChooser.getSelectedFile();
//		ParamHolder.setWorkspace(file.getAbsolutePath());
		
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
	public void drawNewImage() {
		this.centralPanel.drawCurrPage();
	}
}
