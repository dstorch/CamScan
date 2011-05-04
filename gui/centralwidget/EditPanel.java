package centralwidget;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
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
public class EditPanel extends JPanel implements MouseMotionListener, MouseWheelListener, MouseListener {

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
	
	/**
	 * The actual drawn ellipses. These
	 * differ from cornerUR in that they
	 * have the transform applied to them,
	 * making them appear correctly on screen.
	 */
	private Ellipse2D drawableUR;
	private Ellipse2D drawableUL;
	private Ellipse2D drawableDR;
	private Ellipse2D drawableDL;
	
	private PointTransform transUR = new PointTransform();
	private PointTransform transUL = new PointTransform();
	private PointTransform transDR = new PointTransform();
	private PointTransform transDL = new PointTransform();

	/**
	 * The scale factor.
	 */
	private double scaleFactor;

	/**
	 * The buffered image representing the page.
	 */
	private BufferedImage img;

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
		this.scaleFactor = 1;

		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addMouseListener(this);
		this.setBackground(Color.LIGHT_GRAY);
		this.setBorder(new LineBorder(Color.GRAY));

		this.cornerUL = new Ellipse2D.Double();
		this.cornerUR = new Ellipse2D.Double();
		this.cornerDR = new Ellipse2D.Double();
		this.cornerDL = new Ellipse2D.Double();
		
		this.drawableUL = new Ellipse2D.Double();
		this.drawableUR = new Ellipse2D.Double();
		this.drawableDR = new Ellipse2D.Double();
		this.drawableDL = new Ellipse2D.Double();
		
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

		// shift the original corners according to those in the working page
		this.moveCornerTo(cornerUL, corners.upleft().getX(), corners.upleft().getY());
		this.moveCornerTo(cornerUR, corners.upright().getX(), corners.upright().getY());
		this.moveCornerTo(cornerDR, corners.downright().getX(), corners.downright().getY());
		this.moveCornerTo(cornerDL, corners.downleft().getX(), corners.downleft().getY());
		
		// reset translation to zero
		this.transUL = new PointTransform();
		this.transUR = new PointTransform();
		this.transDR = new PointTransform();
		this.transDL = new PointTransform();

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

		// calculate the initial scale factor if we are switching to a
		// new working image
		if (this.img != Parameters.getCoreManager().getWorkingImage()) {
			this.scaleFactor = 1;
			this.img = Parameters.getCoreManager().getWorkingImage();

			double xSideRatio = ((double) this.getWidth())/((double) this.img.getWidth());
			double ySideRatio = ((double) this.getHeight())/((double) this.img.getHeight());

			if (ySideRatio > xSideRatio)
				this.scaleFactor = xSideRatio;
			else
				this.scaleFactor = ySideRatio;

			this.scaleFactor *= 0.95;
		}

		// scale the image and display it, centered on the screen
		if (this.img != null) {
			int newW = (int) (this.img.getWidth() * this.scaleFactor);
			int newH = (int) (this.img.getHeight() * this.scaleFactor);
			g.drawImage(this.img, (this.getWidth() - newW)/2, (this.getHeight() - newH)/2, newW, newH, null);
		}
		
		// 1. scale and translate the upper right point
		double x = this.scaleFactor * (this.cornerUR.getX() - this.getWidth()/2 + transUR.dx) + this.getWidth()/2 ;
		double y = this.scaleFactor * (this.cornerUR.getY() - this.getHeight()/2 + transUR.dy) + this.getHeight()/2;
		this.moveCornerTo(drawableUR, x, y);
		Point p = new Point((int) (cornerUR.getX() + transUR.dx), (int) (cornerUR.getY() + transUR.dy));
		Parameters.getCoreManager().getWorkingPage().corners().setUpRight(p);
		
		// 2. scale and translate the upper left point
		x = this.scaleFactor * (this.cornerUL.getX() - this.getWidth()/2 + transUL.dx) + this.getWidth()/2 ;
		y = this.scaleFactor * (this.cornerUL.getY() - this.getHeight()/2 + transUL.dy) + this.getHeight()/2;
		this.moveCornerTo(drawableUL, x, y);
		p = new Point((int) (cornerUL.getX() + transUL.dx), (int) (cornerUL.getY() + transUL.dy));
		Parameters.getCoreManager().getWorkingPage().corners().setUpLeft(p);
		
		// 3. scale and translate the lower left point
		x = this.scaleFactor * (this.cornerDL.getX() - this.getWidth()/2 + transDL.dx) + this.getWidth()/2 ;
		y = this.scaleFactor * (this.cornerDL.getY() - this.getHeight()/2 + transDL.dy) + this.getHeight()/2;
		this.moveCornerTo(drawableDL, x, y);
		p = new Point((int) (cornerDL.getX() + transDL.dx), (int) (cornerDL.getY() + transDL.dy));
		Parameters.getCoreManager().getWorkingPage().corners().setDownLeft(p);
		
		// 4. scale and translate the lower right point
		x = this.scaleFactor * (this.cornerDR.getX() - this.getWidth()/2 + transDR.dx) + this.getWidth()/2 ;
		y = this.scaleFactor * (this.cornerDR.getY() - this.getHeight()/2 + transDR.dy) + this.getHeight()/2;
		this.moveCornerTo(drawableDR, x, y);
		p = new Point((int) (cornerDR.getX() + transDR.dx), (int) (cornerDR.getY() + transDR.dy));
		Parameters.getCoreManager().getWorkingPage().corners().setDownRight(p);
		
		// make the lines connect to the new point location
		this.updateConnectingLines();
		
		// draw lines
		brush.setColor(Color.BLACK);
		brush.draw(this.lineULUR);
		brush.draw(this.lineURDR);
		brush.draw(this.lineDRDL);
		brush.draw(this.lineDLUL);

		// draw points
		brush.setColor(Color.BLUE);
		brush.draw(this.drawableUR);
		brush.draw(this.drawableUL);
		brush.draw(this.drawableDR);
		brush.draw(this.drawableDL);
		brush.fill(this.drawableUR);
		brush.fill(this.drawableUL);
		brush.fill(this.drawableDR);
		brush.fill(this.drawableDL);
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
		this.moveLine(this.lineULUR, this.drawableUL, this.drawableUR);
		this.moveLine(this.lineURDR, this.drawableUR, this.drawableDR);
		this.moveLine(this.lineDRDL, this.drawableDR, this.drawableDL);
		this.moveLine(this.lineDLUL, this.drawableDL, this.drawableUL);
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
		corner.setFrame(x, y, ELLIPSE_RADIUS * this.scaleFactor, ELLIPSE_RADIUS * this.scaleFactor);
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

		int limit = (int) (100 * this.scaleFactor);
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

		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// move the ellipses on screen
		Point p = new Point(arg0.getX(), arg0.getY());

		// 1. move upper left ellipse during click and drag
		if (this.isWithinCornerEllipse(this.drawableUL, arg0.getX(), arg0.getY())) {
			
			double newX = arg0.getX() - this.transUL.dragX;
			double newY = arg0.getY() - this.transUL.dragY;
			
			this.transUL.dragX += newX;
			this.transUL.dragY += newY;
			
			this.transUL.dx += newX / this.scaleFactor;
			this.transUL.dy += newY / this.scaleFactor;
			
			this.repaint();
		}

		// 2. move upper right ellipse
		else if (this.isWithinCornerEllipse(this.drawableUR, arg0.getX(), arg0.getY())) {
		
			double newX = arg0.getX() - this.transUR.dragX;
			double newY = arg0.getY() - this.transUR.dragY;
			
			this.transUR.dragX += newX;
			this.transUR.dragY += newY;
			
			this.transUR.dx += newX / this.scaleFactor;
			this.transUR.dy += newY / this.scaleFactor;
			
			this.repaint();
		}

		// 3. move lower left ellipse
		else if (this.isWithinCornerEllipse(this.drawableDL, arg0.getX(), arg0.getY())) {
			
			double newX = arg0.getX() - this.transDL.dragX;
			double newY = arg0.getY() - this.transDL.dragY;
			
			this.transDL.dragX += newX;
			this.transDL.dragY += newY;
			
			this.transDL.dx += newX / this.scaleFactor;
			this.transDL.dy += newY / this.scaleFactor;
			
			this.repaint();
		}

		// 4. move lower right ellipse
		else if (this.isWithinCornerEllipse(this.drawableDR, arg0.getX(), arg0.getY())) {
			
			double newX = arg0.getX() - this.transDR.dragX;
			double newY = arg0.getY() - this.transDR.dragY;
			
			this.transDR.dragX += newX;
			this.transDR.dragY += newY;
			
			this.transDR.dx += newX / this.scaleFactor;
			this.transDR.dy += newY / this.scaleFactor;
			
			this.repaint();
			
		}
	}

	/**
	 * Update the scale factor when the mouse wheel
	 * is scrolled up or down.
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


	/**
	 * On mouse press, set the initial location for 
	 * a draw event, if the mouse press is within
	 * the radius of one of the ellipses.
	 * 
	 * Also set the cursor to a hand.
	 */
	public void mousePressed(MouseEvent e) {
		
		// set the hand cursor
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		// upper left corner
		if (this.isWithinCornerEllipse(this.drawableUL, e.getX(), e.getY())) {
			transUL.dragX = e.getX(); transUL.dragY = e.getY();
		}
		
		// upper right corner
		else if (this.isWithinCornerEllipse(this.drawableUR, e.getX(), e.getY())) {
			transUR.dragX = e.getX(); transUR.dragY = e.getY();
		}
		
		// lower left corner
		else if (this.isWithinCornerEllipse(this.drawableDL, e.getX(), e.getY())) {
			transDL.dragX = e.getX(); transDL.dragY = e.getY();
		}

		// lower right corner
		else if (this.isWithinCornerEllipse(this.drawableDR, e.getX(), e.getY())) {
			transDR.dragX = e.getX(); transDR.dragY = e.getY();
		}
		
	}
	
	/**
	 * Restore the default cursor upon mouse release
	 */
	public void mouseReleased(MouseEvent e) {
		this.setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Un-implemented mouse listener methods
	 */
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent arg0) {}

}
