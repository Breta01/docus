package com.bretahajek.scannerapp.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bretahajek.scannerapp.R;
import com.bretahajek.scannerapp.databinding.FragmentHomeBinding;
import com.bretahajek.scannerapp.databinding.TagsDialogBinding;
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.db.Tag;
import com.bretahajek.scannerapp.ui.DocumentAdapter;
import com.bretahajek.scannerapp.ui.DocumentClickCallback;
import com.bretahajek.scannerapp.ui.TagAdapter;
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

        // TODO: Add tags...

        mBinding.searchView.setOnQueryTextListener(
                new android.widget.SearchView.OnQueryTextListener() {
                    private void updateSelectedDocuments(String query) {
                        if (query == null || query.isEmpty()) {
                            subscribeUi(documentViewModel.getDocuments());
                        } else {
                            subscribeUi(documentViewModel.searchDocuments(query));
                        }
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
        // Update the list when the data changes
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
                    mTagAdapter.setTagList(myTags);
                    mBinding.executePendingBindings();
                }
            }
        });
    }

    private void addTag(String name) {
        if (name.length() != 0) {
            Tag tag = new Tag(name, 0);
            tagViewModel.createTag(tag);
        }
    }

    private void buildTagsDialog(Document document) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Set Tags - " + document.getName());

        final TagsDialogBinding binding = DataBindingUtil
                .inflate(requireActivity().getLayoutInflater(), R.layout.tags_dialog,
                        null, false);
        binding.tagsRecyclerView.setAdapter(mTagAdapter);
        binding.addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTag(binding.addTagText.getText().toString());
                binding.addTagText.setText("");
            }
        });


        builder.setView(binding.getRoot());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void buildPopupMenu(DocumentAdapter.DocumentViewHolder holder, View bindView) {
        PopupMenu menu = new PopupMenu(getContext(), bindView);
        menu.inflate(R.menu.document_card_menu);

        final Document document = holder.binding.getDocument();
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.dc_menu_export:
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
}
