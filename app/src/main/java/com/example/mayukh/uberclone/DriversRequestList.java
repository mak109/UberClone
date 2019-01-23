package com.example.mayukh.uberclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class DriversRequestList extends AppCompatActivity implements View.OnClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView listView;
    private ArrayList<String> nearbyDriveRequests;
    private ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_request_list);
        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

        listView = findViewById(R.id.requestListView);
        nearbyDriveRequests = new ArrayList<>();
        adapter = new ArrayAdapter(DriversRequestList.this,android.R.layout.simple_list_item_1,nearbyDriveRequests);
        listView.setAdapter(adapter);
        nearbyDriveRequests.clear();
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(DriversRequestList.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try {


            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOutDriverItem:
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            startActivity(new Intent(DriversRequestList.this,MainActivity.class));
                            finish();

                        }
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateRequestList(location);

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
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestList(currentDriverLocation);
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(DriversRequestList.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DriversRequestList.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            } else {
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestList(currentDriverLocation);
            }
        }
    }

    private void updateRequestList(Location driverLocation) {
        if(driverLocation != null){
            nearbyDriveRequests.clear();
            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(),driverLocation.getLongitude());

            ParseQuery<ParseObject> requestQuery = ParseQuery.getQuery("RequestCar");
            requestQuery.whereNear("passengerLocation",driverCurrentLocation);
             requestQuery.findInBackground(new FindCallback<ParseObject>() {
                 @Override
                 public void done(List<ParseObject> objects, ParseException e) {
                     if(e == null) {
                         if (objects.size() > 0) {
                             for (ParseObject nearRequest : objects) {
                                 Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo((ParseGeoPoint) nearRequest.get("passengerLocation"));
                                 float roundedDistance = Math.round(milesDistanceToPassenger * 10) / 10;
                                 nearbyDriveRequests.add("There are " + roundedDistance + " miles to " + nearRequest.get("username"));
                             }

                         }else {
                             FancyToast.makeText(DriversRequestList.this,"Sorry. There are no requests yet",FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();

                         }
                         adapter.notifyDataSetChanged();
                     }

                 }
             });

        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(DriversRequestList.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                updateRequestList(currentDriverLocation);
            }
        }
    }
}
