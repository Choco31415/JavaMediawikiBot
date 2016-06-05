package WikiBot.ContentRep;


public class Interwiki extends PageLocationContainer {
	
	private int openPos;
	private int closePos;
	
	public Interwiki(String pageName_, String lan_, int openPos_, int closePos_) {
		super(pageName_, lan_);
		openPos = openPos_;
		closePos = closePos_;
	}
	
	//Get information.
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return Where the object starts. In this example link, it returns the position *: *[[Variable]]
	 */
	public int getOuterOpeningPosition() {
		return openPos;
	}
	
	/**
	 * @return Where the object starts, inside the object's enclosing brackets. In this example link, it returns the position *: [[*Variable]]
	 */
	public int getInnerOpeningPosition() {
		return openPos + 2;
	}
	
	/**
	 * @return Where the object ends, inside the object's enclosing brackets. In this example link, it returns the position *: [[Variable*]]
	 */
	public int getInnerClosingPosition() {
		return closePos - 2;
	}
	
	/**
	 * @return Where the object ends. In this example link, it returns the position *: [[Variable]]*
	 */
	public int getOuterClosingPosition() {
		return closePos;
	}
	
	@Override
	public String toString() {
		String output;

		output = pl.getLanguage() + ":" + pl.getTitle() + " (at: " + openPos + ")";
		return output;
	}
}
