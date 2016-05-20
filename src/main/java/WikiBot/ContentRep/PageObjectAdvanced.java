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
	
	//Set information.
	public void addPageObjects(ArrayList<PageObjectAdvanced> po) {
		pageObjects.addAll(po);
	}
	
	public void addParameter(String param) {
		parameters.add(param);
	}
	
	public void setParameter(int index, String param) {
		parameters.set(index, param);
	}
	
	//Get information.
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
	 * @return Of the page objects this contains, get the first object with this header.
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
	 * @return Of the page objects this contains, get the first object with this header and of this object type.
	 */
	public PageObjectAdvanced getPageObject(String header, String objectType) {
		for (PageObjectAdvanced poa : pageObjects) {
			if (poa.getHeader().equalsIgnoreCase(header) && poa.getObjectType().equalsIgnoreCase(objectType)) {
				return poa;
			}
		}
		return null;
	}
	
	/**
	 * @return Of the page objects this contains, get the objects with this object type.
	 */
	public ArrayList<PageObjectAdvanced> getPageObjectOfType(String objectType) {
		ArrayList<PageObjectAdvanced> toReturn = new ArrayList<PageObjectAdvanced>();
		
		for (PageObjectAdvanced poa : pageObjects) {
			if (poa.getObjectType().equalsIgnoreCase(objectType)) {
				toReturn.add(poa);
			}
		}
		
		return toReturn;
	}
	
	public ArrayList<PageObjectAdvanced> getAllPageObjects() {
		return pageObjects;
	}
	
	public ArrayList<PageObjectAdvanced> getAllPageObjectsRecursive() {
		ArrayList<PageObjectAdvanced> toReturn = pageObjects;
		for (PageObjectAdvanced poa : pageObjects) {
			toReturn.addAll(poa.getAllPageObjectsRecursive());
		}
		return toReturn;
	}
	
	public int getNumParameters() {
		return parameters.size();
	}
	
	/**
	 * 
	 * @param index The parameter that you want to get. This starts counting at 0 after the object header.
	 * @return
	 */
	public String getParameter(int index) {
		return parameters.get(index);
	}
}
