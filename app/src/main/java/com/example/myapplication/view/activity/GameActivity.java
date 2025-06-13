package com.example.myapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.controller.GameViewModel;
import com.example.myapplication.controller.ChapterViewModel;
import com.example.myapplication.controller.LevelViewModel;
import com.example.myapplication.model.data.Chapter;
import com.example.myapplication.model.data.Level;
import com.example.myapplication.model.data.Question;
import com.example.myapplication.model.database.AppDatabase;
import com.example.myapplication.view.customview.LivesView;
import com.example.myapplication.view.dialog.ResultDialog;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import android.util.Log; // <--- Import Log

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity"; // <--- Tag untuk Logcat

    private TextView tvQuestionNumber, tvTimer;
    private ImageView ivQuestionImage;
    private FlexboxLayout llQuestionContainer;
    private EditText etDynamicAnswer;
    private Button btnCheckAnswer;
    private LivesView livesView;

    private GameViewModel gameViewModel;
    private LevelViewModel levelViewModel;
    private ChapterViewModel chapterViewModel;
    private int currentLevelId;
    private int currentPlayingChapterId;

    private static final String BLANK_MARKER = "[BLANK]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // --- Initialize UI Elements ---
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvTimer = findViewById(R.id.tvTimer);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        llQuestionContainer = findViewById(R.id.llQuestionContainer);
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
        gameViewModel.loadQuestionsForLevel(currentLevelId);

        // NEW: Observe current question number for display (fixes "Question 0" issue)
        gameViewModel.getCurrentQuestionNumberForDisplay().observe(this, questionNumber -> {
            Log.d(TAG, "Observer _currentQuestionNumberForDisplay fired. questionNumber: " + questionNumber);
            tvQuestionNumber.setText(String.format(Locale.getDefault(), "Question %d", questionNumber));
        });

        // --- Observe current question LiveData ---
        gameViewModel.getCurrentQuestion().observe(this, question -> {
            Log.d(TAG, "Observer _currentQuestion fired. Question: " + (question != null ? question.getQuestionText() : "null"));
            if (question != null) {
                Glide.with(GameActivity.this)
                        .load(question.imageUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(ivQuestionImage);
                displayQuestionWithBlankInput(question.getQuestionText());
            } else {
                Log.d(TAG, "Question is null, showing Level Completion Dialog.");
                showLevelCompletionDialog();
            }
        });

        // ... (Observe timer LiveData and lives LiveData - remains the same) ...
        gameViewModel.getTimerMillisRemaining().observe(this, millis -> {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(minutes);
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

            if (millis <= 0 && gameViewModel.getCurrentQuestion().getValue() != null) {
                Toast.makeText(this, "Time's up! Life lost.", Toast.LENGTH_SHORT).show();
            }
        });

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
            String userAnswer = "";
            if (etDynamicAnswer != null) {
                userAnswer = etDynamicAnswer.getText().toString();
            }

            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isCorrect = gameViewModel.checkAnswer(userAnswer);

            if (isCorrect) {
                Log.d(TAG, "Answer Correct. Calling loadNextQuestion().");
                new ResultDialog("Correct!", "Next Question", () -> gameViewModel.loadNextQuestion())
                        .show(getSupportFragmentManager(), "ResultDialog");
            } else {
                Log.d(TAG, "Answer Incorrect. Resetting input.");
                new ResultDialog("Incorrect! Try again.", "Try Again", () -> {
                    if (etDynamicAnswer != null) etDynamicAnswer.setText("");
                })
                        .show(getSupportFragmentManager(), "ResultDialog");
            }
        });
    }

    /**
     * Dynamically displays the question text, replacing [BLANK] with an EditText.
     * @param questionText The full question text from the database containing [BLANK].
     */
    private void displayQuestionWithBlankInput(String questionText) {
        llQuestionContainer.removeAllViews(); // Clear previous question elements

        String[] parts = questionText.split(BLANK_MARKER, -1);

        if (parts.length > 0 && !parts[0].isEmpty()) {
            TextView tvPrefix = createTextView(parts[0]);
            llQuestionContainer.addView(tvPrefix);
        }

        etDynamicAnswer = createEditTextForBlank();
        llQuestionContainer.addView(etDynamicAnswer);

        if (parts.length > 1 && !parts[1].isEmpty()) {
            TextView tvSuffix = createTextView(parts[1]);
            llQuestionContainer.addView(tvSuffix);
        }
    }

    /**
     * Helper method to create a TextView for question parts.
     */
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setText(text);
        textView.setTextSize(22);
        textView.setTextColor(ContextCompat.getColor(this, R.color.black));
        return textView;
    }

    /**
     * Helper method to create an EditText for the blank input.
     */
    private EditText createEditTextForBlank() {
        EditText editText = new EditText(this);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                (int) (8 * getResources().getDisplayMetrics().density), // left margin
                0,
                (int) (8 * getResources().getDisplayMetrics().density), // right margin
                0);
        editText.setLayoutParams(params);
        editText.setBackgroundResource(R.drawable.underline_edit_text);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setGravity(Gravity.CENTER);
        editText.setTextSize(22);
        editText.setTextColor(ContextCompat.getColor(this, R.color.black));
        editText.setMinWidth((int) (80 * getResources().getDisplayMetrics().density));
        editText.setMaxLines(1);

        return editText;
    }

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

    private void navigateToLevelOrChapterList(int finalScore) {
        new ResultDialog("Level Completed!\nScore: " + finalScore, "Continue", () -> {
            Intent intent = new Intent(GameActivity.this, LevelActivity.class);
            intent.putExtra("chapterId", currentPlayingChapterId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }).show(getSupportFragmentManager(), "LevelCompletionDialog");
    }

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