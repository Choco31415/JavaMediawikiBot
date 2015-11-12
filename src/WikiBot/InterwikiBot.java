package WikiBot;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.ArrayList;

import WikiBot.APIcommands.*;
import WikiBot.APIcommands.Query.QueryBackLinks;
import WikiBot.APIcommands.Query.QueryCategoryMembers;
import WikiBot.APIcommands.Query.QueryImageURL;
import WikiBot.APIcommands.Query.QueryList;
import WikiBot.APIcommands.Query.QueryRecentChanges;
import WikiBot.Content.Page;
import WikiBot.Content.PageLocation;
import WikiBot.Core.BotPanel;

@SuppressWarnings("unused")
public class InterwikiBot extends BotPanel {
	
	private static final long serialVersionUID = 1L;

	/*
	 * This is where I initialize my custom bot.
	 */
	public InterwikiBot() {
		super("Scratch");
		panelName = "InterwikiBot";
		
		botUsername = "InterwikiBot";
		botPassword = "****";//Use "****" for Github.
		
		myWikiLanguage = "en";

		/*lastCheckedPage = readFileAsList("/Last Page Read.txt", 0, false, true).get(0);

		if (lastCheckedPage == null) {
			lastCheckedPage = "";
		}*/
		
		APIlimit = 30;
	}
	
	/*
	 * This is where I put my bot code.
	 */
	@Override
	public void code() {
		proposeEdit(new AppendText(new PageLocation("User:InterwikiBot", "test"), "test", "Test."), "append");
		
		/*ArrayList<String> ignore = new ArrayList<String>();
		ignore.add("Category:Users' Images");
		ignore.add("Category:Users' Logos");
		ignore.add("Category:April Fools' Day Images");
		ignore.add("Scratch Cat");
		ignore.add("Most Common Scripts");
		ArrayList<PageLocation> pageLocs = getCategoryPagesRecursive(new PageLocation("Category:Images", "en"), ignore);
		System.out.println(pageLocs.size());
		
		ArrayList<PageLocation> cluster = new ArrayList<PageLocation>();
		for (int i = 0; i < pageLocs.size(); i++) {
			cluster.add(pageLocs.get(i));
			if (cluster.size() == 10 || i == pageLocs.size() - 1) {
				ArrayList<SimplePage> pages;
				
				pages = getWikiPagesSimple(cluster);
				
				for (int j = 0; j < pages.size(); j++) {
					PageLocation pageLoc = pages.get(j).getPageLocation();
					//	public UploadFileByURL(PageLocation pl_, String URL_, String pageText_, String uploadComment_) {
					APIcommand command = new UploadFileByURL(new PageLocation(pageLoc.getTitle(), "test"), getDirectImageURL(pageLoc), pages.get(j).getRawText(), "Transfering images from " + getWikiURL("en"));
					proposeEdit(command, "upload");
				}
				cluster.clear();
			}
		}*/
	}
}
