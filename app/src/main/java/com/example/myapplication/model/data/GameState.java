package com.example.myapplication.model.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_state")
public class GameState {
    @PrimaryKey
    public int id = 1; // Always 1 for a singleton game state
    public int currentLives;
    public long lastLifeRefillTime; // Unix timestamp in milliseconds

    public GameState(int currentLives, long lastLifeRefillTime) {
        this.currentLives = currentLives;
        this.lastLifeRefillTime = lastLifeRefillTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCurrentLives() { return currentLives; }
    public void setCurrentLives(int currentLives) { this.currentLives = currentLives; }
    public long getLastLifeRefillTime() { return lastLifeRefillTime; }
    public void setLastLifeRefillTime(long lastLifeRefillTime) { this.lastLifeRefillTime = lastLifeRefillTime; }
}