# Game Tebak Gambar Informatika🧠💻🕹️

![Aplikasi](iconAPK.png)

[![Android API Level](https://img.shields.io/badge/API%20Level-24%2B-brightgreen.svg)](https://developer.android.com/about/dashboards)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📝 Deskripsi Proyek

**Guess The Image Game: Komputer** adalah aplikasi kuis edukatif yang interaktif dan menyenangkan, dirancang untuk menguji serta meningkatkan pengetahuan pengguna tentang perangkat keras komputer, perangkat lunak, dan istilah-istilah terkait jaringan. Aplikasi ini sangat cocok untuk pelajar, mahasiswa, atau siapa saja yang ingin belajar dunia teknologi dengan cara yang seru dan menantang!

Aplikasi dibangun dengan arsitektur **Model-View-Controller (MVC)** yang bersih dan terstruktur, menggunakan **Room Persistence Library** untuk penyimpanan data lokal yang efisien.

## ✨ Fitur Utama

* **Navigasi Intuitif:** Halaman awal (Start, Help, About) diikuti dengan pemilihan Chapter dan Level.
* **Progres Terkunci:** Chapter dan Level selanjutnya terkunci, akan terbuka secara dinamis setelah Chapter/Level sebelumnya diselesaikan.
* **Permainan Tebak Gambar:** Setiap Level menyajikan gambar dan pertanyaan isian kosong.
    * **Input Fleksibel:** Pengguna dapat mengetik jawaban langsung ke garis kosong. Aplikasi toleran terhadap huruf besar/kecil dan typo minor.
    * **Jawaban Sinonim:** Khusus untuk beberapa soal, jawaban sinonim (misalnya "link" atau "tautan") diterima.
* **Sistem Timer:** Waktu 2 menit per soal untuk menambah tantangan.
* **Sistem Nyawa:** 5 nyawa awal, berkurang saat salah jawab atau waktu habis. Nyawa terisi ulang otomatis. Game berakhir jika nyawa habis.
* **Sistem Skor Komprehensif:**
    * Poin diberikan berdasarkan ketepatan dan kecepatan jawaban.
    * Akumulasi skor per Level dan total skor per Chapter.
    * Ringkasan skor Chapter setelah Chapter selesai, dan ringkasan skor Global di akhir permainan.
* **Antarmuka Pengguna Modern:** Didesain dengan Material Design dan menggunakan `FlexboxLayout` untuk tampilan soal yang responsif dan rapi.
* **Penyimpanan Data Lokal:** Menggunakan Room Database untuk menyimpan progres game, skor, dan status kunci Chapter/Level.

## 🚀 Memulai Proyek

Ikuti langkah-langkah ini untuk menjalankan proyek di lingkungan pengembangan Anda.

### Prasyarat

* [Android Studio](https://developer.android.com/studio) (Versi terbaru disarankan)
* [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) (Versi 8 atau lebih tinggi)

### Instalasi

1.  **Clone Repositori:**
    ```bash
    git clone [https://github.com/KizuAnee/Projek_Android.git](https://github.com/KizuAnee/Projek_Android)
    ```

2.  **Buka di Android Studio:**
    * Buka Android Studio.
    * Pilih `File > Open` dan navigasikan ke direktori `Projek_Android` yang baru saja Anda clone.

3.  **Build dan Jalankan Aplikasi:**
    * Setelah Android Studio selesai meng-index proyek, klik tombol `Run` (ikon segitiga hijau) di toolbar. Pilih emulator atau perangkat fisik Anda.
    * **Penting (untuk pertama kali/setelah update data):** Setelah aplikasi terinstal di perangkat/emulator, **hapus data aplikasi secara manual** untuk memastikan database awal dimuat dengan benar:
        * Pergi ke **Settings (Pengaturan)** > **Apps (Aplikasi)** > Cari aplikasi Anda (**GuessImageGame**) > **Storage & cache (Penyimpanan & cache)** > **Clear Storage (Hapus Penyimpanan)** atau **Clear Data (Hapus Data)**.
        * Kemudian, jalankan kembali aplikasi dari Android Studio.

## 🎮 Cara Bermain

Berikut adalah panduan singkat untuk menggunakan aplikasi:

1.  **Mulai Game:** Dari Halaman Awal, klik 'Start Game'. Pilih Chapter, lalu pilih Level untuk memulai kuis.
2.  **Jawab Soal:** Setiap Level menampilkan gambar dan pertanyaan isian kosong.
    * Ketik jawaban Anda langsung di kolom yang tersedia.
    * Jawaban sangat fleksibel: tidak peduli huruf besar/kecil atau typo minor.
    * Khusus soal 'Tautan', Anda bisa menjawab 'link' atau 'tautan'.
    * Klik 'Cek Jawaban' untuk memeriksa.
3.  **Sistem Nyawa:** Anda punya 5 nyawa. Kehilangan nyawa jika salah jawab atau waktu habis (2 menit per soal). Nyawa terisi 1 per 6 menit (maksimal 5 nyawa). Nyawa habis = Game Over, kembali ke Halaman Awal.
4.  **Skor:** Jawab benar untuk dapat poin. Salah menjawab atau waktu habis akan mengurangi poin.
5.  **Progres:** Selesaikan semua Level di satu Chapter untuk membuka Chapter berikutnya dan melihat total skor Chapter tersebut.
6.  **Tujuan Akhir:** Selesaikan semua Chapter untuk melihat total skor Anda secara keseluruhan!

## 📂 Struktur Proyek

Proyek ini mengikuti arsitektur Model-View-Controller (MVC) dengan komponen inti:

* `com.example.myapplication.model`: Berisi data (Chapter, Level, Question), definisi database (Room), DAO (Data Access Objects), dan Repository yang mengelola operasi data.
* `com.example.myapplication.view`: Berisi semua Activity (UI), Adapter untuk RecyclerView, Custom View (LivesView), dan Dialog (ResultDialog).
* `com.example.myapplication.controller`: Berisi ViewModel yang bertindak sebagai jembatan antara Model dan View, mengelola logika UI dan data (GameViewModel, ChapterViewModel, LevelViewModel).

## 🛠️ Dependensi

Proyek ini menggunakan beberapa pustaka AndroidX dan pihak ketiga penting:

* **Room Persistence Library:** Untuk penyimpanan data SQLite lokal.
* **Android Lifecycle Components:** LiveData dan ViewModel untuk arsitektur UI yang *lifecycle-aware*.
* **Material Design:** Komponen UI dari Google.
* **Glide:** Untuk memuat dan menampilkan gambar secara efisien.
* **FlexboxLayout:** Untuk layout dinamis yang responsif dan bisa *wrapping*.

```gradle
dependencies {
    // ... standar AndroidX ...

    // Room components
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // implementation("androidx.room:room-rxjava2:2.6.1") // Jika Anda menggunakan RxJava

    // Lifecycle components (ViewModel, LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.1")

    // Image loading library (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // FlexboxLayout for flexible wrapping UI
    implementation("com.google.android.flexbox:flexbox:3.0.0")
}
```
🤝 Kontribusi
Kontribusi disambut baik! Jika Anda menemukan bug atau memiliki saran perbaikan, silakan buka issue atau kirim pull request.

📄 Lisensi
Proyek ini dilisensikan di bawah Lisensi MIT. Lihat file LICENSE untuk detail lebih lanjut.

# Projek ini dikerjakan oleh:
- Amanda Putri			      (2310131220007)
- Muhammad Farros Shofiy 		(2310131310005)
- Muhammad Syauqoni		      (2310131310007)
- Muhammad Rekha Maulidan	   (1910131210005)

PEMROGRAMAN PERANGKAT BERGERAK - PILKOM - 2025