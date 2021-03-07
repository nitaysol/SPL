package bgu.spl.net.impl.NitzKfitz.Commands;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.NitzKfitz.ConnectionsImpl;
import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.User;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

public class Post implements Command {
    String userName;
    String message;
    Vector<User> usersToSend;
    DataBaseSimulator db;
    Connections<String> connections;
    public Post(String userName, String message, Vector<User> usersToSend, DataBaseSimulator db, Connections<String> connections)
    {
        this.userName = userName;
        this.message=message;
        this.usersToSend = usersToSend;
        this.db=db;
        this.connections=connections;
    }

    @Override
    public String execute() {
        if(userName==null) return "ERROR 5";
        User u = db.getUserFromDB(this.userName);
        if(u==null || !u.getIsLoggedIn()) return "ERROR 5";
        u.addToNumOfPosts();
        for(User user: usersToSend)
        {
            synchronized (user) {
                if (user.getIsLoggedIn()) {
                    connections.send(user.getConnectionId(), "NOTIFICATION Public " + userName + " " + message); //push notification if the receiver is logged in
                } else  {
                    user.addToMessagesQueue("NOTIFICATION Public " + userName + " " + message);//if the receiver is logout, add to his MessageQueue
                }
            }
        }
        return "ACK 5";
    }
}
