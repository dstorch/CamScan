package core;

import java.awt.Point;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Position {

	private Point _min;
	private Point _max;
	private String _word;
	
	public Position(Point min, Point max, String word) {
		_min = min;
		_max = max;
		_word = word;
	}
	
	public Integer xmin() {
		return _min.x;
	}
	public Integer ymin() {
		return _min.y;
	}
	public Integer xmax() {
		return _max.x;
	}
	public Integer ymax() {
		return _max.y;
	}
	public String word() {
		return _word;
	}
	
	public void serialize(Element positions) {
		Element word = DocumentHelper.createElement("WORD");
		
		word.addAttribute("xmin", xmin().toString());
		word.addAttribute("ymin", ymin().toString());
		word.addAttribute("xmax", xmax().toString());
		word.addAttribute("ymax", ymax().toString());
		
		word.addAttribute("value", word());
		
		positions.add(word);
	}
	
}
