package centralwidget;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

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
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public ButtonPanel() {
		super();
		this.setLayout(new GridLayout(2,4));
		
		this.controls = new ArrayList<Component>();
		
		// Setup all the buttons
		JButton hFlipButton = new JButton("Horizontal Split");
		this.add(hFlipButton);
		this.controls.add(hFlipButton);
		
		JButton vFlipButton = new JButton("Vertical Split");
		this.add(vFlipButton);
		this.controls.add(vFlipButton);
		
		JButton rotateLeftButton = new JButton("Rotate Left");
		this.add(rotateLeftButton);
		this.controls.add(rotateLeftButton);
		
		JButton rotateRightButton = new JButton("Rotate Right");
		this.add(rotateRightButton);
		this.controls.add(rotateRightButton);
		
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
}
