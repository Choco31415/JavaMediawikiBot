package WikiBot.ContentRep;

import java.util.ArrayList;

import WikiBot.Utils.ArrayUtils;

/**
 * This class stores user information, like the direct url, block info, ect...
 */
public class UserInfo extends InfoContainer {
	
	protected User user;
	
	//Other user info
	private ArrayList<String> groups;
	private ArrayList<String> implicitGroups;
	private ArrayList<String> rights;
	
	//Block info
	private boolean hasBlockInfo = false;
	private int blockID = -1;
	private String blockedBy;
	private String blockReason;
	private String blockExpiration;
	
	public UserInfo(User user_, ArrayList<String> propertyNames_, ArrayList<String> propertyValues_) {
		super(propertyNames_, propertyValues_);
		user = user_;
	}
	
	public UserInfo(User user_) {
		super();
		user = user_;
	}
	
	public User getUser() {
		return user;
	}
	
	/**
	 * Use this method to see if this UserInfo contains information on a user's groups.
	 * @return
	 */
	public boolean hasGroups() { return groups != null; }
	public void setGroups(ArrayList<String> arr) { groups = arr; }
	public ArrayList<String> getGroups() { return groups; }
	
	/**
	 * Use this method to see if this UserInfo contains information on a user's implicit groups.
	 * @return
	 */
	public boolean hasImplicitGroups() { return implicitGroups != null; }
	public void setImplicitGroups(ArrayList<String> arr) { implicitGroups = arr; }
	public ArrayList<String> getImplicitGroups() { return implicitGroups; }
	
	/**
	 * Use this method to see if this UserInfo contains information on a user's rights.
	 * @return
	 */
	public boolean hasRights() { return rights != null; }
	public void setRights(ArrayList<String> arr) { rights = arr; }
	public ArrayList<String> getRights() { return rights; }

	/**
	 * Use this method to see if this UserInfo contains information on a user's edit count.
	 * @return
	 */
	public boolean hasEditCount() { return hasProperty("editcount"); }
	public int getEditCount() { return new Integer(getValue("editcount")); }
	
	/**
	 * Use this method to see if this UserInfo contains information on a user's registration date and time.
	 * @return
	 */
	public boolean hasRegistration() { return hasProperty("registration"); }
	public String getRegistration() { return getValue("registration"); }
	
	/**
	 * Use this method to see if this UserInfo contains information on if a user is emailable.
	 * @return
	 */
	public boolean hasEmailable() { return hasProperty("emailable"); }
	public boolean getEmailable() { return new Boolean(getValue("emailable")); }
	
	/**
	 * Use this method to see if this UserInfo contains information on a user's gender.
	 * @return
	 */
	public boolean hasGender() { return hasProperty("gender"); }
	public String getGender() { return getValue("gender"); }
	
	/**
	 * Use this method to see if this UserInfo contains information on a user's block (or lack thereof).
	 * @return
	 */
	public boolean hasBlockInfo() { return hasBlockInfo; }
	public void setAsNotBlocked() {
		hasBlockInfo = true;
		blockID = -1;//Just to be doubly sure.
	}
	public void setBlockInfo(int blockID_, String blockedBy_, String blockReason_, String blockExpiration_) {
		hasBlockInfo = true;
		blockID = blockID_;
		blockedBy = blockedBy_;
		blockReason = blockReason_;
		blockExpiration = blockExpiration_;
	}
	public boolean isBlocked() { return blockID != -1; }
	public int getBlockID() { return blockID; }
	public String getBlockedBy() { return blockedBy; }
	public String getBlockReason() { return blockReason; }
	public String getBlockExpiration() { return blockExpiration; }
	
	@Override
	public String toString() {
		String output = "";
		
		output += "User info for: " + user.simpleToString();
		for (int i = 0; i < propertyNames.size(); i++) {
			output += "\n" + propertyNames.get(i);
			output += ": " + propertyValues.get(i);
		}
		
		if (hasGroups()) {
			output += "\nGroups: ";
			output += ArrayUtils.compactArray(groups, ", ");
		}
		if (hasImplicitGroups()) {
			output += "\nImplicit Groups: ";
			output += ArrayUtils.compactArray(implicitGroups, ", ");
		}
		if (hasRights()) {
			output += "\nRights: ";
			output += ArrayUtils.compactArray(rights, ", ");
		}
		if (hasBlockInfo()) {
			if (isBlocked()) {
				output += "\nBlocked ";
				output += "\nBlockID: " + blockID;
				output += "\nBlocked by: " + blockedBy;
				output += "\nBlock reason: " + blockReason;
				output += "\nBlock expiration: " + blockExpiration;
			} else {
				output += "\nNot blocked";
			}
		}
		
		return output;
	}
}
