package WikiBot.ContentRep;

import java.util.ArrayList;

/**
 * This class stores information on about an object.
 */
class InfoContainer {
	
	protected ArrayList<String> propertyNames;
	protected ArrayList<String> propertyValues;
	
	public InfoContainer(ArrayList<String> propertyNames_, ArrayList<String> propertyValues_) {
		propertyNames = propertyNames_;
		propertyValues = propertyValues_;
	}
	
	public InfoContainer() {
		propertyNames = new ArrayList<String>();
		propertyValues = new ArrayList<String>();
	}
	
	/**
	 * This method returns an property's value.
	 * It will return null if the requested property is not found.
	 * 
	 * @param propertyName The name of the property you want.
	 */
	public String getValue(String propertyName) {
		int index = propertyNames.indexOf(propertyName);
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
		
		output += "Info container";
		for (int i = 0; i < propertyNames.size(); i++) {
			output += "\n" + propertyNames.get(i);
			output += ":" + propertyValues.get(i);
		}
		
		return output;
	}
}
