package com.example.daniel.sifonride;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;

import com.example.daniel.sifonride.login.LoginActivity;
import com.example.daniel.sifonride.login.SignUp;
import com.example.daniel.sifonride.login.SlidingImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Integer [] images = {R.color.ghh, R.color.colorAccent, R.color.colorPrimaryDark};
    private ArrayList<Integer> imagesArray = new ArrayList<>();
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Button sign_in_btn = findViewById(R.id.sign_in_btn);
        Button register_btn = findViewById(R.id.register_btn);

        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignUp.class));
            }
        });

        init();

    }

    private void init() {
        imagesArray.addAll(Arrays.asList(images));

        final ViewPager mPager = findViewById(R.id.pager);


        mPager.setAdapter(new SlidingImage(MainActivity.this,imagesArray));


        CircleIndicator indicator = findViewById(R.id.indicator);

        indicator.setViewPager(mPager);

        //final float density = getResources().getDisplayMetrics().density;

        //Set circle indicator radius
        //indicator.setRadius(5 * density);

        NUM_PAGES = images.length;

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };

        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 3000, 3000);

        // Pager listener over indicator
        //noinspection deprecation
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currentPage = position;

            }

            @Override
            public void onPageScrolled(int pos, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int pos) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        MainActivity.this.finish();
    }

}
