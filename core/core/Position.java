package core;

import java.awt.Point;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/*******************************************************************
 * Position
 *
 * Represents a single word extracted by OCR together with its
 * bounding box.
 * 
 * @author dstorch
 * 
 *******************************************************************/

public class Position {

	
	/*******************************************************************
	 * 
	 * PRIVATE INSTANCE VARIABLES
	 * 
	 *******************************************************************/
	
	// the top left point of the bounding box
	private Point _min;
	
	// the bottom right word of the bounding box
	private Point _max;
	
	// the word contained in the bounding box
	private String _word;
	
	
	/**
	 * Constructor
	 * 
	 * @param min
	 * @param max
	 * @param word
	 */
	public Position(Point min, Point max, String word) {
		_min = min;
		_max = max;
		_word = word;
	}
	
	/*******************************************************************
	 * 
	 * GETTERS
	 * 
	 *******************************************************************/
	
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
	
	/**
	 * Adds this position to the DOM before
	 * writing out to disk
	 * 
	 * @param positions - the dom4j POSITION tag to add the word to
	 */
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
