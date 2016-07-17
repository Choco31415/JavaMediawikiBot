package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

/**
 * @Description
 * This command deletes a page.
 * 
 * @RequiredRights
 * delete
 * 
 * @MediawikiSupport
 * 1.12+
 */
public class DeletePage extends APIcommand {
	
	public DeletePage(PageLocation pl_, String editSummary_) {
		super("Delete", pl_, true, "delete", "csrf");
		keys.add("action");
		values.add("delete");
		keys.add("title");
		values.add(getTitle());
		keys.add("reason");
		values.add(editSummary_);
		
		enforceMWVersion("1.12");
	}
}
