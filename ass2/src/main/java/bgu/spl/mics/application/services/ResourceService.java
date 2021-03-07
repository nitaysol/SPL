package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicle;
import bgu.spl.mics.application.messages.ReleaseVehicle;
import bgu.spl.mics.application.messages.TerminateAllBrod;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourcesHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService{
	private ResourcesHolder rHolder;
	private AtomicInteger currTick;
	private ConcurrentLinkedQueue<Future<DeliveryVehicle>> FutureToDelivery;

	public ResourceService(int counter, CountDownLatch TimeServiceWait)
	{
		super("Resource Service:" + counter, TimeServiceWait);
		rHolder=ResourcesHolder.getInstance();
		currTick = new AtomicInteger(0);
		FutureToDelivery = new ConcurrentLinkedQueue<>();
	}

	@Override
	protected void initialize() {
		/**
		 * On termination - resolves all {@link Future} of {@link DeliveryVehicle} which he "promised" to handle and
		 * could due to {@link TerminateAllBrod} received.
		 */
		subscribeBroadcast(TerminateAllBrod.class, c->{
			for(Future F: FutureToDelivery)
				if(!F.isDone()) F.resolve(null);
			this.terminate();
		});
		subscribeBroadcast(TickBroadcast.class, c->{
			currTick.set(c.getTick());
		});
		/**
		 * Acquiring vehicle using {@link ResourcesHolder} class.
		 */
		subscribeEvent(AcquireVehicle.class, c->{
			Future<DeliveryVehicle> vehicle = rHolder.acquireVehicle();
			if(!vehicle.isDone()) FutureToDelivery.add(vehicle);
			complete(c,vehicle);
		});
		/**
		 * Releasing vehicle using {@link ResourcesHolder} class.
		 */
		subscribeEvent(ReleaseVehicle.class, c->{
			rHolder.releaseVehicle(c.getVehicleToRelease());
			complete(c,true);
		});
	}

}
