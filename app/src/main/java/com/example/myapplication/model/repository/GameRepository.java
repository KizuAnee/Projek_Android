package com.example.myapplication.model.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.myapplication.model.data.Chapter;
import com.example.myapplication.model.data.GameState;
import com.example.myapplication.model.data.Level;
import com.example.myapplication.model.data.Question;
import com.example.myapplication.model.database.AppDatabase;
import com.example.myapplication.model.database.ChapterDao;
import com.example.myapplication.model.database.GameStateDao;
import com.example.myapplication.model.database.LevelDao;
import com.example.myapplication.model.database.QuestionDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class GameRepository {
    private ChapterDao chapterDao;
    private LevelDao levelDao;
    private QuestionDao questionDao;
    private GameStateDao gameStateDao;
    private ExecutorService databaseWriteExecutor;

    private static final int MAX_LIVES = 5; // Define MAX_LIVES here

    public GameRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        chapterDao = db.chapterDao();
        levelDao = db.levelDao();
        questionDao = db.questionDao();
        gameStateDao = db.gameStateDao();
        databaseWriteExecutor = AppDatabase.databaseWriteExecutor;
    }

    // LiveData methods (for UI observation)
    public LiveData<List<Chapter>> getAllChapters() { return chapterDao.getAllChapters(); }
    public LiveData<List<Level>> getLevelsForChapter(int chapterId) { return levelDao.getLevelsForChapter(chapterId); }
    public LiveData<List<Question>> getQuestionsForLevel(int levelId) { return questionDao.getQuestionsForLevel(levelId); }
    public LiveData<GameState> getGameState() { return gameStateDao.getGameState(); }

    // Insertion methods (async)
    public void insertChapter(Chapter chapter) { databaseWriteExecutor.execute(() -> chapterDao.insert(chapter)); }
    public void insertAllChapters(List<Chapter> chapters) { databaseWriteExecutor.execute(() -> chapterDao.insertAll(chapters)); }
    public void insertLevel(Level level) { databaseWriteExecutor.execute(() -> levelDao.insert(level)); }
    public void insertAllLevels(List<Level> levels) { databaseWriteExecutor.execute(() -> levelDao.insertAll(levels)); }
    public void insertQuestion(Question question) { databaseWriteExecutor.execute(() -> questionDao.insert(question)); }
    public void insertAllQuestions(List<Question> questions) { databaseWriteExecutor.execute(() -> questionDao.insertAll(questions)); }
    public void insertGameState(GameState gameState) { databaseWriteExecutor.execute(() -> gameStateDao.insert(gameState)); }

    // Update methods (async)
    public void updateChapter(Chapter chapter) { databaseWriteExecutor.execute(() -> chapterDao.update(chapter)); }
    public void updateLevel(Level level) { databaseWriteExecutor.execute(() -> levelDao.update(level)); }
    public void updateGameState(GameState gameState) { databaseWriteExecutor.execute(() -> gameStateDao.update(gameState)); }


    // Synchronous fetch methods (for background thread use only, e.g., in GameViewModel's background tasks)
    public Chapter getChapterByIdSync(int chapterId) {
        Future<Chapter> future = databaseWriteExecutor.submit(() -> chapterDao.getChapterById(chapterId));
        try {
            return future.get(); // Blocks until result is available
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Level getLevelByIdSync(int levelId) {
        Future<Level> future = databaseWriteExecutor.submit((Callable<Level>) () -> levelDao.getLevelById(levelId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Chapter> getAllChaptersNonLiveSync() {
        Future<List<Chapter>> future = databaseWriteExecutor.submit(() -> chapterDao.getAllChaptersNonLive());
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Level> getLevelsForChapterNonLiveSync(int chapterId) {
        Future<List<Level>> future = databaseWriteExecutor.submit(() -> levelDao.getLevelsForChapterNonLive(chapterId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Question getQuestionByIdSync(int questionId) {
        Future<Question> future = databaseWriteExecutor.submit(() -> questionDao.getQuestionById(questionId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    // New: Synchronous method to get all chapter titles and their total scores
    public List<Map<String, Object>> getChapterScoresSummarySync() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        List<Chapter> allChapters = getAllChaptersNonLiveSync(); // Get all chapters synchronously

        if (allChapters != null) {
            for (Chapter chapter : allChapters) {
                // For each chapter, get its levels and sum their scores synchronously
                List<Level> levelsInChapter = getLevelsForChapterNonLiveSync(chapter.getId());
                int totalScoreForChapter = 0;
                if (levelsInChapter != null) {
                    for (Level level : levelsInChapter) {
                        totalScoreForChapter += level.getScore();
                    }
                }
                Map<String, Object> chapterData = new HashMap<>();
                chapterData.put("chapterTitle", chapter.getTitle());
                chapterData.put("totalScore", totalScoreForChapter);
                summaries.add(chapterData);
            }
        }
        return summaries;
    }


    // Initial data population (for demonstration)
    public void populateInitialData() {
        databaseWriteExecutor.execute(() -> {
            if (chapterDao.countChapters() == 0) {
                List<Chapter> chapters = Arrays.asList(
                        new Chapter("Chapter 1: Animals", "https://via.placeholder.com/150/0000FF/FFFFFF?text=Animals", true),
                        new Chapter("Chapter 2: Fruits", "https://via.placeholder.com/150/FF0000/FFFFFF?text=Fruits", false),
                        new Chapter("Chapter 3: Objects", "https://via.placeholder.com/150/00FF00/000000?text=Objects", false),
                        new Chapter("Chapter 4: Places", "https://via.placeholder.com/150/FFFF00/000000?text=Places", false),
                        new Chapter("Chapter 5: Vehicles", "https://via.placeholder.com/150/00FFFF/000000?text=Vehicles", false)
                );
                chapterDao.insertAll(chapters);

                // Assuming chapter IDs are 1-based after insertion.
                List<Level> levelsChapter1 = Arrays.asList(
                        new Level(1, 1, false, 0), new Level(1, 2, false, 0),
                        new Level(1, 3, false, 0), new Level(1, 4, false, 0),
                        new Level(1, 5, false, 0)
                );
                levelDao.insertAll(levelsChapter1);

                List<Level> levelsChapter2 = Arrays.asList(
                        new Level(2, 1, false, 0), new Level(2, 2, false, 0),
                        new Level(2, 3, false, 0), new Level(2, 4, false, 0),
                        new Level(2, 5, false, 0)
                );
                levelDao.insertAll(levelsChapter2);

                List<Question> questionsLevel1_1 = Arrays.asList(
                        new Question(1, "https://via.placeholder.com/150/0000FF/FFFFFF?text=Cat", "kucing"),
                        new Question(1, "https://via.placeholder.com/150/FF0000/FFFFFF?text=Dog", "anjing")
                );
                questionDao.insertAll(questionsLevel1_1);

                List<Question> questionsLevel1_2 = Arrays.asList(
                        new Question(2, "https://via.placeholder.com/150/00FF00/000000?text=Apple", "apel"),
                        new Question(2, "https://via.placeholder.com/150/FFFF00/000000?text=Banana", "pisang")
                );
                questionDao.insertAll(questionsLevel1_2);

                // Initial GameState
                gameStateDao.insert(new GameState(MAX_LIVES, System.currentTimeMillis()));
            }
        });
    }
}