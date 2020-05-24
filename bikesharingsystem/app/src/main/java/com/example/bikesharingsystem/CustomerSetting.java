package com.example.bikesharingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CustomerSetting extends AppCompatActivity {
    TextView tname,tphone;
    Button update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tname=findViewById(R.id.name);
        tphone=findViewById(R.id.phone);
        update=findViewById(R.id.Updatebtn);
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(uid);
       ref.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tname.setText(dataSnapshot.child("name").getValue(String.class));
                tphone.setText(dataSnapshot.child("phoneno").getValue(String.class));
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });



       //Update profile
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                String uname=tname.getText().toString();
                FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(uid).child("name").setValue(uname);
                String uphone=tphone.getText().toString();
                FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(uid).child("phoneno").setValue(uphone);
                Toast.makeText(CustomerSetting.this, "Profile successfully updated", Toast.LENGTH_SHORT).show();

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
