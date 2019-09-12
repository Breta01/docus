package com.bretahajek.scannerapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.bretahajek.scannerapp.utils.Converters;

@Database(entities = {Document.class, Tag.class, DocumentTagJoin.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "document-db";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, DATABASE_NAME).build();
            }
        }
        return instance;
    }

    public abstract DocumentDao documentDao();

    public abstract TagDao tagDao();

    public abstract DocumentTagJoinDao documentTagJoinDao();
}
