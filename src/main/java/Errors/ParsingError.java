package Errors;

@SuppressWarnings("serial")
public class ParsingError extends Error {
	
	public ParsingError() {}
	
	public ParsingError(String message) {
		super(message);
	}
}
