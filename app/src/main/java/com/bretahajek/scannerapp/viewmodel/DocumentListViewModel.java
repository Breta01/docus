package com.bretahajek.scannerapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.bretahajek.scannerapp.DataRepository;
import com.bretahajek.scannerapp.ScannerApp;
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.db.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DocumentListViewModel extends AndroidViewModel {
    private final DataRepository mRepository;
    private final MediatorLiveData<List<Document>> mObservableDocuments;
    private List<Tag> filterTags = new ArrayList<>();

    public DocumentListViewModel(Application application) {
        super(application);

        mRepository = ((ScannerApp) application).getRepository();

        mObservableDocuments = new MediatorLiveData<>();
        mObservableDocuments.setValue(null);

        LiveData<List<Document>> documents = mRepository.getDocuments();
        mObservableDocuments.addSource(documents, new Observer<List<Document>>() {
            @Override
            public void onChanged(List<Document> documents) {
                mObservableDocuments.postValue(documents);
            }
        });
    }

    public void clearFilterTags() {
        filterTags.clear();
    }

    public LiveData<List<Document>> getDocuments() {
        if (filterTags.size() == 0) {
            return mObservableDocuments;
        } else {
            int[] tagIds = new int[filterTags.size()];
            for (int i = 0; i < filterTags.size(); i++) {
                tagIds[i] = filterTags.get(i).getId();
            }
            return mRepository.getDocumentsWithTags(tagIds, tagIds.length);
        }
    }

    public LiveData<List<Document>> searchDocuments(String query) {
        if (filterTags.size() == 0) {
            return mObservableDocuments;
        } else {
            int[] tagIds = new int[filterTags.size()];
            for (int i = 0; i < filterTags.size(); i++) {
                tagIds[i] = filterTags.get(i).getId();
            }
            return mRepository.searchDocumentsWithTags(query, tagIds, tagIds.length);
        }
    }

    public void deleteDocument(@NonNull Document document, File deleteFolder) {
        mRepository.deleteDocument(document, deleteFolder);
    }

    public void onTagCheckedChanged(Tag tag, boolean isChecked) {
        boolean b = (isChecked) ? filterTags.add(tag) : filterTags.remove(tag);
    }
}
