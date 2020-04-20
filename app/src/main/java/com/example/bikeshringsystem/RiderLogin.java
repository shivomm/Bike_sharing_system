package com.example.bikeshringsystem;

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

public class RiderLogin extends AppCompatActivity {

    private EditText Email, pass;
    private Button signIn;
    private TextView signup1;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    ProgressDialog progressDoalog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_login);
        signup1 = (TextView) findViewById(R.id.signUp);
        signIn = (Button) findViewById((R.id.Login));
        Email = (EditText) findViewById(R.id.Loginemail);
        pass = (EditText) findViewById(R.id.Loginpass);
        mAuth = FirebaseAuth.getInstance();
        signup1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDoalog = new ProgressDialog(RiderLogin.this);
                progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDoalog.setMax(100);
                progressDoalog.setMessage("Its loading....");
                progressDoalog.setTitle("ProgressDialog bar example");
                progressDoalog.show();
                progressDoalog.dismiss();
                Intent intent = new Intent(RiderLogin.this, RiderSignUp.class);
                startActivity(intent);


                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDoalog = new ProgressDialog(RiderLogin.this);
                progressDoalog.setMax(100);
                progressDoalog.setMessage("Its loading....");
                progressDoalog.setTitle("wait Some time");
                progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDoalog.show();
                String email = Email.getText().toString().trim();
                String password = pass.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Email.setError("Email is Required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    pass.setError("pass or Required");
                    return;
                }
                if (password.length() < 6) {
                    pass.setError("password length short atleast 6 character");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(RiderLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            progressDoalog.dismiss();
                            Toast.makeText(RiderLogin.this,"Login Success full",Toast.LENGTH_LONG).show();
                            Intent intent=new Intent(RiderLogin.this,RiderMap.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            progressDoalog.dismiss();
                            Toast.makeText(RiderLogin.this,"Enter the correct Detail",Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });
    }
}


