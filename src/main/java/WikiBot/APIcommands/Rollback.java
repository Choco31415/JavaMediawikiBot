package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;
import WikiBot.MediawikiData.VersionNumber;

/**
 * This command requires the rollback right.
 */
public class Rollback extends APIcommand {

	/**
	 * A rollback edit.
	 * @param pl_ The page to rollback.
	 * @param userName_ The user whose edits you will rollback. Required by MW.
	 * @param summary_
	 */
	public Rollback(PageLocation pl_, String userName_, String summary_) {
		super(pl_, true, "rollback", "rollback");
		keys.add("action");
		values.add("rollback");
		keys.add("title");
		values.add(pl_.getTitle());
		keys.add("user");
		values.add(userName_);
		keys.add("summary");
		values.add(summary_);
		
		enforceMWVersion(new VersionNumber("1.12"));
	}
	
}
