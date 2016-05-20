package WikiBot.ContentRep;

import java.util.ArrayList;

/**
 * This class stores information on an image, like it's direct url, ect...
 * The Image class, meanwhile, is a representation of how an image is used on a page.
 *
 */
public class ImageInfo extends PageLocationContainer {
	
	ArrayList<String> propertyNames;
	ArrayList<String> propertyValues;
	
	public ImageInfo(PageLocation loc, ArrayList<String> propertyNames_, ArrayList<String> propertyValues_) {
		super(loc);
		propertyNames = propertyNames_;
		propertyValues = propertyValues_;
	}
	
	public ImageInfo(PageLocation loc) {
		super(loc);
		propertyNames = new ArrayList<String>();
		propertyValues = new ArrayList<String>();
	}
	
	/**
	 * This method returns an image property.
	 * It will return null if the requested property is not found.
	 * 
	 * @param name The name of the property you want.
	 */
	public String getProperty(String name) {
		int index = propertyNames.indexOf(name);
		return propertyValues.get(index);
	}
	
	/**
	 * @return The list of properties that this class has information on.
	 */
	public ArrayList<String> getPropertyNames() {
		return propertyNames;
	}
	
	public void addProperty(String name, String value) {
		propertyNames.add(name);
		propertyValues.add(value);
	}
	
	@Override
	public String toString() {
		String output = "";
		
		output += "Image info for: " + getTitle();
		for (int i = 0; i < propertyNames.size(); i++) {
			output += "\n" + propertyNames.get(i);
			output += ":" + propertyValues.get(i);
		}
		
		return output;
	}
}
