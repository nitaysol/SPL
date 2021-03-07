package bgu.spl.net.impl.NitzKfitz.Commands;

import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.User;

public class STAT implements Command {
    private String userName;
    private String userToStat;
    private DataBaseSimulator db;

    public STAT(String userName, String userToStat,DataBaseSimulator db) {
        this.userName=userName;
        this.userToStat=userToStat;
        this.db=db;
    }

    @Override
    public String execute() {
        if(userName==null) return "ERROR 8";
        User u = db.getUserFromDB(this.userName);
        User userToS=db.getUserFromDB(this.userToStat);
        if(u==null || userToS==null|| !u.getIsLoggedIn()) return "ERROR 8";
        return "ACK 8 "+userToS.getNumOfPosts()+" "+userToS.getFollowers().size()+" "+userToS.getFollowing().size();//return the ack by the format asked

    }
}
