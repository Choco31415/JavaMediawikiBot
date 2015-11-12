package WikiBot.Content;

import java.util.ArrayList;

public class Image {
	private Position position;
	private String name;
	private ArrayList<String> parameters = new ArrayList<String>();
	private ArrayList<Link> links = new ArrayList<Link>();
	
	public Image(Position pos_, String name_, ArrayList<String> params, ArrayList<Link> links_) {
		position = pos_;
		name = name_;
		parameters = params;
		links = links_;
	}
	
	public Image(Position pos_, String name_) {
		position = pos_;
		name = name_;
	}
	
	public void addParameter(String param) {
		parameters.add(param);
	}
	
	public void addLink(Link link) {
		links.add(link);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public String getName() {
		return name;
	}
	
	public int getParameterCount() {
		return parameters.size();
	}
	
	public String getParameter(int i) {
		return parameters.get(i);
	}
	
	public ArrayList<Link> getLinks() {
		return links;
	}
	
	public boolean containsLink(Link link) {
		for (int i = 0; i < links.size(); i++) {
			if ((links.get(i)).equals(link)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		String output;

		output = "(Image) Name: " + name + "\nWith links: ";
		for (int i = 0; i < links.size(); i++) {
			output += (links.get(i) + " , ");
		}
		output += "\nWith parameters: ";
		for (int i = 0; i < parameters.size(); i++) {
			output += (parameters.get(i) + " , ");
		}
		output += "\nAt: " + position;
		return output;
	}
}
