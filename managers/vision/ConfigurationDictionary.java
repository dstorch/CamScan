package vision;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ConfigurationDictionary {
	HashMap<String, ConfigurationValue> map;
	public ConfigurationDictionary(){
		this.map = new HashMap<String, ConfigurationValue>();
	}
	public ConfigurationDictionary(Element config){		
		HashMap<String, ConfigurationValue.ValueType> elementNames = new HashMap<String, ConfigurationValue.ValueType>();
		elementNames.put("COLORTEMP", ConfigurationValue.ValueType.ColorTemperature);
		elementNames.put("CONTRASTBOOST", ConfigurationValue.ValueType.ContrastBoost);
		elementNames.put("FLIPH", ConfigurationValue.ValueType.FlipHorizontal);
		elementNames.put("FLIPV", ConfigurationValue.ValueType.FlipVertical);
		elementNames.put("BILATERAL", ConfigurationValue.ValueType.BilateralFilter);
		
		for(Object _key: elementNames.keySet()){
			String key = (String)_key;
			for (Iterator i = config.elementIterator(key); i.hasNext();) {
				Element element = (Element) i.next();
				String value = element.attribute("value").getStringValue();
				ConfigurationValue.ValueType currentType;
				
				//add it to me!
				
			}
		}
	}
	public ConfigurationValue getKey(String key){
		return this.map.get(key);
	}
	public void setKey(String key, ConfigurationValue value){
		this.map.put(key, value);
	}
	public Set getAllKeys(){
		return this.map.keySet();
	}
	
	public void serialize(Element root) {
		Element config = DocumentHelper.createElement("CONFIG");
		root.add(config);
		
		HashMap<ConfigurationValue.ValueType, String> elementNames = new HashMap<ConfigurationValue.ValueType, String>();
		elementNames.put(ConfigurationValue.ValueType.ColorTemperature, "COLORTEMP");
		elementNames.put(ConfigurationValue.ValueType.ContrastBoost, "CONTRASTBOOST");
		elementNames.put(ConfigurationValue.ValueType.FlipHorizontal, "FLIPH");
		elementNames.put(ConfigurationValue.ValueType.FlipVertical, "FLIPV");
		elementNames.put(ConfigurationValue.ValueType.BilateralFilter, "BILATERAL");
		
		for(Object _key: this.getAllKeys()){
			String key = (String)_key;
			ConfigurationValue currentValue = this.getKey(key);
			
			Element e;
			if (elementNames.containsKey(currentValue.type)){
				e = DocumentHelper.createElement( elementNames.get(key) );
			}else{
				e = DocumentHelper.createElement(key);
				System.err.println("Not sure how to serialize a Config/"+key + " element!");
			}
			e.addAttribute("value", currentValue.value().toString());
			config.add(e);
		}
		
	}
}
