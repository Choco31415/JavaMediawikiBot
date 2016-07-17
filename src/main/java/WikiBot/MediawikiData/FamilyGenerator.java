package WikiBot.MediawikiData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.commons.lang3.StringEscapeUtils;

import WikiBot.Core.NetworkingBase;
import WikiBot.Utils.ArrayUtils;
import WikiBot.Utils.FileUtils;

public class FamilyGenerator extends NetworkingBase {

	private static final long serialVersionUID = 1L;
	private static final String RESOURCES_PATH = "src/main/resources";
	
	private BufferedReader br;
	private String input = null;
	
	private String familyName;
	private ArrayList<String> wikiPrefixes = new ArrayList<String>();
	private ArrayList<String> wikiURLs = new ArrayList<String>();
	private ArrayList<String> MWversions = new ArrayList<String>();

	private FamilyGenerator() {
		super();
	}

	static public void main(String[] args) throws IOException, URISyntaxException {
		FamilyGenerator instance = new FamilyGenerator();
		instance.run();
	}
	
	/**
	 * The entry point for making a new family file.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void run() throws IOException, URISyntaxException {
		setLoggerLevel(Level.INFO);
		
		boolean running = true;
		System.out.println("You are running the script that makes a new wiki family.");
		
		//Get user input
		br = new BufferedReader(new InputStreamReader(System.in));
		
		boolean legibleInput;
		do {
			legibleInput = true;
			
			//User options
			System.out.println("b - Build a wiki family");
			System.out.println("e - Exit");
			
			//Read in user input
			input = br.readLine();
			
			switch (input) {
				case "b":
					//Meh.
					break;
				case "e":
					running = false;
					break;
				default:
					System.out.println("Invalid input. Please only enter: m, g, e");
					legibleInput = false;
					break;
			}
		} while (!legibleInput);
		
		System.out.println("What is the name of the wiki family?");
		
		input = br.readLine();
		familyName = input;
		
		System.out.println("Creating family " + familyName + ".");
		
		if (running) {
			manuallyBuildWikiFamily();
			writeFamily();
			System.exit(0);
		} else {
			System.exit(0);
		}
	}
	
	private void manuallyBuildWikiFamily() throws IOException, URISyntaxException {
		//CUI
		boolean running = true;
		boolean legibleInput = true;
		do {
			do {
				legibleInput = true;
				
				//User options
				System.out.println("a - Add a wiki");
				System.out.println("i - Add interwiki family (Requires MW v.1.11+)");
				System.out.println("r - Remove a wiki");
				System.out.println("v - View current family");
				System.out.println("e - Finish and write family");
				
				//Read in user input
				input = br.readLine();
				
				switch (input) {
					case "a":
						addWiki();
						break;
					case "i":
						addInterwikiFamily();
						break;
					case "r":
						removeWiki();
						break;
					case "v":
						printWiki();
						break;
					case "e":
						running = false;
						break;
					default:
						System.out.println("Invalid input. Please only enter: a, i, r, v, e");
						legibleInput = false;
						break;
				}
			} while (!legibleInput);
		} while (running);
	}
	
	private void addInterwikiFamily() throws IOException, URISyntaxException  {
		//Check a few things with the user first.
		boolean excludeDefaultInterwiki = true;
		boolean localOnly = true;
		
		boolean legibleInput = true;
		do {
			legibleInput = true;
			
			//User options
			System.out.println("Do you want to include all interwikis, or just the local family?");
			System.out.println("All will include: Wikipedia, Wikimedia, Commons, Acronym Finder, ect...");
			System.out.println("a - All (This will take a while)");
			System.out.println("l - Local family only (This might take awhile)");
			
			//Read in user input
			input = br.readLine();
			
			switch (input) {
				case "a":
					excludeDefaultInterwiki = false;
					localOnly = false;
					break;
				case "l":
					excludeDefaultInterwiki = true;
					localOnly = true;
					break;
				default:
					System.out.println("Invalid input. Please only enter: a, l");
					legibleInput = false;
					break;
			}
		} while (!legibleInput);
		
		ArrayList<String> toExclude = new ArrayList<String>();
		if (excludeDefaultInterwiki) {
			System.out.println("If any wikis are included by mistake, update "
					+ "Families/Miscalleneous/DefaultInterwikis.txt and contact "
					+ "JMB's owner Choco31415.");
			toExclude = FileUtils.readFileAsList("/Families/Miscalleneous/DefaultInterwikis.txt", 0, true, true);
		}
		
		//Ask for a wiki url, so we can get all wikis in the wiki group.
		System.out.println("What is a URL to a wiki in the family?");
		
		input = br.readLine();
		
		//Check wiki for interwikis
		String url = input;
		String URLapi = getAPIurl(url);
		
		//Get the interwiki map. MW v.1.11+ required
		if (localOnly) {
			url = URLapi + "/api.php?action=query&meta=siteinfo&siprop=interwikimap&format=xml";
		} else {
			url = URLapi + "/api.php?action=query&meta=siteinfo&siprop=interwikimap&sifilteriw=local&format=xml";
		}
		
		String serverOutput = ArrayUtils.compactArray(getURL(url, false, true));
		ArrayList<String> lines = parseTextForItems(serverOutput, "<iw prefix", "/>", 0);
		
		System.out.print("Detected wikis: ");
		for (String line : lines) {
			if (line.contains("language")) {
				url = parseTextForItem(line, "url=", "\"", 1, 0).replace("$", "").replace(" ", "_");
				String prefix = parseTextForItem(line, "iw prefix", "\"");
				
				if (!wikiPrefixes.contains(prefix) && !toExclude.contains(prefix)) {
					System.out.print(prefix + " ");
					
					url = getAPIurl(url);
					
					wikiURLs.add(url);
					wikiPrefixes.add(prefix);
					
					String version = getMWversion(url);
					MWversions.add(version);
				}
			}
		}
		System.out.println("");
	}
	
	private void addWiki() throws IOException, URISyntaxException {
		System.out.println("What is the prefix of your wiki?");
		System.out.println("This is preferably the interwiki prefix of the wiki, if there exists one.");
		
		input = br.readLine();
		String prefix = input.toLowerCase();
		
		System.out.println("What is a wiki url?");
		
		input = br.readLine();

		String URLapi = getAPIurl(input);
		if (URLapi != null) {
			System.out.println("The api url for this wiki is: " + URLapi);
			
			System.out.println("Getting wiki MW version...");
			String version = getMWversion(URLapi);
			
			wikiPrefixes.add(prefix);
			wikiURLs.add(URLapi);
			MWversions.add(version);
			
			System.out.println("Detected MW version " + version);
		}
	}
	
	private void removeWiki() throws IOException {
		System.out.println("What is prefix of the wiki you want to remove?");
		System.out.println("Hit enter to go back.");
		
		input = br.readLine();
		
		if (!input.equals("")) {
			//Remove wiki.
			int index = wikiPrefixes.indexOf(input);
			
			if (index != -1) {
				System.out.println("Removing wiki " + input);
				
				wikiURLs.remove(index);
				wikiPrefixes.remove(index);
				MWversions.remove(index);
			} else {
				System.out.println("Could not find wiki.");
			}
		}
	}
	
	private void printWiki() throws IOException {
		System.out.println("Currently creating wiki family " + familyName);
		if (wikiPrefixes.size() == 0) {
			System.out.println("No wikis in the current family so far.");
		}
		
		for (int i = 0; i < wikiPrefixes.size(); i++) {
			System.out.println("wiki " + (i+1) + ": " + wikiPrefixes.get(i) + " : " + MWversions.get(i) + " : " + wikiURLs.get(i));
		}
	}
	
	/*
	 * 
	 * Some methods for getting wiki information.
	 * 
	 */
	
	/**
	 * Gets the API url from any wiki url.
	 * @param wikiURL
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	private String getAPIurl(String url) throws URISyntaxException, IOException {
		URI uri;
		String host;
		try {
			uri = new URI(url);
			host = uri.getHost();
		} catch (Throwable e) {
			//We're dealing with Russian, Japanese, or some sort of non-standard characters.
			uri = new URI(StringEscapeUtils.escapeHtml4(url));
			System.out.println(uri.toString());
			host = StringEscapeUtils.unescapeHtml4(uri.getHost());
		}
		
		
		//Brute force attack the api location. Can't do much better. ¯\_(ツ)_/¯ 
		String URLbase = "http://" + host + "/w/api.php";
		String URLheader;
		String URLdirectory = null;
		int responseCode = getResponseCode(URLbase);
		
		//Http or https?
		if ( responseCode == 200 ) {
			URLheader = "http://";
		} else {
			URLheader = "https://";
		}
		
		//Is the api at /w or /wiki or something else?
		//Let's try /w first.
		URLbase = URLheader + host + "/w/api.php";
		responseCode = getResponseCode(URLbase);
		boolean isWiki = true;//Is this actually a wiki and not some other website?
		if (responseCode == 200) {
			URLdirectory = "/w";
		} else {
			//Let's try /wiki.
			URLbase = URLheader + host + "/wiki/api.php";
			responseCode = getResponseCode(URLbase);
			if (responseCode == 200) {
				URLdirectory = "/wiki";
			} else {
				//Ask user.
				System.out.println("The api path for this wiki could not be determined."
						+ "\nhost: " + host
						+ "\nPlease go to Special:Version on the wiki, and copy"
						+ "\nthe API path here. Leave out the /api.php."
						+ "\nIf this is not a wiki, type 'n'.");
				
				input = br.readLine();
				if (input.equalsIgnoreCase("n")) {
					isWiki = false;
				} else {
					URLdirectory = input;
				}
			}	
		}
		
		if (isWiki) {
			return URLheader + host + URLdirectory;
		} else {
			return null;
		}
	}
	
	private String getMWversion(String URLapi) throws IOException {
		String serverOutput = ArrayUtils.compactArray(getURL(URLapi + "/api.php?action=query&meta=siteinfo&format=xml", false, true));
		String generator = parseTextForItem(serverOutput, "generator", "\"");
		String version = generator.substring(generator.indexOf(" ")).trim();
		
		return version;
	}
	
	private void writeFamily() {
		String toWrite = "";
		
		if (wikiPrefixes.size() > 0) {
			for (int i = 0; i < wikiPrefixes.size(); i++) {
				if (i != 0) {
					toWrite += "\n";
				}
				toWrite += wikiPrefixes.get(i);
				toWrite += ":" + MWversions.get(i);
				toWrite += ": " + wikiURLs.get(i);
			}
			
			FileUtils.writeFile(toWrite, RESOURCES_PATH + "/Families/" + familyName + ".txt");
		}
	}
	
	@Override
	public boolean log(Level level, String line) {
		if (super.log(level, line)) {
			System.out.println(line);
			return true;
		}
		return false;
	}
}
