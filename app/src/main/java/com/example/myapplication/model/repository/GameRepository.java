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

    private static final int MAX_LIVES = 5;

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
    // IMPORTANT: These insert methods in DAO should return 'long' for auto-generated IDs
    // Example: @Insert(onConflict = OnConflictStrategy.REPLACE) long insert(Chapter chapter);
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


    // Synchronous fetch methods (for background thread use only)
    public Chapter getChapterByIdSync(int chapterId) {
        Future<Chapter> future = databaseWriteExecutor.submit(() -> chapterDao.getChapterById(chapterId));
        try {
            return future.get();
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

    // NEW METHOD: Get total score for a specific chapter synchronously
    public int getTotalScoreForChapterSync(int chapterId) {
        Future<Integer> future = databaseWriteExecutor.submit(() -> {
            List<Level> levelsInChapter = levelDao.getLevelsForChapterNonLive(chapterId); // Use non-live DAO method
            int totalScore = 0;
            if (levelsInChapter != null) {
                for (Level level : levelsInChapter) {
                    totalScore += level.getScore();
                }
            }
            return totalScore;
        });
        try {
            return future.get(); // Blocks until result is available
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0; // Return 0 on error
        }
    }

    // Synchronous method to get all chapter titles and their total scores
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
                // --- Chapter 1: HTML Dasar ---
                Chapter chapter1 = new Chapter("Chapter 1: HTML Dasar", "html2", true);
                long chapter1IdLong = chapterDao.insert(chapter1);
                int chapter1Id = (int) chapter1IdLong;

                Level level1_1 = new Level(chapter1Id, 1, false, 0);
                long level1_1IdLong = levelDao.insert(level1_1);
                int level1_1Id = (int) level1_1IdLong;
                // Perhatikan: Teks pertanyaan dengan ____
                questionDao.insert(new Question(level1_1Id, "taga",
                        "Perintah HTML <a> digunakan untuk membuat sebuah ____.", // <--- Updated with ____
                        "Tautan (link)"));

                Level level1_2 = new Level(chapter1Id, 2, false, 0);
                long level1_2IdLong = levelDao.insert(level1_2);
                int level1_2Id = (int) level1_2IdLong;
                questionDao.insert(new Question(level1_2Id, "tagimg",
                        "Tag <img> digunakan untuk menampilkan ____ di halaman web.", // <--- Updated with ____
                        "Gambar"));

                Level level1_3 = new Level(chapter1Id, 3, false, 0);
                long level1_3IdLong = levelDao.insert(level1_3);
                int level1_3Id = (int) level1_3IdLong;
                questionDao.insert(new Question(level1_3Id, "taghtml",
                        "Dokumen HTML selalu diawali dengan tag ____ dan diakhiri dengan tag penutupnya.", // <--- Updated with ____
                        "<html>"));


                // --- Chapter 2: Perangkat Keras Komputer ---
                Chapter chapter2 = new Chapter("Chapter 2: Perangkat Keras Komputer", "hardware", false);
                long chapter2IdLong = chapterDao.insert(chapter2);
                int chapter2Id = (int) chapter2IdLong;

                Level level2_1 = new Level(chapter2Id, 1, false, 0);
                long level2_1IdLong = levelDao.insert(level2_1);
                int level2_1Id = (int) level2_1IdLong;
                questionDao.insert(new Question(level2_1Id, "keyboard",
                        "Alat yang digunakan untuk mengetik huruf dan angka pada komputer disebut ____.", // <--- Updated with ____
                        "Keyboard"));

                Level level2_2 = new Level(chapter2Id, 2, false, 0);
                long level2_2IdLong = levelDao.insert(level2_2);
                int level2_2Id = (int) level2_2IdLong;
                questionDao.insert(new Question(level2_2Id, "ram",
                        "RAM merupakan singkatan dari ____.", // <--- Updated with ____
                        "Random Access Memory"));

                Level level2_3 = new Level(chapter2Id, 3, false, 0);
                long level2_3IdLong = levelDao.insert(level2_3);
                int level2_3Id = (int) level2_3IdLong;
                questionDao.insert(new Question(level2_3Id, "cpu",
                        "Komponen utama yang menjalankan proses dan perintah di komputer adalah ____.", // <--- Updated with ____
                        "CPU"));


                // --- Chapter 3: Jaringan Komputer ---
                Chapter chapter3 = new Chapter("Chapter 3: Jaringan Komputer", "internet", false);
                long chapter3IdLong = chapterDao.insert(chapter3);
                int chapter3Id = (int) chapter3IdLong;

                Level level3_1 = new Level(chapter3Id, 1, false, 0);
                long level3_1IdLong = levelDao.insert(level3_1);
                int level3_1Id = (int) level3_1IdLong;
                questionDao.insert(new Question(level3_1Id, "lan",
                        "Jenis jaringan yang mencakup area kecil seperti dalam satu gedung disebut ____", // <--- Updated with ____
                        "LAN"));

                Level level3_2 = new Level(chapter3Id, 2, false, 0);
                long level3_2IdLong = levelDao.insert(level3_2);
                int level3_2Id = (int) level3_2IdLong;
                questionDao.insert(new Question(level3_2Id, "router",
                        "Perangkat yang menghubungkan jaringan lokal ke internet disebut ____", // <--- Updated with ____
                        "Router"));

                Level level3_3 = new Level(chapter3Id, 3, false, 0);
                long level3_3IdLong = levelDao.insert(level3_3);
                int level3_3Id = (int) level3_3IdLong;
                questionDao.insert(new Question(level3_3Id, "wifi",
                        "Simbol ____ biasanya menunjukkan bahwa perangkat terhubung ke jaringan nirkabel.", // <--- Updated with ____
                        "Wi-Fi"));

                // Initial GameState
                gameStateDao.insert(new GameState(MAX_LIVES, System.currentTimeMillis()));
            }
        });
    }
}
