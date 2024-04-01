package ContentRep;

import java.util.ArrayList;

import MediawikiData.MediawikiDataManager;
import WikiBot.MediawikiBot;

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
	
	public Page(ArrayList<Integer> linePositions_, ArrayList<Section> sections_,
			ArrayList<PageObjectAdvanced> pageObjects_, ArrayList<Category> categories_, 
			ArrayList<Interwiki> interwikis_, ArrayList<Revision> revisions_,
			String lan_, String title_, int pageID_) {
		super(lan_, title_, pageID_);
		linePositions = linePositions_;
		sections = sections_;
		pageObjects = pageObjects_;
		categories = categories_;
		interwikis = interwikis_;
		revisions = revisions_;
		
		mdm = MediawikiDataManager.getInstance();
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
