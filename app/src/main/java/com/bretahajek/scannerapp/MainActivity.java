package com.bretahajek.scannerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.Manifest;
import android.graphics.Matrix;
import android.util.Log;
import android.util.Rational;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private TextureView viewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent myIntent = new Intent(this, CameraActivity.class);
        startActivity(myIntent);


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

    private ImageCapture getImageCapture(Rational aspectRation) {
        ImageCaptureConfig config = new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(aspectRation)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();
        final ImageCapture imageCapture = new ImageCapture(config);

        ImageButton captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = new File(getFilesDir(),  "test.jpg");
                    imageCapture.takePicture(file,
                        new ImageCapture.OnImageSavedListener() {
                            @Override
                            public void onImageSaved(@NonNull File file) {
                                String msg = "Image saved successfully.";
                                Toast.makeText(
                                        getBaseContext(),
                                        msg,
                                        Toast.LENGTH_SHORT).show();
                                Log.e("ScannerApp", msg);
                            }
                            @Override
                            public void onError(
                                    @NonNull ImageCapture.UseCaseError useCaseError,
                                    @NonNull String message,
                                    Throwable cause) {
                                String msg = "Saving image failed. Please try again.";
                                Toast.makeText(
                                    getBaseContext(),
                                    msg,
                                    Toast.LENGTH_SHORT).show();
                                Log.e("ScannerApp", msg);
                                Log.e("ScannerApp", message);
                            }
                        });
                }
            }
        );

        return imageCapture;
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

        ImageCapture imageCapture = getImageCapture(screenAspectRatio);

        CameraX.bindToLifecycle(this, preview, imageCapture);
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
