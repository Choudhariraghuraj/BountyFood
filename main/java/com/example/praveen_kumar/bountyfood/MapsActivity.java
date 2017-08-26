package com.example.praveen_kumar.bountyfood;

/**
 * Created by PRAVEEN_KUMAR on 18-08-2017.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        View.OnClickListener {

    //Our Map
    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    //To store longitude and latitude from map
    private double longitude;
    private double latitude;
    private double desLatitude;
    private double desLongitude;
    private ImageButton buttonCurrent;
    //Google ApiClient
    private GoogleApiClient googleApiClient;
    final int MARKER_UPDATE_INTERVAL = 300000; /* milliseconds */
    final double MAXIMUM_LIMIT_INTERVAL = 1.2e+6;
    double LIMIT_INTERVAL=0;
    Handler handler = new Handler();
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference agentLatLng = database.getReference("agentLatLng");
    DatabaseReference customerLatLng = database.getReference("customerLatLng");
    DatabaseReference DelivereFlag = database.getReference("DelivereFlag");
    String mode="";
    String msg="";
    String modeFlag="";
    String DelivereFlagRes ="";
    String NotificationBody="";
    LatLng latLng,deslatLng;
    OkHttpClient mClient = new OkHttpClient();
    JSONArray jsonArray = new JSONArray();
    Runnable updateMarker = new Runnable() {
        @Override
        public void run() {
            getCurrentLocation();
            //moveMap();
            handler.postDelayed(this, MARKER_UPDATE_INTERVAL);
            LIMIT_INTERVAL += MARKER_UPDATE_INTERVAL;
        }
    };
    private Object Url;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            NotificationBody = intent.getStringExtra("NotificationBody");
            System.out.println("Message Notification Body RAGHU "+NotificationBody);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);
        // Initializing
        MarkerPoints = new ArrayList<>();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();//add your user refresh tokens who are logged in with firebase.

        jsonArray.put(refreshedToken);
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        System.out.println("raghu "+refreshedToken);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing googleapi client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        handler.postDelayed(updateMarker, MARKER_UPDATE_INTERVAL);

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    //Getting current location
    private void getCurrentLocation() {
        mMap.clear();
        //Creating a location object
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            latLng = new LatLng(latitude, longitude);
            MarkerPoints.add(latLng);
            if(mode.equals("customer")){

                if(NotificationBody.equals("We couldn't reach you in time due to some reasons")){
                    Toast.makeText(this, "Sorry!!! "+NotificationBody, Toast.LENGTH_LONG).show();
                }
                msg = "Agent Not yet Confirmed delivery";
                modeFlag="Agent Location";
                customerLatLng.setValue(latLng);
                    // Read from the database
                    agentLatLng.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            desLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                            desLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                            DelivereFlagRes = dataSnapshot.child("DelivereFlag").getValue(String.class);
                            deslatLng = new LatLng(desLatitude, desLongitude);
                            MarkerPoints.add(deslatLng);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });
               // }
            }else if(mode.equals("agent")){
                msg = "Customer Not yet Confirmed Order";
                modeFlag="Customer Location";
                agentLatLng.setValue(latLng);

                // Read from the database
                customerLatLng.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        desLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                        desLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                        deslatLng = new LatLng(desLatitude, desLongitude);

                        MarkerPoints.add(deslatLng);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });

            }
            //moving the map to location
            moveMap();
        }
    }

    //Function to move the map
    private void moveMap() {
        if(MarkerPoints.size()==1){
            Marker marker =mMap.addMarker(new MarkerOptions()
                    .position(latLng) //setting position
                    .draggable(true) //Making the marker draggable
                    .title(mode).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            marker.showInfoWindow();

        }
            //Adding marker to map
            if(MarkerPoints.size()==2){

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng) //setting position
                        .draggable(true) //Making the marker draggable
                        .title("My Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                marker.showInfoWindow();
                Marker marker1 = mMap.addMarker(new MarkerOptions()
                        .position(deslatLng) //setting position
                        .draggable(true) //Making the marker draggable
                        .title(modeFlag).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

                marker1.showInfoWindow();
            }

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //Animating the camera
        centerIncidentRouteOnMap(MarkerPoints);
        MarkerPoints.clear();
        if(mode.equals("customer")){
            if(DelivereFlagRes == "AgentCOnfirmed"){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Thank You!!! Your Order Successfully deliverd...")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DelivereFlag.setValue("CustomerCOnfirmed");
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            if (distance(latitude, longitude, desLatitude, desLongitude) < 0.3) {
                // if distance < 0.1 miles we take locations as equal
                Toast.makeText(this, "Delivery agent reached you! Grab your Order", Toast.LENGTH_LONG).show();
            }else{
                if(MAXIMUM_LIMIT_INTERVAL == LIMIT_INTERVAL){
                    sendMessage(jsonArray,"Sorry!!!","We couldn't reach you in time due to some reasons","Http:\\google.com","hello");
                }
            }
        }else if(mode.equals("agent")){
            if (distance(latitude, longitude, desLatitude, desLongitude) < 0.3) {
                // if distance < 0.3(500 meters) miles we take locations as equal
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Confirm Delivered")
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DelivereFlag.setValue("AgentCOnfirmed");
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }

    }
    /** calculates the distance between two locations in MILES */
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;
        System.out.println("raghu distance "+dist);
        return dist; // output distance, in MILES
    }

    public void centerIncidentRouteOnMap(ArrayList<LatLng> MarkerPoints) {
        double minLat = Integer.MAX_VALUE;
        double maxLat = Integer.MIN_VALUE;
        double minLon = Integer.MAX_VALUE;
        double maxLon = Integer.MIN_VALUE;
        for (LatLng point : MarkerPoints) {
            maxLat = Math.max(point.latitude, maxLat);
            minLat = Math.min(point.latitude, minLat);
            maxLon = Math.max(point.longitude, maxLon);
            minLon = Math.min(point.longitude, minLon);
        }
        final LatLngBounds bounds = new LatLngBounds.Builder().include(new LatLng(maxLat, maxLon)).include(new LatLng(minLat, minLon)).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);

    }

    @Override
    public void onConnected(Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("myFunction"));
    }
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //Clearing all the markers
        mMap.clear();

        //Adding a new marker to the current pressed position
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        //Getting the coordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //Moving the map
        moveMap();
    }

    @Override
    public void onClick(View v) {
        if(v == buttonCurrent){
            getCurrentLocation();
            moveMap();
        }
    }

    public void sendMessage(final JSONArray recipients, final String title, final String body, final String icon, final String message) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    System.out.println("raghu body "+body);
                    System.out.println("raghu  title "+title);
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
                    data.put("message", message);
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);
                    System.out.println("raghu  recipients "+recipients);
                    String result = postToFCM(root.toString());
                    System.out.println("raghu Main Activity Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    //Toast.makeText(MainActivity.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    //Toast.makeText(MainActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
// Post Notification body with server key
    String postToFCM(String bodyString) throws IOException {

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, bodyString);
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("Authorization", "key=" + "AAAAYYOwbfo:APA91bHZc-R8dAN9si6usmeBTngip4Qyisc6O0LUnxFObGuuqHXppNEKyKSbWfEMhjIvsc26GGMbjLA8AaniDZAM7LQv2Ems9_PmihdnMMuvyeGTcbXbBEgVxxUxJOm_M-pRa2jBYa4k")
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

}
