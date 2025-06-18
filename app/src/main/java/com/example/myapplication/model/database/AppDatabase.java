package com.example.myapplication.model.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.myapplication.model.data.Chapter;
import com.example.myapplication.model.data.GameState;
import com.example.myapplication.model.data.Level;
import com.example.myapplication.model.data.Question;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Chapter.class, Level.class, Question.class, GameState.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChapterDao chapterDao();
    public abstract LevelDao levelDao();
    public abstract QuestionDao questionDao();
    public abstract GameStateDao gameStateDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS); // For background DB writes

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "guess_image_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}