package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateAllBrod;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.messages.BookOrderEvent;
import java.util.concurrent.CountDownLatch;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl.mics.Future;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService{
	private AtomicInteger currTick;
	private Customer customer;
	private Vector<BookOrderEvent> ordersVec;
	private Vector<Future> vecOfOrdersFutures;
	int counter;
	public APIService(int counter, Customer customer, Vector<BookOrderEvent> ordersList, CountDownLatch TimeServiceWait) {
		super("API service:"+counter, TimeServiceWait);
		currTick= new AtomicInteger(0);
		this.vecOfOrdersFutures=new Vector<>();
		this.customer=customer;
		this.ordersVec=ordersList;
		this.ordersVec.sort(new bookOrderEventComparator());
		this.counter = 0;

	}

	@Override
	protected void initialize() {
		/**
		 *On terminating wait for all {@link Future} Events of {@link BookOrderEvent} to be completed.
		 */
		subscribeBroadcast(TerminateAllBrod.class, c->{
			for(Future F: vecOfOrdersFutures)
			{
				F.get();
			}
			this.terminate();
		});
		/**
		 * On each tick check that the orders which apply to the current tick and generates new {@link BookOrderEvent}.
		 */
		subscribeBroadcast(TickBroadcast.class, c->{
			currTick.set(c.getTick());
			for(int i=counter; i<ordersVec.size();i++){
				if (ordersVec.get(counter).getOrderTick() == currTick.get()){
					vecOfOrdersFutures.add(sendEvent(ordersVec.get(counter)));
					counter++;
				}
			}

		});
		
	}

	/**
	 * Comparator for sorting each orders by the tick it should be sent at.
	 */
	private class bookOrderEventComparator implements Comparator<BookOrderEvent>{
		public int compare(BookOrderEvent firstEvent, BookOrderEvent secondEvent){
			return firstEvent.getOrderTick()-secondEvent.getOrderTick();
		}
	}

}
