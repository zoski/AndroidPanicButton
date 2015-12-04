package fr.zoski.gael.panic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        //StrictMode.setThreadPolicy(policy);

        /* Getting GPS position */
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        /* WebSocket things */
        if(isNetworkAvailable()) {
            System.out.println("Network available");
            try{
                uri = new URI("ws://zoski.fr:9555");
                System.out.println("Socket created : " + uri.toString());
            } catch(URISyntaxException e) {
                e.printStackTrace();
            }

            socketClient = new SocketClient(uri, new Draft_10() );
            System.out.println("Go .connect()");
            socketClient.connect();
            System.out.println("Socket is connecting :" + socketClient.isConnecting() + "\nSocket is open :" + socketClient.isOpen());
        }
        else {
            Toast.makeText(this, "Network un available", Toast.LENGTH_SHORT).show();
        }


    }

    public void onClick(View view) {
        /* Contacting server */
        System.out.println("Trying to send message " + msg);
        if(socketClient.isOpen()!=true) {
            Toast.makeText(this, "Not connected, can't send message", Toast.LENGTH_SHORT).show();
        }
        else {
            socketClient.send(msg);
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        }
    }


    protected synchronized void buildGoogleApiClient() {
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
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
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

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}