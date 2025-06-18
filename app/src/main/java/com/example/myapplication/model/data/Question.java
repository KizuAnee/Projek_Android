package com.example.myapplication.model.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "questions")
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int levelId;
    public String imageUrl;
    public String questionText;
    public String answer;

    public Question(int levelId, String imageUrl, String questionText, String answer) {
        this.levelId = levelId;
        this.imageUrl = imageUrl;
        this.questionText = questionText;
        this.answer = answer;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getLevelId() { return levelId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}