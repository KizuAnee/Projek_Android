package com.example.myapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.controller.GameViewModel;
import com.example.myapplication.view.info.AboutActivity;
import com.example.myapplication.view.info.HelpActivity;

public class MainActivity extends AppCompatActivity {

    private GameViewModel gameViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameViewModel.populateInitialData(); // Ensure initial data is populated

        findViewById(R.id.btnStartGame).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChapterActivity.class)));

        findViewById(R.id.btnAbout).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        });


        findViewById(R.id.btnHelp).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
        });

    }
}
