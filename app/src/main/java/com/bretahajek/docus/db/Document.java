package com.bretahajek.docus.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.DateFormat;
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

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getFolder() {
        return folder;
    }

    public int getPageCount() {
        return pageCount;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getFormatedDate() {
        return DateFormat.getDateInstance().format(getCreationDate());
    }
}
