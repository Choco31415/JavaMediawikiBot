package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

/**
 * This command edits a page section.
 * 
 * Rights required:
 * edit
 * 
 * MW version required:
 * 1.13+
 */
public class EditSection extends APIcommand {
	
	/**
	 * Edit a section on the page.
	 * @param pl_ The page.
	 * @param sectionID_ 0 for the top section. "new" to add a new section.
	 * @param text_ Section content.
	 * @param editSummary_
	 */
	public EditSection(PageLocation pl_, int sectionID_, String text_, String editSummary_) {
		super("Edit section", pl_, true, "edit", "csrf");
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
		
		enforceMWVersion("1.13");
	}
}
