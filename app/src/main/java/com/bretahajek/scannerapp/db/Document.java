package com.bretahajek.scannerapp.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Document {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    @NonNull
    public String name;

    @ColumnInfo(name = "folder")
    @NonNull
    public String folder;

    @ColumnInfo(name = "page_count")
    public int pageCount;

    @ColumnInfo(name = "creation_date")
    public Date creationDate;

    public Document(
            final String name, final String folder, final Date creationDate, final int pageCount) {
        this.name = name;
        this.folder = folder;
        this.pageCount = pageCount;
        this.creationDate = creationDate;
    }
}
