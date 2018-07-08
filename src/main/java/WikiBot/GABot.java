package WikiBot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.OrderBy;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

import WikiBot.APIcommands.EditPage;
import WikiBot.APIcommands.EditSection;
import WikiBot.ContentRep.*;
import WikiBot.Core.GenericBot;
import WikiBot.Utils.FileUtils;


public class GABot extends GenericBot {
	
	private static final long serialVersionUID = 1L;

	private static String username;
	private static String password;
	private static String botPropFile; // Where username + password are stored.
	
	private static GABot instance; // Bot instantiates itself to avoid making every method static.
	
	private final static Map<String, String> wikiViews = new HashMap<>(); //  Maps wiki to view id
	private static String[] GAvisualStats; // stats to track visually
	private static String[] GAvisualStatsFilenames; // images names of stast to track visually
	
	private static int daysTracking; // Number of days to track
	private static int topPageCount; // Number of top pages to track.
	
	private static final int GRAPH_WIDTH = 700;
	private static final int GRAPH_HEIGHT = 250;
	
	private static String defaultStatsPage; // Default page for new stats page on a wiki.
	private static String GAstatPage; // Page name for stats page on a wiki
	
	private static String timezone;
	
	// For Google Analytics
	private static final String KEY_FILE_LOCATION = "/InterwikiService.json";
	private static final String APPLICATION_NAME = "interwiki";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	
	/*
	 * This is where I initialize my custom Mediawiki bot.
	 */
	public GABot() {
		super("Scratch", "en");
		
		//Preferences
		APIlimit = 30;//The amount of items to get per query call, if there are multiple items.
		getRevisions = false;//Don't get page revisions.
		
		APIthrottle = 0.5;//Minimum time between any API commands.
		
		setLoggerLevel(Level.FINE);//How fine should the logger be? Visit NetworkingBase.java for logger level info.
		
		botPropFile = "/BotProperties.properties";
		
		defaultStatsPage = "/GAPageDefault.txt";

		wikiViews.put("de", "63671826");// wiki and its corresponding view id.
		wikiViews.put("en", "172738445");
		wikiViews.put("fr", "153835435");
		wikiViews.put("hu", "153842565");
		wikiViews.put("id", "153876466");
		wikiViews.put("ja", "153760467");
		wikiViews.put("nl", "153846344");
		wikiViews.put("ru", "154005849");
		wikiViews.put("test", "150479279");
		GAvisualStats = new String[]{"ga:pageviews", "ga:sessions"}; 
		GAvisualStatsFilenames = new String[]{"InterwikiBot_GA_Pageviews.png", "InterwikiBot_GA_Sessions.png"}; 
		
		daysTracking = 7;
		topPageCount = 50;
		
		timezone = "Europe/Berlin";
		
		setLogPropagation(true);
		
		if (instance == null) {
			instance = this;
		} else {
			throw new ConcurrentModificationException();//There should not be more then one GenericBot!!!
		}
	}
	
	/**
	 * Get an instance of GenericBot.
	 * @return
	 */
	public static GABot getInstance() {
		if (instance == null) {
			instance = new GABot();
		}
		
		return instance;
	}
	
	public static void main(String[] args) throws GeneralSecurityException, IOException, CloneNotSupportedException {
		GABot b = getInstance();
		
		b.run();
	}
	
	/**
	 * This is the entry point for the Object portion of the bot.
	 * 
	 * @param botPassword The password of the bot.
	 * @param delay The amount of seconds to delay checks.
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 * @throws CloneNotSupportedException 
	 */
	public void run() throws GeneralSecurityException, IOException, CloneNotSupportedException {
		// Load username and password.
		loadPropFile();
		
		// Last minute configuration changes. Done here due to username requirement.
		GAstatPage = "User:" + username + "/GA Stats";
		
		// Run.
		AnalyticsReporting service = initializeAnalyticsReporting(KEY_FILE_LOCATION);
		String newStatsPageText = readGApageDefault();
		
		// For each wiki...
		for (String wiki : wikiViews.keySet()) {
			// Log in.
			User user = new User(wiki, username);
			boolean loggedIn = logIn(user, password);
			
			if (!loggedIn) {
				throw new Error("Didn't log into wiki: " + wiki);
			} else {
				String viewID = wikiViews.get(wiki);
				
				// Initialize stats page, if needed.
				PageLocation statsPageLoc = new PageLocation(wiki, GAstatPage);
				if (!this.doesPageExist(statsPageLoc)) {
					// Create the GA page.
					EditPage command = new EditPage(statsPageLoc, newStatsPageText, "Creating page.");
					APIcommand(command);
					logInfo("Initialized page on wiki " + wiki + ".");
				}
				
				// Read in page for updating.
				Page statsPage = getWikiPage(statsPageLoc);
				
				// Upload charts.
				String[] filenames = gatherSiteStatisticsCharts(daysTracking, viewID, service);
				
				for (int i = 0; i < filenames.length; i++) {
					String localFilename = filenames[i];
					String wikiFilename = GAvisualStatsFilenames[i];
					
					Path path = Paths.get(localFilename);
					PageLocation uploadTo = new PageLocation(wiki, wikiFilename);
					
					this.uploadFile(uploadTo, path, "Updating statistic.", "A wiki statistic chart.");
					logInfo("Upload file " + localFilename + " to wiki " + wiki + ".");
				}
				
				// Check that the GA page has all necessary images.
				boolean hasImages = true;
				ArrayList<Image> images = statsPage.getImagesRecursive();
				int i = 0;
				while (hasImages && i < GAvisualStatsFilenames.length) {
					// Search page for image GAvisualStatsFilenames[i]
					boolean hasImage = false;
					String imageName = GAvisualStatsFilenames[i];
					
					for (Image image : images) {
						if (image.getImageName().equals("File:" + imageName)) {
							hasImage = true;
						}
					}
					
					if (!hasImage) {
						// Image not found.
						hasImages = false;
					}
					i++;
				}
				
				if (!hasImages) {
					// Missing image(s) detected. Replace section 1.
					String sectionText = "== Site Statistics ==\n";
					
					for (String imageName : GAvisualStatsFilenames) {
						sectionText += "[[File:" + imageName + "]]\n\n";
					}
					
					EditSection command = new EditSection(statsPageLoc, 1, sectionText, "Editing statistics tracked.");
					APIcommand(command);
					logInfo("Edited statistics images on wiki " + wiki + ".");
				}
				
				// Append popular pages.
				PageViewTuple[] pvt = getPopularPages(topPageCount, viewID, service);
				
				String sectionText = "== Popular Pages ==\n"
						+ "Note: Page view counts are for the most recent 30 day period.\n";
				for (PageViewTuple tuple : pvt) {
					if (tuple != null) {
						if (tuple.name.contains(".php")) {
							sectionText += "\n#  " + tuple.name + " (" + tuple.viewCount + " views)";
						} else {
							sectionText += "\n#  [[" + tuple.name + "]] (" + tuple.viewCount + " views)";
						}
					}
				}
				
				EditSection command = new EditSection(statsPageLoc, 2, sectionText, "Updating popular pages.");
				APIcommand(command);
				logInfo("Updated popular pages on wiki " + wiki + ".");
			}
		}
	}
	
	/**
	 * Load the bot properties file. Contains username and password.
	 */
	public void loadPropFile() {
		ArrayList<String> properties = new ArrayList<>();
		properties.add("username");
		properties.add("password");
		
		ArrayList<String> values = FileUtils.readProperties(botPropFile, properties);
		
		username = values.get(0);
		password = values.get(1);
	}
	
	/**
	 * Read in the default GA wiki page text.
	 * @return
	 */
	public String readGApageDefault() {
		String text = "";
		
		ArrayList<String> defaultTextArray = FileUtils.readFileAsList(defaultStatsPage, 0, false, false);
		
		for (String line : defaultTextArray) {
			text += line + "\n";
		}
		
		return text;
	}

	/***
	 * From the given {@code service} and {@code viewID}, graph GAvisualStats for the past {@code days} days.
	 * @param days Days to graph for.
	 * @param viewID ViewID to collect stats on.
	 * @param service Service with access to given viewID.
	 * @return Filenames of graphs.
	 * @throws IOException
	 * @throws CloneNotSupportedException
	 */
	private String[] gatherSiteStatisticsCharts(int days, String viewID, AnalyticsReporting service) throws IOException, CloneNotSupportedException {
		String[] filenames = new String[GAvisualStats.length];
		
		int[][] statData = getSiteStatisticsByDay(GAvisualStats, days, viewID, service);
		
		for (int i = 0; i < GAvisualStats.length; i++) {
			String statName = GAvisualStats[i].substring(3);
			statName = statName.substring(0, 1).toUpperCase() + statName.substring(1);
			int[] data = statData[i];
			
			String filename = outputSiteStatisticChart(days, data, statName);
			filenames[i] = filename;
		}
		
		return filenames;
	}
	
	/**
	 * Given {@code data}, where a stat {@code statName} is tracked for the past {@code days} days, graph it.
	 * @param days The number of days the stat {@code statName} is tracked.
	 * @param data The data.
	 * @param statName The stat being tracked.
	 * @return A filename of the graph.
	 * @throws IOException
	 * @throws CloneNotSupportedException
	 */
	private String outputSiteStatisticChart(int days, int[] data, String statName) throws IOException, CloneNotSupportedException {
		// Generate the graph dataset.
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		// Input data to dataset.
		Calendar then = Calendar.getInstance(TimeZone.getTimeZone(timezone)); // For getting the date.
		then.add(Calendar.DAY_OF_MONTH, -days); // Go to the beginning.
		for (int datapoint : data) {
			String date = new SimpleDateFormat("MMM dd").format(then.getTime());
			dataset.addValue(datapoint, statName, date);
			then.add(Calendar.DAY_OF_MONTH, 1); // Advance a day.
		}

		// Create chart based on dataset.
		JFreeChart lineChartObject = ChartFactory.createLineChart(
		         statName,
		         "","",
		         dataset,
		         PlotOrientation.VERTICAL,
		         true,true,false);
		lineChartObject.removeLegend(); // Annoying.

		// Start configuring appearances.
		CategoryPlot plot = (CategoryPlot) lineChartObject.getPlot();	
		plot.setDataset(1, dataset); // For area renderer.
		
		// Plot background color, outline, and range gridlines.
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlineVisible(false);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		
		/*
		 * JFreeChart doesn't space y-axis labels nicely.
		 * So we add padding to each label.
		 */
		NumberAxis range = (NumberAxis) plot.getRangeAxis();
		range.setTickLabelInsets(new RectangleInsets(0, 0, 16, 0));
		
		// Alter margins. Category margin specifically messes with the area fill.
		CategoryAxis domain = plot.getDomainAxis();
		domain.setCategoryMargin(0);
		domain.setLowerMargin(-0.05); // Left margin
		domain.setUpperMargin(-0.05); // Right marin
		
		// Make renderer
		CategoryItemRenderer lineR = plot.getRenderer();
		AreaRenderer areaR = new AreaRenderer();

		// Fill
		areaR.setEndType(AreaRendererEndType.TRUNCATE);
		areaR.setSeriesPaint(0, new Color(5, 141, 199, 100));
		
		
		// Line
		lineR.setSeriesPaint(0, new Color(5, 141, 199)); 
		lineR.setSeriesStroke(0, new BasicStroke(4));
		
		// Circle markers
		((LineAndShapeRenderer) lineR).setSeriesShapesVisible(0, true);
		int diameter = 9;
		lineR.setSeriesShape(0, new Ellipse2D.Double(-diameter/2, -diameter/2, diameter, diameter));
		
		// Set dual renderers
		plot.setRenderer(0, areaR);
		plot.setRenderer(1, lineR);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		
		// Export!!
		int width = GRAPH_WIDTH;    /* Width of the image */
	    int height = GRAPH_HEIGHT;   /* Height of the image */ 
	    String filename = statName + ".png";
	    File file = new File(filename); 
	    ChartUtilities.saveChartAsPNG(file, lineChartObject, width ,height);
	    
	    return file.getAbsolutePath();
	}
	
	/**
	 * Initializes an Analytics Reporting API V4 service object.
	 *
	 * @return An authorized Analytics Reporting API V4 service object.
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private static AnalyticsReporting initializeAnalyticsReporting(String keyFileLocation) throws GeneralSecurityException, IOException {

	    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
	    GoogleCredential credential = GoogleCredential
	        .fromStream(FileUtils.class.getResourceAsStream(keyFileLocation))
	        .createScoped(AnalyticsReportingScopes.all());

	    // Construct the Analytics Reporting service object.
	    return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
	        .setApplicationName(APPLICATION_NAME).build();
	}
	  
	/**
	 * Quer {@code statistics} for the past {@code day} days from view {@code viewID}, accessible from service {@code AnalyticsReporting}.
	 * @param statistics The GA statistics to query.
	 * @param days The number of days to retrieve data on.
	 * @param viewID The view's ID of the website.
	 * @param service An AnalyticsService object that can access the current view.
	 * @return An array of page view counts.
	 * @throws IOException 
	 */
	private static int[][] getSiteStatisticsByDay(String[] statistics, int days, String viewID, AnalyticsReporting service) throws IOException {
		int[][] toReturn = new int[statistics.length][days];
			  
		// Create the DateRange object.
		DateRange dateRange = new DateRange();
		dateRange.setStartDate("7DaysAgo");
		dateRange.setEndDate("1DaysAgo");

		// Create the Metrics object.
		List<Metric> metrics = new ArrayList<Metric>();
		for (String statistic : statistics) {
			Metric metric = new Metric()
					.setExpression(statistic); // Get this statistic.
			metrics.add(metric);
		}
		GAstatPage = "User:" + username + "/GA Stats";
		// Get statistics organized by day.
		Dimension pages = new Dimension()
		    		.setName("ga:nthDay");

		// Create the ReportRequest object.
		ReportRequest request = new ReportRequest()
		        .setViewId(viewID)
		        .setDateRanges(Arrays.asList(dateRange))
		        .setMetrics(metrics)
		        .setDimensions(Arrays.asList(pages));

		ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
		requests.add(request);

		// Create the GetReportsRequest object.
		GetReportsRequest getReport = new GetReportsRequest()
		        .setReportRequests(requests);

		// Call the batchGet method.
		GetReportsResponse response = service.reports().batchGet(getReport).execute();
		    
		// Traverse the JSON via Google's custom classes.
		Report report = response.getReports().get(0);
		List<ReportRow> rows = report.getData().getRows();
		  
		int reportedDays = rows.size();
		int missingDays = days - reportedDays;
		for (int i = 0; i < reportedDays; i++) {
			ReportRow row = rows.get(i);
			    
			List<DateRangeValues> metricsTag = row.getMetrics();
			    
			DateRangeValues values = metricsTag.get(0);
			for (int statID = 0; statID < statistics.length; statID++) {
				toReturn[statID][i+missingDays] = Integer.parseInt(values.getValues().get(statID));
			}
		}
		  
		return toReturn;
	}
	  
	/**
	 * A class for combining page name with its view count.
	 * @author ErnieParke
	 *
	 */
	static class PageViewTuple {
		private PageViewTuple() {
			  
		}
		  
		String name;
		int viewCount;
		    
		@Override
		public String toString(){
			return name + " : " + viewCount + " views";
		}
	}
	  
	/**
	 * Query the top {@code count} popular pages of view {@code viewID}, accessible from service {@code service}.
	 * @param count The number of pages to get.
	 * @param viewID The view's ID of the website.
	 * @param service An AnalyticsService object.
	 * @return An array of pageViewTuple.
	 * @throws IOException 
	 */
	private PageViewTuple[] getPopularPages(int count, String viewID, AnalyticsReporting service) throws IOException {
	    PageViewTuple[] toReturn = new PageViewTuple[count];
	  
	    // Create the DateRange object.
	    DateRange dateRange = new DateRange();
	    dateRange.setStartDate("30daysAgo");
	    dateRange.setEndDate("1daysAgo");

	    // Create the Metrics object.
	    Metric metric = new Metric()
	        .setExpression("ga:pageviews")
	        .setAlias("pageviews"); // Get the expression statistic, but return it named as alias.
	    Metric nMetric = new Metric()
		        .setExpression("-ga:pageviews");
	    
	    Dimension pages = new Dimension()
	    		.setName("ga:PagePath");
	    
	    OrderBy order = new OrderBy()
	    		.setFieldName("-ga:pageviews");
	    

	    // Create the ReportRequest object.
	    ReportRequest request = new ReportRequest()
	        .setViewId(viewID)
	        .setDateRanges(Arrays.asList(dateRange))
	        .setMetrics(Arrays.asList(metric, nMetric))
	        .setDimensions(Arrays.asList(pages))
	        .setOrderBys(Arrays.asList(order))
	        .setFiltersExpression("ga:PagePath!@&"); // pagePath !contains &

	    ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
	    requests.add(request);

	    // Create the GetReportsRequest object.
	    GetReportsRequest getReport = new GetReportsRequest()
	        .setReportRequests(requests);

	    // Call the batchGet method.
	    GetReportsResponse response = service.reports().batchGet(getReport).execute();
	    
	    
	    // Traverse the JSON via Google's custom classes.
	    Report report = response.getReports().get(0);
	    List<ReportRow> rows = report.getData().getRows();
	  
	    int rowID = 0;
	    int pvtID = 0;//pvt = PageViewTuple
	    while (pvtID < count) {
	    	if (rowID < rows.size()) {
			    ReportRow row = rows.get(rowID);
			  
			    List<String> dimensions = row.getDimensions();
			    List<DateRangeValues> metrics = row.getMetrics();
			  
			    PageViewTuple pvt = new PageViewTuple();
			    String path = dimensions.get(0);
			    if (!path.equals("/")) {
			    	//It's safe to split the page name out of the path.
			    	String[] halves = path.split("wiki/");
				    if (halves.length == 1) {
				    	pvt.name = path;
				    	
				    	rowID++; // disabled
				    	continue; // disabled
				    } else {
				    	pvt.name = halves[1];
				    }
				    DateRangeValues values = metrics.get(0);
				    pvt.viewCount = Integer.parseInt(values.getValues().get(0));
				  
				    // Store for later.
				    toReturn[pvtID] = pvt;
				    pvtID++;
			    }
			    rowID++;
	    	} else {
	    		pvtID = count; // Break out of the loop.
	    	}   	
	    }
	  
	  	return toReturn;
	}

}
