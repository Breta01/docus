package com.bretahajek.scannerapp.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
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

    public Tag(final String name, final int documentCount) {
        this.name = name;
        this.documentCount = documentCount;
    }
}
