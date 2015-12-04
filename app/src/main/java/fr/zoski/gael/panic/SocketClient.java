package fr.zoski.gael.panic;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by gael on 04/12/15.
 */
public class SocketClient extends WebSocketClient {

    public SocketClient(URI serverURI) {
        super(serverURI);
    }

    public SocketClient(URI serverURI, Draft draft) {
        super(serverURI, draft);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("opened connection" );
    }

    @Override
    public void onMessage(String s) {
        System.out.println( "received: " + s );
    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {
        System.out.println("ERROR");
        e.printStackTrace();
    }
}
