package com.example.myapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.controller.GameViewModel;
import com.example.myapplication.controller.ChapterViewModel;
import com.example.myapplication.controller.LevelViewModel;
import com.example.myapplication.model.data.Chapter;
import com.example.myapplication.model.data.Level;
import com.example.myapplication.model.database.AppDatabase;
import com.example.myapplication.view.customview.LivesView;
import com.example.myapplication.view.dialog.ResultDialog;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity {

    private TextView tvQuestionNumber, tvTimer;
    private ImageView ivQuestionImage;
    private TextView tvQuestionText; // NEW: Declare TextView for question text
    private EditText etAnswer;
    private Button btnCheckAnswer;
    private LivesView livesView;

    private GameViewModel gameViewModel;
    private LevelViewModel levelViewModel;
    private ChapterViewModel chapterViewModel;
    private int currentLevelId;
    private int currentPlayingChapterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // --- Initialize UI Elements ---
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvTimer = findViewById(R.id.tvTimer);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        tvQuestionText = findViewById(R.id.tvQuestionText); // NEW: Initialize tvQuestionText
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        livesView = findViewById(R.id.livesView);


        // --- Retrieve Intent Extras ---
        currentLevelId = getIntent().getIntExtra("levelId", -1);
        currentPlayingChapterId = getIntent().getIntExtra("chapterId", -1);

        // --- Validate Intent Extras ---
        if (currentLevelId == -1 || currentPlayingChapterId == -1) {
            Toast.makeText(this, "Error: Invalid level or chapter ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Initialize ViewModels ---
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        levelViewModel = new ViewModelProvider(this).get(LevelViewModel.class);
        chapterViewModel = new ViewModelProvider(this).get(ChapterViewModel.class);

        // Inform GameViewModel about the current chapter for unlock logic
        gameViewModel.setCurrentPlayingChapterId(currentPlayingChapterId);

        // --- Load initial questions for the level first ---
        // Call loadQuestionsForLevel BEFORE setting up the observer that might react to null.
        // The observer is set up immediately, but the _currentQuestion MutableLiveData
        // will only be updated by loadQuestionsForLevel, preventing the initial null trigger.
        gameViewModel.loadQuestionsForLevel(currentLevelId);


        // --- Observe current question LiveData ---
        // This observer will now correctly react to the question being loaded (not null)
        // or to null only *after* all questions have been iterated.
        gameViewModel.getCurrentQuestion().observe(this, question -> {
            if (question != null) {
                tvQuestionNumber.setText(String.format(Locale.getDefault(), "Question %d", gameViewModel.currentQuestionIndex));

                int resId = getResources().getIdentifier(question.getImageUrl(), "drawable", getPackageName());
                Log.d("GameActivity", "Loading question image: " + question.getImageUrl() + " (resId: " + resId + ")");

                Glide.with(GameActivity.this)
                        .load(resId)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(ivQuestionImage);


                tvQuestionText.setText(question.getQuestionText());
                etAnswer.setText("");
            } else {
                showLevelCompletionDialog();
            }
        });





        // --- Observe timer LiveData ---
        gameViewModel.getTimerMillisRemaining().observe(this, millis -> {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(minutes);
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

            if (millis <= 0 && gameViewModel.getCurrentQuestion().getValue() != null) {
                Toast.makeText(this, "Time's up! Life lost.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Observe lives LiveData ---
        gameViewModel.getLives().observe(this, lives -> {
            livesView.setLives(lives);
            if (lives <= 0) {
                Toast.makeText(this, "No lives left! Game Over.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // --- Set Check Answer Button Listener ---
        btnCheckAnswer.setOnClickListener(v -> {
            String userAnswer = etAnswer.getText().toString();
            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isCorrect = gameViewModel.checkAnswer(userAnswer);

            if (isCorrect) {
                new ResultDialog("Correct!", "Next Question", () -> gameViewModel.loadNextQuestion())
                        .show(getSupportFragmentManager(), "ResultDialog");
            } else {
                new ResultDialog("Incorrect! Try again.", "Try Again", () -> etAnswer.setText(""))
                        .show(getSupportFragmentManager(), "ResultDialog");
            }
        });

        // --- Observe current question LiveData ---
        // Removed: gameViewModel.loadQuestionsForLevel(currentLevelId); from here
        // It's now called earlier, before the observer.
    }

    /**
     * Called when all questions in the current level have been answered.
     */
    private void showLevelCompletionDialog() {
        int finalScore = gameViewModel.getFinalLevelScore();
        gameViewModel.markLevelCompleted(currentLevelId, finalScore);

        gameViewModel.updateChapterUnlockStatus(currentPlayingChapterId);

        chapterViewModel.getAllChapters().observe(this, allChapters -> {
            if (allChapters != null && !allChapters.isEmpty()) {
                int lastChapterId = allChapters.get(allChapters.size() - 1).getId();

                if (currentPlayingChapterId == lastChapterId) {
                    levelViewModel.getLevelsForChapter(currentPlayingChapterId).observe(this, levelsInCurrentChapter -> {
                        if (levelsInCurrentChapter != null && !levelsInCurrentChapter.isEmpty()) {
                            boolean currentChapterFullyCompleted = true;
                            for (Level level : levelsInCurrentChapter) {
                                if (!level.isCompleted()) {
                                    currentChapterFullyCompleted = false;
                                    break;
                                }
                            }
                            if (currentChapterFullyCompleted) {
                                showGameCompletionSummary();
                            } else {
                                navigateToLevelOrChapterList(finalScore);
                            }
                        } else {
                            navigateToLevelOrChapterList(finalScore);
                        }
                    });
                } else {
                    navigateToLevelOrChapterList(finalScore);
                }
            } else {
                navigateToLevelOrChapterList(finalScore);
            }
        });
    }

    /**
     * Helper to navigate back to Level or Chapter list after level completion.
     */
    private void navigateToLevelOrChapterList(int finalScore) {
        new ResultDialog("Level Completed!\nScore: " + finalScore, "Continue", () -> {
            Intent intent = new Intent(GameActivity.this, LevelActivity.class);
            intent.putExtra("chapterId", currentPlayingChapterId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }).show(getSupportFragmentManager(), "LevelCompletionDialog");
    }

    /**
     * Displays the summary of scores across all chapters when the entire game is completed.
     */
    private void showGameCompletionSummary() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Map<String, Object>> chapterSummaries = gameViewModel.getChapterScoresSummarySync();

            StringBuilder summary = new StringBuilder("Game Completed!\nYour Scores:\n");
            if (chapterSummaries != null && !chapterSummaries.isEmpty()) {
                for (Map<String, Object> chapterData : chapterSummaries) {
                    String title = (String) chapterData.get("chapterTitle");
                    int score = (int) chapterData.get("totalScore");
                    if (title != null) {
                        summary.append(String.format(Locale.getDefault(), "%s: %d\n", title, score));
                    }
                }
            } else {
                summary.append("No score data available.");
            }

            runOnUiThread(() -> {
                new ResultDialog(summary.toString(), "Back to Main", () -> {
                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }).show(getSupportFragmentManager(), "GameCompletionSummary");
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameViewModel != null) {
            gameViewModel.getCurrentQuestion().removeObservers(this);
            gameViewModel.getTimerMillisRemaining().removeObservers(this);
            gameViewModel.getLives().removeObservers(this);
        }
    }
}
