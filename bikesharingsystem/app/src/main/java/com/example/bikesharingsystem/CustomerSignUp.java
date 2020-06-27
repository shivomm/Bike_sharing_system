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

public class CustomerSignUp extends AppCompatActivity {
    private EditText Cname,Cemail,Cpass,Crpass;
    private Button Register;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_sign_up);

        Cname=(EditText)findViewById(R.id.CustomerName);
        Cemail=(EditText)findViewById(R.id.CustomerEmail);
        Cpass=(EditText)findViewById(R.id.CustomerPass);
        Crpass=(EditText)findViewById(R.id.Customerrpass);
        Register=(Button)findViewById(R.id.CReg);
        auth=FirebaseAuth.getInstance();

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String name=Cname.getText().toString();
               String email=Cemail.getText().toString();
               String password=Cpass.getText().toString();
               String cpassword= Crpass.getText().toString();
               if(!(password.equals(cpassword)))
               {
                   Crpass.setError("Password does not match");
                   return;
               }
               else
                   {
                       Toast.makeText(CustomerSignUp.this,"Account successully Created",Toast.LENGTH_LONG).show();
                       String user_id=auth.getCurrentUser().getUid();
                       FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id).child("name").setValue(name);
                       FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id).child("email").setValue(email);
                       FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id).child("password").setValue(password);
                       startActivity(new Intent(CustomerSignUp.this,Customer.class));
                       finish();
                   }

            }
        });//Register
    }
}
