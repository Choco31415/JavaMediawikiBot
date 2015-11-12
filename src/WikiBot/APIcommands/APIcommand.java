package WikiBot.APIcommands;

import java.util.ArrayList;

import WikiBot.Content.PageLocation;
import WikiBot.Content.PageLocationContainer;

public class APIcommand extends PageLocationContainer {
	
	boolean requiresPOST = false;

	protected ArrayList<String> keys = new ArrayList<String>();
	protected ArrayList<String> values = new ArrayList<String>();

	public APIcommand(PageLocation pl_, ArrayList<String> keys_, ArrayList<String> values_, boolean requiresPOST_) {
		super(pl_);
		keys.addAll(keys_);
		values.addAll(values_);
		requiresPOST = requiresPOST_;
	}
	
	
	public APIcommand(PageLocation pl_, ArrayList<String> keys_, ArrayList<String> values_) {
		super(pl_);
		keys.addAll(keys_);
		values.addAll(values_);
	}
	
	public APIcommand(PageLocation pl_, boolean requiresPOST_) {
		super(pl_);
		requiresPOST = requiresPOST_;
	}
	
	public APIcommand(PageLocation pl_) {
		super(pl_);
	}	
	
	public APIcommand(String language, ArrayList<String> keys_, ArrayList<String> values_, boolean requiresPOST_) {
		super(new PageLocation("null", language));
		keys.addAll(keys_);
		values.addAll(values_);
		requiresPOST = requiresPOST_;
	}
	
	
	public APIcommand(String language, ArrayList<String> keys_, ArrayList<String> values_) {
		super(new PageLocation("null", language));
		keys.addAll(keys_);
		values.addAll(values_);
	}
	
	public APIcommand(String language, boolean requiresPOST_) {
		super(new PageLocation("null", language));
		requiresPOST = requiresPOST_;
	}
	
	public APIcommand(String language) {
		super(new PageLocation("null", language));
	}	
	
	public void addParameter(String key, String value) {
		keys.add(key);
		values.add(value);
	}
	
	public boolean removeParameter(String key) {
		if (keys.contains(key)) {
			values.remove(keys.indexOf(key));
			keys.remove(key);
			return true;
		} else {
			return false;
		}
	}
	
	public PageLocation getPageLocation() {
		return pl;
	}
	
	public boolean doesKeyExist(String key) {
		return keys.contains(key);
	}
	
	public ArrayList<String> getKeys() {
		return keys;
	}
	
	public ArrayList<String> getValues() {
		return values;
	}
	
	public String[] getKeysArray() {
		String[] temp = new String[keys.size()];
		for (int i = 0; i < keys.size(); i++) {
			temp[i] = keys.get(i);
		}
		return temp;
	}
	
	public String[] getValuesArray() {
		String[] temp = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			temp[i] = values.get(i);
		}
		return temp;
	}
	
	public String getValue(String key) {
		return values.get(keys.indexOf(key));
	}
	
	public void setRequiresGET(boolean bool) { requiresPOST = bool; }
	public boolean requiresGET() { return requiresPOST; }
	
	public String getSummary() {
		String temp;
		temp = "\n(Edit) Wiki: " + pl.getLanguage();
		temp += "\nPage name: " + pl.getTitle();
		temp += "\nEdit type: " + getValue("action");
		for (String key : keys) {
			if (!(key.equals("action") || key.equals("title") || key.contains("text") || key.equals("filename"))) {
				temp += "\n"  + key.substring(0,1).toUpperCase() + key.substring(1) + ": " + getValue(key);
			}
		}
		temp += "\nText: \n" + getValue("text");
		return temp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (APIcommand.class.isAssignableFrom(obj.getClass())) {
			APIcommand edit2 = (APIcommand)obj;
			boolean similar = true;
			String[] keys2 = edit2.getKeysArray();
			
			for (String key : keys2) {
				if (!keys.contains(key)) {
					similar = false;
					break;
				}
				String value = edit2.getValue(key);
				if (!value.equalsIgnoreCase(getValue(key))) {
					similar = false;
					break;
				}
			}
			
			return similar;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getSummary();
	}
}
