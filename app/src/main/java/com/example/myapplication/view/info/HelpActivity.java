package com.example.myapplication.view.info;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView tvHelpContent = findViewById(R.id.tvHelpContent);
        Button btnBack = findViewById(R.id.btnBackToMain);

        String helpText =
                "CARA BERMAIN Tebak Gambar: Komputer!\n" +
                "1.  Mulai Game: Dari Halaman Awal, klik 'Start Game'. Pilih Chapter, lalu pilih Level untuk memulai kuis.\n" +
                "2.  Jawab Soal: Setiap Level menampilkan gambar dan pertanyaan isian kosong.\n" +
                "    -   Ketik jawaban Anda langsung di kolom yang tersedia.\n" +
                "    -   Jawaban sangat fleksibel: tidak peduli huruf besar/kecil atau typo minor.\n" +
                "    -   Khusus soal 'Tautan', Anda bisa menjawab 'link' atau 'tautan'.\n" +
                "    -   Klik 'Cek Jawaban' untuk memeriksa.\n" +
                "Sistem Game:\n" +
                "3.  Nyawa: Anda punya 5 nyawa. Kehilangan nyawa jika salah jawab atau waktu habis (2 menit per soal).\n" +
                "    -   Nyawa akan terisi 1 per 6 menit (maksimal 5 nyawa).\n" +
                "    -   Nyawa habis = Game Over, kembali ke Halaman Awal.\n" +
                "4.  Skor: Jawab benar untuk dapat poin. Salah menjawab atau waktu habis akan mengurangi poin.\n" +
                "5.  Progres: Selesaikan semua Level di satu Chapter untuk membuka Chapter berikutnya dan melihat total skor Chapter tersebut.\n" +
                "6.  Tujuan Akhir: Selesaikan semua Chapter untuk melihat total skor Anda secara keseluruhan!\n";
        tvHelpContent.setText(helpText);

        btnBack.setOnClickListener(v -> finish());
    }
}