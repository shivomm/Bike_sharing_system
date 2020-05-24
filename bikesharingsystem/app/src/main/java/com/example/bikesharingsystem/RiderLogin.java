package com.example.bikesharingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RiderLogin extends AppCompatActivity {
    private EditText pass;
    private Button signIn;
    private FirebaseAuth mAuth;

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_login);
        signIn = (Button) findViewById(R.id.RLogin);
        pass = (EditText) findViewById(R.id.password);
        mAuth=FirebaseAuth.getInstance();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(RiderLogin.this);
                progressDialog.setMax(100);
                progressDialog.setTitle("Signing in ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                final String password = pass.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    pass.setError("Password Required !");
                    return;
                }
                if (password.length() < 8) {
                    pass.setError("password length short! Need atleast 8 character");
                    return;
                }

                String uid=mAuth.getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("Users").child("Riders").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String p=dataSnapshot.child("password").getValue(String.class);
                        if(p.equals(password))
                        {
                            startActivity(new Intent(RiderLogin.this,Rider.class));
                            finish();
                        }
                        else
                        {
                            Toast.makeText(RiderLogin.this, "Password invalid !", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });//Login Button

    }

}
