package core;

import java.util.*;
import java.awt.Point;
import java.io.*;
import org.dom4j.* ;
import org.dom4j.io.* ;

import vision.ConfigurationDictionary;

/*******************************************************************
 * XMLReader
 *
 * Provides public functionality for parsing document and page XML
 * and instantiating the corresponding objects.
 * 
 * @author dstorch
 * 
 *******************************************************************/

@SuppressWarnings("rawtypes")
public class XMLReader {

	/**
	 * Given a path to the XML file for a document, parses
	 * the XML and returns the corresponding Document object.
	 * 
	 * @param path - path to the document to parse
	 * @return the Document object corresponding to the parsed XML
	 * 
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 */
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
				Attribute nameAtt = pageEl.attribute("name");
				String metafile = metafileAtt.getStringValue();
				String name = nameAtt.getStringValue();

				int order = Integer.parseInt(orderAtt.getStringValue());

				Page p = parsePage(metafile, order, d, name);
				d.addPage(p);
			}
		}

		return d;
	}

	/**
	 * Given a path to page XML, parses the XML and
	 * returns the corresponding Page object
	 * 
	 * @param path - a String indicating the Page XML file
	 * @param order - the page number for this page
	 * @param parent - the Document containing the page
	 * @param name - the Name of the page
	 * @return the Page object instantiated from the XML
	 * 
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 */
	public Page parsePage(String path, int order, Document parent, String name) throws FileNotFoundException, DocumentException {
		Page p = new Page(parent, order, name);
		p.setMetafile(path);

		SAXReader reader = new SAXReader();
		org.dom4j.Document document = reader.read(new FileReader(path));
		Element root = document.getRootElement();

		Attribute nameAttr = root.attribute("name");
		p.setName(nameAttr.getStringValue());

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

	/**
	 * Instantiates and returns a Point from an XML tag
	 * containing x and y attributes.
	 * 
	 * @param element - the dom4j element to extract the Point from
	 * @return the resulting Point object
	 */
	private Point pointFromXML(Element element) {
		String xstr = element.attribute("x").getStringValue();
		String ystr = element.attribute("y").getStringValue();
		int x = Integer.parseInt(xstr);
		int y = Integer.parseInt(ystr);
		return new Point(x, y);
	}

	/**
	 * Instantiates a Corners object based on the corresponding
	 * dom4j element.
	 * 
	 * @param corners - the CORNERS dom4j tag
	 * @return the corresponding Corners object
	 */
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


	/**
	 * Parses a text XML tag
	 * 
	 * @param textEl - the dom4j tag
	 * @return the corresponding PageText object
	 */
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

	/**
	 * Extracts a Position object from a corresponding
	 * dom4j XML tag
	 * 
	 * @param position - the dom4j tag
	 * @return the corresponing Position instance
	 */
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

	
	/**
	 * Parse and return the configuration dictionary
	 * from the corresponding XML.
	 * 
	 * @param config - the dom4j element
	 * @return the corresponding ConfigurationDictionary
	 */
	private ConfigurationDictionary parseConfig(Element config) {
		return new ConfigurationDictionary(config);
	}

}
