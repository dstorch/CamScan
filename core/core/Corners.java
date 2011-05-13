package core;

import java.awt.Point;
import org.dom4j.*;

/*******************************************************************
 * Corners
 *
 * A container for the four corners of a document. This object is
 * set accordingly based on automatic corner finding, and after
 * user adjustment in edit mode.
 * 
 * @author dstorch
 * 
 *******************************************************************/

public class Corners {

	private Point _upright;
	private Point _downright;
	private Point _upleft;
	private Point _downleft;
	
	// default constructor guesses plausible corner locations
	public Corners() {
		_upleft = new Point(50, 50);
		_downleft = new Point(50, 200);
		_upright = new Point(100, 50);
		_downright = new Point(100, 200);
	}
	
	public Corners(Point upleft, Point upright, Point downleft, Point downright) {
		_upright = upright;
		_downright = downright;
		_upleft = upleft;
		_downleft = downleft;
	}
	

	/*******************************************************************
	 * 
	 * GETTERS
	 * 
	 *******************************************************************/
	
	public Point upright() {
		return _upright;
	}
	public Point downright() {
		return _downright;
	}
	public Point upleft() {
		return _upleft;
	}
	public Point downleft() {
		return _downleft;
	}
	public int width(){
		return _upright.x - _upleft.x;
	}
	public int height(){
		return _downleft.y - _upleft.y;
	}
	
	/*******************************************************************
	 * 
	 * SETTERS
	 * 
	 *******************************************************************/
	
	public void setUpRight(Point p) {
		_upright = p;
	}
	
	public void setUpLeft(Point p) {
		_upleft = p;
	}
	
	public void setDownRight(Point p) {
		_downright = p;
	}
	
	public void setDownLeft(Point p) {
		_downleft = p;
	}
	

	/**
	 * A helper to serialize. Adds a single point
	 * to the CORNERS tag.
	 * 
	 * @param el - the element to which we will add a point
	 * @param point - the Point object to add
	 */
	private void addPointAttribute(Element el, Point point) {
		Integer x = new Integer(point.x);
		Integer y = new Integer(point.y);
		el.addAttribute("x", x.toString());
		el.addAttribute("y", y.toString());
	}
	
	/**
	 * Given the root element of an existing dom4j object,
	 * adds this Corners object to the DOM.
	 * 
	 * @param root - the root dom4j element of an XML document
	 * to write to disk
	 */
	public void serialize(Element root) {
		Element corners = DocumentHelper.createElement("CORNERS");
		root.add(corners);
		
		Element upright = DocumentHelper.createElement("UPRIGHT");
		addPointAttribute(upright, upright());
		corners.add(upright);
		
		Element downright = DocumentHelper.createElement("DOWNRIGHT");
		addPointAttribute(downright, downright());
		corners.add(downright);
		
		Element upleft = DocumentHelper.createElement("UPLEFT");
		addPointAttribute(upleft, upleft());
		corners.add(upleft);
		
		Element downleft = DocumentHelper.createElement("DOWNLEFT");
		addPointAttribute(downleft, downleft());
		corners.add(downleft);
		
	}
	
	public String toString(){
		return String.format("<Corners TL: (%d,%d), TR: (%d,%d), BL: (%d,%d), BR: (%d,%d)>",
				this._upleft.x, this._upleft.y,
				this._upright.x, this._upright.y,
				this._downleft.x, this._downleft.y,
				this._downright.x, this._downright.y);
	}
	
}
