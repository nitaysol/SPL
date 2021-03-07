package bgu.spl.net.impl.NitzKfitz;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.NitzKfitz.Commands.*;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Vector;


public class BidiMessaginProtocolImp implements BidiMessagingProtocol<String> {

    private boolean shouldTerminate = false;
    private int connectionId;
    private String userName=null;
    private Connections<String> connections;
    private DataBaseSimulator DataBase;



    public BidiMessaginProtocolImp(DataBaseSimulator DataBase)
    {
        this.DataBase=DataBase;

    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionId=connectionId;
        this.connections= connections;
    }

    @Override
    public void process(String message) {
        String[] Message=message.split(" ");
        switch(Message[0]){
            case "REGISTER":
                Register Reg=new Register(Message[1],Message[2],DataBase,connectionId);
                connections.send(connectionId,Reg.execute());
                break;

            case "LOGIN":
                Login Lgin=new Login(Message[1],Message[2], DataBase, connectionId);
                if(userName==null) {
                    String answer = Lgin.execute();
                    connections.send(connectionId, answer);
                    if (answer.startsWith("ACK")) {
                        userName = Message[1];
                        //Sending him POST/PM Messages
                        //When user login and his MessageQueue is not empty - should push notifications of PM/Post when relevant
                        for (String UserMessage = DataBase.getUserFromDB(userName).getMessages().poll(); UserMessage != null; UserMessage = DataBase.getUserFromDB(userName).getMessages().poll()) {
                            connections.send(connectionId, UserMessage);
                        }
                    }
                }
                else{
                    //Someone is already logged in
                    connections.send(connectionId, "ERROR 2");
                }
                break;

            case "LOGOUT":
                Logout lgout=new Logout(userName,connectionId,DataBase, connections);
                String answer = lgout.execute();
                connections.send(connectionId,answer);
                if(answer.startsWith("ACK")) { //only if success LOGOUT
                    connections.disconnect(connectionId);
                }
                userName=null;
                break;

            case "FOLLOW":
                Vector<User> UsersToFollow=new Vector<>();
                for(int i=3;i<Message.length;i++) { //add to follow list of the user
                    User userToFollow = DataBase.getUserFromDB(Message[i]);
                    if (userToFollow != null)
                        synchronized (userToFollow) {
                            if (!UsersToFollow.contains(userToFollow)) //check if the specific userToFollow is already in the list
                                UsersToFollow.add(DataBase.getUserFromDB(Message[i]));
                        }
                }
                boolean FollowUnFollow=Message[1].equals("0"); //check if the command is Follow/UnFollow
                Follow flw=new Follow(userName,FollowUnFollow,Integer.parseInt(Message[2]),UsersToFollow,DataBase);
                    connections.send(connectionId, flw.execute());

                break;

            case "POST":
                if(userName==null)
                    connections.send(connectionId,"ERROR 5");
                else {
                    Vector<User> Followers = DataBase.getUserFromDB(userName).getFollowers();
                    Vector<User> usersToPost = new Vector<>();
                    synchronized (Followers){
                        for(User follower: Followers) {
                                usersToPost.add(follower);//when POST - all followers should get the POST

                        }
                    }
                    for (int i = 1; i < Message.length; i++) {
                        if (Message[i].startsWith("@")) { //handling the case that someone TAG in the POST message
                            String userNameToPost = Message[i].substring(1);
                            if (DataBase.getUserFromDB(userNameToPost) != null) {
                                if(!usersToPost.contains(DataBase.getUserFromDB(userNameToPost))) { //add to userToPost only if the user not already in that list
                                    usersToPost.add(DataBase.getUserFromDB(userNameToPost));
                                }
                            }
                        }
                    }
                    Post pst = new Post(userName, message.replaceFirst("POST ", ""), usersToPost, DataBase, connections);
                    connections.send(connectionId, pst.execute());
                }
                break;

            case "PM":
                PM pm=new PM(userName,message,Message[1],DataBase, connections);
                connections.send(connectionId,pm.execute());
                break;

            case "USERLIST":
                UserListMessages userLmessages=new UserListMessages(userName,DataBase);
                    connections.send(connectionId,userLmessages.execute());
                break;

            case "STAT":
                STAT stt=new STAT(userName, Message[1],DataBase);
                connections.send(connectionId,stt.execute());
                break;
        }
    }

    @Override
    public boolean shouldTerminate() {
        return this.shouldTerminate;
    }
}
