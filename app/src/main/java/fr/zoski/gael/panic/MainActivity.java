package fr.zoski.gael.panic;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.java_websocket.drafts.Draft_10;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static String SERVER_URI = "ws://192.168.43.19:9555";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String lat;
    private String longu;
    private String msg;
    private SocketClient socketClient;
    private URI uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Getting GPS position throw Google API*/
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        /* WebSocket things */
        if (isNetworkAvailable()) {
            System.out.println("Network available");
            connect();
        } else {
            Toast.makeText(this, "Network un available", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick(View view) {
        /* Contacting server */
        System.out.println("Trying to send message " + msg);
        if (socketClient.isOpen() != true) {
            Toast.makeText(this, "Not connected, can't send message\n Trying to reconnect", Toast.LENGTH_SHORT).show();
            connect();
        } else {
            socketClient.send(msg);
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            lat = String.valueOf(mLastLocation.getLatitude());
            longu = String.valueOf(mLastLocation.getLongitude());
            msg = lat + " " + longu;
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        System.out.println("API connected");
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        socketClient.close();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connection failed");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Does the connection part.
     * Create an URI, then a socket and open a connection with the remote server.
     */
    private void connect() {
        try {
            uri = new URI(SERVER_URI);
            System.out.println("Socket created : " + uri.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socketClient = new SocketClient(uri, new Draft_10());
        System.out.println("Go .connect()");
        socketClient.connect();
        System.out.println("Socket is connecting :" + socketClient.isConnecting() +
                "\nSocket is open :" + socketClient.isOpen());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }
}