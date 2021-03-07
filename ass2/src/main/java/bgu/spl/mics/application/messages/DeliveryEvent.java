package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;


public class DeliveryEvent implements Event<Boolean> {
    private String address;
    private int distance;
    public DeliveryEvent(String address, int distance)
    {
        this.address=address;
        this.distance=distance;
    }
    public String getAddress(){
        return this.address;
    }
    public int getDistance(){
        return this .distance;
    }
}
