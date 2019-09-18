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

    @Query("SELECT d.id, d.name, d.page_count, d.folder, d.creation_date FROM document AS d " +
            "INNER JOIN document_tag_join " +
            "ON d.id=document_tag_join.document_id " +
            "WHERE document_tag_join.tag_id=:tagId")
    LiveData<List<Document>> getDocumentsForTag(final int tagId);

    @Query("SELECT t.id, t.name FROM tag AS t " +
            "INNER JOIN document_tag_join " +
            "ON t.id=document_tag_join.tag_id " +
            "WHERE document_tag_join.document_id=:documentId")
    LiveData<List<Tag>> getTagsForDocument(final int documentId);


}
