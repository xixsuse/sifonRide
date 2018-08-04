package com.example.daniel.sifonride.nav.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.daniel.sifonride.R;
import com.example.daniel.sifonride.common.Common;
import com.example.daniel.sifonride.model.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    private static final int PROFILE_GALLERY = 2;
    private static final int PROFILE_CAMERA_REQUEST = 1;
    private static final String TAG = "Profile activity";

    FirebaseFirestore mFs = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings ;
    private String mProfile;

    private ImageView mProfileImage;
    private Button uploadBtn;

    Uri resultUri;
    Bundle extras;
    private String resultUriInString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile upload");

        mProfileImage = findViewById(R.id.profileSetting);
        uploadBtn = findViewById(R.id.profileBtn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileUpload();
            }
        });
        settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        viewProfile();
    }

    private void viewProfile(){

        //noinspection ConstantConditions
        mFs.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(Common.riderDetails).document(Common.rider_info)
                .addSnapshotListener(ProfileActivity.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null){
                            Log.e(TAG, "Error"+e.getMessage());
                        }
                        String source = documentSnapshot.getMetadata().isFromCache() ?
                                "local cache" : "server";
                        Log.d(TAG, "Data fetched from " + source);

                        if (documentSnapshot.exists()){
                            mProfile = documentSnapshot.getString("Profile Image");
                            if (mProfile != null){
                                uploadBtn.setText(R.string.update_profile);
                                Glide.with(ProfileActivity.this).load(mProfile).into(mProfileImage);
                            }
                        }
                    }
                });
        mFs.setFirestoreSettings(settings);
    }

    private void profileUpload() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.image_upload, null);

        final AlertDialog dialog = mBuilder.create();
        dialog.setView(view);
        dialog.show();

        TextView title = view.findViewById(R.id.imageDialogText);
        TextView camera = view.findViewById(R.id.camera);
        TextView gallery = view.findViewById(R.id.gallery);
        Button cancel = view.findViewById(R.id.imageAlertBtn);

        title.setText(R.string.update_profile);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, PROFILE_CAMERA_REQUEST);
                }

            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), PROFILE_GALLERY);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private void saveProfileFromCamera(){
        final ProgressDialog dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setMessage("Loading .....");
        if (Util.Operations.isOnline(ProfileActivity.this)) {
            if (extras != null) {
                dialog.show();

                //noinspection ConstantConditions
                StorageReference filePath = FirebaseStorage.getInstance().getReference(Common.riders)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Profile Image");
                //                Bitmap bitmap = null;
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                Bitmap bitmap = (Bitmap) extras.get("data");


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filePath.putBytes(data);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //noinspection ConstantConditions
                        resultUriInString = taskSnapshot.getUploadSessionUri().toString();
                        Map<String, Object> map = new HashMap<>();
                        map.put("Profile Image", resultUriInString);
                        FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
                        //noinspection ConstantConditions
                        mFireStore.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection(Common.riderDetails).document(Common.rider_info)
                                .update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Glide.with(ProfileActivity.this).load(resultUriInString).into(mProfileImage);
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Upload Complete", Toast.LENGTH_SHORT).show();
                                finish();

                            }
                        });

                    }
                });
            }
        }else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileFromGallery(){
        final ProgressDialog dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setMessage("Loading .....");
        if (Util.Operations.isOnline(ProfileActivity.this)) {
            if (resultUri != null) {
                dialog.show();

                //noinspection ConstantConditions
                StorageReference filePath = FirebaseStorage.getInstance().getReference(Common.riders)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Profile Image");
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filePath.putBytes(data);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //noinspection ConstantConditions
                        resultUriInString = taskSnapshot.getUploadSessionUri().toString();
                        Map<String, Object> map = new HashMap<>();
                        map.put("Profile Image", resultUriInString);
                        FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();
                        //noinspection ConstantConditions
                        mFireStore.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection(Common.riderDetails).document(Common.rider_info)
                                .update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Glide.with(ProfileActivity.this).load(resultUriInString).into(mProfileImage);
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Upload Complete", Toast.LENGTH_SHORT).show();
                                finish();

                            }
                        });

                    }
                });
            }
        }else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dat) {
        super.onActivityResult(requestCode, resultCode, dat);

        switch(requestCode) {
            case PROFILE_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    resultUri = dat.getData();
                    //noinspection ConstantConditions
                    saveProfileFromGallery();
                }
                break;
            case PROFILE_CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK){
                    // resultUri = dat.getData();
                    //noinspection ConstantConditions
                    extras = dat.getExtras();
                    //Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //mImageView.setImageBitmap(imageBitmap);
                    saveProfileFromCamera();
                }
                break;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }



}
