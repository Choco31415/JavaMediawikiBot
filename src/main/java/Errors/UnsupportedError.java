package Errors;

@SuppressWarnings("serial")
public class UnsupportedError extends Error {
	
	public UnsupportedError() { }
	
	public UnsupportedError(String message) {
		super(message);
	}
}