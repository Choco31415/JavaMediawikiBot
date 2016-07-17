package WikiBot.ContentRep;

import java.util.ArrayList;

/**
 * This class stores image information, like it's direct url, ect...
 * The Image class, meanwhile, is a representation of how an image is used on a page.
 * 
 * All metadata values are stored as JSON text.
 */
public class ImageInfo extends InfoContainer {
	
	protected PageLocation pl;
	
	public ImageInfo(PageLocation loc_, ArrayList<String> propertyNames_, ArrayList<String> propertyValues_) {
		super(propertyNames_, propertyValues_);
		pl = loc_;
	}
	
	public ImageInfo(PageLocation loc_) {
		super();
		pl = loc_;
	}
	
	//Language methods
	public PageLocation getPageLocation() {
		return pl;
	}
	
	/**
	 * This method returns a property's value.
	 * It will return null if the requested property is not found.
	 * 
	 * All metadata values are stored as JSON text.
	 * 
	 * @param propertyName The name of the property you want.
	 */
	public String getValue(String propertyName) {
		return super.getValue(propertyName);
	}
	
	@Override
	public String toString() {
		String output = "";
		
		output += "Image info for: " + pl.getTitle();
		for (int i = 0; i < propertyNames.size(); i++) {
			output += "\n" + propertyNames.get(i);
			output += ": " + propertyValues.get(i);
		}
		
		return output;
	}
}
