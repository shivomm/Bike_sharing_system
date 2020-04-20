package com.example.bikeshringsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

public class CustomerSignUp extends AppCompatActivity {

    private EditText Cname,Cemail,Cpass,Crpass,Cphone;
    private Button Register;

   private TextView login;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_sign_up);

        Cname=(EditText)findViewById(R.id.CustomerName);
        Cemail=(EditText)findViewById(R.id.CustomerEmail);
        Cpass=(EditText)findViewById(R.id.CustomerPass);
        Cpass=(EditText)findViewById(R.id.Customerrpass);
        Cphone=(EditText)findViewById(R.id.CustomerPhoneNumber);
        Register=(Button)findViewById(R.id.CReg);
        auth=FirebaseAuth.getInstance();
      Register.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
             String Email=Cemail.getText().toString();
             String password=Cpass.getText().toString();
             auth.createUserWithEmailAndPassword(Email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                     if(task.isSuccessful())
                     {
                         Toast.makeText(CustomerSignUp.this,"User Created",Toast.LENGTH_LONG).show();
                         String user_id=auth.getCurrentUser().getUid();
                         DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                         current_user_db.setValue(true);
                         startActivity(new Intent(getApplicationContext(),CustomerLogin.class));
                     }
                     else
                     {
                         Toast.makeText(CustomerSignUp.this,"User Creation Failed",Toast.LENGTH_LONG).show();
                     }

                 }
             });
          }
      });
    }
}