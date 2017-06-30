package WikiBot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.swing.SortOrder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.OrderBy;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;
import com.google.common.base.CaseFormat;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import WikiBot.APIcommands.EditPage;
import WikiBot.APIcommands.EditSection;
import WikiBot.ContentRep.*;
import WikiBot.ContentRep.SiteInfo.SiteStatistics;
import WikiBot.Core.GenericBot;
import WikiBot.Utils.FileUtils;
import WikiBot.APIcommands.APIcommand;


public class GABot extends GenericBot {
	
	private static final long serialVersionUID = 1L;

	private static String username;
	private static String password;
	private static String botPropFile;
	
	private static GABot instance;
	
	private static String defaultStatsFile;
	
	private final static Map<String, String> wikiViews = new HashMap<>();
	private static String[] GAstatNames;
	private static String[] GAstatImageNames;
	private static String GAstatPage;
	
	private static int daysTracking;
	private static int topPageCount;
	
	private static final int GRAPH_WIDTH = 700;
	private static final int GRAPH_HEIGHT = 250;
	
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
		
		defaultStatsFile = "/GAPageDefault.txt";

		wikiViews.put("de", "63671826");//wiki to GA view id
		wikiViews.put("fr", "153835435");
		wikiViews.put("hu", "153842565");
		wikiViews.put("id", "153876466");
		wikiViews.put("ja", "153760467");
		wikiViews.put("nl", "153846344");
		//wikiViews.put("ru", "153914983");
		wikiViews.put("test", "150479279");
		GAstatNames = new String[]{"ga:pageviews", "ga:sessions"}; // GA statistics to collect.
		GAstatImageNames = new String[]{"InterwikiBot_GA_Pageviews.png", "InterwikiBot_GA_Sessions.png"}; // image names for statistics.
		
		daysTracking = 7;
		topPageCount = 50;
		
		setLogPropagation(true);
		
		if (instance == null) {
			instance = this;
		} else {
			throw new ConcurrentModificationException();//There should not be more then one GenericBot!!!
		}
	}
	
	/**
	 * Get an instance of GenericBot.
	 * If GenericBot has not been instantiated yet, the
	 * family and homeWikiLanguage are both set to null.
	 * @return
	 */
	public static GABot getInstance() {
		if (instance == null) {
			instance = new GABot();
		}
		
		return instance;
	}
	
	/*
	 * This is where I read in the bot password and create an instance.
	 */
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
	 */
	public void run() throws GeneralSecurityException, IOException {
		// Load username and password.
		loadPropFile();
		
		// Last minute configuration changes.
		GAstatPage = "User:" + username + "/GA Stats";
		
		// Run.
		AnalyticsReporting service = initializeAnalyticsReporting(KEY_FILE_LOCATION);
		String defaultStatsPage = readGAPageDefault();
		
		for (String wiki : wikiViews.keySet()) {
			try {
				// Log in.
				User user = new User(wiki, username);
				boolean loggedIn = logIn(user, password);
				
				if (!loggedIn) {
					throw new Error("Didn't log into wiki: " + wiki);
				} else {
					// Start querying and editing.
					String viewID = wikiViews.get(wiki);
					
					// Initialize stats page, if needed.
					PageLocation statsPageLoc = new PageLocation(wiki, GAstatPage);
					if (!this.doesPageExist(statsPageLoc)) {
						// Create the GA page.
						EditPage command = new EditPage(statsPageLoc, defaultStatsPage, "Creating page.");
						APIcommand(command);
						logInfo("Initialized page on wiki " + wiki + ".");
					}
					Page statsPage = getWikiPage(statsPageLoc);
					
					// Upload charts.
					String[] filenames = outputSiteStatisticsCharts(daysTracking, viewID, service);
					
					for (int i = 0; i < filenames.length; i++) {
						String filename = filenames[i];
						String imageName = GAstatImageNames[i];
						
						Path path = Paths.get(filename);
						PageLocation uploadTo = new PageLocation(wiki, imageName);
						
						this.uploadFile(uploadTo, path, "Updating statistic.", "A wiki statistic chart.");
						logInfo("Upload file " + filename + " to wiki " + wiki + ".");
					}
					
					// Check that the GA page has all necessary images.
					boolean hasImages = true;
					ArrayList<Image> images = statsPage.getImagesRecursive();
					int i = 0;
					while (hasImages && i < GAstatImageNames.length) {
						boolean hasImage = false;
						String imageName = GAstatImageNames[i];
						
						for (Image image : images) {
							if (image.getImageName().equals("File:" + imageName)) {
								hasImage = true;
							}
						}
						
						if (!hasImage) {
							hasImages = false;
						}
						i++;
					}
					
					if (!hasImages) {
						String sectionText = "== Site Statistics ==\n";
						
						for (String imageName : GAstatImageNames) {
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
				
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
	}
	
	public void loadPropFile() {
		ArrayList<String> properties = new ArrayList<>();
		properties.add("username");
		properties.add("password");
		
		ArrayList<String> values = FileUtils.readProperties(botPropFile, properties);
		
		username = values.get(0);
		password = values.get(1);
	}
	
	public String readGAPageDefault() {
		String text = "";
		
		ArrayList<String> defaultTextArray = FileUtils.readFileAsList(defaultStatsFile, 0, false, false);
		
		for (String line : defaultTextArray) {
			text += line + "\n";
		}
		
		return text;
	}

	private String[] outputSiteStatisticsCharts(int days, String viewID, AnalyticsReporting service) throws IOException, CloneNotSupportedException {
		String[] filenames = new String[GAstatNames.length];
		
		int[][] statData = getSiteStatisticsByDay(GAstatNames, days, viewID, service);
		
		for (int i = 0; i < GAstatNames.length; i++) {
			String statName = GAstatNames[i].substring(3);
			statName = statName.substring(0, 1).toUpperCase() + statName.substring(1);
			int[] data = statData[i];
			
			String filename = outputSiteStatisticChart(days, data, statName);
			filenames[i] = filename;
		}
		
		return filenames;
	}
	
	private String outputSiteStatisticChart(int days, int[] data, String statName) throws IOException, CloneNotSupportedException {
		// Generate our dataset.
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Calendar then = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin")); // For getting the date.
		then.add(Calendar.DAY_OF_MONTH, -days); // Go to the beginning.
		for (int datapoint : data) {
			String date = new SimpleDateFormat("MMM dd").format(then.getTime());
			dataset.addValue(datapoint, statName, date);
			then.add(Calendar.DAY_OF_MONTH, 1); // Advance a day.
		}

		JFreeChart lineChartObject = ChartFactory.createLineChart(
		         statName,
		         "","",
		         dataset,
		         PlotOrientation.VERTICAL,
		         true,true,false);
		lineChartObject.removeLegend();

		// Start configuring apperances.
		CategoryPlot plot = (CategoryPlot) lineChartObject.getPlot();	
		plot.setDataset(1, dataset); // For area renderer.
		
		// Plot background color, outline, and range gridlines.
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlineVisible(false);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		
		/*
		 * JFreeChart wants labels to not overlap.
		 * So we add padding to each label to reomve labels.
		 */
		NumberAxis range = (NumberAxis) plot.getRangeAxis();
		range.setTickLabelInsets(new RectangleInsets(0, 0, 16, 0));
		
		// Alter marins. Category margin specifically messes with the area fill.
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
		
		// Markers
		((LineAndShapeRenderer) lineR).setSeriesShapesVisible(0, true);
		int radius = 9;
		lineR.setSeriesShape(0, new Ellipse2D.Double(-radius/2, -radius/2, radius, radius));
		
		// Set renderer
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
	  
	static class PageViewTuple {
		private PageViewTuple() {
			  
		}
		  
		String name;
		int viewCount;
		    
		@Override
		public String toString(){
			return name + ": " + viewCount + " views";
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