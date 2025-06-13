package com.example.myapplication.controller;

import android.app.Application;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.example.myapplication.model.data.Chapter;
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
import android.util.Log;

public class GameViewModel extends AndroidViewModel {

    private static final String TAG = "GameViewModel";

    private GameRepository repository;

    public void populateInitialData() {
        repository.populateInitialData();
    }

    private MutableLiveData<Question> _currentQuestion = new MutableLiveData<>();
    public LiveData<Question> getCurrentQuestion() {
        return _currentQuestion;
    }

    private LiveData<List<Question>> _questionsForLevel;
    private int _internalQuestionListIndex = 0;
    private List<Question> cachedQuestions;

    private MutableLiveData<Integer> _currentQuestionNumberForDisplay = new MutableLiveData<>();
    public LiveData<Integer> getCurrentQuestionNumberForDisplay() {
        return _currentQuestionNumberForDisplay;
    }

    private MutableLiveData<Long> _timerMillisRemaining = new MutableLiveData<>();
    public LiveData<Long> getTimerMillisRemaining() {
        return _timerMillisRemaining;
    }

    private CountDownTimer gameTimer;
    private static final long GAME_TIMER_DURATION_MILLIS = 2 * 60 * 1000;

    private MutableLiveData<Integer> _currentScore = new MutableLiveData<>();
    public LiveData<Integer> getCurrentScore() {
        return _currentScore;
    }

    private int levelScore = 0;
    private int questionsAnsweredCorrectly = 0;
    private int questionsAnsweredIncorrectly = 0;
    private boolean timerExceededOnCurrentQuestion = false;

    private LiveData<GameState> _gameState;
    private MutableLiveData<Integer> _lives = new MutableLiveData<>();
    public LiveData<Integer> getLives() {
        return _lives;
    }
    private static final long MAX_LIVES = 5;
    private static final long LIFE_REFILL_INTERVAL_MILLIS = 6 * 60 * 1000;

    private int currentPlayingLevelId;
    private int currentPlayingChapterId;

    private int loadedLevelId = -1;
    private Observer<List<Question>> _questionsListObserver;

    public GameViewModel(@NonNull Application application) {
        super(application);
        repository = new GameRepository(application);
        _currentScore.setValue(0);
        _currentQuestionNumberForDisplay.setValue(0);

        _gameState = repository.getGameState();
        _gameState.observeForever(gameState -> {
            if (gameState != null) {
                checkAndRefillLives(gameState);
                _lives.setValue(gameState.getCurrentLives());
            } else {
                repository.insertGameState(new GameState((int) MAX_LIVES, System.currentTimeMillis()));
            }
        });
    }

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
            current.setLastLifeRefillTime(System.currentTimeMillis());
            repository.updateGameState(current);
            _lives.setValue(current.getCurrentLives());
        }
    }

    public void loadQuestionsForLevel(int levelId) {
        Log.d(TAG, "loadQuestionsForLevel called for levelId: " + levelId + ", loadedLevelId: " + loadedLevelId);
        if (levelId != loadedLevelId) {
            Log.d(TAG, "NEW LEVEL DETECTED: " + levelId + ". Resetting state for new level.");
            if (_questionsForLevel != null && _questionsListObserver != null) {
                _questionsForLevel.removeObserver(_questionsListObserver);
                Log.d(TAG, "Removed old _questionsForLevel observer.");
            }

            loadedLevelId = levelId;
            currentPlayingLevelId = levelId;

            _internalQuestionListIndex = 0; // Reset ke 0
            levelScore = 0;
            questionsAnsweredCorrectly = 0;
            questionsAnsweredIncorrectly = 0;
            timerExceededOnCurrentQuestion = false;

            _questionsForLevel = repository.getQuestionsForLevel(levelId);

            _questionsListObserver = questions -> {
                cachedQuestions = questions;
                Log.d(TAG, "_questionsListObserver fired. cachedQuestions size: " + (questions != null ? questions.size() : "null") + ", _internalQuestionListIndex (before loadNext): " + _internalQuestionListIndex);

                if (questions != null && !questions.isEmpty() && _internalQuestionListIndex == 0) {
                    Log.d(TAG, "Initial question list received for new level. Calling loadNextQuestion().");
                    loadNextQuestion();
                } else if (questions != null && questions.isEmpty()) {
                    Log.d(TAG, "Level has no questions. Setting _currentQuestion to null.");
                    _currentQuestion.setValue(null);
                } else if (_internalQuestionListIndex > 0) {
                    Log.d(TAG, "Observer re-fired, but already past initial load. _internalQuestionListIndex: " + _internalQuestionListIndex);
                }
            };
            _questionsForLevel.observeForever(_questionsListObserver);
        } else {
            Log.d(TAG, "loadQuestionsForLevel called for SAME levelId: " + levelId + ". State NOT reset.");
        }
    }

    public void loadNextQuestion() {
        Log.d(TAG, "loadNextQuestion called. BEFORE increment, _internalQuestionListIndex: " + _internalQuestionListIndex);
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        timerExceededOnCurrentQuestion = false;

        _internalQuestionListIndex++;
        Log.d(TAG, "loadNextQuestion - AFTER increment, _internalQuestionListIndex: " + _internalQuestionListIndex);

        if (cachedQuestions != null && (_internalQuestionListIndex - 1) < cachedQuestions.size()) {
            _currentQuestion.setValue(cachedQuestions.get(_internalQuestionListIndex - 1));
            _currentQuestionNumberForDisplay.setValue(_internalQuestionListIndex);
            Log.d(TAG, "loadNextQuestion - Setting _currentQuestion to Q at index " + (_internalQuestionListIndex - 1) + ", _currentQuestionNumberForDisplay to: " + _internalQuestionListIndex);
            startTimer(); // <--- OK SEKARANG karena startTimer() tidak private
        } else {
            _currentQuestion.setValue(null);
            Log.d(TAG, "loadNextQuestion - No more questions for this level. Setting _currentQuestion to null.");
        }
    }

    // UBAH DARI 'private' MENJADI 'protected'
    protected void startTimer() { // <--- PERBAIKAN DI SINI
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        _timerMillisRemaining.setValue(GAME_TIMER_DURATION_MILLIS);
        gameTimer = new CountDownTimer(GAME_TIMER_DURATION_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                _timerMillisRemaining.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                _timerMillisRemaining.setValue(0L);
                timerExceededOnCurrentQuestion = true;
                consumeLife();
                loadNextQuestion();
            }
        }.start();
    }

    public boolean checkAnswer(String userAnswer) {
        Question current = _currentQuestion.getValue();
        if (current == null) {
            return false;
        }

        String correctAnswer = current.getAnswer();
        boolean isCorrect = false;

        String normalizedUserAnswer = userAnswer.trim().toLowerCase(Locale.getDefault());
        String normalizedCorrectAnswer = correctAnswer.trim().toLowerCase(Locale.getDefault());

        if (current.getLevelId() == 1) {
            isCorrect = normalizedUserAnswer.equals("tautan (link)") ||
                    normalizedUserAnswer.equals("tautan") ||
                    normalizedUserAnswer.equals("link");
        } else {
            isCorrect = normalizedUserAnswer.equals(normalizedCorrectAnswer);
        }

        if (isCorrect) {
            levelScore += (timerExceededOnCurrentQuestion ? 50 : 100);
            questionsAnsweredCorrectly++;
        } else {
            levelScore -= 20;
            if (levelScore < 0) levelScore = 0;
            questionsAnsweredIncorrectly++;
            consumeLife();
        }
        _currentScore.setValue(levelScore);

        return isCorrect;
    }

    public int getFinalLevelScore() {
        return levelScore;
    }

    public int getCurrentPlayingLevelId() {
        return currentPlayingLevelId;
    }

    public void markLevelCompleted(int levelId, int score) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Level levelToUpdate = repository.getLevelByIdSync(levelId);
            if (levelToUpdate != null) {
                levelToUpdate.setCompleted(true);
                levelToUpdate.setScore(score);
                repository.updateLevel(levelToUpdate);
            }
        });
    }

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
                    List<Chapter> allChapters = repository.getAllChaptersNonLiveSync();
                    if (allChapters != null) {
                        for (int i = 0; i < allChapters.size(); i++) {
                            if (allChapters.get(i).getId() == chapterId) {
                                if (i + 1 < allChapters.size()) {
                                    Chapter nextChapter = allChapters.get(i + 1);
                                    if (!nextChapter.isUnlocked()) {
                                        nextChapter.setUnlocked(true);
                                        repository.updateChapter(nextChapter);
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

    public List<Map<String, Object>> getChapterScoresSummarySync() {
        List<Map<String, Object>> summaries = new ArrayList<>();
        List<Chapter> allChapters = repository.getAllChaptersNonLiveSync();

        if (allChapters != null) {
            for (Chapter chapter : allChapters) {
                List<Level> levelsInChapter = repository.getLevelsForChapterNonLiveSync(chapter.getId());
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
        if (_questionsForLevel != null && _questionsListObserver != null) {
            _questionsForLevel.removeObserver(_questionsListObserver);
        }
        if (_gameState != null) {
            _gameState.removeObserver(gameState -> { /* do nothing */ });
        }
    }
}