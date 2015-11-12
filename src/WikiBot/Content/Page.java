package WikiBot.Content;

import java.util.ArrayList;

/**
 * Page is a custom class designed to store Wiki pages.
 * It includes several functions to edit, replace, and view specific article contents.
 * 
 * Look at this page for links: https://github.com/maxamillion40/WikiBots-in-Java/wiki
 */
public class Page extends SimplePage {
	
	private ArrayList<String> content = new ArrayList<String>();
	private ArrayList<Section> sections = new ArrayList<Section>();
	private ArrayList<Link> links = new ArrayList<Link>();
	private ArrayList<Template> templates = new ArrayList<Template>();
	private ArrayList<Image> images = new ArrayList<Image>();
	private ArrayList<String> categories = new ArrayList<String>();
	private ArrayList<Interwiki> interwikis = new ArrayList<Interwiki>();
	private ArrayList<Revision> revisions = new ArrayList<Revision>();
	
	public Page(String title_, int pageID_, String lan_) {
		super(title_, lan_, pageID_);
	}

	//Set variables.
	public void setContent(ArrayList<String> content_) {
		content = content_;
	}
	
	public void setContentLine(String content_, int lineID) {
		if (lineID >= content.size()) {
			return;
		} else {
			content.set(lineID, content_);
		}
	}
	
	//Modify variables.
	public void addLine(String content_) {
		content.add(content_);
	}
	
	public void addSection(Section section) {
		sections.add(section);
	}
	
	
	public void addLink(Link link) {
		links.add(link);
	}
	
	public void addTemplate(Template template) {
		templates.add(template);
	}
	
	public void addImage(Image image) {
		images.add(image);
	}
	
	public void addCategory(String category) {
		categories.add(category);
	}
	
	public void addInterwiki(Interwiki wiki) {
		interwikis.add(wiki);
	}
	
	public void addRevisions(ArrayList<Revision> revisions_) {
		revisions.addAll(revisions_);
	}
	
	public void setRawText(String txt) {
		rawText = txt;
	}
	
	//Get information.
	//Content methods
	public ArrayList<String> getContent() {
		return content;
	}
	
	public String getContentLine(int lineID) {
		return content.get(lineID);
	}
	
	public int getLineCount() {
		return content.size();
	}
	
	//Section methods
	public Section getSection(int i) {
		return sections.get(i);
	}
	
	public String getLastSectionText() {
		return getSectionText(sections.size());
	}
	
	public String getSectionText(int i) {
		
		if (i < 0 || i > sections.size()) {
			throw new IndexOutOfBoundsException();
		}
 		
		int line1;
		int line2;
		
		if (i == 0) {
			line1 = 0;
		} else {
			line1 = sections.get(i-1).getPosition().getLine();
		}
		if (i < sections.size()) {
			line2 = sections.get(i).getPosition().getLine();
		} else {
			line2 = content.size();
		}
		
		String temp = "";
		
		for (int line = line1; line < line2; line++) {
			temp += content.get(line);
			if (line < line2-1) {
				temp += "\n";
			}
		}
		return temp;
	}
	
	public Section getSection(Position pos) {
		int i = 0;
		while (pos.isGreaterThen(sections.get(i).getPosition())) {
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
	
	//Link methods
	public ArrayList<Link> getLinks() {
		return links;
	}
	
	//Template methods
	public boolean templatesContainLink(Link link) {
		for (int i = 0; i < templates.size(); i ++) {
			if ((templates.get(i)).containsLink(link)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Template> getTemplates() {
		return templates;
	}
	
	public Template getTemplate(String template) {
		for (Template tmp : templates) {
			if (tmp.getName().equalsIgnoreCase(template)) {
				return tmp;
			}
		}
		return null;
	}
	
	public boolean containsTemplate(String template) {
		for (Template tmp : templates) {
			if (tmp.getName().equalsIgnoreCase(template)) {
				return true;
			}
		}
		return false;
	}
	
	public void removeTemplate(String template) {
		for (Template tmp : templates) {
			if (tmp.getName().equalsIgnoreCase(template)) {
				templates.remove(tmp);
				return;
			}
		}
	}
	
	//Image methods
	public ArrayList<Image> getImages() {
		return images;
	}
	
	public boolean imagesContainLink(Link link) {
		for (int i = 0; i < images.size(); i ++) {
			if ((images.get(i)).containsLink(link)) {
				return true;
			}
		}
		return false;
	}
	
	//Category methods
	public boolean containsCategory(String category) {
		for (int i = 0; i < categories.size(); i++) {
			if ((categories.get(i)).equals(category)) {
				return true;
			}
		}
		return false;
	}
	
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

	@Override
	public String toString() {
		String output;

		output = "PAGE PAGE ;; Name: " + title + " ;; PAGE PAGE\nWith id: " + pageID  + "\n";
		output += "Language: " + lan + "\n";
		for (int i = 0; i < content.size(); i++) {
			output += (content.get(i) + "\n");
		}
		output += "\nWith sections: \n";
		for (int i = 0; i < sections.size(); i++) {
			output += (sections.get(i).toString2() + "\n");
		}	
		output += "\nWith links: \n";
		for (int i = 0; i < links.size(); i++) {
			output += (links.get(i) + "\n");
		}
		output += "\nWith templates: \n";
		for (int i = 0; i < templates.size(); i++) {
			output += (templates.get(i) + "\n");
		}
		output += "\nWith images: \n";
		for (int i = 0; i < images.size(); i++) {
			output += (images.get(i) + "\n");
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
