package com.example.daniel.sifonride.nav.setting;

import android.app.ProgressDialog;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.daniel.sifonride.R;
import com.example.daniel.sifonride.common.Common;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;

import static com.example.daniel.sifonride.common.Common.countryCode;

public class PhoneNumSettings extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    EditText number;
    TextInputLayout numberText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_num_settings);


        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update phone number");

        mFirestore = FirebaseFirestore.getInstance();

        number = findViewById(R.id.number);
        numberText = findViewById(R.id.number_text);

        final CountryCodePicker codePicker = findViewById(R.id.codePickerS);
        codePicker.registerCarrierNumberEditText(number);
        codePicker.setDefaultCountryUsingNameCode(countryCode);
        codePicker.setHintExampleNumberEnabled(true);

        Button save = findViewById(R.id.phoneNumSave);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Loading bar
                final ProgressDialog builder = new ProgressDialog(PhoneNumSettings.this);
                builder.setMessage("Uploading.....");
                builder.show();

                if (TextUtils.isEmpty(number.getText().toString())){
                    numberText.setError("Required");
                }else {
                    Map<String, Object> infoChange = new HashMap<>();
                    infoChange.put("Phone number", codePicker.getFullNumberWithPlus());
                    //noinspection ConstantConditions
                    mFirestore.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .collection(Common.riderDetails).document(Common.rider_info).update(infoChange)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    finish();
                                    builder.dismiss();
                                }
                            });
                }
            }
        });
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

    //TODO: verify phone number.
}