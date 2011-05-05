package westwidget;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import search.SearchResults;

import core.Parameters;

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
	
	/**
	 * The text field.
	 */
	private JTextField searchTextField;
	
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
		this.searchTextField = new JTextField();
		this.add(this.searchTextField);
		
		// Setup the search button.
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		this.add(searchButton);
		
		// TODO: Binding enter to the search button. Dunno why it doesn't work
		searchButton.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search");
		searchButton.getActionMap().put("search", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
		       System.out.println("OK");
		    }
		});
		
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
		SearchResults results = Parameters.getCoreManager().search(this.searchTextField.getText());
		Parameters.setSearchResults(results);
		this.centralPanel.updatePanels(true);
	}
}
