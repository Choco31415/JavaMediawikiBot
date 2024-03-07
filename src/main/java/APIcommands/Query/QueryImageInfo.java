package APIcommands.Query;

import java.util.ArrayList;

import APIcommands.APIcommand;
import ContentRep.PageLocation;

/**
 * @Description
 * This command gets info about an image.
 * 
 * All query-able properties can be found here:
 * https://www.mediawiki.org/wiki/API:Imageinfo
 * 
 * Recommended not used.
 * 
 * @RequiredRights
 * none
 * 
 * @MediawikiSupport
 * Minimum:
 * 1.12+
 * 
 * Specifics:
 * Support for certain properties was only introduced in later versions of mediawiki.
 * The support table is:
 * userid: 1.17+
 * parseDocument: 1.17+
 * canonicalTitle: 1.23+
 * dimensions: 1.16+
 * mime: 1.13+
 * thumbmime: 1.17+
 * mediatype: 1.18+
 * metadata: 1.12+
 * commonmetadata: 1.23+
 * extmetadata: 1.23+
 * archivename: 1.13+
 * bitdepth: 1.14+
 */
public class QueryImageInfo extends APIcommand {
	/**
	 * Please checkout the following page to ensure
	 * that the properties you are getting are supported
	 * by the MW version of your wiki:
	 * https://www.mediawiki.org/wiki/API:Imageinfo
	 * 
	 * @param loc The image to get info about.
	 * @param propertiesToGet The image properties you are querying.
	 */
	public QueryImageInfo(PageLocation loc, ArrayList<String> propertiesToGet) {
		super("Query image info", loc);
		keys.add("format");
		values.add("json");
		keys.add("action");
		values.add("query");
		keys.add("titles");
		values.add(loc.getTitle());
		keys.add("prop");
		values.add("imageinfo");
		keys.add("iiprop");
		values.add(compactArray(propertiesToGet, "|"));
		
		enforceMWVersion("1.11");
		
		localEnforce(propertiesToGet);
	}
	
	private void localEnforce(ArrayList<String> propertiesToGet) {
		//MW requirements and such
		if (propertiesToGet.contains("userid")) {
			enforceMWVersion("1.17");
		}
		if (propertiesToGet.contains("parseDocument")) {
			enforceMWVersion("1.17");
		}
		if (propertiesToGet.contains("canonicaltitle")) {
			enforceMWVersion("1.23");
		}
		if (propertiesToGet.contains("dimensions")) {
			enforceMWVersion("1.16");
		}
		if (propertiesToGet.contains("mime")) {
			enforceMWVersion("1.13");
		}
		if (propertiesToGet.contains("thumbmime")) {
			enforceMWVersion("1.17");
		}
		if (propertiesToGet.contains("mediatype")) {
			enforceMWVersion("1.18");
		}
		if (propertiesToGet.contains("metadata")) {
			enforceMWVersion("1.12");
		}
		if (propertiesToGet.contains("commonmetadata")) {
			enforceMWVersion("1.23");
		}
		if (propertiesToGet.contains("extmetadata")) {
			enforceMWVersion("1.12");
		}
		if (propertiesToGet.contains("archivename")) {
			enforceMWVersion("1.13");
		}
		if (propertiesToGet.contains("bitdepth")) {
			enforceMWVersion("1.14");
		}
	}
}
