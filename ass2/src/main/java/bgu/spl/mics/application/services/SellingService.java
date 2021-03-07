package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateAllBrod;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.*;
import java.util.concurrent.CountDownLatch;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{
	private MoneyRegister moneyRegister;
	private AtomicInteger currTick;
	public SellingService(int counter, CountDownLatch TimeServiceWait) {
		super("Selling Service:"+counter, TimeServiceWait);
		this.moneyRegister=MoneyRegister.getInstance();
		currTick = new AtomicInteger(0);
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
		 * When {@link BookOrderEvent} is being handled by this process it first checks availability of the book in inventory
		 * Therfore, it generates new {@link checkBookAvailabilityEvent} Event and send it to msgBus to be taken care of.
		 * If available it locks {@link Customer} customer, and trying to acquire the book from {@link Inventory} using
		 * msgBus functionallity.
		 */
		subscribeEvent(BookOrderEvent.class,c->{
            //Saving processTick as currentTick
            int proccessTick = currTick.get();
			//checking book availability before starting
			checkBookAvailabilityEvent cbal = new checkBookAvailabilityEvent(c.getBook());
			Future<Integer> Fprice = sendEvent(cbal);
			//in case no one can handle it complete with null or book isnt available
			if(Fprice== null || Fprice.get()==-1 ||c.getCustomer().getAvailableCreditAmount()<Fprice.get()) complete(c,null);
			else {
                //Book is available and customer have enough money to purchase it.
                int price = Fprice.get();
                synchronized (c.getCustomer()) {
                    //verifying the customer has enough money again(in case other thread bought a book at this moment
                    if (c.getCustomer().getAvailableCreditAmount() < Fprice.get()) complete(c, null);
                    else {
                        TakeBookFromInvEvent takeBookFromInvEvent = new TakeBookFromInvEvent(c.getBook());
                        Future<Integer> taken = sendEvent(takeBookFromInvEvent);
                        //If some other Thread took the book complete with null
                        if (taken == null || taken.get() == -1) {
                            complete(c, null);
                        }
                        //successfully bought the book sending a delivery and generating recipe.
                        else {
                            this.moneyRegister.chargeCreditCard(c.getCustomer(), price);
                            OrderReceipt recipt = generateRecipe(c.getCustomer(), c.getBook(), price, proccessTick, c.getOrderTick());
                            DeliveryEvent DE = new DeliveryEvent(c.getCustomer().getAddress(), c.getCustomer().getDistance());
                            sendEvent(DE);
                            complete(c, recipt);
                        }
                    }
                }
            }

		});
	}

	/**
	 * Private function which generates a new recipt and saving it in {@link MoneyRegister}
	 * @param c represent the {@link Customer} making the purchase.
	 * @param book represent the {@link String} he wants to buy.
	 * @param price represnt the book price.
	 * @param processTick
	 * @param orderTick
	 * @return {@link OrderReceipt} in order the complete the event.
	 */
	private OrderReceipt generateRecipe(Customer c,String book, int price, int processTick, int orderTick){
		OrderReceipt recipt = new OrderReceipt(this.getName(), 0,c.getId(),book,price,this.currTick.get(),orderTick, processTick);
		c.addToCustomerRecipeVec(recipt);
		this.moneyRegister.file(recipt);
		return recipt;

	}

}
