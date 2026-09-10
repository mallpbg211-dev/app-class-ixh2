# 📱 IX-H Smart Classroom App (Aplikasi Kelas IX-H)

Aplikasi manajemen kelas modern berbasis Android dengan **Jetpack Compose** dan **Material Design 3**. Dirancang khusus untuk mempermudah kegiatan kelas, absensi presensi siswa, jadwal pelajaran & piket harian, rekapitulasi uang kas, serta buku catatan pelanggaran kedisiplinan.

---

## ✨ Fitur Utama

- 📋 **Presensi Siswa Harian**: Pencatatan kehadiran (Hadir, Sakit, Izin, Alpa) dengan filter cepat dan kalkulasi persentase kehadiran kelas secara real-time.
- 💰 **Buku Kas Kelas (Senin - Sabtu)**: Penarikan kas harian bebas hari Minggu, pencatatan pemasukan & pengeluaran, saldo otomatis, serta filter mutasi transaksi.
- 📅 **Jadwal Pelajaran & Piket (Senin - Sabtu)**: Menampilkan mata pelajaran per hari serta daftar giliran piket harian kelas IX-H.
- ⚖️ **Buku Catatan Kedisiplinan & Poin**: Rekap poin pelanggaran siswa transparan dan terstruktur.
- ⏱️ **Countdown Menuju Kelulusan**: Hitung mundur hari kelulusan kelas 9 SMP/MTs.
- ☁️ **Cloud Sync / Firebase Ready**: Integrasi `google-services.json` untuk sinkronisasi data antar pengurus kelas.

---

## 🛠️ Spesifikasi & Teknologi

- **Platform**: Android (Min SDK 26, Target SDK 35)
- **Bahasa**: Kotlin
- **UI Framework**: Jetpack Compose & Material Design 3
- **Build System**: Gradle (Kotlin DSL - `.gradle.kts`)
- **Arsitektur**: MVVM (Model-View-ViewModel) + StateFlow

---

## 🚀 Cara Build Menjadi File APK di GitHub Actions

1. Buka tab **Actions** di repositori GitHub ini.
2. Klik workflow **Build Android APK**.
3. Klik tombol **Run workflow** di sebelah kanan.
4. Setelah build selesai (bertanda centang hijau ✓), buka detail build dan download file APK di bagian **Artifacts**.
