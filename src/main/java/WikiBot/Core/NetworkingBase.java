package WikiBot.Core;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
class NetworkingBase extends javax.swing.JPanel {
	
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
	
	/**
	 * @param ur The url you want to get.
	 * @param unescapeText Unescapes string literals. Ex: \n, \s, \ u
	 * @param unescapeHTML4 Unescapes HTML4 text. Ex: &#039;
	 */
	protected String[] getURL(String ur, boolean unescapeText, boolean unescapeHTML4) throws IOException {
		logInfo("Loading: " + ur);
		
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
        HttpResponse response = null;
		
        HttpPost httpost = new HttpPost(url);
    	
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (int i = 0; i < key.length; i++) {
        	nvps.add(new BasicNameValuePair(key[i], value[i]));
        }

        httpost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        try {
			response = httpclient.execute(httpost, context);
		} catch (SocketException e) {
			throw new NetworkError("Cannot connect to server at: " + url);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return response.getEntity();
	}
	
	public String URLencode(String url) {
		url = url.replace(" ", "_");
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
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
