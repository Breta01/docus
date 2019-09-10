package com.bretahajek.scannerapp;

import android.os.AsyncTask;

import com.bretahajek.scannerapp.db.AppDatabase;
import com.bretahajek.scannerapp.db.Document;
import com.bretahajek.scannerapp.utils.AsyncResponse;

public class DataRepository {
    private static DataRepository sInstance;

    private final AppDatabase mDatabase;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
    }

    public static DataRepository getInstance(final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    public class GetDocumentAsync extends AsyncTask<String, Void, Document> {
        public AsyncResponse<Document> delegate = null;
        private AppDatabase database;

        public GetDocumentAsync(AppDatabase database, AsyncResponse delegate) {
            this.database = database;
            this.delegate = delegate;
        }

        @Override
        protected Document doInBackground(String... names) {
            return database.documentDao().findByName(names[0]);
        }

        @Override
        protected void onPostExecute(Document result) {
            delegate.processFinish(result);
        }
    }
}
