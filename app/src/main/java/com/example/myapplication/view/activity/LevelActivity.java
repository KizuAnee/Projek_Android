package com.example.myapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.LevelViewModel; // Import new ViewModel
import com.example.myapplication.model.data.Level;
import com.example.myapplication.view.adapter.LevelAdapter;

public class LevelActivity extends AppCompatActivity {

    private LevelAdapter levelAdapter;
    private LevelViewModel levelViewModel; // Use LevelViewModel
    private int chapterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        chapterId = getIntent().getIntExtra("chapterId", -1);
        if (chapterId == -1) {
            finish();
            return;
        }

        levelViewModel = new ViewModelProvider(this).get(LevelViewModel.class); // Get LevelViewModel

        RecyclerView rvLevels = findViewById(R.id.rvLevels);
        levelAdapter = new LevelAdapter(level -> {
            // Level unlocked status is handled in LevelAdapter
            Intent intent = new Intent(LevelActivity.this, GameActivity.class);
            intent.putExtra("levelId", level.getId());
            startActivity(intent);
        }, levelViewModel, this); // Pass ViewModel and LifecycleOwner to adapter

        rvLevels.setLayoutManager(new LinearLayoutManager(this));
        rvLevels.setAdapter(levelAdapter);

        levelViewModel.getLevelsForChapter(chapterId).observe(this, levels -> {
            if (levels != null) {
                levelAdapter.submitList(levels);
            }
        });
    }
}