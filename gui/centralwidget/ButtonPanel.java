package centralwidget;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	
	/**
	 * The contrast button.
	 */
	private JButton contrastButton;
	
	/**
	 * Reference to the central panel.
	 */
	private CentralPanel centralPanel;
	
	/**
	 * The temperature slider;
	 */
	private JSlider temperatureSlider;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public ButtonPanel(EditPanel editPanel, CentralPanel centralPanel) {
		super();
		this.setLayout(new GridLayout(2,4));
		
		this.editPanel = editPanel;
		this.centralPanel = centralPanel;
		
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
		
		this.contrastButton = new JButton("Boost Contrast");
		this.add(this.contrastButton);
		this.contrastButton.addActionListener(new ContrastListener());
		this.controls.add(this.contrastButton);
		
		JLabel temperatureLabel = new JLabel("Temperature: ", SwingConstants.CENTER);
		this.add(temperatureLabel);
		this.controls.add(temperatureLabel);
		
		this.temperatureSlider = new JSlider(JSlider.HORIZONTAL,-50, 50, 5);
		this.temperatureSlider.addChangeListener(new TempListener());
		this.add(this.temperatureSlider);
		this.controls.add(this.temperatureSlider);
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
				vSplitButton.setText("Vertical Split");
			} else if (mode == EditPanelMode.STANDARD){
				vSplitButton.setText("Cancel Split");
				hSplitButton.setText("Apply Split");
			} else if (mode == EditPanelMode.VSPLIT) {
				hSplitButton.setText("Horizontal Split");
				vSplitButton.setText("Vertical Split");
			}
			
			editPanel.toggleHorizontalFlipMode();
			
//			try {
//				Parameters.getCoreManager().setProcessedImage(Parameters.getCoreManager().getWorkingPage().getRawImgFromDisk());
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
		}
	}
	
	/**
	 * Listener for the Vertical Split button.
	 */
	private class VerticalSplitListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			EditPanelMode mode = editPanel.getEditPanelMode();
			
			if (mode == EditPanelMode.VSPLIT) {
				hSplitButton.setText("Horizontal Split");
				vSplitButton.setText("Vertical Split");
			} else if (mode == EditPanelMode.STANDARD) {
				vSplitButton.setText("Cancel Split");
				hSplitButton.setText("Apply Split");
			} else if (mode == EditPanelMode.HSPLIT) {
				hSplitButton.setText("Horizontal Split");
				vSplitButton.setText("Vertical Split");
			}
			
			editPanel.toggleVerticalFlipMode();
			
//			try {
//				Parameters.getCoreManager().setProcessedImage(Parameters.getCoreManager().getWorkingPage().getRawImgFromDisk());
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
		}
	}
	
	/**
	 * Listener for the Flip Horizontally button.
	 */
	private class FlipHorizontallyListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Parameters.getCoreManager().flipImage(false);
			Parameters.getCoreManager().getEditImageTransform();
			centralPanel.updatePanels(false);
		}
	}
	
	/**
	 * Listener for the Flip Vertically button.
	 */
	private class FlipVerticallyListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			Parameters.getCoreManager().flipImage(true);
			Parameters.getCoreManager().getEditImageTransform();
			centralPanel.updatePanels(false);
		}
	}
	
	private class ContrastListener implements ActionListener {

		private boolean boostContrast = false;
		
		public void actionPerformed(ActionEvent arg0) {
			
			this.boostContrast = !this.boostContrast;
			
			if (this.boostContrast) {
				contrastButton.setText("Reduce Contrast");
			} else { 
				contrastButton.setText("Boost Contrast");
			}	

			Parameters.getCoreManager().boostConstrast();
			Parameters.getCoreManager().getEditImageTransform();
			centralPanel.updatePanels(false);
		}
	}
	
	/**
	 * Implementation of a ChangeListener for the 
	 * temperature slider.
	 */
	private class TempListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			TempChangeThread t = new TempChangeThread();
			t.start();
		}
	}
	
	private class TempChangeThread extends Thread {
		
		public void run() {
			Parameters.getCoreManager().changeTemperature(temperatureSlider.getValue());
			Parameters.getCoreManager().getEditImageTransform();
			centralPanel.updatePanels(false);
		}
	}
}
