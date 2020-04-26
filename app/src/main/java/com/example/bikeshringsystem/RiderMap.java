package com.example.bikeshringsystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
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

import java.util.List;
import java.util.Map;


public class RiderMap extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    LocationRequest locationRequest;
    private final float DEFAULT_ZOOM = 15;
    private boolean mLocationPermissionGranded;
    public static final int PERMISSION_REQUEST_CODE = 9001;
    private Button mlogout,mRequest;
    private String customerId="";
    private LinearLayout mCostumerInfo;
    private ImageView mCostumerProfileImage;
    private TextView mCostumerName,mCostumerPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
             mlogout=(Button)findViewById(R.id.logout);
             mCostumerInfo=(LinearLayout)findViewById(R.id.CustomerInfo);
             mCostumerProfileImage=(ImageView)findViewById(R.id.customerProfileImage);
             mCostumerName=(TextView)findViewById(R.id.CustomerName);
        mCostumerPhone=(TextView)findViewById(R.id.CustomerPhone);
                 mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(RiderMap.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        if(mLocationPermissionGranded){
            Toast.makeText(this, "Ready to Map!", Toast.LENGTH_SHORT).show();
        }else
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(RiderMap.this);
        getAssigenCustomer();

    }

    private void getAssigenCustomer() {
        String driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("CustomerRideID");
        assignCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    customerId=dataSnapshot.getValue().toString();
                    getAssigenCustomerInfo();
                    getAssigenCustomerPickUplocation();



                }
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
                    mCostumerInfo.setVisibility(View.GONE);
                    mCostumerName.setText("");
                    mCostumerPhone.setText("");
                    mCostumerProfileImage.setImageResource(R.mipmap.ic_default_user);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getAssigenCustomerInfo() {

        DatabaseReference mCustomerDataBase= FirebaseDatabase.getInstance().getReference("Users").child("Customers").child(customerId);

        mCustomerDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                    if(map.get("Name")!=null)
                    {
                        String name=map.get("Name").toString();
                        mCostumerName.setText("Name: "+name);

                    }
                    if(map.get("Phone")!=null)
                    {
                        String phone=map.get("Phone").toString();
                        mCostumerPhone.setText("Phone No: "+phone);

                    }
                   if(map.get("profileImageUrl")!=null)
                    {

                        Glide.with(getApplication()).load( map.get("profileImageUrl").toString()).into(mCostumerProfileImage);

                    }
                    mCostumerInfo.setVisibility(View.VISIBLE);
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
    private void getAssigenCustomerPickUplocation() {
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
                     pickMarker=mMap.addMarker(new MarkerOptions().position(driverLatlng).title("Pickup Location"));

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
                                         //if (!(geoFireWorking == null)) {
                                            /* geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                                                 @Override
                                                 public void onComplete(String key, DatabaseError error) {
                                                     if (error != null) {
                                                         System.err.println("There was an error removing the location from GeoFire: " + error);

                                                     } else {
                                                         System.out.println("Location removed on server successfully!");

                                                     }
                                                 }
                                             });
                                         }*/
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
                                              /*if (!(geoFireAvailable == null)) {
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
                                              }*/
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


}
