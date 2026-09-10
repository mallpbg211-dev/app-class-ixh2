package com.example.ui.screens.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ClassViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ActiveGameContainer(
    gameId: String,
    viewModel: ClassViewModel,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Hub",
                        tint = Color.White
                    )
                }

                Text(
                    text = when (gameId) {
                        "calc" -> "Mental Calculation"
                        "extreme" -> "Extreme Addition"
                        "grid" -> "Grid Memory"
                        "dash" -> "Math Dash"
                        "word" -> "Fix Word Anagram"
                        "mystery" -> "IX-H Mystery"
                        "reflex" -> "Ghost Reflex"
                        "oracle" -> "Oracle Sequence"
                        else -> "Mini Game"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Implementation
            when (gameId) {
                "calc" -> MentalCalculationGame(onFinish = { score -> viewModel.finishGame("calc", score) })
                "extreme" -> ExtremeAdditionGame(onFinish = { score -> viewModel.finishGame("extreme", score) })
                "grid" -> GridMemoryGame(onFinish = { score -> viewModel.finishGame("grid", score) })
                "dash" -> MathDashGame(onFinish = { score -> viewModel.finishGame("dash", score) })
                "word" -> FixWordGame(onFinish = { score -> viewModel.finishGame("word", score) })
                "mystery" -> ClassMysteryGame(onFinish = { score -> viewModel.finishGame("mystery", score) })
                "reflex" -> GhostReflexGame(onFinish = { score -> viewModel.finishGame("reflex", score) })
                "oracle" -> OracleSequenceGame(onFinish = { score -> viewModel.finishGame("oracle", score) })
            }
        }
    }
}

// 1. Mental Calculation Game (Customizable questions: 5, 10, 15)
@Composable
fun MentalCalculationGame(onFinish: (Int) -> Unit) {
    var totalQuestions by remember { mutableIntStateOf(5) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var isStarted by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    var numA by remember { mutableIntStateOf(12) }
    var numB by remember { mutableIntStateOf(15) }
    var operator by remember { mutableStateOf("+") }
    var options by remember { mutableStateOf(listOf<Int>()) }
    var correctAnswer by remember { mutableIntStateOf(27) }

    fun generateQuestion() {
        val op = listOf("+", "-", "*").random()
        operator = op
        when (op) {
            "+" -> {
                numA = Random.nextInt(10, 80)
                numB = Random.nextInt(10, 80)
                correctAnswer = numA + numB
            }
            "-" -> {
                numA = Random.nextInt(30, 99)
                numB = Random.nextInt(5, numA)
                correctAnswer = numA - numB
            }
            "*" -> {
                numA = Random.nextInt(3, 12)
                numB = Random.nextInt(3, 12)
                correctAnswer = numA * numB
            }
        }
        val wrongAnswers = mutableSetOf<Int>()
        while (wrongAnswers.size < 3) {
            val delta = listOf(-10, -5, -2, -1, 1, 2, 5, 10).random()
            val candidate = correctAnswer + delta
            if (candidate != correctAnswer && candidate >= 0) {
                wrongAnswers.add(candidate)
            }
        }
        options = (wrongAnswers + correctAnswer).shuffled()
    }

    if (!isStarted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🧮", fontSize = 54.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Mental Calculation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Pilih jumlah soal untuk melatih kecepatan berhitung:",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(5, 10, 15).forEach { qCount ->
                    Button(
                        onClick = { totalQuestions = qCount },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (totalQuestions == qCount) PrimaryBlue else DarkSurfaceVariant
                        )
                    ) {
                        Text("$qCount Soal")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    score = 0
                    currentQuestionIndex = 0
                    generateQuestion()
                    isStarted = true
                    isGameOver = false
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Mulai Ujian Cepat", fontWeight = FontWeight.Bold)
            }
        }
    } else if (isGameOver) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🏆", fontSize = 60.sp)
            Text(
                text = "Selesai!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Skor Akhir: $score / ${totalQuestions * 20}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGoldLight
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { isStarted = false }
            ) {
                Text("Main Lagi")
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { (currentQuestionIndex + 1).toFloat() / totalQuestions },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryBlueLight,
                trackColor = DarkSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Soal ${currentQuestionIndex + 1}/$totalQuestions", color = DarkTextSecondary)
                Text(text = "Skor: $score", fontWeight = FontWeight.Bold, color = AccentGoldLight)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$numA $operator $numB = ?",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEach { opt ->
                            Button(
                                onClick = {
                                    if (opt == correctAnswer) {
                                        score += 20
                                    }
                                    if (currentQuestionIndex + 1 < totalQuestions) {
                                        currentQuestionIndex++
                                        generateQuestion()
                                    } else {
                                        isGameOver = true
                                        onFinish(score)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Text(text = opt.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Extreme Addition Game
@Composable
fun ExtremeAdditionGame(onFinish: (Int) -> Unit) {
    var runningSum by remember { mutableIntStateOf(0) }
    var nextDigit by remember { mutableIntStateOf(Random.nextInt(1, 10)) }
    var streak by remember { mutableIntStateOf(0) }
    var isStarted by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf(listOf<Int>()) }

    fun refreshOptions() {
        nextDigit = Random.nextInt(2, 12)
        val target = runningSum + nextDigit
        val wrong1 = target + Random.nextInt(1, 4)
        val wrong2 = (target - Random.nextInt(1, 4)).coerceAtLeast(0)
        options = listOf(target, wrong1, wrong2).distinct().shuffled()
    }

    if (!isStarted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🔥", fontSize = 54.sp)
            Text(text = "Extreme Addition", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = "Jumlahkan angka baru secara beruntun secepat mungkin!",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkTextSecondary,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {
                    runningSum = Random.nextInt(5, 15)
                    streak = 0
                    isStarted = true
                    isGameOver = false
                    refreshOptions()
                }
            ) {
                Text("Mulai Tantangan")
            }
        }
    } else if (isGameOver) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "💀", fontSize = 54.sp)
            Text(text = "Salah Hitung!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CoralRed)
            Text(text = "Streak Berhasil: $streak", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { isStarted = false }) { Text("Coba Lagi") }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Combo Streak: $streak 🔥", color = AccentGoldLight, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Total Saat Ini: $runningSum", fontSize = 18.sp, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+ $nextDigit = ?",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlueLight
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                options.forEach { opt ->
                    Button(
                        onClick = {
                            if (opt == runningSum + nextDigit) {
                                runningSum += nextDigit
                                streak++
                                refreshOptions()
                            } else {
                                isGameOver = true
                                onFinish(streak * 10)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text(text = opt.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. Grid Memory Game (Dark Mystery 3x3)
@Composable
fun GridMemoryGame(onFinish: (Int) -> Unit) {
    var round by remember { mutableIntStateOf(1) }
    var highlightedTiles by remember { mutableStateOf(setOf<Int>()) }
    var selectedTiles by remember { mutableStateOf(setOf<Int>()) }
    var isMemorizing by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    fun startRound() {
        selectedTiles = emptySet()
        val count = (2 + round).coerceAtMost(6)
        val target = mutableSetOf<Int>()
        while (target.size < count) {
            target.add(Random.nextInt(0, 9))
        }
        highlightedTiles = target
        isMemorizing = true
    }

    LaunchedEffect(isMemorizing) {
        if (isMemorizing) {
            delay(1600)
            isMemorizing = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Ronde $round - Hafalkan Ubin Misteri", color = Color.White, fontWeight = FontWeight.Bold)
        Text(
            text = if (isMemorizing) "Mengingat pola ubin..." else "Buka ubin yang tadi menyala!",
            color = if (isMemorizing) AccentGoldLight else PrimaryBlueLight
        )

        // 3x3 Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (row in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val isHighlighted = isMemorizing && highlightedTiles.contains(index)
                        val isSelected = selectedTiles.contains(index)
                        val isCorrect = !isMemorizing && isSelected && highlightedTiles.contains(index)

                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when {
                                        isHighlighted -> PurpleMystery
                                        isCorrect -> EmeraldGreen
                                        isSelected -> CoralRed
                                        else -> DarkSurfaceVariant
                                    }
                                )
                                .clickable(enabled = !isMemorizing && !isGameOver) {
                                    if (!selectedTiles.contains(index)) {
                                        val newSet = selectedTiles + index
                                        selectedTiles = newSet
                                        if (!highlightedTiles.contains(index)) {
                                            isGameOver = true
                                            onFinish((round - 1) * 20)
                                        } else if (newSet.containsAll(highlightedTiles)) {
                                            round++
                                            startRound()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isHighlighted || isCorrect) {
                                Text(text = "✨", fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }

        if (highlightedTiles.isEmpty()) {
            Button(onClick = { startRound() }) {
                Text("Mulai Permainan")
            }
        }

        if (isGameOver) {
            Text(text = "Permainan Berakhir!", color = CoralRed, fontWeight = FontWeight.Bold)
            Button(onClick = {
                round = 1
                isGameOver = false
                startRound()
            }) {
                Text("Ulangi")
            }
        }
    }
}

// 4. Math Dash Game (True / False 3 seconds)
@Composable
fun MathDashGame(onFinish: (Int) -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var expressionText by remember { mutableStateOf("14 + 18 = 32") }
    var isActuallyTrue by remember { mutableStateOf(true) }
    var isStarted by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(30) }

    fun nextQuestion() {
        val a = Random.nextInt(5, 25)
        val b = Random.nextInt(5, 25)
        val isTrue = Random.nextBoolean()
        isActuallyTrue = isTrue
        val displayedResult = if (isTrue) a + b else a + b + listOf(-2, -1, 1, 2).random()
        expressionText = "$a + $b = $displayedResult"
        timeLeft = 30
    }

    LaunchedEffect(isStarted, timeLeft, isGameOver) {
        if (isStarted && !isGameOver) {
            delay(100)
            if (timeLeft > 0) {
                timeLeft--
            } else {
                isGameOver = true
                onFinish(score)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isStarted) {
            Text(text = "⚡", fontSize = 54.sp)
            Text(text = "Math Dash", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "Tentukan Benar atau Salah sebelum waktu habis!", color = DarkTextSecondary, modifier = Modifier.padding(12.dp))
            Button(onClick = {
                score = 0
                isStarted = true
                isGameOver = false
                nextQuestion()
            }) {
                Text("Mulai Dash")
            }
        } else if (isGameOver) {
            Text(text = "Waktu Habis!", color = CoralRed, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "Skor Akhir: $score", color = AccentGoldLight, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { isStarted = false }) { Text("Main Lagi") }
        } else {
            LinearProgressIndicator(
                progress = { timeLeft / 30f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (timeLeft < 10) CoralRed else PrimaryBlueLight
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Skor: $score", color = AccentGoldLight, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(text = expressionText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (!isActuallyTrue) {
                            score += 10
                            nextQuestion()
                        } else {
                            isGameOver = true
                            onFinish(score)
                        }
                    },
                    modifier = Modifier.weight(1f).height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("SALAH", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (isActuallyTrue) {
                            score += 10
                            nextQuestion()
                        } else {
                            isGameOver = true
                            onFinish(score)
                        }
                    },
                    modifier = Modifier.weight(1f).height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("BENAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 5. Fix Word Anagram Game
@Composable
fun FixWordGame(onFinish: (Int) -> Unit) {
    val wordList = listOf(
        Pair("BENDAHARA", "Penanggung jawab uang kas kelas IX-H"),
        Pair("WALIKELAS", "Ibu guru pembimbing tercinta kita"),
        Pair("PELAJARAN", "Jadwal harian menuntut ilmu"),
        Pair("SEKOLAH", "Tempat berkumpulnya 32 siswa IX-H"),
        Pair("WISUDA", "Momen kelulusan yang kita tunggu bersama"),
        Pair("ABSENSI", "Pencatatan presensi harian siswa"),
        Pair("SOLIDARITAS", "Nilai kekompakan utama kelas IX-H")
    )

    var currentWordIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var userLetters by remember { mutableStateOf(listOf<Char>()) }
    val (targetWord, clue) = wordList[currentWordIndex]
    var scrambledLetters by remember { mutableStateOf(targetWord.toList().shuffled()) }

    LaunchedEffect(currentWordIndex) {
        scrambledLetters = wordList[currentWordIndex].first.toList().shuffled()
        userLetters = emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Kata ${currentWordIndex + 1}/${wordList.size}", color = DarkTextSecondary)
        Text(text = "Petunjuk: $clue", color = AccentGoldLight, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)

        // Assembled word slots
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                targetWord.forEachIndexed { idx, _ ->
                    val char = userLetters.getOrNull(idx)
                    Surface(
                        modifier = Modifier.padding(horizontal = 4.dp).size(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (char != null) PrimaryBlue else DarkSurfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = char?.toString() ?: "_", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Letter Bank
        Text(text = "Pilih Huruf:", color = DarkTextSecondary, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(horizontal = 8.dp)) {
            scrambledLetters.forEachIndexed { idx, ch ->
                Button(
                    onClick = {
                        userLetters = userLetters + ch
                        val newScrambled = scrambledLetters.toMutableList()
                        newScrambled.removeAt(idx)
                        scrambledLetters = newScrambled

                        val currentGuess = userLetters.joinToString("")
                        if (currentGuess == targetWord) {
                            score += 20
                            if (currentWordIndex + 1 < wordList.size) {
                                currentWordIndex++
                            } else {
                                onFinish(score)
                            }
                        }
                    },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Text(text = ch.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    scrambledLetters = targetWord.toList().shuffled()
                    userLetters = emptyList()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
            ) {
                Text("Reset Huruf")
            }
        }
    }
}

// 6. IX-H Mystery Room Riddle
@Composable
fun ClassMysteryGame(onFinish: (Int) -> Unit) {
    var stage by remember { mutableIntStateOf(1) }
    var statusText by remember { mutableStateOf("Misteri 1: Kunci Lemari Piala IX-H hilang! Ada 3 petunjuk di papan tulis.") }
    var isSolved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🕵️ Investigasi Ruang IX-H", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = when (stage) {
                        1 -> "Teka-Teki 1: Kunci disimpan di meja pengurus yang berawalan huruf 'B'. Di meja siapakah itu?"
                        2 -> "Teka-Teki 2: Jam berapa bel literasi pagi berbunyi setiap hari Selasa?"
                        else -> "Teka-Teki 3: Berapa jumlah seluruh kursi siswa di dalam ruang kelas IX-H?"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }

        val choices = when (stage) {
            1 -> listOf("Meja Bendahara", "Meja Sekretaris", "Meja Wali Kelas")
            2 -> listOf("07:00 WIB", "08:15 WIB", "09:30 WIB")
            else -> listOf("32 Kursi", "28 Kursi", "36 Kursi")
        }

        choices.forEach { choice ->
            Button(
                onClick = {
                    val correct = when (stage) {
                        1 -> choice == "Meja Bendahara"
                        2 -> choice == "07:00 WIB"
                        else -> choice == "32 Kursi"
                    }
                    if (correct) {
                        if (stage < 3) {
                            stage++
                        } else {
                            isSolved = true
                            onFinish(100)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
            ) {
                Text(text = choice, fontWeight = FontWeight.Bold)
            }
        }

        if (isSolved) {
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🎉 Luar Biasa! Semua teka-teki misteri kelas IX-H berhasil dipecahkan!",
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFF15803D),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 7. Ghost Reflex Game (Reaction timer in ms)
@Composable
fun GhostReflexGame(onFinish: (Int) -> Unit) {
    var state by remember { mutableStateOf("WAIT") } // WAIT, READY, GO, RESULT
    var reactionTimeMs by remember { mutableLongStateOf(0L) }
    var startTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(state) {
        if (state == "READY") {
            val wait = Random.nextLong(1500, 3500)
            delay(wait)
            startTime = System.currentTimeMillis()
            state = "GO"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(
                when (state) {
                    "READY" -> Color(0xFF7F1D1D)
                    "GO" -> EmeraldGreen
                    else -> DarkSurface
                }
            )
            .clickable {
                when (state) {
                    "WAIT" -> state = "READY"
                    "READY" -> state = "WAIT" // Too early!
                    "GO" -> {
                        val elapsed = System.currentTimeMillis() - startTime
                        reactionTimeMs = elapsed
                        state = "RESULT"
                        onFinish(elapsed.toInt())
                    }
                    "RESULT" -> state = "WAIT"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (state) {
                "WAIT" -> {
                    Text(text = "👻", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Ghost Reflex", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Sentuh layar untuk bersiap...", color = DarkTextSecondary)
                }
                "READY" -> {
                    Text(text = "🛑 TUNGGU SINYAL...", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(text = "Jangan sentuh sebelum warna hijau muncul!", color = Color.White.copy(alpha = 0.7f))
                }
                "GO" -> {
                    Text(text = "⚡ SENTUH SEKARANG! ⚡", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                "RESULT" -> {
                    Text(text = "$reactionTimeMs ms", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = AccentGoldLight)
                    Text(
                        text = if (reactionTimeMs < 250) "Gila! Refleks Kilat Detektif IX-H!" else "Bagus! Terus latih kecepatan refleksmu!",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Sentuh untuk main lagi", color = DarkTextSecondary)
                }
            }
        }
    }
}

// 8. Oracle Sequence Game (Simon Says)
@Composable
fun OracleSequenceGame(onFinish: (Int) -> Unit) {
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var userIndex by remember { mutableIntStateOf(0) }
    var activeRune by remember { mutableStateOf<Int?>(null) }
    var isShowingSequence by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    fun addStep() {
        sequence = sequence + Random.nextInt(0, 4)
        userIndex = 0
        isShowingSequence = true
    }

    LaunchedEffect(isShowingSequence) {
        if (isShowingSequence) {
            delay(500)
            for (item in sequence) {
                activeRune = item
                delay(600)
                activeRune = null
                delay(200)
            }
            isShowingSequence = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Oracle Sequence", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = "Ronde Berjalan: ${sequence.size}", color = AccentGoldLight, fontWeight = FontWeight.Bold)

        // 4 Runes
        val colors = listOf(PrimaryBlue, EmeraldGreen, AccentGold, PurpleMystery)
        val names = listOf("Azure", "Emerald", "Amber", "Amethyst")

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (r in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (c in 0 until 2) {
                        val runeId = r * 2 + c
                        val isActive = activeRune == runeId

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isActive) colors[runeId] else colors[runeId].copy(alpha = 0.35f))
                                .border(2.dp, if (isActive) Color.White else colors[runeId], RoundedCornerShape(20.dp))
                                .clickable(enabled = !isShowingSequence && sequence.isNotEmpty()) {
                                    if (sequence.isNotEmpty()) {
                                        if (sequence[userIndex] == runeId) {
                                            userIndex++
                                            if (userIndex == sequence.size) {
                                                score = sequence.size
                                                addStep()
                                            }
                                        } else {
                                            onFinish(score)
                                            sequence = emptyList()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = names[runeId], color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (sequence.isEmpty()) {
            Button(onClick = {
                score = 0
                addStep()
            }) {
                Text("Mulai Urutan Mistis")
            }
        }
    }
}
