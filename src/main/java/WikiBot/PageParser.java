package WikiBot;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import Content.Category;
import Content.ExternalLink;
import Content.Image;
import Content.Interwiki;
import Content.Link;
import Content.Page;
import Content.PageLocation;
import Content.PageObject;
import Content.PageObjectAdvanced;
import Content.Revision;
import Content.Section;
import Content.SimplePage;
import Content.Template;
import Errors.ParsingError;
import Mediawiki.MediawikiDataManager;

public class PageParser {
	
	// For data passing purposes only.
	private class PageParseData {
		public ArrayList<PageObjectAdvanced> pageObjects;
		public ArrayList<Category> categories;
		public ArrayList<Interwiki> interwikis;
		
		public PageParseData() {
			pageObjects = new ArrayList<PageObjectAdvanced>();
			categories = new ArrayList<Category>();
			interwikis = new ArrayList<Interwiki>();
		}
		
		public void merge(PageParseData toMerge) {
			categories.addAll(toMerge.categories);
			interwikis.addAll(toMerge.interwikis);
		}
	}
	
	// Class variables
	private static MediawikiBot bot;
	private static MediawikiDataManager mdm;
	
	// Configuration variables
	private boolean resolveDisambiguates = false; // Resolve some parsing disambiguates at the expense of extra API calls.
	
	public PageParser(MediawikiBot bot_, boolean resolveDisambiguates_) {
		bot = bot_;
		resolveDisambiguates = resolveDisambiguates_;
		
		mdm = bot.getMDM();
	}
	
	public PageParser(MediawikiBot bot_) {
		bot = bot_;
		
		mdm = bot.getMDM();
	}
	
	// SimplePage parsing
	public SimplePage parseSimplePage(JsonNode code, String language) {
		String title = code.get("title").asText();
		int pageID = code.get("pageid").asInt();
		
		// Initialize the SimplePage object with this info.
		SimplePage newPage = new SimplePage(language, title, pageID);
		
		String rawText = code.findValue("*").asText();
		newPage.setRawText(rawText);
		
		return newPage;
	}
	
	// Experimental Page parsing 
	public Page parsePage(JsonNode code, String language) {
		String title = code.get("title").asText();
		int pageID = code.get("pageid").asInt();
		
		// Initialize the SimplePage object with this info.
		String rawText = code.findValue("*").asText();
		
		ArrayList<Integer> linePositions = parsePageForNewLines(rawText);
		PageParseData pData = parseTextForPageObjects(rawText, new PageLocation(language, title), 0, 0);
		ArrayList<Section> sections = parsePageForSections(rawText);
		
		Page page = new Page(linePositions, sections,
				pData.pageObjects, pData.categories,
				pData.interwikis,
				language, title, pageID);
		
		page.setRawText(rawText);
		
		return page;
	}
	
	private ArrayList<Integer> parsePageForNewLines(String rawText) {
		ArrayList<Integer> linePositions = new ArrayList<Integer>();
		
		MediawikiDataManager mdm = bot.getMDM();
		
		//Only these escape texts impact new lines.
		ArrayList<String> escapeOpenText = mdm.getHTMLCommentOpenText();
		ArrayList<String> escapeCloseText = mdm.getHTMLCommentCloseText();

		for (int i = 0; i != -1; i = rawText.indexOf('\n', i+1)) {
			if (isPositionParsedAsMediawiki(rawText, i, escapeOpenText, escapeCloseText)) {
				linePositions.add(i);
			}
		}
		
		return linePositions;
	}
	
	private PageParseData parseTextForPageObjects(String rawText, PageLocation loc, int pos, int depth) {
		PageParseData pData = new PageParseData();
		
		int lastOpenIndex;
		int openIndex = -1;
		int innerCloseIndex = -1;
		int objectID = -1;
	
		String[] openStrings = new String[]{"{{", "[[", "["};
		String[] closeStrings = new String[]{"}}", "]]", "]"};

		do {
			//Find the text opening for a page object.
			lastOpenIndex = openIndex;
			int lowestIndex = -1;
			for (int i = 0; i < openStrings.length; i++) {
				openIndex = rawText.indexOf(openStrings[i], lastOpenIndex);
				if ((openIndex < lowestIndex || lowestIndex == -1) && openIndex != -1) {
					lowestIndex = openIndex;
					objectID = i;
				}
			}
			
			//Test that we might have an object.
			openIndex = lowestIndex;
			if (openIndex != -1 && isPositionParsedAsMediawiki(rawText, openIndex+pos)) {
				//Test that we do have a page object.
				try {
					innerCloseIndex = findClosingPosition(rawText, openStrings[objectID], closeStrings[objectID], openIndex);
				} catch (ParsingError e) {
					//TODO: "Detected possible unclosed Mediawiki object at page: " + getTitle() + " language: " + getLanguage());
				}
				
				if (objectID == 1) {
					//Mediawiki preference. If a link closing can be put one character further, do it.
					if (rawText.length() >= innerCloseIndex + 1 + closeStrings[1].length()) {
						if (rawText.substring(innerCloseIndex + 1, innerCloseIndex + 1 + closeStrings[1].length()).equals(closeStrings[1])) {
							innerCloseIndex += 1;
						}
					}
				}
				
			objectParse:
				if (innerCloseIndex != -1) {
					// We have a page object. Parse!
					PageObjectAdvanced po = null;
					String objectText = rawText.substring(openIndex + openStrings[objectID].length(), innerCloseIndex);
					String header;
					
					int tempIndex = objectText.indexOf("|");
					if (tempIndex != -1) {
						header = objectText.substring(0, tempIndex);
					} else {
						header = objectText;
					}
					
					//Handle some edge cases where the header is incorrectly guessed.
					if (header.equals("[") || header.equals("]")) {
						openIndex += 1;
						break objectParse;
					}
					
					int outerCloseIndex = innerCloseIndex + closeStrings[objectID].length();
					boolean isLink = false;
					if (objectID == 0) {
						//{{
						//Check that we have a valid template.
						for (String ignore : mdm.getTemplateIgnore()) {
							if (header.length() >= ignore.length() && header.substring(0, ignore.length()).equalsIgnoreCase(ignore)) {
								openIndex += ignore.length();
								break objectParse;
							}
						}
						
						//Check that we do not have a {{{ }}} parameter.
						if (header.length() > 0 && header.substring(0,1).equals("{")) {
							openIndex += 2;
							break objectParse;
						}
						
						po = new Template(openIndex+pos, outerCloseIndex+pos, loc.getTitle(), header);
					} else if (objectID == 1) {
						//[[
						if (header.length() == 0) {
							//False positive.
							openIndex = outerCloseIndex;
							break objectParse;
						} else if (header.length() > 6 && (header.substring(0,5).equalsIgnoreCase("File:") || header.substring(0,6).equalsIgnoreCase("Image:"))) {
							//We have an image.
							po = new Image(openIndex+pos, outerCloseIndex+pos, header);
						} else if (header.length() > 9 && header.substring(0,9).equalsIgnoreCase("Category:")) {
							//We have a category.
							pData.categories.add(new Category(objectText, openIndex+pos, outerCloseIndex+pos));
							
							openIndex = outerCloseIndex;
							break objectParse; // Done.
						} else {
							//Check for an interwiki.
							for (String iw: mdm.getWikiPrefixes()) {
								if (header.length() >= iw.length() && header.substring(0, iw.length()).equals(iw)) {
									pData.interwikis.add(new Interwiki(iw, header.substring(iw.length()+1).trim(), openIndex+pos, outerCloseIndex+pos));
									
									openIndex = outerCloseIndex;
									break objectParse;
								}
							}
							
							//Check that this is a valid link.
							//Extra [ means the link is slightly further down.
							//Odd multiples of [ do not create a link.
							if (header.substring(0,1).equals("[")) {
								openIndex += openStrings[objectID].length(); // Advance the cursor.
								break objectParse;
							}
							
							isLink = true;
							po = new Link(openIndex+pos, outerCloseIndex+pos, loc.getTitle(), header);
						}
					} else {
						//[
						//Is this an external link?
						if ((header.length() > 7 && header.substring(0, 7).equals("http://")) || (header.length() > 2 && header.substring(0, 2).equals("//"))) {
							isLink = true;
							po = new ExternalLink(openIndex+pos, outerCloseIndex+pos, header);
						} else {
							openIndex += 1;
							break objectParse; // Not an object.
						}
					}
					
					//Page objects within the current page object.
					PageParseData pData2 = parseTextForPageObjects(objectText, loc, openIndex + pos + openStrings[objectID].length(), depth+1);
					pData.merge(pData2);
					ArrayList<PageObjectAdvanced> objects = pData2.pageObjects;
					
					//Resolve link/template disambiguates
					//Templates encased by [[ ]] are disambiguates in that they could turn into a link, or stay a template.
					//It all depends on if the template is multi-lined, hence breaking the link.
					if (isLink && resolveDisambiguates) {
						for (PageObject object: objects) {
							if (object.getObjectType().equalsIgnoreCase("Template")) {
								
								// Check if the template is muli-lined.
								SimplePage sp = bot.getWikiSimplePage(new PageLocation(loc.getLanguage(), ((Template)object).getTemplateName()));
								if (sp.getRawText().contains("\n") && depth == 0) {
									//It is!
									pData.pageObjects.addAll(pData2.pageObjects);
									
									openIndex = outerCloseIndex;
									break objectParse;
								}
							}
						}
					}
					
					//If a link contains another link, the outer link isn't really a link...
					if (isLink) {
						for (PageObjectAdvanced o : objects) {
							if (o.getObjectType().equalsIgnoreCase("link")) {
								openIndex += 2; // Advanced the cursor.
								break objectParse;
							}
						}
						
						// Check if the link can start further.
						if (objectText.contains(openStrings[1])) {
							openIndex += 2;
							break objectParse;
						}
					}
					
					//Parse for parameters.
					ArrayList<Integer> parameterLocations= new ArrayList<Integer>();
					while (tempIndex != -1) {
						if (isPositionParsedAsMediawiki(rawText, tempIndex+pos)) {
							parameterLocations.add(tempIndex);
						}
						tempIndex = objectText.indexOf("|", tempIndex+1);
					}
					
					//Weed out extra | locations that child page objects use.
					for (PageObject object : objects) {
						ArrayList<Integer> toRemove = new ArrayList<Integer>();
						for (int i = 0; i < parameterLocations.size(); i++) {
							int paramLoc = parameterLocations.get(i);//The global location of a child parameter.
							int deliminatorLoc = paramLoc+openIndex+pos+po.getOpeningString().length();//The global location of the |
							if ( deliminatorLoc > object.getOuterOpeningPosition() && deliminatorLoc < object.getOuterClosingPosition()) {
								//This parameter is used by a child page object.
								toRemove.add(paramLoc);
							} else if (paramLoc > object.getOuterClosingPosition()) {
								//Break because any further | to be checked are guaranteed to be higher then the child page object.
								break;
							}
						}
						
						//Remove bad parameter locations.
						parameterLocations.removeAll(toRemove);
					}
					
					//Add parameters to our current PageObject.
					for (int i = 0; i < parameterLocations.size(); i++) {
						int paramLoc = parameterLocations.get(i)+1;
						int paramEndLoc;
						if (i == parameterLocations.size()-1) {
							paramEndLoc = objectText.length();
						} else {
							paramEndLoc = parameterLocations.get(i+1);
						}
						
						po.addParameter(objectText.substring(paramLoc, paramEndLoc));
					}
					
					// Wrap up and save work.
					po.addPageObjects(objects);
					
					pData.pageObjects.add(po);
					
					//Done. Continue iteration.
					openIndex = outerCloseIndex;
				} else {
					//No page object found. Move on.
					openIndex += openStrings[objectID].length();
				}
			} else {
				if (openIndex != -1) {
					openIndex += openStrings[objectID].length();
				}
			}
			
		} while (openIndex != -1);
		
		return pData;
	}
	
	private ArrayList<Section> parsePageForSections(String rawText) {
		ArrayList<Section> sections = new ArrayList<Section>();
		
		int openIndex = 0;
		int depth;
		String sectionText;
		
		do {
			//Find the section opening text.
			depth = 2;
			sectionText = new String(new char[depth]).replace("\0", "=");
			openIndex = rawText.indexOf("\n" + sectionText, openIndex+1);
			if (openIndex != -1) {
				//Figure out how deep the section is.
				int oldIndex = openIndex;
				
				//Increase depth of section until we can no longer do so.
				while (openIndex == oldIndex) {
					depth++;
					sectionText = new String(new char[depth]).replace("\0", "=");
					openIndex = rawText.indexOf("\n" + sectionText, oldIndex);
				}
				
				//Backtrack.
				depth--;
				sectionText = new String(new char[depth]).replace("\0", "=");
				openIndex = oldIndex;
				
				//Test that we do in fact have a section. They should not contain \n.
				int closeIndex = rawText.indexOf(sectionText + "\n", openIndex+depth);
				if (closeIndex != -1) {
					String substring = rawText.substring(openIndex+1, closeIndex);
					if (!substring.contains("\n")) {
						//We have a valid section. Add it.
						sections.add(new Section(substring.substring(depth), openIndex, depth));
					}
				}
			}
		} while (openIndex != -1);
		
		return sections;
	}
	
	/**
	 * This method uses the default escape texts provided in src/main/resources/MWEscapeTexts.
	 * @param pos The position to check.
	 * @return Is this position parsed as Mediawiki, or not?
	 */
	public boolean isPositionParsedAsMediawiki(String rawText, int pos) {
		ArrayList<String> MWEscapeOpenText = mdm.getMWEscapeOpenText();
		ArrayList<String> MWEscapeCloseText = mdm.getMWEscapeCloseText();
		return isPositionParsedAsMediawiki(rawText, pos, MWEscapeOpenText, MWEscapeCloseText);
	}
	
	public boolean isPositionParsedAsMediawiki(String rawText, int pos, ArrayList<String> escapeOpenText, ArrayList<String> escapeCloseText) {
		//Start from the beginning of the page and work up.
		int cursor = 0;
		int lowest;//Earliest occurrence of MW escaped text.
		int end = -1;//Last found MW escaped text close.
		
		do {
			cursor = end;
			
			//Find the lowest MW escaped text.
			lowest = -1;
			int lowestEscapeTextID = -1;
			end = -1;
			
			for (int escapeTextID = 0; escapeTextID < escapeOpenText.size(); escapeTextID++) {
				int index;//The location where this markup occurs.
				String markupOpening = escapeOpenText.get(escapeTextID);
				
				//Find the lowest location of this markup, if it exists.
				//Any markup that is an HTML tag requires some leeway.
				if (markupOpening.substring(0, 1).equals("<") && markupOpening.contains(">")) {
					//This markup is a tag.
					//Check if we have a potential tag opening.
					markupOpening = markupOpening.substring(0, markupOpening.indexOf('>'));
					
					index = rawText.indexOf(markupOpening, cursor);
					
					if (index != -1) {
						//We have a potential tag opening. We need to make sure this tag opening is an actual tag, and not something else.
						
						String nextChar = rawText.substring(index + markupOpening.length(), index + markupOpening.length()+1);
						//Check if the tag opening is plain, aka has no attributes or spacing.
						if (nextChar.equals(">")) {
							//This is our tag.
						} else {
							//Check if the tag has some spacing in it.
							if (nextChar.equals(" ") || nextChar.equals("\n")) {
								//Check that the tag opening closes properly.
								if (rawText.indexOf(">", index+1) == -1) {
									//Nope.
									index = -1;
								}
							} else {
								//Nope.
								index = -1;
							}
						}
					}
				} else {
					//This markup is not a tag.
					index = rawText.indexOf(markupOpening, cursor);
				}
				
				if (index != -1) {
					//Record which markup tag is the lowest.
					if (lowest == -1 || index < lowest) {
						//Yep, lowest.
						//One last check though!
						//We need to make sure this tag closes.
						end = rawText.indexOf(escapeCloseText.get(escapeTextID), index+1);
						if (end != -1) {
							//This tag closes.
							lowest = index;
							lowestEscapeTextID = escapeTextID;
							
							//Update end
							end += escapeCloseText.get(lowestEscapeTextID).length();
						}
					}
				}
			}
			
			//Move cursor
			if (lowest != -1) {
				//Move the cursor to the end of the markup pair.
				cursor = lowest;
			} else {
				//Reached end of page.
				cursor = -1;
			}
		} while (pos >= cursor && pos > end && cursor != -1 && end != -1);
		
		if (lowest == -1) {
			//No markup text found on page.
			return true;
		} else {
			//Check if this markup text contains @pos.
			return !(pos >= cursor && end > pos);
		}
	}
	
	private int findClosingPosition(String rawText, String open, String close, int start) throws ParsingError {
		//Method for finding where [[ ]] and {{ }} end.
		int i = start;
		int j;
		int k = 0;//Keeps track of the last found closing.
		int depth = 1;

		k = i;
		i = rawText.indexOf(open, i+open.length());
		j = rawText.indexOf(close, k+open.length());
		if (i > j || (i == -1 && j != -1)) {
			//This object has depth 1.
			k = j;
			depth = 0;
		} else {
			do {
				//Checking individual line.
				if (i<=j && i != -1) {
					//Depth increased
					k = i;
					i = rawText.indexOf(open, i+open.length());
					j = rawText.indexOf(close, k+open.length());
					depth++;
				} else if (j != -1) {
					//Depth decreased
					k = j;
					if (i != -1) {
						i = rawText.indexOf(open, j+close.length());
					}
					j = rawText.indexOf(close, j+close.length());
					depth--;
				}
			} while(depth>0 && j != -1);
		}
		if (depth != 0 ) {
			throw new ParsingError();
		}
		return k;
	}
	
	// Type methods
	public void setDisambiguateStatus(boolean flag) {
		resolveDisambiguates = flag;
	}
	
	public boolean getDisambiguateStatus() {
		return resolveDisambiguates;
	}
}
