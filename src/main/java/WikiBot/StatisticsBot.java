package WikiBot;

import java.io.Console;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Scanner;
import java.util.logging.Level;

import WikiBot.APIcommands.EditPage;
import WikiBot.ContentRep.*;
import WikiBot.ContentRep.SiteInfo.SiteStatistics;
import WikiBot.Core.GenericBot;
import WikiBot.Utils.FileUtils;
import WikiBot.APIcommands.APIcommand;


public class StatisticsBot extends GenericBot {
	
	private static final long serialVersionUID = 1L;

	private static String username;
	private static String language;
	private static User bot;
	
	private static StatisticsBot instance;
	
	private static PageLocation statsPage;
	private static String defaultStatsFile;
	
	private static String[] statsTracked;
	
	private static SimpleDateFormat dateFormat;
	
	/*
	 * This is where I initialize my custom Mediawiki bot.
	 */
	public StatisticsBot() {
		super("Scratch", "en");
		
		//Preferences
		APIlimit = 30;//The amount of items to get per query call, if there are multiple items.
		getRevisions = false;//Don't get page revisions.
		
		APIthrottle = 0.5;//Minimum time between any API commands.
		
		setLoggerLevel(Level.INFO);//How fine should the logger be? Visit NetworkingBase.java for logger level info.
		
		username = "InterwikiBot";
		language = "en";
		bot = new User(username, language);
		
		statsPage = new PageLocation("User:" + username + "/International Stats", language);
		defaultStatsFile = "/DefaultStatsPage.txt";
		
		statsTracked = new String[]{"pages", "articles", "edits", "images", "users", "activeusers", "admins"};
		
		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		
		if (instance == null) {
			instance = this;
		} else {
			throw new ConcurrentModificationException();//There should not be more then one GenericBot!!!
		}
	}
	
	/**
	 * Get an instance of GenericBot.
	 * If GenericBot has not been instantiated yet, the
	 * family and homeWikiLanguage are both set to null.
	 * @return
	 */
	public static StatisticsBot getInstance() {
		if (instance == null) {
			instance = new StatisticsBot();
		}
		
		return instance;
	}
	
	/*
	 * This is where I read in the bot password and create an instance.
	 */
	public static void main(String[] args) {
		StatisticsBot b = getInstance();
		
		String botPassword = "";
		
		System.out.println("Please input the bot password:");
		
		//Accommodate Eclipse test run environments.
		try {
			Console console = System.console();
			botPassword = new String(console.readPassword());
		} catch (Error|Exception e) {
			Scanner sc = new Scanner(System.in);
			botPassword = sc.nextLine();
		}
		
		boolean softStart = true;
		
		System.out.println("Do you want a soft start? Y/N");
		
		//Accommodate Eclipse test run environments.
		try {
			Console console = System.console();
			softStart = console.readLine().equalsIgnoreCase("Y");
		} catch (Error|Exception e) {
			Scanner sc = new Scanner(System.in);
			softStart = sc.nextLine().equalsIgnoreCase("Y");
		}
		
		b.run(botPassword, softStart);
	}
	
	/*
	 * This is the entry point for the Object portion of the bot.
	 */
	public void run(String botPassword, boolean softStart) {
		boolean loggedIn = logIn(bot, botPassword);
		
		if (!loggedIn) {
			throw new Error("Didn't log in.");
		}
		
		// Run.
		boolean running = true;
		boolean firstLoop = true;
		while (running) {
			//If soft starting, do not run the first loop.
			if (!firstLoop && softStart || !softStart) {
				runChecks();
			}
			
			int daysLeft = 7;
			while (running & daysLeft > 0) {
				System.out.println(daysLeft + " days until next check.");
				int dayInSeconds = 60*60*24;
				try {
					Thread.sleep(1000*dayInSeconds);
				} catch (InterruptedException e) {
					e.printStackTrace();
					running = false;// If there is an error, continuing to run will likely be a bad idea.
				}
				daysLeft--;
			}
		}
	}

	/*
	 * This is the method where StatisticsBot collects statistics on all wikis, and then
	 * pushes them to the wiki.
	 */
	public void runChecks() {
		// Check if the statistics page has been initialized...
		Page sp = null;
		boolean pageExists = doesPageExist(statsPage);
		if (pageExists) {
			sp = getWikiPage(statsPage);
		}
		
		if (!pageExists || !sp.getRawText().contains("<!--Initialized-->")) {
			// Not initialized, so initialize it.
			ArrayList<String> defaultTextArray = FileUtils.readFileAsList(defaultStatsFile, 0, false, false);
			String defaultText = "";
			
			for (String line : defaultTextArray) {
				defaultText += line + "\n";
			}
			
			APIcommand initPage = new EditPage(statsPage, defaultText, "Initializing.");
			APIcommand(initPage); // Push text.
		}
		
		// Update sp to have content.
		if (sp == null) {
			sp = getWikiPage(statsPage);
		}
		
		String rawText = sp.getRawText();
		
		/*
		 * Now for the statistics collection.
		 */
		
		// Gather wiki data...
		for (int sectionID = sp.getNumSections(); sectionID > 0; sectionID--) {
			Section section = sp.getSectionByNum(sectionID);
			String sectionTitle = section.getSectionTitle().trim().toLowerCase();
			
			String timeStamp = dateFormat.format(new java.util.Date());
			
			System.out.println(sectionTitle);
			
			if (mdm.getWikiPrefixes().contains(sectionTitle)) {
				String wikiPrefix = sectionTitle;
				
				// Parse out section information.
				int startingPos = section.getPosition();
				int endingPos = -1;
				if (sectionID == sp.getNumSections()) {
					endingPos = rawText.length();
				} else {
					Section nextSection = sp.getSectionByNum(sectionID+1);
					endingPos = nextSection.getPosition();
				}
				
				String sectionText = rawText.substring(startingPos, endingPos);
				
				// Get wiki statistics...
				SiteStatistics ss =  getSiteStatistics(wikiPrefix);
				
				// Format it into wiki text.
				String insertText = "|-";
				
				// Time...
				insertText += "\n|" + timeStamp;
						
				// Stats...
				for (String stat : statsTracked) {
					insertText += "\n|";
					if (ss.hasProperty(stat)) {
						insertText += ss.getValue(stat);
					}
				}
				
				insertText += "\n";
				
				// Insert the text.
				int pos = startingPos + sectionText.indexOf("|}");
				
				rawText = rawText.substring(0, pos) + insertText + rawText.substring(pos);
			}
		}
		
		// Update the page.
		APIcommand updatePage = new EditPage(statsPage, rawText, "Weekly update.");
		APIcommand(updatePage);
	}
}