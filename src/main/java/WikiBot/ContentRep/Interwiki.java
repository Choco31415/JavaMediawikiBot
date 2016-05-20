package WikiBot.ContentRep;


public class Interwiki extends PageLocationContainer {
	
	private int openPos;
	private int closePos;
	
	public Interwiki(String pageName_, String lan_, int openPos_, int closePos_) {
		super(pageName_, lan_);
		openPos = openPos_;
		closePos = closePos_;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getOpeningPosition() {
		return openPos;
	}
	
	public int getClosingPosition() {
		return closePos;
	}
	
	@Override
	public String toString() {
		String output;

		output = pl.getLanguage() + ":" + pl.getTitle() + " (at: " + openPos + ")";
		return output;
	}
}
