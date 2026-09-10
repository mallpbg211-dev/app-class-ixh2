package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerStats(
    val playerName: String = "Siswa IX-H",
    val totalScore: Int = 340,
    val gamesPlayed: Int = 12,
    val detectiveRank: String = "Detektif Madya",
    val unlockedBadges: List<String> = listOf("First Win", "Math Whiz", "Night Owl")
)

data class MiniGameInfo(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val icon: String,
    val difficulty: String,
    val currentHighScore: Int = 0
)

class GameRepository {

    private val _playerStats = MutableStateFlow(PlayerStats())
    val playerStats: StateFlow<PlayerStats> = _playerStats.asStateFlow()

    private val _highScores = MutableStateFlow(
        mutableMapOf(
            "calc" to 85,
            "extreme" to 120,
            "grid" to 70,
            "dash" to 95,
            "word" to 60,
            "mystery" to 100,
            "reflex" to 185, // 185 ms
            "oracle" to 8
        )
    )
    val highScores: StateFlow<Map<String, Int>> = _highScores.asStateFlow()

    fun updateScore(gameId: String, newScore: Int) {
        val currentHigh = _highScores.value[gameId] ?: 0
        if (gameId == "reflex") {
            // For reflex, lower is better (milliseconds)
            if (currentHigh == 0 || newScore < currentHigh) {
                val copy = _highScores.value.toMutableMap()
                copy[gameId] = newScore
                _highScores.value = copy
            }
        } else {
            if (newScore > currentHigh) {
                val copy = _highScores.value.toMutableMap()
                copy[gameId] = newScore
                _highScores.value = copy
            }
        }

        val updatedStats = _playerStats.value.copy(
            totalScore = _playerStats.value.totalScore + (newScore / 5).coerceAtLeast(5),
            gamesPlayed = _playerStats.value.gamesPlayed + 1,
            detectiveRank = when {
                _playerStats.value.totalScore > 1000 -> "Grandmaster IX-H"
                _playerStats.value.totalScore > 500 -> "Detektif Utama"
                _playerStats.value.totalScore > 200 -> "Detektif Madya"
                else -> "Penyelidik Pemula"
            }
        )
        _playerStats.value = updatedStats
    }

    val gamesList = listOf(
        MiniGameInfo("calc", "Mental Calculation", "Hitung cepat kustom soal", "Latih otak menghitung operasi matematika dengan batas waktu & pilihan jumlah soal.", "🧮", "Menengah"),
        MiniGameInfo("extreme", "Extreme Addition", "Penjumlahan berantai tanpa henti", "Hitung deret angka acak yang bertambah terus dalam combo streak!", "🔥", "Sulit"),
        MiniGameInfo("grid", "Grid Memory", "Mengingat pola ubin misteri", "Ubin menyala di kegelapan kelas malam. Hafalkan posisi dan buka kembali.", "⬛", "Menengah"),
        MiniGameInfo("dash", "Math Dash", "Tebak Benar atau Salah kilat", "Hanya 3 detik per soal! Tentukan apakah rumus atau persamaan yang tampil benar atau salah.", "⚡", "Mudah-Sedang"),
        MiniGameInfo("word", "Fix Word Anagram", "Susun kata sandi IX-H", "Pecahkan anagram huruf acak yang berhubungan dengan istilah kelas & sekolah.", "🔤", "Menengah"),
        MiniGameInfo("mystery", "IX-H Mystery", "Teka-teki petunjuk ruang kelas", "Temukan kunci rahasia ruang kelas lewat serangkaian deduksi logika dan kode sandi.", "🕵️", "Menengah"),
        MiniGameInfo("reflex", "Ghost Reflex", "Uji kecepatan refleks sensorik", "Tunggu tanda bayangan berubah menjadi kilatan hijau, lalu sentuh layar secepat kilat!", "👻", "Sulit"),
        MiniGameInfo("oracle", "Oracle Sequence", "Ikuti urutan memori mistis", "Hafalkan urutan pola cahaya misterius yang kian bertambah panjang setiap babak.", "🔮", "Menengah")
    )
}
