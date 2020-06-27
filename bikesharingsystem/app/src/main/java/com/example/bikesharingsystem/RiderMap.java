package com.example.bikesharingsystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class RiderMap extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    LocationRequest locationRequest;
    private final float DEFAULT_ZOOM = 15;
    private boolean mLocationPermissionGranded;
    public static final int PERMISSION_REQUEST_CODE = 9001;
    private Button mRequest,confirm;
    private String customerId="";
    TextView cust_details;
    CardView card;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_map);
        card=findViewById(R.id.cardView);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //back button
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        cust_details=findViewById(R.id.editText2);
        confirm=findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {//Confirm booking of ride
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Confirm_Ride_R.class));
                finish();
            }
        });

        if(mLocationPermissionGranded){
            Toast.makeText(this, "Ready to Map!", Toast.LENGTH_SHORT).show();
        }
        else//Location Permission not granted,request for permission
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(RiderMap.this);
        getAssigenCustomer(); //Find nearby customer requesting ride

    }

    private void getAssigenCustomer() { //offering ride
        String driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("CustomerRideID");
        assignCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if customer found
                if(dataSnapshot.exists())
                {
                    //Get system date and time
                    String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                    String time=new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());

                    customerId=dataSnapshot.getValue().toString();
                    //set customer id in Offers database
                    String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference("Offers").child(uid).child(currentDate+";"+time).child("Customer").setValue(customerId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //successfully set the customer in Offers for this driver
                            }
                        }
                    });

                    DatabaseReference cust_name=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
                    cust_name.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String cust_name=dataSnapshot.child("name").getValue(String.class);
                            String cust_phone=dataSnapshot.child("phoneno").getValue(String.class);
                            cust_details.setText(cust_name+"    "+cust_phone);
                            confirm.setEnabled(true);
                            card.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    getAssigenCustomerPickUplocation();

                }
                //if customer not found
                else
                {
                    customerId="";
                    if(pickMarker!=null)
                    {
                        pickMarker.remove();
                    }
                    if(assigCustomerPickupLocationListener!=null) {
                        assigCustomerPickupLocation.removeEventListener(assigCustomerPickupLocationListener);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    Marker pickMarker;
    DatabaseReference assigCustomerPickupLocation;
    private ValueEventListener assigCustomerPickupLocationListener;

    private void getAssigenCustomerPickUplocation() {//get location of assigned customer
        assigCustomerPickupLocation=FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assigCustomerPickupLocationListener=assigCustomerPickupLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() &&!customerId.equals(""))
                {
                    List<Object>map=(List<Object>)dataSnapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;
                    if(map.get(0)!=null)
                    {
                        locationLat=Double.parseDouble(map.get(0).toString());

                    }
                    if(map.get(1)!=null)
                    {
                        locationLng=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatlng=new LatLng(locationLat,locationLng);
                    pickMarker=mMap.addMarker(new MarkerOptions().position(driverLatlng).title("Customer Pickup Location"));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranded=true;
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.setMyLocationEnabled(true);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(RiderMap.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(RiderMap.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete( Task<Location> task) {

                        if (task.isSuccessful()) {

                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));


                                String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference refAvailable= FirebaseDatabase.getInstance().getReference("DriverAvailable");
                                DatabaseReference refWorking= FirebaseDatabase.getInstance().getReference("driverWorking");
                                GeoFire geoFireAvailable=new GeoFire(refAvailable);
                                GeoFire geoFireWorking=new GeoFire(refWorking);

                                switch(customerId) {
                                    case "":
                                        if (!(geoFireWorking == null)) {
                                            geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                                                @Override
                                                public void onComplete(String key, DatabaseError error) {
                                                    if (error != null) {
                                                        System.err.println("There was an error removing the location from GeoFire: " + error);

                                                    } else {
                                                        System.out.println("Location removed on server successfully!");

                                                    }
                                                }
                                            });
                                        }
                                        // catch (Exception e){
                                        //  e.printStackTrace();
                                        //}
                                        geoFireAvailable.setLocation(userId, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                if (error != null) {

                                                } else {

                                                }
                                            }
                                        });
                                        break;
                                    default:
                                        if (!(geoFireAvailable == null)) {
                                            geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                                                @Override
                                                public void onComplete(String key, DatabaseError error) {
                                                    if (error != null) {
                                                        System.err.println("There was an error removing the location from GeoFire: " + error);

                                                    } else {
                                                        System.out.println("Location removed on server successfully!");

                                                    }
                                                }
                                            });
                                        }
                                        geoFireWorking.setLocation(userId, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                if (error != null) {

                                                } else {

                                                }
                                            }
                                        });


                                }

                            }

                            else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        }
                        else {
                            Toast.makeText(RiderMap.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


}
