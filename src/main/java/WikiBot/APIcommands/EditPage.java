package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

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
	}
}
