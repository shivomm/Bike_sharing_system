package com.example.bikesharingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneAuth extends AppCompatActivity {

    public static final String TAG = "TAG";
    FirebaseAuth fauth;
    EditText phoneNumber,codeEnter;
    ProgressBar progressBar;
    TextView state;
    Button next;
    CountryCodePicker codePicker;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken token;
    boolean verificationInProgress=false;
    private String phoneNum = "";
    int exist=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        next=findViewById(R.id.nextBtn);
        fauth=FirebaseAuth.getInstance();
        phoneNumber=findViewById(R.id.phone);
        codeEnter=findViewById(R.id.codeEnter);
        progressBar=findViewById(R.id.progressBar);
        state=findViewById(R.id.state);
        codePicker=findViewById(R.id.ccp);

        //if user already is signed in and is a customer,direct to home page
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            final String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.hasChild(uid)) {
                        startActivity(new Intent(getApplicationContext(),Customer.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(!verificationInProgress)
               {
                   if(!phoneNumber.getText().toString().isEmpty() && phoneNumber.getText().toString().length()==10)
                   {
                       phoneNum ="+"+codePicker.getSelectedCountryCode()+phoneNumber.getText().toString();
                       progressBar.setVisibility(View.VISIBLE);
                       state.setText("Sending OTP ..");
                       state.setVisibility(View.VISIBLE);
                       requestOTP(phoneNum);
                       next.setEnabled(false);

                   }
                   else
                   {
                       phoneNumber.setError("Phone number is not valid");
                   }
               }
               else
               {
                   String userOTP=codeEnter.getText().toString();
                   if(!userOTP.isEmpty() && userOTP.length()==6){
                       PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,userOTP);
                       verifyAuth(credential);

                   }
                   else
                   {
                       codeEnter.setError("Valid OTP is required");
                   }
               }
            }
        });




    }

    private void verifyAuth(PhoneAuthCredential credential) {
        fauth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Log.d("TAG",""+phoneNum);
                    checkforPhoneNumber(phoneNum);
                }
                else
                {
                    Toast.makeText(PhoneAuth.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkforPhoneNumber(final String phone) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Phone");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(phone))  //old phone number exits,directs to register page
                {
                    startActivity(new Intent(PhoneAuth.this, CustomerLogin.class));
                    finish();
                }
                else {
                    //new phone number found,direct to register page
                    String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference("Phone").
                            child(phoneNum).setValue(phoneNum);
                    FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(uid).child("phoneno").setValue(phoneNum);
                    Toast.makeText(PhoneAuth.this, "Authentication is successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PhoneAuth.this, CustomerSignUp.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void requestOTP(String phoneNum)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNum, 60L, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                progressBar.setVisibility(View.GONE);
                state.setVisibility(View.GONE);
                codeEnter.setVisibility(View.VISIBLE);
                verificationId=s;
                token=forceResendingToken;
                next.setText("Verify");
                next.setEnabled(true);
                verificationInProgress=true;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(PhoneAuth.this, "Cannot create account" +e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
