package com.bretahajek.scannerapp;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.io.File;


public class CameraFragment extends Fragment {
    private static int IMMERSIVE_FLAG_TIMEOUT = 300;
    private OnFragmentInteractionListener callback;
    private TextureView viewFinder;
    private ConstraintLayout container;
    private Runnable immersiveModeRunnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getActivity().getActionBar();
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


    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        container.postDelayed(immersiveModeRunnable, IMMERSIVE_FLAG_TIMEOUT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = (ConstraintLayout) view;
        container.post(immersiveModeRunnable);

        viewFinder = (TextureView) container.getViewById(R.id.view_finder);
        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
    }

    private void startCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        viewFinder.getDisplay().getRealMetrics(metrics);
        Rational screenAspectRatio = new Rational(metrics.widthPixels, metrics.heightPixels);
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .build();
        // @TODO: Add on layout change listener
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

    private ImageCapture getImageCapture(Rational aspectRation) {
        ImageCaptureConfig config = new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(aspectRation)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();
        final ImageCapture imageCapture = new ImageCapture(config);

        ImageButton captureButton = container.findViewById(R.id.capture_button);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File file = new File(
                                getActivity().getExternalFilesDir(null),
                                "test.jpg");
                        Log.e(
                                getString(R.string.app_name),
                                "Saving to: " + file.getAbsolutePath());

                        imageCapture.takePicture(file,
                                new ImageCapture.OnImageSavedListener() {
                                    @Override
                                    public void onImageSaved(@NonNull File file) {
                                        String msg = getString(R.string.image_save_success);
                                        Toast.makeText(
                                                getActivity().getBaseContext(),
                                                msg,
                                                Toast.LENGTH_SHORT).show();
                                        Log.e(getString(R.string.app_name), msg);
                                    }

                                    @Override
                                    public void onError(
                                            @NonNull ImageCapture.UseCaseError useCaseError,
                                            @NonNull String message,
                                            Throwable cause) {
                                        String msg = getString(R.string.image_save_failed);
                                        Toast.makeText(
                                                getActivity().getBaseContext(),
                                                msg,
                                                Toast.LENGTH_SHORT).show();
                                        Log.e(getString(R.string.app_name), msg);
                                        Log.e(getString(R.string.app_name), message);
                                    }
                                });

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Display flash animation to indicate that photo was captured
                            container.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    container.setForeground(new ColorDrawable(Color.BLACK));
                                    container.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            container.setForeground(null);
                                        }
                                    }, 75);
                                }
                            }, 200);
                        }
                    }
                }
        );

        return imageCapture;
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener callback) {
        this.callback = callback;
    }

    /** Communicating with Other Fragments for more information.
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     */
    public interface OnFragmentInteractionListener {
    }
}
