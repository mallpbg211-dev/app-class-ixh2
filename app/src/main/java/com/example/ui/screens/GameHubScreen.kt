package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MiniGameInfo
import com.example.ui.ClassViewModel
import com.example.ui.screens.games.ActiveGameContainer
import com.example.ui.theme.*

@Composable
fun GameHubScreen(
    viewModel: ClassViewModel,
    modifier: Modifier = Modifier
) {
    val activeGameId by viewModel.activeGameId.collectAsState()
    val playerStats by viewModel.playerStats.collectAsState()
    val highScores by viewModel.highScores.collectAsState()
    val gamesList = viewModel.gamesList

    var showLeaderboardDialog by remember { mutableStateOf(false) }

    if (activeGameId != null) {
        ActiveGameContainer(
            gameId = activeGameId!!,
            viewModel = viewModel,
            onBack = { viewModel.launchGame(null) }
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 16.dp)
                .testTag("game_hub_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Player Profile Header (Cinematic Mystery Theme)
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        PurpleDark,
                                        Navy900,
                                        Color(0xFF1E1B4B)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Brush.radialGradient(listOf(PurpleMystery, PrimaryBlueDark))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🕵️", fontSize = 28.sp)
                                    }

                                    Column {
                                        Surface(
                                            color = PurpleMystery.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = playerStats.detectiveRank,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PurpleMystery
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = playerStats.playerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showLeaderboardDialog = true }
                                ) {
                                    Icon(
                                        Icons.Default.Leaderboard,
                                        contentDescription = "Leaderboard",
                                        tint = AccentGoldLight,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GameStatItem(
                                    label = "Total Poin",
                                    value = "${playerStats.totalScore} XP",
                                    color = AccentGoldLight,
                                    modifier = Modifier.weight(1f)
                                )
                                GameStatItem(
                                    label = "Selesai Main",
                                    value = "${playerStats.gamesPlayed} x",
                                    color = PrimaryBlueLight,
                                    modifier = Modifier.weight(1f)
                                )
                                GameStatItem(
                                    label = "Tantangan",
                                    value = "Aktif",
                                    color = EmeraldGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Section Title
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "8 Dark School Mini-Games",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Asah logika, memori, & refleks ala misteri IX-H",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextSecondary
                        )
                    }

                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Offline Ready",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlueLight
                        )
                    }
                }
            }

            // 8 Game Cards
            items(gamesList, key = { it.id }) { game ->
                val currentHighScore = highScores[game.id] ?: 0
                MiniGameCard(
                    game = game,
                    highScore = currentHighScore,
                    onPlay = { viewModel.launchGame(game.id) }
                )
            }
        }
    }

    // Leaderboard & Achievement Dialog
    if (showLeaderboardDialog) {
        AlertDialog(
            onDismissRequest = { showLeaderboardDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AccentGold)
                    Text("Papan Peringkat IX-H")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Skor Tertinggi Game Hub Kelas IX-H:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    gamesList.forEach { game ->
                        val hs = highScores[game.id] ?: 0
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = game.icon, fontSize = 20.sp)
                                    Text(
                                        text = game.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = if (game.id == "reflex") "$hs ms" else "$hs Poin",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLeaderboardDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun GameStatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = DarkTextSecondary)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun MiniGameCard(
    game: MiniGameInfo,
    highScore: Int,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("game_card_${game.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = game.icon, fontSize = 22.sp)
                }

                Surface(
                    color = AccentGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (game.id == "reflex") "$highScore ms" else "$highScore pts",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGoldLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = game.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = game.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(text = "Mainkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
