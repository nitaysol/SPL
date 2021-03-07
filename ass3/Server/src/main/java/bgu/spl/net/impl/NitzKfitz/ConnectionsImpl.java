package bgu.spl.net.impl.NitzKfitz;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {
    private AtomicInteger id;
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionHandlers;
    public ConnectionsImpl()
    {
        connectionHandlers=new ConcurrentHashMap<>();
        id = new AtomicInteger(0);
    }


    public boolean send(int connectionId, T msg)
    {

        ConnectionHandler<T> currentCH = connectionHandlers.get(connectionId);
        if (currentCH != null) {
		synchronized(currentCH){
                	currentCH.send(msg);
                	return true;
		}
        }
        return false;
    }

    public int addConnection(ConnectionHandler<T> CH) //add Connection to the ConnectionHandler
    {
        int current_id = id.getAndIncrement();
        connectionHandlers.put(current_id, CH);
        return current_id;
    }

    public void broadcast(T msg)
    {
        Iterator<Integer> it=connectionHandlers.keySet().iterator();
        while(it.hasNext()){
            send(it.next(),msg);
        }
    }



    public void disconnect(int connectionId)
    {
            connectionHandlers.remove(connectionId);
    }
}
