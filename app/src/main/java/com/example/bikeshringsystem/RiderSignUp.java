package com.example.bikeshringsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RiderSignUp extends AppCompatActivity {


    private EditText RFullName,REmail,Rpass,Rrpass,Rphone;
    private Button registration;
    private TextView lgn;
    private FirebaseAuth fauth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ProgressDialog progressDoalog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_sign_up);
        RFullName=(EditText)findViewById(R.id.RName);
        REmail=(EditText)findViewById(R.id.REmail);
        Rpass=(EditText)findViewById(R.id.RPass);
        Rrpass=(EditText)findViewById(R.id.RRrpass);
        Rphone=(EditText)findViewById(R.id.RPhoneNumber);
        registration=(Button)findViewById(R.id.REg);
              fauth=FirebaseAuth.getInstance();
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=REmail.getText().toString().trim();
                String password=Rpass.getText().toString().trim();
                String Repeat_password=Rrpass.getText().toString().trim();
                progressDoalog = new ProgressDialog(RiderSignUp.this);
                progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDoalog.setMax(100);
                progressDoalog.setMessage("Its loading....");
                progressDoalog.setTitle("Please wait some time");
                progressDoalog.show();
                if(!Repeat_password.equals(password))
                {
                    progressDoalog.dismiss();
                    Rrpass.setError("Not the confirem password !");
                    return;

                }
                if(TextUtils.isEmpty(email)) {
                    progressDoalog.dismiss();
                    REmail.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty(password))
                {progressDoalog.dismiss();
                    Rpass.setError("pass or Required");
                    return;
                }
                if(password.length()<6)
                {
                    progressDoalog.dismiss();
                    Rpass.setError("password length short atleast 6 character");
                    return;
                }
                fauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            progressDoalog.dismiss();
                            Toast.makeText(RiderSignUp.this,"User Created",Toast.LENGTH_LONG).show();
                            String user_id=fauth.getCurrentUser().getUid();
                            DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                            current_user_db.setValue(true);
                            startActivity(new Intent(getApplicationContext(),RiderLogin.class));
                        }
                        else
                        {
                            progressDoalog.dismiss();
                            Toast.makeText(RiderSignUp.this,"Error",Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }
        });


    }
}
