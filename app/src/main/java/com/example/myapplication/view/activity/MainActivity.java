package com.example.myapplication.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.controller.GameViewModel;

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

        findViewById(R.id.btnHelp).setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "Help information will go here.", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnAbout).setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "About this game will go here.", Toast.LENGTH_SHORT).show());
    }
}