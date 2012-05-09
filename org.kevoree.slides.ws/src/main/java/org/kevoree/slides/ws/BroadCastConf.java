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

    private Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();
    private static String keynoteID = "KEYID";
    private static String joinID = "JOIN";

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        connections.add(connection);
    }

    private void broadcast(String msg, WebSocketConnection conn, String roomID) {
        for (WebSocketConnection connection : connections) {
            if (!connection.equals(conn)) {
                if (connection.data(keynoteID) != null && connection.data(keynoteID).toString().equals(roomID)) {
                    connection.send(msg);
                }
            }
        }
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        connections.remove(connection);
    }

    public void onMessage(WebSocketConnection connection, String message) {
        System.out.println("on msg "+message);
        if (connection.data(keynoteID) != null) {
            broadcast(message, connection, connection.data(keynoteID).toString());
        } else {
            if(message.contains(joinID)){
                connection.data(keynoteID,message.replace(keynoteID,""));
            }
        }
    }

}
