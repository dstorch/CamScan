package centralwidget;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import core.Parameters;

/**
 * The button panel that contains basic tools for
 * manipulating the current page.
 * 
 * @author Stelios
 *
 */
public class ButtonPanel extends JPanel {
	
	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * All the controls.
	 */
	private ArrayList<Component> controls;
	
	/**
	 * A reference to the edit panel is needed
	 * so that the action listeners can take
	 * their effect.
	 */
	private EditPanel editPanel;
	
	/**
	 * The JButton by which vertical split
	 * mode is entered
	 */
	private JButton vSplitButton;
	
	/**
	 * The JButton by which horizontal split
	 * mode is entered
	 */
	private JButton hSplitButton;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public ButtonPanel(EditPanel editPanel) {
		super();
		this.setLayout(new GridLayout(2,4));
		
		this.editPanel = editPanel;
		
		this.controls = new ArrayList<Component>();
		
		// Setup all the buttons
		this.hSplitButton = new JButton("Horizontal Split");
		this.add(this.hSplitButton);
		this.hSplitButton.addActionListener(new HorizontalSplitListener());
		this.controls.add(this.hSplitButton);
		
		this.vSplitButton = new JButton("Vertical Split");
		this.add(vSplitButton);
		this.vSplitButton.addActionListener(new VerticalSplitListener());
		this.controls.add(this.vSplitButton);
		
		JButton flipHorizontally = new JButton("Flip Horizontally");
		this.add(flipHorizontally);
		flipHorizontally.addActionListener(new FlipHorizontallyListener());
		this.controls.add(flipHorizontally);
		
		JButton flipVertically = new JButton("Flip Vertically");
		this.add(flipVertically);
		flipVertically.addActionListener(new FlipVerticallyListener());
		this.controls.add(flipVertically);
		
		JLabel contrastLabel = new JLabel("Contrast: ", SwingConstants.CENTER);
		this.add(contrastLabel);
		this.controls.add(contrastLabel);
		
		JSlider contrastSlider = new JSlider();
		this.add(contrastSlider);
		this.controls.add(contrastSlider);
		
		JLabel temperatureLabel = new JLabel("Temperature: ", SwingConstants.CENTER);
		this.add(temperatureLabel);
		this.controls.add(temperatureLabel);
		
		JSlider temperatureSlider = new JSlider();
		this.add(temperatureSlider);
		this.controls.add(temperatureSlider);
	}
	
	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/
	
	/**
	 * Sets the visibility of each individual component.
	 * 
	 * @param b True if the components should become visible,
	 * false if otherwise
	 */
	public void setComponentsVisible(boolean visible) {
		for (Component c : this.controls) {
			c.setVisible(visible);
		}
	}
	
	/****************************************
	 * 
	 * Event Listeners
	 * 
	 ****************************************/
	
	/**
	 * Listener for the Horizontal Split button.
	 */
	private class HorizontalSplitListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			EditPanelMode mode = editPanel.getEditPanelMode();
			
			if (mode == EditPanelMode.HSPLIT) {
				hSplitButton.setText("Horizontal Split");
			} else {
				hSplitButton.setText("Cancel Split");
			}
			
			editPanel.toggleHorizontalFlipMode();
		}
	}
	
	/**
	 * Listener for the Vertical Split button.
	 */
	private class VerticalSplitListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			EditPanelMode mode = editPanel.getEditPanelMode();
			
			if (mode == EditPanelMode.VSPLIT) {
				vSplitButton.setText("Vertical Split");
			} else {
				vSplitButton.setText("Cancel Split");
			}
			
			editPanel.toggleVerticalFlipMode();
		}
	}
	
	/**
	 * Listener for the Flip Horizontally button.
	 */
	private class FlipHorizontallyListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Parameters.getCoreManager().flipImage(true, true);
		}
	}
	
	/**
	 * Listener for the Flip Vertically button.
	 */
	private class FlipVerticallyListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			Parameters.getCoreManager().flipImage(false, true);
		}
	}
}
