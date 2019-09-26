package com.bretahajek.scannerapp;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.bretahajek.scannerapp.db.AppDatabase;
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.db.DocumentTagJoin;
import com.bretahajek.scannerapp.db.Tag;

import org.apache.commons.io.FileUtils;

import java.io.File;
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

    public LiveData<List<Document>> searchDocuments(String query) {
        return mDatabase.documentDao().searchAll(query);
    }

    public LiveData<List<Document>> getDocumentsWithTags(int[] tagIds, int minMatchCount) {
        return mDatabase.documentDao().getAllWithTags(tagIds, minMatchCount);
    }

    public LiveData<List<Document>> searchDocumentsWithTags(
            String query, int[] tagIds, int minMatchCount) {
        return mDatabase.documentDao().searchAllWithTags(query, tagIds, minMatchCount);
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

    public void deleteDocument(final Document document, final File deleteFolder) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (deleteFolder != null) {
                    try {
                        FileUtils.deleteDirectory(deleteFolder);
                        mDatabase.documentDao().delete(document);
                    } catch (Exception e) {
                        Log.e("Data Repository", "Unable to delete document.");
                    }
                }
            }
        });
    }

    public LiveData<List<Tag>> getTags() {
        return mDatabase.tagDao().getAll();
    }

    public LiveData<List<Tag>> getDocumentTags(final Document document) {
        return mDatabase.documentTagJoinDao().getTagsForDocument(document.getId());
    }

    public void insertTags(final Tag... tags) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.tagDao().insertAll(tags);
            }
        });
    }

    public void updateDocumentTagJoin(final Document document, final List<Tag> tags) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                for (Tag tag : tags) {
                    if (tag.isChanged()) {
                        DocumentTagJoin docTagJoin = new DocumentTagJoin(
                                document.getId(), tag.getId());

                        if (tag.isState()) {
                            mDatabase.documentTagJoinDao().insert(docTagJoin);
                        } else {
                            mDatabase.documentTagJoinDao().delete(docTagJoin);
                        }
                    }
                }
            }
        });

    }
}
