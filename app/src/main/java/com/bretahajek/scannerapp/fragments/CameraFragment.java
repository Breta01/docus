package com.bretahajek.scannerapp.fragments;

import android.app.ActionBar;
import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.ocr.PageDetector;
import com.bretahajek.scannerapp.ui.PageSurface;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.ByteBuffer;


public class CameraFragment extends Fragment {
    private static int IMMERSIVE_FLAG_TIMEOUT = 300;
    private OnFragmentInteractionListener callback;
    private TextureView viewFinder;
    private PageSurface pageSurface;
    private ConstraintLayout container;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (!HomeFragment.allPermissionsGranted(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                    CameraFragmentDirections.actionCameraToHome()
            );
        }

        analyzerThread.start();
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

        Button cameraButton = view.findViewById(R.id.button_back_home);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageSurface.pause();
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                        CameraFragmentDirections.actionCameraToHome()
                );
            }
        });

        pageSurface = (PageSurface) container.getViewById(R.id.page_surface);

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
        ImageAnalysis imageAnalysis = getImageAnalysis(screenAspectRatio);

        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
    }

    private ImageCapture getImageCapture(Rational aspectRatio) {
        ImageCaptureConfig config = new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
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
                                        Log.d(getString(R.string.app_name), msg);
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

        return imageCapture;
    }

    private ImageAnalysis getImageAnalysis(Rational aspectRation) {
        ImageAnalysisConfig config = new ImageAnalysisConfig.Builder()
                .setTargetAspectRatio(aspectRation)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis(config);

        imageAnalysis.setAnalyzer(new PageAnalyzer());

        return imageAnalysis;
    }

    private class PageAnalyzer implements ImageAnalysis.Analyzer {
        private Mat imageToMat(ImageProxy image) {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            int height = image.getHeight();
            int width = image.getWidth();
            // Get YUV channels
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] data = new byte[ySize + uSize + vSize];

            yBuffer.get(data, 0, ySize);
            uBuffer.get(data, ySize, uSize);
            vBuffer.get(data, ySize + uSize, vSize);

            Mat yuvMat = new Mat(height + (height / 2), width, CvType.CV_8UC1);
            yuvMat.put(0, 0, data);
            Mat rgbMat = new Mat(height, width, CvType.CV_8UC3);
            Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_I420, 3);
            yuvMat.release();
            return rgbMat;
        }

        @Override
        public void analyze(ImageProxy image, int rotationDegrees) {

            if (image.getFormat() == 35 && image.getPlanes()[0].getPixelStride() == 1) {
                Mat matImage = imageToMat(image);
                Point[] corners = PageDetector.getPageCorners(matImage).toArray();
                pageSurface.updateCorners(corners);

                Log.d("ROTATION", Integer.toString(rotationDegrees));
                Log.d("Height Img", Integer.toString(image.getHeight()));
                Log.d("Width Img", Integer.toString(image.getWidth()));
                Log.d("Mat width", Integer.toString(matImage.width()));
                Log.d("Mat height", Integer.toString(matImage.height()));
                Log.d("Mat rows", Integer.toString(matImage.rows()));
                Log.d("Mat cols", Integer.toString(matImage.cols()));
            }
        }
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
