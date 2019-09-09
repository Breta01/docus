package com.bretahajek.scannerapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TagDao {
    @Query("SELECT * FROM tag")
    List<Tag> getAll();

    @Query("SELECT * FROM tag WHERE id IN (:tagIds)")
    List<Tag> loadAllByIds(int[] tagIds);

    @Query("SELECT * FROM tag WHERE name LIKE :name LIMIT 1")
    Tag findByName(String name);

    @Insert
    void insertAll(Tag... tags);

    @Delete
    void delete(Tag tag);

    @Update
    void update(Tag tag);
}
