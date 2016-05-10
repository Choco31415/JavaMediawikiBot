package WikiBot;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.ArrayList;

import WikiBot.APIcommands.*;
import WikiBot.ContentRep.*;
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
		
		myWikiLanguage = "en";

		/*lastCheckedPage = readFileAsList("/Last Page Read.txt", 0, false, true).get(0);

		if (lastCheckedPage == null) {
			lastCheckedPage = "";
		}*/
		
		APIlimit = 30;
		logAPIresults = false;
		revisionDepth = 5;
	}
	
	/*
	 * This is where I put my bot code.
	 */
	@Override
	public void code() {
		getRevisionContent = false;
		
		PageLocation img = new PageLocation("File:ErnieParke Top.png", "en");
		System.out.println(getImageInfo(img));
		System.out.println("a:" + getDirectImageURL(img));
		
		//proposeEdit(new AppendText(new PageLocation("User:InterwikiBot", "test"), "test", "Test."), "append");
		
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
	
	/*
	 * This method is for processing a page beyond what is offered.
	 * For example, in the DACH wiki, the en template creates an en interwiki.
	 */
	public void processFurther(Page pg) {
		Template temp = (Template)pg.getPageObject("en", "Template");
		if (temp != null) {
			pg.addInterwiki(new Interwiki(temp.getParameter(0), "en", -1, -1));
		}
	}
	
	public Page getWikiPage(PageLocation pl) {
		Page temp = super.getWikiPage(pl);
		processFurther(temp);
		return temp;
	}
	
	/**
	 * IMPORTANT: This method only accepts pages from the same wiki.
	 */
	public ArrayList<Page> getWikiPagesBatch(ArrayList<PageLocation> pls) {
		
		if (pls.size() == 0) {
			throw new Error();
		}
		
		//Check that everything is from the same language.
		String wikiLang = pls.get(0).getLanguage();
		for (PageLocation pl : pls) {
			if (!pl.getLanguage().equals(wikiLang)) {
				throw new Error();
			}
		}
		
		ArrayList<Page> temp = getWikiPages(pls);
		
		for (Page pg : temp) {
			processFurther(pg);
		}
		
		return temp;
	}
}
