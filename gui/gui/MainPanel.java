package gui;


import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
	 * Constructor(s)
	 * 
	 ****************************************/

	/**
	 * Constructor.
	 */
	public MainPanel() {
		super();
		this.setLayout(new BorderLayout());
		
		// Setup and display the file chooser
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select Workspace");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int returnVal = fileChooser.showOpenDialog(this);

		// Handle the case where the user closes the file chooser
		// without choosing the workspace
		while (returnVal != JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(this, "Please select a workspace.", "No Workspace Selected", JOptionPane.WARNING_MESSAGE);
			returnVal = fileChooser.showOpenDialog(this);
		}
		
		// Once the user selects the workspace, its absolute
		// path is stored
		File file = fileChooser.getSelectedFile();
		ParamHolder.setWorkspace(file.getAbsolutePath());

		// Setup the central panel
		CentralPanel centralPanel = new CentralPanel();
		this.add(centralPanel, BorderLayout.CENTER);
		
		// Setup the east panel
		EastPanel eastPanel = new EastPanel();
		this.add(eastPanel, BorderLayout.EAST);

		// Setup the west panel
		WestPanel westPanel = new WestPanel(eastPanel.getPageExpPanel(), centralPanel);
		this.add(westPanel, BorderLayout.WEST);
	}
}
