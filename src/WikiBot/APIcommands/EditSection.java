package WikiBot.APIcommands;

import WikiBot.Content.PageLocation;

public class EditSection extends APIcommand {
	
	public EditSection(PageLocation pl_, int sectionID_, String text_, String editSummary_) {
		super(pl_, true);
		keys.add("action");
		values.add("edit");
		keys.add("title");
		values.add(pl.getTitle());
		keys.add("section");
		values.add("" + sectionID_);
		keys.add("appendtext");
		values.add(text_);
		keys.add("summary");
		values.add(editSummary_);
		keys.add("bot");
		values.add("true");
	}
}
