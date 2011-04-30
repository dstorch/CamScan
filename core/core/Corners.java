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
	
}