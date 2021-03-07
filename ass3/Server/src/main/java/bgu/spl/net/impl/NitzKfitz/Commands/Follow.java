package bgu.spl.net.impl.NitzKfitz.Commands;

import bgu.spl.net.impl.NitzKfitz.DataBaseSimulator;
import bgu.spl.net.impl.NitzKfitz.User;

import java.util.List;
import java.util.Vector;

public class Follow implements Command {
    private String userName;
    private boolean follow;
    private int numOfUsers;
    private Vector<User> userNameListToFollowUnFollow;
    private DataBaseSimulator db;


    public Follow(String userName, boolean follow, int numOfUsers, Vector<User> userNameListToFollowUnFollow, DataBaseSimulator db){
        this.userName=userName;
        this.follow=follow;
        this.numOfUsers=numOfUsers;
        this.userNameListToFollowUnFollow=userNameListToFollowUnFollow;
        this.db=db;
    }

    @Override
    public String execute()
    {
        Vector<String> vecOfSuccess = new Vector<>();
        if(userName==null) return "ERROR 4";
        User u = db.getUserFromDB(this.userName);
        if(u==null) return "ERROR 4";
        synchronized (u) {
            if (!u.getIsLoggedIn()) return "ERROR 4";
            if (follow) {
                for (User user : this.userNameListToFollowUnFollow) {
                    synchronized (user) {
                        if (db.getUserFromDB(userName).addFollowing(user)) //check if success add to following
                        {
                            vecOfSuccess.add(user.getUserName());
                            user.addFollower(db.getUserFromDB(userName));
                        }
                    }
                }
            }
        else
        {
            for(User user:this.userNameListToFollowUnFollow){
                if(db.getUserFromDB(userName).removeFollowing(user)){ //check if success remove from following
                    synchronized (user) {
                        vecOfSuccess.add(user.getUserName());
                        user.removeFollower(db.getUserFromDB(userName));
                    }
                }
            }
        }
        }
        if(vecOfSuccess.size()==0) //if not success follow/unfollow for anyone
            return "ERROR 4";
        String outPut = "ACK 4 " + vecOfSuccess.size();
        for (String username : vecOfSuccess) {
            outPut +=" "+ username;
        }
        return outPut;
    }

}
