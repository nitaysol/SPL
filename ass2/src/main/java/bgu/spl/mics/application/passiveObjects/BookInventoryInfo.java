package bgu.spl.mics.application.passiveObjects;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a information about a certain book in the inventory.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class BookInventoryInfo {
	private String title;
	private AtomicInteger amount;
	private int price;
	private Semaphore semaphore;

	public BookInventoryInfo(String title,int amount,int price)
	{
		this.title=title;
		this.amount=new AtomicInteger(amount);
		this.price=price;
		semaphore=new Semaphore(amount);
	}

	/**
     * Retrieves the title of this book.
     * <p>
     * @return The title of this book.   
     */
	public String getBookTitle() {
		return this.title;
	}

	/**
     * Retrieves the amount of books of this type in the inventory.
     * <p>
     * @return amount of available books.      
     */
	public int getAmountInInventory() {
		return this.amount.get();
	}

	/**
     * Retrieves the price for  book.
     * <p>
     * @return the price of the book.
     */
	public int getPrice() {
		return this.price;
	}

	public boolean takeBook(){

			if(semaphore.tryAcquire()) {
				this.amount.decrementAndGet();
				return true;
			}
			else
				return false;
	}
	

	
}
