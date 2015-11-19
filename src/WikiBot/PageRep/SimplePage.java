package WikiBot.PageRep;


/**
 * SimplePage is a simple version of page that does not store information such as links, templates, interwiki, and more.
 */
public class SimplePage extends PageLocationContainer implements Cloneable {

	protected int pageID;
	protected String lan;
	protected String rawText;
	
	public SimplePage(String title_, String lan_, int pageID_) {
		super(title_, lan_);
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
	/*public Page createPage() {
		
	}*/
	
	@Override
	public String toString() {
		String output;

		output = "SIMPLE PAGE ;; Name: " + title + " ;; PAGE PAGE\nWith id: " + pageID  + "\n";
		output += "Language: " + lan + "\n";
		output += "Raw text: \n" + rawText;
		return output;
	}
	
	public SimplePage clone() {
		SimplePage output = new SimplePage(title, lan, pageID);
		output.setRawText(rawText);
		return output;
	}
}
