package WikiBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import WikiBot.ContentRep.PageLocation;

public class ConnectionGraph {
	Map<PageLocation, ArrayList<PageLocation>> knownPagesToLinks;
	ArrayList<PageLocation> unknownPages;
	boolean hasOverlappingLanguages = false;
	
	public ConnectionGraph() {
		knownPagesToLinks = new HashMap<>();
		unknownPages = new ArrayList<>();
	}
	
	/**
	 * Adds a page and its connections to this connection graph.
	 * @param pl
	 * @param connections
	 */
	public void addPage(PageLocation pl, ArrayList<PageLocation> linksTo) {
		if (!knownPagesToLinks.keySet().contains(pl)) {
			// Check if page overlaps with others
			for (PageLocation knownPage : knownPagesToLinks.keySet()) {
				if (pl.getLanguage().equals(knownPage.getLanguage())) {
					hasOverlappingLanguages = true;
				}
			}
			
			// Add the page to our graph.
			knownPagesToLinks.put(pl, linksTo);
			
			// Remove page from unknown.
			if (unknownPages.contains(pl)) {
				unknownPages.remove(pl);
			}
			
			// Add unknown connections.
			for (PageLocation connection : linksTo) {
				if (!knownPagesToLinks.keySet().contains(connection)) {
					if (!unknownPages.contains(connection)) {
						unknownPages.add(connection);
					}
				}
			}
		}
	}
	
	/**
	 * Returns a linked to page that is not yet included in this graph.
	 * @return
	 */
	public ArrayList<PageLocation> getNonincludedLinkedPages() {
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
		return knownPagesToLinks.keySet();
	}
	
	public ArrayList<PageLocation> getPageLinks(PageLocation pl) {
		return knownPagesToLinks.get(pl);
	}
	
	public void markAsRedirect(PageLocation from, PageLocation to) {
		if (unknownPages.contains(from) && !knownPagesToLinks.keySet().contains(to)) {
			unknownPages.add(to);
		}
		while (unknownPages.contains(from)) {
			// Remove all of the redirect.
			unknownPages.remove(from);
		}
		
		
		for (ArrayList<PageLocation> linksTo : knownPagesToLinks.values()) {
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
		
		for (PageLocation page : knownPagesToLinks.keySet()) {
			// Check each known page.
			ArrayList<PageLocation> linksTo = knownPagesToLinks.get(page);
			
			if (linksTo.size() < knownPagesToLinks.size() - 1) {
				// This page does not have all possible links.
				incomplete.add(page);
			}
		}
		
		return incomplete;
	}
	
	@Override
	public String toString() {
		String toReturn = "Connection Graph\n";
		for (PageLocation page : knownPagesToLinks.keySet()) {
			// Get this page.
			ArrayList<PageLocation> linksTo = knownPagesToLinks.get(page);
			
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
