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
	 * This is where I initialize my custom Mediawiki bot.
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
		
		int sample = 10;
		
		ArrayList<PageLocation> locs = getAllPages("En", sample);
		
		for (int i = 0; i < locs.size(); i += 10) {
			ArrayList<PageLocation> subLocs = new ArrayList<PageLocation>();
			subLocs.addAll(locs.subList(i, Math.min(i+10, locs.size())));
			ArrayList<Page> pages = getWikiPages(subLocs);
			
			for (Page page : pages) {
				String mainPageName = "En:" + page.getTitle();
				String templatePageName = mainPageName + "/translate";
				String templatePageRawText = 
"{{Translate\n" +
"|En=" + mainPageName + "\n" +
"|It=???\n" +
"|Fr=???\n" +
"|Es=???\n" + 
"|Pl=???\n" +
"|Sk=???\n" +
"|TR=???\n" +
"|Pt=???\n" +
"}}";
				String mainpageRawText = page.getRawText();
				ArrayList<Link> links = page.getLinksRecursive();
				for (int l = links.size()-1; l >= 0; l--) {
					Link link = links.get(l);
					if (!link.getHeader().contains("File:") && !link.getHeader().contains("Image:") && !link.getHeader().contains("Template:") && !link.getHeader().substring(0, 1).equals(":")) {
						mainpageRawText = mainpageRawText.substring(0, link.getOpeningPosition()) + "En:" + mainpageRawText.substring(link.getOpeningPosition());
					}
				}
				mainpageRawText = "{{" + templatePageName + "}}\n" + page.getRawText();
				proposeEdit(new EditPage(new PageLocation(mainPageName, "test"), mainpageRawText, "Uploading Translate Page"), "Main");
				proposeEdit(new EditPage(new PageLocation(templatePageName, "test"), templatePageRawText, "Uploading Page"), "Template");
			}
		}
		
		
		//proposeEdit(new AppendText(new PageLocation("User:InterwikiBot", "test"), "test", "Test."), "append");
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
