package centralwidget;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import core.Event;
import core.Mode;
import core.Parameters;

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

	/**
	 * The zoom-in button.
	 */
	private JButton zoomInButton;

	/**
	 * The zoom-out button.
	 */
	private JButton zoomOutButton;

	/**
	 * The back button
	 */
	private JButton backButton;

	/**
	 * The next button
	 */
	private JButton nextButton;

	/**
	 * The view radio button.
	 */
	private JRadioButton viewRButton;

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

		this.centralPanel = centralPanel;

		c.gridx = 0;
		this.backButton = new JButton("Back");
		this.backButton.addActionListener(new BackButtonListener());
		this.add(this.backButton, c);

		c.gridx = 1;
		this.nextButton = new JButton("Next");
		this.nextButton.addActionListener(new NextButtonListener());
		this.add(this.nextButton, c);

		c.gridx = 2;
		c.insets = new Insets(0,125,0,0);
		zoomInButton = new JButton("Zoom In");
		this.zoomInButton.addActionListener(new ZoomInListener());
		this.add(zoomInButton, c);

		c.gridx = 3;
		c.insets = new Insets(0,0,0,0);
		zoomOutButton = new JButton("Zoom Out");
		this.zoomOutButton.addActionListener(new ZoomOutListener());
		this.add(zoomOutButton, c);

		// Setup the mode listener for the radio buttons
		// for switching between the view and edit modes.
		ModeListener modeListener = new ModeListener();

		// the view mode magnifying glass icon
		c.gridx = 4;
		c.insets = new Insets(0,125,0,0);
		JRadioButton viewIcon = new JRadioButton(new ImageIcon("libraries/icons/magnify.png"));
		this.add(viewIcon, c);

		// Setup the view mode radio button.
		c.gridx = 5;
		c.insets = new Insets(0,0,0,0);
		this.viewRButton = new JRadioButton("View Mode");
		this.viewRButton.setActionCommand("VIEW");
		this.viewRButton.setSelected(true);
		this.viewRButton.addActionListener(modeListener);
		this.add(this.viewRButton, c);

		// the edit mode pencil icon
		c.gridx = 6;
		c.insets = new Insets(0,10,0,0);
		JRadioButton editIcon = new JRadioButton(new ImageIcon("libraries/icons/pencil.png"));
		this.add(editIcon, c);

		// Setup the edit mode radio button.
		c.gridx = 7;
		c.insets = new Insets(0,0,0,0);
		JRadioButton editRButton = new JRadioButton("Edit Mode");
		editRButton.setActionCommand("EDIT");
		editRButton.addActionListener(modeListener);
		this.add(editRButton, c);

		// Group the view mode and edit mode radio buttons
		// together.
		this.modeButtonGroup = new ButtonGroup();
		this.modeButtonGroup.add(this.viewRButton);
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

	/**
	 * Selects the view radio button.
	 */
	public void selectViewRButton() {
		this.viewRButton.setSelected(true);
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
			Mode lastMode = centralPanel.getCurrentMode();
			if (e.getActionCommand().equals("VIEW")) {
				centralPanel.switchToViewPanel(true, lastMode, null);
			} else {
				centralPanel.switchToEditPanel(true, lastMode, null);
			}
		}
	}

	/**
	 * The ZoomIn action listener.
	 */
	private class ZoomInListener implements ActionListener {

		/**
		 * Handles zooming in.
		 */
		public void actionPerformed(ActionEvent arg0) {
			if (centralPanel.getViewPanel().isVisible())
				centralPanel.getViewPanel().zoomIn();
			else if (centralPanel.getEditPanel().isVisible())
				centralPanel.getEditPanel().zoomIn();
		}		
	}

	/**
	 * The ZoomOut action listener.
	 */
	private class ZoomOutListener implements ActionListener {

		/**
		 * Handles zooming out.
		 */
		public void actionPerformed(ActionEvent arg0) {
			if (centralPanel.getViewPanel().isVisible())
				centralPanel.getViewPanel().zoomOut();
			else if (centralPanel.getEditPanel().isVisible())
				centralPanel.getEditPanel().zoomOut();
		}	
	}

	/**
	 * The back button action listener.
	 */
	private class BackButtonListener implements ActionListener {

		/**
		 * Handles going back in the history
		 */
		public void actionPerformed(ActionEvent arg0) {
			
			Event e = Parameters.getCoreManager().getHistory().back();
			
			// if null, then we don't need to do anything
			// if not null, then go backwards in the history
			if (e != null) {
				
				// we need to reset the core manager if the Event from the history
				// was either an Edit or a View
				if (e.getMode() == Mode.EDIT || e.getMode() == Mode.VIEW) {
					
					System.out.println(e);
					
					// try to reset the core manager instance variables,
					// throwing an exception if needed
					try {
						Parameters.getCoreManager().setFromEvent(e);
						
						Mode lastMode = centralPanel.getCurrentMode();
						
						if (e.getMode() == Mode.EDIT) {
							centralPanel.switchToEditPanel(false, lastMode, null);
						} else if (e.getMode() == Mode.VIEW) {
							centralPanel.switchToViewPanel(false, lastMode, null);
						}
						
						centralPanel.updatePanels(false);
						
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(Parameters.getFrame(),
								"Unexpected problem encountered while going back!",
								"Back Button Error", JOptionPane.ERROR_MESSAGE);
					}
					
				} else {
					// TODO
					// make going back to search mode work
				}
			}
		}	
	}


	/**
	 * The next button action listener.
	 */
	private class NextButtonListener implements ActionListener {

		/**
		 * Handles going back in the history
		 */
		public void actionPerformed(ActionEvent arg0) {
			// TODO
			System.out.println("next button pressed");
		}	
	}
}
