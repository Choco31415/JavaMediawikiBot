package Content;

public class Link extends PageObjectAdvanced {
	
	String destination;
	
	/**
	 * @param openPos_ The opening position of the link.
	 * @param closePos_ The closing position of the link.
	 * @param rawLink_ The link text.
	 */
	public Link(int openPos_, int closePos_, String pageTitle_, String rawLink_) {
		super("Link", rawLink_, openPos_, closePos_, "[[", "]]");
		if ( rawLink_.substring(0,1).equals("/") || rawLink_.substring(0,1).equals("#")) {
			//This link is headed to a subpage/subsection and the destination must reflect that.
			destination = pageTitle_ + rawLink_;
		} else if (rawLink_.substring(0,1).equals(":")) {
			//Category and or file link.
			destination = rawLink_.substring(1);
		} else {
			destination = rawLink_;
		}
	}
	
	public String getDestination() {
		return destination;
	}
	
	/**
	 * Occasionally a link will point towards a page section.
	 * This method returns the link destination without that section.
	 */
	public String getDestinationWithoutSection() {
		int index = destination.indexOf("#");
		
		if (index != -1) {
			return destination.substring(0, index);
		} else {
			return destination;
		}
	}
	
	/**
	 * @return Does this link have alternate display text?
	 */
	public boolean hasDisplayText() {
		return parameters.size() > 0;
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
		return "(Link) To: " + destination + " (Link Text: " + getDisplayedText() + ") (at: " + openPos + ")";
	}
}
