package com.bretahajek.docus.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.Navigation;

import com.bretahajek.docus.MainActivity;
import com.bretahajek.docus.R;
import com.bretahajek.docus.fragments.CameraFragmentDirections.ActionCameraToPreview;
import com.bretahajek.docus.ocr.PageDetector;
import com.bretahajek.docus.ui.PageSurface;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


public class CameraFragment extends Fragment {
    private static String MY_PREFS = "flash_prefs";
    private static String FLASH_STATUS = "flash_on";
    private boolean isFlashOn;

    SharedPreferences myPreferences;
    SharedPreferences.Editor myEditor;

    private static int IMMERSIVE_FLAG_TIMEOUT = 300;
    private Camera cam;
    private TextureView viewFinder;
    private PageSurface pageSurface;
    private PreviewView previewView;
    private ConstraintLayout container;
    private String documentName;
    private Preview preview = null;

    private ImageCapture imageCapture = null;
    private ImageAnalysis imageAnalyzer = null;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private HandlerThread analyzerThread = new HandlerThread("PageAnalysis");
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
    }

    private ImageCapture.OnImageSavedCallback imListener = new ImageCapture.OnImageSavedCallback() {
        @Override
        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
            String msg = getString(R.string.image_save_success);
            Toast.makeText(
                    getActivity().getBaseContext(),
                    msg,
                    Toast.LENGTH_SHORT).show();
            ActionCameraToPreview action = CameraFragmentDirections.actionCameraToPreview(outputFileResults.getSavedUri().getPath());
            action.setDocumentName(documentName);
            toggleFlashActivity(false);
            Navigation.findNavController(
                    requireActivity(), R.id.fragment_container).navigate(action);
        }

        @Override
        public void onError(@NonNull ImageCaptureException exception) {
            String msg = getString(R.string.image_save_failed);
            Toast.makeText(
                    getActivity().getBaseContext(),
                    msg,
                    Toast.LENGTH_SHORT).show();
            Log.i(getString(R.string.app_name), exception.getMessage());
            Log.e(getString(R.string.app_name), msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        documentName = CameraFragmentArgs.fromBundle(getArguments()).getDocumentName();

        if (!HomeFragment.allPermissionsGranted(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                    CameraFragmentDirections.actionCameraToHome()
            );
        }

        // handle when back button is pressed - mainly to turn of flash.
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                toggleFlashActivity(false);
                pageSurface.pause();
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                        CameraFragmentDirections.actionCameraToHome()
                );
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // store flash status
        myPreferences = getActivity().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        myEditor = getActivity().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE).edit();

        isFlashOn = myPreferences.getBoolean(FLASH_STATUS, false);

        cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());
        analyzerThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = (ConstraintLayout) view;
        container.post(immersiveModeRunnable);
        previewView = view.findViewById(R.id.preview_view);
        pageSurface = (PageSurface) container.getViewById(R.id.page_surface);

        viewFinder = (TextureView) container.getViewById(R.id.view_finder);
        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                cameraProviderFuture.addListener(() -> {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        startCamera(cameraProvider);
                        buildCameraUi();
                    } catch (ExecutionException | InterruptedException e) {
                        //No errors need to be handled
                    }
                }, ContextCompat.getMainExecutor(getContext()));
            }
        });
    }

    private void toggleFlashActivity(boolean flash){
        cam.getCameraControl().enableTorch(flash);
    }

    @Override
    public void onResume() {
        super.onResume();
        container.postDelayed(immersiveModeRunnable, IMMERSIVE_FLAG_TIMEOUT);

        if (!HomeFragment.allPermissionsGranted(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                    CameraFragmentDirections.actionCameraToHome()
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pageSurface.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void startCamera(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();


        DisplayMetrics metrics = new DisplayMetrics();
        viewFinder.getDisplay().getRealMetrics(metrics);
        Size screenSize = new Size(metrics.widthPixels, metrics.heightPixels);

        preview = new Preview.Builder()
                .setTargetResolution(screenSize)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(screenSize)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetResolution(screenSize)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor(), new PageAnalyzer());

        cam = cameraProvider.bindToLifecycle((LifecycleOwner) getActivity(), cameraSelector, preview, imageCapture, imageAnalyzer);
    }

    private void buildCameraUi() {
        // Go back button
        Button backButton = container.findViewById(R.id.button_back_home);
        if (documentName != null)
            backButton.setText(R.string.finish);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFlashActivity(false);
                pageSurface.pause();
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                        CameraFragmentDirections.actionCameraToHome()
                );
            }
        });

        // Toggle flash button
        ToggleButton toggleFlash = container.findViewById(R.id.toggle_flash);
        toggleFlash.setChecked(isFlashOn);
        toggleFlashActivity(isFlashOn);

        toggleFlash.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(cam.getCameraInfo().hasFlashUnit()){
                            isFlashOn = toggleFlash.isChecked();
                            myEditor.putBoolean(FLASH_STATUS, isFlashOn);
                            myEditor.apply();
                            toggleFlashActivity(isFlashOn);
                        } else{
                            Toast.makeText(getActivity(), "Flash not available", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // Camera capture button
        ImageButton captureButton = container.findViewById(R.id.capture_button);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File file = new File(
                                getActivity().getExternalFilesDir(null),
                                "tmp.jpg");
                        Log.i(
                                getString(R.string.app_name),
                                "Saving to: " + file.getAbsolutePath());

                        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

                        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(getActivity()), imListener);

                        // If possible display flash animation to indicate that photo was captured
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
    }

    private class PageAnalyzer implements ImageAnalysis.Analyzer {
        private Mat imageToGrayscaleMat(ImageProxy image) {
            ImageProxy.PlaneProxy plane = image.getPlanes()[0];
            int height = image.getHeight();
            int width = image.getWidth();
            // Get Y channel - gray
            ByteBuffer yBuffer = plane.getBuffer();
            return new Mat(height, width, CvType.CV_8UC1, yBuffer);
        }

        @Override
        public void analyze(ImageProxy image) {
            if (image.getFormat() == 35 && image.getPlanes()[0].getPixelStride() == 1) {
                Mat matImage = imageToGrayscaleMat(image);
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                if (rotationDegrees != 0) {
                    int rotate = -1;
                    switch (rotationDegrees) {
                        case 90:
                            rotate = Core.ROTATE_90_CLOCKWISE;
                            break;
                        case 180:
                            rotate = Core.ROTATE_180;
                            break;
                        case 270:
                            rotate = Core.ROTATE_90_COUNTERCLOCKWISE;
                            break;
                    }
                    if (rotate != -1) {
                        Core.rotate(matImage, matImage, rotate);
                    }
                }

                MatOfPoint corners = PageDetector.getPageCorners(matImage);
                MatOfPoint2f relCorners = new MatOfPoint2f();
                corners.convertTo(relCorners, CvType.CV_32F);
                Core.multiply(
                        relCorners,
                        new Scalar(1 / (float) matImage.cols(), 1 / (float) matImage.rows()),
                        relCorners);

                pageSurface.updateCorners(relCorners.toArray());
            }
        }
    }
}
