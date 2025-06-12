package com.example.myapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.controller.ChapterViewModel; // Import new ViewModel
import com.example.myapplication.model.data.Chapter;
import com.example.myapplication.view.adapter.ChapterAdapter;

public class ChapterActivity extends AppCompatActivity {

    private ChapterAdapter chapterAdapter;
    private ChapterViewModel chapterViewModel; // Use ChapterViewModel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

        chapterViewModel = new ViewModelProvider(this).get(ChapterViewModel.class); // Get ChapterViewModel

        RecyclerView rvChapters = findViewById(R.id.rvChapters);
        chapterAdapter = new ChapterAdapter(chapter -> {
            if (chapter.isUnlocked()) {
                Intent intent = new Intent(ChapterActivity.this, LevelActivity.class);
                intent.putExtra("chapterId", chapter.getId());
                startActivity(intent);
            } else {
                android.widget.Toast.makeText(this, "This chapter is locked! Complete previous chapter to unlock.", android.widget.Toast.LENGTH_SHORT).show();
            }
        }, chapterViewModel, this); // Pass ViewModel and LifecycleOwner to adapter

        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setAdapter(chapterAdapter);

        chapterViewModel.getAllChapters().observe(this, chapters -> {
            if (chapters != null) {
                chapterAdapter.submitList(chapters);
            }
        });
    }
}