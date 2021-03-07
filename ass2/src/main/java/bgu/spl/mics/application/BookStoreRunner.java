package bgu.spl.mics.application;
import java.io.*;

import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.concurrent.CountDownLatch;
import java.util.HashMap;
import java.util.Vector;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {

    private static int sellingServices;
    private static int inventoryService;
    private static int logisticsServices;
    private static int resourcesService;
    private static int speedTimeService;
    private static int durationTimeService;
    private static BookInventoryInfo[] books;
    private static DeliveryVehicle[] vehicles;
    private static Customer[] customers;
    private static Vector<Thread> threads;
    private static Vector<BookOrderEvent> orderSchedule;
    private static CountDownLatch TimeServiceWait;

    public static void main(String[] args) {

        String jsonInputFile = args[0];
        String customerInputFile=args[1];
        String booksInputFile=args[2];
        String ordersInputFile=args[3];
        String moneyInputFile=args[4];

        try {
            readFromJsonFile(jsonInputFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        threads = new Vector<>();
        Inventory inv=Inventory.getInstance();
        inv.load(books);

        //Start all the Services

        for (int i = 1; i <= sellingServices; i++) {
            Thread sellingThread = new Thread(new SellingService(i, TimeServiceWait));
            sellingThread.start();
            threads.add(sellingThread);
        }

        for (int i = 1; i <= inventoryService; i++) {
            Thread inventoryThread = new Thread(new InventoryService(i, TimeServiceWait));
            inventoryThread.start();
            threads.add(inventoryThread);
        }

        for (int i = 1; i <= logisticsServices; i++) {
            Thread logisticsThread = new Thread(new LogisticsService(i, TimeServiceWait));
            logisticsThread.start();
        }

        for (int i = 1; i <= resourcesService; i++) {
            Thread resourcesThread = new Thread(new ResourceService(i, TimeServiceWait));
            resourcesThread.start();
        }
        //Waiting for all Services to register
        try{
            TimeServiceWait.await();
        }catch (InterruptedException e){}
        Thread timeThread = new Thread(new TimeService(speedTimeService, durationTimeService));
        timeThread.start();


        //Waiting for Inventory and Selling services to end before continuing to output
        for(Thread t: threads){
            try {
                t.join();
            }
            catch(InterruptedException e){}
        }
        //Files Outputs:
        /* Books in Inventory */
        inv.printInventoryToFile(booksInputFile);

        /* Order Receipts */
        MoneyRegister.getInstance().printOrderReceipts(ordersInputFile);

        /* Customers in the system */
        CustomersOutputFile(customerInputFile,customers);

        /* MoneyRegister object */
        MoneyRegister moneyRegister=MoneyRegister.getInstance();
        MoneyRegisterOutputFile(moneyInputFile,moneyRegister);
    }

    private static void CustomersOutputFile(String fileNameToOutput, Customer[] CutomersArray)
    {
        HashMap<Integer,Customer> customersHashMap=new HashMap<>();
        for(int i=0;i<CutomersArray.length;i++){
            customersHashMap.put(CutomersArray[i].getId(),CutomersArray[i]);
        }
        PrintToFile.printToFile(fileNameToOutput,customersHashMap);
    }

    private static void MoneyRegisterOutputFile(String fileNameToOutput,MoneyRegister moneyRegister)
    {
        PrintToFile.printToFile(fileNameToOutput,moneyRegister);
    }


    private static void readFromJsonFile(String jsonfileName) throws FileNotFoundException
    {
        //BookInventoryInfo
        String bookTitle;
        int bookAmount;
        int bookPrice;
        BookInventoryInfo book;


        //DeliveryVehicle
        int licenseNum;
        int speedAllow;
        DeliveryVehicle vehicle;
        ResourcesHolder rh;

        //Customer
        int customerID;
        String customerName;
        String customerAddress;
        int customerDistance;
        int customerCreditCard;
        int customerAmountCreditCard;
        Customer customer;

        //Order Schedule
        String bookTitleOrder;
        int TickOrder;

        Gson gson=new Gson();
        JsonObject jsonObject=gson.fromJson(new FileReader(jsonfileName),JsonObject.class);

        //going over books Inventory

        JsonArray InventoryArray=jsonObject.getAsJsonArray("initialInventory");
        books=new BookInventoryInfo[InventoryArray.size()];
        for(int i=0;i<books.length;i++){
            bookTitle=((JsonObject)InventoryArray.get(i)).getAsJsonPrimitive("bookTitle").getAsString();
            bookAmount=((JsonObject)InventoryArray.get(i)).getAsJsonPrimitive("amount").getAsInt();
            bookPrice=((JsonObject)InventoryArray.get(i)).getAsJsonPrimitive("price").getAsInt();
            book=new BookInventoryInfo(bookTitle,bookAmount,bookPrice);
            books[i]=book;
        }


        //going over Vehicle Resources

        rh=ResourcesHolder.getInstance();
        JsonArray ResourcesArray=jsonObject.getAsJsonArray("initialResources").get(0).getAsJsonObject().getAsJsonArray("vehicles");
        vehicles=new DeliveryVehicle[ResourcesArray.size()];
        for(int j=0;j<vehicles.length;j++)
        {
            licenseNum=((JsonObject)ResourcesArray.get(j)).getAsJsonObject().getAsJsonPrimitive("license").getAsInt();
            speedAllow=((JsonObject)ResourcesArray.get(j)).getAsJsonObject().getAsJsonPrimitive("speed").getAsInt();
            vehicle=new DeliveryVehicle(licenseNum,speedAllow);
            vehicles[j]=vehicle;
        }
        rh.load(vehicles);

        //Services Amount

        speedTimeService=jsonObject.getAsJsonObject("services").getAsJsonObject("time").getAsJsonPrimitive("speed").getAsInt();
        durationTimeService=jsonObject.getAsJsonObject("services").getAsJsonObject("time").getAsJsonPrimitive("duration").getAsInt();

        sellingServices=jsonObject.getAsJsonObject("services").getAsJsonPrimitive("selling").getAsInt();

        inventoryService=jsonObject.getAsJsonObject("services").getAsJsonPrimitive("inventoryService").getAsInt();

        logisticsServices=jsonObject.getAsJsonObject("services").getAsJsonPrimitive("logistics").getAsInt();

        resourcesService=jsonObject.getAsJsonObject("services").getAsJsonPrimitive("resourcesService").getAsInt();


        //going over customers

        JsonArray CustomersArray=jsonObject.getAsJsonObject("services").getAsJsonArray("customers");
        customers=new Customer[CustomersArray.size()];
        TimeServiceWait = new CountDownLatch(customers.length + sellingServices + inventoryService + logisticsServices + resourcesService);
        for(int k=0;k<customers.length;k++)
        {
            customerID=((JsonObject)CustomersArray.get(k)).getAsJsonPrimitive("id").getAsInt();
            customerName=((JsonObject)CustomersArray.get(k)).getAsJsonPrimitive("name").getAsString();
            customerAddress=((JsonObject)CustomersArray.get(k)).getAsJsonPrimitive("address").getAsString();
            customerDistance=((JsonObject)CustomersArray.get(k)).getAsJsonPrimitive("distance").getAsInt();
            customerCreditCard=((JsonObject)CustomersArray.get(k)).getAsJsonObject("creditCard").getAsJsonPrimitive("number").getAsInt();
            customerAmountCreditCard=((JsonObject)CustomersArray.get(k)).getAsJsonObject("creditCard").getAsJsonPrimitive("amount").getAsInt();
            customer=new Customer(customerID,customerName, customerAddress,customerDistance,customerCreditCard,customerAmountCreditCard);
            customers[k]=customer;
            JsonArray Orders=CustomersArray.get(k).getAsJsonObject().getAsJsonArray("orderSchedule");
            //going over each customer's order schedule
            orderSchedule=new Vector<>();
            for(int i=0;i< Orders.size();i++)
            {
                bookTitleOrder=((JsonObject)Orders.get(i)).getAsJsonPrimitive("bookTitle").getAsString();
                TickOrder=((JsonObject)Orders.get(i)).getAsJsonPrimitive("tick").getAsInt();
                orderSchedule.add(new BookOrderEvent(customer,bookTitleOrder,TickOrder));
            }
            Thread APIThread=new Thread(new APIService(k+1,customer,orderSchedule, TimeServiceWait));
            APIThread.start();
        }
    }

}
