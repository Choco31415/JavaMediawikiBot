package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

public class AppendText extends APIcommand {
	public AppendText(PageLocation pl_, String appendText_, String editSummary_) {
		super(pl_, true, "edit", "csrf");
		keys.add("action");
		values.add("edit");
		keys.add("title");
		values.add(getTitle());
		keys.add("appendtext");
		values.add(appendText_);
		keys.add("summary");
		values.add(editSummary_);
		keys.add("bot");
		values.add("true");
	}
}
