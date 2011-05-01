package centralwidget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import core.Parameters;

/**
 * This panel is used for only viewing a page.
 * 
 * @author Stelios
 *
 */
public class ViewPanel extends JPanel {
	
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
		Graphics2D brush = (Graphics2D) g;
		
		BufferedImage img = Parameters.getCurrPageImg();
		g.drawImage(img, (this.getWidth() - img.getWidth())/2, (this.getHeight() - img.getHeight())/2, null);
	}
}
