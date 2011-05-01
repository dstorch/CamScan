package centralwidget;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * The Toolbal Panel contains basic tools for the Central Panel,
 * such as switching mode tools, going back and forth between images,
 * etc.
 * 
 * @author Stelios
 *
 */
public class ToolbarPanel extends JPanel {
	
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
	 * The mode button group.
	 */
	private ButtonGroup modeButtonGroup;
	
	/****************************************
	 * 
	 * Constants
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public ToolbarPanel(CentralPanel centralPanel) {
		super();
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		//this.setBackground(Color.GRAY);
		this.centralPanel = centralPanel;
		
		c.gridx = 0;
		JButton backButton = new JButton("Back");
		this.add(backButton, c);
		
		c.gridx = 1;
		JButton nextButton = new JButton("Next");
		this.add(nextButton, c);
		
		// Setup the mode listener for the radio buttons
		// for switching between the view and edit modes.
		ModeListener modeListener = new ModeListener();
		
		// Setup the view mode radio button.
		c.gridx = 2;
		c.insets = new Insets(0,250,0,0);
		JRadioButton viewRButton = new JRadioButton("View Mode");
		viewRButton.setActionCommand("VIEW");
		viewRButton.setSelected(true);
		viewRButton.addActionListener(modeListener);
		this.add(viewRButton, c);
		
		// Setup the edit mode radio button.
		c.gridx = 3;
		c.insets = new Insets(0,0,0,0);
		JRadioButton editRButton = new JRadioButton("Edit Mode");
		editRButton.setActionCommand("EDIT");
		editRButton.addActionListener(modeListener);
		this.add(editRButton, c);
		
		// Group the view mode and edit mode radio buttons
		// together.
		this.modeButtonGroup = new ButtonGroup();
		this.modeButtonGroup.add(viewRButton);
		this.modeButtonGroup.add(editRButton);
	}
	
	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/
	
	/**
	 * Unselects both mode radio buttons.
	 */
	public void unselectModeButtons() {
		//this.modeButtonGroup.clearSelection();
	}
	
	/****************************************
	 * 
	 * Event Listener Classes
	 * 
	 ****************************************/
	
	/**
	 * The Mode Listener class for the mode radio buttons.
	 */
	private class ModeListener implements ActionListener {

		/**
		 * Handles the switch of radio buttons.
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("VIEW")) {
				centralPanel.switchToViewPanel();
			} else {
				centralPanel.switchToEditPanel();
			}
		}
		
	}
}
