package com.bretahajek.scannerapp.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bretahajek.scannerapp.AppExecutors;
import com.bretahajek.scannerapp.DataRepository;
import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.ScannerApp;
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.ocr.PageDetector;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.io.FileUtils;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ScanPreviewFragment extends Fragment {
    private String documentName;
    private String imagePath;
    private boolean createNew = false;

    private ImageView scanPreview;
    private boolean imageReady = false;  // Check if image is ready for moving

    private AlertDialog dialog;

    private DataRepository dataRepository;
    private Activity mActivity;


    public ScanPreviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePath = ScanPreviewFragmentArgs.fromBundle(getArguments()).getImagePath();
        documentName = ScanPreviewFragmentArgs.fromBundle(getArguments()).getDocumentName();
        createNew = (documentName == null);

        // Run cropping in background
        new asyncScanPreview().execute(imagePath);

        mActivity = requireActivity();
        dataRepository = ((ScannerApp) mActivity.getApplication()).getRepository();
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
        buildDialog(false);
    }

    private void buildDialog(boolean rebuild) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final TextInputEditText textInput = new TextInputEditText(requireContext());

        if (documentName == null) {
            final TextInputLayout inputLayout = new TextInputLayout(getContext());
            inputLayout.setPadding(
                    getResources().getDimensionPixelOffset(R.dimen.dp_19),
                    0,
                    getResources().getDimensionPixelOffset(R.dimen.dp_19),
                    0
            );

            textInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            textInput.setHint(R.string.doc_name_hint);
            inputLayout.addView(textInput);

            builder.setTitle("Create New Document");
            builder.setView(inputLayout);
            if (rebuild) {
                builder.setMessage("Unable to create document, please try different name.");
            }
        } else {
            builder.setTitle("Document: " + documentName);
            builder.setMessage("Adding the new page to the document.");
        }

        builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (documentName == null) {
                    documentName = textInput.getText().toString().trim();
                }

                saveImageToDocument("home");
            }
        });
        builder.setNeutralButton("Add page", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (documentName == null) {
                    documentName = textInput.getText().toString().trim();
                }

                saveImageToDocument("camera");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                goBackToCamera(documentName);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                goBackToCamera(documentName);
            }
        });
        dialog = builder.create();

        if (documentName == null) {
            textInput.addTextChangedListener(new TextWatcher() {
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
        }

        dialog.getWindow().setDimAmount(0.0f);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        if (documentName == null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
        }
    }

    private String stringToDirectory(String s) {
        String convertedString = Normalizer
                .normalize(s, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9_/\\.]", "_");
        convertedString = convertedString.substring(0, Math.min(convertedString.length(), 100));
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        return convertedString + "-" + timestamp;
    }

    private void saveImageToDocument(final String goTo) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Document document = dataRepository.findByName(documentName);
                boolean create = (document == null) || createNew;
                if (create) {
                    String documentString = stringToDirectory(documentName);
                    document = new Document(documentName, documentString, new Date(), 1);
                } else {
                    document.pageCount += 1;
                }

                File documentDir = new File(
                        getActivity().getExternalFilesDir(null), document.folder);

                if (!documentDir.exists() && !documentDir.mkdirs()) {
                    Log.e("ScanPreviewFragment", "Unable to create the document.");
                    documentName = null;
                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            buildDialog(true);
                        }
                    });

                } else {
                    while (!imageReady) {
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {
                        }
                    }

                    File sourceFile = new File(imagePath);
                    File dstFile = new File(
                            documentDir, document.pageCount + ".jpg");
                    try {
                        FileUtils.moveFile(sourceFile, dstFile);
                    } catch (IOException e) {
                        Log.e("ScanPreviewFragment", "Unable to save file.");
                        documentName = null;
                        buildDialog(true);
                    }

                    if (create) {
                        dataRepository.insertDocuments(document);
                    } else {
                        dataRepository.updateDocument(document);
                    }

                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (goTo.equals("camera")) {
                                goBackToCamera(documentName);
                            } else if (goTo.equals("home")) {
                                goHome();
                            }
                        }
                    });
                }
            }
        });
    }

    private void goHome() {
        Navigation.findNavController(
                mActivity, R.id.fragment_container)
                .navigate(ScanPreviewFragmentDirections.actionPreviewToHome());
    }

    private void goBackToCamera(String docName) {
        ScanPreviewFragmentDirections.ActionPreviewToCamera action =
                ScanPreviewFragmentDirections.actionPreviewToCamera();
        action.setDocumentName(docName);
        Navigation.findNavController(
                mActivity, R.id.fragment_container).navigate(action);
    }

    private class asyncScanPreview extends AsyncTask<String, Bitmap, Void> {
        @Override
        protected Void doInBackground(String... paths) {
            Mat image = Imgcodecs.imread(paths[0]);
            Mat rgbImage = Mat.zeros(image.size(), image.type());

            image = PageDetector.getPage(image);

            Imgproc.cvtColor(image, rgbImage, Imgproc.COLOR_BGR2RGB);
            Bitmap bitmap = Bitmap.createBitmap(
                    rgbImage.cols(), rgbImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgbImage, bitmap);
            publishProgress(bitmap);

            // Display image then save
            Imgcodecs.imwrite(paths[0], image);
            imageReady = true;
            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            // Wait for scan preview in case it hasn't been loaded yet
            while (scanPreview == null) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                }
            }
            scanPreview.setImageBitmap(values[0]);
        }
    }
}
