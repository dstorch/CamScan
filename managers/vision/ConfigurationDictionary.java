package vision;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ConfigurationDictionary {
	HashMap<String, ConfigurationValue> map;
	public ConfigurationDictionary(){
		this.map = new HashMap<String, ConfigurationValue>();
	}
	public ConfigurationDictionary(Element config){
		this.map = new HashMap<String, ConfigurationValue>();
		
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
				ConfigurationValue.ValueType currentType = elementNames.get(element.getName());
				
				try{
					if (currentType == ConfigurationValue.ValueType.FlipHorizontal || currentType == ConfigurationValue.ValueType.FlipVertical || currentType == ConfigurationValue.ValueType.BilateralFilter || currentType == ConfigurationValue.ValueType.ContrastBoost){
						this.setKey(new ConfigurationValue(currentType, value.equals("true")? true:false));
					}else if (currentType == ConfigurationValue.ValueType.ColorTemperature){
						this.setKey(new ConfigurationValue(currentType, Integer.parseInt(value)));
					}else{
						System.err.println("Not sure how to process a type!");
					}
				}catch(InvalidTypingException e){
					System.err.println("Couldn't create configuration value for element valued: " + value + " (type " + element.getName() + ").");
				}
			}
		}
	}
	public ConfigurationValue getKey(ConfigurationValue.ValueType key){
		return this.map.get(ConfigurationValue.type2name(key));
	}
	public ConfigurationValue getKeyWithName(String key){
		return this.map.get(key);
	}
	public void setKey(ConfigurationValue value){
		this.map.put(ConfigurationValue.type2name(value.type), value);
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
			ConfigurationValue currentValue = this.getKeyWithName(key);
			
			Element e;
			if (elementNames.containsKey(currentValue.type)){
				e = DocumentHelper.createElement( elementNames.get(currentValue.type) );
			}else{
				e = DocumentHelper.createElement(key);
				System.err.println("Not sure how to serialize a Config/"+key + " element!");
			}
			e.addAttribute("value", currentValue.value().toString());
			config.add(e);
		}
		
	}
	
	private String join(List<String> s, String seperator){
		  if (s == null || seperator == null){return null;}
		  if (s.size() == 0){return "";}
		  
		  StringBuilder out=new StringBuilder();
		  
		  out.append(s.get(0));
		  for(int i=1;i<s.size();i++){
			  out.append(seperator).append(s.get(i));
		  }
		  
		  return out.toString();
	}
	
	public String toString(){
		LinkedList<String> repr = new LinkedList<String>();;
		for(Object _key: this.getAllKeys()){
			String key = (String)_key;
			ConfigurationValue currentValue = this.getKeyWithName(key);
			
			repr.add( key + ": " + currentValue.value().toString() );
		}
		return join(repr, "\n");
	}
}
