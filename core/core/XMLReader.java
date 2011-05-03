package core;

import java.util.*;
import java.awt.Point;
import java.io.*;
import org.dom4j.* ;
import org.dom4j.io.* ;

import vision.ConfigurationDictionary;

@SuppressWarnings("rawtypes")
public class XMLReader {

	public Document parseDocument(String path) throws FileNotFoundException, DocumentException {
		
		Document d = new Document();
		d.setPathName(path);
		
		SAXReader reader = new SAXReader();
		org.dom4j.Document document = null;
		
		// return null if the document does not exist
		try {
			document = reader.read(new FileReader(path));
		} catch (FileNotFoundException e) {
			return null;
		}
		
		Element root = document.getRootElement();
		
		Attribute nameAttr = root.attribute("name");
		d.setName(nameAttr.getStringValue());
		
		for (Iterator i = root.elementIterator("PAGES"); i.hasNext();) {
			Element pages = (Element) i.next();
			for (Iterator j = pages.elementIterator("PAGE"); j.hasNext();) {
				Element pageEl = (Element) j.next();
				Attribute orderAtt = pageEl.attribute("order");
				Attribute metafileAtt = pageEl.attribute("metafile");
				String metafile = metafileAtt.getStringValue();
				
				int order = Integer.parseInt(orderAtt.getStringValue());
				
				Page p = parsePage(metafile, order, d);
				d.addPage(p);
			}
		}
		
		return d;
	}
	
	private Page parsePage(String path, int order, Document parent) throws FileNotFoundException, DocumentException {
		Page p = new Page(parent, order);
		p.setMetafile(path);
		
		SAXReader reader = new SAXReader();
		org.dom4j.Document document = reader.read(new FileReader(path));
		Element root = document.getRootElement();
		
		
		for (Iterator i = root.elementIterator("IMG"); i.hasNext();) {
			Element element = (Element) i.next();
			Attribute rawAttribute = element.attribute("path");
			Attribute processedAttr = element.attribute("processed");
			
			p.setRawFile(rawAttribute.getStringValue());
			p.setProcessedFile(processedAttr.getStringValue());
		}
		
		// call helpers
		for (Iterator i = root.elementIterator("CORNERS"); i.hasNext();) {
			Element element = (Element) i.next();
			p.setCorners(parseCorners(element));
		}
		for (Iterator i = root.elementIterator("TEXT"); i.hasNext();) {
			Element element = (Element) i.next();
			p.setPageText(parseText(element));
		}
		for (Iterator i = root.elementIterator("CONFIG"); i.hasNext();) {
			Element element = (Element) i.next();
			p.setConfig(parseConfig(element));
		}
		
		if (p.fullText().equals("")) p.launchOcrThread();
		
		return p;
	}
	
	private Point pointFromXML(Element element) {
		String xstr = element.attribute("x").getStringValue();
		String ystr = element.attribute("y").getStringValue();
		int x = Integer.parseInt(xstr);
		int y = Integer.parseInt(ystr);
		return new Point(x, y);
	}
	
	
	private Corners parseCorners(Element corners) {
		
		Point upright = null, upleft = null, downright = null, downleft = null;
		
		for (Iterator i = corners.elementIterator("UPRIGHT"); i.hasNext();) {
			Element element = (Element) i.next();
			upright = pointFromXML(element);
		}
	
		for (Iterator i = corners.elementIterator("UPLEFT"); i.hasNext();) {
			Element element = (Element) i.next();
			upleft = pointFromXML(element);
		}
		
		for (Iterator i = corners.elementIterator("DOWNRIGHT"); i.hasNext();) {
			Element element = (Element) i.next();
			downright = pointFromXML(element);
		}
	
		for (Iterator i = corners.elementIterator("DOWNLEFT"); i.hasNext();) {
			Element element = (Element) i.next();
			downleft = pointFromXML(element);
		}
		
		return new Corners(upleft, upright, downleft, downright);
	}
	
	
	private PageText parseText(Element textEl) {
		String fullText = "";
		for (Iterator i = textEl.elementIterator("FULLTEXT"); i.hasNext();) {
			Element element = (Element) i.next();
			fullText = element.getText();
		}
		
		PageText pageText = new PageText(fullText);
		
		Element positions = null;
		for (Iterator i = textEl.elementIterator("POSITIONS"); i.hasNext();) {
			positions = (Element) i.next();
		}
		
		for (Iterator i = positions.elementIterator("WORD"); i.hasNext();) {
			Element element = (Element) i.next();
			Position position = parsePosition(element);
			pageText.addPosition(position);
		}
		
		return pageText;
	}
	
	private Position parsePosition(Element position) {
		String xminStr = position.attribute("xmin").getStringValue();
		String yminStr = position.attribute("ymin").getStringValue();
		String xmaxStr = position.attribute("xmax").getStringValue();
		String ymaxStr = position.attribute("ymax").getStringValue();
		
		int xmin = Integer.parseInt(xminStr);
		int ymin = Integer.parseInt(yminStr);
		int xmax = Integer.parseInt(xmaxStr);
		int ymax = Integer.parseInt(ymaxStr);
		
		Point min = new Point(xmin, ymin);
		Point max = new Point(xmax, ymax);
		
		String word = position.attribute("value").getStringValue();
		
		return new Position(min, max, word);
	}
	
	private ConfigurationDictionary parseConfig(Element config) {
		return new ConfigurationDictionary(config);
	}
	
}
