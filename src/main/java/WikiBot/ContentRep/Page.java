package WikiBot.ContentRep;

import java.util.ArrayList;

import WikiBot.Core.GenericBot;
import WikiBot.MediawikiData.MediawikiDataManager;

/**
 * Page is a custom class designed to store Wiki pages.
 * It includes several functions to edit, replace, and view specific article contents.
 * 
 * The class is organized into two parts. The first half is useful methods. The second half is parsing code used to generate class data.
 * 
 * Page data, specifically that stored in the arraylist pageObjects, is stored in a tree hierarchy.
 * Example: A link is stored within a template, which is stored within a template.
 * All methods in this class, unless recursive, only access first level objects.
 */
public class Page extends SimplePage {
	
	//MW Data Manager
	private MediawikiDataManager mdm;
	
	//Page data
	private ArrayList<Integer> linePositions = new ArrayList<Integer>();
	private ArrayList<Section> sections = new ArrayList<Section>();
	private ArrayList<PageObjectAdvanced> pageObjects = new ArrayList<PageObjectAdvanced>();//Templates, links, more
	private ArrayList<Category> categories = new ArrayList<Category>();
	private ArrayList<Interwiki> interwikis = new ArrayList<Interwiki>();
	private ArrayList<Revision> revisions = new ArrayList<Revision>();
	
	public Page(String lan_, String title_, int pageID_) {
		super(lan_, title_, pageID_);
		
		mdm = MediawikiDataManager.getInstance();
	}
	
	//Modify variables.
	@Override
	public void setRawText(String rawText_) {
		rawText = rawText_;
		parseRawText();
	}
	
	public void setRevisions(ArrayList<Revision> revisions_) {
		revisions.addAll(revisions_);
	}
	
	public void addCategory(Category category) {
		categories.add(category);
	}
	
	public void addInterwiki(Interwiki wiki) {
		interwikis.add(wiki);
	}
	
	//Get information.
	//Line methods
	public int getLinePosition(int line) {
		return linePositions.get(line);
	}
	
	public int getNumLines() {
		return linePositions.size();
	}
	
	//Section methods
	/**
	 * Sections start counting at 1.
	 * @param i The id of the section you want.
	 * @return A Section object.
	 */
	public Section getSectionByNum(int i) {
		return sections.get(i-1);
	}
	
	public String getHeaderText() {
		return getSectionText(0);
	}
	
	public String getLastSectionText() {
		return getSectionText(sections.size());
	}
	
	/**
	 * Get the section text of a section. 0 is the page headers. Sections start counting at 1.
	 * @param i The section that you want to get the text of.
	 * @return The section's text.
	 */
	public String getSectionText(int i) {
		
		if (i < 0 || i > sections.size()) {
			throw new IndexOutOfBoundsException();
		}
 		
		int pos1;
		int pos2;
		
		if (i == 0) {
			pos1 = 0;
		} else {
			pos1 = sections.get(i-1).getPosition();
		}
		if (i < sections.size()) {
			pos2 = sections.get(i).getPosition();
		} else {
			pos2 = rawText.length();
		}
		
		return rawText.substring(pos1, pos2);
	}
	
	/**
	 * Find the section that corresponds with this text position.
	 * @param pos The text position.
	 * @return A section id.
	 */
	public Section getSection(int pos) {
		
		if (pos < 0 || pos >= rawText.length()) {
			throw new IndexOutOfBoundsException();
		}
		
		int i = 0;
		while (i != sections.size() && pos > sections.get(i).getPosition()) {
			i++;
		}
		if (i == 0) {
			return null;
		} else {
			return sections.get(i-1);
		}
	}
	
	public int getNumSections() {
		return sections.size();
	}
	
	public int getNumTopLevelSections() {
		int temp = 0;
		
		for (Section st : sections) {
			if (st.getDepth() == 2) {
				temp++;
			}
		}
		
		return temp;
	}
	
	//PageObject methods
	public int getNumPageObjects() {
		return pageObjects.size();
	}
	
	public int getNumPageObjectsOfType(String objectType) {
		int count = 0;
		for (PageObjectAdvanced object: pageObjects) {
			//Check that the object type matches.
			if (object.getObjectType().equalsIgnoreCase(objectType)) {
				count++;
			}
		}
		return count;
	}
	
	public int getNumPageObjectsByHeader(String header) {
		int count = 0;
		for (PageObjectAdvanced object: pageObjects) {
			//Check that the header matches.
			if (object.getHeader().trim().equalsIgnoreCase(header.trim())) {
				count++;
			}
		}
		return count;
	}
	
	public PageObjectAdvanced getPageObject(int index) {
		return pageObjects.get(index);
	}
	
	public PageObjectAdvanced getPageObject(String header) {		
		for (PageObjectAdvanced poa : pageObjects) {
			//Check that header matches.
			if (poa.getHeader().trim().equalsIgnoreCase(header.trim())) {
				return poa;
			}
		}
		
		return null;
	}
	
	public PageObjectAdvanced getPageObject(String header, String objectType) {		
		for (PageObjectAdvanced poa : pageObjects) {
			//Check that header matches.
			if (poa.getHeader().trim().equalsIgnoreCase(header.trim())) {
				//Check that object type matches.
				if (poa.getObjectType().equalsIgnoreCase(objectType)) {
					return poa;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<PageObjectAdvanced> getAllPageObjects() {
		return pageObjects;
	}
	
	public ArrayList<PageObjectAdvanced> getAllPageObjectsRecursive() {
		ArrayList<PageObjectAdvanced> toReturn = new ArrayList<PageObjectAdvanced>();
		
		for (PageObjectAdvanced poa : pageObjects) {
			toReturn.addAll(poa.getAllPageObjectsRecursive());
			toReturn.add(poa);
		}
		
		return toReturn;
	}
	
	/**
	 * Only get top level external links.
	 */
	public ArrayList<ExternalLink> getExternalLinks() {
		return getPageObjects(ExternalLink.class);
	}
	
	/**
	 * Get all external links.
	 */
	public ArrayList<ExternalLink> getExternalLinksRecursive() {
		return getPageObjectsRecursive(ExternalLink.class);
	}
	
	/**
	 * Only get top level links.
	 */
	public ArrayList<Link> getLinks() {
		return getPageObjects(Link.class);
	}
	
	/**
	 * Get all links.
	 */
	public ArrayList<Link> getLinksRecursive() {
		return getPageObjectsRecursive(Link.class);
	}
	
	/**
	 * Only get top level images.
	 */
	public ArrayList<Image> getImages() {
		return getPageObjects(Image.class);
	}
	
	/**
	 * Get all images.
	 */
	public ArrayList<Image> getImagesRecursive() {
		return getPageObjectsRecursive(Image.class);
	}
	
	/**
	 * Only get top level templates.
	 */
	public ArrayList<Template> getTemplates() {
		return getPageObjects(Template.class);
	}
	
	/**
	 * Get all templates.
	 */
	public ArrayList<Template> getTemplatesRecursive() {
		return getPageObjectsRecursive(Template.class);
	}
	
	/**
	 * @param objectType A class that extends PageObjectAdvanced.
	 * @return A list of all page objects of the {@objectType} class.
	 */
	@SuppressWarnings("unchecked")
	public <T extends PageObjectAdvanced> ArrayList<T> getPageObjects(Class<T> objectType) {
		ArrayList<T> toReturn = new ArrayList<T>();
		
		for (PageObjectAdvanced object : pageObjects) {
			if (objectType.isAssignableFrom(object.getClass())) {
				toReturn.add((T)object);
			}
		}
		
		return toReturn;
	}
	
	/**
	 * @param objectType A class that extends PageObjectAdvanced.
	 * @return A list of all page objects of the {@objectType} class.
	 */
	@SuppressWarnings("unchecked")
	public <T extends PageObjectAdvanced> ArrayList<T> getPageObjectsRecursive(Class<T> objectType) {
		ArrayList<T> toReturn = new ArrayList<T>();
		
		for (PageObjectAdvanced object : getAllPageObjectsRecursive()) {
			if (objectType.isAssignableFrom(object.getClass())) {
				toReturn.add((T)object);
			}
		}
		
		return toReturn;
	}
	
	//Category methods
	public boolean containsCategory(String name) {
		String categoryName = (new PageTitle(name)).getTitleWithoutNameSpace();
		for (int i = 0; i < categories.size(); i++) {
			if ((categories.get(i).getCategoryName()).equalsIgnoreCase(categoryName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param name The name of the category. It accepts Category:CatName and CatName.
	 * @return The category that has name {@name}. Null if not found.
	 */
	public Category getCategory(String name) {
		String categoryName = (new PageTitle(name)).getTitleWithoutNameSpace();
		
		for (Category cat : categories) {
			if (cat.getCategoryNameWithoutNameSpace().equalsIgnoreCase(categoryName)) {
				return cat;
			}
		}
		return null;
	}
	
	public int getNumCategories() { return categories.size(); }
	public Category getCategory(int index) { return categories.get(index); }
	
	//Interwiki methods
	public boolean containsInterwiki(String language) {
		for (Interwiki iw : interwikis) {
			if (iw.getLanguage().equalsIgnoreCase(language)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsInterwiki(String language, String pageName) {
		for (Interwiki iw : interwikis) {
			if (iw.getLanguage().equals(language)) {
				if (iw.getTitle().equalsIgnoreCase(pageName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Interwiki getInterwiki(String language) {
		for (Interwiki iw : interwikis) {
			if (iw.getLanguage().equals(language)) {
				return iw;
			}
		}
		throw new Error();
	}
	
	public Interwiki getInterwiki(int index) {
		return interwikis.get(index);
	}
	
	public ArrayList<Interwiki> getInterwikis() {
		return interwikis;
	}
	
	public int getNumInterwiki() {
		return interwikis.size();
	}
	
	//Revision methods
	public int getRevisionCount() {
		return revisions.size();
	}
	
	public Revision getRevision(int i) {
		return revisions.get(i);
	}
	
	//Type methods
	public SimplePage createSimplePage() {
		SimplePage output = new SimplePage(lan, getTitle(), pageID);
		output.setRawText(rawText);
		return output;
	}
	
	/*
	 * All page parsing code goes below!
	 * Unless you are a developer, you can safely ignore all code below.
	 */
	
	private void parseRawText() {
		//1-new lines
		//2-pageObjects
		//3-sections
		//Clear any residual data.
		linePositions.clear();
		sections.clear();
		pageObjects.clear();
		categories.clear();
		interwikis.clear();
		
		//Generate data.
		parsePageForNewLines();
		parsePageForPageObjects();
		parsePageForSections();
	}
	
	private void parsePageForNewLines() {
		//Only these escape texts impact new lines.
		ArrayList<String> escapeOpenText = mdm.getHTMLCommentOpenText();
		ArrayList<String> escapeCloseText = mdm.getHTMLCommentCloseText();

		for (int i = 0; i != -1; i = rawText.indexOf('\n', i+1)) {
			if (isPositionParsedAsMediawiki(i, escapeOpenText, escapeCloseText)) {
				linePositions.add(i);
			}
		}
	}
	
	private void parsePageForPageObjects() {
		parseTextForPageObjects(rawText, 0, 0);
	}
	
	private ArrayList<PageObjectAdvanced> parseTextForPageObjects(String text, int pos, int depth) {
		ArrayList<PageObjectAdvanced> output = new ArrayList<PageObjectAdvanced>();
		
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
				openIndex = text.indexOf(openStrings[i], lastOpenIndex);
				if ((openIndex < lowestIndex || lowestIndex == -1) && openIndex != -1) {
					lowestIndex = openIndex;
					objectID = i;
				}
			}
			
			//Test that we might have an object.
			openIndex = lowestIndex;
			if (openIndex != -1 && isPositionParsedAsMediawiki(openIndex+pos)) {
				//Test that we do have a page object.
				innerCloseIndex = findClosingPosition(text, openStrings[objectID], closeStrings[objectID], openIndex);
				
				if (objectID == 1) {
					//Mediawiki preference. If a link closing can be put one character further, do it.
					if (text.length() >= innerCloseIndex + 1 + closeStrings[1].length()) {
						if (text.substring(innerCloseIndex + 1, innerCloseIndex + 1 + closeStrings[1].length()).equals(closeStrings[1])) {
							innerCloseIndex += 1;
						}
					}
				}
				
			objectParse:
				if (innerCloseIndex != -1) {
					// We have a page object. Parse!
					PageObjectAdvanced po = null;
					String objectText = text.substring(openIndex + openStrings[objectID].length(), innerCloseIndex);
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
						
						po = new Template(openIndex+pos, outerCloseIndex+pos, getTitle(), header);
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
							categories.add(new Category(objectText, openIndex+pos, outerCloseIndex+pos));
							
							openIndex = outerCloseIndex;
							break objectParse;
						} else {
							//Check for interwiki
							for (String iw: mdm.getWikiPrefixes()) {
								if (header.length() >= iw.length() && header.substring(0, iw.length()).equals(iw)) {
									interwikis.add(new Interwiki(iw, header.substring(iw.length()+1).trim(), openIndex+pos, outerCloseIndex+pos));
									
									openIndex = outerCloseIndex;
									break objectParse;
								}
							}
							
							//Check that this is indeed a link.
							//Extra [ means the link is slightly further down.
							//Odd multiples of [ do not create a link.
							if (header.substring(0,1).equals("[")) {
								openIndex += openStrings[objectID].length();
								break objectParse;
							}
							
							isLink = true;
							po = new Link(openIndex+pos, outerCloseIndex+pos, getTitle(), header);
						}
					} else {
						//[
						//Is this an external link?
						if ((header.length() > 7 && header.substring(0, 7).equals("http://")) || (header.length() > 2 && header.substring(0, 2).equals("//"))) {
							isLink = true;
							po = new ExternalLink(openIndex+pos, outerCloseIndex+pos, header);
						} else {
							openIndex += 1;
							break objectParse;
						}
					}
					
					//Page objects within the current page object.
					ArrayList<PageObjectAdvanced> objects = parseTextForPageObjects(objectText, openIndex + pos + openStrings[objectID].length(), depth+1);
					
					//Resolve link/template disambiguates
					//Aka templates encased by [[ ]] are disambiguates in that they could turn into a link, or stay a template.
					//It all depends on if the template is multi-lined or not.
					GenericBot bot = GenericBot.getInstance();
					if (isLink && bot.shouldParseThurough()) {
						for (PageObject object: objects) {
							if (object.getObjectType().equalsIgnoreCase("Template")) {
								SimplePage sp = bot.getWikiSimplePage(new PageLocation(lan, ((Template)object).getTemplateName()));
								if (sp.getRawText().contains("\n") && depth == 0) {
									//This is parsed as a template.
									//Add all page objects to the page.
									for (PageObject object2: objects) {
										if (object2.getObjectType().equalsIgnoreCase("Category")) {
											categories.add((Category)object2);
										} else {
											pageObjects.add((PageObjectAdvanced)object2);
										}
									}
									
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
								openIndex += 2;
								break objectParse;
							}
						}
						
						if (objectText.contains(openStrings[1])) {
							openIndex += 2;
							break objectParse;
						}
					}
					
					//Parse for parameters.
					ArrayList<Integer> parameterLocations= new ArrayList<Integer>();
					while (tempIndex != -1) {
						if (isPositionParsedAsMediawiki(tempIndex+pos)) {
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
					
					po.addPageObjects(objects);
					
					output.add(po);
					
					//Add the page object to the page.
					if (depth == 0) {
						pageObjects.add(po);
					}
					
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
		
		return output;
	}
	
	private void parsePageForSections() {
		int openIndex = 0;
		int depth;
		String sectionText;
		
		do {
			//Find section opening text.
			depth = 2;
			sectionText = new String(new char[depth]).replace("\0", "=");
			openIndex = rawText.indexOf("\n" + sectionText, openIndex+1);
			if (openIndex != -1) {
				int oldIndex = openIndex;
				//Increase depth of section until we can no longer do so.
				while (openIndex == oldIndex) {
					depth++;
					sectionText = new String(new char[depth]).replace("\0", "=");
					openIndex = rawText.indexOf("\n" + sectionText, oldIndex);
				}
				//Backtrack a bit.
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
	}
	
	/**
	 * This method uses the default escape texts provided in src/main/resources/MWEscapeTexts.
	 * @param pos The position to check.
	 * @return Is this position parsed as Mediawiki, or not?
	 */
	public boolean isPositionParsedAsMediawiki(int pos) {
		ArrayList<String> MWEscapeOpenText = mdm.getMWEscapeOpenText();
		ArrayList<String> MWEscapeCloseText = mdm.getMWEscapeCloseText();
		return isPositionParsedAsMediawiki(pos, MWEscapeOpenText, MWEscapeCloseText);
	}
	
	public boolean isPositionParsedAsMediawiki(int pos, ArrayList<String> escapeOpenText, ArrayList<String> escapeCloseText) {
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
	
	private int findClosingPosition(String text, String open, String close, int start) {
		//Method for finding where [[ ]] and {{ }} end.
		int i = start;
		int j;
		int k = 0;//Keeps track of last found closing.
		int depth = 1;

		k = i;
		i = text.indexOf(open, i+open.length());
		j = text.indexOf(close, k+open.length());
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
					i = text.indexOf(open, i+open.length());
					j = text.indexOf(close, k+open.length());
					depth++;
				} else if (j != -1) {
					//Depth decreased
					k = j;
					if (i != -1) {
						i = text.indexOf(open, j+close.length());
					}
					j = text.indexOf(close, j+close.length());
					depth--;
				}
			} while(depth>0 && j != -1);
		}
		if (depth != 0 ) {
			GenericBot bot = GenericBot.getInstance();
			bot.logFine("Detected possible unclosed parseable item at page: " + getTitle() + " language: " + getLanguage());
			return start + open.length();
		}
		return k;
	}
	
	@Override
	public String toString() {
		String output;

		output = "PAGE PAGE ;; Name: " + getTitle() + " ;; PAGE PAGE\nWith id: " + pageID  + "\n";
		output += "Language: " + lan + "\n";
		output += rawText;
		output += "\nWith sections: \n";
		for (int i = 0; i < sections.size(); i++) {
			output += (sections.get(i).toString2() + "\n");
		}	
		output += "\nWith page objects: \n";
		for (int i = 0; i < pageObjects.size(); i++) {
			output += (pageObjects.get(i) + "\n");
		}
		output += "\nWith categories: \n";
		for (int i = 0; i < categories.size(); i++) {
			output += (categories.get(i) + " , ");
		}
		output += "\nWith interwikis: \n";
		for (int i = 0; i < interwikis.size(); i++) {
			output += (interwikis.get(i) + " , ");
		}
		output += "\n\nWith revision history: \n";
		for (int i = 0; i < revisions.size(); i++) {
			output += (revisions.get(i) + "\n");
		}
		return output;
	}
}
