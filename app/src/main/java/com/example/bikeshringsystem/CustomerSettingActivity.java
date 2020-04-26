package com.example.bikeshringsystem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomerSettingActivity extends AppCompatActivity {

    private EditText mName,mphone;
    private Button mback,mconfirm;
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDataBase;
    private String userId;
    private String mName1,mPhone1;
    private ImageView mprofileImage;
    private  Uri resultUri;
    private String profileImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_setting);
        mName=(EditText)findViewById(R.id.name);
        mphone=(EditText)findViewById(R.id.phone);
        mconfirm=(Button)findViewById(R.id.confirm);
        mback=(Button)findViewById(R.id.back);
        mprofileImage=(ImageView)findViewById(R.id.profileImage);
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        mCustomerDataBase= FirebaseDatabase.getInstance().getReference("Users").child("Customers").child(userId);
        getUserInfo();
        mprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
               // startActivityForResult(intent,1);
                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), 1);

            }
        });
        mconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SaveUserInformation();
            }
        });
        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

    }
private void getUserInfo()
{
    mCustomerDataBase.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
            {
                Map<String,Object>map=(Map<String,Object>)dataSnapshot.getValue();
                if(map.get("Name")!=null)
                {
                    mName1=map.get("Name").toString();
                    mName.setText(mName1);
                }
                if(map.get("Phone")!=null)
                {
                    mPhone1=map.get("Phone").toString();
                    mphone.setText(mPhone1);
                }
                if(map.get("profileImageUrl")!=null)
                {
                  profileImageUrl=map.get("profileImageUrl").toString();

                     Glide.with(getApplication()).load(profileImageUrl).into(mprofileImage);

                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
}
    private void SaveUserInformation() {
        mName1=mName.getText().toString();
        mPhone1=mphone.getText().toString();
        Map userInfo=new HashMap();
        userInfo.put("Name",mName1);
        userInfo.put("Phone",mPhone1);
        mCustomerDataBase.updateChildren(userInfo);
        if(resultUri!=null)
        {
            StorageReference filePath= FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
            Bitmap bitmap=null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
           ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
            byte[] data=baos.toByteArray();
            UploadTask uploadTask=filePath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri=taskSnapshot.getUploadSessionUri();
                        Map newImage=new HashMap();
                        newImage.put("profileImageUrl",downloadUri.toString());
                        mCustomerDataBase.updateChildren(newImage);
                        finish();
                        return;
                }
            });

        }
        else {
            finish();
            return;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== Activity.RESULT_OK)
        {
            final Uri imageUri=data.getData();
            resultUri=imageUri;
            mprofileImage.setImageURI(resultUri);

        }
    }
}
