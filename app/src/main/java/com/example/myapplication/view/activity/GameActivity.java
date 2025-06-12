package com.example.myapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.myapplication.controller.ChapterViewModel; // Import ChapterViewModel
import com.example.myapplication.controller.LevelViewModel; // Import LevelViewModel
import com.example.myapplication.model.data.Chapter; // Import Chapter data class
import com.example.myapplication.model.data.Level; // Import Level data class
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
    private EditText etAnswer;
    private Button btnCheckAnswer;
    private LivesView livesView;

    private GameViewModel gameViewModel;
    private LevelViewModel levelViewModel;   // Instance of LevelViewModel
    private ChapterViewModel chapterViewModel; // NEW: Instance of ChapterViewModel
    private int currentLevelId;
    private int currentPlayingChapterId; // To hold the ID of the current chapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // --- Initialize UI Elements ---
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvTimer = findViewById(R.id.tvTimer);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        livesView = findViewById(R.id.livesView);

        // --- Retrieve Intent Extras ---
        currentLevelId = getIntent().getIntExtra("levelId", -1);
        currentPlayingChapterId = getIntent().getIntExtra("chapterId", -1); // Get chapterId from Intent

        // --- Validate Intent Extras ---
        if (currentLevelId == -1 || currentPlayingChapterId == -1) {
            Toast.makeText(this, "Error: Invalid level or chapter ID.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if IDs are invalid
            return;
        }

        // --- Initialize ViewModels ---
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        levelViewModel = new ViewModelProvider(this).get(LevelViewModel.class);
        chapterViewModel = new ViewModelProvider(this).get(ChapterViewModel.class); // NEW: Initialize ChapterViewModel

        // Inform GameViewModel about the current chapter for unlock logic
        gameViewModel.setCurrentPlayingChapterId(currentPlayingChapterId);

        // --- Observe current question LiveData ---
        gameViewModel.getCurrentQuestion().observe(this, question -> {
            if (question != null) {
                // Update UI with current question details
                tvQuestionNumber.setText(String.format(Locale.getDefault(), "Question %d", gameViewModel.currentQuestionIndex));
                Glide.with(GameActivity.this)
                        .load(question.imageUrl) // Load image from URL
                        .placeholder(R.drawable.ic_placeholder_image) // Show placeholder while loading
                        .into(ivQuestionImage);
                etAnswer.setText(""); // Clear previous answer input
            } else {
                // If question is null, it means all questions for this level are answered
                showLevelCompletionDialog();
            }
        });

        // --- Observe timer LiveData ---
        gameViewModel.getTimerMillisRemaining().observe(this, millis -> {
            // Convert milliseconds to minutes and seconds for display
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(minutes);
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

            if (millis <= 0 && gameViewModel.getCurrentQuestion().getValue() != null) {
                // Timer ran out while a question was active.
                // Life loss and next question load are handled by GameViewModel's onFinish().
                Toast.makeText(this, "Time's up! Life lost.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Observe lives LiveData ---
        gameViewModel.getLives().observe(this, lives -> {
            livesView.setLives(lives); // Update custom LivesView
            if (lives <= 0) {
                // Game Over condition: no lives left
                Toast.makeText(this, "No lives left! Game Over.", Toast.LENGTH_LONG).show();
                // Navigate back to Main Activity and clear activity stack
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finish GameActivity
            }
        });

        // --- Set Check Answer Button Listener ---
        btnCheckAnswer.setOnClickListener(v -> {
            String userAnswer = etAnswer.getText().toString();
            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isCorrect = gameViewModel.checkAnswer(userAnswer); // Check answer and update score/lives

            if (isCorrect) {
                new ResultDialog("Correct!", "Next Question", () -> gameViewModel.loadNextQuestion())
                        .show(getSupportFragmentManager(), "ResultDialog");
            } else {
                // If incorrect, prompt to try again and reset input field
                new ResultDialog("Incorrect! Try again.", "Try Again", () -> etAnswer.setText(""))
                        .show(getSupportFragmentManager(), "ResultDialog");
            }
        });

        // --- Load initial questions for the level when activity starts ---
        gameViewModel.loadQuestionsForLevel(currentLevelId);
    }

    /**
     * Called when all questions in the current level have been answered.
     */
    private void showLevelCompletionDialog() {
        int finalScore = gameViewModel.getFinalLevelScore();
        // Mark the current level as completed in the database with its final score
        gameViewModel.markLevelCompleted(currentLevelId, finalScore);

        // Trigger the logic to potentially unlock the next chapter
        gameViewModel.updateChapterUnlockStatus(currentPlayingChapterId);

        // Determine if the entire game is completed (i.e., this is the last level of the last chapter)
        chapterViewModel.getAllChapters().observe(this, allChapters -> { // Use chapterViewModel
            if (allChapters != null && !allChapters.isEmpty()) { // Ensure not null and not empty
                // Find the last chapter's ID
                int lastChapterId = allChapters.get(allChapters.size() - 1).getId();

                // Check if current chapter is the last one AND if it's fully completed
                if (currentPlayingChapterId == lastChapterId) {
                    levelViewModel.getLevelsForChapter(currentPlayingChapterId).observe(this, levelsInCurrentChapter -> {
                        if (levelsInCurrentChapter != null && !levelsInCurrentChapter.isEmpty()) { // Ensure not null and not empty
                            boolean currentChapterFullyCompleted = true;
                            for (Level level : levelsInCurrentChapter) { // Traditional for-each loop
                                if (!level.isCompleted()) {
                                    currentChapterFullyCompleted = false;
                                    break;
                                }
                            }
                            if (currentChapterFullyCompleted) {
                                // All levels in the last chapter are completed, show full game summary
                                showGameCompletionSummary();
                            } else {
                                // Last chapter but not all levels done, navigate back to levels
                                navigateToLevelOrChapterList(finalScore);
                            }
                        } else {
                            // No levels found for current chapter (shouldn't happen if data is consistent)
                            navigateToLevelOrChapterList(finalScore);
                        }
                    });
                } else {
                    // Not the last chapter, just navigate back to levels
                    navigateToLevelOrChapterList(finalScore);
                }
            } else {
                // No chapters found (shouldn't happen if initial data populates)
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
            intent.putExtra("chapterId", currentPlayingChapterId); // Pass current chapter ID
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }).show(getSupportFragmentManager(), "LevelCompletionDialog");
    }

    /**
     * Displays the summary of scores across all chapters when the entire game is completed.
     */
    private void showGameCompletionSummary() {
        // This runs on a background thread to fetch all data synchronously
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Map<String, Object>> chapterSummaries = gameViewModel.getChapterScoresSummarySync(); // Get synchronous summary

            // Build the summary string on the background thread
            StringBuilder summary = new StringBuilder("Game Completed!\nYour Scores:\n");
            if (chapterSummaries != null && !chapterSummaries.isEmpty()) { // Ensure not null and not empty
                for (Map<String, Object> chapterData : chapterSummaries) { // Traditional for-each loop
                    String title = (String) chapterData.get("chapterTitle");
                    int score = (int) chapterData.get("totalScore");
                    // Append data only if retrieved successfully
                    if (title != null) {
                        summary.append(String.format(Locale.getDefault(), "%s: %d\n", title, score));
                    }
                }
            } else {
                summary.append("No score data available.");
            }

            // Post the result back to the UI thread to show the dialog
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
        // Remove observers to prevent memory leaks, especially for LiveData.observeForever
        gameViewModel.getCurrentQuestion().removeObservers(this);
        gameViewModel.getTimerMillisRemaining().removeObservers(this);
        gameViewModel.getLives().removeObservers(this);
        // LiveData observers created with 'this' (the activity) as LifecycleOwner
        // are automatically removed when the activity is destroyed.
    }
}