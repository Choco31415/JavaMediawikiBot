package WikiBot.MediawikiData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This class stores various Mediawiki data.
 * Its methods and contents are static and public so as to have all classes have access to the same data manager.
 */
public class MediawikiDataManager {

	public static ArrayList<String> Interwiki = new ArrayList<String>();
	public static ArrayList<String> InterwikiURL = new ArrayList<String>();
	public static ArrayList<String> TemplateIgnore = new ArrayList<String>();
	public static ArrayList<String> MWEscapeOpenText = new ArrayList<String>();
	public static ArrayList<String> MWEscapeCloseText = new ArrayList<String>();
	
	public MediawikiDataManager() {
		ArrayList<String> temp = readFileAsList("/MWEscapeTexts.txt", 0, "#", true, true);
		for (int i = 0; i < temp.size(); i += 2) {
			MWEscapeOpenText.add(temp.get(i));
			MWEscapeCloseText.add(temp.get(i + 1));
		}
		
		temp = readFileAsList("/TemplateIgnore.txt", 0, "//", true, true);
		for (int i = 0; i < temp.size(); i += 1) {
			TemplateIgnore.add(temp.get(i));
		}
	}
	
	public void readFamily(String family, int commentBufferLineCount) {
		ArrayList<String> lines = readFileAsList("/Families/" + family + ".txt", commentBufferLineCount, false, true);
		
		// Gather array size
		Interwiki = new ArrayList<String>();
		InterwikiURL = new ArrayList<String>();
		
		for (String line : lines) {
			if (!line.equals("")) {
				int index = line.indexOf(":");
				Interwiki.add(line.substring(0, index).trim());
				InterwikiURL.add(line.substring(index+1).trim());
			}
		}
	}
	
	public ArrayList<String> getInterwiki() {
		return Interwiki;
	}
	
	public ArrayList<String> getInterwikiURL() {
		return InterwikiURL;
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
	
	public String getWikiURL(String wikiPrefix) {
		wikiPrefix = wikiPrefix.replace(":", "");

		for (int i = 0; i < Interwiki.size(); i++) {
			if (Interwiki.get(i).equalsIgnoreCase(wikiPrefix) || Interwiki.get(i).equalsIgnoreCase(wikiPrefix + ":")) {
				return InterwikiURL.get(i);
			}
		}
		throw new Error();
	}
	
	public String getLanguageFromURL(String url) {
		for (int i = 0; i < InterwikiURL.size(); i++) {
			if (InterwikiURL.get(i).equalsIgnoreCase(url)) {
				return Interwiki.get(i);
			}
		}
		throw new Error();
	}
	
	public ArrayList<String> readFileAsList(String location, int commentBufferLineCount, boolean comments, boolean ignoreBlankLines) {
		return readFileAsList(location, commentBufferLineCount, "#", comments, ignoreBlankLines);
	}
	
	public ArrayList<String> readFileAsList(String location, int commentBufferLineCount, String commentHeader, boolean comments, boolean ignoreBlankLines) {
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
				if (comments && (line.length() > commentHeader.length() && line.substring(0,commentHeader.length()).equals(commentHeader))) {
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
}
