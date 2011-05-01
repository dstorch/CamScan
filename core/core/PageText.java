package core;

import java.util.*;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class PageText {
	
	private String _fullText;
	private List<Position> _positions;

	public PageText(String fullText) {
		_fullText = fullText;
		_positions = new LinkedList<Position>();
	}
	
	public String fullText() {
		return _fullText;
	}
	public List<Position> positions() {
		return _positions;
	}
	
	public void addPosition(Position p) {
		_positions.add(p);
	}
	
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
