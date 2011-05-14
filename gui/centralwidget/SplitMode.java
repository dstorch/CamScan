package centralwidget;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import core.Corners;

@SuppressWarnings("serial")
public class SplitMode extends EditPanel {

	private EditPanel editPanel;
	
	// the original corners
	private Corners box1;
	private Corners box2;
	
	// the translated corners
	private Corners box1updated;
	private Corners box2updated;
	
	/**
	 * the color to display points when they are
	 * being dragged.
	 */
	private static Color draggingColor = Color.RED;

	/**
	 * the color in which to draw the first set of points when
	 * they are not being dragged
	 */
	private static Color staticColor1 = Color.GREEN;
	
	/**
	 * the color in which to draw the first set of points when
	 * they are not being dragged
	 */
	private static Color staticColor2 = Color.ORANGE;
	
	/**
	 * The actual drawn ellipses. These
	 * differ from cornerUR in that they
	 * have the transform applied to them,
	 * making them appear correctly on screen.
	 */
	private Ellipse2D drawableUR1;
	private Ellipse2D drawableUL1;
	private Ellipse2D drawableDR1;
	private Ellipse2D drawableDL1;
	private Ellipse2D drawableUR2;
	private Ellipse2D drawableUL2;
	private Ellipse2D drawableDR2;
	private Ellipse2D drawableDL2;;
	
	
	/**
	 * These point transform objects represent the affine
	 * transform that must be applied to a point based on
	 * the user dragging and dropping the point.
	 */
	private PointTransform transUR1 = new PointTransform();
	private PointTransform transUL1 = new PointTransform();
	private PointTransform transDR1 = new PointTransform();
	private PointTransform transDL1 = new PointTransform();
	private PointTransform transUR2 = new PointTransform();
	private PointTransform transUL2 = new PointTransform();
	private PointTransform transDR2 = new PointTransform();
	private PointTransform transDL2 = new PointTransform();
	
	
	/**
	 * Connecting lines for each of the two boxes
	 */
	private Line2D lineULUR1;
	private Line2D lineURDR1;
	private Line2D lineDRDL1;
	private Line2D lineDLUL1;
	private Line2D lineULUR2;
	private Line2D lineURDR2;
	private Line2D lineDRDL2;
	private Line2D lineDLUL2;
	
	
	public SplitMode(Corners box1, Corners box2, EditPanel panel) {
		this.box1 = box1;
		this.box2 = box2;
		this.editPanel = panel;
		
		this.box1updated = new Corners();
		this.box2updated = new Corners();
		
		this.drawableUL1 = new Ellipse2D.Double();
		this.drawableUR1 = new Ellipse2D.Double();
		this.drawableDR1 = new Ellipse2D.Double();
		this.drawableDL1 = new Ellipse2D.Double();
		this.drawableUL2 = new Ellipse2D.Double();
		this.drawableUR2 = new Ellipse2D.Double();
		this.drawableDR2 = new Ellipse2D.Double();
		this.drawableDL2 = new Ellipse2D.Double();

		this.lineULUR1 = new Line2D.Double();
		this.lineURDR1 = new Line2D.Double();
		this.lineDRDL1 = new Line2D.Double();
		this.lineDLUL1 = new Line2D.Double();
		this.lineULUR2 = new Line2D.Double();
		this.lineURDR2 = new Line2D.Double();
		this.lineDRDL2 = new Line2D.Double();
		this.lineDLUL2 = new Line2D.Double();
	}
	
	/**
	 * Set the corner color based on whether or not the
	 * corner is currently being dragged.
	 * 
	 * @param pt - the PointTransform for the corner to draw
	 * @param g - the graphics object used to draw the corner
	 */
	private void setCornerColor(PointTransform pt, Graphics2D g, boolean isBox1) {
		if (pt.dragging) {
			g.setColor(draggingColor);
		} else if (isBox1) {
			g.setColor(staticColor1);
		} else {
			g.setColor(staticColor2);
		}
	}

	public void paint(Graphics2D brush) {

		// apply the transforms to the corners
		editPanel.scaleAndTranslate(this.box1.upright(), this.transUR1, editPanel.transCanvas, this.drawableUR1);
		editPanel.scaleAndTranslate(this.box1.upleft(), this.transUL1, editPanel.transCanvas, this.drawableUL1);
		editPanel.scaleAndTranslate(this.box1.downleft(), this.transDL1, editPanel.transCanvas, this.drawableDL1);
		editPanel.scaleAndTranslate(this.box1.downright(), this.transDR1, editPanel.transCanvas, this.drawableDR1);
		
		editPanel.scaleAndTranslate(this.box2.upright(), this.transUR2, editPanel.transCanvas, this.drawableUR2);
		editPanel.scaleAndTranslate(this.box2.upleft(), this.transUL2, editPanel.transCanvas, this.drawableUL2);
		editPanel.scaleAndTranslate(this.box2.downleft(), this.transDL2, editPanel.transCanvas, this.drawableDL2);
		editPanel.scaleAndTranslate(this.box2.downright(), this.transDR2, editPanel.transCanvas, this.drawableDR2);

		// make the lines connect to the new point location
		this.updateConnectingLinesSplitMode();
		
		// update the corners so that they will be correct
		// when the "apply split" function is called
		this.updateCorners(box1updated, box2updated);
		
		// draw lines
		brush.setColor(Color.BLACK);
		brush.draw(this.lineULUR1);
		brush.draw(this.lineURDR1);
		brush.draw(this.lineDRDL1);
		brush.draw(this.lineDLUL1);
		brush.draw(this.lineULUR2);
		brush.draw(this.lineURDR2);
		brush.draw(this.lineDRDL2);
		brush.draw(this.lineDLUL2);

		// draw points
		setCornerColor(this.transUR1, brush, true);
		brush.draw(this.drawableUR1);
		brush.fill(this.drawableUR1);
		setCornerColor(this.transUL1, brush, true);
		brush.draw(this.drawableUL1);
		brush.fill(this.drawableUL1);
		setCornerColor(this.transDR1, brush, true);
		brush.draw(this.drawableDR1);
		brush.fill(this.drawableDR1);
		setCornerColor(this.transDL1, brush, true);
		brush.draw(this.drawableDL1);
		brush.fill(this.drawableDL1);
		
		setCornerColor(this.transUR2, brush, false);
		brush.draw(this.drawableUR2);
		brush.fill(this.drawableUR2);
		setCornerColor(this.transUL2, brush, false);
		brush.draw(this.drawableUL2);
		brush.fill(this.drawableUL2);
		setCornerColor(this.transDR2, brush, false);
		brush.draw(this.drawableDR2);
		brush.fill(this.drawableDR2);
		setCornerColor(this.transDL2, brush, false);
		brush.draw(this.drawableDL2);
		brush.fill(this.drawableDL2);
	}
	
	public void mouseDragged(MouseEvent e) {
		if (this.transImage.dragging) editPanel.dragPoint(transImage, e);
		else if (this.transUL1.dragging) editPanel.dragPoint(this.transUL1, e);
		else if (this.transUR1.dragging) editPanel.dragPoint(this.transUR1, e);
		else if (this.transDL1.dragging) editPanel.dragPoint(this.transDL1, e);
		else if (this.transDR1.dragging) editPanel.dragPoint(this.transDR1, e);
		else if (this.transUL2.dragging) editPanel.dragPoint(this.transUL2, e);
		else if (this.transUR2.dragging) editPanel.dragPoint(this.transUR2, e);
		else if (this.transDL2.dragging) editPanel.dragPoint(this.transDL2, e);
		else if (this.transDR2.dragging) editPanel.dragPoint(this.transDR2, e);
		else if (editPanel.transImage.dragging) editPanel.dragPoint(editPanel.transImage, e);
		else if (editPanel.transCanvas.dragging) editPanel.dragPoint(editPanel.transCanvas, e);
	}
	
	public void mousePressed(MouseEvent e) {
		// set the hand cursor
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// register drags for the corners of box1
		if (this.isWithinCornerEllipse(this.drawableUL1, e.getX(), e.getY())) {
			this.registerMousePress(this.transUL1, e);
		} else if (this.isWithinCornerEllipse(this.drawableUR1, e.getX(), e.getY())) {
			this.registerMousePress(this.transUR1, e);
		} else if (this.isWithinCornerEllipse(this.drawableDL1, e.getX(), e.getY())) {
			this.registerMousePress(this.transDL1, e);
		} else if (this.isWithinCornerEllipse(this.drawableDR1, e.getX(), e.getY())) {
			this.registerMousePress(this.transDR1, e);
		}
		
		// register drags for the corners of box2
		else if (this.isWithinCornerEllipse(this.drawableUL2, e.getX(), e.getY())) {
			this.registerMousePress(this.transUL2, e);
		} else if (this.isWithinCornerEllipse(this.drawableUR2, e.getX(), e.getY())) {
			this.registerMousePress(this.transUR2, e);
		} else if (this.isWithinCornerEllipse(this.drawableDL2, e.getX(), e.getY())) {
			this.registerMousePress(this.transDL2, e);
		} else if (this.isWithinCornerEllipse(this.drawableDR2, e.getX(), e.getY())) {
			this.registerMousePress(this.transDR2, e);
		}

		// the image
		else if (editPanel.isWithinImage(e.getX(), e.getY())) {
			editPanel.registerMousePress(editPanel.transImage, e);
		}

		// otherwise drag the canvas
		else {
			editPanel.registerMousePress(editPanel.transCanvas, e);
		}
	}
	
	/**
	 * Updates all lines connecting the corners.
	 */
	private void updateConnectingLinesSplitMode() {
		this.moveLine(this.lineULUR1, this.drawableUL1, this.drawableUR1);
		this.moveLine(this.lineURDR1, this.drawableUR1, this.drawableDR1);
		this.moveLine(this.lineDRDL1, this.drawableDR1, this.drawableDL1);
		this.moveLine(this.lineDLUL1, this.drawableDL1, this.drawableUL1);
		this.moveLine(this.lineULUR2, this.drawableUL2, this.drawableUR2);
		this.moveLine(this.lineURDR2, this.drawableUR2, this.drawableDR2);
		this.moveLine(this.lineDRDL2, this.drawableDR2, this.drawableDL2);
		this.moveLine(this.lineDLUL2, this.drawableDL2, this.drawableUL2);
	}
	
	public void mouseReleased() {
		this.transUR1.dragging = false;
		this.transUL1.dragging = false;
		this.transDR1.dragging = false;
		this.transDL1.dragging = false;
		this.transUR2.dragging = false;
		this.transUL2.dragging = false;
		this.transDR2.dragging = false;
		this.transDL2.dragging = false;
	}
	
	
	/**
	 * Update the corner locations, in pixel coordinates of the actual
	 * image (not the display canvas), based on the user's dragging of
	 * the corner point
	 * 
	 * @param c - the Corners instance to update
	 */
	private void updateCorners(Corners c1, Corners c2) {
		// 1. top left
		Point p = new Point((int) (this.box1.upleft().x+this.transUL1.dx-transImage.dx),
				(int) (this.box1.upleft().y+this.transUL1.dy-transImage.dy));
		c1.setUpLeft(p);
		
		// 2. top right
		p = new Point((int) (this.box1.upright().x+this.transUR1.dx-transImage.dx),
				(int) (this.box1.upright().y+this.transUR1.dy-transImage.dy));
		c1.setUpRight(p);
		
		// 3. bottom right
		p = new Point((int) (this.box1.downright().x+this.transDR1.dx-transImage.dx),
				(int) (this.box1.downright().y+this.transDR1.dy-transImage.dy));
		c1.setDownRight(p);
		
		// 4. bottom left
		p = new Point((int) (this.box1.downleft().x+this.transDL1.dx-transImage.dx),
				(int) (this.box1.downleft().y+this.transDL1.dy-transImage.dy));
		c1.setDownLeft(p);
		
		
		// 1. top left
		p = new Point((int) (this.box2.upleft().x+this.transUL2.dx-transImage.dx),
				(int) (this.box2.upleft().y+this.transUL2.dy-transImage.dy));
		c2.setUpLeft(p);
		
		// 2. top right
		p = new Point((int) (this.box2.upright().x+this.transUR2.dx-transImage.dx),
				(int) (this.box2.upright().y+this.transUR2.dy-transImage.dy));
		c2.setUpRight(p);
		
		// 3. bottom right
		p = new Point((int) (this.box2.downright().x+this.transDR2.dx-transImage.dx),
				(int) (this.box2.downright().y+this.transDR2.dy-transImage.dy));
		c2.setDownRight(p);
		
		// 4. bottom left
		p = new Point((int) (this.box2.downleft().x+this.transDL2.dx-transImage.dx),
				(int) (this.box2.downleft().y+this.transDL2.dy-transImage.dy));
		c2.setDownLeft(p);
	}
	
	public Corners getFirstPageCorners() {
		return this.box1updated;
	}
	
	public Corners getSecondPageCorners() {
		return this.box2updated;
	}
}
