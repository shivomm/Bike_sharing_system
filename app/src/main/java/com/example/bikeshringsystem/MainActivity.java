package com.example.bikeshringsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {



    private Button mdriver,mcustomer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mdriver=(Button)findViewById(R.id.driver);
        mcustomer=(Button)findViewById(R.id.customer);
        mdriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,RiderLogin.class);
                startActivity(intent);
                finish();
            }
        });
        mcustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,CustomerLogin.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
