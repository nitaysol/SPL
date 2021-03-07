package bgu.spl.mics.application.passiveObjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;


import static org.junit.Assert.*;

public class InventoryTest {
    private Inventory i;
    @Before
    public void setUp() throws Exception {
        try {
            i= Inventory.getInstance();
        }
        catch(Exception e){
            throw new IllegalArgumentException("Inventory default constructor isnt defined");
        }
    }

    @After
    public void tearDown() throws Exception {
        i=null;
        assertNull(i);
    }

    @Test
    public void getInstance() {
        Inventory i2 = Inventory.getInstance();
        if(!(i2==i))
            fail("getInstance didnt return a instance");
    }

    @Test
    public void load() {
        BookInventoryInfo[] inventory=new BookInventoryInfo[10];
        for(int j=0;j<inventory.length;j++){
            BookInventoryInfo b = new BookInventoryInfo("Book"+j,j,j);
            inventory[j]=b;
        }
        i.load(inventory);
        for(int j=1; j<inventory.length;j++)
        {
            //Bookj price should be J if entered correctly.
            assertEquals(j,i.checkAvailabiltyAndGetPrice("Book"+j));
        }
    }

    @Test
    public void take() {
        OrderResult or2 = OrderResult.NOT_IN_STOCK;
        OrderResult or = OrderResult.SUCCESSFULLY_TAKEN;
        BookInventoryInfo[] in = new BookInventoryInfo[5];
        for(int j=0;j<in.length;j++){
            BookInventoryInfo bii = new BookInventoryInfo("Book"+j,j,j);
            in[j]=bii;
        }
        i.load(in);
        assertEquals(or, i.take("Book1"));
        assertEquals(or2,i.take("Book0"));
        for(int j=2;j<in.length;j++){
            assertEquals(j, in[j].getAmountInInventory());
        }
    }

    @Test
    public void checkAvailabiltyAndGetPrice() {
        assertEquals(-1,i.checkAvailabiltyAndGetPrice("Book"));
        BookInventoryInfo[] inventory=new BookInventoryInfo[2];
        inventory[0]= new BookInventoryInfo("Book1",3,30);
        inventory[1]= new BookInventoryInfo("Book2",0,40);
        i.load(inventory);
        //available
        assertEquals(30,i.checkAvailabiltyAndGetPrice("Book1"));
        //not available
        assertEquals(-1,i.checkAvailabiltyAndGetPrice("Book2"));
    }

    @Test
    public void printInventoryToFile() {
        BookInventoryInfo[] inventory=new BookInventoryInfo[10];
        for(int j=0;j<inventory.length;j++){
            BookInventoryInfo b = new BookInventoryInfo("Book"+j,j,j);
            inventory[j]=b;
        }
        i.load(inventory);
        i.printInventoryToFile("FileCheck");
        //assuming its in the same directory for the tests
        boolean check = new File("FileCheck").exists();
        if(!check) fail("didnt generate file");
    }
}