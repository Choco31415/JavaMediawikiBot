package Content;

import java.util.ArrayList;
import java.util.Date;

public class Revision extends PageLocationContainer {
	private String user;
	private String comment;
	private Date date;
	private String revContent = null;
	private ArrayList<String> flags;
	
	public Revision(PageLocation pl_, String user_, String comment_, Date date_, ArrayList<String> flags_) {
		super(pl_);
		user = user_;
		comment = comment_;
		date = date_;
		flags = flags_;
	}
	
	public void setRevisionContent(String revContent_) {
		revContent = revContent_;
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
	
	/**
	 * 
	 * @return Flags are revision status qualities. For example: minor, bot, new
	 */
	public ArrayList<String> getFlags() {
		return flags;
	}
	
	/**
	 * 
	 * @return Does this revision create a new page?
	 */
	public boolean isNewPage() {
		return flags.contains("new");
	}
	
	/**
	 * 
	 * @return Is this revision minor?
	 */
	public boolean isMinor() {
		return flags.contains("minor");
	}
	
	/**
	 * 
	 * @return Is this revision a bot edit?
	 */
	public boolean isBot() {
		return flags.contains("bot");
	}
	
	public boolean hasRevisionContent() {
		return revContent != null;
	}
		
	public String getRevisionContent() {
		return revContent;
	}
	
	@Override
	public String toString() {
		String output;

		//Attach if the revision contains page contents or not.
		output = "(Revision";
		if (hasRevisionContent()) {
			output += " ; Content included";
		} else {
			output += " ; Content not included";
		}
		output += ") ";
		
		//Attach flag data.
		if (flags.size() > 0) {
			output += "(";
			for (int i = 0; i < flags.size(); i++) {
				if (i > 0) {
					output += " ";//padding
				}
				output += flags.get(i).substring(0, 1);
			}
			output += ") ";
		}
		
		//Attach general revision data.
		output += "Page: " + getTitle() + " User: " + user + " Timestamp: " + date + " Comment: " + comment;
		
		return output;
	}
}
