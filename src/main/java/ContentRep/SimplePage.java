package ContentRep;

/**
 * SimplePage is a simple version of page that does not store information such as links, templates, interwiki, and more.
 */
public class SimplePage extends PageLocationContainer implements Cloneable {

	protected int pageID;
	protected String lan;
	protected String rawText;
	
	public SimplePage(String lan_, String title_, int pageID_) {
		super(lan_, title_);
		pageID = pageID_;
		lan = lan_;
	}
	
	public void setRawText(String txt) {
		rawText = txt;
	}
	
	//Get information.
	//Page id methods
	public int getPageID() {
		return pageID;
	}
	
	//String methods
	public String getString() {
		return lan;
	}
	
	//Raw Text methods
	public String getRawText() {
		return rawText;
	}
	
	//Type methods
	@Override
	public String toString() {
		String output;

		output = "SIMPLE PAGE ;; Name: " + getTitle() + " ;; PAGE PAGE\nWith id: " + pageID  + "\n";
		output += "Language: " + lan + "\n";
		output += "Raw text: \n" + rawText;
		return output;
	}
	
	public SimplePage clone() {
		SimplePage output = new SimplePage(lan, getTitle(), pageID);
		output.setRawText(rawText);
		return output;
	}
}
