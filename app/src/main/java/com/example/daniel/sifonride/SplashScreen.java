package com.example.daniel.sifonride;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread splash = new Thread() {
            @Override
            public void run() {

                try {
                    sleep(1000 * 3);
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.run();
            }
        };
        splash.start();
    }
}
