package vision;



public class ConfigurationValue {
	
	private double _dbl;
	private int _int;
	private boolean _bool;
	public ValueType type;
	
	public static enum ValueType{
		FlipHorizontal, FlipVertical, ColorTemperature, ContrastBoost, BilateralFilter;
	}
	
	public ConfigurationValue(ValueType type, int value) throws InvalidTypingException{
		if (type != ValueType.ColorTemperature){
			throw new InvalidTypingException("Configuration type and value do not match");
		}
		this._int = value;
		this.type = type;
	}
	public ConfigurationValue(ValueType type, double value) throws InvalidTypingException{
		if (true){
			throw new InvalidTypingException("Configuration type and value do not match");
		}
		this._dbl = value;
		this.type = type;
	}
	public ConfigurationValue(ValueType type, boolean value) throws InvalidTypingException{
		if (type != ValueType.FlipHorizontal && type != ValueType.FlipVertical && type != ValueType.ContrastBoost && type != ValueType.BilateralFilter){
			throw new InvalidTypingException("Configuration type and value do not match");
		}
		this._bool = value;
		this.type = type;
	}
	
	
	public Object value(){
		if (this.type == ValueType.ColorTemperature){
			return (Object)new Integer(this._int);
		}else if (this.type == ValueType.FlipHorizontal || this.type == ValueType.FlipVertical || this.type == ValueType.ContrastBoost || this.type == ValueType.BilateralFilter){
			return (Object)new Boolean(this._bool);
		}else if (false){
			return (Object)new Double(this._dbl);
		}
		System.err.println("ConfigurationValue doesn't know how to return a value for its type!");
		return null;
	}
}