package WikiBot.APIcommands;

import java.io.IOException;
import java.util.ArrayList;

import WikiBot.ContentRep.PageLocation;
import WikiBot.ContentRep.PageLocationContainer;
import WikiBot.ContentRep.User;
import WikiBot.Errors.UnsupportedError;
import WikiBot.Utils.ArrayUtils;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import MediawikiData.MediawikiDataManager;
import MediawikiData.VersionNumber;

/**
 * @Description
 * This class is a base for making APIcommands.
 * 
 * APIcommands are commands to the mediawiki API.
 * 
 * Only use this class to create new commands. Ex: EditPage
 */
public class APIcommand extends PageLocationContainer {
	
	protected String commandName;//One or two words to summarize what this edit does.
	
	protected boolean requiresHttpEntity = false;
	protected boolean requiresPOST = false;
	protected boolean unescapeHTML = true;
	
	protected String oldTokenType = "";//Pre MW 1.24
	protected String newTokenType = "";//MW 1.24 and above

	protected ArrayList<String> keys = new ArrayList<String>();
	protected ArrayList<String> values = new ArrayList<String>();
	
	public APIcommand(String commandName_, PageLocation pl_, boolean requiresPOST_, String oldTokenType_, String newTokenType_) {
		super(pl_);
		commandName = commandName_;
		requiresPOST = requiresPOST_;
		oldTokenType = oldTokenType_;
		newTokenType = newTokenType_;
	}
	
	public APIcommand(String commandName_, PageLocation pl_) {
		super(pl_);
		commandName = commandName_;
	}
	
	public APIcommand(String commandName_, String language, boolean requiresPOST_, String oldTokenType_, String newTokenType_) {
		super(new PageLocation(language, "null"));
		commandName = commandName_;
		requiresPOST = requiresPOST_;
		oldTokenType = oldTokenType_;
		newTokenType = newTokenType_;
	}
	
	public APIcommand(String commandName_, String language) {
		super(new PageLocation(language, "null"));
		commandName = commandName_;
	}
	
	protected void enforceMWVersion(String introduced) {
		enforceMWVersion(introduced, null);
	}
	
	protected void enforceMWVersion(String introduced, String removed) {
		VersionNumber myVersion = getMWVersion();
		
		if ((introduced == null || myVersion.compareTo(introduced) < 0) && (removed == null || myVersion.compareTo(removed) > 0)) {
			throw new UnsupportedError("The " + getLanguage() + " wiki does not support this API command. (command name: " + commandName + ")");
		}
	}
	
	public VersionNumber getMWVersion() {
		MediawikiDataManager mdm = MediawikiDataManager.getInstance();
		
		return mdm.getWikiMWVersion(getLanguage());
	}
	
	public String getOldTokenType() {
		return oldTokenType;
	}
	
	public String getNewTokenType() {
		return newTokenType;
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
	
	public HttpEntity getHttpEntity(String token) {
		return null;// For extended command needs only.
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
		return keys.toArray(temp);
	}
	
	public String[] getValuesArray() {
		String[] temp = new String[values.size()];
		return values.toArray(temp);
	}
	
	public String getValue(String key) {
		return values.get(keys.indexOf(key));
	}
	
	public void setRequiresEntity(boolean bool) { requiresHttpEntity = bool; }
	public boolean requiresEntity() { return requiresHttpEntity; }
	public void setRequiresPOST(boolean bool) { requiresPOST = bool; }
	public boolean requiresPOST() { return requiresPOST; }
	public void setUnescapeHTML(boolean bool) { unescapeHTML = bool; }
	public boolean shouldUnescapeHTML() { return unescapeHTML; }
	
	//A simple one or two words to summarize what this edit does.
	public String getCommandName() {
		return commandName;
	}
	
	public String getSummary() {
		String temp;
		temp = "\n(Edit) Wiki: " + pl.getLanguage();
		temp += "\nPage name: " + pl.getTitle();
		temp += "\nEdit type: " + getValue("action");
		for (String key : keys) {
			if (!(key.equals("action") || key.equals("title") || key.contains("text") || key.equals("filename") || key.equals("from") || key.equals("format"))) {
				if (getValue(key) == null) {
					 temp += "\n"  + key.substring(0,1).toUpperCase() + key.substring(1);
				} else {
					 temp += "\n"  + key.substring(0,1).toUpperCase() + key.substring(1) + ": " + getValue(key);
				}
			}
		}
		if (doesKeyExist("text")) {
			temp += "\nText: \n" + getValue("text");
		}
		if (requiresEntity()) {
			String entity;
			try {
				entity = EntityUtils.toString(getHttpEntity("<token>"));
			} catch (ParseException e) {
				entity = "Required.";
			} catch (IOException e) {
				entity = "Required.";
			}
			temp += "\nEntity: " + entity;
		}
		return temp;
	}
	
	protected String compactPLArray(ArrayList<PageLocation> array, String delimitor) {
		//This takes an array of page locations and compacts it into one string.
		String output = "";
		
		for (int i = 0; i < array.size(); i++) {
			output += array.get(i).getTitle();
			if (i != array.size()-1) {
				output += delimitor;
			}
		}
		
		return output;
	}
	
	/**
	 * This takes an array of users and compacts it into on string.
	 * @param users The group of users to compact.
	 * @return A String.
	 */
	protected String compactUserArray(ArrayList<User> users) {
		String parsedUserNames = "";
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			
			if (i != 0){
				parsedUserNames += "|";
			}
			
			parsedUserNames += user.getUserName();
		}
		return parsedUserNames;
	}
	
	/**
	 * This takes an array of strings and compacts it into one string.
	 * @param array The array of Strings to compact.
	 * @return A String.
	 */
	protected String compactArray(ArrayList<String> array, String delimitor) {
		return ArrayUtils.compactArray(array, delimitor);
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
