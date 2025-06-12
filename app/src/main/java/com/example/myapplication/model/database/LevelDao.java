package com.example.myapplication.model.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.data.Level;

import java.util.List;

@Dao
public interface LevelDao {
    @Query("SELECT * FROM levels WHERE chapterId = :chapterId ORDER BY levelNumber ASC")
    LiveData<List<Level>> getLevelsForChapter(int chapterId);


    @Query("SELECT * FROM levels WHERE chapterId = :chapterId ORDER BY levelNumber ASC")
    List<Level> getLevelsForChapterNonLive(int chapterId);

    @Query("SELECT * FROM levels WHERE id = :levelId")
    Level getLevelById(int levelId); // Non-Live version

    @Query("SELECT COUNT(*) FROM levels WHERE chapterId = :chapterId AND completed = 1")
    LiveData<Integer> getCompletedLevelsCountForChapter(int chapterId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Level level);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Level> levels);

    @Update
    void update(Level level);
}