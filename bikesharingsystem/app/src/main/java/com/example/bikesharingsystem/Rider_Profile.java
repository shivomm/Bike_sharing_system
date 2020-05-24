package com.example.bikesharingsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class Rider_Profile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDataBase;
    private EditText mName,mphone;
    private String userId;
    private String mName1,mPhone1;
    private ImageView mprofileImage;
    private Uri resultUri;
    private String profileImageUrl;
    int TAKE_IMAGE_CODE=10001;
    private Button mlogout,mrecentRide,edit_profile_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider__profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        edit_profile_btn=findViewById(R.id.Edit_profile);
        mrecentRide=findViewById(R.id.recent_rides);

        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),CustomerSetting.class));
            }
        });

        mrecentRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),R_Recent_rides.class));
            }
        });

        mName=(EditText)findViewById(R.id.name);
        mphone=(EditText)findViewById(R.id.phone);
        mprofileImage=(ImageView)findViewById(R.id.profileImage);
        mlogout=findViewById(R.id.Logout);

        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        mCustomerDataBase=FirebaseDatabase.getInstance().getReference("Users").child("Riders").child(userId);
         getCustomerInfo();
        mprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), TAKE_IMAGE_CODE);
            }
        });


        //Logout button
        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Rider_Profile.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_IMAGE_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            resultUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                mprofileImage.setImageBitmap(bitmap);
                handleUpload(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }



    private void handleUpload(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference reference= FirebaseStorage.getInstance().getReference().child("profileImages").child(uid+".jpeg");
        reference.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getDownloadUrl(reference);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG","OnFailure",e.getCause());
                    }
                });

    }

    private void getDownloadUrl(StorageReference reference)
    {
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("TAG","OnSuccess"+uri);
                setUserProfileImage(uri);
            }
        });
    }
    private void setUserProfileImage(Uri uri)
    {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri).build();

        user.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Rider_Profile.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Rider_Profile.this, "Profile image failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getCustomerInfo()
    {
        mCustomerDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    mName1=dataSnapshot.child("name").getValue().toString();
                    mName.setText(mName1);
                    mPhone1=dataSnapshot.child("phoneno").getValue().toString();
                    mphone.setText(mPhone1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }//getCustomerInfo


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

}
