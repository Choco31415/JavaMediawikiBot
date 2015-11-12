package WikiBot.Core;
 
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList; 
import java.util.Arrays;
import java.util.Locale;

import static org.apache.commons.lang3.StringEscapeUtils.*;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import WikiBot.APIcommands.APIcommand;
import WikiBot.APIcommands.Query.*;
import WikiBot.Content.Image;
import WikiBot.Content.Interwiki;
import WikiBot.Content.Link;
import WikiBot.Content.Page;
import WikiBot.Content.PageLocation;
import WikiBot.Content.Position;
import WikiBot.Content.Revision;
import WikiBot.Content.Section;
import WikiBot.Content.SimplePage;
import WikiBot.Content.Template;

/**
 * Generic Bot is the parent of every other bot.
 */
public class GenericBot extends javax.swing.JPanel {
	
	protected static final long serialVersionUID = 1L;
	protected static ArrayList<String> log = new ArrayList<String>();
	protected static ArrayList<String> Interwiki;
	protected static ArrayList<String> InterwikiURL;
	protected static ArrayList<String> MagicWords = new ArrayList<String>();
	protected static ArrayList<String> MWEscapeOpenText = new ArrayList<String>();
	protected static ArrayList<String> MWEscapeCloseText = new ArrayList<String>();
	protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
	protected static String baseURL = "http://wiki.scratch.mit.edu/w";
	
	protected static int numErrors = 0;
	protected static ArrayList<String> loggedInAtLanguages = new ArrayList<String>();//This arraylist keeps track of which languages you are logged in at.
	
    private static HttpClient httpclient;
	private static HttpClientContext context;

	//Configuration variables.
	protected static int APIlimit = 10;//The [hard] maximum items per query call. 
	protected static int revisionDepth = 10;//The number of revisions to include per page.
	protected static boolean getRevisionContent = false;//When getting a page, should previous page content be queried?
	protected static boolean printPageDownloads;//Should the bot log page downloads?
	
	public GenericBot() {
		//Read in some files.
		ArrayList<String> temp = readFileAsList("/MWEscapeTexts.txt", 0, true, true);
		for (int i = 0; i < temp.size(); i += 2) {
			MWEscapeOpenText.add(temp.get(i));
			MWEscapeCloseText.add(temp.get(i + 1));
		}
		
		temp = readFileAsList("/MagicWords.txt", 0, true, true);
		for (int i = 0; i < temp.size(); i += 1) {
			MagicWords.add(temp.get(i));
		}
		
		httpclient = HttpClientBuilder.create().build();
		context =  HttpClientContext.create();
	}
	
	public Page getWikiPage(PageLocation loc) {
		//This method fetches a Wiki page.
		String XMLcode = getWikiPageXMLCode(loc);
		
		return parseWikiPage(XMLcode);
	}
	
	static public SimplePage getWikiPageSimple(PageLocation loc) {
		//This method fetches a Wiki page.
		String XMLcode = getWikiPageXMLCode(loc);
		
		return parseWikiPageSimple(XMLcode);
	}
	
	static public boolean doesPageExist(PageLocation loc) {
		String XMLcode = getWikiPageXMLCode(loc);
		
		return !XMLcode.contains("\"pages\":{\"-1\"");
	}
	
	static private String getWikiPageXMLCode(PageLocation loc) {		
		String page = APIcommand(new QueryPageContent(loc));
		
	    if (printPageDownloads) {
	    	pageDownloaded(loc.getTitle());
	    }
	    
	    return page;
	}
	
	static public ArrayList<Page> getWikiPages(ArrayList<PageLocation> locs) {
		
		ArrayList<Page> pages = new ArrayList<Page>();
		
		String XMLcode = getWikiPagesXMLCode(locs);
		
		ArrayList<String> pageXMLstrings = getXMLItems(XMLcode, "pageid", "\"}]}", 0);
		for (String st : pageXMLstrings) {
			pages.add(parseWikiPage(st + "\"}]}"));
		}
		
		return pages;
	}
	
	static public ArrayList<SimplePage> getWikiPagesSimple(ArrayList<PageLocation> locs) {		
		ArrayList<SimplePage> simplePages = new ArrayList<SimplePage>();
		
		String XMLstring = getWikiPagesXMLCode(locs);
		
		ArrayList<String> pageXMLstrings = getXMLItems(XMLstring, "pageid", "\"}]}", 0);
		for (String st : pageXMLstrings) {
			simplePages.add(parseWikiPageSimple(st + "\"}]}"));
		}
		
		return simplePages;
	}
	
	static private String getWikiPagesXMLCode(ArrayList<PageLocation> locs) {
		if (locs.size() == 0) {
			return null;
		}
		
		String XMLstring = APIcommand(new QueryPageContent(locs));
		
        if (printPageDownloads) {
        	if (locs.size() > 1) {
        		pageDownloaded(locs.get(0).getTitle(), locs.get(locs.size()-1).getTitle());
        	} else {
        		pageDownloaded(locs.get(0).getTitle());
        	}
        }
        
        return XMLstring;
	}

	static protected SimplePage parseWikiPageSimple(String XMLcode) {
		/*
		 * This is a custom built XML parser for Wiki pages.
		 * It creates a SimplePage object.
		 **/
		
		SimplePage newPage = null;
		String title = parseXMLforInfo("title", XMLcode, "\",", 3, 0);
		try {
			newPage = new SimplePage(title, getLanguageFromURL(baseURL), Integer.parseInt(parseXMLforInfo("pageid", XMLcode, ",", 2, 0)));
		} catch (NumberFormatException e) {
			throw new Error("Incorrect page name: " + title + " BaseURL: " + baseURL);
		}
		newPage.setRawText(XMLcode.substring(XMLcode.indexOf("\"*\"") + 5, XMLcode.indexOf("\"}]}")));
		
		return newPage;
	}
	
	static protected Page parseWikiPage(String XMLcode) {
		/*
		 * This is a custom built XML parser for Wiki pages.
		 * It creates a Page object.
		 **/
		
		Page newPage = null;
		String title = parseXMLforInfo("title", XMLcode, "\",", 3, 0);
		try {
			newPage = new Page(title, Integer.parseInt(parseXMLforInfo("pageid", XMLcode, ",", 2, 0)), getLanguageFromURL(baseURL));
		} catch (NumberFormatException e) {
			throw new Error("Incorrect page name: " + title + " BaseURL: " + baseURL);
		}
		newPage.setRawText(XMLcode.substring(XMLcode.indexOf("\"*\"") + 5, XMLcode.indexOf("\"}]}")));
		
		String line = "";
		int j = 0;
		for (int i = XMLcode.indexOf("\"*\"") + 4; i!=-1; i=j) {
			j = XMLcode.indexOf("\n", i+1);

			if (j > 0) {
				line = XMLcode.substring(i+1, j);
				newPage.addLine(line);

			} else {
				line = XMLcode.substring(i+1, XMLcode.indexOf("\"}]}"));
				newPage.addLine(line);
			}
		}
		parsePageForTemplates(newPage);
		parsePageForLinks(newPage);
		parsePageForSections(newPage);
		getPastReveisions(newPage);
		return newPage;
	}
	
	static private void parsePageForTemplates(Page page) {
		Template temp;
		ArrayList<String> lines = page.getContent();
		Position pos;
		Position end;
		//Check each line
		for (int i = 0; lines.size()>i; i++) {
			//Parse each template
			for(int p = (lines.get(i)).indexOf("{{"); p != -1; p=(lines.get(i)).indexOf("{{", p+1)) {
				pos = new Position(i, p);
				//Check that the text is parsed as MediaWiki.
				if (isPositionParsedAsMediawiki(page, pos)) {
					//Check that the template is not a parameter.
					if (lines.get(i).substring(p, p+3).equals("{{{")) {
						p += 3;
					} else {
						//Find and parse that template
						end = findClosingPosition(page, "{{", "}}", new Position(i, lines.get(i).indexOf("{{", p)));
						if (end != null) {
							if (end.getLine() == i) {
								//We have a single line template.
								temp = parseTemplate(page, new ArrayList<String>(Arrays.asList(lines.get(i))), p, end.getPosInLine(), pos);
							} else {
								//We have a multi-line template.
								temp = parseTemplate(page, new ArrayList<String>(lines.subList(i, end.getLine()+1)), p, end.getPosInLine(), pos);
							}
							if (temp != null) {
								page.addTemplate(temp);
							}
						}
					}
				}
			}
		}
	}
	
	static private Template parseTemplate(Page page, ArrayList<String> text, int buffer, int topBuffer, Position pos) {
		//Parse multiple lines for a single template.

		Template temp;
		String title;
		if (text.get(0).indexOf("|", buffer) != -1 && ((text.get(0).indexOf("|", buffer) < text.get(0).indexOf("}}", buffer)) || text.size() != 1)) {
			title = text.get(0).substring(text.get(0).indexOf("{{", buffer) + 2, text.get(0).indexOf("|", buffer));
			temp = new Template(pos, title);
		} else {
			if (text.get(0).indexOf("}}", buffer) != -1) {
				title = text.get(0).substring(text.get(0).indexOf("{{", buffer) + 2, text.get(0).indexOf("}}", buffer));
			} else {
				title = text.get(0).substring(text.get(0).indexOf("{{", buffer) + 2);
			}
			if (MagicWords.contains(title)) {
				return null;
			} else {
				temp = new Template(pos, title);
			}
		}
		parseTemplateTextForLinks(page, temp, text, buffer, pos);
		parseTextForParameters(page, temp, null, text, buffer+2, topBuffer-1, pos, true);
		return temp;
	}
	
	static private void parseTextForParameters(Page page, Template templ, Image img, ArrayList<String> text, int buffer, int topBuffer, Position pos, boolean TemplNotImg) {
		//We find parameters in templates.
		String line;
		int j = -1;//cursor for |
		int k = buffer;
		int q;
		String param = "";
		//i is the line offset from pos
		for (int i = 0; i < text.size(); i++) {
			//This for loop goes through a line at a time.
			line = text.get(i);
			if (i == 0) {
				j = line.indexOf("|", buffer);
			} else {
				j = line.indexOf("|");
			}
			for (int m = 0; j != -1 && !(i+1 == text.size() && j > topBuffer); m++) {
				if (m != 0) {
					k = j;
					j = line.indexOf("|", j+1);
				}
				
				//To ensure links don't mess up parameter parsing.
				q = line.indexOf("[[", k);
				while (q < j && q != -1) {
					if (q < j && q != -1) {
						q = findClosingPosition(page, "[[", "]]", new Position(pos.getLine() + i, q)).getPosInLine();
						j = line.indexOf("|", q);
					}
					q = line.indexOf("[[", q);
				}
				

				if (k != -1) {
					if (j == -1) {
						if (i + 1 == text.size()) {
							param = line.substring(k+1, topBuffer);
						} else {
							param = line.substring(k+1, line.length());
						}
					} else if (j != -1 ) {
						if (i + 1 == text.size() && j > topBuffer) {
							param = line.substring(k+1, topBuffer);
						} else {
							param = line.substring(k+1, j);
						}
					}
				}
				
				//Check that text is parsed as Mediawiki;
				if (!isPositionParsedAsMediawiki(page, new Position(pos.getLine() + i, k)) && m != 0) {
					continue;
				}
				
				if (m != 0) {
					if (TemplNotImg) {
						templ.addParameter(param);
					} else {
						img.addParameter(param);
					}
				}
			}
			j = -1;
			k = -1;
		}
	}
	
	static private void  parseTemplateTextForLinks(Page page, Template temp, ArrayList<String> lines, int buffer, Position pos) {
		//Parse multiple lines for links.
		for (int i = 0; i < lines.size(); i++) {
			if (i+1 < lines.size()) {
				parseLineForLinksImagesCategories(page, temp, null, lines.get(i), buffer, Integer.MAX_VALUE, new Position(pos.getLine() + i, pos.getPosInLine()), 1);
			} else {
				parseLineForLinksImagesCategories(page, temp, null, lines.get(i), buffer, (lines.get(i).indexOf("}}")), new Position(pos.getLine() + i, pos.getPosInLine()), 1);
			}
		}
	}
	
	static private void parseLineForLinksImagesCategories(Page page, Template templ, Image img, String line, int buffer, int topBuffer, Position pos, int inputDataType) {
		/*
		 * Variable Input Data Type:
		 * 0-Page (true)
		 * 1-Template (false)
		 * 2-Image
		 */

		//Parse a single line for stuff

		//Check that the text is parsed as MediaWiki.
		
		int i = line.indexOf("[[", buffer);
		int j = -1;//Tracks item closing position.
		int k = line.indexOf("[", buffer);
		String text;
		while ((i != -1 || k != -1) && (i<topBuffer || k<topBuffer)) {
			if (i <= k && i != -1) {
				//We have a Wikilink, image, or category.
				j = line.indexOf("]]", i);
				if (i != -1 && j == -1) {
					logError("Unclosed or multi-line link/image/category detected at " + new Position(pos.getLine(), i) + ". Page title: " + page.getTitle());
					return;
				}
				
				if (line.indexOf("||", i) != -1 && line.indexOf("||", i) < j) {
					logError("Double pipes detected in link/image/category at " + new Position(pos.getLine(), i) + ". Page title: " + page.getTitle());
				}
				if (line.indexOf("|", i) != -1 && line.indexOf("|", i) < j) {
					text = line.substring(i+2, line.indexOf("|", i));	
				} else {
					text = line.substring(i+2, j);
				}
				if ((text.length() > 9 && text.substring(0,9).equals("Category:"))) {
					//We have a category!
					//Check that text is parsed as Mediawiki;
					if (isPositionParsedAsMediawiki(page, new Position(pos.getLine(), i))) {
						page.addCategory(text.substring(9));
					}
					

				} else if ((text.length() > 5 && text.substring(0,5).equals("File:"))) {
					//We have an image!
					//Check that text is parsed as Mediawiki;
					if (isPositionParsedAsMediawiki(page, new Position(pos.getLine(), i))) {
						k = i;
						i = line.indexOf("[[", i+1);
						j = findClosingPosition(page, "[[", "]]", new Position(pos.getLine(), k)).getPosInLine()-1;
						if (i > j || i == -1) {
							i = k;
						} else {
							i = j;
						}
						Image image = parseImage(page, line, text, i, new Position(pos.getLine(), i), j, inputDataType == 0);
						if (image == null) {
							logError("Image Error at: " + pos + " Page title: " + page.getTitle());
						} else {
							if (inputDataType == 0) {
								page.addImage(image);
							} else {
								templ.addImage(image);
							}
						}
					}
				} else {
					//We have a link!
					//Check that text is parsed as Mediawiki;
					if (isPositionParsedAsMediawiki(page, new Position(pos.getLine(), i))) {
						Link link = parseLink(page, line, text, i, pos, inputDataType == 0);
						if (link != null) {
							if (inputDataType == 0) {
								page.addLink(link);
							} else {
								if (inputDataType == 1) {
									templ.addLink(link);
								} else {
									img.addLink(link);
								}
							}
						}
					}
				}
				//Iteration!
				k = i;	
				i = line.indexOf("[[", k+1);
				k = line.indexOf("[", k+1);
			} else {
				//We might have an external link. Must check.
				
				//Check that text is parsed as Mediawiki;
				if (isPositionParsedAsMediawiki(page, new Position(pos.getLine(), k))) {
					Link link = parseExternalLink(page, line, k, j, pos);
					if (link != null) {
						if (inputDataType == 0) {
							page.addLink(link);
						} else if (inputDataType == 1) {
							templ.addLink(link);
						} else {
							img.addLink(link);
						}
					}
				}
				
				//Iteration!
				if (i != -1) {
					i = line.indexOf("[[", k+1);
				}
				k = line.indexOf("[", k+1);
			}
		}
	}
	
	static private Link parseLink(Page page, String line, String text, int i, Position pos, boolean PageNotTemp) {
		//We know we don't have a file or a category. Now to check if have an interwiki link.
		String linkText = null;
		boolean temp = true;
		String temp2;
		for (int l = 0; l < Interwiki.size(); l++) {
			temp2 = Interwiki.get(l);
			if (text.length() > temp2.length() && text.substring(0,temp2.length()).equalsIgnoreCase(temp2)) {
				//We weed out interwiki links, checking against interwiki prefixes one at a time.
				temp = false;
				String interwikiText = text.substring(temp2.length());
				interwikiText = interwikiText.trim();
				page.addInterwiki(new Interwiki(interwikiText, temp2));
			}
		}
		if (temp) {

			if (text.contains("]") || text.contains("[")) {
				//This is not a link, but some page text.
				return null;
			}
			if ( text.substring(0,1).equals("/") || text.substring(0,1).equals("#")) {
				//This link is headed to a subpage/subsection and the destination must reflect that.
				linkText = text;
				text = page.getTitle() + text;
			} else if (text.substring(0,1).equals(":")) {
				//Category and or file link.
				text = text.substring(1);
			}
			if (line.indexOf("|", i) < line.indexOf("]]", i) && line.indexOf("|", i) != -1) {
				//Parse for link text, the text a user actually sees.
				//Account for [[Scratch Wiki talk:Community Portal|]] = Community Portal
				linkText = line.substring(line.indexOf("|", i)+1, line.indexOf("]]", i));
				if (linkText.equals("")) {
					if (text.indexOf(":") == -1) {
						logError("Link with no displayed text detected at " + new Position(pos.getLine(), i) + ". Page title: " + page.getTitle());
					} else {
						linkText = text.substring(text.indexOf(":"));
					}
				}
			}
			Link link_;
			if (PageNotTemp) {
				if (linkText == null) {
					link_ = new Link(new Position(pos.getLine(), i), text);
				} else {
					link_ = new Link(new Position(pos.getLine(), i), text, linkText);
				}
				if (page.templatesContainLink(link_)) {
					return null;
				}
			} else {
				if (linkText == null) {
					link_ = new Link(new Position(pos.getLine(), i), text);
				} else {
					link_ = new Link(new Position(pos.getLine(), i), text, linkText);
				}
			}
			return link_;
		}
		return null;
	}
	
	static private Link parseExternalLink(Page page, String line, int k, int j, Position pos) {
		//We have an external link!

		if (line.length() > 2 && !line.substring(0,2).equals("  ")) {
			String text;
			j = line.indexOf("]", k);
			if (j == -1) {
				//No external link here.
				return null;
			}
			if (line.indexOf("|", k) != -1 && line.indexOf("|", k) <= j) {
				text = line.substring(k+1, line.indexOf("|", k));
			} else {
				text = line.substring(k+1, j);
			}
			

			if ((text.length() > 7 && text.substring(0, 7).equals("http://")) || (text.length() > 2 && text.substring(0, 2).equals("//"))) {
				
				Link link_;
				if (text.contains(" ")) {
					int index = text.indexOf(" ");
					link_ = new Link(new Position(pos.getLine(), k), text.substring(0, index), text.substring(index+1));
				} else {
					link_ = new Link(new Position(pos.getLine(), k), text);
				}
				if (!page.templatesContainLink(link_)) {
					return link_;
				}
			}
		}
		return null;
	}
	
	static private Image parseImage(Page page, String line, String text, int i, Position pos, int topBuffer, boolean pageNotTemp) {
		//Position, name, parameters, links.
		Image image = new Image(pos, text);
		parseTextForParameters(page, null, image, new ArrayList<String>(Arrays.asList(line.substring(pos.getPosInLine()+1, topBuffer+1))), 0, topBuffer-1-pos.getPosInLine(), pos, false);
		parseLineForLinksImagesCategories(page, null, image, line, pos.getPosInLine()+1, topBuffer+1, pos, 2);
		return image;
	}
	
	static private void parsePageForLinks(Page page) {
		//Position, Link, Link Text
		ArrayList<String> content = page.getContent();
		for (int i = 0; i < content.size(); i++) {
			parseLineForLinksImagesCategories(page, null, null, content.get(i), 0, Integer.MAX_VALUE, new Position(i, 0), 0);
		}
	}
	
	static protected Position findClosingPosition(Page page, String open, String close, Position start) {
		//Method for finding where [[ ]] and {{ }} end.
		int lineNum = start.getLine();
		int i = start.getPosInLine();
		int j;
		int k = 0;//Keeps track of last found closing.
		int depth = 1;

		String line = page.getContentLine(lineNum);
		k = i;
		i = line.indexOf(open, i+open.length());
		j = line.indexOf(close, k+open.length());
		if (i > j || (i == -1 && j != -1)) {
			//This object has depth 1.
			depth = 0;
			k = j;
		} else {
			do {
				//Looking one line at a time.
				do {
					//Checking individual line.
					if (i<=j && i != -1) {
						//Depth increased
						k = i;
						i = line.indexOf(open, i+open.length());
						j = line.indexOf(close, k+open.length());
						depth++;
					} else if (j != -1) {
						//Depth decreased
						k = j;
						if (i != -1) {
							i = line.indexOf(open, j+close.length());
						}
						j = line.indexOf(close, j+close.length());
						depth--;
					}
				} while (depth != 0 && j != -1);
				
				if (depth != 0) {
					//Load in next line, and set up i and j
					lineNum++;
					if (lineNum < page.getLineCount()) {
						line = page.getContentLine(lineNum);
					}
					i = line.indexOf(open, 0);
					j = line.indexOf(close, 0);
					k = 0;
				}
			} while(depth>0 && lineNum < page.getLineCount());
		}
		if (lineNum > page.getLineCount()) {
			logError("Unclosed parseable item at: " + start + "  Page title: " + page.getTitle());
			return null;
		}
		return new Position(lineNum, k+1);
	}
	
	static private void parsePageForSections(Page page) {
		//Position, title, depth.
		ArrayList<String> content = page.getContent();
		String line;
		boolean found;
		int index;
		int index2;
		Position pos;
		int j;
		for (int i = 0; i < content.size(); i++) {
			line = content.get(i);
			found = true;
			for (j = 1; found; j++) {
				found = false;
				index = line.indexOf(new String(new char[j]).replace("\0", "="));
				if (index == 0) {
					index2 = line.indexOf(new String(new char[j]).replace("\0", "="), index+1);
					if (index2 != -1) {
						found = true;
					}
				}
			}
			j -= 2;
			if (j != 0) {
				index = line.indexOf(new String(new char[j]).replace("\0", "="));
				index2 = line.indexOf(new String(new char[j]).replace("\0", "="), index+1);
				pos = new Position(i, 0);
				page.addSection(new Section(line.substring(index+j, index2), pos, j));
			}
		}
	}
	
	static private void getPastReveisions(Page page) {
		//This method fetches the revisions of a page.
		String returned = APIcommand(new QueryPageRevisions(page.getPageLocation(), revisionDepth, getRevisionContent));
		
		//Parse page for info.
		if (getRevisionContent) {
			page.addRevisions(getRevisions(returned, "<rev user=", "</rev>", true, page.getTitle()));
		} else {
			page.addRevisions(getRevisions(returned, "<rev user=", "\" />", false, page.getTitle()));
		}
	}
	
	static public ArrayList<Revision> getPastRevisions(PageLocation loc, int localRevisionDepth, boolean getContent) {
		//This method fetches the revisions of a page.
		String returned = APIcommand(new QueryPageRevisions(loc, Math.min(localRevisionDepth, APIlimit), getContent));
		
		//Parse page for info.
		if (getContent) {
			return getRevisions(returned, "<rev user=", "</rev>", true, loc.getTitle());
		} else {
			return getRevisions(returned, "<rev user=", "\" />", false, loc.getTitle());
		}
	}
	
	/**
	 * This method gets 30 recent changes per query call, so it might make multiple query calls.
	 * @param depth The amount of revisions you want returned.
	 * @return A list of recent changes wrapped in revisions.
	 */
	static public ArrayList<Revision> getRecentChanges(String language, int depth) {
		//This method fetches the recent changes.
		ArrayList<Revision> toReturn = new ArrayList<Revision>();
		String rccontinue = null;//Used to continue queries.
		
		do {
			//Make a query call.
			String returned;
			if (rccontinue == null) {
				returned = APIcommand(new QueryRecentChanges(language, Math.min(30, APIlimit)));
			} else {
				returned = APIcommand(new QueryRecentChanges(language, Math.min(30, APIlimit), rccontinue));
			}
			ArrayList<Revision> returnedRevisions = getRevisions(returned, "<rc type=", "\" />", false, null);
		
			//Make sure we return the correct amount of items.
			int numRevisionsNeeded = depth - toReturn.size();
			if (returnedRevisions.size() > numRevisionsNeeded) {
				toReturn.addAll(returnedRevisions.subList(0, numRevisionsNeeded));
			} else {
				toReturn.addAll(returnedRevisions);
			}
			
			//Try continuing the query.
			try {
				rccontinue = parseXMLforInfo("rccontinue", returned, "\"");
			} catch (IndexOutOfBoundsException e) {
				rccontinue = null;
			}
		} while (rccontinue != null && toReturn.size() != depth);
		 
		 return toReturn;
	}
	
	/*
	 * This is a specialized function and should not be used outside of this class.
	 */
	static private ArrayList<Revision> getRevisions(String XMLdata, String openingText, String closingText, boolean includeContent, String forceTitle) {
		//This method takes XML data and parses it for revisions.
		ArrayList<Revision> output = new ArrayList<Revision>();
		String revision;
		String user;
		String comment;
		String tempDate;
		Date date = null;
		String content;
		String title;
		int j = 0;
		int k = -1;
		while (j != -1) {
			j = XMLdata.indexOf(openingText, k+1);
			k = XMLdata.indexOf(closingText, j+1);
			if (j != -1) {
				//No errors detected.
				revision = XMLdata.substring(j, k+closingText.length());
				user = parseXMLforInfo("user", revision, "\"", 2, 0);
				tempDate = parseXMLforInfo("timestamp", revision, "\"", 2, 0);
				date = createDate(tempDate);
				content = null;
				if (includeContent) {
					comment = parseXMLforInfo("comment", revision, "\" contentformat", 2, 0);
					content = parseXMLforInfo("xml:space=\"preserve\"", revision, "</rev>");
				} else {
					comment = parseXMLforInfo("comment", revision, closingText, 2, 0);
				}
				
				//Generate and store revision
				Revision rev;
				if (forceTitle == null) {
					title = parseXMLforInfo("title", revision, "\"");
					rev = new Revision(new PageLocation(title, getLanguageFromURL(baseURL)), user, comment, date);
				} else {
					rev = new Revision(new PageLocation(forceTitle, getLanguageFromURL(baseURL)), user, comment, date);
				}
				rev.setPageContent(content);
				output.add(rev);
			}
		}
		return output;
	}
	
	/**
	 * This method gets 100 category members per query call, so it might make multiple query calls.
	 * @param loc The page location of the category.
	 * @return Returns an arraylist of all pages in a category.
	 */
	static public ArrayList<PageLocation> getCategoryPages(PageLocation loc) {
		String returned;
		ArrayList<PageLocation> toReturn = new ArrayList<PageLocation>();
		String cmcontinue = null;
		
		do {
			//Make a query call.
			if (cmcontinue == null) {
				returned = APIcommand(new QueryCategoryMembers(loc.getLanguage(), loc.getTitle(), Math.min(100, APIlimit)));
			} else {
				returned = APIcommand(new QueryCategoryMembers(loc.getLanguage(), loc.getTitle(), Math.min(100, APIlimit), cmcontinue));
			}
			System.out.println(returned);

			//Parse page for info.
			ArrayList<String> pageNames = getPages(returned, "<cm pageid=", "/>");
			
			//Transfer page names into wrapper class.
			for (String pageName : pageNames) {
				PageLocation loc2 = new PageLocation(pageName, loc.getLanguage());
				toReturn.add(loc2);
			}	
			
			//Try continuing the query.
			try {
				cmcontinue = parseXMLforInfo("cmcontinue", returned, "\"");
			} catch (IndexOutOfBoundsException e) {
				cmcontinue = null;
			}
		} while (cmcontinue != null);
		
		return toReturn;
	}
	
	static public ArrayList<PageLocation> getCategoryPagesRecursive(PageLocation loc) {
		ArrayList<PageLocation> pageLocs = new ArrayList<PageLocation>();
		ArrayList<PageLocation> toAdd = new ArrayList<PageLocation>();
		
		toAdd = getCategoryPages(loc);
		
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
	 * @param ignore Do not include these categories and pages in the returned result.
	 * @param loc The page location.
	 */
	static public ArrayList<PageLocation> getCategoryPagesRecursive(PageLocation loc, ArrayList<String> ignore) {
		ArrayList<PageLocation> pageLocs = new ArrayList<PageLocation>();
		ArrayList<PageLocation> toAdd = new ArrayList<PageLocation>();
		
		toAdd = getCategoryPages(loc);
		
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
	
	/*
	 * This method gets all pages that links to loc.
	 */
	static public ArrayList<PageLocation> getPagesThatLinkTo(PageLocation loc) {
		return getPagesThatLinkTo(loc, Integer.MAX_VALUE);
	}
	
	/**
	 * This method gets 30 recent changes per query call, so it might make multiple query calls.
	 * @param loc The page location to get back links for.
	 * @param depth The amount of pages to get.
	 * @return A list of page that link to loc.
	 */
	static public ArrayList<PageLocation> getPagesThatLinkTo(PageLocation loc, int depth) {
		//This method gets all the pages that link to another page. Redirects are included.
		ArrayList<PageLocation> toReturn = new ArrayList<PageLocation>();
		String blcontinue = null;
		
		do {
			//Make a query call.
			String returned;
			if (blcontinue == null) {
				returned = APIcommand(new QueryBackLinks(loc, Math.min(30, APIlimit)));
			} else {
				returned = APIcommand(new QueryBackLinks(loc, Math.min(30, APIlimit), blcontinue));
			}
	
			//Parse page for info.
			ArrayList<String> pageTitles = getPages(returned, "<bl pageid=", "/>");
			
			//Transfer page names into wrapper class.
			for (String title : pageTitles) {
				if (toReturn.size() != depth) {
					toReturn.add(new PageLocation(title, loc.getLanguage()));
				} else {
					break;
				}
			}
			
			//Try continuing the query.
			try {
				blcontinue = parseXMLforInfo("blcontinue", returned, "\"");
			} catch (IndexOutOfBoundsException e) {
				blcontinue = null;
			}
		} while (blcontinue != null && toReturn.size() != depth);
		
		return toReturn;
	}
	
	static public ArrayList<PageLocation> getAllPages(String language, int depth) {
		return getAllPages(language, depth, null);
	}
	
	/**
	 * This method gets 30 recent changes per query call, so it might make multiple query calls.
	 * @param language
	 * @param depth
	 * @param from
	 * @return
	 */
	static public ArrayList<PageLocation> getAllPages(String language, int depth, String from) {
		ArrayList<PageLocation> toReturn = new ArrayList<PageLocation>();
		String apcontinue = null;
		
		do {
			//Make query call.
			String returned;
			if (apcontinue == null) {
				if (from != null) {
					returned = APIcommand(new QueryAllPages(language, Math.min(3, APIlimit), from));
				} else {
					returned = APIcommand(new QueryAllPages(language, Math.min(3, APIlimit)));
				}
			} else {
				returned = APIcommand(new QueryAllPages(language, Math.min(3, APIlimit), apcontinue));
			}
			
			//Parse text returned.
			ArrayList<String> pageTitles= getPages(returned, "<p pageid=", "/>");
			
			//Transfer page names into wrapper class.
			for (String title : pageTitles) {
				if (toReturn.size() != depth) {
					toReturn.add(new PageLocation(title, language));
				} else {
					break;
				}
			}
			
			//Try continuing the query.
			try {
				apcontinue = parseXMLforInfo("apcontinue", returned, "\"");
				apcontinue = apcontinue.replace("_", " ");
				System.out.println(apcontinue);
			} catch (IndexOutOfBoundsException e) {
				apcontinue = null;
			}
		} while (apcontinue != null && toReturn.size() != depth);
		
		log.add("All pages queried.");
		
		return toReturn;
	}
	
	static protected ArrayList<String> getPages(String XMLdata, String openingText, String closingText) {
		//This method takes XMLdata and parses it for page names.
		ArrayList<String> output = new ArrayList<String>();
		int j = 0;
		int k = -1;
		String temp;
		//Parse page for info.
		do {
			j = XMLdata.indexOf(openingText, k+1);
			k = XMLdata.indexOf(closingText, j+1);
			if (j != -1) {
				//No errors detected.
				temp = XMLdata.substring(j, k+6);
				output.add(parseXMLforInfo("title", temp, "\""));
			}
		} while(j != -1);
		return output;
	}
	
	static protected ArrayList<String> getXMLItems(String XMLdata, String openingText, String closingText, int botBuffer) {
		//This method takes XMLdata and parses it for page names.
		ArrayList<String> output = new ArrayList<String>();
		int j = 0;
		int k = -1;

		//Parse page for info.
		do {
			j = XMLdata.indexOf(openingText, k+1);
			k = XMLdata.indexOf(closingText, j+1);
			if (j != -1) {
				//No errors detected.
				output.add(XMLdata.substring(j+botBuffer, k));
			}
		} while(j != -1);
		return output;
	}
	
	/**
	 * @param ur The url you want to get.
	 * @param unescapeText Unescapes string literals. Ex: \n, \s
	 * @param unescapeHTML4 Unescapes HTML4 text, including unicode. Stronger than unescapeText. Ex: \u0065
	 */
	static protected String[] getURL(String ur, boolean unescapeText, boolean unescapeHTML4) throws IOException {
		log("Loading: " + ur);
		
		//This method actual fetches a web page, and turns it into a more easily use-able format.
        URL oracle = null;
		try {
			oracle = new URL(ur);
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
		}

		BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(oracle.openStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
        	logError("Connection cannot be opened.");
        	return null;
        }
        
        ArrayList<String> page = new ArrayList<String>();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
        	if (unescapeText) {
        		inputLine = unescapeJava(inputLine);
        	}
        	if (unescapeHTML4) {
        		inputLine = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(inputLine));
        	}
        	
            page.add(inputLine);
        }
        in.close();
        
        return page.toArray(new String[page.size()]);
	}
	
	static public boolean isPositionParsedAsMediawiki(Page page, Position pos) {
		//Start from the beginning of the page and work up.
		Position cursor = new Position(0,0);
		int lowest;//Earliest occurrence of MW escaped text.
		Position end = new Position(0,0);//Last found MW escaped text close.
		
		do {
			cursor = end;
			//Find MW escaped text in line.
			lowest = -1;
			String line = page.getContentLine(cursor.getLine());
			int markupTextID = -1;
			for (int i = 0; i < MWEscapeOpenText.size(); i++) {
				int index = line.indexOf(MWEscapeOpenText.get(i), cursor.getPosInLine());
				if (index != -1) {
					if (lowest == -1 || index < lowest) {
						lowest = index;
						markupTextID = i;
					}
				}
			}
			//Move cursor
			if (lowest == -1) {
				cursor.increaseLine(1);
				cursor.setPosInLine(0);
			} else {
				cursor.setPosInLine(lowest);

				end = findClosingPosition(page, MWEscapeOpenText.get(markupTextID), MWEscapeCloseText.get(markupTextID), cursor);
			}
		} while (pos.isGreaterThen(cursor) && pos.isGreaterThen(end));
		
		if (lowest == -1) {
			//No escaped text on line.
			return true;
		} else {
			//Escaped text on line.
			return !(pos.isGreaterThen(cursor) && end.isGreaterThen(pos));
		}
	}
	
	/**
	 * @param loc The pageLocation of the file.
	 * @return A String of the url that goes directly to the image file (and nothing else).
	 */
	static String getDirectImageURL(PageLocation loc) {
		log("Getting image direct url: " + loc.getTitle());
		
		String xmlString = APIcommand(new QueryImageURL(loc));
		
		if (xmlString.contains("\"missing\":\"\"")) {
			logError("File does not exist.");
			return null;
		}
		
		return parseXMLforInfo("url\"", xmlString, "\",");
	}
	
	static public boolean logIn(String username, String password, String language) {
        HttpEntity entity = null;
        List<Cookie> cookies = null;
        
        baseURL = getWikiURL(language);
        
        try {
        	logCookies(cookies);
        } catch (NullPointerException e) {
        	log("None");
        }

        //LOG IN
        String token = null;
        String xmlString = "";
        for (int j = 0; j < 2; j++) {
        	if (token == null) {
        		entity = getPOST(baseURL + "/api.php?action=login&format=xml", new String[]{"lgname", "lgpassword"}, new String[]{username, password});
        	} else {
        		entity = getPOST(baseURL + "/api.php?action=login&format=xml", new String[]{"lgname", "lgpassword", "lgtoken"}, new String[]{username, password, token});
        	}
	
	        cookies = context.getCookieStore().getCookies();
	        logCookies(cookies);
	        
	        try {
				xmlString = EntityUtils.toString(entity);
				System.out.println(xmlString);
				if (j == 0) {
					token = parseXMLforInfo("token", xmlString, "\"");
					log(token);
				}
			} catch (org.apache.http.ParseException | IOException e) {
				e.printStackTrace();
			}
        }
        
        boolean success = xmlString.contains("Success");
		log("Login status at " + language + ": " + success);
        
		if (success) {
			loggedInAtLanguages.add(language);
		}
		
        return success;
	}
	
	static public String APIcommand(APIcommand command) {
		baseURL = getWikiURL(command.getPageLocation().getLanguage());
		
		String textReturned;
		if (command.requiresGET()) {
			textReturned = APIcommandGET(command);
		} else {
			textReturned = APIcommandHTTP(command);
		}
		
		if (textReturned != null) {
			//Do a quick look-over of the xml recieved
			if (textReturned.contains("<!DOCTYPE HTML>") || textReturned.contains("<!DOCTYPE html>")) {
				//We are handling HTML output
				//Unescape html
				textReturned = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(textReturned));
				
				//Error handling
				if (textReturned.contains("This is an auto-generated MediaWiki API documentation page")) {
					System.out.println(textReturned.substring(0,textReturned.indexOf("</a>")+4));
					log("MediawikiAPI documentation page returned.");
					
					String error = parseXMLforInfo("error code", textReturned, "\"");
					if (textReturned.contains("info=")) {
						error += ":" + parseXMLforInfo("info", textReturned, "\"");
					}
					logError(error);
					throw new Error(error);
				} else {
					if (textReturned.length() < 1000) {
						System.out.println("HTML: " + textReturned);
					} else {
						System.out.println("HTML: " + textReturned.substring(0, 1000));	
					}
					//log("HTML: " + textReturned);
					
					if (textReturned.contains("<warnings>")) {
						logError("Warnings recieved when editing " + command.getTitle() + ".");
					} else if (textReturned.contains("\"warnings\"")) {
						String xmlSnippet = parseXMLforInfo("\"warnings\"", textReturned, "},");
						String error = "";
						for (int i = 0, prevI = 0; i != -1; prevI = i, i = xmlSnippet.indexOf("\n", i+1)) {
							if (prevI != 0) {
								String temp = xmlSnippet.substring(prevI, i);
								temp = temp.replace("\n", "");
								temp = temp.trim();
								error += temp + " | ";
							}
						}
						logError(error);
					} else {
				        log(command.getTitle() + " has been edited.");
					}
				}
			} else if (textReturned.contains("<?xml version")) {
				//We are handling XML output
			} else {
				//We are handling JSON output
				
				//Error handling
				if (textReturned.contains("This is an auto-generated MediaWiki API documentation page")) {
					System.out.println(textReturned.substring(0,textReturned.indexOf("/API")+4));
					log("MediawikiAPI documentation page returned.");
					
					String error = "||" + parseXMLforInfo("code", textReturned, "\",", 3, 0);
					if (textReturned.contains("info")) {
						error += ":" + parseXMLforInfo("info", textReturned, "\",", 3, 0);
					}
					logError(error);
					throw new Error(error);
				} else {
					if (textReturned.length() < 1000) {
						System.out.println("JSON: " + textReturned);
					} else {
						System.out.println("JSON: " + textReturned.substring(0, 1000));	
					}
					//log("JSON: " + textReturned);
					
					if (textReturned.contains("\"error\"")) {
						logError(parseXMLforInfo("\"info\"", textReturned, "\","));
					} else if (textReturned.contains("Internal Server Error")){
						logError("Internal Server Error");
						log(textReturned);
						throw new Error("Internal Server Error");
					} else {
				        log(command.getPageLocation().getTitle() + " has been edited.");
					}
				}
			}
		}
		
		return textReturned;
	}
	
	/**
	 * Automatically unescapes HTML5.
	 */
	static private String APIcommandHTTP(APIcommand command) {
		String url = baseURL + "/api.php?";
		String[] editKeys = command.getKeysArray();
		String[] editValues = command.getValuesArray();
		
		for (int i = 0; i < editKeys.length; i++) {
			url += URLencode(editKeys[i]) + "=" + URLencode(editValues[i]);
			if (i != editKeys.length-1) {
				url += "&";
			}
		}
		
		try {
			return compactArray(getURL(url, false, true), "\n");
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	static private String APIcommandGET(APIcommand command) {
		HttpEntity entity;
		
		String token = getEditToken();
		
		String[] editKeys;
		String[] editValues;
		editKeys = command.getKeysArray();
		editValues = command.getValuesArray();
		
		//Get command parameters
		String[] keys = new String[editKeys.length + 1];
		String[] values = new String[editValues.length + 1];
		
		//Shift parameters to larger array
		for (int i = 0; i < editKeys.length; i++) {
			keys[i] = editKeys[i];
			values[i] = editValues[i];
		}
		
		//Add a few more parameters
		keys[keys.length-1] = "token";
		values[keys.length-1] = "" + token;

		//Send the command!
        entity = getPOST(baseURL + "/api.php?", keys, values);
        try {
			String xmlString = EntityUtils.toString(entity);
			
			return xmlString;
		} catch (org.apache.http.ParseException | IOException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	/*
	 * A method for creating a Web POST request.
	 */
	static private HttpEntity getPOST(String url, String[] name, String[] value) {
        HttpResponse response = null;
		
        HttpPost httpost = new HttpPost(url);
    	
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (int i = 0; i < name.length; i++) {
        	nvps.add(new BasicNameValuePair(name[i], value[i]));
        }

        httpost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        try {
			response = httpclient.execute(httpost, context);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return response.getEntity();
	}
	
	static public void logCookies(List<Cookie> cookies) {
		log("List of cookies: ");
		if (cookies.isEmpty()) {
			log("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
            	log("- " + cookies.get(i).toString());
            }
        }
	}
	
	static private String getEditToken() {
		String[] keys = new String[]{"action", "type", "format"};
		String[] values = null;
		values = new String[]{"tokens", "edit", "xml"};
        
        HttpEntity entity = getPOST(baseURL + "/api.php?", keys, values);
        
		String xmlString = "";
		String token = "";
		try {
			xmlString = EntityUtils.toString(entity);
			token = parseXMLforInfo("edittoken", xmlString, "\"");
		} catch (org.apache.http.ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return token;
	}
	
	static protected String parseXMLforInfo(String info, String XMLcode, String ending) {
		//This method aids in XML parsing.
		int i = XMLcode.indexOf(info);
		if (i == -1) {
			throw new IndexOutOfBoundsException();
		}
		i += info.length() + 2;
		int j = XMLcode.indexOf(ending, i+1);
		if (j != -1) {
			return XMLcode.substring(i,  j);
		} else {
			return "";
		}
	}
	
	static protected String parseXMLforInfo (String info, String XMLcode, String ending, int bufferBot, int bufferTop) {
		//This method aids in XML parsing.
		int i = 0;
		i = XMLcode.indexOf(info);
		i += info.length() + bufferBot;
		return XMLcode.substring(i, XMLcode.indexOf(ending, i) - bufferTop);
	}
	
	public ArrayList<String> readFileAsList(String location, int commentBufferLineCount, boolean comments, boolean ignoreBlankLines) {
		try {
			// Read in the file!
			InputStream in = getClass().getResourceAsStream(location);
			BufferedReader br = new BufferedReader(
						new InputStreamReader(in)
					);
			
			// Ignore the comment
			for (int i = 0; i < commentBufferLineCount; i++) {
				br.readLine();
			}
			
			// Gather array size
			ArrayList<String> lines = new ArrayList<String>();
			
			// Parse file array into java int array
			String line;
			line = br.readLine();
			do {
				if (comments && (line.length() > 0 && line.substring(0,1).equals("#"))) {
					//We have a comment. Ignore it.
				} else if (ignoreBlankLines && line.length() == 0) {
					//We have an empty line.
				} else {
					lines.add(line);
				}
				line = br.readLine();
			} while (line != null);
			
			in.close();
			br.close();
			
			return lines;
			
		} catch (IOException e) {
			System.out.println("Error reading in list.");
		}
		return null;
	}
	
	static public String compactArray(String[] array) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (String item: array) {
			output+=item;
		}
		
		return output;
	}
	
	static public String compactArray(String[] array, String delimitor) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (int i = 0; i < array.length; i++) {
			output+= array[i];
			if (i != array.length-1) {
				output += delimitor;
			}
		}
		
		return output;
	}
	
	public static String getWikiURL(String wikiPrefix) {
		wikiPrefix = wikiPrefix.replace(":", "");

		for (int i = 0; i < Interwiki.size(); i++) {
			if (Interwiki.get(i).equalsIgnoreCase(wikiPrefix) || Interwiki.get(i).equalsIgnoreCase(wikiPrefix + ":")) {
				return InterwikiURL.get(i);
			}
		}
		throw new Error();
	}
	
	public static String getLanguageFromURL(String url) {
		for (int i = 0; i < InterwikiURL.size(); i++) {
			if (InterwikiURL.get(i).equalsIgnoreCase(url)) {
				return Interwiki.get(i);
			}
		}
		throw new Error();
	}
	
	static public Date createDate(String text) {
		//This takes a wiki date and converts it into a java date.
		Date date = null;
		try {
			date = dateFormat.parse(text);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return date;
	}
	
	static public String URLencode(String url) {
		url = url.replace(" ", "_");
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	static public void log(String line) {
		log.add(line);
	}
	
	static public void logError(String line) {
		numErrors++;
		log.add("ERROR: " + line);
	}
	
	static public ArrayList<String> getLog() {
		return log;
	}
	
	static public void printLog() {
		System.out.println("Log:");
		for (int i = 0; i < log.size(); i++) {
			System.out.println(log.get(i));
		}
	}
	
	static public String concatLog() {
		String temp = "Log:\n";
		for (int i = 0; i < log.size(); i++) {
			temp += log.get(i) + "\n";
		}
		return temp;
	}
	
	static public void setPPD(boolean boon) {
		printPageDownloads = boon;
	}
	
	static public void pageDownloaded(String name) {
		log(baseURL + " // " + name + " is downloaded.");
	}
	
	static public void pageDownloaded(String name, String name2) {
		log(baseURL + " // " + name + " through " + name2 + " is downloaded.");
	}
}
