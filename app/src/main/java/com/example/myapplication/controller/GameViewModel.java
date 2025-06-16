package com.example.myapplication.controller;

import android.app.Application;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.myapplication.model.data.Chapter; // Needed for markLevelCompleted
import com.example.myapplication.model.data.GameState;
import com.example.myapplication.model.data.Level;
import com.example.myapplication.model.data.Question;
import com.example.myapplication.model.database.AppDatabase;
import com.example.myapplication.model.repository.GameRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors; // For fetching single items on background thread

public class GameViewModel extends AndroidViewModel {

    private GameRepository repository;

    // For Main Activity (Initial data)
    public void populateInitialData() {
        repository.populateInitialData();
    }

    // For Game Activity
    private MutableLiveData<Question> _currentQuestion = new MutableLiveData<>();
    public LiveData<Question> getCurrentQuestion() {
        return _currentQuestion;
    }

    private LiveData<List<Question>> _questionsForLevel;
    public int currentQuestionIndex = 0; // Made public for direct access in Activity
    private List<Question> cachedQuestions;

    private MutableLiveData<Long> _timerMillisRemaining = new MutableLiveData<>();
    public LiveData<Long> getTimerMillisRemaining() { return _timerMillisRemaining; }

    private CountDownTimer gameTimer;
    private static final long GAME_TIMER_DURATION_MILLIS = 2 * 60 * 1000; // 2 minutes

    private MutableLiveData<Integer> _currentScore = new MutableLiveData<>();
    public LiveData<Integer> getCurrentScore() { return _currentScore; }

    private int levelScore = 0; // Score for the current level
    private int questionsAnsweredCorrectly = 0;
    private int questionsAnsweredIncorrectly = 0;
    private boolean timerExceededOnCurrentQuestion = false; // Flag for current question

    // Lives management
    private LiveData<GameState> _gameState;
    private MutableLiveData<Integer> _lives = new MutableLiveData<>();
    public LiveData<Integer> getLives() { return _lives; }
    private static final long MAX_LIVES = 5;
    private static final long LIFE_REFILL_INTERVAL_MILLIS = 6 * 60 * 1000; // 6 minutes

    // Store the ID of the level currently being played
    private int currentPlayingLevelId;
    private int currentPlayingChapterId;
    public GameViewModel(@NonNull Application application) {
        super(application);
        repository = new GameRepository(application);
        _currentScore.setValue(0);

        _gameState = repository.getGameState();
        _gameState.observeForever(gameState -> {
            if (gameState != null) {
                checkAndRefillLives(gameState);
                _lives.setValue(gameState.getCurrentLives());
            } else {
                repository.insertGameState(new GameState((int)MAX_LIVES, System.currentTimeMillis()));
            }
        });
    }

    // New setter for currentPlayingChapterId
    public void setCurrentPlayingChapterId(int chapterId) {
        this.currentPlayingChapterId = chapterId;
    }

    private void checkAndRefillLives(GameState gameState) {
        long now = System.currentTimeMillis();
        long lastRefill = gameState.getLastLifeRefillTime();
        int currentLives = gameState.getCurrentLives();

        if (currentLives < MAX_LIVES) {
            long timePassed = now - lastRefill;
            int livesToRefill = (int) (timePassed / LIFE_REFILL_INTERVAL_MILLIS);

            if (livesToRefill > 0) {
                int newLives = (int) Math.min(MAX_LIVES, currentLives + livesToRefill);
                gameState.setCurrentLives(newLives);
                // Update last refill time to the point where the last life was earned
                gameState.setLastLifeRefillTime(lastRefill + (livesToRefill * LIFE_REFILL_INTERVAL_MILLIS));
                repository.updateGameState(gameState);
                _lives.setValue(newLives);
            }
        }
    }

    public void consumeLife() {
        GameState current = _gameState.getValue();
        if (current != null && current.getCurrentLives() > 0) {
            current.setCurrentLives(current.getCurrentLives() - 1);
            // If lives drop, update last refill time to now for new refill cycle
            current.setLastLifeRefillTime(System.currentTimeMillis());
            repository.updateGameState(current);
            _lives.setValue(current.getCurrentLives());
        }
    }

    public void loadQuestionsForLevel(int levelId) {
        currentPlayingLevelId = levelId; // Store the level ID
        _questionsForLevel = repository.getQuestionsForLevel(levelId);
        _questionsForLevel.observeForever(questions -> {
            cachedQuestions = questions;
            currentQuestionIndex = 0;
            levelScore = 0; // Reset level score
            questionsAnsweredCorrectly = 0;
            questionsAnsweredIncorrectly = 0;
            timerExceededOnCurrentQuestion = false;
            loadNextQuestion();
        });
    }

    public void loadNextQuestion() {
        if (gameTimer != null) {
            gameTimer.cancel(); // Cancel previous timer
        }
        timerExceededOnCurrentQuestion = false; // Reset for new question

        if (cachedQuestions != null && currentQuestionIndex < cachedQuestions.size()) {
            _currentQuestion.setValue(cachedQuestions.get(currentQuestionIndex));
            currentQuestionIndex++;
            startTimer(); // Restart timer for new question
        } else {
            _currentQuestion.setValue(null); // No more questions (level completed)
        }
    }

    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        _timerMillisRemaining.setValue(GAME_TIMER_DURATION_MILLIS); // Reset timer display
        gameTimer = new CountDownTimer(GAME_TIMER_DURATION_MILLIS, 1000) { // Update every second
            @Override
            public void onTick(long millisUntilFinished) {
                _timerMillisRemaining.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                _timerMillisRemaining.setValue(0L);
                timerExceededOnCurrentQuestion = true;
                consumeLife(); // Lose a life if timer runs out
                // Automatically move to the next question
                loadNextQuestion();
            }
        }.start();
    }

    public boolean checkAnswer(String userAnswer) {
        String correctAnswer = _currentQuestion.getValue() != null ? _currentQuestion.getValue().getAnswer() : "";
        boolean isCorrect = correctAnswer.equalsIgnoreCase(userAnswer.trim());

        if (isCorrect) {
            levelScore += (timerExceededOnCurrentQuestion ? 50 : 100); // 100 if perfect, 50 if timer exceeded
            questionsAnsweredCorrectly++;
        } else {
            levelScore -= 20; // Deduct points for incorrect answer
            if (levelScore < 0) levelScore = 0; // Score can't go below zero
            questionsAnsweredIncorrectly++;
            consumeLife(); // Lose a life for incorrect answer
        }
        _currentScore.setValue(levelScore); // Update current score

        return isCorrect;
    }

    public int getFinalLevelScore() {
        return levelScore;
    }

    public int getCurrentPlayingLevelId() {
        return currentPlayingLevelId;
    }

    // Update level completion and score in DB
    public void markLevelCompleted(int levelId, int score) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Level levelToUpdate = repository.getLevelByIdSync(levelId); // Synchronous fetch on background thread
            if (levelToUpdate != null) {
                levelToUpdate.setCompleted(true);
                levelToUpdate.setScore(score);
                repository.updateLevel(levelToUpdate);
            }
        });
    }

    // Update chapter unlock status (now takes chapterId directly)
    public void updateChapterUnlockStatus(int chapterId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Level> levelsInChapter = repository.getLevelsForChapterNonLiveSync(chapterId);
            if (levelsInChapter != null && !levelsInChapter.isEmpty()) {
                boolean allLevelsCompleted = true;
                for (Level level : levelsInChapter) {
                    if (!level.isCompleted()) {
                        allLevelsCompleted = false;
                        break;
                    }
                }

                if (allLevelsCompleted) {
                    // Find the next chapter and unlock it
                    List<Chapter> allChapters = repository.getAllChaptersNonLiveSync();
                    if (allChapters != null) {
                        for (int i = 0; i < allChapters.size(); i++) {
                            if (allChapters.get(i).getId() == chapterId) {
                                if (i + 1 < allChapters.size()) {
                                    Chapter nextChapter = allChapters.get(i + 1);
                                    if (!nextChapter.isUnlocked()) {
                                        nextChapter.setUnlocked(true);
                                        repository.updateChapter(nextChapter);
                                        // A Toast or message here would need to be handled by the UI
                                        // E.g., send an event via LiveData
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    // New: Get a summary of all chapter scores for game completion screen (synchronous in background)
    public List<Map<String, Object>> getChapterScoresSummarySync() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        List<Chapter> allChapters = repository.getAllChaptersNonLiveSync(); // Sync fetch

        if (allChapters != null) {
            for (Chapter chapter : allChapters) {
                List<Level> levelsInChapter = repository.getLevelsForChapterNonLiveSync(chapter.getId()); // Sync fetch
                int totalScore = 0;
                if (levelsInChapter != null) {
                    for (Level level : levelsInChapter) {
                        totalScore += level.getScore();
                    }
                }
                Map<String, Object> chapterData = new HashMap<>();
                chapterData.put("chapterTitle", chapter.getTitle());
                chapterData.put("totalScore", totalScore);
                summaries.add(chapterData);
            }
        }
        return summaries;
    }



    @Override
    protected void onCleared() {
        super.onCleared();
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        if (_questionsForLevel != null) {
            _questionsForLevel.removeObserver(questions -> { /* do nothing */ });
        }
        if (_gameState != null) {
            _gameState.removeObserver(gameState -> { /* do nothing */ });
        }
    }
}