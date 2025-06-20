package com.example.myapplication.model.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.data.Chapter;

import java.util.List;

@Dao
public interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY id ASC")
    LiveData<List<Chapter>> getAllChapters();

    @Query("SELECT * FROM chapters ORDER BY id ASC")
    List<Chapter> getAllChaptersNonLive();

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    Chapter getChapterById(int chapterId);

    @Query("SELECT COUNT(*) FROM chapters")
    int countChapters();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Chapter chapter);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Chapter> chapters);

    @Update
    void update(Chapter chapter);
}