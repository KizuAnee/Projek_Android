package com.example.myapplication.model.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "levels")
public class Level {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int chapterId;
    public int levelNumber;
    public boolean completed;
    public int score;

    public Level(int chapterId, int levelNumber, boolean completed, int score) {
        this.chapterId = chapterId;
        this.levelNumber = levelNumber;
        this.completed = completed;
        this.score = score;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }
    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Level level = (Level) obj;

        return id == level.id &&
                chapterId == level.chapterId &&
                levelNumber == level.levelNumber &&
                completed == level.completed &&
                score == level.score;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + chapterId;
        result = 31 * result + levelNumber;
        result = 31 * result + (completed ? 1 : 0);
        result = 31 * result + score;
        return result;
    }
}