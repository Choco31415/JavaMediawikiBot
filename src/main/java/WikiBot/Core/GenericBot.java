package WikiBot.Core;
 
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Iterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import WikiBot.APIcommands.APIcommand;
import WikiBot.APIcommands.Login;
import WikiBot.APIcommands.UploadFileChunk;
import WikiBot.APIcommands.Query.*;
import WikiBot.ContentRep.ImageInfo;
import WikiBot.ContentRep.InfoContainer;
import WikiBot.ContentRep.Page;
import WikiBot.ContentRep.PageLocation;
import WikiBot.ContentRep.Revision;
import WikiBot.ContentRep.SimplePage;
import WikiBot.ContentRep.User;
import WikiBot.ContentRep.UserInfo;
import WikiBot.ContentRep.SiteInfo.SiteStatistics;
import WikiBot.Errors.NetworkError;
import WikiBot.MediawikiData.MediawikiDataManager;
import WikiBot.MediawikiData.VersionNumber;
import WikiBot.Utils.ArrayUtils;

/**
 * GenericBot is an API used to interface with Mediawiki.
 * 
 * Most bot methods may be found in this class.
 * Some logger methods may be found in NetworkingBase.
 * 
 * Implementation:
 * To create a bot, make a new class that extends GenericBot.
 * You will need to provide a main method to run code.
 * 
 * To create a bot with a GUI, check out BotPanel.
 * 
 * @author: ErnieParke/Choco31415
 */
@SuppressWarnings("serial")
public class GenericBot extends NetworkingBase {
	
	protected final long serialVersionUID = 1L;
	
	// Generic variables
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
	
	// Class variables
	private MediawikiDataManager mdm; // Access to the MDM class.
	private String baseURL = ""; // The url on which the bot is currently operating.
	
	// Status variables
	private ArrayList<String> loggedInAtLanguages = new ArrayList<String>(); // A list of wikis the bot is logged into.
	private long lastCommandTimestamp = 0; // The timestamp of the last API command.

	// Configuration variables.
	public int APIlimit = 10; // The maximum items per query call. 
	public int revisionDepth = 10; // The number of revisions to include per page.
	public boolean getRevisions = false; // When getting a page, should revisions be included?
	public boolean getRevisionContent = false; // When getting a revision, should the revision content be included?	
	public double APIthrottle = 0.5; // The minimum amount of time between API commands.
	public int maxFileChunkSize = 20000; // The max size in bytes of a file chunk. Used for file uploads.
	public boolean parseThrough = false; // When page parsing, should templates be fetched to disambiguate between links and templates?
	
	protected final String homeWikiLanguage; // The default wiki of a bot.
	
	protected int interruptedConnectionWait = 5; // How long to wait to retry on a failed connection. 0 = fail completely
	
	public GenericBot(File family_, String homeWikiLanguage_) {				
		// Instantiate the MDM.
		mdm = new MediawikiDataManager();
		
		// Load in the bot family info.
		mdm.readFamily(family_, 0);
		
		// Set variable
		homeWikiLanguage = homeWikiLanguage_;
	}
	
	/**
	 * Get the page at a certain location.
	 * @param loc The location.
	 * @return A Page.
	 */
	public Page getWikiPage(PageLocation loc) {
		JsonNode serverOutput = getWikiPageJsonCode(loc);
		JsonNode pages = serverOutput.findValue("pages");
		JsonNode page = pages.elements().next();
		
		return parseWikiPage(page);
	}
	
	/**
	 * Get the page at a certain location.
	 * @param loc The location.
	 * @return A SimplePage.
	 */
	public SimplePage getWikiSimplePage(PageLocation loc) {
		JsonNode serverOutput = getWikiPageJsonCode(loc);
		JsonNode pages = serverOutput.findValue("pages");
		JsonNode page = pages.elements().next();
		
		return parseWikiSimplePage(page);
	}
	
	/**
	 * Check if a page exists at a location.
	 * @param loc The location.
	 * @return A boolean.
	 */
	public boolean doesPageExist(PageLocation loc) {
		JsonNode serverOutput = getWikiPageJsonCode(loc);
		
		return serverOutput.findValue("missing") == null && serverOutput.findValue("invalid") == null; // If contains missing tag, the page is missing.
	}
	
	private JsonNode getWikiPageJsonCode(PageLocation loc) {		
		String serverOutput = APIcommand(new QueryPageContent(loc));
		
		// Read in the JSON!
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readValue(serverOutput, JsonNode.class);
		} catch (IOException e1) {
			logError("Was expecting Json, but did not receive Json from server.");
			return null;
		}
		
	    logFine(baseURL + " // " + loc.getTitle() + " is downloaded.");
	    
	    return rootNode;
	}
	
	/**
	 * Get the pages at multiple locations. These must all be on the same wiki!
	 * @param locs The locations.
	 * @return An ArrayList of Page.
	 */
	public ArrayList<Page> getWikiPages(ArrayList<PageLocation> locs) {
		ArrayList<Page> pages = new ArrayList<Page>();
		
		// Enforce APIlimit.
		for (int i = 0; i < locs.size(); i += APIlimit) {
			// Get a small chunk of page locations.
			ArrayList<PageLocation> chunkOfPages;
			if (i + APIlimit <= locs.size()) {
				chunkOfPages = new ArrayList<PageLocation>(locs.subList(i, i+APIlimit));
			} else {
				chunkOfPages = new ArrayList<PageLocation>(locs.subList(i, locs.size()));
			}
			
			// Process this small chunk of page locations.
			JsonNode serverOutput = getWikiPagesJsonCode(chunkOfPages);
			
			JsonNode pageNodes = serverOutput.findValue("pages");
			for (JsonNode pageNode : pageNodes) {
				pages.add(parseWikiPage(pageNode));
			}

		}
		
		return pages;
	}
	
	/**
	 * Get the pages at multiple locations. These must all be on the same wiki!
	 * @param locs The locations.
	 * @return An ArrayList of SimplePage.
	 */
	public ArrayList<SimplePage> getWikiSimplePages(ArrayList<PageLocation> locs) {		
		ArrayList<SimplePage> simplePages = new ArrayList<SimplePage>();
		
		// Enforce APIlimit.
		for (int i = 0; i < locs.size(); i += APIlimit) {
			// Get a small chunk of page locations.
			ArrayList<PageLocation> chunkOfPages;
			if (i + APIlimit <= locs.size()) {
				chunkOfPages = new ArrayList<PageLocation>(locs.subList(i, i+APIlimit));
			} else {
				chunkOfPages = new ArrayList<PageLocation>(locs.subList(i, locs.size()));
			}
			
			// Process this small chunk of page locations.
			JsonNode serverOutput = getWikiPagesJsonCode(chunkOfPages);
			
			JsonNode pageNodes = serverOutput.findValue("pages");
			for (JsonNode pageNode : pageNodes) {
				simplePages.add(parseWikiSimplePage(pageNode));
			}
		}
		
		return simplePages;
	}
	
	private JsonNode getWikiPagesJsonCode(ArrayList<PageLocation> locs) {
		if (locs.size() == 0) {
			return null;
		}
		
		String serverOutput = APIcommand(new QueryPageContent(locs));
		
		// Read in the JSON!
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readValue(serverOutput, JsonNode.class);
		} catch (IOException e1) {
			logError("Was expecting Json, but did not receive Json from server.");
			return null;
		}
		
		// Logging
        logFine(baseURL + " // " + locs.get(0).getTitle()
				+ " through " + locs.get(locs.size()-1).getTitle()
				+ " is downloaded.");
		
		// Fine detailed logging
		String finest = "Specifically, this includes: "; 
		for (int i = 0; i < locs.size(); i++) {
			if (i != 0) {
				finest += ", ";
			}
			finest += locs.get(i).getTitle();
		}
		logFinest(finest);
        
        return rootNode;
	}

	protected SimplePage parseWikiSimplePage(JsonNode code) {
		/*
		 * This is a custom built XML parser for Wiki pages.
		 * It creates a SimplePage object.
		 **/
		
		SimplePage newPage = null;
		
		// Parse out page information
		String title = code.get("title").asText();
		int pageid = code.get("pageid").asInt();
		String wikiPrefix = mdm.getWikiPrefixFromURL(baseURL);
		
		// Initialize the SimplePage object with this info.
		newPage = new SimplePage(wikiPrefix, title, pageid);
		
		String rawText = code.findValue("*").asText();
		newPage.setRawText(rawText);
		
		return newPage;
	}
	
	protected Page parseWikiPage(JsonNode code) {
		/*
		 * This is a custom built XML parser for Wiki pages.
		 * It creates a Page object.
		 **/
		
		Page newPage = null;
		
		// Parse out page information
		String title = code.get("title").asText();
		int pageid = code.get("pageid").asInt();
		String wikiPrefix = mdm.getWikiPrefixFromURL(baseURL);
		
		// Initialize the SimplePage object with this info.
		newPage = new Page(wikiPrefix, title, pageid);
		String rawText = code.findValue("*").asText();
		newPage.setRawText(rawText);
		
		// Get revisions, if needed.
		getPageRevisions(newPage);
		return newPage;
	}
	
	/**
	 * @param page The page to attach revisions to.
	 */
	private void getPageRevisions(Page page) {
		// This method fetches the revisions of a page, if needed.
		if (getRevisions) {
			ArrayList<Revision> revisions = getPastRevisions(page.getPageLocation(), revisionDepth, getRevisionContent);
			page.setRevisions(revisions);
		}
	}
	
	/**
	 * Get the revisions for a page at a location.
	 * @param loc The location.
	 * @param localRevisionDepth How deep to go.
	 * @param getContent Whether or not to include the page content at the time of the revision.
	 * @return
	 */
	public ArrayList<Revision> getPastRevisions(PageLocation loc, int localRevisionDepth, boolean getContent) {
		ArrayList<Revision> toReturn = new ArrayList<>();
		
		int revDepth = Math.min(localRevisionDepth, APIlimit);
		String serverOutput = APIcommand(new QueryPageRevisions(loc, revDepth, getContent));
		
		
		// Read in the JSON!
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readValue(serverOutput, JsonNode.class);
		} catch (IOException e1) {
			logError("Was expecting Json, but did not receive Json from server.");
			return null;
		}		
		
		// Parse output for info.
		JsonNode query = rootNode.get("query");
		
		String title = query.findValue("title").asText();
		PageLocation revisionLoc = new PageLocation(loc.getLanguage(), title);
		
		JsonNode revisionList = query.findValue("revisions");
		for (JsonNode revisionNode : revisionList) {
			String user = revisionNode.get("user").asText();
			String comment = revisionNode.get("comment").asText();
			Date date = createDate(revisionNode.get("timestamp").asText());
			
			// Parse for flags
			ArrayList<String> flags = new ArrayList<String>();
			ArrayList<String> flagsToSearchFor = new ArrayList<String>();
			flagsToSearchFor.add("minor");
			flagsToSearchFor.add("new");
			flagsToSearchFor.add("bot");
			for (String flag: flagsToSearchFor) {
				if (revisionNode.has(flag)) {
					// Flag found.
					flags.add(flag);
				}
			}
			
			Revision revision = new Revision(revisionLoc, user, comment, date, flags);
			
			if (getContent) {
				String content = revisionNode.get("*").asText();
				revision.setRevisionContent(content);
			}
			
			toReturn.add(revision);
		}
		
		return toReturn;
	}
	
	/**
	 * This method gets 30 recent changes max per query call, so it might make multiple query calls.
	 * @param depth The amount of revisions you want returned.
	 * @return A list of recent changes wrapped in revisions.
	 */
	public ArrayList<Revision> getRecentChanges(String language, int depth) {
		// This method fetches the recent changes.
		ArrayList<Revision> toReturn = new ArrayList<Revision>();
		String rccontinue = null; // Used to continue queries.
		
		logFine("Getting recent changes.");
		
		int revisionsNeeded = depth;
		do {
			revisionsNeeded = depth - toReturn.size();
			int batchSize = Math.min(APIlimit, revisionsNeeded);
			
			// Make a query call.
			String serverOutput;
			if (rccontinue == null) {
				serverOutput = APIcommand(new QueryRecentChanges(language, batchSize));
			} else {
				serverOutput = APIcommand(new QueryRecentChanges(language, batchSize, rccontinue));
			}
			
			// Read in the JSON!
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readValue(serverOutput, JsonNode.class);
			} catch (IOException e1) {
				logError("Was expecting Json, but did not receive Json from server.");
				return null;
			}
			
			// Read in the revisions returned.
			JsonNode query = rootNode.get("query");
			
			JsonNode rcList = query.findValue("recentchanges");
			for (JsonNode rcNode : rcList) {
				String title = rcNode.get("title").asText();
				PageLocation rcLoc = new PageLocation(language, title);
				String user = rcNode.get("user").asText();
				String comment = rcNode.get("comment").asText();
				Date date = createDate(rcNode.get("timestamp").asText());
				
				// Parse for flags
				ArrayList<String> flags = new ArrayList<String>();
				ArrayList<String> flagsToSearchFor = new ArrayList<String>();
				flagsToSearchFor.add("minor");
				flagsToSearchFor.add("new");
				flagsToSearchFor.add("bot");
				for (String flag: flagsToSearchFor) {
					if (rcNode.has(flag)) {
						// Flag found.
						flags.add(flag);
					}
				}
				
				Revision revision = new Revision(rcLoc, user, comment, date, flags);
				
				toReturn.add(revision);
			}
			
			// Try to continue the query.
			if (rootNode.findValue("rccontinue") != null) {
				rccontinue = rootNode.findValue("rccontinue").asText();
				
				logFiner("Next page batch starts at: " + rccontinue);
			} else {
				rccontinue = null;
			}
		} while (rccontinue != null && toReturn.size() < depth);
		 
		 return toReturn;
	}
	
	/**
	 * Get the pages in a category, non-recursively.
	 * This method gets 100 category members max per query call, so it might make multiple query calls.
	 * @param loc The page location of the category.
	 * @return Returns An ArrayList of PageLocation.
	 */
	public ArrayList<PageLocation> getCategoryPages(PageLocation loc) {
		ArrayList<PageLocation> toReturn = new ArrayList<PageLocation>();
		String cmcontinue = null;
		
		logFine("Getting category pages.");
		
		do {
			// Make a query call.
			String serverOutput;
			if (cmcontinue == null) {
				serverOutput = APIcommand(new QueryCategoryMembers(loc.getLanguage(), loc.getTitle(), Math.min(100, APIlimit)));
			} else {
				serverOutput = APIcommand(new QueryCategoryMembers(loc.getLanguage(), loc.getTitle(), Math.min(100, APIlimit), cmcontinue));
			}
			
			// Read in the JSON!
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readValue(serverOutput, JsonNode.class);
			} catch (IOException e1) {
				logError("Was expecting Json, but did not receive Json from server.");
				return null;
			}

			// Parse page for info.
			JsonNode query = rootNode.get("query");
			
			JsonNode categoryList = query.get("categorymembers");
			for (JsonNode categoryNode : categoryList) {
				String title = categoryNode.get("title").asText();
				PageLocation categoryPage = new PageLocation(loc.getLanguage(), title);
				
				toReturn.add(categoryPage);
			}
			
			// Try continuing the query.
			if (rootNode.findValue("cmcontinue")  != null) {
				cmcontinue = rootNode.findValue("cmcontinue").asText();
				
				logFiner("Next page batch starts at: " + cmcontinue);
			} else {
				cmcontinue = null;
			}
		} while (cmcontinue != null);
		
		return toReturn;
	}
	
	/**
	 * Get the pages in a category, recursively.
	 * This method gets 100 category members max per query call, so it might make multiple query calls.
	 * @param loc The page location of the category.
	 * @return Returns An ArrayList of PageLocation.
	 */
	public ArrayList<PageLocation> getCategoryPagesRecursive(PageLocation loc) {
		ArrayList<PageLocation> pageLocs = new ArrayList<PageLocation>();
		ArrayList<PageLocation> toAdd = new ArrayList<PageLocation>();
		
		toAdd = getCategoryPages(loc);
		
		logFiner("Getting category pages (recursive) for: " + loc.getTitle());
		
		/*
		 * Look through all page names. If there is a category, get those pages.
		 */
		for (PageLocation pageLoc : toAdd) {
			if (pageLoc.getNameSpace().equals("Category")) {
				pageLocs.addAll(getCategoryPagesRecursive(pageLoc));
			} else {
				pageLocs.add(pageLoc);
			}
		}
		
		return pageLocs;
	}
	
	/**
	 * Get the pages in a category, recursively.
	 * This method gets 100 category members max per query call, so it might make multiple query calls.
	 * @param ignore Do not include these categories and pages in the returned result.
	 * @param loc The page location of the category.
	 * @return Returns An ArrayList of PageLocation.
	 */
	public ArrayList<PageLocation> getCategoryPagesRecursive(PageLocation loc, ArrayList<String> ignore) {
		ArrayList<PageLocation> pageLocs = new ArrayList<PageLocation>();
		ArrayList<PageLocation> toAdd = new ArrayList<PageLocation>();
		
		toAdd = getCategoryPages(loc);
		
		logFiner("Getting category pages (recursive) for: " + loc.getTitle());
		
		/*
		 * Look through all page names. If there is a category, get those pages.
		 */
		for (PageLocation pageLoc : toAdd) {
			if (!ignore.contains(pageLoc.getTitle())) {
				if (pageLoc.getNameSpace().equals("Category")) {
					pageLocs.addAll(getCategoryPagesRecursive(pageLoc, ignore));
				} else {
					pageLocs.add(pageLoc);
				}
			}
		}
		
		return pageLocs;
	}
	
	/**
	 * Get all of the pages that link to a location.
	 * This method gets 30 pages max per query call, so it might make multiple query calls.
	 * @param loc The location to get back links for.
	 * @return An ArrayList of PageLocation.
	 */
	public ArrayList<PageLocation> getPagesThatLinkTo(PageLocation loc) {
		return getPagesThatLinkTo(loc, Integer.MAX_VALUE);
	}
	
	/**
	 * Get the pages that link to a location.
	 * This method gets 30 pages per query call, so it might make multiple query calls.
	 * @param loc The location to get back links for.
	 * @param depth The maximum amount of pages to get.
	 * @return An ArrayList of PageLocation.
	 */
	public ArrayList<PageLocation> getPagesThatLinkTo(PageLocation loc, int depth) {
		// This method gets all the pages that link to another page. Redirects are included.
		ArrayList<PageLocation> toReturn = new ArrayList<PageLocation>();
		String blcontinue = null;
		
		logFine("Getting pages that link to: "  + loc.getTitle());
		
		int backlinksNeeded;
		do {
			// Make a query call.
			backlinksNeeded = depth - toReturn.size();
			int batchSize = Math.min(Math.min(30, APIlimit), backlinksNeeded);
			
			String serverOutput;
			if (blcontinue == null) {
				serverOutput = APIcommand(new QueryBackLinks(loc, batchSize));
			} else {
				serverOutput = APIcommand(new QueryBackLinks(loc, batchSize, blcontinue));
			}
	
			// Read in the JSON!
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readValue(serverOutput, JsonNode.class);
			} catch (IOException e1) {
				logError("Was expecting Json, but did not receive Json from server.");
				return null;
			}

			// Parse page for info.
			JsonNode query = rootNode.get("query");
			
			JsonNode backlinkList = query.findValue("backlinks");
			for (JsonNode backlinkNode : backlinkList) {
				String title = backlinkNode.get("title").asText();
				PageLocation backlinkLoc = new PageLocation(loc.getLanguage(), title);
				
				toReturn.add(backlinkLoc);
			}
			
			// Try continuing the query.
			if (rootNode.findValue("blcontinue") != null) {
				blcontinue = rootNode.findValue("blcontinue").asText();
				
				logFiner("Next backlink batch starts at: " + blcontinue);
			} else {
				blcontinue = null;
			}
		} while (blcontinue != null && toReturn.size() < depth);
		
		return toReturn;
	}
	
	/**
	 * This method gets all pages on a wiki.
	 * @param language The wiki.
	 * @param depth The maximum amount of pages to get.
	 * @return An ArrayList of PageLocation.
	 */
	public ArrayList<PageLocation> getAllPages(String language, int depth) {
		return getAllPages(language, depth, null, null);
	}
	
	/**
	 * This method gets all pages on a wiki after a certain prefix.
	 * @param language The wiki.
	 * @param depth The maximum amount of pages to get.
	 * @param from The prefix.
	 * @return
	 */
	public ArrayList<PageLocation> getAllPages(String language, int depth, String from) {
		return getAllPages(language, depth, from, null);
	}
	
	/**
	 * This methods gets all pages on a wiki after a certain prefix and in a certain namespace.
	 * This method gets 30 pages max per query call, so it might make multiple query calls.
	 * 
	 * @param language The wiki.
	 * @param depth The maximum amount of pages to get.
	 * @param from The prefix.
	 * @param apnamespace The id of the namespace being crawled.
	 * @return An ArrayList containing a subset of all pages.
	 */
	public ArrayList<PageLocation> getAllPages(String language, int depth, String from, Integer apnamespace) {
		ArrayList<PageLocation> toReturn = new ArrayList<PageLocation>();
		String apcontinue = null;
		
		logFine("Getting all pages starting from " + from + ".");
		
		do {
			// Make a query call.
			int pagesNeeded = toReturn.size() - depth;
			int batchSize = Math.min(APIlimit, pagesNeeded);
			String serverOutput;
			if (apnamespace == null) {
				if (apcontinue == null) {
					if (from != null) {
						serverOutput = APIcommand(new QueryAllPages(language, batchSize, from));
					} else {
						serverOutput = APIcommand(new QueryAllPages(language, batchSize));
					}
				} else {
					serverOutput = APIcommand(new QueryAllPages(language, batchSize, apcontinue));
				}
			} else {
				if (apcontinue == null) {
					if (from != null) {
						serverOutput = APIcommand(new QueryAllPages(language, batchSize, from, apnamespace));
					} else {
						serverOutput = APIcommand(new QueryAllPages(language, batchSize, apnamespace));
					}
				} else {
					serverOutput = APIcommand(new QueryAllPages(language, batchSize, apcontinue, apnamespace));
				}
			}
			
			// Read in the Json!!!
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readValue(serverOutput, JsonNode.class);
			} catch (IOException e1) {
				logError("Was expecting Json, but did not receive Json from server.");
				return null;
			}

			// Parse page for info.
			JsonNode query = rootNode.get("query");
			
			JsonNode pageList = query.findValue("allpages");
			for (JsonNode pageNode : pageList) {
				String title = pageNode.get("title").asText();
				PageLocation pageLoc = new PageLocation(language, title);
				
				toReturn.add(pageLoc);
			}
			
			// Try continuing the query.
			if (rootNode.findValue("apcontinue") != null) {
				apcontinue = rootNode.findValue("apcontinue").asText();

				logFiner("Next page batch starts at: " + apcontinue);
			} else {
				apcontinue = null;
			}
		} while (apcontinue != null && toReturn.size() < depth);
		
		return toReturn;
	}
	
	/**
	 * Query the wiki for a list of pages with this prefix. Search in the main namespace.
	 * Warning: Only supported in MW v.1.23 and above!
	 * @param language The language of the wiki.
	 * @param prefix The prefix that you are searching for.
	 * @return A list of pages with the given prefix.
	 */
	public ArrayList<PageLocation> getPagesByPrefix(String language, String prefix) {
		return getPagesByPrefix(language, prefix, 0);
	}
	
	/**
	 * Query the wiki for a list of pages with this prefix. Search in the given namespace.
	 * Warning: Only supported in MW v.1.23 and above!
	 * 
	 * @param language The language of the wiki.
	 * @param prefix The prefix that you are searching for.
	 * @param psnamespace The id of the namespace to search in.
	 * @return An ArrayList of pages with the given prefix.
	 */
	public ArrayList<PageLocation> getPagesByPrefix(String language, String prefix, int psnamespace) {
		ArrayList<PageLocation> toReturn = new ArrayList<PageLocation>();
		Integer psoffset = null;
		
		do {
			// Make query call.
			String serverOutput;
			if (psoffset == null) {
				serverOutput = APIcommand(new QueryPrefix(language, prefix, 0, psnamespace));
			} else {
				serverOutput = APIcommand(new QueryPrefix(language, prefix, psoffset, psnamespace));
			}
			
			// Read in the JSON!
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readValue(serverOutput, JsonNode.class);
			} catch (IOException e1) {
				logError("Was expecting Json, but did not receive Json from server.");
				return null;
			}

			// Parse page for info.
			JsonNode query = rootNode.get("query");
			
			JsonNode prefixList = query.findValue("prefixsearch");
			for (JsonNode prefixNode : prefixList) {
				String title = prefixNode.get("title").asText();
				PageLocation prefixLoc = new PageLocation(language, title);
				
				toReturn.add(prefixLoc);
			}	
			
			// Try continuing the query.
			if (rootNode.findValue("psoffset") != null) {
				psoffset = rootNode.findValue("psoffset").asInt();
			} else {
				psoffset = null;
			}
		} while (psoffset != null);
		
		logFine("All pages with prefix " + prefix + " queried.");
		
		return toReturn;
	}
	
	/**
	 * Get the direct url for a file.
	 * @param loc The pageLocation of the file.
	 * @return A String.
	 */
	protected String getDirectImageURL(PageLocation loc) {
		ArrayList<String> properties = new ArrayList<String>();
		properties.add("url");
		ImageInfo info = getImageInfo(loc, properties);
		return info.getValue("url");
	}
	
	/**
	 * Get the url, dimensions, and byte size of a file.
	 * @param loc The pageLocation of the file.
	 * @return A ImageInfo.
	 */
	protected ImageInfo getImageInfo(PageLocation loc) {
		ArrayList<String> properties = new ArrayList<String>();
		properties.add("url");
		properties.add("size");
		return getImageInfo(loc, properties);
	}
	
	/**
	 * Query the properties of a file.
	 * 
	 * The list of accepted properties to query is here: https://www.mediawiki.org/wiki/API:Imageinfo
	 * 
	 * Getting metadata, commonmetadata, or extmetadata will return a Json string.
	 * 
	 * Getting size also returns width and height.
	 * 
	 * @param loc The pageLocation of the file.
	 * @param propertyNames The list of properties you are querying for.
	 * @return A ImageInfo.
	 */
	protected ImageInfo getImageInfo(PageLocation loc, ArrayList<String> propertyNames) {
		logFine("Getting file info for: " + loc.getTitle());
		logFiner("Getting properties: " + ArrayUtils.compactArray(propertyNames, ", "));
		
		String serverOutput = APIcommand(new QueryImageInfo(loc, propertyNames));
		
		// Read in the JSON!
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readValue(serverOutput, JsonNode.class);
		} catch (IOException e1) {
			logError("Was expecting Json, but did not receive Json from server.");
			return null;
		}
		
		// Check for not found or redirect
		if (rootNode.findParent("-1") != null) {
			logWarning(loc.getTitle() + " does not exist.");
			return null;
		}
		if (rootNode.findValue("imagerepository").asText().equals("")) {
			logWarning(loc.getTitle() + " is a redirect, not a file.");
			return null;
		}
		
		// Set up ImageInfo class
		ImageInfo toReturn = new ImageInfo(loc);
		
		// Get info
		int property = 0;
		do {
			String name = propertyNames.get(property).toLowerCase();
			String value = "";
			
			// Handle special cases
			if (name.equalsIgnoreCase("dimensions")) {
				name = "size";//Alias
			}
			if (name.equalsIgnoreCase("size")) {
				// Size returns size, width, and height
				propertyNames.add("width");
				propertyNames.add("height");
			}
			if (name.equalsIgnoreCase("UploadWarning")) {
				// UploadWarning returns HTML
				name = "html";
			}
			
			// Get value
			if (name.equals("metadata") || name.equals("commonmetadata") || name.equals("extmetadata")) {
				// Mediawiki returns JSON for these parameters.
				try {
					value = mapper.writeValueAsString(rootNode.findValue(name));
				} catch (JsonProcessingException e) {
					logError("Json Processing Error: " + e.getLocalizedMessage());
					e.printStackTrace();
				}
			} else {
				// Mediawiki returns String for these parameters
				value = rootNode.findValue(name).asText();
			}
			
			// Store value
			toReturn.addProperty(name, value);
			
			property++;
		} while (property < propertyNames.size());
		
		return toReturn;
	}
	
	/**
	 * Query if the user exists or not.
	 * Warning: Only supported in MW v.1.12 and above!
	 * @param user The user to check on.
	 * @return A boolean.
	 */
	protected boolean doesUserExist(User user) {
		return getUserInfo(user, new ArrayList<String>()) != null;
	}
	
	/**
	 * Query the block info, groups, and editcount of a user.
	 * Warning: Only supported in MW v.1.12 and above!
	 * @param user The user to check on.
	 * @return A UserInfo.
	 */
	protected UserInfo getUserInfo(User user) {
		ArrayList<String> properties = new ArrayList<String>();
		properties.add("blockinfo");
		properties.add("groups");
		properties.add("editcount");
		
		return getUserInfo(user, properties);
	}
	
	/**
	 * Query the properties of a user.
	 * 
	 * The list of accepted properties to query is here: https://www.mediawiki.org/wiki/API:Users
	 * 
	 * This method does not support getting the properties group, implicitgroup, rights, centralid, or cancreate.
	 * In fact, doing so will throw an error.
	 * 
	 * Also, banned and invalid user will be returned as null.
	 * 
	 * Warning: Only supported in MW v.1.12 and above!
	 * @param user The user to check on.
	 * @param propertyNames The list of properties you are querying for.
	 * @return An ArrayList of ImageInfo
	 */
	protected UserInfo getUserInfo(User user, ArrayList<String> propertyNames) {
		ArrayList<User> userNames = new ArrayList<User>();
		userNames.add(user);
		return getUserInfo(userNames, propertyNames).get(0);
	}
	
	/**
	 * Query the block info, groups, and edit count of multiple users. All users must be from the same wiki!
	 * 
	 * This method gets 50 users max per query call, so it might make multiple query calls.
	 * 
	 * Warning: Only supported in MW v.1.12 and above!
	 * @param language The language of the wiki with all users.
	 * @param userNames The usernames to check on.
	 * @return An ArrayList of UserInfo.
	 */
	protected ArrayList<UserInfo> getUserInfo(ArrayList<User> users) {
		ArrayList<String> properties = new ArrayList<String>();
		properties.add("blockinfo");
		properties.add("groups");
		properties.add("editcount");
		
		return getUserInfo(users, properties);
	}
	
	/**
	 * Query the properties of multiple users. All users must be from the same wiki!
	 * 
	 * The list of accepted properties to query is here: https://www.mediawiki.org/wiki/API:Users
	 * 
	 * Getting centralids also returns attachedlocal.
	 * While Centralids is supported, it is currently experimental in MediaWiki.
	 * 
	 * All non-existent users will be returned as null.
	 * 
	 * This method gets 50 users max per query call, so it might make multiple query calls.
	 * 
	 * Warning: Only supported in MW v.1.12 and above!
	 * @param language The language of the wiki with all users.
	 * @param userNames The usernames to check on.
	 * @param propertyNames The list of properties you are querying for.
	 * @return An ArrayList of ImageInfo.
	 */
	protected ArrayList<UserInfo> getUserInfo(ArrayList<User> users, ArrayList<String> propertyNames) {
		// Logging
		String userLogMessage = "Getting user info for: ";
		for (User u : users) {
			userLogMessage += u.getUserName() + ", ";
		}
		
		logFine(userLogMessage);
		logFiner("Getting properties: " + ArrayUtils.compactArray(propertyNames, ", "));
		
		// Method code below
		ArrayList<UserInfo> toReturn = new ArrayList<UserInfo>();
		
		String language = users.get(0).getLanguage();
		
		// Enforce APIlimit
		int querySize = Math.min(50, APIlimit);
		for (int i = 0; i < users.size(); i += querySize) {
			// Get a small chunk of users.
			ArrayList<User> chunkOfUsers = new ArrayList<User>();
			if (i + querySize <= users.size()) {
				chunkOfUsers = new ArrayList<User>(users.subList(i,  i + querySize));
			} else {
				chunkOfUsers = new ArrayList<User>(users.subList(i, users.size()));
			}
			
			// Query the server.
			String serverOutput = APIcommand(new QueryUsers(chunkOfUsers, propertyNames));
			
			// Read in the JSON!
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readValue(serverOutput, JsonNode.class);
			} catch (IOException e1) {
				logError("Was expecting Json, but did not receive Json from server.");
				return null;
			}
			
			// Parse out user info.
			boolean firstUser = true;
			for (JsonNode user : rootNode.findValue("users")) {
				String userName = user.findValue("name").asText();
				UserInfo userInfo = new UserInfo(new User(language, userName));
				boolean userExists = true;
				
				// Check that the user exists.
				if (user.findParent("missing") != null || user.findParent("invalid") != null) {
					userInfo = null;
					userExists = false;
				}
				
				if (userExists) {
					// Parse for queried properties one at a time
					int property = 0;
					do {
						String propName = propertyNames.get(property).toLowerCase();
						String value = "";
						
						// Handle special cases, while avoiding duplicates
						if (firstUser) {
							if (propName.equalsIgnoreCase("centralids")) {
								propertyNames.add("attachedlocal");
							}
						}
						
						// Get and store values
						if (propName.equals("blockinfo")) {
							// This is a doozie to handle. Twitch a twitch.
							if (user.findValue("blockid") == null) {
								userInfo.setAsNotBlocked();
							} else {
								// This user is blocked.
								int blockID = user.findValue("blockid").asInt();
								String blockedBy = user.findValue("blockedby").asText();
								String blockReason = user.findValue("blockreason").asText();
								String blockExpiration = user.findValue("blockexpiry").asText();
								
								userInfo.setBlockInfo(blockID, blockedBy, blockReason, blockExpiration);
							}
						} else {
							if (propName.equals("groups") || propName.equals("implicitgroups") || propName.equals("rights")) {
								// Mediawiki returns Json array for these parameters.
								ArrayList<String> temp = new ArrayList<String>();
								
								for (JsonNode node : user.findValue(propName)) {
									temp.add(node.asText());
								}
								
								switch (propName) {
									case "groups":
										userInfo.setGroups(temp);
										break;
									case "implicitgroups":
										userInfo.setImplicitGroups(temp);
										break;
									case "rights":
										userInfo.setRights(temp);
										break;
									default:
										break;
								}
							} else {
								if (propName.equals("centralid") || propName.equals("attachedlocal")) {
									// Mediawiki returns JSON for these parameters.
									try {
										value = mapper.writeValueAsString(rootNode.findValue(propName));
									} catch (JsonProcessingException e) {
										logError("Json Processing Error: " + e.getLocalizedMessage());
										e.printStackTrace();
									}
								} else {
									// Mediawiki returns String for these parameters
									value = rootNode.findValue(propName).asText();
								}
								
								if (propName.equals("emailable")) {
									value = user.findValue("emailable") != null ? "true" : "false";
								}
								
								userInfo.addProperty(propName, value);
							}
						}
						
						property++;
					} while (property < propertyNames.size());
				}
				
				// Store the user info
				toReturn.add(userInfo);	
				
				firstUser = false;
			}
		}
		
		return toReturn;
	}
	
	/**
	 * Get the contributions of a user, up to a certain limit.
	 * @param users The user to query.
	 * @param depth The max amount of revisions, per user, to return.
	 * @return An ArrayList of a user's contributions.
	 */
	public ArrayList<Revision> getUserContribs(User user, int depth) {
		ArrayList<User> users = new ArrayList<User>();
		users.add(user);
		return getUserContribs(users, depth).get(0);
	}
	
	/**
	 * Get the contributions of multiple users, up to a certain limit.
	 * @param users The users to query.
	 * @param depth The max amount of revisions, per user, to return.
	 * @return An ArrayList of ArrayList of Revision. In other words, it is a list of a users' list of contributions.
	 */
	public ArrayList<ArrayList<Revision>> getUserContribs(ArrayList<User> users, int depth) {
		// Logging
		String userLogMessage = "Getting contribs for users: ";
		for (User u : users) {
			userLogMessage += u.getUserName() + ", ";
		}
		
		logFine(userLogMessage);
		
		// Method code below
		ArrayList<ArrayList<Revision>> multiContribs = new ArrayList<>();
		
		String language = users.get(0).getLanguage();
		
		// For now, we only query certain properties.
		ArrayList<String> properties = new ArrayList<String>();
		properties.add("title");
		properties.add("comment");
		properties.add("timestamp");
		properties.add("flags");
		
		// Query users individually.
		int maxQuerySize = Math.min(50, APIlimit);
		for (int u = 0; u < users.size(); u++) {
			// Get a user.
			User user = users.get(u);
			
			// Query user's contributions.
			multiContribs.add(new ArrayList<Revision>());
			boolean moreRevisionsExist = true;
			String queryContinue = null; // User for continuing queries.
			while (multiContribs.get(u).size() < depth && moreRevisionsExist) {
				// Query the server.
				int querySize = Math.min(maxQuerySize, depth - multiContribs.get(u).size());

				APIcommand queryUserContribs = new QueryUserContribs(user, properties, querySize);
				if (queryContinue != null) {
					queryUserContribs.addParameter("ucstart", queryContinue);
				}
				
				String serverOutput = APIcommand(queryUserContribs);
				
				
				// Read in the JSON!
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = null;
				try {
					rootNode = mapper.readValue(serverOutput, JsonNode.class);
				} catch (IOException e1) {
					logError("Was expecting Json, but did not receive Json from server.");
					return null;
				}
				
				// Parse JSON for contribs
				JsonNode queryNode = rootNode.findValue("query");
				JsonNode userContribs = queryNode.findValue("usercontribs");
				
				for (int contribID = 0; contribID < userContribs.size(); contribID++) {
					JsonNode contrib = userContribs.get(contribID);
					
					// Parse for revision info
					String title = contrib.findValue("title").textValue();
					PageLocation loc = new PageLocation(language, title);
					String userName = user.getUserName();
					String comment = contrib.findValue("comment").textValue();
					Date date = createDate(contrib.findValue("timestamp").textValue());
					
					// Parse for flags
					ArrayList<String> flags = new ArrayList<String>();
					
					if (contrib.has("new")) {
						flags.add("new");
					}
					if (contrib.has("top")) {
						flags.add("top");
					}
					if (contrib.has("minor")) {
						flags.add("minor");
					}
					
					// Package and ship the revision! Then have it sink due to a bunyip and cucumber sandwiches.
					multiContribs.get(u).add(new Revision(loc, userName, comment, date, flags));
				}
				
				// Parse for query continue
				JsonNode queryContinueNode = rootNode.findValue("query-continue");
				if (queryContinueNode == null) {
					moreRevisionsExist = false;
				} else {
					JsonNode ucstartNode = queryContinueNode.findValue("ucstart");
					queryContinue = ucstartNode.textValue();
				}
			}
		}
		
		return multiContribs;
	}
	
	/**
	 * Get the site statistics for a wiki.
	 * 
	 * Warning: Only supported in MW v.1.11 and above!
	 * @param language The wiki to get site statistics for.
	 * @return A SiteStatistics containing the site statistics.
	 */
	public SiteStatistics getSiteStatistics(String language) {
		// Logging
		String userLogMessage = "Getting site statistics for wiki: " + language;
		
		logFine(userLogMessage);
		
		// Method code below.
		ArrayList<String> propertyNames = new ArrayList<String>();
		propertyNames.add("statistics");
		
		SiteStatistics container = new SiteStatistics(language);
		
		// Query the server.
		container = (SiteStatistics) getSiteInfo(container, language, propertyNames);
		
		return container;
		
	}
	
	/**
	 * Get info about a wiki's properties.
	 * 
	 * @param container An InfoContainer to store info in.
	 * @param language The wiki to get site info for.
	 * @param propertyNames The site properties to query.
	 * @returnA The input infoContainer updated to include server output.
	 */
	public InfoContainer getSiteInfo(InfoContainer container, String language, ArrayList<String> propertyNames) {		
		// Method code below
			
		// Query the server.
		String serverOutput = APIcommand(new QuerySiteInfo(language, propertyNames));
		
		// Read in the JSON!
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readValue(serverOutput, JsonNode.class);
		} catch (IOException e1) {
			logError("Was expecting Json, but did not receive Json from server.");
			return null;
		}
		
		// Parse out site info / queried properties.
		int property = 0;
		do {
			String propName = propertyNames.get(property).toLowerCase();
			
			// First off, we'll start by getting the property object.
			JsonNode propNode = rootNode.findValue(propName);
			
			// Secondly, we'll iterate over all data pairs in the property object.
			Iterator<Map.Entry<String, JsonNode>> nodeIterator = propNode.fields();
			while ( nodeIterator.hasNext() ) {
				Map.Entry<String, JsonNode> childPropNode = nodeIterator.next();
				String childName = childPropNode.getKey();
				String value = childPropNode.getValue().asText();
				
				// Store the info.
				container.addProperty(childName, value);
			}
			
			// Next!
			property++;
		} while (property < propertyNames.size());
		
		return container;
	}
	
	/**
	 * Upload a file to a wiki. The max file size supported is ~2GB.
	 * 
	 * @param loc The PageLocation to upload to.
	 * @param localPath The local path of the file.
	 * @param uploadComment The upload comment.
	 * @param pageText The page text to use.
	 * @return
	 */
	public void uploadFile(PageLocation loc, Path localPath, String uploadComment, String pageText) {
		// Get prepared to read in the file.
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(localPath);
		} catch (IOException e) {
			logError("Couldn't read " + localPath + " for upload.");
			return;
		}
		
		// Useful data
		int filesize = bytes.length;
		int offset = 0;
		String filekey = "";
		String results = "";
		boolean firstLoop = true;
		
		do {
			// Prepare a chunk for sending.
			byte[] chunk;
			if (offset < filesize-maxFileChunkSize) {
				chunk = Arrays.copyOfRange(bytes, offset, offset+maxFileChunkSize);
			} else {
				chunk = Arrays.copyOfRange(bytes, offset, filesize);
			}
			
			// Send it.
			String serverOutput;
			if (firstLoop) {
				serverOutput = APIcommand(new UploadFileChunk(loc, filesize, chunk));
			} else {
				serverOutput = APIcommand(new UploadFileChunk(loc, filesize, chunk, offset, filekey));
			}
			
			// Read in the Json!!!
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readValue(serverOutput, JsonNode.class);
			} catch (IOException e1) {
				logError("Was expecting Json, but did not receive Json from server.");
				return;
			}
			
			// Read in server output.
			JsonNode uploadNode = rootNode.get("upload");
			
			results = uploadNode.get("result").asText();
			if (!results.equals("Success")){
				offset = Integer.parseInt(uploadNode.get("offset").asText());
			}
			filekey = uploadNode.get("filekey").asText();
			
			
			firstLoop = false;
		} while (!results.equals("Success"));
		
		// Read the results!
		results = APIcommand(new UploadFileChunk(loc, filekey, uploadComment, pageText));
	}
	
	/**
	 * Log into a wiki.
	 * @param user The username.
	 * @param password The password.
	 * @return Login success status.
	 */
	public boolean logIn(User user, String password) {       
        baseURL = mdm.getWikiURL(user.getLanguage());
        
        try {
        	logCookies();
        } catch (NullPointerException e) {
        	logFinest("No cookies detected.");
        }

        // Log in!
		APIcommand login = new Login(user, password);
		
		String serverOutput = APIcommand(login);
    	
        logCookies();
        
        boolean success = serverOutput.contains("Success");
		logFiner("Login status at " + user.getLanguage() + ": " + success);
        
		if (success) {
			loggedInAtLanguages.add(user.getLanguage());
		}
		
        return success;
	}
	
	/**
	 * Perform an APIcommand.
	 * @param command The APIcommand.
	 * @return Server output.
	 */
	public String APIcommand(APIcommand command) {
		// Check throttle.
		throttleAction();
		
		// Do the command!
		baseURL = mdm.getWikiURL(command.getPageLocation().getLanguage());
		
		String textReturned = "";
		boolean networkError;
		do {
			networkError = false; // No bugs have occurred yet this loop...
			
			// Look out for network issues. Attempt the command.
			try {
				if (command.requiresPOST()) {
					if (command.requiresEntity()) {
						textReturned = APIcommandHttpEntity(command);
					} else {
						textReturned = APIcommandPOST(command);
					}
				} else {
					textReturned = APIcommandHTTP(command);
				}
			} catch (Error e) {
				e.printStackTrace();
				// Network issues encountered. Handle them.
				networkError = true;
				if (interruptedConnectionWait > 0) {
					logInfo("Network issue encountered. Waiting " + interruptedConnectionWait + " seconds.");
					try {
						Thread.sleep(interruptedConnectionWait*1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} else {
					throw e;
				}
			}
		} while (networkError);
		
		logFinest("API results obtained.");

		// Handle mediawiki output.
		if (textReturned != null) {
			if (!command.doesKeyExist("format") || command.getValue("format").equalsIgnoreCase("html") || textReturned.contains("DOCTYPE")) {
				// We are handling HTML output. It is the default output format.
				// We will parse it for any errors/warnings.
				
				//Unescape html
				textReturned = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(textReturned));
				
				if (textReturned.contains("This is an auto-generated MediaWiki API documentation page")) {
					//You got the Mediawiki API documentation sent back.
					logFinest("MediawikiAPI documentation page returned.");
					
					String error = parseTextForItem(textReturned, "error code", "\"");
					if (textReturned.contains("info=")) {
						error += ":" + parseTextForItem(textReturned, "info", "\"");
					}
					logError(error);
					throw new Error(error);
				} else {
					// Log a small portion of the html. It's nice.
					if (textReturned.length() < 1000) {
						logFinest("HTML: " + textReturned);
					} else {
						logFinest("HTML: " + textReturned.substring(0, 1000));	
					}

					if (textReturned.contains("<warnings>")) {
						logError("Warnings were received when editing " + command.getTitle() + ".");
					} else {
						// Check other possibilities for errors/warnings being returned..
						String errorMessage = null;
						if (textReturned.contains("\"warning")) {
							errorMessage = "\"warning";
						}
						if (textReturned.contains("\"error")) {
							errorMessage = "\"error";
						}
						
						if (errorMessage != null) {
							// Errors/warnings detected.
							String errorSnippet = parseTextForItem(textReturned, errorMessage, "}");
							String error = "";
							
							for (int i = 0, prevI = 0; i != -1; prevI = i, i = errorSnippet.indexOf("\n", i+1)) {
								if (prevI != 0) {
									String temp = errorSnippet.substring(prevI, i);
									temp = temp.replace("\n", "");
									temp = temp.trim();
									error += temp + " | ";
								}
							}
							logError(error);
						} else {
							// Everything looks ok.
							logFinest(command.getTitle() + " has been updated.");
						}
					}
				}
			} else if (command.getValue("format").equalsIgnoreCase("xml")) {
				// We are handling XML output. We do not do anything.
				logFinest("XML received.");
				if (textReturned.length() < 1000) {
					logFinest("XML: " + textReturned);
				} else {
					logFinest("XML: " + textReturned.substring(0, 1000));	
				}
			} else if (command.getValue("format").equalsIgnoreCase("php")) {
				// We are handling PHP output. We do not do anything.
				logFinest("PHP received.");
				if (textReturned.length() < 1000) {
					logFinest("PHP: " + textReturned);
				} else {
					logFinest("PHP: " + textReturned.substring(0, 1000));	
				}
			} else if (command.getValue("format").equalsIgnoreCase("Json")){
				// We are handling Json output.
				// We will look for errors/warnings.
				
				// Error handling
				if (textReturned.contains("This is an auto-generated MediaWiki API documentation page")) {
					// The Mediawiki API documentation was returned.
					logFinest("Mediawiki API documentation page returned.");
					
					throw new Error("Mediawiki API documentation page returned.");
				} else {
					// Log a small portion of the JSON. It's nice.
					if (textReturned.length() < 1000) {
						logFinest("Json: " + textReturned);
					} else {
						logFinest("Json: " + textReturned.substring(0, 1000));	
					}

					if (textReturned.contains("Internal Server Error")) {
						logError("Internal Server Error");
						logFinest(textReturned);
						throw new Error("Internal Server Error");
					} else {
				        logFinest("Downloaded page " + command.getPageLocation().getTitle() + ".");
					}
				}
			}
		}
		
		return textReturned;
	}

	/**
	 * Perform a HTTP APIcommand.
	 * @param command The APIcommand.
	 * @return Server output.
	 */
	private String APIcommandHTTP(APIcommand command) {
		// Build the command url.
		String url = baseURL + "/api.php?";
		String[] editKeys = command.getKeysArray();
		String[] editValues = command.getValuesArray();
		
		for (int i = 0; i < editKeys.length; i++) {
			url += URLencode(editKeys[i]) + "=" + URLencode(editValues[i]);
			if (i != editKeys.length-1) {
				url += "&";
			}
		}
		
		// Run the url!
		try {
			String output = removeBOM(EntityUtils.toString(getURL(url)));
			if (output == null) {
				throw new NetworkError("Cannot connect to server at: " + baseURL);
			} else {
				return output;
			}
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	/**
	 * Perform a POST APIcommand.
	 * @param command The APIcommand.
	 * @return Server output.
	 */
	private String APIcommandPOST(APIcommand command) {
		// Build the POST request.
		HttpEntity response;
		
		// Add a token to our POST request
		String token = getToken(command);
		
		if (command.getCommandName() == "login") {
			command.addParameter("lgtoken", token);
		} else {
			command.addParameter("token", token);
		}
		
		// Get the key and value pairs of the command.
		String[] keys = command.getKeysArray();
		String[] values = command.getValuesArray();		
		
		// Send the command!
        response = getPOST(baseURL + "/api.php?", keys, values);
        try {
			String serverOutput = removeBOM(EntityUtils.toString(response));
			
			return serverOutput;
		} catch (org.apache.http.ParseException | IOException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	private String APIcommandHttpEntity(APIcommand command) {
		// Build the command url
		String url = baseURL + "/api.php?";
		String[] editKeys = command.getKeysArray();
		String[] editValues = command.getValuesArray();
		
		for (int i = 0; i < editKeys.length; i++) {
			url += URLencode(editKeys[i]) + "=" + URLencode(editValues[i]);
			if (i != editKeys.length-1) {
				url += "&";
			}
		}
		
		// Add a token to our POST request
		String token = getToken(command);
		HttpEntity entity = command.getHttpEntity(token);
		
		HttpEntity response = getPOST(url, entity);
		
		try {
			String serverOutput = removeBOM(EntityUtils.toString(response));
			
			return serverOutput;
		} catch (org.apache.http.ParseException | IOException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	/**
	 * Some MW actions require a token to execute. This is for security purposes.
	 * This method gets that security token.
	 * @param command The command that requires the token.
	 * @return A token.
	 * @outdated
	 */
	protected String getToken(APIcommand command) {
		String[] keys = null;
		String[] values = null;
		
		VersionNumber MWVersion = command.getMWVersion();
		String tokenType;
		
		// Token getting changes a bit between MW versions 1.20 and 1.24.
		if (MWVersion.compareTo("1.24") >= 0) {
			tokenType = command.getNewTokenType();
		} else {
			tokenType = command.getOldTokenType();
		}
		String tokenField = tokenType + "token";
		
		// Build the API call
		// Handle special cases first.
		if (tokenType.equals("login") && MWVersion.compareTo("1.27") < 0) {
			// Login format does not change pre 1.26.
			keys = new String[]{"action", "lgname", "lgpassword", "format"};
			values = new String[]{"login", command.getValue("lgname"), command.getValue("lgpassword"), "json"};
			tokenField = "token";
		} else if (tokenType.equals("rollback") && MWVersion.compareTo("1.24") < 0) {
			keys = new String[]{"action", "prop", "rvtoken", "titles", "user", "format"};
			values = new String[]{"query", "revisions", "rollback", command.getTitle(), command.getValue("user"), "xml"};
		} else {
			// General cases
			if (MWVersion.compareTo("1.24") >= 0) {
				keys = new String[]{"action", "meta", "type", "format"};
				values = new String[]{"query", "tokens", tokenType, "json"};
			} else if (MWVersion.compareTo("1.20") >= 0) {
				keys = new String[]{"action", "type", "format"};
				values = new String[]{"tokens", tokenType, "json"};
			} else {
				keys = new String[]{"action", "prop", "intoken", "titles", "format"};
				values = new String[]{"query", "info", tokenType, command.getTitle(), "json"};
			}
		}
        
        HttpEntity entity = getPOST(baseURL + "/api.php?", keys, values);
        
		String token = "";
		try {
			String serverOutput = removeBOM(EntityUtils.toString(entity));
			
			logFinest("Received token response: " + serverOutput);

			// Read in the Json!!!
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = mapper.readValue(serverOutput, JsonNode.class);
			} catch (IOException e1) {
				logError("Was expecting Json, but did not receive Json from server.");
				return null;
			}
			
			// Fetch the token!!!
			if (rootNode.findValue(tokenField) != null) {
				token = rootNode.findValue(tokenField).asText();
			} else {
				throw new Error("Something failed with your API command. Make sure that you are logged in, aren't moving a page to an existing page, ect...");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return token;
	}
	
	/*
	 * <notice>
	 * 
	 * 
	 * The code below is some minor class code. Unless you are an advanced user, you can ignore it.
	 * 
	 * 
	 * </notice>
	 */
	
	/**
	 * This takes a String date and converts it into a Date object.
	 * @param text A String representing a date.
	 * @return A Date object.
	 */
	public Date createDate(String text) {
		Date date = null;
		try {
			date = dateFormat.parse(text);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return date;
	}
	
	/**
	 * This method makes sure that the bot does not do particular actions too quickly.
	 */
	private void throttleAction() {
		long currentTime = System.currentTimeMillis();
		long timeDifference = currentTime - lastCommandTimestamp;
		long timeToWait = (long) (1000*APIthrottle - timeDifference);
		if (timeToWait > 0) {
			sleep(timeToWait);
		}
		lastCommandTimestamp = System.currentTimeMillis();
	}
	
	private void sleepInSeconds(double time) {
		try {
			Thread.sleep((int)(1000*time));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String getHomeWikiLanguage() { return homeWikiLanguage; }
	public boolean shouldParseThrough() { return parseThrough; }
}
