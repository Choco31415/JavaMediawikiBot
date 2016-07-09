package WikiBot.Core;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import WikiBot.Errors.NetworkError;

/**
 * NetworkingBases handles all networking and logging for a bot.
 * 
 * For the average user, this class is only interesting for the logger code.
 * 
 * Logging levels are: - SEVERE (fatal errors)
 *                     - WARNING (might be errors)
 *                     - INFO (general GUI stuff)
 *                     - CONFIG (finer GUI stuff)
 *                     - FINE (bot methods)
 *                     - FINER (finer bot method info)
 *                     - FINEST (server output)
 *                     
 * @author ErnieParke/Choco31415
 */
@SuppressWarnings("serial")
public class NetworkingBase extends javax.swing.JPanel {
	
	//Log variables.
	private ArrayList<String> logger = new ArrayList<String>();
	public Level logLevel = Level.INFO;
	
	//These variables are used for networking purposes (GET and POST requests).
    private HttpClient httpclient;
	private HttpClientContext context;
	
	//Instantiation.
	public NetworkingBase() {
		httpclient = HttpClientBuilder.create().build();
		context =  HttpClientContext.create();
	}
	
	/*
	 * Logger code.
	 */
	
	/**
	 * Add finest info to the logger.
	 * @param line 
	 */
	public void logFinest(String line) {
		log(Level.FINEST, line);
	}
	
	/**
	 * Add finer info to the logger.
	 * @param line 
	 */
	public void logFiner(String line) {
		log(Level.FINER, line);
	}
	
	/**
	 * Add fine info to the logger.
	 * @param line 
	 */
	public void logFine(String line) {
		log(Level.FINE, line);
	}
	
	/**
	 * Add configuration info to the logger.
	 * @param line
	 */
	public void logConfig(String line) {
		log(Level.CONFIG, line);
	}
	
	/**
	 * Add a line to the logger.
	 * @param line 
	 */
	public void logInfo(String line) {
		log(Level.INFO, line);
	}
	
	/**
	 * Add a warning to the logger.
	 * @param line
	 */
	public void logWarning(String line) {
		log(Level.WARNING, "WARNING: " + line);
	}
	
	/**
	 * Add an error to the logger.
	 * @param line
	 */
	public void logError(String line) {
		log(Level.SEVERE, "ERROR: " + line);
	}
	
	public boolean log(Level level, String line) {
		if (level.intValue() >= logLevel.intValue()) {
			logger.add("[" + getTimeStamp() + "]: " + line);
			
			return true;
		}
		return false;
	}
	
	/**
	 * Set how fine you want the log.
	 * Refer to the comment at the top of NetworkingBase to see what each level corresponds to.
	 * 
	 * @param level The fineness level.
	 */
	public void setLoggerLevel(Level level) {
		logLevel = level;
	}
	
	/**
	 * Get the most recent logger line.
	 * @return
	 */
	public String getNewestLoggerLine() {
		if (logger.size() == 0) {
			return "";
		} else {
			return logger.get(logger.size()-1);
		}
	}
	
	/**
	 * Obtain a String copy of the log.
	 * @return
	 */
	public String exportLog() {
		String toReturn = null;

		toReturn = compactArray(logger, "\n");

		return toReturn;
	}
	
	/*
	 * <notice>
	 * 
	 * 
	 * Networking code is below. Unless you are an advanced user, you can safely ignore the code below.
	 * 
	 * 
	 * </notice>
	 */
	
	/*
	 * File I/O code is below.
	 */
	
	/**
	 * Read in a text file.
	 * @param location The location of the file.
	 * @param commentBufferLineCount How many lines to ignore at the beginning of the file.
	 * @param hasComments If true, a line that starts with # is considered a comment, and hence is ignored.
	 * @param ignoreBlankLines If true, blank lines are ignored.
	 * @return The text file.
	 */
	public ArrayList<String> readFileAsList(String location, int commentBufferLineCount, boolean hasComments, boolean ignoreBlankLines) {
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
				if (hasComments && (line.length() > 0 && line.substring(0,1).equals("#"))) {
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
			logError("Error reading in list at: " + location);
		}
		return null;
	}
	
	/**
	 * Write a txt file.
	 * @param text The file text.
	 * @param location The file location.
	 */
	public void writeFile(String text, String location) {
		PrintWriter writer = null;
		
		try {
			logInfo("Writting file: " + location);
			writer = new PrintWriter(location, "UTF-8");
		} catch (FileNotFoundException e) {
			logError("File not found");
			return;
		} catch (UnsupportedEncodingException e) {
			logError("Unsupported file format");
			return;
		}
		writer.write(text);
		writer.close();
	}
	
	/*
	 * Networking code is below.
	 */
	
	/**
	 * @param ur The url you want to get.
	 * @param unescapeText Unescapes string literals. Ex: \n, \s, \ u
	 * @param unescapeHTML4 Unescapes HTML4 text. Ex: & #039;
	 */
	protected String[] getURL(String ur, boolean unescapeText, boolean unescapeHTML4) throws IOException {
		logFiner("Loading: " + ur);
		
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
        		inputLine = StringEscapeUtils.unescapeJava(inputLine);
        	}
        	if (unescapeHTML4) {
        		inputLine = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(inputLine));
        	}
        	
            page.add(inputLine);
        }
        in.close();
        
        return page.toArray(new String[page.size()]);
	}

	/*
	 * A method for creating a Web POST request.
	 */
	protected HttpEntity getPOST(String url, String[] key, String[] value) {
		logFiner("Loading: " + url);
		
        HttpResponse response = null;
		
        HttpPost httpost = new HttpPost(url);
    	
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (int i = 0; i < key.length; i++) {
        	nvps.add(new BasicNameValuePair(key[i], value[i]));
        }

        httpost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        try {
			response = httpclient.execute(httpost, context);
		} catch (SocketException|NoHttpResponseException e) {
			throw new NetworkError("Cannot connect to server at: " + url);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return response.getEntity();
	}
	
	/**
	 * Check if the url exists.
	 * @param url
	 * @return
	 */
	public int getResponseCode(String url) {
        URL oracle = null;
		try {
			oracle = new URL(url);
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
		}
		
		try {
			//Send a HEAD request. This is super fast way to just check if a page exists or not.
			HttpURLConnection huc = (HttpURLConnection) oracle.openConnection();
			huc.setRequestMethod("HEAD");
			
			return huc.getResponseCode();
		} catch (UnknownHostException e) {
			logError("Unkown host: " + url);
			return 401;
		} catch (SSLHandshakeException e) {
			return 401;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new Error("Something went wrong");
		}
	}
	
	public String URLencode(String url) {
		url = url.replace(" ", "_");
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String URLdecode(String url) {	
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Log these cookies using the log() method.
	 * @param cookies
	 */
	public void logCookies() {
		List<Cookie> cookies = context.getCookieStore().getCookies();
		
		logFinest("List of cookies: ");
		if (cookies.isEmpty()) {
			logFinest("None");
	    } else {
	        for (int i = 0; i < cookies.size(); i++) {
	            logFinest("- " + cookies.get(i).toString());
	           }
	    }
	}
	
	/*
	 * Parse text output for information
	 */
	
	protected ArrayList<String> parseTextForItems(String text, String openingText, String closingText, int botBuffer) {
		//This method takes text and parses it for data items, ex: page names.
		ArrayList<String> output = new ArrayList<String>();
		int j = 0;//cursor
		int k = -1;

		//Parse page for info.
		do {
			j = text.indexOf(openingText, k+1);
			k = text.indexOf(closingText, j+1);
			if (j != -1) {
				//No errors detected.
				output.add(text.substring(j+botBuffer, k));
			}
		} while(j != -1);
		return output;
	}
	
	protected String parseTextForItem(String text, String opening, String ending) {
		//This method takes text and parses it for a data item
		return parseTextForItem(text, opening, ending, 2, 0);
	}
	
	protected String parseTextForItem(String text, String opening, String ending, int bufferBot, int bufferTop) {
		//This method takes text and parses it for a data item
		int i = 0;
		i = text.indexOf(opening);
		if (i == -1) {
			throw new IndexOutOfBoundsException();
		}
		i += opening.length() + bufferBot;
		return text.substring(i, text.indexOf(ending, i) - bufferTop);
	}
	
	/*
	 * General utility code is below.
	 */
	
	public String compactArray(ArrayList<String> array) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (String item: array) {
			output+=item;
		}
		
		return output;
	}
	
	public String compactArray(String[] array) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (String item: array) {
			output+=item;
		}
		
		return output;
	}
	
	public String compactArray(ArrayList<String> array, String delimitor) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (int i = 0; i < array.size(); i++) {
			output+= array.get(i);
			if (i != array.size()-1) {
				output += delimitor;
			}
		}
		
		return output;
	}
	
	public String compactArray(String[] array, String delimitor) {
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
	
	/**
	 * This method gets a String timestamp in the format h:mm:ss.
	 * @return A String.
	 */
	public String getTimeStamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
		return sdf.format(date);
	}
}
