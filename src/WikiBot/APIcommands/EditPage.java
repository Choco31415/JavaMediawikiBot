package WikiBot.APIcommands;

import WikiBot.Content.PageLocation;

public class EditPage extends APIcommand {
	
	public EditPage(PageLocation pl_, String text_, String editSummary_) {
		super(pl_, true);
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
