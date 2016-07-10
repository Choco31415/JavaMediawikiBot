package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

/**
 * This command edits a page, or creates one if the
 * page location does not exist.
 * 
 * Rights required:
 * edit
 * 
 * MW version required:
 * 1.13+
 */
public class EditPage extends APIcommand {
	
	public EditPage(PageLocation pl_, String text_, String editSummary_) {
		super("Edit page", pl_, true, "edit", "csrf");
		keys.add("action");
		values.add("edit");
		keys.add("title");
		values.add(getTitle());
		keys.add("text");
		values.add(text_);
		keys.add("summary");
		values.add(editSummary_);
		keys.add("bot");
		values.add("true");
		
		enforceMWVersion("1.13");
	}
}
