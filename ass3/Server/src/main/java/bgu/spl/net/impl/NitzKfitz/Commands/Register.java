package bgu.spl.net.impl.NitzKfitz.Commands;


import bgu.spl.net.impl.NitzKfitz.ConnectionsImpl;
import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;

public class Register implements Command{
    private String userName;
    private String password;
    private DataBaseSimulator db;
    private int connectionID;

    public Register(String userName, String password, DataBaseSimulator db, int connectionID)
    {
        this.userName=userName;
        this.password=password;
        this.db = db;
        this.connectionID=connectionID;
    }

    public String execute(){
        if(db.registerToDB(userName,password,connectionID))
            return "ACK 1";
        return "ERROR 1";

    }
}
