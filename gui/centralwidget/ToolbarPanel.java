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

import core.Mode;
import javax.swing.JRadioButton;
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
	
	/**
	 * The button for moving down to the next page
	 */
	private JButton nextPageDown;
	
	/**
	 * The button for going up to the previous page
	 */
	private JButton nextPageUp;

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
		zoomInButton = new JButton(new ImageIcon(Parameters.ZOOM_IN));
		this.zoomInButton.addActionListener(new ZoomInListener());
		this.zoomInButton.setToolTipText("Zoom In");
		this.add(zoomInButton, c);

		c.gridx = 1;
		c.insets = new Insets(0,0,0,0);
		zoomOutButton = new JButton(new ImageIcon(Parameters.ZOOM_OUT));
		this.zoomOutButton.addActionListener(new ZoomOutListener());
		this.zoomOutButton.setToolTipText("Zoom In");
		this.add(zoomOutButton, c);

		// Setup the mode listener for the radio buttons
		// for switching between the view and edit modes.
		ModeListener modeListener = new ModeListener();

		// the view mode magnifying glass icon
		c.gridx = 2;
		c.insets = new Insets(0,150,0,0);
		JRadioButton viewIcon = new JRadioButton(new ImageIcon(Parameters.VIEW));
		this.add(viewIcon, c);

		// Setup the view mode radio button.
		c.gridx = 3;
		c.insets = new Insets(0,0,0,0);
		this.viewRButton = new JRadioButton("View Mode");
		this.viewRButton.setActionCommand("VIEW");
		this.viewRButton.setSelected(true);
		this.viewRButton.addActionListener(modeListener);
		this.add(this.viewRButton, c);

		// the edit mode pencil icon
		c.gridx = 4;
		c.insets = new Insets(0,10,0,0);
		JRadioButton editIcon = new JRadioButton(new ImageIcon(Parameters.EDIT));
		this.add(editIcon, c);

		// Setup the edit mode radio button.
		c.gridx = 5;
		c.insets = new Insets(0,0,0,0);
		JRadioButton editRButton = new JRadioButton("Edit Mode");
		editRButton.setActionCommand("EDIT");
		editRButton.addActionListener(modeListener);
		this.add(editRButton, c);

		// the down arrow button
		c.gridx = 6;
		c.insets = new Insets(0,150,0,0);
		this.nextPageUp = new JButton(new ImageIcon(Parameters.UP_ARROW));
		this.nextPageUp.addActionListener(new NextPageUpListener());
		this.add(this.nextPageUp, c);
		
		// the up arrow button
		c.gridx = 7;
		c.insets = new Insets(0,0,0,0);
		this.nextPageDown = new JButton(new ImageIcon(Parameters.DOWN_ARROW));
		this.nextPageDown.addActionListener(new NextPageDownListener());
		this.add(this.nextPageDown, c);
		
		
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
				centralPanel.switchToViewPanel();
				Parameters.setIsInEditMode(false);
			} else {
				centralPanel.switchToEditPanel();
				Parameters.setIsInEditMode(true);
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
	 * The down arrow action listener.
	 */
	private class NextPageDownListener implements ActionListener {

		/**
		 * Handles switching to the next page down
		 */
		public void actionPerformed(ActionEvent arg0) {
			Parameters.getPageExpPanel().incrementIndex();
		}
		
	}
	
	/**
	 * The down arrow action listener.
	 */
	private class NextPageUpListener implements ActionListener {

		/**
		 * Handles switching to the next page down
		 */
		public void actionPerformed(ActionEvent arg0) {
			Parameters.getPageExpPanel().decrementIndex();
		}
		
	}

}
