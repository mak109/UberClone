package com.example.mayukh.uberclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button btnRequestCar,btnBeep;
    private boolean isUberCancelled = true;
    private boolean isCarReady = false;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnRequestCar = findViewById(R.id.btnRequestCar);
        btnBeep = findViewById(R.id.btnBeep);
        btnRequestCar.setOnClickListener(this);
        btnBeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDriverUpdates();
            }
        });

        ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size() >0 && e == null){
                    isUberCancelled = false;
                    btnRequestCar.setText("CANCEL YOUR UBER ORDER");
                    getDriverUpdates();
                }
            }
        });

        findViewById(R.id.btnLogOutFromPassengerActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            startActivity(new Intent(PassengerActivity.this,MainActivity.class));
                            finish();

                        }
                    }
                });
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateCameraPassengerLocation(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PassengerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    private void updateCameraPassengerLocation(Location pLocation) {
        if (isCarReady == false) {
            LatLng passengerLocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 10));
            mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here!!!!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }
    }

    @Override
    public void onClick(View view) {
        if(isUberCancelled)
        {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location passengerCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (passengerCurrentLocation != null) {
                    ParseObject requestCar = new ParseObject("RequestCar");
                    requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(), passengerCurrentLocation.getLongitude());
                    requestCar.put("passengerLocation", userLocation);
                    requestCar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(PassengerActivity.this, "Car Request has been sent", Toast.LENGTH_SHORT).show();
                                btnRequestCar.setText("CANCEL YOUR UBER ORDER");
                                isUberCancelled = false;
                            } else
                                FancyToast.makeText(PassengerActivity.this, e.getMessage(), FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                        }
                    });
                } else {
                    FancyToast.makeText(this, "Unknown Error. Something went wrong!!!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                }
            }
            }

        }
        else {
            ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> usersList, ParseException e) {
                    if(usersList.size() >0 && e == null){
                        isUberCancelled = true;
                        btnRequestCar.setText("REQUEST A NEW ORDER");

                        for(ParseObject user : usersList){
                            user.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null)
                                        Toast.makeText(PassengerActivity.this,"Request/s deleted",Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                }
            });
        }
    }

private void getDriverUpdates(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ParseQuery<ParseObject> uberRequestQuery = ParseQuery.getQuery("RequestCar");
                uberRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                uberRequestQuery.whereEqualTo("requestAccepted",true);
                uberRequestQuery.whereExists("driverOfMe");

                uberRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(objects.size() > 0 && e == null){
                            isCarReady = true;
                            for(final ParseObject requestObject : objects){
                                ParseQuery<ParseUser> driverQuery = ParseUser.getQuery();
                                driverQuery.whereEqualTo("username",requestObject.getString("driverOfMe"));
                                driverQuery.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> drivers, ParseException e) {
                                        for(ParseUser driverOfRequest : drivers) {
                                            ParseGeoPoint driverOfRequestLocation = driverOfRequest.getParseGeoPoint("driverLocation");
                                            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                Location passengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                                ParseGeoPoint pLocationAsParseGeoPoint = new ParseGeoPoint(passengerLocation.getLatitude(), passengerLocation.getLongitude());
                                                double milesDistance = driverOfRequestLocation.distanceInMilesTo(pLocationAsParseGeoPoint);

                                                if (milesDistance < 0.3) {

                                                    requestObject.deleteInBackground(new DeleteCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if (e == null) {
                                                                Toast.makeText(PassengerActivity.this, "Your Uber is Ready!!!", Toast.LENGTH_SHORT).show();
                                                                isCarReady = false;
                                                                isUberCancelled = true;
                                                                btnRequestCar.setText("You can request for a new order");
                                                            }
                                                        }
                                                    });
                                                } else {

                                                    float roundedDistance = Math.round(milesDistance * 10) / 10;
                                                    Toast.makeText(PassengerActivity.this, requestObject.get("driverOfMe") + " is " + roundedDistance + " miles away from you. Please wait!", Toast.LENGTH_SHORT).show();
                                                    LatLng pLocation = new LatLng(passengerLocation.getLatitude(), passengerLocation.getLongitude());


                                                    LatLng dLocation = new LatLng(driverOfRequestLocation.getLatitude(), driverOfRequestLocation.getLongitude());
                                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                    Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
                                                    Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location"));
                                                    ArrayList<Marker> myMarkers = new ArrayList<>();
                                                    myMarkers.add(driverMarker);
                                                    myMarkers.add(passengerMarker);

                                                    for (Marker marker : myMarkers) {
                                                        builder.include(marker.getPosition());
                                                    }
                                                    LatLngBounds bounds = builder.build();

                                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                                                    mMap.animateCamera(cameraUpdate);

                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }else {
                            isCarReady = false;
                        }
                    }
                });

            }
        },0,3000);


}
}

