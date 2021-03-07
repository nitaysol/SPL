package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TerminateAllBrod;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	private AtomicInteger currentTime;
	private int speed;
	private int duration;
	public TimeService(int speed, int duration) {
		super("Time Service", null);
		this.currentTime=new AtomicInteger(1);
		this.speed= speed;
		this.duration= duration;
	}

	/**
	 * On init, generates a new Timer.schedule which runs according the specified time given at the constructor.
	 */
	@Override
	protected void initialize() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (currentTime.get() > duration - 1) {
					sendBroadcast(new TerminateAllBrod());
					timer.cancel();
					timer.purge();
				} else {
					sendBroadcast(new TickBroadcast(currentTime.get()));
					currentTime.incrementAndGet();
				}
			}
		}, 0, speed);
		subscribeBroadcast(TerminateAllBrod.class, c->{
			this.terminate();
		});

	}



}
