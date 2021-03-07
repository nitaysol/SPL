package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.MessageBusImpl;
import java.io.*;
import java.util.HashMap;
/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Inventory {

	private HashMap<String, BookInventoryInfo> invInfo;
	private static class SingletonHolder {
		private static Inventory instance = new Inventory();
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Inventory getInstance() {
		return Inventory.SingletonHolder.instance;
	}
	//Constructor
	private Inventory(){
		invInfo= new HashMap<>();
	}
	
	/**
     * Initializes the store inventory. This method adds all the items given to the store
     * inventory.
     * <p>
     * @param inventory 	Data structure containing all data necessary for initialization
     * 						of the inventory.
     */
	public synchronized void load (BookInventoryInfo[ ] inventory ) {
		if(inventory!=null) {
			for (BookInventoryInfo b : inventory) {
				invInfo.put(b.getBookTitle(), b);
			}
		}
	}
	
	/**
     * Attempts to take one book from the store.
     * <p>
     * @param book 		Name of the book to take from the store
     * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
     * 			The first should not change the state of the inventory while the 
     * 			second should reduce by one the number of books of the desired type.
     */
	public OrderResult take (String book) {
		if (invInfo.get(book) != null && invInfo.get(book).takeBook())
			return OrderResult.SUCCESSFULLY_TAKEN;
		else
			return OrderResult.NOT_IN_STOCK;

	}
	
	
	
	/**
     * Checks if a certain book is available in the inventory.
     * <p>
     * @param book 		Name of the book.
     * @return the price of the book if it is available, -1 otherwise.
     */
	public int checkAvailabiltyAndGetPrice(String book) {
		if (invInfo.get(book)==null) return -1;
		synchronized (invInfo.get(book)) {
			BookInventoryInfo b = invInfo.get(book);
			if (b != null && b.getAmountInInventory() > 0)
				return b.getPrice();
			else
				return -1;
		}
	}
	
	/**
     * 
     * <p>
     * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
     * should be the titles of the books while the values (type {@link Integer}) should be
     * their respective available amount in the inventory. 
     * This method is called by the main method in order to generate the output.
     */
	public void printInventoryToFile(String filename){
		PrintToFile.printToFile(filename,createNameAmountHashMap());
	}

	private HashMap<String,Integer> createNameAmountHashMap()
	{
		HashMap<String, Integer> hashMapToPrint = new HashMap<>();
		HashMap<String, BookInventoryInfo> cloneOfUsedHashMap = invInfo;
		for (HashMap.Entry<String, BookInventoryInfo> entry : cloneOfUsedHashMap.entrySet()) {
			hashMapToPrint.put(entry.getKey(), entry.getValue().getAmountInInventory());
		}
		return hashMapToPrint;
	}


}
