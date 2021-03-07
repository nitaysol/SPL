package bgu.spl.net.impl.NitzKfitz.Commands;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.NitzKfitz.ConnectionsImpl;
import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.User;

public class Logout implements Command {
    private String userName;
    private int connectionId;
    private DataBaseSimulator db;
    Connections<String> connections;
    public Logout(String userName, int connectionId , DataBaseSimulator db, Connections<String> connections) {
        this.userName=userName;
        this.connectionId = connectionId;
        this.db = db;
        this.connections=connections;
    }


    @Override
    public String execute() {
        if(userName==null) return "ERROR 3";
        User u = db.getUserFromDB(userName);
        if(u==null) return "ERROR 3";
        synchronized (u) {
            if (u.getIsLoggedIn()) { //user can logout only if he logged in
                u.logout();
                return "ACK 3";
            }
        }
        return "ERROR 3";
    }
}