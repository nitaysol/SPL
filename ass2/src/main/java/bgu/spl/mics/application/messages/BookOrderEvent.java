package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;

public class BookOrderEvent implements Event<OrderReceipt> {
    private Customer c;
    private String book;
    private int orderTick;
    public BookOrderEvent(Customer c, String book, int orderTick){
        this.c=c;
        this.book=book;
        this.orderTick=orderTick;
    }
    public Customer getCustomer(){
        return this.c;
    }
    public String getBook(){
        return this.book;
    }
    public int getOrderTick(){
        return this.orderTick;
    }
}
