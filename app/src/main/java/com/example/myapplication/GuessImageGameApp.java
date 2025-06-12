package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.model.database.AppDatabase;
import com.example.myapplication.model.repository.GameRepository;

public class GuessImageGameApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the database and populate initial data (if not already done)
        AppDatabase.getDatabase(this); // This will create the database
        new GameRepository(this).populateInitialData(); // Populate data in a background thread
    }
}