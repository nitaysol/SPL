package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer implements Serializable {
	private int id;
	private String name;
	private String address;
	private int distance;
	private Vector<OrderReceipt> vecOfReceipts;
	private int creditCardNum;
	private Integer amountInCreditCard;

	public Customer(int id, String name, String address, int distance, int creditCardNum,int amountInCreditCard)
	{
		this.id = id;
		this.name = name;
		this.address = address;
		this.distance = distance;
		vecOfReceipts = new Vector<>();
		this.creditCardNum=creditCardNum;
		this.amountInCreditCard=amountInCreditCard;

	}
	/**
     * Retrieves the name of the customer.
     */
	public String getName() {
		return name;
	}

	/**
     * Retrieves the ID of the customer  . 
     */
	public int getId() {
		return id;
	}
	
	/**
     * Retrieves the address of the customer.  
     */
	public String getAddress() {
		return address;
	}
	
	/**
     * Retrieves the distance of the customer from the store.  
     */
	public int getDistance() {
		return distance;
	}

	
	/**
     * Retrieves a list of receipts for the purchases this customer has made.
     * <p>
     * @return A list of receipts.
     */
	public List<OrderReceipt> getCustomerReceiptList() {
		return vecOfReceipts;
	}
	
	/**
     * Retrieves the amount of money left on this customers credit card.
     * <p>
     * @return Amount of money left.   
     */
	public int getAvailableCreditAmount() {


		return amountInCreditCard.intValue();
	}
	
	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber() {
		return creditCardNum;
	}
	public boolean withDrawFromCreditCard(int amount)
	{
		synchronized (amountInCreditCard){
			if(amountInCreditCard-amount>=0)
			{
				amountInCreditCard=amountInCreditCard-amount;
				return true;
			}
			else
				return false;
		}
	}

	/**
	 * Gets a OrderReceipt or and adds it to the customer orders vector
	 * @param or
	 */
	public void addToCustomerRecipeVec(OrderReceipt or){
		vecOfReceipts.add(or);
	}

	
}
