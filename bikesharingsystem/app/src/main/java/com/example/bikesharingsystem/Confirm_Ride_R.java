package com.example.bikesharingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Confirm_Ride_R extends AppCompatActivity {
    TextView tname,tphone,tsource,tdest;
    String name,phone,source,destinaton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm__ride__r);
        tname=findViewById(R.id.tname);
        tphone=findViewById(R.id.tphone);
        tsource=findViewById(R.id.tsource);
        tdest=findViewById(R.id.tdest);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String time=new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Offers").child(uid).child(currentDate+";"+time).child("Date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Added date in database
            }
        });

        FirebaseDatabase.getInstance().getReference("Offers").child(uid).child(currentDate+";"+time).child("Time").setValue(currentTime).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Added time in database
            }
        });

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Offers").child(uid).child(currentDate+";"+time);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String cust_id=dataSnapshot.child("Customer").getValue(String.class);
                source=dataSnapshot.child("source").getValue(String.class);
                destinaton=dataSnapshot.child("destination").getValue(String.class);

                //get Customer details
                DatabaseReference cust=FirebaseDatabase.getInstance().getReference("Users").child("Customers").child(cust_id);
                cust.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        name=dataSnapshot.child("name").getValue(String.class);
                        phone=dataSnapshot.child("phoneno").getValue(String.class);
                        tname.setText(name);
                        tphone.setText(phone);
                        tsource.setText("   Pickup : "+source);
                        tdest.setText("   To: "+destinaton);
                        remove_offer();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //get Customer details
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void remove_offer(){
        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriverAvailable").child(uid);
        ref.setValue(null);

        ref=FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(uid);
        ref.setValue(null);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(Confirm_Ride_R.this,Rider.class));
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
