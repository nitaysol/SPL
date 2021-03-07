package bgu.spl.net.impl.NitzKfitz.Commands;

import bgu.spl.net.impl.NitzKfitz.ConnectionsImpl;
import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.User;

public class Login implements Command {
    private String userName;
    private String password;
    private DataBaseSimulator db;
    private int connectionId;

    public Login(String userName, String password, DataBaseSimulator db, int connectionId){
        this.userName=userName;
        this.password=password;
        this.db=db;
        this.connectionId=connectionId;

    }

    @Override
    public String execute() {
        User u = db.getUserFromDB(userName);
        if(u==null) return "ERROR 2";
        synchronized (u) {
            if (!u.getIsLoggedIn() && u.getPassword().equals(password)) { //cant login if you login already | password incorrect
                u.login(connectionId);
                return "ACK 2";
            }
        }
        return "ERROR 2";
    }
}
