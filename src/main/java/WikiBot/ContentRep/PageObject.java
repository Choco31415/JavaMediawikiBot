package WikiBot.ContentRep;

/**
 * This class is used to represent various features of a page.
 * For example, templates, image, categories, and more.
 */
public abstract class PageObject {
	
	//Name variables will be created in their respective classes.
	protected int openPos;
	protected int closePos;
	protected String myObjectType;//Ex: Category
	
	protected String textOpening;//The text denoting the opening of this object.
	protected String textClosing;//The text denoting the closing of this object.

	public PageObject(String myObjectType_, int openPos_, int closePos_, String textOpening_, String textClosing_) {
		myObjectType = myObjectType_;
		openPos = openPos_;
		closePos = closePos_;
		textOpening = textOpening_;
		textClosing = textClosing_;
	}
	
	//Set information
	public void setOpeningPosition(int index) {
		openPos = index;
	}
	
	//Get information.
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
		return openPos + textOpening.length();
	}
	
	/**
	 * @return Where the object ends, inside the object's enclosing brackets. In this example link, it returns the position *: [[Variable*]]
	 */
	public int getInnerClosingPosition() {
		return closePos - textClosing.length();
	}
	
	/**
	 * @return Where the object ends. In this example link, it returns the position *: [[Variable]]*
	 */
	public int getOuterClosingPosition() {
		return closePos;
	}
	
	/**
	 * @return The object type. For example, Category.
	 */
	public String getObjectType() {
		return myObjectType;
	}
	
	/**
	 * @return The text denoting the beginning of this object. In a link, it should return [[ or [.
	 */
	public String getOpeningString() {
		return textOpening;
	}
	
	/**
	 * @return The text denoting the ending of this object. In a link, it should return ]] or ].
	 */
	public String getClosingString() {
		return textClosing;
	}

	/**
	 * A simple toString method that is 1 line long.
	 * @return A String.
	 */
	public abstract String simpleToString();
	
	public abstract String toString();
	
	//TODO: Add equals method.
}
