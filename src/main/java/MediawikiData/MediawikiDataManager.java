package MediawikiData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	public void readDefaultFamily(String family, int commentBufferLineCount) {
		readFamily(new File("/Families/" + family + ".txt"), commentBufferLineCount);
	}
	
	public void readFamily(Path family_, int commentBufferLineCount) {
		String familyFile = FileUtils.readFile(family_.getAbsolutePath());
		
		// Initialize variables.
		WikiPrefix = new ArrayList<String>();
		WikiURL = new ArrayList<String>();
		
		// Read in family file.
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readValue(familyFile, JsonNode.class);
		} catch (IOException e1) {
			// Dangerous to just return.
			throw new Error("Was expecting Json, but did not receive Json from server.");
		}
		
		for (JsonNode wiki : rootNode) {
			String prefix = wiki.get("prefix").asText();
			String version = wiki.get("version").asText();
			String url = wiki.get("url").asText();
			
			WikiPrefix.add(prefix);
			WikiMWVersion.add(new VersionNumber(version));
			WikiURL.add(url);
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
