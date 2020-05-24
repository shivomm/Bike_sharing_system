package com.example.bikesharingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bikesharingsystem.Model.Userhelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RiderSignUp extends AppCompatActivity {
    private EditText Rname,Remail,Rpass,Rrpass;
    private Button Register;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_sign_up);

        Rname=(EditText)findViewById(R.id.Rname);
        Remail=(EditText)findViewById(R.id.REmail);
        Rpass=(EditText)findViewById(R.id.RPass);
        Rrpass=(EditText)findViewById(R.id.RRrpass);
        Register=(Button)findViewById(R.id.RiderRegister);
        auth=FirebaseAuth.getInstance();

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=Rname.getText().toString();
                String email=Remail.getText().toString();
                String password=Rpass.getText().toString();
                String cpassword= Rrpass.getText().toString();
                if(!(password.equals(cpassword)))
                {
                    Rrpass.setError("Password does not match");
                    return;
                }
                else
                {
                    Toast.makeText(RiderSignUp.this,"Account successully Created",Toast.LENGTH_LONG).show();
                    String user_id=auth.getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(user_id).child("name").setValue(name);
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(user_id).child("email").setValue(email);
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(user_id).child("password").setValue(password);
                    startActivity(new Intent(RiderSignUp.this,Rider.class));
                    finish();
                }

            }
        });//Register
    }
}
