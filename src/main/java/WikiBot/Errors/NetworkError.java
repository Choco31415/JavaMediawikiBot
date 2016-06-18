package WikiBot.Errors;

@SuppressWarnings("serial")
public class NetworkError extends Error {
	
	public NetworkError() { }
	
	public NetworkError(String message) {
		super(message);
	}
}
