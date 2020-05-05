package WikiBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import WikiBot.ContentRep.PageLocation;

public class BufferPool<U, V> {
	Map<U, Queue<V>> bufferPool;
	int maxSize;
	int needsFlushed;
	
	public BufferPool(int maxSize_) {
		bufferPool = new HashMap<>();
		maxSize = maxSize_;
	}
	
	/**
	 * Adds a key and value pair to the buffer pool.
	 * @param key A key.
	 * @param value A value.
	 */
	public void addToBuffer(U key, V value) {
		// Check if language in bufferPool. If not, add it.
		if ( !bufferPool.containsKey(key) ) {
			bufferPool.put(key, new LinkedList<V>());
		}
		
		// Add page location to buffer.
		Queue<V> buffer = bufferPool.get(key);
		buffer.add(value);
	}
	
	/**
	 * Returns true if any pools are at least {@code maxSize} in size.
	 * 
	 * @return A boolean.
	 */
	public boolean needsFlushed() {
		int maxBufferSize = -1;
		
		for (Queue<V> buffer : bufferPool.values()) {
			if (buffer.size() > maxBufferSize) {
				maxBufferSize = buffer.size();
			}
		}
		
		return maxBufferSize >= maxSize;
	}
	
	/**
	 * Returns the key of the largest buffer.
	 * 
	 * @return
	 */
	public U getLargestBufferKey() {
		int maxBufferSize = -1;
		U largestBufferKey = null;
		
		for (U key : bufferPool.keySet()) {
			Queue<V> buffer = bufferPool.get(key);
			if (buffer.size() > maxBufferSize) {
				maxBufferSize = buffer.size();
				largestBufferKey = key;
			}
		}
		
		return largestBufferKey;
	}
	
	/**
	 * Get the size of the largest buffer pool.
	 * @return
	 */
	public int getLargestBufferSize() {
		U largestBufferKey = getLargestBufferKey();
		return bufferPool.get(largestBufferKey).size();
	}
	
	/**
	 * Returns true if all buffers are empty.
	 * @return
	 */
	public boolean isEmpty() {
		return bufferPool.size() == 0; // Aka, all buffers are empty and non-existent.
	}
	
	/**
	 * Flushes the buffer of the corresponding key.
	 * 
	 * @param key The buffer's key.
	 * @return The buffer associated with {@code key}.
	 */
	public Queue<V> flushPool(U key) {
		return bufferPool.remove(key);
	}
	
	/**
	 * Returns true if {@code value} is found in the buffer associated with {@code key}.
	 * 
	 * @param key The buffer's key.
	 * @param value
	 * @return
	 */
	public boolean contains(U key, V value) {
		return bufferPool.containsKey(key) && bufferPool.get(key).contains(value);
	}
	
//	@Override
//	public String toString() {
//		String toReturn = "BufferPool\n";
//		for (PageLocation page : knownPagesToLinks.keySet()) {
//			// Get this page.
//			ArrayList<PageLocation> linksTo = knownPagesToLinks.get(page);
//			
//			// Format this page
//			toReturn += page + " -> ";
//			for (PageLocation link : linksTo) {
//				toReturn += link + ",";
//			}
//			toReturn += "\n";
//		}
//		toReturn += "unknown -> ";
//		for (PageLocation unknown : unknownPages) {
//			toReturn += unknown + ", ";
//		}
//		return toReturn;
//	}
}
