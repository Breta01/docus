package com.bretahajek.scannerapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DocumentDao {
    @Query("SELECT * FROM document")
    List<Document> getAll();

    @Query("SELECT * FROM document WHERE id IN (:documentIds)")
    List<Document> loadAllByIds(int[] documentIds);

    @Query("SELECT * FROM document WHERE name LIKE :name LIMIT 1")
    Document findByName(String name);

    @Insert
    void insertAll(Document... documents);

    @Delete
    void delete(Document document);

    @Update
    void update(Document document);
}
