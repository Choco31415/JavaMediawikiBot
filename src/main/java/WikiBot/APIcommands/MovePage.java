package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

/**
 * To move a page and leave no redirect behind, the suppressredirect right is needed. By default, this is only given to bots and sysops.
 */
public class MovePage extends APIcommand {

	public MovePage(PageLocation from_, PageLocation to_, String editSummary_, boolean moveTalk_, boolean moveSubpages_, boolean leaveRedirect_) {
		super("Move", from_, true, "move", "csrf");
		keys.add("action");
		values.add("move");
		keys.add("from");
		values.add(getTitle());
		keys.add("to");
		values.add(to_.getTitle());
		keys.add("reason");
		values.add(editSummary_);
		keys.add("format");
		values.add("xml");
		if (moveTalk_) {
			keys.add("moveTalk");
			values.add(null);
		}
		if (moveSubpages_) {
			keys.add("movesubpages");
			values.add(null);
		}
		if (!leaveRedirect_) {
			keys.add("noredirect");
			values.add(null);
		}
	}
	
	/**
	 * The standard move operation. The talk page is not moved, and a redirect is left behind.
	 */
	public MovePage(PageLocation from_, PageLocation to_, String editSummary_) {
		super("Move", from_, true, "move", "csrf");
		keys.add("action");
		values.add("move");
		keys.add("from");
		values.add(getTitle());
		keys.add("to");
		values.add(to_.getTitle());
		keys.add("reason");
		values.add(editSummary_);
		keys.add("format");
		values.add("xml");
	}
}
