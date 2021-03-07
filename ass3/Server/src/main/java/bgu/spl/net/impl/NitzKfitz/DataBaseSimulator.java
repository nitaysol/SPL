package bgu.spl.net.impl.NitzKfitz;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
public class DataBaseSimulator {
    private ConcurrentHashMap<String, User> Users;
    private Vector<String> usersOrderd;

    public DataBaseSimulator()
    {
        this.Users=new ConcurrentHashMap<>();
        usersOrderd=new Vector<>();
    }

    public Vector<String> getOrderdUsers(){return this.usersOrderd;}

    public boolean registerToDB(String userName, String Password,int connectionID){
        User u = Users.putIfAbsent(userName, new User(userName,Password, connectionID));
        if(u==null)
            usersOrderd.add(userName);
        return u==null;
    }
    public User getUserFromDB(String userName){
        return Users.get(userName);
    }
}
