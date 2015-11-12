package WikiBot.Content;


public class Link {
	public Position position;
	public String link;
	public String linkText;
	
	public Link(Position pos_, String link_, String linkText_) {
		position = pos_;
		link = link_;
		linkText = linkText_;
	}
	
	public Link(Position pos_, String link_) {
		position = pos_;
		link = link_;
		linkText = link_;
	}
	
	public void setPosition(Position pos) {
		position = pos;
	}
	
	public void setLinkText(String text) {
		linkText = text;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public String getDestination() {
		return link;
	}
	
	public String getLinkText() {
		return linkText;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass().equals(Link.class)) {
			Link link2 = (Link)obj;
			if (link2.getPosition().equals(getPosition())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "(Link) Text: " + link + " Link Text: " + linkText + " (Position: " + position + ")";
	}
}
