package com.example.myapplication.view.info;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Button btnBack = findViewById(R.id.btnBackToMain);
        btnBack.setOnClickListener(v -> finish()); // kembali ke MainActivity
    }
}
