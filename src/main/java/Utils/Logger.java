package Utils;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * A custom logger class because other loggers cannot replicate what this does.
 */
public class Logger {
	
	private static Logger instance;
	
	private boolean propagating = false;

	private ArrayList<String> logger = new ArrayList<String>();
	private Level logLevel = Level.INFO;//Default;
	
	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		
		return instance;
	}
	
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
			String message = "[" + TimeUtils.getTimeStamp() + "]: " + line;
			logger.add(message);
			
			if (propagating) {
				System.out.println(message);
			}
			
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
	 * Set if logger propagates to Stdout.
	 * @param set A boolean.
	 */
	public void setPropagation(boolean set) {
		propagating = set;
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

		toReturn = ArrayUtils.compactArray(logger, "\n");

		return toReturn;
	}
	
	/**
	 * Print the full log.
	 * @return
	 */
	public void printLog() {
		for (String line : logger) {
			System.out.println(line);
		}
	}
}
