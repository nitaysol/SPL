package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TakeBookFromInvEvent;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.passiveObjects.OrderResult;
import java.util.concurrent.CountDownLatch;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{
	private Inventory inv;
	private AtomicInteger currTick;

	public InventoryService(int counter, CountDownLatch TimeServiceWait){
		super("Inventory Service:" + counter,TimeServiceWait);
		currTick = new AtomicInteger(0);
		inv=Inventory.getInstance();
	}

	@Override
	protected void initialize() {

		subscribeBroadcast(TerminateAllBrod.class, c->{
			this.terminate();
		});

		subscribeBroadcast(TickBroadcast.class, c->{
			currTick.set(c.getTick());
		});
		/**
		 * Trying to acquire a book from {@link Inventory} - if success -> return price , else returns -1.
		 */
		subscribeEvent(TakeBookFromInvEvent.class, c->{
			int price = inv.checkAvailabiltyAndGetPrice(c.getBook());
			if(inv.take(c.getBook())== OrderResult.SUCCESSFULLY_TAKEN){
				complete(c, price);
			}
			else{
				complete(c,-1);
			}
		});
		/**
		 * Checking a book using {@link Inventory} instance hold in this class.
		 */
		subscribeEvent(checkBookAvailabilityEvent.class, c->{
			complete(c, inv.checkAvailabiltyAndGetPrice(c.getBook()));
		});

		
	}

}
