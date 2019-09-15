package com.bretahajek.scannerapp.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.ui.DocumentAdapter;
import com.bretahajek.scannerapp.ui.DocumentClickCallback;
import com.bretahajek.scannerapp.viewmodel.DocumentListViewModel;

import java.util.List;


public class HomeFragment extends Fragment {
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int REQUEST_CODE_PERMISSIONS = 10;

    private DocumentAdapter mDocumentAdapter;
    private FragmentHomeBinding mBinding;

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
        final DocumentListViewModel viewModel =
                new ViewModelProvider(this).get(DocumentListViewModel.class);

        // TODO: Add tags...

        mBinding.searchView.setOnQueryTextListener(
                new android.widget.SearchView.OnQueryTextListener() {
                    private void updateSelectedDocuments(String query) {
                        if (query == null || query.isEmpty()) {
                            subscribeUi(viewModel.getDocuments());
                        } else {
                            subscribeUi(viewModel.searchDocuments(query));
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

        subscribeUi(viewModel.getDocuments());
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

    private void subscribeUi(LiveData<List<Document>> liveData) {
        // Update the list when the data changes
        liveData.observe(this, new Observer<List<Document>>() {
            @Override
            public void onChanged(@Nullable List<Document> myDocuments) {
                if (myDocuments != null) {
                    mDocumentAdapter.setDocumentList(myDocuments);
                    mBinding.executePendingBindings();
                }
            }
        });
    }

    private final DocumentClickCallback mDocumentClickCallback = new DocumentClickCallback() {
        @Override
        public void onClick(Document document) {

            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                Log.i("Document Event", document.getName());
//                ((MainActivity) getActivity()).show(product);
            }
        }
    };
}
