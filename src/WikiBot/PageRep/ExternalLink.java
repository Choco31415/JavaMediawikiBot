package WikiBot.PageRep;


public class ExternalLink extends PageObjectAdvanced {
	
	String link;//Where you are linking to.
	String linkText;
	
	/**
	 * @param openPos_ The opening position of the link.
	 * @param closePos_ The closing position of the link.
	 * @param rawLink_ The link text.
	 */
	public ExternalLink(int openPos_, int closePos_, String rawLink_) {
		super("External Link", rawLink_, openPos_, closePos_, "[", "]");
		int tempIndex = rawLink_.indexOf(" ");
		if (tempIndex != -1) {
			link = rawLink_.substring(0, tempIndex);
			linkText = rawLink_.substring(tempIndex+1);
		}
	}
	
	public String getDestination() {
		return link;
	}
	
	public String getDisplayedText() {
		return linkText;
	}
	
	public String simpleToString() {
		return toString();
	}
	
	@Override
	public String toString() {
		return "(Ext. Link) Text: " + link + " Link Text: " + linkText + " (at: " + openPos + ")";
	}
}

