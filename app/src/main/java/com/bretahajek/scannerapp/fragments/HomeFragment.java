package com.bretahajek.scannerapp.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bretahajek.scannerapp.AppExecutors;
import com.bretahajek.scannerapp.BuildConfig;
import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.databinding.FragmentHomeBinding;
import com.bretahajek.scannerapp.databinding.TagChipBinding;
import com.bretahajek.scannerapp.databinding.TagsDialogBinding;
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.db.Tag;
import com.bretahajek.scannerapp.ui.DocumentAdapter;
import com.bretahajek.scannerapp.ui.DocumentClickCallback;
import com.bretahajek.scannerapp.ui.TagAdapter;
import com.bretahajek.scannerapp.utils.Exporter;
import com.bretahajek.scannerapp.viewmodel.DocumentListViewModel;
import com.bretahajek.scannerapp.viewmodel.TagListViewModel;

import java.io.File;
import java.util.List;


public class HomeFragment extends Fragment {
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int REQUEST_CODE_PERMISSIONS = 10;

    private FragmentHomeBinding mBinding;

    private DocumentAdapter mDocumentAdapter;
    private TagAdapter mTagAdapter;

    private DocumentListViewModel documentViewModel;
    private TagListViewModel tagViewModel;

    public HomeFragment() {
    }

    public static boolean allPermissionsGranted(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!allPermissionsGranted(requireContext())) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);

        mTagAdapter = new TagAdapter();
        mDocumentAdapter = new DocumentAdapter(mDocumentClickCallback);
        mBinding.documentsList.setAdapter(mDocumentAdapter);

        if (documentViewModel != null) {
            documentViewModel.clearFilterTags();
        }

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                        HomeFragmentDirections.actionHomeToCamera()
                );
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        documentViewModel = new ViewModelProvider(this).get(DocumentListViewModel.class);
        tagViewModel = new ViewModelProvider(this).get(TagListViewModel.class);

        mBinding.searchView.setOnQueryTextListener(
                new android.widget.SearchView.OnQueryTextListener() {
                    private void updateSelectedDocuments(String query) {
                        documentViewModel.searchDocuments(query);
                    }

                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        updateSelectedDocuments(s);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        updateSelectedDocuments(s);
                        return false;
                    }
                });

        subscribeUi(documentViewModel.getDocuments());
        subsribeTagsUi(tagViewModel.getTags());
    }

    private void inflateTags(List<Tag> tagList) {
        for (Tag tag : tagList) {
            final TagChipBinding binding = DataBindingUtil
                    .inflate(requireActivity().getLayoutInflater(), R.layout.tag_chip,
                            null, false);
            binding.setTag(tag);
            binding.setCallback(this);
            mBinding.tagGroup.addView(binding.getRoot());
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted(requireContext())) {
                Toast.makeText(
                        requireContext(),
                        getString(R.string.permission_notgranted),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void subscribeUi(LiveData<List<Document>> liveDocumentData) {
        // Update the document list when the data changes
        liveDocumentData.observe(this, new Observer<List<Document>>() {
            @Override
            public void onChanged(@Nullable List<Document> myDocuments) {
                if (myDocuments != null) {
                    mDocumentAdapter.setDocumentList(myDocuments);
                    mBinding.executePendingBindings();
                }
            }
        });
    }

    private void subsribeTagsUi(LiveData<List<Tag>> liveTagData) {
        liveTagData.observe(this, new Observer<List<Tag>>() {
            @Override
            public void onChanged(@Nullable List<Tag> myTags) {
                if (myTags != null) {
                    if (mTagAdapter.getItemCount() < myTags.size()) {
                        // Add new tags to list
                        // TODO: Implement removing
                        inflateTags(myTags.subList(mTagAdapter.getItemCount(), myTags.size()));
                    }

                    mTagAdapter.setTagList(myTags);
                    mBinding.executePendingBindings();
                }
            }
        });
    }

    private void subsribeDocumentTagsUi(final LiveData<List<Tag>> liveTagData) {
        // TODO: Change updating
        liveTagData.observe(this, new Observer<List<Tag>>() {
            @Override
            public void onChanged(@Nullable List<Tag> myTags) {
                if (myTags != null) {
                    mTagAdapter.setDocumentTagList(myTags);
                    // Remove observer after loading - need them only on spawn of dialog
                    liveTagData.removeObserver(this);
                }
            }
        });
    }

    private void buildTagsDialog(final Document document) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Set Tags - " + document.getName());

        final TagsDialogBinding binding = DataBindingUtil
                .inflate(requireActivity().getLayoutInflater(), R.layout.tags_dialog,
                        null, false);

        subsribeDocumentTagsUi(tagViewModel.getDocumentTags(document));
        binding.tagsRecyclerView.setAdapter(mTagAdapter);
        binding.addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.addTagText.getText().toString();
                if (name.length() != 0) {
                    Tag tag = new Tag(name);
                    tagViewModel.createTag(tag);
                    binding.addTagText.setText("");
                }
            }
        });

        builder.setView(binding.getRoot());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                tagViewModel.updateDocumentTags(document, mTagAdapter.getTagList());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mTagAdapter.clearTagStates();
            }
        });

        builder.create().show();
    }

    private void showHideProgressBar(boolean show) {
        if (show) {
            mBinding.progressBar.setVisibility(View.VISIBLE);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            mBinding.progressBar.setVisibility(View.GONE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void exportDocument(Document document) {
        final File documentFolder = new File(
                getActivity().getExternalFilesDir(null), document.getFolder());
        final File output = new File(
                documentFolder, document.getFolder().split("-")[0] + ".pdf");

        showHideProgressBar(true);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final File out = Exporter.exportAsPdf(documentFolder, output);

                if (out != null) {
                    Uri uri = FileProvider.getUriForFile(
                            getContext(), BuildConfig.APPLICATION_ID + ".provider", out);
                    Intent intent = new Intent()
                            .setAction(Intent.ACTION_SEND)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf"))
                            .putExtra(Intent.EXTRA_STREAM, uri);

                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            showHideProgressBar(false);
                        }
                    });

                    startActivity(Intent.createChooser(intent, "Share exported file."));
                } else {
                    // TODO: Handle errors, retry
                }
            }
        });


    }

    private void buildPopupMenu(DocumentAdapter.DocumentViewHolder holder, View bindView) {
        PopupMenu menu = new PopupMenu(getContext(), bindView);
        menu.inflate(R.menu.document_card_menu);

        final Document document = holder.binding.getDocument();
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    // TODO: Add rename button
                    case R.id.dc_menu_export:
                        exportDocument(document);
                        break;
                    case R.id.dc_menu_tags:
                        buildTagsDialog(document);
                        break;
                    case R.id.dc_menu_delete:
                        // TODO: "Are you sure" dialog
                        File deleteFolder = new File(
                                getContext().getExternalFilesDir(null), document.getFolder());
                        documentViewModel.deleteDocument(document, deleteFolder);
                        break;
                }
                return false;
            }
        });
        menu.show();
    }

    private final DocumentClickCallback mDocumentClickCallback = new DocumentClickCallback() {
        @Override
        public void onClick(Document document) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                Navigation.findNavController(getActivity(), R.id.fragment_container).navigate(
                        HomeFragmentDirections.actionHomeToDocument(document.getFolder()));
            }
        }

        @Override
        public void onLongClick(DocumentAdapter.DocumentViewHolder viewHolder, View bindView) {
            buildPopupMenu(viewHolder, bindView);
        }

        @Override
        public void onMenuClick(DocumentAdapter.DocumentViewHolder viewHolder, View bindView) {
            buildPopupMenu(viewHolder, bindView);
        }
    };

    public void onTagCheckedChanged(Tag tag, boolean isChecked) {
        Log.i("Check", tag.getName());
        documentViewModel.onTagCheckedChanged(tag, isChecked);
    }
}
