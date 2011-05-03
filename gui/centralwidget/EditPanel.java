package centralwidget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import core.Corners;
import core.Parameters;

/**
 * The display panel where the image will be displayed. The user will
 * also be able to customize the corners of the image/page by dragging
 * their ellipse-representations around.
 * 
 * @author Stelios
 *
 */
public class EditPanel extends JPanel implements MouseMotionListener {

	/****************************************
	 * 
	 * Constants
	 * 
	 ****************************************/

	/**
	 * The default radius of the ellipses representing
	 * the corners of the page.
	 */
	private static int ELLIPSE_RADIUS = 10;

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/

	/**
	 * The upper-left corner.
	 */
	private Ellipse2D cornerUL;

	/**
	 * The upper-right corner.
	 */
	private Ellipse2D cornerUR;

	/**
	 * The down-right corner.
	 */
	private Ellipse2D cornerDR;

	/**
	 * The down-left corner.
	 */
	private Ellipse2D cornerDL;

	/**
	 * The line connecting the upper left
	 * and the upper right corner.
	 */
	private Line2D lineULUR;

	/**
	 * The line connecting the upper right
	 * and the down right corner.
	 */
	private Line2D lineURDR;

	/**
	 * The line connecting the down right
	 * and the down left corner.
	 */
	private Line2D lineDRDL;

	/**
	 * The line connecting the down left
	 * and the upper left corner.
	 */
	private Line2D lineDLUL;

	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/

	/**
	 * Constructor.
	 */
	public EditPanel() {
		super();

		this.addMouseMotionListener(this);
		this.setBackground(Color.LIGHT_GRAY);
		this.setBorder(new LineBorder(Color.GRAY));

		this.cornerUL = new Ellipse2D.Double();
		this.cornerUR = new Ellipse2D.Double();
		this.cornerDR = new Ellipse2D.Double();
		this.cornerDL = new Ellipse2D.Double();

		this.lineULUR = new Line2D.Double();
		this.lineURDR = new Line2D.Double();
		this.lineDRDL = new Line2D.Double();
		this.lineDLUL = new Line2D.Double();

		this.repaint();
	}

	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/
	
	/**
	 * Updates the corners when the current page changes.
	 */
	public void updateCornersOnPanel() {
		
		Corners corners = Parameters.getCoreManager().getWorkingPage().corners();
		
		this.moveCornerTo(cornerUL, corners.upleft().getX(), corners.upleft().getY());
		this.moveCornerTo(cornerUR, corners.upright().getX(), corners.upright().getY());
		this.moveCornerTo(cornerDR, corners.downright().getX(), corners.downright().getY());
		this.moveCornerTo(cornerDL, corners.downleft().getX(), corners.downleft().getY());
		
		this.updateConnectingLines();
		this.repaint();
	}

	/**
	 * The paintComponent method.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D brush = (Graphics2D) g;

		BufferedImage img = Parameters.getCurrPageImg();
		
		if (img != null)
			g.drawImage(img, (this.getWidth() - img.getWidth())/2, (this.getHeight() - img.getHeight())/2, null);
		
		brush.setColor(Color.BLACK);
		brush.draw(this.lineULUR);
		brush.draw(this.lineURDR);
		brush.draw(this.lineDRDL);
		brush.draw(this.lineDLUL);

		brush.setColor(Color.BLUE);
		brush.draw(this.cornerUR);
		brush.draw(this.cornerUL);
		brush.draw(this.cornerDR);
		brush.draw(this.cornerDL);
		brush.fill(this.cornerUR);
		brush.fill(this.cornerUL);
		brush.fill(this.cornerDR);
		brush.fill(this.cornerDL);
	}

	/****************************************
	 * 
	 * Private Methods
	 * 
	 ****************************************/
	
	/**
	 * Updates all lines connecting the corners.
	 */
	private void updateConnectingLines() {
		this.moveLine(this.lineULUR, this.cornerUL, this.cornerUR);
		this.moveLine(this.lineURDR, this.cornerUR, this.cornerDR);
		this.moveLine(this.lineDRDL, this.cornerDR, this.cornerDL);
		this.moveLine(this.lineDLUL, this.cornerDL, this.cornerUL);
	}

	/**
	 * Given a corner and a pair of x, y coordinates, it moves
	 * the corner to these coordinates.
	 * 
	 * @param corner The given corner.
	 * @param x The new x-location of the corner
	 * @param y The new y-location of the corner
	 */
	private void moveCornerTo(Ellipse2D corner, double x, double y) {
		corner.setFrame(x, y, ELLIPSE_RADIUS, ELLIPSE_RADIUS);
	}

	private void moveLine(Line2D line, Ellipse2D cornerA, Ellipse2D cornerB) {
		line.setLine(cornerA.getCenterX(), cornerA.getCenterY(), cornerB.getCenterX(), cornerB.getCenterY());
	}

	/**
	 * Returns true if the mouse cursor (coordinates passed in as mX and mY)
	 * is within a certain radius around the ellipse.
	 * 
	 * @param ellipse The given ellipse
	 * @param mX The x-mouse-coordinate
	 * @param mY The y-mouse-coordinate
	 * @return True if the mouse cursor (coordinates passed in as mX and mY)
	 * is within a certain radius around the ellipse
	 */
	private boolean isWithinCornerEllipse(Ellipse2D ellipse, double mX, double mY) {

		int limit = 25;
		return (ellipse.getCenterX() - limit <= mX && mX <= ellipse.getCenterX() + limit &&
				ellipse.getCenterY() - limit <= mY && mY <= ellipse.getCenterY() + limit);
	}

	/****************************************
	 * 
	 * Event Listener Methods
	 * 
	 ****************************************/

	/**
	 * Handles mouse dragging. If the user clicks on a corner,
	 * they can drag it around.
	 */
	public void mouseDragged(MouseEvent arg0) {
		
		Point p = new Point(arg0.getX(), arg0.getY());
		if (this.isWithinCornerEllipse(this.cornerUL, arg0.getX(), arg0.getY())) {
			this.moveCornerTo(cornerUL, arg0.getX() - this.cornerUL.getWidth()/2, arg0.getY() - this.cornerUL.getHeight()/2);
			this.moveLine(this.lineDLUL, this.cornerDL, this.cornerUL);
			this.moveLine(this.lineULUR, this.cornerUL, this.cornerUR);
			Parameters.getCoreManager().getWorkingPage().corners().setUpLeft(p);
		} else if (this.isWithinCornerEllipse(this.cornerUR, arg0.getX(), arg0.getY())) {
			this.moveCornerTo(cornerUR, arg0.getX() - this.cornerUR.getWidth()/2, arg0.getY() - this.cornerUR.getHeight()/2);
			this.moveLine(this.lineULUR, this.cornerUL, this.cornerUR);
			this.moveLine(this.lineURDR, this.cornerUR, this.cornerDR);
			Parameters.getCoreManager().getWorkingPage().corners().setUpRight(p);
		} else if (this.isWithinCornerEllipse(this.cornerDL, arg0.getX(), arg0.getY())) {
			this.moveCornerTo(cornerDL, arg0.getX() - this.cornerDL.getWidth()/2, arg0.getY() - this.cornerDL.getHeight()/2);
			this.moveLine(this.lineDLUL, this.cornerDL, this.cornerUL);
			this.moveLine(this.lineDRDL, this.cornerDR, this.cornerDL);
			Parameters.getCoreManager().getWorkingPage().corners().setDownLeft(p);
		} else if (this.isWithinCornerEllipse(this.cornerDR, arg0.getX(), arg0.getY())) {
			this.moveCornerTo(cornerDR, arg0.getX() - this.cornerDR.getWidth()/2, arg0.getY() - this.cornerDR.getHeight()/2);
			this.moveLine(this.lineDRDL, this.cornerDR, this.cornerDL);
			this.moveLine(this.lineURDR, this.cornerUR, this.cornerDR);
			Parameters.getCoreManager().getWorkingPage().corners().setDownRight(p);
		}

		this.repaint();
	}

	public void mouseMoved(MouseEvent arg0) {}
}
