package com.example.myapplication.view.info;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tvHelpContent = findViewById(R.id.tvAboutContent);
        Button btnBack = findViewById(R.id.btnBackToMain);

        String helpText =
                "Game Tebak Gambar: Komputer adalah aplikasi kuis seru yang menguji pengetahuanmu tentang perangkat keras, " +
                        "perangkat lunak, dan berbagai istilah dunia komputer. " +
                        "Cocok untuk pelajar, mahasiswa, atau siapa saja yang ingin belajar teknologi sambil bermain!" +
                        "\nDeveloping By Rakha, Manda, Isaw, Farros - PILKOM 2025";
        tvHelpContent.setText(helpText);
        btnBack.setOnClickListener(v -> finish()); // kembali ke MainActivity
    }
}
