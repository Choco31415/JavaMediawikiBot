package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

/**
 * @Description
 * This command appends text to a page.
 * 
 * Recommended used raw.
 * 
 * @RequiredRights
 * edit
 * 
 * @MediawikiSupport
 * 1.13+
 */
public class AppendText extends APIcommand {
	public AppendText(PageLocation pl_, String appendText_, String editSummary_) {
		super("Append text", pl_, true, "edit", "csrf");
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
		
		enforceMWVersion("1.13");
	}
}
