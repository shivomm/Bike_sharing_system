package com.example.bikeshringsystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.util.HashMap;
import java.util.List;


public class CustomerMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private final float DEFAULT_ZOOM = 12;
    private boolean mLocationPermissionGranded;
    public static final int PERMISSION_REQUEST_CODE = 9001;
    private Button mlogout,mRequest;
    private LatLng pickupLocation;
    private boolean requestBol=false;
    private  Marker pickupMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.cmap);
        mapFragment.getMapAsync(this);
        mlogout = (Button) findViewById(R.id.clogout);
        mRequest = (Button) findViewById(R.id.Request);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CustomerMap.this);


        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMap.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestBol)
                {
                    requestBol=false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);
                    if(driverFoundID!=null)
                    {

                        DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                        driverRef.setValue(true);
                        driverFoundID=null;

                    }
                    driverFound=false;
                    radius=1;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                System.err.println("There was an error removing the location from GeoFire: " + error);

                            } else {
                                System.out.println("Location removed on server successfully!");

                            }
                        }
                    });
                    if(pickupMarker!=null)
                    {
                        pickupMarker.remove();
                    }
                    mRequest.setText("call For Ride");

                }else {
                    requestBol=true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Toast.makeText(CustomerMap.this, "Location is Not saved ", Toast.LENGTH_SHORT).show();
                            } else {
                                pickupLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                               pickupMarker=mMap.addMarker(new MarkerOptions().position(pickupLocation).title("PickupHere"));
                                getClosestDriver();
                                mRequest.setText("Getting your Driver...");


                            }
                        }
                    });
                }



            }
        });

        if (mLocationPermissionGranded) {
            Toast.makeText(this, "Ready to Map!", Toast.LENGTH_SHORT).show();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }
        }
    }
          private int radius=1;
    private boolean driverFound=false;
    private String driverFoundID;
    GeoQuery geoQuery;
    private void getClosestDriver() {
        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
        GeoFire geoFire=new GeoFire(driverLocation);
        geoQuery=geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
       geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBol) {
                    driverFound = true;
                    driverFoundID=key;
                    DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    String customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map=new HashMap();
                    map.put("CustomerRideID",customerID);
                    driverRef.updateChildren(map);

                    mRequest.setText("Loooking for Driver Location....");
                    getDriverLocation();

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }


            }


            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mDriverMarker;
    DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation() {
        driverLocationRef=FirebaseDatabase.getInstance().getReference().child("driverWorking").child(driverFoundID).child("l");
        driverLocationRefListener=driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol)
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
                      /*if(mDriverMarker!=null)
                      {
                          mDriverMarker.remove();
                      }*/
                    mMap.addMarker(new MarkerOptions().position(driverLatlng).title("YourDriver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bike)));
                      Location loc1=new Location("");
                      loc1.setLatitude(pickupLocation.latitude);
                      loc1.setLongitude(pickupLocation.longitude);
                      Location loc2=new Location("");
                        loc2.setLatitude(driverLatlng.latitude);
                        loc2.setLongitude(driverLatlng.longitude);
                        float distance=loc1.distanceTo(loc2);
                        if(distance<100.0)
                        {
                            mRequest.setText("Driver Here ");
                        }
                          else {
                            mRequest.setText("Driver Found  " + String.valueOf(distance));
                        }



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

        SettingsClient settingsClient = LocationServices.getSettingsClient(CustomerMap.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(CustomerMap.this, new OnSuccessListener<LocationSettingsResponse>() {
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

                            } else {
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
                        } else {
                            Toast.makeText(CustomerMap.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });




    }


}
