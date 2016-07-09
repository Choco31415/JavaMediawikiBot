package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

/**
 * This action requires the "delete" right.
 */
public class DeletePage extends APIcommand {
	
	public DeletePage(PageLocation pl_, String editSummary_) {
		super(pl_, true, "delete", "csrf");
		keys.add("action");
		values.add("delete");
		keys.add("title");
		values.add(getTitle());
		keys.add("reason");
		values.add(editSummary_);
	}
}
