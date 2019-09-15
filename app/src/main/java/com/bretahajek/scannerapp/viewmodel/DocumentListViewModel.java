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

import java.io.File;
import java.util.List;

public class DocumentListViewModel extends AndroidViewModel {
    private final DataRepository mRepository;
    private final MediatorLiveData<List<Document>> mObservableDocuments;

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

    public LiveData<List<Document>> getDocuments() {
        return mObservableDocuments;
    }

    public LiveData<List<Document>> searchDocuments(String query) {
        return mRepository.searchDocuments(query);
    }

    public void deleteDocument(@NonNull Document document, File deleteFolder) {
        mRepository.deleteDocument(document, deleteFolder);
    }
}
