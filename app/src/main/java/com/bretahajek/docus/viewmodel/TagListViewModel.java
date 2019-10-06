package com.bretahajek.docus.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.bretahajek.docus.DataRepository;
import com.bretahajek.docus.ScannerApp;
import com.bretahajek.docus.db.Document;
import com.bretahajek.docus.db.Tag;

import java.util.List;

public class TagListViewModel extends AndroidViewModel {
    private final DataRepository mRepository;
    private final MediatorLiveData<List<Tag>> mObservableTags;

    public TagListViewModel(@NonNull Application application) {
        super(application);

        mRepository = ((ScannerApp) application).getRepository();

        mObservableTags = new MediatorLiveData<>();
        mObservableTags.setValue(null);

        LiveData<List<Tag>> tags = mRepository.getTags();
        mObservableTags.addSource(tags, new Observer<List<Tag>>() {
            @Override
            public void onChanged(List<Tag> tags) {
                mObservableTags.postValue(tags);
            }
        });
    }

    public LiveData<List<Tag>> getTags() {
        return mObservableTags;
    }

    public LiveData<List<Tag>> getDocumentTags(final Document document) {
        return mRepository.getDocumentTags(document);
    }

    public void createTag(Tag tag) {
        mRepository.insertTags(tag);
    }

    public void updateDocumentTags(Document document, List<Tag> tags) {
        mRepository.updateDocumentTagJoin(document, tags);
    }
}
