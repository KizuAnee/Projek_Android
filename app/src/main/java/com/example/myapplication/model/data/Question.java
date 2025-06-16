package com.example.myapplication.model.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "questions")
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int levelId;
    public String imageUrl;
    public String questionText; // <--- NEW FIELD FOR QUESTION TEXT
    public String answer;

    public Question(int levelId, String imageUrl, String questionText, String answer) { // <--- NEW CONSTRUCTOR
        this.levelId = levelId;
        this.imageUrl = imageUrl;
        this.questionText = questionText; // Initialize new field
        this.answer = answer;
    }


    // Getters and Setters (pastikan ditambahkan untuk questionText juga)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getLevelId() { return levelId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getQuestionText() { return questionText; } // <--- NEW GETTER
    public void setQuestionText(String questionText) { this.questionText = questionText; } // <--- NEW SETTER
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}