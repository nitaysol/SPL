package bgu.spl.mics;

import bgu.spl.mics.application.messages.TerminateAllBrod;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.services.ResourceService;

import java.util.concurrent.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	//variables
	private ConcurrentHashMap <MicroService, LinkedBlockingQueue<Message>> microSqueues;
	private ConcurrentHashMap<Class <? extends Event>, ConcurrentLinkedQueue<MicroService>> EventsToSubscribes;
	private ConcurrentHashMap<Class <? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> BroadToSubscribes;
	private ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Class<? extends Message>>> SubscribesToEvents;
	private ConcurrentHashMap<Event,Future> EventToFuture;


	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	/**
	 * Restricted to this class-only constructor
	 */
	private MessageBusImpl() {
		microSqueues=new ConcurrentHashMap<>();
		EventsToSubscribes=new ConcurrentHashMap<>();
		SubscribesToEvents=new ConcurrentHashMap<>();
		EventToFuture=new ConcurrentHashMap<>();
		BroadToSubscribes=new ConcurrentHashMap<>();


	}

	/**
	 *C
	 * @return instance of this.
	 */
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Add micro-service to the event RoundRobin queue
	 * Add the Event to the micro-service queue of events
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 * @param <T>
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		SubscribesToEvents.computeIfAbsent(m,c->new ConcurrentLinkedQueue<>()).add(type);
		EventsToSubscribes.computeIfAbsent(type,c->new ConcurrentLinkedQueue<>()).add(m);
	}

	/**
	 * Same as prev function just for BroadCast instead of Event
	 * @param type 	The type to subscribe to.
	 * @param m    	The subscribing micro-service.
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		SubscribesToEvents.computeIfAbsent(m,c->new ConcurrentLinkedQueue<>()).add(type);
		BroadToSubscribes.computeIfAbsent(type,c->new ConcurrentLinkedQueue<>()).add(m);
	}

	/**
	 * Gets an Event and resolved it with given result
	 * @param e      The completed event.
	 * @param result The resolved result of the completed event.
	 * @param <T>
	 */
	@Override
	public <T> void complete(Event<T> e, T result) {
	    EventToFuture.get(e).resolve(result);
	}

	/**
	 * Send broadcast to all micro-services which subscribed to b
	 * @param b 	The message to added to the queues.
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
        if (BroadToSubscribes.get(b.getClass()) != null) {
            for (MicroService m : BroadToSubscribes.get(b.getClass())) {
                addToMicroServiceQueue(m, b);
            }
        }
	}

	/**
	 * Gets a Message msg and adds it to the given micro-service m
	 * @param m
	 * @param msg
	 */
	private void addToMicroServiceQueue(MicroService m, Message msg)
	{
		try {
			microSqueues.get(m).put(msg);
		}
		catch(InterruptedException ex){ }
	}

	/**
	 * Gets Event e and add it to a micro-service who can handle this kind of event in RoundRobin manner.
	 * @param e     	The event to add to the queue.
	 * @param <T>
	 * @return Future which will hold the result once completed.
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> F = new Future<>();
		if (EventsToSubscribes.get(e.getClass()) != null ) {
			synchronized (EventsToSubscribes.get(e.getClass())) {
				if(!EventsToSubscribes.get(e.getClass()).isEmpty()) {
					MicroService m = EventsToSubscribes.get(e.getClass()).poll();
					EventToFuture.put(e, F);
					addToMicroServiceQueue(m, e);
					EventsToSubscribes.get(e.getClass()).add(m);
				}
				//If there is no micro-service who can handle the event
				else{
					return null;
				}
			}
		}
		//If no one has registered to this kind of event
		 else {
			return null;
		}

		return F;
	}

	/**
	 * Gets micro-service m and registers it by generating a new Blocking queue for it to consume messages.
	 * @param m the micro-service to create a queue for.
	 */
	@Override
	public void register(MicroService m) {
		microSqueues.put(m,new LinkedBlockingQueue<Message>());
	}

	/**
	 * Gets a micro-service m and delete all of its related contents in the data structure.
	 * Also, resolves all Future which he cannot handle due to terminating.
	 * @param m the micro-service to unregister.
	 */
	@Override
	public void unregister(MicroService m) {
            ConcurrentLinkedQueue<Class<? extends Message>> serviceMsgsTypes = SubscribesToEvents.get(m);
            if (serviceMsgsTypes != null) {
                for (Class<? extends Message> msgType : serviceMsgsTypes) {
                	//Checking which type of Message msgType is and continuing accordingly
                    if (Event.class.isAssignableFrom(msgType)) {
                        synchronized (EventsToSubscribes.get(msgType)) {
                            EventsToSubscribes.get(msgType).remove(m);
                        }
                    } else {
                        synchronized (BroadToSubscribes.get(msgType)) {
                            BroadToSubscribes.get(msgType).remove(m);
                        }
                    }
                }
                //Atomic action of removing m from SubscribesToEvents hash map
                SubscribesToEvents.remove(m);
            }
            synchronized (microSqueues.get(m)) {
            	//Resolving all current Messages on queue of Event type
                for(Message msg: microSqueues.get(m))
				{
					if(Event.class.isAssignableFrom(msg.getClass()))
					{
						EventToFuture.get(msg).resolve(null);
					}
				}
                microSqueues.get(m).clear();
                microSqueues.remove(m);
            }

	}

	/**
	 * Gets a micro-service m and returns a Message msg which is the first on queue
	 * @param m The micro-service requesting to take a message from its message
	 *          queue.
	 * @return Message msg
	 * @throws InterruptedException
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(microSqueues.get(m)==null)
			throw new IllegalStateException("Micro Service: "+m.getName()+" was never registered");
		Message msg = null;
		try {
			msg=microSqueues.get(m).take();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return msg;

	}

	

}
