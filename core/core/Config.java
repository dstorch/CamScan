package core;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Config {

	private float _colorTemp;
	private boolean _contrastBoost;
	private boolean _fliph;
	private boolean _flipv;
	
	// this should establish defaults
	public Config() {
		_colorTemp = 0;
		_contrastBoost = false;
		_fliph = false;
		_flipv = false;
	}
	
	public Float colorTemp() {
		return _colorTemp;
	}
	public boolean contrastBoost() {
		return _contrastBoost;
	}
	public boolean flipH() {
		return _fliph;
	}
	public boolean flipV() {
		return _flipv;
	}

	
	public String flipHStr() {
		if (flipH()) return "yes";
		else return "no";
	}
	public String flipVStr() {
		if (flipV()) return "yes";
		else return "no";
	}
	public String constrastBoostStr() {
		if (contrastBoost()) return "yes";
		else return "no";
	}
	
	public void setColorTemp(float temp) {
		_colorTemp = temp;
	}
	public void setContrastBoost(boolean boost) {
		_contrastBoost = boost;
	}
	public void setFlipH(boolean fliph) {
		_fliph = fliph;
	}
	public void setFlipV(boolean flipv) {
		_flipv = flipv;
	}
	
	public void serialize(Element root) {
		Element config = DocumentHelper.createElement("CONFIG");
		root.add(config);
		
		Element colortempEl = DocumentHelper.createElement("COLORTEMP");
		colortempEl.addAttribute("value", colorTemp().toString());
		config.add(colortempEl);
		
		Element boostEl = DocumentHelper.createElement("CONTRASTBOOST");
		boostEl.addAttribute("value", constrastBoostStr());
		config.add(boostEl);
		
		Element fliphEL = DocumentHelper.createElement("FLIPH");
		fliphEL.addAttribute("value", flipHStr());
		config.add(fliphEL);
		
		Element flipvEL = DocumentHelper.createElement("FLIPV");
		flipvEL.addAttribute("value", flipVStr());
		config.add(flipvEL);
		
	}
	
}
