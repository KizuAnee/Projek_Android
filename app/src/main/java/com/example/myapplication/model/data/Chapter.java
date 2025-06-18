package com.example.myapplication.model.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chapters")
public class Chapter {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String imageUrl;
    public boolean unlocked;

    public Chapter(String title, String imageUrl, boolean unlocked) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.unlocked = unlocked;
    }

    // Getters and Setters (or use public fields for simplicity with Room)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}