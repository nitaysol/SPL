package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.passiveObjects.*;
import java.util.concurrent.CountDownLatch;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {
	private AtomicInteger currTick;
	public LogisticsService(int counter, CountDownLatch TimeServiceWait) {
		super("Logistic Service:" + counter, TimeServiceWait);
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
		 * When a {@link DeliveryEvent} is being sent by {@link SellingService} it process it and sending a {@link AcquireVehicle}
		 * to {@link ResourcesHolder} then it waits for the future he got to be resolved.
		 */
		subscribeEvent(DeliveryEvent.class, c->{
			AcquireVehicle acquireVehicleEvent = new AcquireVehicle();
			Future<Future<DeliveryVehicle>> dvFuture = sendEvent(acquireVehicleEvent);
			Future <DeliveryVehicle> dv=null;

			//if dv==null - no Resource service available / if dv.get()==null - Resource service terminated before resolving AcquireVehicle Future
			//As mentioned in the forum if there is no vehicle or terminated skip the Delivery
			if(dvFuture!=null) dv=dvFuture.get();
			if(dvFuture==null) complete(c, true);
			else if(dv!=null && dv.get()!=null) {
                dv.get().deliver(c.getAddress(), c.getDistance());
                ReleaseVehicle releaseVehicleEvent = new ReleaseVehicle(dv.get());
                sendEvent(releaseVehicleEvent);
            }
			complete(c,true);
		});
		
	}

}
