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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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

public class DriversRequestList extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView listView;
    private ArrayList<String> nearbyDriveRequests;
    private ArrayList<Double> passengersLatitude;
    private ArrayList<Double> passengersLongitude;
    private ArrayList<String> requestcarUsernames;
    private ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_request_list);
        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

        listView = findViewById(R.id.requestListView);
        listView.setOnItemClickListener(this);
        nearbyDriveRequests = new ArrayList<>();
        passengersLatitude = new ArrayList<>();
        passengersLongitude = new ArrayList<>();
        requestcarUsernames = new ArrayList<>();
        adapter = new ArrayAdapter(DriversRequestList.this,android.R.layout.simple_list_item_1,nearbyDriveRequests);
        listView.setAdapter(adapter);
        nearbyDriveRequests.clear();
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(DriversRequestList.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            initializeLocationListener();
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

        if (Build.VERSION.SDK_INT < 23) {
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
            saveDriverLocationToParse(driverLocation);

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(),driverLocation.getLongitude());

            ParseQuery<ParseObject> requestQuery = ParseQuery.getQuery("RequestCar");
            requestQuery.whereNear("passengerLocation",driverCurrentLocation);
            requestQuery.whereDoesNotExist("driverOfMe");
             requestQuery.findInBackground(new FindCallback<ParseObject>() {
                 @Override
                 public void done(List<ParseObject> objects, ParseException e) {
                     if(e == null) {
                         if (objects.size() > 0) {
                             if(nearbyDriveRequests.size() >0){
                                 nearbyDriveRequests.clear();
                             }
                             if(passengersLatitude.size() >0){
                                 passengersLatitude.clear();
                             }
                             if(passengersLongitude.size() >0){
                                 passengersLongitude.clear();
                             }
                             if(requestcarUsernames.size() >0){
                                 requestcarUsernames.clear();
                             }
                             for (ParseObject nearRequest : objects) {
                                 ParseGeoPoint pLocation = (ParseGeoPoint) nearRequest.get("passengerLocation");
                                 Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo(pLocation);
                                 float roundedDistance = Math.round(milesDistanceToPassenger * 10) / 10;
                                 nearbyDriveRequests.add("There are " + roundedDistance + " miles to " + nearRequest.get("username"));
                                 passengersLatitude.add(pLocation.getLatitude());
                                 passengersLongitude.add(pLocation.getLongitude());
                                 requestcarUsernames.add(nearRequest.get("username")+"");

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
                initializeLocationListener();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
               Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestList(currentDriverLocation);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        if(ContextCompat.checkSelfPermission(DriversRequestList.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (cdLocation != null) {
                Intent intent = new Intent(this, ViewLocationsMapActivity.class);
                intent.putExtra("dLatitude", cdLocation.getLatitude());
                intent.putExtra("dLongitude", cdLocation.getLongitude());
                intent.putExtra("pLatitude", passengersLatitude.get(position));
                intent.putExtra("pLongitude", passengersLongitude.get(position));
                intent.putExtra("rUsername",requestcarUsernames.get(position));
                startActivity(intent);
            }
        }

    }

    private void initializeLocationListener(){
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

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
    }
    private void saveDriverLocationToParse(Location location)
    {
        ParseUser driver = ParseUser.getCurrentUser();
        ParseGeoPoint driverLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
        driver.put("driverLocation",driverLocation);
        driver.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Toast.makeText(DriversRequestList.this,"Location Saved",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
