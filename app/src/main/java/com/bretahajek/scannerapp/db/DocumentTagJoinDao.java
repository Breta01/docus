package com.bretahajek.scannerapp.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DocumentTagJoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DocumentTagJoin documentTagJoin);

    @Delete
    void delete(DocumentTagJoin documentTagJoin);

    @Query("SELECT * FROM document " +
            "INNER JOIN document_tag_join " +
            "ON document.id=document_tag_join.document_id " +
            "WHERE document_tag_join.tag_id=:tagId")
    LiveData<List<Document>> getDocumentsForTag(final int tagId);

    @Query("SELECT * FROM tag " +
            "INNER JOIN document_tag_join " +
            "ON tag.id=document_tag_join.tag_id " +
            "WHERE document_tag_join.document_id=:documentId")
    LiveData<List<Tag>> getTagsForDocument(final int documentId);


}
