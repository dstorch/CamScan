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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import core.CoreManager;
import core.Corners;
import core.Page;
import core.Parameters;

/**
 * The display panel where the image will be displayed. The user will
 * also be able to customize the corners of the image/page by dragging
 * their ellipse-representations around.
 * 
 * @author Stelios
 *
 */
@SuppressWarnings("serial")
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
	private static int ELLIPSE_RADIUS = 20;

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/

	/**
	 * Stores the data for horizonal and vertical split
	 */
	private SplitMode horizontalSplit;
	private SplitMode verticalSplit;
	private EditPanelMode mode;
	
	/**
	 * The upper-left corner.
	 */
	private Point cornerUL;

	/**
	 * The upper-right corner.
	 */
	private Point cornerUR;

	/**
	 * The down-right corner.
	 */
	private Point cornerDR;

	/**
	 * The down-left corner.
	 */
	private Point cornerDL;

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

	/**
	 * These point transform objects represent the affine
	 * transform that must be applied to a point based on
	 * the user dragging and dropping the point.
	 */
	private PointTransform transUR = new PointTransform();
	private PointTransform transUL = new PointTransform();
	private PointTransform transDR = new PointTransform();
	private PointTransform transDL = new PointTransform();
	protected PointTransform transImage = new PointTransform();
	protected PointTransform transCanvas = new PointTransform();

	/**
	 * The scale factor.
	 */
	private double scaleFactor;

	/**
	 * The buffered image representing the page.
	 */
	private BufferedImage img;

	/**
	 * The (x, y) position of the image
	 */
	private Point imgPosition;

	/**
	 * the color to display points when they are
	 * being dragged.
	 */
	private static Color draggingColor = Color.RED;

	/**
	 * the color in which to draw the points when
	 * they are not being dragged
	 */
	private static Color staticColor = Color.BLUE;

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

		this.mode = EditPanelMode.STANDARD;
		
		this.cornerUL = new Point();
		this.cornerUR = new Point();
		this.cornerDR = new Point();
		this.cornerDL = new Point();

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
		if (this.scaleFactor > 0.2)
			this.scaleFactor -= 0.2;
		this.repaint();
	}

	/**
	 * Updates the corners when the current page changes.
	 */
	public void updateCornersOnPanel() {

		Page page = Parameters.getCoreManager().getWorkingPage();

		if (page != null){
			if (page.corners() != null ) {

				Corners corners = page.corners();
				
				// shift the original corners according to those in the working page
				cornerUL = new Point((int) corners.upleft().getX(), (int) corners.upleft().getY());
				cornerUR = new Point((int) corners.upright().getX(), (int) corners.upright().getY());
				cornerDR = new Point((int) corners.downright().getX(), (int) corners.downright().getY());
				cornerDL = new Point((int) corners.downleft().getX(), (int) corners.downleft().getY());
				
				// reset translation to zero
				this.transUL = new PointTransform();
				this.transUR = new PointTransform();
				this.transDR = new PointTransform();
				this.transDL = new PointTransform();
				this.transImage = new PointTransform();
				this.transCanvas = new PointTransform();

				this.updateConnectingLines();
				this.repaint();
			}
		}
	}

	/**
	 * The paintComponent method.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D brush = (Graphics2D) g;

		CoreManager cm = Parameters.getCoreManager();
		Page wp = null;
		if (cm != null) {
			wp = cm.getWorkingPage();
		}

		// break if anything is null
		if (cm == null || wp == null) return;

		// calculate the initial scale factor if we are switching to a
		// new working image
		if (this.img != cm.getProcessedImage()) {
			this.scaleFactor = 1;
			this.img = Parameters.getCoreManager().getProcessedImage();

			// set the initial corners when we change images
			updateCornersOnPanel();
			
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

			double cornerX = (this.getWidth() - this.img.getWidth()) / 2;
			double cornerY = (this.getHeight() - this.img.getHeight()) / 2;

			double newX = this.scaleFactor * (cornerX - this.getWidth()/2 + transImage.dx + transCanvas.dx) + this.getWidth()/2 ;
			double newY = this.scaleFactor * (cornerY - this.getHeight()/2 + transImage.dy + transCanvas.dy) + this.getHeight()/2;
			this.imgPosition = new Point((int) newX, (int) newY);

			brush.drawImage(this.img, (int) newX, (int) newY, newW, newH, null);
			
		}
		
		switch (this.mode) {
			case STANDARD :
				this.paintStandardMode(brush);
				break;
			case HSPLIT :
				horizontalSplit.paint(brush);
				break;
			case VSPLIT :
				verticalSplit.paint(brush);
				break;
		}

	}


	/****************************************
	 * 
	 * Private Methods
	 * 
	 ****************************************/

	
	private void paintStandardMode(Graphics2D brush) {

		Page wp = Parameters.getCoreManager().getWorkingPage();

		// apply the transforms to the corners
		this.scaleAndTranslate(this.cornerUR, this.transUR, this.transCanvas, this.drawableUR);
		this.scaleAndTranslate(this.cornerUL, this.transUL, this.transCanvas, this.drawableUL);
		this.scaleAndTranslate(this.cornerDL, this.transDL, this.transCanvas, this.drawableDL);
		this.scaleAndTranslate(this.cornerDR, this.transDR, this.transCanvas, this.drawableDR);

		// make the lines connect to the new point location
		this.updateConnectingLines();
		
		// update the corner positions with respect to the
		// actual image coordinates
		this.updateCorners(wp.corners());

		// draw lines
		brush.setColor(Color.BLACK);
		brush.draw(this.lineULUR);
		brush.draw(this.lineURDR);
		brush.draw(this.lineDRDL);
		brush.draw(this.lineDLUL);

		// draw points
		setCornerColor(this.transUR, brush);
		brush.draw(this.drawableUR);
		brush.fill(this.drawableUR);

		setCornerColor(this.transUL, brush);
		brush.draw(this.drawableUL);
		brush.fill(this.drawableUL);

		setCornerColor(this.transDR, brush);
		brush.draw(this.drawableDR);
		brush.fill(this.drawableDR);

		setCornerColor(this.transDL, brush);
		brush.draw(this.drawableDL);
		brush.fill(this.drawableDL);
	}
	
	/**
	 * Set the corner color based on whether or not the
	 * corner is currently being dragged.
	 * 
	 * @param pt - the PointTransform for the corner to draw
	 * @param g - the graphics object used to draw the corner
	 */
	private void setCornerColor(PointTransform pt, Graphics2D g) {
		if (pt.dragging) {
			g.setColor(draggingColor);
		} else {
			g.setColor(staticColor);
		}
	}
	
	/**
	 * This is the core method implementing the transformations necessary
	 * for the pan and zoom interface of the edit panel.
	 * 
	 * @param corner - the point to transform
	 * @param pointTransform - gives the x and y translation of the corner to transform
	 * @param canvasTransform - gives the x and y translation of the canvas
	 * @param drawable - the Ellipse2D object that will get drawn onscreen
	 */
	protected void scaleAndTranslate(Point corner, PointTransform pointTransform,
			PointTransform canvasTransform, Ellipse2D drawable) {
		
		double marginX = (this.getWidth() - this.img.getWidth()) / 2;
		double marginY = (this.getHeight() - this.img.getHeight()) / 2;
		double cornerX = corner.x + marginX;
		double cornerY = corner.y + marginY;
		double x = this.scaleFactor * (cornerX - this.getWidth()/2 + pointTransform.dx + canvasTransform.dx) + this.getWidth()/2 ;
		double y = this.scaleFactor * (cornerY - this.getHeight()/2 + pointTransform.dy + canvasTransform.dy) + this.getHeight()/2;
		this.moveCornerTo(drawable, x, y);
		
	}
	
	/**
	 * Updates all lines connecting the corners.
	 */
	protected void updateConnectingLines() {
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
	protected void moveCornerTo(Ellipse2D corner, double x, double y) {
		corner.setFrame(x, y, ELLIPSE_RADIUS * this.scaleFactor, ELLIPSE_RADIUS * this.scaleFactor);
	}


	/**
	 * Update the corner locations, in pixel coordinates of the actual
	 * image (not the display canvas), based on the user's dragging of
	 * the corner point
	 * 
	 * @param c - the Corners instance to update
	 */
	private void updateCorners(Corners c) {
		// 1. top left
		Point p = new Point((int) (this.cornerUL.x+this.transUL.dx-transImage.dx),
				(int) (this.cornerUL.y+this.transUL.dy-transImage.dy));
		c.setUpLeft(p);
		
		// 2. top right
		p = new Point((int) (this.cornerUR.x+this.transUR.dx-transImage.dx),
				(int) (this.cornerUR.y+this.transUR.dy-transImage.dy));
		c.setUpRight(p);
		
		// 3. bottom right
		p = new Point((int) (this.cornerDR.x+this.transDR.dx-transImage.dx),
				(int) (this.cornerDR.y+this.transDR.dy-transImage.dy));
		c.setDownRight(p);
		
		// 4. bottom left
		p = new Point((int) (this.cornerDL.x+this.transDL.dx-transImage.dx),
				(int) (this.cornerDL.y+this.transDL.dy-transImage.dy));
		c.setDownLeft(p);
	}
	
	/**
	 * Update the lines which connect the four corner points.
	 * 
	 * @param line - the {@link Line2D} object to update
	 * @param cornerA - one of the corners connected by the line
	 * @param cornerB - the second corner connected by the line
	 */
	protected void moveLine(Line2D line, Ellipse2D cornerA, Ellipse2D cornerB) {
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
	protected boolean isWithinCornerEllipse(Ellipse2D ellipse, double mX, double mY) {

		int limit = (int) (40 * this.scaleFactor);
		return (ellipse.getCenterX() - limit <= mX && mX <= ellipse.getCenterX() + limit &&
				ellipse.getCenterY() - limit <= mY && mY <= ellipse.getCenterY() + limit);
	}

	/**
	 * Returns true if the mouse press event is within the
	 * image
	 * 
	 * @param mX The x-mouse-coordinate
	 * @param mY The y-mouse-coordinate
	 */
	protected boolean isWithinImage(double mX, double mY) {
		int imgX = this.imgPosition.x;
		int imgY = this.imgPosition.y;
		return (mX > imgX && mX < (imgX + this.img.getWidth() * this.scaleFactor) &&
				mY > imgY && mY < (imgY + this.img.getHeight() * this.scaleFactor));
	}

	/****************************************
	 * 
	 * Event Listener Methods
	 * 
	 ****************************************/

	private void mouseDraggedStandard(MouseEvent e) {

		// move the image
		if (this.transImage.dragging) this.dragPoint(transImage, e);
		else if (this.transUL.dragging) this.dragPoint(this.transUL, e);
		else if (this.transUR.dragging) this.dragPoint(this.transUR, e);
		else if (this.transDL.dragging) this.dragPoint(this.transDL, e);
		else if (this.transDR.dragging) this.dragPoint(this.transDR, e);
		else if (this.transCanvas.dragging) this.dragPoint(this.transCanvas, e);

	}
	
	protected void dragPoint(PointTransform point, MouseEvent e) {
		
		double newX = e.getX() - point.dragX;
		double newY = e.getY() - point.dragY;

		point.dragX += newX;
		point.dragY += newY;

		point.dx += newX / this.scaleFactor;
		point.dy += newY / this.scaleFactor;
	
	}
	
	/**
	 * Handles mouse dragging. If the user clicks on a corner,
	 * they can drag it around.
	 */
	public void mouseDragged(MouseEvent e) {

		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		if (this.mode == EditPanelMode.STANDARD) {
			this.mouseDraggedStandard(e);
		} else if (this.mode == EditPanelMode.HSPLIT) {
			horizontalSplit.mouseDragged(e);
		} else if (this.mode == EditPanelMode.VSPLIT) {
			verticalSplit.mouseDragged(e);
		}
		
		this.repaint();
	}

	/**
	 * Update the scale factor when the mouse wheel
	 * is scrolled up or down.
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {

		int notches = e.getWheelRotation();

		if (notches < 0) {
			if (this.scaleFactor > 0.2)
				this.scaleFactor -= 0.2;
		} else {
			this.scaleFactor += 0.2;
		}

		this.repaint();
	}

	
	public void mousePressed(MouseEvent e) {
		
		if (this.mode == EditPanelMode.STANDARD) {
			this.mousePressedStandard(e);
		} else if (this.mode == EditPanelMode.HSPLIT) {
			horizontalSplit.mousePressed(e);
		} else if (this.mode == EditPanelMode.VSPLIT) {
			verticalSplit.mousePressed(e);
		}

	}
	
	/**
	 * On mouse press, set the initial location for 
	 * a draw event, if the mouse press is within
	 * the radius of one of the ellipses.
	 * 
	 * Also set the cursor to a hand.
	 */
	public void mousePressedStandard(MouseEvent e) {

		// set the hand cursor
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// upper left corner
		if (this.isWithinCornerEllipse(this.drawableUL, e.getX(), e.getY())) {
			this.registerMousePress(this.transUL, e);
		}

		// upper right corner
		else if (this.isWithinCornerEllipse(this.drawableUR, e.getX(), e.getY())) {
			this.registerMousePress(this.transUR, e);
		}

		// lower left corner
		else if (this.isWithinCornerEllipse(this.drawableDL, e.getX(), e.getY())) {
			this.registerMousePress(this.transDL, e);
		}

		// lower right corner
		else if (this.isWithinCornerEllipse(this.drawableDR, e.getX(), e.getY())) {
			this.registerMousePress(this.transDR, e);
		}

		// the image
		else if (this.isWithinImage(e.getX(), e.getY())) {
			this.registerMousePress(this.transImage, e);
		}

		// otherwise drag the canvas
		else {
			this.registerMousePress(this.transCanvas, e);
		}

	}
	
	/**
	 * Mutates the PointTransform data in order to
	 * register the beginning of a drag event.
	 * 
	 * @param point - the PointTransform to update
	 * @param e - the MouseEvent giving the coordinates where
	 * 	the drag event has begun
	 */
	protected void registerMousePress(PointTransform point, MouseEvent e) {
		point.dragX = e.getX();
		point.dragY = e.getY();
		point.dragging = true;
	}

	/**
	 * Restore the default cursor upon mouse release.
	 * Also reset each corner point to a non-dragging state.
	 */
	public void mouseReleased(MouseEvent e) {
		this.setCursor(Cursor.getDefaultCursor());

		this.transUR.dragging = false;
		this.transUL.dragging = false;
		this.transDR.dragging = false;
		this.transDL.dragging = false;
		this.transImage.dragging = false;
		this.transCanvas.dragging = false;
		
		if (this.verticalSplit != null) verticalSplit.mouseReleased();
		if (this.horizontalSplit != null) horizontalSplit.mouseReleased();
		
		this.repaint();
	}

	/**
	 * Un-implemented mouse listener methods
	 */
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent arg0) {}
	
	/****************************************
	 * 
	 * SPLIT HORIZONTAL AND SPLIT VERTICAL
	 * 
	 ****************************************/
	
	public EditPanelMode getEditPanelMode() {
		return this.mode;
	}
	
	public void toggleHorizontalFlipMode() {
		
		// apply the horizontal split and toggle back to standard mode
		if (this.mode == EditPanelMode.HSPLIT) {
			
			Corners box1 = this.horizontalSplit.getFirstPageCorners();
			Corners box2 = this.horizontalSplit.getSecondPageCorners();
			
			// actually split into two pages
			Parameters.getCoreManager().applySplit(box1, box2);
			
			this.mode = EditPanelMode.STANDARD;
		}
		
		// apply the vertical split and toggle back to standard mode
		else if (this.mode == EditPanelMode.VSPLIT) {
			
			Corners box1 = this.verticalSplit.getFirstPageCorners();
			Corners box2 = this.verticalSplit.getSecondPageCorners();
			
			// actually split into two pages
			Parameters.getCoreManager().applySplit(box1, box2);
			
			this.mode = EditPanelMode.STANDARD;
		}
		
		// otherwise toggle to horizontal split mode
		else if (this.mode == EditPanelMode.STANDARD){
		
			this.mode = EditPanelMode.HSPLIT;
			
			// get the corners from the working page
			Page wp = Parameters.getCoreManager().getWorkingPage();
			
			// midpoint between top left and bottom left
			Point mid1 = this.midpoint(wp.corners().upleft(), wp.corners().downleft());
			
			// midpoint between top right and bottom right
			Point mid2 = this.midpoint(wp.corners().upright(), wp.corners().downright());
			
			// make a "margin" between the points
			Point mid1up = new Point(mid1.x, mid1.y - 10);
			Point mid2up = new Point(mid2.x, mid2.y - 10);
			Point mid1down = new Point(mid1.x, mid1.y + 10);
			Point mid2down = new Point(mid2.x, mid2.y + 10);
			
			Corners box1 = new Corners(wp.corners().upleft(), wp.corners().upright(), mid1up, mid2up);
			Corners box2 = new Corners(mid1down, mid2down, wp.corners().downleft(), wp.corners().downright());
			
			this.horizontalSplit = new SplitMode(box1, box2, this);
		
		}
		
		this.repaint();
	}
	
	public void toggleVerticalFlipMode() {
		
		// toggle back to standard mode---cancel button was pressed
		if (this.mode == EditPanelMode.VSPLIT) {			
			this.mode = EditPanelMode.STANDARD;
		}
		
		// toggle back to standard mode---cancel button was pressed
		else if (this.mode == EditPanelMode.HSPLIT) {
			this.mode = EditPanelMode.STANDARD;
		}
		
		// otherwise toggle to horizontal split mode
		else if (this.mode == EditPanelMode.STANDARD){
		
			this.mode = EditPanelMode.VSPLIT;
			
			// get the corners from the working page
			Page wp = Parameters.getCoreManager().getWorkingPage();
			
			// midpoint between top left and top right
			Point mid1 = this.midpoint(wp.corners().upleft(), wp.corners().upright());
			
			// midpoint between bottom left and bottom right
			Point mid2 = this.midpoint(wp.corners().downleft(), wp.corners().downright());
			
			// make a "margin" between the points
			Point mid1left = new Point(mid1.x - 10, mid1.y);
			Point mid2left = new Point(mid2.x - 10, mid2.y);
			Point mid1right = new Point(mid1.x + 10, mid1.y);
			Point mid2right = new Point(mid2.x + 10, mid2.y);
			
			Corners box1 = new Corners(wp.corners().upleft(), mid1left, wp.corners().downleft(), mid2left);
			Corners box2 = new Corners(mid1right, wp.corners().upright(), mid2right, wp.corners().downright());
			
			this.verticalSplit = new SplitMode(box1, box2, this);
			
		}
		
		this.repaint();
	}
	
	private Point midpoint(Point p1, Point p2) {
		int x = (p1.x + p2.x) / 2;
		int y = (p1.y + p2.y) / 2;
		return new Point(x, y);
	}
}
