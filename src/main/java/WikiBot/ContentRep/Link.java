package WikiBot.ContentRep;


public class Link extends PageObjectAdvanced {
	
	String link;
	
	/**
	 * @param openPos_ The opening position of the link.
	 * @param closePos_ The closing position of the link.
	 * @param rawLink_ The link text.
	 */
	
	public Link(int openPos_, int closePos_, String pageTitle_, String rawLink_) {
		super("Link", rawLink_, openPos_, closePos_, "[[", "]]");
		if ( rawLink_.substring(0,1).equals("/") || rawLink_.substring(0,1).equals("#")) {
			//This link is headed to a subpage/subsection and the destination must reflect that.
			link = pageTitle_ + rawLink_;
		} else if (rawLink_.substring(0,1).equals(":")) {
			//Category and or file link.
			link = rawLink_.substring(1);
		} else {
			link = rawLink_;
		}
	}
	
	public String getDestination() {
		return link;
	}
	
	public String getDisplayedText() {
		if (parameters.size() > 0) {
			return parameters.get(0);
		} else {
			return header;
		}
	}
	
	public String simpleToString() {
		return toString();
	}
	
	@Override
	public String toString() {
		return "(Link) To: " + link + " (Link Text: " + getDisplayedText() + ") (at: " + openPos + ")";
	}
}
