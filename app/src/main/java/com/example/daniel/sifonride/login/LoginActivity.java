package com.example.daniel.sifonride.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.sifonride.HomeActivity;
import com.example.daniel.sifonride.MainActivity;
import com.example.daniel.sifonride.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "Sign_In";
    private Button sign_in_btn;
    private EditText email, pass;
    private TextInputLayout emailT, passT;
    private TextView sign_up_click , new_pass;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.log_in);

        sign_in_btn = findViewById(R.id.sign_in_btn2);
        email = findViewById(R.id.sign_in_email);
        pass = findViewById(R.id.sign_in_pass);
        emailT = findViewById(R.id.sign_in_email_T);
        passT = findViewById(R.id.sign_in_pass_T);

        sign_up_click = findViewById(R.id.sign_up_click);
        new_pass = findViewById(R.id.new_password);

        sign_in_btn.setOnClickListener(this);
        sign_up_click.setOnClickListener(this);
        new_pass.setOnClickListener(this);

        textEditors();
        textWatcherMethod();

    }

    @Override
    public void onClick(View view) {
        if (view == sign_in_btn){
            sign_in();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LoginActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                startActivity(new Intent(LoginActivity.this, MainActivity.class));

                LoginActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void textEditors() {

        String text = "Forgot password? GET NEW";
        String suText = "Don't have an account? SIGN UP ";
        SpannableString fps = new SpannableString(text);
        SpannableString sus = new SpannableString(suText);


        ForegroundColorSpan pv = new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary));
        ClickableSpan pvSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUp.class));
            }
        };
        fps.setSpan(pvSpan, 17, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        fps.setSpan(pv, 17, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        new_pass.setText(fps);
        new_pass.setMovementMethod(LinkMovementMethod.getInstance());

        ForegroundColorSpan lg = new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary));
        ClickableSpan lgSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUp.class));
            }
        };

        sus.setSpan(lgSpan, 23, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sus.setSpan(lg, 23, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sign_up_click.setText(sus);
        sign_up_click.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void textWatcherMethod(){

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0){
                    if (emailT.isErrorEnabled()){
                        emailT.setErrorEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0){
                    if (passT.isErrorEnabled())

                        passT.setErrorEnabled(false);

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

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validateEmail() {
        String mail = email.getText().toString().trim();

        if (mail.isEmpty()) {
            emailT.setError(getString(R.string.err_msg_email));
            requestFocus(email);
            return false;
        } else if(!isValidEmail(mail)){
            emailT.setError(getString(R.string.err_msg_email_2));
            requestFocus(email);
            return false;
        }
        else {
            emailT.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        if (pass.getText().toString().trim().isEmpty()) {
            passT.setError(getString(R.string.err_msg_password));
            requestFocus(pass);
            return false;
        } else if (pass.getText().toString().length() < 6) {
            passT.setError(getString(R.string.err_msg_password_2));
            requestFocus(pass);
            return false;
        }else{
            passT.setErrorEnabled(false);
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

    private void sign_in() {
        if (validateEmail() && validatePassword()){
            closedKeyBoard();

            final ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage("Signing In");
            progress.show();
            progress.setCancelable(false);

            mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));


                            } else {
                                progress.dismiss();
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                //noinspection ConstantConditions
                                Toast.makeText(LoginActivity.this, "Authentication failed."
                                                +task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }
    }

}
