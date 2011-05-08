package centralwidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

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
	 * Private Instance Variables
	 * 
	 ****************************************/

	/**
	 * The buffered image representing the page.
	 */
	private BufferedImage img;

	/**
	 * The image label holding the page image.
	 */
	private JLabel imageLabel;

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
		this.setLayout(new BorderLayout());
		this.setBackground(Color.LIGHT_GRAY);
		this.setBorder(new LineBorder(Color.GRAY));
		this.scaleFactor = 1;
		this.imageLabel = new JLabel();
		JScrollPane imageScroller = new JScrollPane(this.imageLabel);
		this.add(imageScroller, BorderLayout.CENTER);
	}

	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/

	/**
	 * Zooms in.
	 */
	public void zoomIn() {
		this.scaleFactor += 0.2;
		this.repaint();
	}

	/**
	 * Zooms out.
	 */
	public void zoomOut() {
		if (this.scaleFactor > 0.2) {
			this.scaleFactor -= 0.2;
			this.repaint();
		}
	}

	/**
	 * The paintComponent method.
	 */
	@Override
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		
		// When the page has changed, set the new img instance variable
		// and modify the scale factor.
		if (this.img != Parameters.getCoreManager().getWorkingImage() && Parameters.getCoreManager().getWorkingImage() != null) {

			this.img = Parameters.getCoreManager().getWorkingImage();

			double xSideRatio = ((double) this.getWidth())/((double) this.imageLabel.getWidth());
			double ySideRatio = ((double) this.getHeight())/((double) this.imageLabel.getHeight());

			if (ySideRatio > xSideRatio)
				this.scaleFactor = xSideRatio;
			else
				this.scaleFactor = ySideRatio;

			this.scaleFactor *= 0.95;
		}
		
		// Scale and draw the image.
		if (this.img != null) {
			int newW = (int) (this.img.getWidth(this) * this.scaleFactor);
			int newH = (int) (this.img.getHeight(this) * this.scaleFactor);
			this.imageLabel.setLocation((this.getWidth() - newW)/2, (this.getHeight() - newH)/2);
			this.imageLabel.setBounds((this.getWidth() - newW)/2, (this.getHeight() - newH)/2, newW, newH);
			this.imageLabel.setIcon(this.getScaledImageIcon());
		}
	}

	/**
	 * Scales the given buffered image and returns in as an
	 * ImageIcon.
	 * 
	 * @param src The buffered image to scale
	 * @param scale The scale factor
	 * @return The resulting ImageIcon
	 */
	private ImageIcon getScaledImageIcon() {
		int w = (int)(this.scaleFactor * this.img.getWidth(this));
		int h = (int)(this.scaleFactor * this.img.getHeight(this));
		int type = BufferedImage.TYPE_INT_RGB;
		BufferedImage dst = new BufferedImage(w, h, type);
		Graphics2D g2 = dst.createGraphics();
		g2.drawImage(this.img, 0, 0, w, h, this.imageLabel);
		g2.dispose();
		return new ImageIcon(dst);
	}
}