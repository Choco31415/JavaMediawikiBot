package WikiBot.ContentRep;

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
	
	/*
	 * Set information methods.
	 */
	
	public void addPageObjects(ArrayList<PageObjectAdvanced> po) {
		pageObjects.addAll(po);
	}
	
	public void addParameter(String param) {
		parameters.add(param);
	}
	
	public void setParameter(int index, String param) {
		parameters.set(index, param);
	}
	
	/*
	 * Get information methods.
	 */
	/**
	 * Gets the header of the page object.
	 * For templates, it would be the template name.
	 * For images, it would be the image.
	 * So on.
	 * @return header
	 */
	public String getHeader() {
		return header;
	}
	
	/**
	 * @return The number of pageObjects this object contains.
	 */
	public int getNumPageObjects() {
		return pageObjects.size();
	}
	
	/**
	 * @return All page objects of a certain type.
	 */
	public int getNumPageObjectsOfType(String objectType) {
		int count = 0;
		for (PageObject object: pageObjects) {
			//Check that object type matches.
			if (object.getObjectType().equalsIgnoreCase(objectType)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Of the page objects this contains, get the count of objects with this header.
	 * @return A count.
	 */
	public int getNumPageObjectsByHeader(String header) {
		int count = 0;
		for (PageObjectAdvanced object: pageObjects) {
			//Check that trimmed header matches.
			if (object.getHeader().trim().equalsIgnoreCase(header.trim())) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Of the page objects this contains, get the object with this id.
	 * @param index The id of this page object.
	 * @return A pageObject.
	 */
	public PageObject getPageObject(int index) {
		return pageObjects.get(index);
	}
	
	/**
	 * Of the page objects this contains, get the first object with this header.
	 * @return A PageObjectAdvanced.
	 */
	public PageObjectAdvanced getPageObject(String header) {
		for (PageObjectAdvanced poa : pageObjects) {
			//Check that trimmed header matches.
			if (poa.getHeader().trim().equalsIgnoreCase(header.trim())) {
				return poa;
			}
		}
		return null;
	}
	
	/**
	 * Of the page objects this contains, get the first object with this header and of this object type.
	 * @return A PageObjectAdvanced.
	 */
	public PageObjectAdvanced getPageObject(String header, String objectType) {
		for (PageObjectAdvanced poa : pageObjects) {
			//Check that trimmed header matches.
			if (poa.getHeader().trim().equalsIgnoreCase(header.trim())) {
				//Check that object type matches.
				if (poa.getObjectType().equalsIgnoreCase(objectType)) {
					return poa;
				}
			}
		}
		return null;
	}
	
	/**
	 * Of the page objects this contains, get the objects with this object type.
	 * @return An ArrayList of PageObjectAdvanced.
	 */
	public ArrayList<PageObjectAdvanced> getPageObjectOfType(String objectType) {
		ArrayList<PageObjectAdvanced> toReturn = new ArrayList<PageObjectAdvanced>();
		
		for (PageObjectAdvanced poa : pageObjects) {
			//Check that object type matches.
			if (poa.getObjectType().equalsIgnoreCase(objectType)) {
				toReturn.add(poa);
			}
		}
		
		return toReturn;
	}
	
	/**
	 * Return all PageObjectAdvanced in this object, not recursively.
	 * @return An ArrayList of PageObjectAdvanced.
	 */
	public ArrayList<PageObjectAdvanced> getAllPageObjects() {
		return pageObjects;
	}
	
	/**
	 * Return all PageObjectAdvanced in this object, recursively.
	 * @return An ArrayList of PageObjectAdvanced.
	 */
	public ArrayList<PageObjectAdvanced> getAllPageObjectsRecursive() {
		ArrayList<PageObjectAdvanced> toReturn = new ArrayList<PageObjectAdvanced>();
		
		for (PageObjectAdvanced poa : pageObjects) {
			toReturn.addAll(poa.getAllPageObjectsRecursive());
			toReturn.add(poa);
		}
		
		return toReturn;
	}
	
	/**
	 * Returns the number of parameters this object has. For example, this example image has 2: [[File:Cat.png|180px|thumb]]
	 * @return An int.
	 */
	public int getNumParameters() {
		return parameters.size();
	}
	
	/**
	 * Get a parameter from this object.
	 * @param index The parameter that you want to get. This starts counting at 0 after the object header.
	 * @return A String.
	 */
	public String getParameter(int index) {
		return parameters.get(index);
	}
}
