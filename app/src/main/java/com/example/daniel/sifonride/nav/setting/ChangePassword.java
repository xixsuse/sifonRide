package com.example.daniel.sifonride.nav.setting;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.daniel.sifonride.R;
import com.example.daniel.sifonride.common.Common;
import com.example.daniel.sifonride.model.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class ChangePassword extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ChangePassword" ;
    private TextInputLayout oldPassA, newPassA;
    private EditText oldPass, newPass;
    private Button nextToNew, changePassBtn;
    private LinearLayout oldPassLayout, newPassLayout;
    private FirebaseFirestore mFirestore;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Password ");

        mFirestore = FirebaseFirestore.getInstance();

        oldPass =  findViewById(R.id.old_pass);
        newPass = findViewById(R.id.newPassword);
        oldPassA = findViewById(R.id.old_pasS);
        nextToNew = findViewById(R.id.nextToPass);
        changePassBtn = findViewById(R.id.changePass);
        newPassA = findViewById(R.id.newPasswordA);
        oldPassLayout = findViewById(R.id.oldPassLayout);
        newPassLayout = findViewById(R.id.newPassLayout);


        nextToNew.setOnClickListener(this);
        changePassBtn.setOnClickListener(this);


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
    public void onClick(View v) {
        if (v == nextToNew){
            verifyPassword();
        }if (v == changePassBtn){
            changePassword();
        }
    }

    private void verifyPassword(){
        if (Util.Operations.isOnline(this)) {
            final ProgressDialog waiting = new ProgressDialog(ChangePassword.this);
            waiting.setMessage("Loading...");
            waiting.setCancelable(false);
            waiting.show();
            //noinspection ConstantConditions
            mFirestore.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(Common.riderDetails).document(Common.rider_info).addSnapshotListener(ChangePassword.this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (documentSnapshot.exists()) {
                        pass = documentSnapshot.getString("Password");
                        if (!oldPass.getText().toString().isEmpty()) {
                            if (pass.equals(oldPass.getText().toString())) {
                                waiting.dismiss();
                                changePassword();
                            }else {
                                waiting.dismiss();
                                oldPassA.setError("Please your password is incorrect");
                            }
                        } else {
                            waiting.dismiss();
                            oldPassA.setError("Empty password");
                        }
                    } else {
                        waiting.dismiss();

                        oldPassA.setError("Please enter your current password");
                    }

                }

            });

        }else {
            Toast.makeText(getApplicationContext(), "You are offline", Toast.LENGTH_LONG).show();
        }
    }

    private void changePassword() {
        oldPassLayout.setVisibility(View.GONE);
        newPassLayout.setVisibility(View.VISIBLE);
        final ProgressDialog waiting = new ProgressDialog(ChangePassword.this);
        waiting.setMessage("Loading...");
        waiting.setCancelable(false);
        if (Util.Operations.isOnline(this)) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String newPassword = newPass.getText().toString();
            waiting.show();

            if (verifyNewPassword()) {
                assert user != null;
                user.updatePassword(newPassword)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("Password", newPassword);

                                    //noinspection ConstantConditions
                                    mFirestore.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection(Common.riderDetails).document(Common.rider_info).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            newPassLayout.setVisibility(View.GONE);

                                           finish();
                                        }
                                    });
                                    Log.d(TAG, "User password updated.");
                                }
                            }
                        });
            } else {
                waiting.dismiss();
                verifyNewPassword();
            }

        }else {
            waiting.dismiss();
            Toast.makeText(getApplicationContext(), "You are offline", Toast.LENGTH_LONG).show();
        }

    }

    private boolean verifyNewPassword(){
        if (newPass.getText().toString().isEmpty()){
            newPassA.setError("Please enter a password");
            return false;
        }else if (newPass.getText().toString().length() < 6){
            newPassA.setError("Password too short");
            return false;
        }else {
            return true;
        }
    }

}
