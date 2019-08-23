package com.breta.scannerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;
import android.graphics.Matrix;
import android.util.Rational;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private TextureView viewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        viewFinder = findViewById(R.id.view_finder);

        if (allPermissionsGranted()) {
            viewFinder.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {
        Rational screenAspectRatio = new Rational(viewFinder.getWidth(), viewFinder.getHeight());
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .build();
        Preview preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(
            new Preview.OnPreviewOutputUpdateListener() {
                @Override
                public void onUpdated(Preview.PreviewOutput output) {
                    ViewGroup parent = (ViewGroup) viewFinder.getParent();
                    parent.removeView(viewFinder);
                    parent.addView(viewFinder, 0);

                    viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                }
            }
        );

        CameraX.bindToLifecycle((LifecycleOwner) this, preview);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post(new Runnable() {
                    @Override
                    public void run() {
                        startCamera();
                    }
                });
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission: REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
