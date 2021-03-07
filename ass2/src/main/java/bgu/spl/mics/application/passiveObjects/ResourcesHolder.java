package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import bgu.spl.mics.application.messages.DeliveryEvent;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {
	private ConcurrentLinkedQueue<DeliveryVehicle> deliveryVehicleQueue;
	private ConcurrentLinkedQueue<Future<DeliveryVehicle>> FutureToDelivery;
	private Object objcetToSync;


	private static class SingletonHolder {
		private static ResourcesHolder instance = new ResourcesHolder();
	}
	/**
     * Retrieves the single instance of this class.
     */
	public static ResourcesHolder getInstance() {
		return SingletonHolder.instance;
	}
	private ResourcesHolder(){
		deliveryVehicleQueue= new ConcurrentLinkedQueue<>();
		FutureToDelivery = new ConcurrentLinkedQueue<>();
        objcetToSync = new Object();

	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
		Future<DeliveryVehicle> F = new Future<>();
		synchronized (objcetToSync) {
            DeliveryVehicle dv = deliveryVehicleQueue.poll();
            if (dv != null) {
                F.resolve(dv);
            } else {
                FutureToDelivery.add(F);
            }
        }
		return F;
	}
	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
        synchronized (objcetToSync) {
            Future<DeliveryVehicle> F = FutureToDelivery.poll();
            if (F != null) {
                F.resolve(vehicle);
            } else {
                deliveryVehicleQueue.add(vehicle);
            }
        }
    }

	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		for(DeliveryVehicle v: vehicles)
		{
				deliveryVehicleQueue.add(v);

		}
	}

}
