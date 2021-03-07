package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;

public class TakeBookFromInvEvent implements Event<Integer> {
    private String book;
    public TakeBookFromInvEvent(String book)
    {
        this.book = book;
    }
    public String getBook()
    {
        return this.book;

    }
}
