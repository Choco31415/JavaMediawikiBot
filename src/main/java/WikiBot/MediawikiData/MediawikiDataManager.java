package WikiBot.MediawikiData;

import java.util.ArrayList;

import WikiBot.Utils.FileUtils;

/**
 * This class stores various Mediawiki data.
 * To allow access from any class, its contents are static and public.
 */
public class MediawikiDataManager {
	
	public static MediawikiDataManager instance;

	public ArrayList<String> WikiPrefix = new ArrayList<String>();
	public ArrayList<VersionNumber> WikiMWVersion = new ArrayList<VersionNumber>();
	public ArrayList<String> WikiURL = new ArrayList<String>();
	public ArrayList<String> TemplateIgnore = new ArrayList<String>();
	public ArrayList<String> MWEscapeOpenText = new ArrayList<String>();
	public ArrayList<String> MWEscapeCloseText = new ArrayList<String>();
	public ArrayList<String> HTMLCommentOpenText = new ArrayList<String>();
	public ArrayList<String> HTMLCommentCloseText = new ArrayList<String>();
	
	public MediawikiDataManager() {
		ArrayList<String> temp = FileUtils.readFileAsList("/MWEscapeTexts.txt", 0, true, "#", true);
		for (int i = 0; i < temp.size(); i += 2) {
			MWEscapeOpenText.add(temp.get(i));
			MWEscapeCloseText.add(temp.get(i + 1));
		}
		
		temp = FileUtils.readFileAsList("/TemplateIgnore.txt", 0, true, "//", true);
		for (int i = 0; i < temp.size(); i += 1) {
			TemplateIgnore.add(temp.get(i));
		}
		
		temp = FileUtils.readFileAsList("/HTMLComments.txt", 0, true, "#", true);
		for (int i = 0; i < temp.size(); i += 2) {
			HTMLCommentOpenText.add(temp.get(i));
			MWEscapeOpenText.add(temp.get(i));
			HTMLCommentCloseText.add(temp.get(i + 1));
			MWEscapeCloseText.add(temp.get(i + 1));
		}
		
		if (instance == null) {
			instance = this;
		}
	}
	
	public static MediawikiDataManager getInstance() {
		if (instance == null) {
			instance = new MediawikiDataManager();
		}
		
		return instance;
	}
	
	public void readFamily(String family, int commentBufferLineCount) {
		ArrayList<String> lines = FileUtils.readFileAsList("/Families/" + family + ".txt", commentBufferLineCount, false, true);
		
		// Gather array size
		WikiPrefix = new ArrayList<String>();
		WikiURL = new ArrayList<String>();
		
		for (String line : lines) {
			if (!line.equals("")) {
				int index = line.indexOf(":");
				int index2 = line.indexOf(":", index+1);
				WikiPrefix.add(line.substring(0, index).trim());
				WikiMWVersion.add(new VersionNumber(line.substring(index+1, index2).trim()));
				WikiURL.add(line.substring(index2+1).trim());
			}
		}
	}
	
	public ArrayList<String> getWikiPrefixes() {
		return WikiPrefix;
	}
	
	public String getWikiPrefix(int index) {
		return WikiPrefix.get(index);
	}
	
	public ArrayList<VersionNumber> getWikiMWVersions() {
		return WikiMWVersion;
	}
	
	public VersionNumber getWikiMWVersion(int index) {
		return WikiMWVersion.get(index);
	}
	
	public VersionNumber getWikiMWVersion(String wikiPrefix) {
		int index = WikiPrefix.indexOf(wikiPrefix);
		return WikiMWVersion.get(index);
	}
	
	public ArrayList<String> getWikiURLs() {
		return WikiURL;
	}
	
	public String getWikiURL(int index) {
		return WikiURL.get(index);
	}
	
	public String getWikiURL(String wikiPrefix) {
		int index = WikiPrefix.indexOf(wikiPrefix);
		
		return WikiURL.get(index);
	}
	
	public int getNumWikis() {
		return WikiPrefix.size();
	}
	
	public ArrayList<String> getTemplateIgnore() {
		return TemplateIgnore;
	}
	
	public ArrayList<String> getMWEscapeOpenText() {
		return MWEscapeOpenText;
	}
	
	public ArrayList<String> getMWEscapeCloseText() {
		return MWEscapeCloseText;
	}
	
	public ArrayList<String> getHTMLCommentOpenText() {
		return HTMLCommentOpenText;
	}
	
	public ArrayList<String> getHTMLCommentCloseText() {
		return HTMLCommentCloseText;
	}
	
	public String getWikiPrefixFromURL(String url) {
		for (int i = 0; i < WikiURL.size(); i++) {
			if (WikiURL.get(i).equalsIgnoreCase(url)) {
				return WikiPrefix.get(i);
			}
		}
		throw new Error();
	}
}
