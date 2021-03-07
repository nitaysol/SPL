package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class checkBookAvailabilityEvent implements Event<Integer> {
    private String book;
    public checkBookAvailabilityEvent(String book){
        this.book=book;
    }
    public String getBook(){
        return this.book;
    }
}
