package Mediawiki;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//import Utils.FileUtils;
import org.apache.commons.io.FileUtils;

/**
 * This class stores various Mediawiki data.
 * To allow access from any class, its contents are static and public.
 */
public class MediawikiDataManager {
	
	private static String MWEscapeTextsFile = "MWEscapeTexts.txt";
	private static String MWTemplateTextsFile = "TemplateIgnore.txt";
	private static String HTMLCommentsFile = "HTMLComments.txt";
	
	public static MediawikiDataManager instance;

	public ArrayList<String> WikiPrefix = new ArrayList<String>();
	public ArrayList<VersionNumber> WikiMWVersion = new ArrayList<VersionNumber>();
	public ArrayList<String> WikiURL = new ArrayList<String>();
	public ArrayList<String> TemplateIgnore = new ArrayList<String>();
	public ArrayList<String> MWEscapeOpenText = new ArrayList<String>();
	public ArrayList<String> MWEscapeCloseText = new ArrayList<String>();
	public ArrayList<String> HTMLCommentOpenText = new ArrayList<String>();
	public ArrayList<String> HTMLCommentCloseText = new ArrayList<String>();
	
	@SuppressWarnings("all")
	public MediawikiDataManager() {
		try {
			String comment = "//";
			List<String> lines = FileUtils.readLines(new File(MWEscapeTextsFile), "UTF-8");
			for (int i = 0; i < lines.size(); i += 2) {
				while (lines.get(i).startsWith(comment)) { // Ignore comments
					i++;
				}
				MWEscapeOpenText.add(lines.get(i));
				MWEscapeCloseText.add(lines.get(i + 1));
			}
			
			lines = FileUtils.readLines(new File(MWTemplateTextsFile), "UTF-8");
			for (int i = 0; i < lines.size(); i += 1) {
				while (lines.get(i).startsWith(comment)) { // Ignore comments
					i++;
				}
				TemplateIgnore.add(lines.get(i));
			}
			
			lines = FileUtils.readLines(new File(HTMLCommentsFile), "UTF-8");
			for (int i = 0; i < lines.size(); i += 2) {
				while (lines.get(i).startsWith(comment)) { // Ignore comments
					i++;
				}
				HTMLCommentOpenText.add(lines.get(i));
				MWEscapeOpenText.add(lines.get(i)); // TODO: Is this duplicated??
				HTMLCommentCloseText.add(lines.get(i + 1));
				MWEscapeCloseText.add(lines.get(i + 1));
			}
			
			if (instance == null) {
				instance = this;
			}
		} catch (Exception e) {
			throw new Error("IOException: MW data files not located.");
		}
	}
	
	public static MediawikiDataManager getInstance() {
		if (instance == null) {
			instance = new MediawikiDataManager();
		}
		
		return instance;
	}
	
	public void readFamily(String familyFile) throws IOException {
		String familyFileJSON = FileUtils.readFileToString(new File(familyFile), "UTF-8");
		
		// Initialize variables.
		WikiPrefix = new ArrayList<String>();
		WikiURL = new ArrayList<String>();
		
		// Read in family file.
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readValue(familyFileJSON, JsonNode.class);
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
