package WikiBot.Core;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import WikiBot.Utils.Logger;

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
	private Logger logger = Logger.getInstance();
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
		return logger.log(level, line);
	}
	
	/**
	 * Set how fine you want the log.
	 * Refer to the comment at the top of NetworkingBase to see what each level corresponds to.
	 * 
	 * @param level The fineness level.
	 */
	public void setLoggerLevel(Level level) {
		logger.setLoggerLevel(level);
	}
	
	/**
	 * Get the most recent logger line.
	 * @return
	 */
	public String getNewestLoggerLine() {
		return logger.getNewestLoggerLine();
	}
	
	/**
	 * Obtain a String copy of the log.
	 * @return
	 */
	public String exportLog() {
		return logger.exportLog();
	}
	
	/**
	 * Set if logger propagates to Stdout.
	 * @param set
	 */
	public void setLogPropagation(boolean set) {
		logger.setPropagation(set);
	}
	
	/*
	 * <notice>
	 * 
	 * 
	 * Class code is below. Unless you are an advanced user, you can safely ignore the code below.
	 * 
	 * 
	 * </notice>
	 */
	
	/*
	 * Networking code is below.
	 */
	
	/**
	 * @param ur The url you want to get.
	 * @param unescapeHTML4 Unescapes HTML4 text. Ex: & #039;
	 */
	protected String[] getURL(String ur, boolean unescapeHTML4) throws IOException {
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
	protected HttpEntity getPOST(String url, String[] keys, String[] values) {
		logFiner("Loading: " + url);
		
        HttpResponse response = null;
		
        HttpPost httpost = new HttpPost(url);
    	
        // Attach keys and values.
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (int i = 0; i < keys.length; i++) {
        	nvps.add(new BasicNameValuePair(keys[i], values[i]));
        }

        httpost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
        
        // Fetch the url.
        try {
			response = httpclient.execute(httpost, context);
		} catch (SocketException|NoHttpResponseException e) {
			throw new NetworkError("Cannot connect to server at: " + url);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return response.getEntity();
	}
	
	protected HttpEntity getPOST(String url, HttpEntity entity) {
		logFiner("Multipart loading: " + url);
		
		HttpResponse response = null;
		
		HttpPost httpost = new HttpPost(url);
		httpost.setEntity(entity);
		
		//Fetch the url.
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
	 * Get the http response for a page.
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
	
	protected String parseTextForItem(String text, String opening, String closing) {
		//This method takes text and parses it for a data item
		int start = 0;
		start = text.indexOf(opening);
		if (start == -1) {
			throw new IndexOutOfBoundsException();
		}
		start += opening.length() + 2; // Buffer of 2 for most data formats.
		
		int end = text.indexOf(closing, start);
		
		return text.substring(start, end);
	}
}