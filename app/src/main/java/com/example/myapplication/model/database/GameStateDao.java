package com.example.myapplication.model.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.data.GameState;

@Dao
public interface GameStateDao {
    @Query("SELECT * FROM game_state WHERE id = 1")
    LiveData<GameState> getGameState();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GameState gameState);

    @Update
    void update(GameState gameState);
}