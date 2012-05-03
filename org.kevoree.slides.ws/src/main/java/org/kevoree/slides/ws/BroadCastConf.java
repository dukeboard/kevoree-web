package org.kevoree.slides.ws;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 03/05/12
 * Time: 15:10
 */
public class BroadCastConf extends BaseWebSocketHandler {

    private int connectionCount;
    private Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        connections.add(connection);
    }

    private void broadcast(String msg) {
        for (WebSocketConnection connection : connections) {
            connection.send(msg);
        }
    }


    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        connections.remove(connection);
    }


    public void onMessage(WebSocketConnection connection, String message) {
       broadcast(message);
    }

}
