package com.bretahajek.docus.fragments;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bretahajek.docus.R;
import com.bretahajek.docus.fragments.CameraFragmentDirections.ActionCameraToPreview;
import com.bretahajek.docus.ocr.PageDetector;
import com.bretahajek.docus.ui.PageSurface;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;

import java.io.File;
import java.nio.ByteBuffer;


public class CameraFragment extends Fragment {
    private static int IMMERSIVE_FLAG_TIMEOUT = 300;

    private TextureView viewFinder;
    private PageSurface pageSurface;
    private ConstraintLayout container;
    private String documentName;
    private ImageCapture imageCapture = null;
    private ImageAnalysis imageAnalyzer = null;
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

    private ImageCapture.OnImageSavedListener imListener = new ImageCapture.OnImageSavedListener() {
        @Override
        public void onImageSaved(@NonNull File file) {
            String msg = getString(R.string.image_save_success);
            Toast.makeText(
                    getActivity().getBaseContext(),
                    msg,
                    Toast.LENGTH_SHORT).show();
            ActionCameraToPreview action = CameraFragmentDirections.actionCameraToPreview(file.getPath());
            action.setDocumentName(documentName);
            Navigation.findNavController(
                    requireActivity(), R.id.fragment_container).navigate(action);
        }

        @Override
        public void onError(
                @NonNull ImageCapture.ImageCaptureError imageCaptureError,
                @NonNull String message,
                @Nullable Throwable cause) {
            String msg = getString(R.string.image_save_failed);
            Toast.makeText(
                    getActivity().getBaseContext(),
                    msg,
                    Toast.LENGTH_SHORT).show();
            Log.i(getString(R.string.app_name), message);
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

        pageSurface = (PageSurface) container.getViewById(R.id.page_surface);

        viewFinder = (TextureView) container.getViewById(R.id.view_finder);
        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                buildCameraUi();
                startCamera();
            }
        });
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

    private void startCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        viewFinder.getDisplay().getRealMetrics(metrics);
        Rational screenAspectRatio = new Rational(metrics.widthPixels, metrics.heightPixels);

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

        ImageCaptureConfig config = new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();
        imageCapture = new ImageCapture(config);


        ImageAnalysisConfig analyzerConfig = new ImageAnalysisConfig.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();
        imageAnalyzer = new ImageAnalysis(analyzerConfig);
        imageAnalyzer.setAnalyzer(new PageAnalyzer());

        CameraX.bindToLifecycle(getViewLifecycleOwner(), preview, imageCapture, imageAnalyzer);
    }

    private void buildCameraUi() {
        // Go back button
        Button backButton = container.findViewById(R.id.button_back_home);
        if (documentName != null)
            backButton.setText(R.string.finish);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageSurface.pause();
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                        CameraFragmentDirections.actionCameraToHome()
                );
            }
        });

        // Camere capture button
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

                        imageCapture.takePicture(file, imListener);

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
        public void analyze(ImageProxy image, int rotationDegrees) {
            if (image.getFormat() == 35 && image.getPlanes()[0].getPixelStride() == 1) {
                Mat matImage = imageToGrayscaleMat(image);
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
