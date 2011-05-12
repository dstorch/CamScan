package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import core.Parameters;
import core.SystemConfiguration;

public class SettingsDialog extends JDialog {
	
	/************************************************
	 * 
	 * PRIVATE INSTANCE VARIABLES
	 * 
	 ************************************************/
	
	private JTextField tessField;
	private JTextField pythonField;
	private JButton applyButton;

	/************************************************
	 * 
	 * CONSTRUCTOR
	 * 
	 ************************************************/
	
	public SettingsDialog(JFrame owner) {
		super(owner, "Settings");
		
		this.setLayout(new BorderLayout());
		JPanel centerPane = new JPanel(new SpringLayout());
		this.add(centerPane, BorderLayout.CENTER);
		
		// labels for text fields in the spring layout
		String[] labels = {"Tesseract Executable: ", "Python Path: "};

        //Create and populate the panel.
        // tesseract path field
        JLabel l = new JLabel(labels[0], JLabel.TRAILING);
        centerPane.add(l);
        this.tessField = new JTextField(SystemConfiguration.TESS_PATH, 20);
        l.setLabelFor(this.tessField);
        centerPane.add(this.tessField);
      
        // python path field
        l = new JLabel(labels[1], JLabel.TRAILING);
        centerPane.add(l);
        this.pythonField = new JTextField(SystemConfiguration.PYTHON_PATH, 20);
        l.setLabelFor(this.pythonField);
        centerPane.add(this.pythonField);
        
        // the south panel
        JPanel southPanel = new JPanel();
        this.add(southPanel, BorderLayout.SOUTH);
        
        this.applyButton = new JButton("Apply");
        this.applyButton.addActionListener(new ApplyActionListener());
        southPanel.add(this.applyButton);
        
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(centerPane,
                                        2, 2, //rows, cols
                                        6, 6,        //initX, initY
                                        6, 6);       //xPad, yPad

        
		
		this.setLocation((owner.getWidth() - this.getWidth()) / 2, (owner.getHeight() - this.getHeight()) / 2);
		this.setVisible(true);
		this.pack();
	}
	
	/************************************************
	 * 
	 * ACTION LISTENERS
	 * 
	 ************************************************/
	
	private class ApplyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			String newTessPath = tessField.getText().trim();
			File tessFile = new File(newTessPath);
			
			// set tesseract path, or display an error message
			if (tessFile.exists() && !tessFile.isDirectory()) {
				SystemConfiguration.TESS_PATH = newTessPath;
			} else {
				JOptionPane.showMessageDialog(Parameters.getFrame(),
						"The file "+newTessPath+" is not a valid Tesseract executable!",
						"Settings Error", JOptionPane.ERROR_MESSAGE);
				tessField.setText(SystemConfiguration.TESS_PATH);
			}
			
			String newPythonPath = pythonField.getText().trim();
			File pythonFile = new File(newPythonPath);
			
			// set python path, or display an error message
			if (pythonFile.exists() && !pythonFile.isDirectory()) {
				SystemConfiguration.PYTHON_PATH = newPythonPath;
			} else {
				JOptionPane.showMessageDialog(Parameters.getFrame(),
						"The path "+newPythonPath+" is not a valid Python path!",
						"Settings Error", JOptionPane.ERROR_MESSAGE);
				pythonField.setText(SystemConfiguration.PYTHON_PATH);
			}
			
		}	
	}
}