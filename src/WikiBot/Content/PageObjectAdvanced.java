package WikiBot.Content;

import java.util.ArrayList;

public abstract class PageObjectAdvanced extends PageObject {
	//Name variables will be created in their respective classes.
	protected String header;
	protected ArrayList<PageObjectAdvanced> pageObjects = new ArrayList<PageObjectAdvanced>();
	protected ArrayList<String> parameters = new ArrayList<String>();

	public PageObjectAdvanced(String myObjectType_, String header_, int openPos_, int closePos_, ArrayList<String> parameters_, ArrayList<PageObjectAdvanced> pageObjects_, String textOpening_, String textClosing_) {
		super(myObjectType_, openPos_, closePos_, textOpening_, textClosing_);
		header = header_;
		pageObjects = pageObjects_;
		parameters = parameters_;
	}
	
	public PageObjectAdvanced(String myObjectType_, String header_, int openPos_, int closePos_, String textOpening_, String textClosing_) {
		super(myObjectType_, openPos_, closePos_, textOpening_, textClosing_);
		header = header_;
	}
	
	//Set information.
	public void addPageObjects(ArrayList<PageObjectAdvanced> po) {
		pageObjects.addAll(po);
	}
	
	public void addParameter(String param) {
		parameters.add(param);
	}
	
	//Get information.
	
	public String getHeader() {
		return header;
	}
	
	public int getNumPageObjects() {
		return pageObjects.size();
	}
	
	/**
	 * @return All page objects of a certain type.
	 */
	public int getNumPageObjectsOfType(String objectType) {
		int count = 0;
		for (PageObject object: pageObjects) {
			if (object.getObjectType().equalsIgnoreCase(objectType)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * @return Of the page objects this contains, get the number with this header.
	 */
	public int getNumPageObjectsByHeader(String header) {
		int count = 0;
		for (PageObjectAdvanced object: pageObjects) {
			if (object.getHeader().equalsIgnoreCase(header)) {
				count++;
			}
		}
		return count;
	}
	
	public PageObject getPageObject(int index) {
		return pageObjects.get(index);
	}
	
	/**
	 * @return Of the page objects this contains, get the objects with this header.
	 */
	public PageObjectAdvanced getPageObject(String header) {
		for (PageObjectAdvanced poa : pageObjects) {
			if (poa.getHeader().equalsIgnoreCase(header)) {
				return poa;
			}
		}
		return null;
	}
	
	/**
	 * @return Of the page objects this contains, get the objects with this header and of this object type.
	 */
	public PageObjectAdvanced getPageObject(String header, String objectType) {
		for (PageObjectAdvanced poa : pageObjects) {
			if (poa.getHeader().equalsIgnoreCase(header) && poa.getObjectType().equalsIgnoreCase(objectType)) {
				return poa;
			}
		}
		return null;
	}
	
	public ArrayList<PageObjectAdvanced> getAllPageObjects() {
		return pageObjects;
	}
	
	public String getParameter(int index) {
		return parameters.get(index);
	}
}
