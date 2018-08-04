package com.example.daniel.sifonride.nav;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.daniel.sifonride.MainActivity;
import com.example.daniel.sifonride.R;
import com.example.daniel.sifonride.autocomplete.AutocompleteHome;
import com.example.daniel.sifonride.autocomplete.AutocompleteWork;
import com.example.daniel.sifonride.common.Common;
import com.example.daniel.sifonride.nav.setting.LanguageSettings;
import com.example.daniel.sifonride.nav.setting.PhoneNumSettings;
import com.example.daniel.sifonride.nav.setting.Privacy_setting;
import com.example.daniel.sifonride.nav.setting.ProfileActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "Settings";
    private CircleImageView mProfileImage;
    private TextView name_setting, emailAddress, phoneNumber, mHomeAddress, mHome,mWork, mWorkAddress,
            privacySetting, language, sign_out;
    private ImageView mHomeImage, mWorkImage;
    private String mName, mPhone,mOther, mEmail, mProfile;

    FirebaseFirestoreSettings settings;
    FirebaseFirestore mFs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        mProfileImage = findViewById(R.id.profileImage);
        name_setting = findViewById(R.id.name_setting);
        emailAddress = findViewById(R.id.email_setting);
        phoneNumber = findViewById(R.id.phone_setting);
        mHomeImage = findViewById(R.id.homeImageS);
        mHomeAddress = findViewById(R.id.home_addressS);
        mHome = findViewById(R.id.add_home_addressS);
        mWorkImage = findViewById(R.id.workImageS);
        mWorkAddress = findViewById(R.id.workSS);
        mWork = findViewById(R.id.workS);
        privacySetting = findViewById(R.id.privacy_settings);
        language = findViewById(R.id.language);
        sign_out = findViewById(R.id.signOut);

        mProfileImage.setOnClickListener(this);
        emailAddress.setOnClickListener(this);
        phoneNumber.setOnClickListener(this);
        mHomeImage.setOnClickListener(this);
        mHomeAddress.setOnClickListener(this);
        mHome.setOnClickListener(this);
        mWorkImage.setOnClickListener(this);
        mWorkAddress.setOnClickListener(this);
        mWork.setOnClickListener(this);
        privacySetting.setOnClickListener(this);
        language.setOnClickListener(this);

        sign_out.setOnClickListener(this);

        settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        //for getting user details
        mFs = FirebaseFirestore.getInstance();
        getUserInfo();
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

    @Override
    public void onClick(View view) {
        if (view == mProfileImage){
            startActivity(new Intent(Settings.this, ProfileActivity.class));
        }else if (view ==emailAddress){
            Toast.makeText(this, "Haha", Toast.LENGTH_SHORT).show();
        }else if (view ==phoneNumber){
            startActivity(new Intent(Settings.this, PhoneNumSettings.class));
        }else if (view ==mHomeImage){
            gotoHome();
        }else if (view ==mHomeAddress){
            gotoHome();
        }else if (view ==mHome){
            gotoHome();
        }else if (view ==mWorkImage){
            gotoWork();
        }else if (view ==mWorkAddress){
            gotoWork();
        }else if (view ==mWork){
            gotoWork();
        }else if (view ==privacySetting){
            startActivity(new Intent(Settings.this, Privacy_setting.class));
        }else if (view ==language){
            startActivity(new Intent(Settings.this, LanguageSettings.class));
        }else if (view == sign_out){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Settings.this, MainActivity.class));
        }
    }

    private void gotoHome(){
        startActivity(new Intent(Settings.this, AutocompleteHome.class));
    }

    private void gotoWork(){
        startActivity(new Intent(Settings.this, AutocompleteWork.class));
    }

    private void getUserInfo(){

        //noinspection ConstantConditions
        mFs.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(Common.riderDetails).document(Common.rider_info)
                .addSnapshotListener(Settings.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null){
                            Log.e(TAG, "Error"+e.getMessage());
                        }
                        String source = documentSnapshot.getMetadata().isFromCache() ?
                                "local cache" : "server";
                        Log.d(TAG, "Data fetched from " + source);

                        if (documentSnapshot.exists()){
                            mName  = documentSnapshot.getString("Name");
                            mOther = documentSnapshot.getString("Other name");
                            mEmail = documentSnapshot.getString("Email");
                            mPhone= documentSnapshot.getString("Phone number");
                            mProfile = documentSnapshot.getString("Profile Image");
                            Common.mHomeAddress = documentSnapshot.getString("Home Address");

                            if (documentSnapshot.getString("Home Lat") != null && documentSnapshot.getString("Home Lng") != null)
                                Common.mHomeLatLng = new LatLng(Double.parseDouble(documentSnapshot.getString("Home Lat")),
                                        Double.parseDouble(documentSnapshot.getString("Home Lng")));
                            if (documentSnapshot.getString("Work Address") != null)
                                Common.mWorkAddress = documentSnapshot.getString("Work Address");
                            if (documentSnapshot.getString("Work Lat") != null && documentSnapshot.getString("Work Lng") != null)
                                Common.mWorkLatLng = new LatLng(Double.parseDouble(documentSnapshot.getString("Work Lat")),
                                        Double.parseDouble(documentSnapshot.getString("Work Lng")));

                            String names = mName + " " + mOther;

                            name_setting.setText(names);
                            emailAddress.setText(mEmail);
                            phoneNumber.setText(mPhone);

                            if (mProfile != null)
                                Glide.with(getApplicationContext()).load(mProfile).into(mProfileImage);
                            if (Common.mHomeAddress != null){
                                mHomeAddress.setVisibility(View.VISIBLE);
                                mHomeAddress.setText(Common.mHomeAddress);
                                mHome.setText(R.string.home);
                            }else {
                                mHomeAddress.setVisibility(View.GONE);
                                mHome.setText(R.string.add_home);
                            }
                            if (Common.mWorkAddress != null){
                                mWorkAddress.setVisibility(View.VISIBLE);
                                mWorkAddress.setText(Common.mWorkAddress);
                                mWork.setText(R.string.work);
                            }else {
                                mWorkAddress.setVisibility(View.GONE);
                                mWork.setText(R.string.add_work);
                            }
                        }
                    }
                });
        mFs.setFirestoreSettings(settings);
    }

}