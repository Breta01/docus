package com.bretahajek.docus.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TagDao {
    @Query("SELECT * FROM tag")
    LiveData<List<Tag>> getAll();

    @Query("SELECT * FROM tag WHERE id IN (:tagIds)")
    LiveData<List<Tag>> loadAllByIds(int[] tagIds);

    @Query("SELECT * FROM tag WHERE name LIKE :name LIMIT 1")
    LiveData<Tag> findByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Tag... tags);

    @Delete
    void delete(Tag tag);

    @Update
    void update(Tag tag);
}
