package com.bretahajek.scannerapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.bretahajek.scannerapp.db.AppDatabase;
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.db.Tag;

import java.util.List;

public class DataRepository {
    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private final AppExecutors mExecutors;
    private MediatorLiveData<List<Document>> mObservableDocuments;

    private DataRepository(final AppDatabase database, final AppExecutors executors) {
        mDatabase = database;
        mExecutors = executors;

        mObservableDocuments = new MediatorLiveData<>();
        mObservableDocuments.addSource(
                mDatabase.documentDao().getAll(),
                new Observer<List<Document>>() {
                    @Override
                    public void onChanged(List<Document> documents) {
                        mObservableDocuments.postValue(documents);
                    }
                });
    }

    public static DataRepository getInstance(
            final AppDatabase database, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database, executors);
                }
            }
        }
        return sInstance;
    }

    // Accessing the database
    public LiveData<List<Document>> getDocuments() {
        return mObservableDocuments;
    }

    public Document findByName(final String name) {
        return mDatabase.documentDao().findByName(name);
    }

    public void insertDocuments(final Document... documents) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.documentDao().insertAll(documents);
            }
        });
    }

    public void updateDocument(final Document document) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.documentDao().update(document);
            }
        });
    }

    public LiveData<List<Tag>> getTags() {
        return mDatabase.tagDao().getAll();
    }
}
