package core;

import java.awt.Point;
import org.dom4j.*;

public class Corners {

	private Point _upright;
	private Point _downright;
	private Point _upleft;
	private Point _downleft;
	
	public Corners(Point upleft, Point upright, Point downleft, Point downright) {
		_upright = upright;
		_downright = downright;
		_upleft = upleft;
		_downleft = downleft;
	}
	
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
	
	private void addPointAttribute(Element el, Point point) {
		Integer x = new Integer(point.x);
		Integer y = new Integer(point.y);
		el.addAttribute("x", x.toString());
		el.addAttribute("y", y.toString());
	}
	
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
