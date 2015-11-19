package WikiBot.Content;

import java.util.ArrayList;

public class Image extends PageObjectAdvanced {
	
	public Image(int openPos_, int closePos_, String imageName_, ArrayList<String> params, ArrayList<Link> links_) {
		//public PageObject(String myObjectType_, int openPos_, String textOpening_, String textClosing_) { 
		super("Image", imageName_, openPos_, closePos_, params, null, "[[", "]]");
	}
	
	public Image(int openPos_, int closePos_, String imageName_) {
		super("Image", imageName_, openPos_, closePos_, "[[", "]]");
	}
	
	public String getImageName() {
		return header;
	}
	
	public String simpleToString() {
		return "(Image) Name: " + header + " (at: " + openPos + ")";
	}
	
	@Override
	public String toString() {
		String output;

		output = simpleToString();
		output += "\nWith paramaters: ";
		for (int i = 0; i < parameters.size(); i++) {
			output += (parameters.get(i) + " , ");
		}
		output += "\nWith pageObjects: ";
		for (int i = 0; i < pageObjects.size(); i++) {
			output += (pageObjects.get(i).simpleToString() + " , ");
		}

		return output;
	}
}
