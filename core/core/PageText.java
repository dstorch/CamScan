package core;

import java.util.*;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/*******************************************************************
 * PageText
 *
 * Represents the extracted OCR text and the bounding box
 * information for each word.
 * 
 * @author dstorch
 * 
 *******************************************************************/

public class PageText {
	
	private String _fullText;
	private List<Position> _positions;

	/*******************************************************************
	 * 
	 * CONSTRUCTORS
	 * 
	 *******************************************************************/
	
	public PageText() {
		_positions = new LinkedList<Position>();
		_fullText = "";
	}
	
	public PageText(String fullText) {
		_fullText = fullText;
		_positions = new LinkedList<Position>();
	}
	
	/*******************************************************************
	 * 
	 * GETTERS
	 * 
	 *******************************************************************/
	
	public String fullText() {
		return _fullText;
	}
	public List<Position> positions() {
		return _positions;
	}
	
	public void addPosition(Position p) {
		_positions.add(p);
	}
	
	public void setFullText(String text) {
		_fullText = text;
	}
	
	/**
	 * Adds this object to the dom4j document before
	 * it is written to disk.
	 * 
	 * @param root - the root element of the dom4j document
	 */
	public void serialize(Element root) {
		Element text = DocumentHelper.createElement("TEXT");
		root.add(text);
		
		Element fulltext = DocumentHelper.createElement("FULLTEXT");
		fulltext.setText(fullText());
		text.add(fulltext);
		
		Element positions = DocumentHelper.createElement("POSITIONS");
		for (Position p : positions()) {
			p.serialize(positions);
		}
		text.add(positions);
	}
	
}
