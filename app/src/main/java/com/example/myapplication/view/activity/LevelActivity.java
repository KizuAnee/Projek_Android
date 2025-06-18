package com.example.myapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.LevelViewModel;
import com.example.myapplication.model.data.Level;
import com.example.myapplication.view.adapter.LevelAdapter;

public class LevelActivity extends AppCompatActivity {

    private LevelAdapter levelAdapter;
    private LevelViewModel levelViewModel;
    private int chapterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        // Menerima chapterId dari Intent sebelumnya (dari ChapterActivity)
        chapterId = getIntent().getIntExtra("chapterId", -1);
        if (chapterId == -1) {
            // Jika chapterId tidak valid, tutup aktivitas
            finish();
            return;
        }

        levelViewModel = new ViewModelProvider(this).get(LevelViewModel.class);

        RecyclerView rvLevels = findViewById(R.id.rvLevels);
        levelAdapter = new LevelAdapter(level -> {
            // Ketika sebuah level diklik, kirimkan kedua ID ke GameActivity
            Intent intent = new Intent(LevelActivity.this, GameActivity.class);
            intent.putExtra("levelId", level.getId());
            intent.putExtra("chapterId", level.getChapterId()); // Mengirimkan chapterId dari objek Level
            startActivity(intent);
        }, levelViewModel, this); // Pass ViewModel dan LifecycleOwner ke adapter

        rvLevels.setLayoutManager(new LinearLayoutManager(this));
        rvLevels.setAdapter(levelAdapter);

        // Mengamati daftar level untuk chapter yang dipilih
        levelViewModel.getLevelsForChapter(chapterId).observe(this, levels -> {
            if (levels != null) {
                levelAdapter.submitList(levels);
            }
        });
    }
}