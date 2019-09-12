package com.bretahajek.scannerapp.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "document_tag_join",
        primaryKeys = {"document_id", "tag_id"},
        foreignKeys = {
                @ForeignKey(entity = Document.class,
                        parentColumns = "id",
                        childColumns = "document_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Tag.class,
                        parentColumns = "id",
                        childColumns = "tag_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class DocumentTagJoin {
    @ColumnInfo(name = "document_id")
    public int documentId;

    @ColumnInfo(name = "tag_id", index = true)
    public int tagId;

    public DocumentTagJoin(final int documentId, final int tagId) {
        this.documentId = documentId;
        this.tagId = tagId;
    }
}
