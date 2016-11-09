package WikiBot.Utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import javax.imageio.ImageIO;

public class FileUtils {
	
	private static Logger logger = Logger.getInstance();//For logging purposes.

	/**
	 * Read in a text file.
	 * @param path The path to the file.
	 * @param commentBufferLineCount How many lines to ignore at the beginning of the file.
	 * @param hasComments If true, a line that starts with # is considered a comment, and hence is ignored.
	 * @param ignoreBlankLines If true, blank lines are ignored.
	 * @return The text file.
	 */	
	public static ArrayList<String> readFileAsList(String path, int commentBufferLineCount, boolean hasComments, boolean ignoreBlankLines) {
		return readFileAsList(path, commentBufferLineCount, hasComments, "#", ignoreBlankLines);
	}
	/**
	 * Read in a text file.
	 * @param path The path to the file.
	 * @param commentBufferLineCount How many lines to ignore at the beginning of the file.
	 * @param hasComments If true, a line that starts with # is considered a comment, and hence is ignored.
	 * @param commentHeader The header of a comment.
	 * @param ignoreBlankLines If true, blank lines are ignored.
	 * @return The text file.
	 */	
	public static ArrayList<String> readFileAsList(String path, int commentBufferLineCount, boolean hasComments, String commentHeader, boolean ignoreBlankLines) {
		try {
			// Read in the file!
			InputStream in = FileUtils.class.getResourceAsStream(path);
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
				if (hasComments && (line.length() > commentHeader.length() && line.substring(0,commentHeader.length()).equals(commentHeader))) {
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
			logger.logError("Error reading in list.");
		}
		return null;
	}
	
	/**
	 * Write a txt file.
	 * @param text The file text.
	 * @param path The path to the file.
	 */
	public static void writeFile(String text, String path) {
		PrintWriter writer = null;
		
		try {
			logger.logInfo("Writting file: " + path);
			writer = new PrintWriter(path, "UTF-8");
		} catch (FileNotFoundException e) {
			logger.logError("File not found");
			return;
		} catch (UnsupportedEncodingException e) {
			logger.logError("Unsupported file format");
			return;
		}
		writer.write(text);
		writer.close();
	}
	
	/**
	 * Read in an image.
	 * @param path The path to the image
	 * @return
	 */
	public static BufferedImage readImage(String path) {
		BufferedImage toReturn = null;
		
		try {
			toReturn = ImageIO.read(FileUtils.class.getResourceAsStream(path));
		} catch (IOException e) {
			throw new Error("Could not read image: " + path);
		}
		
		return toReturn;
	}
	
	/**
	 * Read in a .properties file. Thanks to Java for the code.
	 * 
	 * @param path The path to the properties file.
	 * @param properties The properties to read in.
	 * @return The values of the properties, in the same order as passed in.
	 */
	public static ArrayList<String> readProperties(String path, ArrayList<String> properties) {
		Properties prop = new Properties();
		InputStream in = null;
		
		ArrayList<String> values = new ArrayList<String>();

		try {

			in = FileUtils.class.getResourceAsStream(path);

			// Load a properties file.
			prop.load(in);

			// Read in each value one by one.
			for (String property : properties) {
				values.add(prop.getProperty(property));
			}

		} catch (IOException ex) {
			throw new Error("Could not read property file: " + path);
		}
		
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return values;
	}
}
