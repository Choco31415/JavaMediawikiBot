package WikiBot.ContentRep;

import java.util.ArrayList;

/**
 * This class stores image information, like it's direct url, ect...
 * The Image class, meanwhile, is a representation of how an image is used on a page.
 * 
 * Unfortunately, this class recreates 
 */
public class ImageInfo extends InfoContainer {
	
	protected PageLocation pl;
	
	public ImageInfo(PageLocation loc, ArrayList<String> propertyNames_, ArrayList<String> propertyValues_) {
		super(propertyNames_, propertyValues_);
	}
	
	public ImageInfo(PageLocation loc) {
		super();
	}
	
	//Language methods
	public PageLocation getPageLocation() {
		return pl;
	}
	
	@Override
	public String toString() {
		String output = "";
		
		output += "Image info for: " + pl.getTitle();
		for (int i = 0; i < propertyNames.size(); i++) {
			output += "\n" + propertyNames.get(i);
			output += ":" + propertyValues.get(i);
		}
		
		return output;
	}
}
