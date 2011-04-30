package vision;

import java.util.HashMap;
import java.util.Set;

public class ConfigurationDictionary {
	HashMap<String, ConfigurationValue> map;
	public ConfigurationDictionary(){
		this.map = new HashMap<String, ConfigurationValue>();
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
}
