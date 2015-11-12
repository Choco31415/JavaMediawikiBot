package WikiBot.Content;


public class Interwiki extends PageLocationContainer {
	
	public Interwiki(String pageName_, String lan_) {
		super(pageName_, lan_);
	}
	
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		String output;

		output = pl.getLanguage() + ":" + pl.getTitle();
		return output;
	}
}
