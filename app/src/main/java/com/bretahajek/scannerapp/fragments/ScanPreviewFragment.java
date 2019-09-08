package com.bretahajek.scannerapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.ocr.PageDetector;
import com.google.android.material.textfield.TextInputLayout;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class ScanPreviewFragment extends Fragment {
    private String documentName;
    private String imagePath;
    private ImageView scanPreview;


    public ScanPreviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        documentName = ScanPreviewFragmentArgs.fromBundle(getArguments()).getDocumentName();
        imagePath = ScanPreviewFragmentArgs.fromBundle(getArguments()).getImagePath();

        // Run cropping in background
        new asyncScanPreview().execute(imagePath);

        // TODO: Handle hardware back button separatly
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanPreview = view.findViewById(R.id.scanPreview);

        if (documentName == null) {
            // TODO: Change alert depending on document name

        }

        final TextInputLayout inputLayout = new TextInputLayout(getContext());
        inputLayout.setPadding(
                getResources().getDimensionPixelOffset(R.dimen.dp_19),
                0,
                getResources().getDimensionPixelOffset(R.dimen.dp_19),
                0
        );
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        input.setHint(R.string.doc_name_hint);
        inputLayout.addView(input);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Create New Document");
        builder.setView(inputLayout);

        builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Finish button
            }
        });
        builder.setNeutralButton("Add page", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Continue button
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });


        final AlertDialog dialog = builder.create();

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean enabled = editable.toString().length() != 0;
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(enabled);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(enabled);
            }
        });

        dialog.getWindow().setDimAmount(0.0f);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    private class asyncScanPreview extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... paths) {
            Mat image = Imgcodecs.imread(paths[0]);

            image = PageDetector.getPage(image);

            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
            Bitmap bitmap = Bitmap.createBitmap(
                    image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(image, bitmap);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Wait for scan preview in case it hasn't been loaded yet
            while (scanPreview == null) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }

            scanPreview.setImageBitmap(result);
        }
    }
}
