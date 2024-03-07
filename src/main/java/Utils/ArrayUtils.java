package Utils;

import java.util.ArrayList;

/**
 * Various helpful methods for working with arrays.
 */
public class ArrayUtils {

	public static String compactArray(ArrayList<String> array) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (String item: array) {
			output+=item;
		}
		
		return output;
	}
	
	public static String compactArray(String[] array) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (String item: array) {
			output+=item;
		}
		
		return output;
	}
	
	public static String compactArray(ArrayList<String> array, String delimitor) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (int i = 0; i < array.size(); i++) {
			output+= array.get(i);
			if (i != array.size()-1) {
				output += delimitor;
			}
		}
		
		return output;
	}
	
	public static String compactArray(String[] array, String delimitor) {
		//This takes an array of strings and compacts it into one string.
		String output = "";
		
		for (int i = 0; i < array.length; i++) {
			output+= array[i];
			if (i != array.length-1) {
				output += delimitor;
			}
		}
		
		return output;
	}
}
