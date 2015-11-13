package WikiBot.Content;

import java.util.Date;

public class Revision extends PageLocationContainer {
	private String user;
	private String comment;
	private Date date;
	private String page = null;
	
	//TODO: Add revision status. Ex: minor, bot
	public Revision(PageLocation pl_, String user_, String comment_, Date date_) {
		super(pl_);
		user = user_;
		comment = comment_;
		date = date_;
	}
	
	public void setPageContent(String page_) {
		page = page_;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getComment() {
		return comment;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getPage() {
		return page;
	}
	
	@Override
	public String toString() {
		String output;

		output = "(Revision";
		if (page != null) {
			output += " ; Page included";
		} else {
			output += " ; Page not included";
		}
		output += ") Page: " + getTitle() + " User: " + user + " Timestamp: " + date + " Comment: " + comment;
		
		return output;
	}
}
