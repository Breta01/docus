package com.bretahajek.scannerapp.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Tag {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    @NonNull
    public String name;

    @ColumnInfo(name = "document_count")
    public int documentCount;

    @Ignore
    public boolean state;

    public Tag(final String name, final int documentCount) {
        this.name = name;
        this.documentCount = documentCount;
    }

    public Tag(final String name, final int documentCount, final boolean state) {
        this.name = name;
        this.documentCount = documentCount;
        this.state = state;
    }

    public void switchState() {
        state = !state;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public boolean isState() {
        return state;
    }
}
