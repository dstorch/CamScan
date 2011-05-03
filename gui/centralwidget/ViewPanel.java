package centralwidget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import core.Parameters;

/**
 * This panel is used for only viewing a page.
 * 
 * @author Stelios
 *
 */
public class ViewPanel extends JPanel implements MouseWheelListener {
	
	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * The buffered image representing the page.
	 */
	private BufferedImage img;
	
	/**
	 * The scale factor.
	 */
	private double scaleFactor;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public ViewPanel() {
		super();
		this.setBackground(Color.LIGHT_GRAY);
		this.addMouseWheelListener(this);
		this.setBorder(new LineBorder(Color.GRAY));
		
		this.scaleFactor = 1;
	}
	
	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/
	
	/**
	 * The paintComponent method.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Parameters.getCoreManager().updateWorkingImage();
		
		if (this.img != Parameters.getCurrPageImg()) {
			this.scaleFactor = 1;
			this.img = Parameters.getCurrPageImg();
		}	
		
		if (this.img != null) {
			int newW = (int) (this.img.getWidth() * this.scaleFactor);
			int newH = (int) (this.img.getHeight() * this.scaleFactor);
			g.drawImage(this.img, (this.getWidth() - newW)/2, (this.getHeight() - newH)/2, newW, newH, null);
		}
	}

	/**
	 * Handles the mouse wheel movement.
	 * Used for zooming.
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {

		int notches = e.getWheelRotation();

		if (notches < 0) {
			if (this.scaleFactor > 0.2)
				this.scaleFactor -= 0.1;
		} else {
			this.scaleFactor += 0.1;
		}
		
		this.repaint();
	}
}
