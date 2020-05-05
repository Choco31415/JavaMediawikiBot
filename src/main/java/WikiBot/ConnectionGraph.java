package WikiBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import WikiBot.ContentRep.PageLocation;

public class ConnectionGraph {
	Map<PageLocation, ArrayList<PageLocation>> knownConnections;
	ArrayList<PageLocation> unknownPages;
	boolean hasOverlappingLanguages = false;
	
	public ConnectionGraph() {
		knownConnections = new HashMap<>();
		unknownPages = new ArrayList<>();
	}
	
	/**
	 * Adds a page and its connections to this connection graph.
	 * @param pl
	 * @param connections
	 */
	public void addPage(PageLocation pl, ArrayList<PageLocation> linksTo) {
		// Avoid duplicate add
		if (!knownConnections.keySet().contains(pl)) {
			// Check if page language overlaps with others
			for (PageLocation knownPage : knownConnections.keySet()) {
				if (pl.getLanguage().equals(knownPage.getLanguage())) {
					hasOverlappingLanguages = true;
				}
			}
			
			// Add the page to our graph.
			knownConnections.put(pl, linksTo);
			
			// Remove page from unknown.
			if (unknownPages.contains(pl)) {
				unknownPages.remove(pl);
			}
			
			// Add unknown connections.
			for (PageLocation connection : linksTo) {
				if (!knownConnections.keySet().contains(connection)) {
					if (!unknownPages.contains(connection)) {
						unknownPages.add(connection);
					}
				}
			}
		}
	}
	
	/**
	 * Returns a list of pages missing data in the ConnectionGraph.
	 * @return
	 */
	public ArrayList<PageLocation> pagesToDownload() {
		return unknownPages;
	}
	
	/**
	 * Returns true if all linked to pages are known.
	 * @return
	 */
	public boolean isComplete() {
		return unknownPages.size() == 0;
	}
	
	/**
	 * Returns true if multiple pages in this wiki have the same language.
	 * @return
	 */
	public boolean hasOverlappingLanguages() {
		return hasOverlappingLanguages;
	}
	
	/**
	 * Get the full list of pages in this graph.
	 * @return The known pages.
	 */
	public Set<PageLocation> getKnownPages() {
		return knownConnections.keySet();
	}
	
	public ArrayList<PageLocation> getPageLinks(PageLocation pl) {
		return knownConnections.get(pl);
	}
	
	public void markAsRedirect(PageLocation from, PageLocation to) {
		if (unknownPages.contains(from) && !knownConnections.keySet().contains(to) && !unknownPages.contains(to)) {
			unknownPages.add(to);
		}
		if (unknownPages.contains(from)) {
			unknownPages.remove(from);
		}
		
		for (ArrayList<PageLocation> linksTo : knownConnections.values()) {
			// Check each known page's links.			
			if (linksTo.contains(from)) {
				linksTo.remove(from);
				linksTo.add(to);
			}
		}
	}
	
	/**
	 * Get the full list of pages that don't have every possible link.
	 * @return A list of pages.
	 */
	public ArrayList<PageLocation> getIncompletePages() {
		ArrayList<PageLocation> incomplete = new ArrayList<>();
		
		for (PageLocation page : knownConnections.keySet()) {
			// Check each known page.
			ArrayList<PageLocation> linksTo = knownConnections.get(page);
			
			if (linksTo.size() < knownConnections.size() - 1) {
				// This page does not have all possible links.
				incomplete.add(page);
			}
		}
		
		return incomplete;
	}
	
	@Override
	public String toString() {
		String toReturn = "Connection Graph\n";
		for (PageLocation page : knownConnections.keySet()) {
			// Get this page.
			ArrayList<PageLocation> linksTo = knownConnections.get(page);
			
			// Format this page
			toReturn += page + " -> ";
			for (PageLocation link : linksTo) {
				toReturn += link + ",";
			}
			toReturn += "\n";
		}
		toReturn += "unknown -> ";
		for (PageLocation unknown : unknownPages) {
			toReturn += unknown + ", ";
		}
		return toReturn;
	}
}
