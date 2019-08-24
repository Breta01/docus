package com.bretahajek.scannerapp;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;


public class CameraActivity extends AppCompatActivity {
    private FrameLayout container;
    private static int IMMERSIVE_FLAG_TIMEOUT = 500;

    private Runnable immersiveModeRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        container = findViewById(R.id.fragment_container);
        container.postDelayed(immersiveModeRunnable, IMMERSIVE_FLAG_TIMEOUT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        container.postDelayed(immersiveModeRunnable, IMMERSIVE_FLAG_TIMEOUT);
    }
}
