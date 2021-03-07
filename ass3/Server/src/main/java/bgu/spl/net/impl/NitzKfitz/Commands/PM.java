package bgu.spl.net.impl.NitzKfitz.Commands;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.NitzKfitz.ConnectionsImpl;
import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.User;

import java.util.List;

public class PM implements Command {
    String userName;
    String message;
    String userToSendTo;
    DataBaseSimulator db;
    Connections<String> connections;

    public PM(String userName, String message, String userToSendTo, DataBaseSimulator db, Connections<String> connections) {
        this.userName = userName;
        this.message = message;
        this.userToSendTo = userToSendTo;
        this.db = db;
        this.connections=connections;

    }


    @Override
    public String execute() {
        if(userName==null) return "ERROR 6";
        User u = db.getUserFromDB(this.userName);
        User userToSend = db.getUserFromDB(this.userToSendTo);
        if(u==null || userToSend==null) return "ERROR 6";
            synchronized (userToSend) {
                if (userToSend.getIsLoggedIn()) {
                    connections.send(userToSend.getConnectionId(), "NOTIFICATION "+ message.replaceFirst(userToSendTo, userName)); //push notification when the receiver is logged in
                }
                else
                    userToSend.addToMessagesQueue("NOTIFICATION " +  message.replaceFirst(userToSendTo, userName)); //if the receiver is logout, add to his MessageQueue
            }
        return "ACK 6";
    }
}