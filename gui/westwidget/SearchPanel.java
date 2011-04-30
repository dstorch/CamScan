package westwidget;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import centralwidget.CentralPanel;

/**
 * This panel contains the search box.
 * 
 * @author Stelios
 *
 */
public class SearchPanel extends JPanel implements ActionListener {

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * Reference to the central panel.
	 */
	private CentralPanel centralPanel;
	
	/****************************************
	 * 
	 * Constructors.
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 * 
	 * @param centralPanel Reference to the central panel
	 */
	public SearchPanel(CentralPanel centralPanel) {
		super();
		this.setLayout(new GridLayout(4, 1));
		this.centralPanel = centralPanel;
		
		// Setup the search label.
		JLabel searchLabel = new JLabel("Search: ");
		this.add(searchLabel);
		
		// Setup the search text field.
		JTextField searchTextField = new JTextField();
		this.add(searchTextField);
		
		// Setup the search button.
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		this.add(searchButton);
		
		// Setup a dummy label to leave some space below
		// the search panel.
		JLabel dummyLabel = new JLabel();
		this.add(dummyLabel);
	}
	
	/****************************************
	 * 
	 * Misc Methods
	 * 
	 ****************************************/

	/**
	 * Handles the searching. Called when the search
	 * button is clicked.
	 */
	public void actionPerformed(ActionEvent arg0) {
		this.centralPanel.switchToSearchResultsPanel();
	}
}
