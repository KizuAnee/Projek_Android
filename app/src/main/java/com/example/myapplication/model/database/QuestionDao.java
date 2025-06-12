package com.example.myapplication.model.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.model.data.Question;

import java.util.List;

@Dao
public interface QuestionDao {
    @Query("SELECT * FROM questions WHERE levelId = :levelId ORDER BY id ASC")
    LiveData<List<Question>> getQuestionsForLevel(int levelId);

    @Query("SELECT * FROM questions WHERE id = :questionId")
    Question getQuestionById(int questionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Question question);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Question> questions);
}