package bgu.spl.net.impl.NitzKfitz.Commands;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.NitzKfitz.ConnectionsImpl;
import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.User;

import java.util.Iterator;


public class UserListMessages implements Command {
    private String userName;
    private DataBaseSimulator db;

    public UserListMessages(String userName, DataBaseSimulator db) {
        this.userName = userName;
        this.db = db;
    }


    @Override
    public String execute() {
        if(userName==null) return "ERROR 7";
        User u = db.getUserFromDB(userName);
        if (userName == null || !u.getIsLoggedIn()) {
            return "ERROR 7";
        }
        Iterator<String> it = db.getOrderdUsers().iterator(); //get all the registered users in DATABASE
        String outPut = "ACK 7 " + db.getOrderdUsers().size();
        while (it.hasNext()) {
            outPut += " " + it.next();
        }
        return outPut;

    }
}
