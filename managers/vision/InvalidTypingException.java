package vision;

/*
 * Exception raised when a ConfigurationValue has the wrong value for its data type.
 */
public class InvalidTypingException extends Exception {
	public InvalidTypingException(String msg){
		super(msg);
	}
}
