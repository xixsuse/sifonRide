package com.example.daniel.sifonride.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.sifonride.HomeActivity;
import com.example.daniel.sifonride.MainActivity;
import com.example.daniel.sifonride.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.example.daniel.sifonride.common.Common.countryCode;
import static com.example.daniel.sifonride.common.Common.riderDetails;
import static com.example.daniel.sifonride.common.Common.rider_info;
import static com.example.daniel.sifonride.common.Common.riders;

public class SignUp extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG ="Register" ;
    private Button register_btn;
    private TextView log_in, privacyText;
    private EditText register_email, register_pass, name, other, phone;
    private TextInputLayout register_email_T, register_pass_T, name_T, other_T, phone_T;
    private CheckBox checkBox;
    private CountryCodePicker codePicker;

    private FirebaseAuth mAuth;
    private ProgressDialog waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.sign_up);

        checkCountryCode();

        register_btn = findViewById(R.id.register_btn2);
        register_email = findViewById(R.id.register_email);
        register_pass = findViewById(R.id.register_pass);
        name = findViewById(R.id.register_name);
        other = findViewById(R.id.register_other);
        phone = findViewById(R.id.register_phone);

        register_email_T = findViewById(R.id.register_email_T);
        register_pass_T = findViewById(R.id.register_pass_T);
        name_T = findViewById(R.id.register_name_T);
        other_T = findViewById(R.id.register_other_T);
        phone_T = findViewById(R.id.register_phone_T);

        checkBox = findViewById(R.id.checkPrivacy);
        log_in = findViewById(R.id.log_in);
        privacyText = findViewById(R.id.privacyText);

        codePicker = findViewById(R.id.codePicker);
        codePicker.registerCarrierNumberEditText(phone);
        codePicker.setDefaultCountryUsingNameCode(countryCode);
        codePicker.setHintExampleNumberEnabled(true);


        register_btn.setOnClickListener(this);

        textEditors();
        textWatcherMethod();
    }

    @Override
    public void onClick(View view) {
        if (view == register_btn){
            register();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SignUp.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                startActivity(new Intent(SignUp.this, MainActivity.class));
                SignUp.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkCountryCode(){
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        assert manager != null;
        countryCode = manager.getNetworkCountryIso();
    }

    private void textEditors(){

        String text = "I agree to the outline terms and condition";
        String lgText = "Already have an account? SIGN IN ";
        SpannableString pvs = new SpannableString(text);
        SpannableString lgs = new SpannableString(lgText);


        ForegroundColorSpan pv = new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark));
        ClickableSpan pvSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUp.this, LoginActivity.class));
            }
        };
        pvs.setSpan(pvSpan, 23, 42, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        pvs.setSpan(pv, 23, 42, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        privacyText.setText(pvs);
        privacyText.setMovementMethod(LinkMovementMethod.getInstance());

        ForegroundColorSpan lg = new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark));
        ClickableSpan lgSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUp.this, LoginActivity.class));
            }
        };

        lgs.setSpan(lgSpan, 24, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lgs.setSpan(lg, 24, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        log_in.setText(lgs);
        log_in.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void textWatcherMethod(){

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0){
                    if (name_T.isErrorEnabled()){
                        name_T.setErrorEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        other.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0){
                    if (other_T.isErrorEnabled()){
                        other_T.setErrorEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0){
                    if (phone_T.isErrorEnabled()){
                        phone_T.setErrorEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        register_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0){
                    if (register_email_T.isErrorEnabled()){
                        register_email_T.setErrorEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        register_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0){
                    if (register_pass_T.isErrorEnabled())

                        register_pass_T.setErrorEnabled(false);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateName() {
        if (name.getText().toString().trim().isEmpty()) {
            name_T.setErrorEnabled(true);
            name_T.setError(getString(R.string.err_msg_name));
            requestFocus(name);
            return false;
        } else {
            name_T.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateOtherName(){
        if (other.getText().toString().trim().isEmpty()) {
            other_T.setErrorEnabled(true);
            other_T.setError(getString(R.string.err_msg_other_name));
            requestFocus(name);
            return false;
        } else {
            other_T.setErrorEnabled(false);
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validateEmail() {
        String email = register_email.getText().toString().trim();

        if (email.isEmpty()) {
            register_email_T.setError(getString(R.string.err_msg_email));
            requestFocus(register_email);
            return false;
        } else if(!isValidEmail(email)){
            register_email_T.setError(getString(R.string.err_msg_email_2));
            requestFocus(register_email);
            return false;
        }
        else {
            register_email_T.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePhoneNumber() {
        if (phone.getText().toString().trim().isEmpty()) {
            phone_T.setErrorEnabled(true);
            phone_T.setError(getString(R.string.err_msg_phone));
            requestFocus(name);
            return false;
        }else if (phone.getText().toString().length() < 7) {
            phone_T.setErrorEnabled(true);
            phone_T.setError(getString(R.string.err_msg_phone));
            requestFocus(name);
            //TODO: Verify phone number using code picker
        } else{

            phone_T.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePassword() {
        if (register_pass.getText().toString().trim().isEmpty()) {
            register_pass_T.setError(getString(R.string.err_msg_password));
            requestFocus(register_pass);
            return false;
        } else if (register_pass.getText().toString().length() < 6) {
            register_pass_T.setError(getString(R.string.err_msg_password_2));
            requestFocus(register_pass);
            return false;
        }else{
            register_pass_T.setErrorEnabled(false);
        }

        return true;
    }

    private void closedKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void register() {
        if (validateName() && validateOtherName() && validateEmail()
                &&validatePhoneNumber() && validatePassword()) {
            if (checkBox.isChecked()){

                closedKeyBoard();

                waiting = new ProgressDialog(this);
                waiting.setMessage("Signing In");
                waiting.show();
                waiting.setCancelable(false);

                mAuth.createUserWithEmailAndPassword(register_email.getText().toString(),
                        register_pass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        addUserDetails();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waiting.dismiss();
                    }
                });
            }else {
                Toast.makeText(this, "Click the check box to continue", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void addUserDetails(){

        //noinspection ConstantConditions
        String userId = mAuth.getCurrentUser().getUid();


        Map<String, Object> users = new HashMap<>();
        users.put("Name", name.getText().toString());
        users.put("Other name", other.getText().toString());
        users.put("Phone number", codePicker.getFullNumberWithPlus());
        users.put("Email", register_email.getText().toString());
        users.put("Password", register_pass.getText().toString());

        FirebaseFirestore fb = FirebaseFirestore.getInstance();
        fb.collection(riders)
                .document(userId).collection(riderDetails).document(rider_info)
                .set(users).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(SignUp.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waiting.dismiss();
                Log.w(TAG, "Error writing document", e);
            }
        });
    }

}