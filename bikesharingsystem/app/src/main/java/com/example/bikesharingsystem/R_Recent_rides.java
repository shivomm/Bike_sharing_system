package com.example.bikesharingsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class R_Recent_rides extends AppCompatActivity {

    ListView mylist;
    ArrayList<String> myArrayList=new ArrayList<>();
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_rides);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        final ArrayAdapter<String> myArrayAdapter=new ArrayAdapter<String>(R_Recent_rides.this,R.layout.mytextview,myArrayList);
        mylist=findViewById(R.id.listView1);
        mylist.setAdapter(myArrayAdapter);
        final String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref= FirebaseDatabase.getInstance().getReference().child("Offers").child(uid);
        //String details;
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                String details=dataSnapshot.child("Date").getValue(String.class);
                details=details+" ,  "+dataSnapshot.child("Time").getValue(String.class);
                // details=details+"\n     Driver ID:"+dataSnapshot.child("driver").getValue(String.class);
                details=details+"\n\n     FROM:   "+dataSnapshot.child("source").getValue(String.class);
                details=details+"\n\n     TO:   "+dataSnapshot.child("destination").getValue(String.class);

                myArrayList.add(""+details);
                myArrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                myArrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
