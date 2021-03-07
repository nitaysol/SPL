package bgu.spl.net.impl.NitzKfitz;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class User {
    private String userName;
    private String password;
    private Vector<User> following;
    private ConcurrentLinkedQueue<String> messages;
    private Vector<User> followers;
    private int connectionID;
    private AtomicBoolean isLoggedIn;
    private AtomicInteger numOfPosts;

    public User(String userName, String password, int connectionID){
        this.userName=userName;
        this.password=password;
        following = new Vector<>();
        messages = new ConcurrentLinkedQueue<>();
        followers = new Vector<>();
        this.connectionID=connectionID;
        isLoggedIn = new AtomicBoolean(
                false);
        numOfPosts = new AtomicInteger(0);
    }
    public String getUserName(){
        return this.userName;
    }
    public String getPassword(){
        return this.password;
    }
    public boolean getIsLoggedIn(){
        return this.isLoggedIn.get();
    }
    public void logout(){
        isLoggedIn.set(false);
    }
    public void login(int connectionID){
        isLoggedIn.set(true);
        this.connectionID=connectionID;
    }
    public boolean addFollower(User u){
        return addToFollowersFollowingVec(u, followers);
    } //add follower to user
    public boolean removeFollower(User u){
        return removeFromFollowersFollowingVec(u,followers);
    }
    public boolean addFollowing(User u){
        return addToFollowersFollowingVec(u,following);
    } //add following to user
    public boolean removeFollowing(User u){
        return removeFromFollowersFollowingVec(u, following);
    }
    private boolean addToFollowersFollowingVec(User u, Vector<User> vec) //if success add following/follower
    {
        synchronized (vec) {
            if(vec.contains(u)) return false;
            vec.add(u);
        }
        return true;
    }
    private boolean removeFromFollowersFollowingVec(User u, Vector<User> vec) //if success remove following/follower
    {
        synchronized (vec) {
            return vec.remove(u);
        }

    }
    public void addToMessagesQueue(String s)
    {
        messages.add(s);
    } //add To MessageQueue if needed
    public String takeFromMessages(){
        return messages.poll();
    }
    public void addToNumOfPosts(){
        numOfPosts.incrementAndGet();
    } //save num of user post
    public int getNumOfPosts(){
        return this.numOfPosts.get();
    }
    public Vector<User> getFollowing(){
        return this.following;
    }

    public Vector<User> getFollowers(){
        return this.followers;
    }

    public ConcurrentLinkedQueue<String> getMessages(){return this.messages;}

    public int getConnectionId() { return this.connectionID; }
    }