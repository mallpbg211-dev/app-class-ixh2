package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.ClassViewModel
import com.example.ui.theme.*

// 1. Interactive Seating Plan Dialog (16 Double Desks = 32 seats)
@Composable
fun SeatingPlanDialog(
    viewModel: ClassViewModel,
    onDismiss: () -> Unit
) {
    val seating by viewModel.seating.collectAsState()
    val students by viewModel.students.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var selectedSeatForSwap by remember { mutableStateOf<Pair<Int, Boolean>?>(null) } // deskIndex, isLeft

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Denah Tempat Duduk IX-H",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentRole == UserRole.ADMIN) "Pilih 2 kursi untuk menukar posisi duduk" else "Layout 16 Meja Ganda (32 Siswa)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Front of Classroom: Whiteboard & Teacher Desk
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📋 PAPAN TULIS & MEJA WALI KELAS (DEPAN)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4 Rows x 4 Columns of double desks
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val rows = seating.chunked(4) // 4 rows of 4 desks
                    items(rows) { rowDesks ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowDesks.forEach { desk ->
                                val leftStudent = students.find { it.id == desk.leftStudentId }
                                val rightStudent = students.find { it.id == desk.rightStudentId }

                                DoubleDeskCard(
                                    desk = desk,
                                    leftStudent = leftStudent,
                                    rightStudent = rightStudent,
                                    selectedSwap = selectedSeatForSwap,
                                    canSwap = currentRole == UserRole.ADMIN,
                                    modifier = Modifier.weight(1f),
                                    onSeatClick = { isLeft ->
                                        if (currentRole == UserRole.ADMIN) {
                                            if (selectedSeatForSwap == null) {
                                                selectedSeatForSwap = Pair(desk.deskIndex, isLeft)
                                            } else {
                                                val (firstDesk, firstIsLeft) = selectedSeatForSwap!!
                                                viewModel.swapSeats(firstDesk, firstIsLeft, desk.deskIndex, isLeft)
                                                selectedSeatForSwap = null
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚪 Pintu Masuk (Kiri Belakang)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Button(onClick = onDismiss) {
                        Text("Selesai")
                    }
                }
            }
        }
    }
}

@Composable
private fun DoubleDeskCard(
    desk: SeatPosition,
    leftStudent: Student?,
    rightStudent: Student?,
    selectedSwap: Pair<Int, Boolean>?,
    canSwap: Boolean,
    modifier: Modifier = Modifier,
    onSeatClick: (Boolean) -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                text = "Meja #${desk.deskIndex + 1}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val isLeftSelected = selectedSwap?.first == desk.deskIndex && selectedSwap.second
                val isRightSelected = selectedSwap?.first == desk.deskIndex && !selectedSwap.second

                SingleSeatButton(
                    student = leftStudent,
                    isSelected = isLeftSelected,
                    modifier = Modifier.weight(1f),
                    onClick = { onSeatClick(true) }
                )

                SingleSeatButton(
                    student = rightStudent,
                    isSelected = isRightSelected,
                    modifier = Modifier.weight(1f),
                    onClick = { onSeatClick(false) }
                )
            }
        }
    }
}

@Composable
private fun SingleSeatButton(
    student: Student?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, PrimaryBlue, RoundedCornerShape(6.dp)) else Modifier
            ),
        color = if (student != null) {
            if (student.gender == "L") Color(0xFFE0F2FE) else Color(0xFFFCE7F3)
        } else Color.LightGray.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (student != null) {
                Text(
                    text = "#${student.id}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (student.gender == "L") PrimaryBlueDark else Color(0xFF9D174D)
                )
                Text(
                    text = student.nickname,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(text = "Kosong", fontSize = 8.sp, color = TextSecondary)
            }
        }
    }
}

// 2. Gallery & Official News Dialog
@Composable
fun GalleryDialog(
    viewModel: ClassViewModel,
    onDismiss: () -> Unit
) {
    val gallery by viewModel.gallery.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var selectedFilter by remember { mutableStateOf("Semua") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = remember(gallery, selectedFilter) {
        if (selectedFilter == "Semua") gallery else gallery.filter { it.category == selectedFilter }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dokumentasi & Berita Acara",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Arsip kegiatan resmi keluarga besar IX-H",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Semua", "Dokumentasi Foto", "Berita Acara").forEach { cat ->
                            FilterChip(
                                selected = selectedFilter == cat,
                                onClick = { selectedFilter = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    if (currentRole == UserRole.ADMIN) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Tambah", tint = PrimaryBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (item.category == "Berita Acara") Color(0xFFFEF3C7) else Color(0xFFE0F2FE),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.category == "Berita Acara") Color(0xFFB45309) else PrimaryBlueDark
                                        )
                                    }

                                    Text(text = item.date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (item.officialNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = Color.White.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Catatan: ${item.officialNotes}",
                                            modifier = Modifier.padding(8.dp),
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("Dokumentasi Foto") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Arsip / Berita Acara") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Kegiatan / Rapat") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Catatan Hasil Keputusan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && desc.isNotBlank()) {
                        viewModel.addGalleryItem(title.trim(), desc.trim(), cat, note.trim())
                        showAddDialog = false
                    }
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }
}

// 3. Countdown Dialog (Exam & Graduation - Fully Editable)
@Composable
fun CountdownDialog(
    viewModel: ClassViewModel? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val countdownEvents by (viewModel?.countdownEvents ?: remember {
        MutableStateFlow(
            listOf(
                CountdownEvent(
                    id = "event_exam",
                    title = "Asesmen Akhir Sekolah (Ujian)",
                    targetDate = "2027-05-18",
                    category = "Ujian",
                    note = "Gladi dan Asesmen Akhir Sekolah Penentu Kelulusan."
                ),
                CountdownEvent(
                    id = "event_graduation",
                    title = "Wisuda & Pelepasan Kelas IX-H",
                    targetDate = "2027-06-25",
                    category = "Wisuda",
                    note = "Momen perpisahan akbar dan pelepasan siswa angkatan 2026/2027."
                )
            )
        )
    }).collectAsState(initial = emptyList())

    var editingEvent by remember { mutableStateOf<CountdownEvent?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<CountdownEvent?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hitung Mundur Angkatan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target ujian, wisuda, & agenda penting IX-H",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar: Add Event & Quick Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${countdownEvents.size} Target Agenda",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            isAddingNew = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Tambah Target", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of Countdown Cards
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (countdownEvents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = TextSecondary.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Belum ada agenda hitung mundur.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(countdownEvents, key = { it.id }) { item ->
                            val daysLeft = calculateCountdownDays(item.targetDate)
                            val formattedDate = formatCountdownDate(item.targetDate)

                            val (badgeColor, badgeIcon) = when (item.category) {
                                "Wisuda" -> Pair(AccentGold, Icons.Default.School)
                                "Acara" -> Pair(EmeraldGreen, Icons.Default.Celebration)
                                "Libur" -> Pair(CoralRed, Icons.Default.BeachAccess)
                                else -> Pair(PrimaryBlue, Icons.Default.BorderColor)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.08f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(badgeColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                badgeIcon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Target: $formattedDate",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }

                                        // Countdown Badge
                                        Surface(
                                            color = badgeColor,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val badgeText = when {
                                                    daysLeft > 0 -> "H-$daysLeft"
                                                    daysLeft == 0L -> "HARI INI!"
                                                    else -> "${-daysLeft} Hari"
                                                }
                                                val subText = when {
                                                    daysLeft > 0 -> "Hari Lagi"
                                                    daysLeft == 0L -> "Sukses! 🎉"
                                                    else -> "Telah Lewat"
                                                }
                                                Text(
                                                    text = badgeText,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = subText,
                                                    fontSize = 9.sp,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                            }
                                        }
                                    }

                                    if (item.note.isNotBlank()) {
                                        Text(
                                            text = "“${item.note}”",
                                            fontSize = 11.sp,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(start = 56.dp)
                                        )
                                    }

                                    // Action Buttons: Edit & Delete
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { editingEvent = item },
                                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Ubah Tanggal", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        TextButton(
                                            onClick = { eventToDelete = item },
                                            colors = ButtonDefaults.textButtonColors(contentColor = CoralRed),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Hapus", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quote at end of list
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "“Waktu tidak akan menunggu. Manfaatkan setiap detik belajar bersama keluarga besar IX-H agar kita bisa tersenyum bangga saat kelulusan nanti!”",
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup")
                }
            }
        }
    }

    // Modal Edit / Add Countdown
    if (editingEvent != null || isAddingNew) {
        val target = editingEvent ?: CountdownEvent(
            title = "",
            targetDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            category = "Ujian",
            note = ""
        )

        CountdownEditDialog(
            initialEvent = target,
            isNew = isAddingNew,
            onDismiss = {
                editingEvent = null
                isAddingNew = false
            },
            onSave = { savedEvent ->
                if (isAddingNew) {
                    viewModel?.addCountdownEvent(savedEvent)
                    Toast.makeText(context, "Target baru berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel?.updateCountdownEvent(savedEvent)
                    Toast.makeText(context, "Target hitung mundur berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                }

                // Trigger cloud sync if online
                if (viewModel != null) {
                    CloudSyncManager.instance.uploadAllToCloud(viewModel.repository) { _, _ -> }
                }

                editingEvent = null
                isAddingNew = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Hapus Target?") },
            text = { Text("Apakah Anda yakin ingin menghapus '${eventToDelete?.title}' dari daftar hitung mundur angkatan?") },
            confirmButton = {
                Button(
                    onClick = {
                        eventToDelete?.let {
                            viewModel?.deleteCountdownEvent(it.id)
                            if (viewModel != null) {
                                CloudSyncManager.instance.uploadAllToCloud(viewModel.repository) { _, _ -> }
                            }
                            Toast.makeText(context, "Target berhasil dihapus.", Toast.LENGTH_SHORT).show()
                        }
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { eventToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

// Dialog Form for Adding/Editing Countdown Event
@Composable
private fun CountdownEditDialog(
    initialEvent: CountdownEvent,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (CountdownEvent) -> Unit
) {
    var title by remember { mutableStateOf(initialEvent.title) }
    var category by remember { mutableStateOf(initialEvent.category) }
    var note by remember { mutableStateOf(initialEvent.note) }

    // Parse initial date components (yyyy-MM-dd)
    val parsedDateParts = remember(initialEvent.targetDate) {
        val parts = initialEvent.targetDate.split("-")
        if (parts.size == 3) {
            Triple(parts[0].toIntOrNull() ?: 2027, parts[1].toIntOrNull() ?: 5, parts[2].toIntOrNull() ?: 18)
        } else {
            Triple(2027, 5, 18)
        }
    }

    var selectedYear by remember { mutableStateOf(parsedDateParts.first) }
    var selectedMonth by remember { mutableStateOf(parsedDateParts.second) }
    var selectedDay by remember { mutableStateOf(parsedDateParts.third) }

    val monthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    // Format ISO string
    val currentIsoDate = String.format(java.util.Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
    val previewDaysLeft = calculateCountdownDays(currentIsoDate)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Tambah Target Baru" else "Edit Hitung Mundur",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Judul
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Target / Acara") },
                    placeholder = { Text("Misal: Ujian Sekolah, Wisuda, Camping...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Kategori Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Ujian", "Wisuda", "Acara", "Libur").forEach { cat ->
                            val isSelected = category == cat
                            val chipColor = when (cat) {
                                "Wisuda" -> AccentGold
                                "Acara" -> EmeraldGreen
                                "Libur" -> CoralRed
                                else -> PrimaryBlue
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Tanggal Selector (Day, Month, Year)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pilih Tanggal Target:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Day Selector
                        var dayExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { dayExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                            ) {
                                Text("Tgl $selectedDay", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            DropdownMenu(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false }
                            ) {
                                (1..31).forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text("$d") },
                                        onClick = {
                                            selectedDay = d
                                            dayExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Month Selector
                        var monthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.4f)) {
                            OutlinedButton(
                                onClick = { monthExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                            ) {
                                Text(monthNames[selectedMonth - 1], fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            DropdownMenu(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false }
                            ) {
                                monthNames.forEachIndexed { idx, mName ->
                                    DropdownMenuItem(
                                        text = { Text(mName) },
                                        onClick = {
                                            selectedMonth = idx + 1
                                            monthExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year Selector
                        var yearExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.1f)) {
                            OutlinedButton(
                                onClick = { yearExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                            ) {
                                Text("$selectedYear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            DropdownMenu(
                                expanded = yearExpanded,
                                onDismissRequest = { yearExpanded = false }
                            ) {
                                listOf(2026, 2027, 2028, 2029, 2030).forEach { y ->
                                    DropdownMenuItem(
                                        text = { Text("$y") },
                                        onClick = {
                                            selectedYear = y
                                            yearExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Preview Badge
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        Column {
                            Text(
                                text = "Preview: $selectedDay ${monthNames[selectedMonth - 1]} $selectedYear",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            val statusPreview = when {
                                previewDaysLeft > 0 -> "Sisa $previewDaysLeft hari lagi (H-$previewDaysLeft)"
                                previewDaysLeft == 0L -> "Tepat pada hari ini! 🎉"
                                else -> "Sudah lewat ${-previewDaysLeft} hari lalu"
                            }
                            Text(
                                text = statusPreview,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Catatan / Motivasi
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan / Pesan Motivasi (Opsional)") },
                    placeholder = { Text("Contoh: Wajib bawa kartu ujian & seragam batik") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            val updated = initialEvent.copy(
                                title = title.trim(),
                                targetDate = currentIsoDate,
                                category = category,
                                note = note.trim()
                            )
                            onSave(updated)
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Helpers for Countdown calculation and formatting
private fun formatCountdownDate(isoDate: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = sdf.parse(isoDate) ?: return isoDate
        val outSdf = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("id", "ID"))
        outSdf.format(date)
    } catch (e: Exception) {
        isoDate
    }
}

private fun calculateCountdownDays(isoDate: String): Long {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val target = sdf.parse(isoDate) ?: return 0L
        val calToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val calTarget = java.util.Calendar.getInstance().apply {
            time = target
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val diffMs = calTarget.timeInMillis - calToday.timeInMillis
        diffMs / (1000 * 60 * 60 * 24)
    } catch (e: Exception) {
        0L
    }
}

// 4. Social Media & School Location Dialog
@Composable
fun SocialMapDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Media Sosial & Lokasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Text(
                    text = "Akses saluran resmi komunitas kelas IX-H dan lokasi sekolah:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                SocialMediaLinkItem(
                    title = "Instagram Kelas IX-H",
                    subtitle = "@ixh.official.class",
                    emoji = "📸",
                    color = Color(0xFFBE185D),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com"))
                        context.startActivity(intent)
                    }
                )

                SocialMediaLinkItem(
                    title = "TikTok Kelas IX-H",
                    subtitle = "@ixh_genz_vibes",
                    emoji = "🎵",
                    color = Color(0xFF0F172A),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://tiktok.com"))
                        context.startActivity(intent)
                    }
                )

                SocialMediaLinkItem(
                    title = "Spotify Playlist Kelas",
                    subtitle = "Lagu Belajar & Nostalgia IX-H",
                    emoji = "🎧",
                    color = Color(0xFF15803D),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
                        context.startActivity(intent)
                    }
                )

                SocialMediaLinkItem(
                    title = "Lokasi Sekolah di Peta",
                    subtitle = "Buka petunjuk arah Google Maps",
                    emoji = "📍",
                    color = PrimaryBlueDark,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=SMP+Negeri+Sekolah"))
                        context.startActivity(intent)
                    }
                )

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
private fun SocialMediaLinkItem(
    title: String,
    subtitle: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
    }
}

// 5. Export Report Dialog (Attendance & Cash)
@Composable
fun ExportReportDialog(
    viewModel: ClassViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
    val attendanceMap by viewModel.attendance.collectAsState()
    val selectedDate by viewModel.selectedAttendanceDate.collectAsState()
    val transactions by viewModel.cashTransactions.collectAsState()

    val totalIncome = transactions.filter { it.type == CashType.PEMASUKAN }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == CashType.PENGELUARAN }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    val reportText = remember(students, attendanceMap, selectedDate, transactions) {
        val todayRecords = attendanceMap[selectedDate] ?: emptyList()
        val recMap = todayRecords.associateBy { it.studentId }

        val hadir = students.count { recMap[it.id]?.status == AttendanceStatus.HADIR }
        val izin = students.count { recMap[it.id]?.status == AttendanceStatus.IZIN }
        val sakit = students.count { recMap[it.id]?.status == AttendanceStatus.SAKIT }
        val alpa = students.count { recMap[it.id]?.status == AttendanceStatus.ALPA }

        buildString {
            append("========================================\n")
            append("      LAPORAN RESMI KELAS IX-H          \n")
            append("========================================\n")
            append("Tanggal: $selectedDate\n")
            append("Wali Kelas: Dra. Hj. Sri Wahyuni, M.Pd.\n\n")
            append("[ 1. REKAPITULASI PRESENSI HARIAN ]\n")
            append("- Hadir: $hadir Siswa\n")
            append("- Izin : $izin Siswa\n")
            append("- Sakit: $sakit Siswa\n")
            append("- Alpa : $alpa Siswa\n\n")
            append("Rincian Presensi Siswa:\n")
            students.forEach { s ->
                val st = recMap[s.id]?.status?.label ?: "Hadir"
                append(String.format("%02d. %-24s : %s\n", s.id, s.name, st))
            }
            append("\n[ 2. LAPORAN KAS KELAS ]\n")
            append("- Total Pemasukan  : Rp $totalIncome\n")
            append("- Total Pengeluaran: Rp $totalExpense\n")
            append("- Saldo Kas Aktif  : Rp $balance\n")
            append("========================================\n")
            append("Dicetak otomatis via Aplikasi IX-H Hub\n")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ekspor & Cetak Laporan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        item {
                            Text(
                                text = reportText,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Laporan IX-H", reportText))
                            Toast.makeText(context, "Laporan disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Teks")
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Laporan Resmi Kelas IX-H")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Kirim / Cetak Laporan"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim / Cetak")
                    }
                }
            }
        }
    }
}

// 6. User Role Switcher Dialog
@Composable
fun RoleSwitcherDialog(
    viewModel: ClassViewModel,
    onDismiss: () -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsState()
    var selectedRole by remember { mutableStateOf(currentRole) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Peran Pengguna") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pilih peran akun untuk menguji otorisasi fitur aplikasi:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                UserRole.values().forEach { role ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedRole = role
                                pinError = false
                            },
                        color = if (selectedRole == role) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = selectedRole == role,
                                onClick = {
                                    selectedRole = role
                                    pinError = false
                                }
                            )
                            Column {
                                Text(
                                    text = role.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = role.badge,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                if (selectedRole == UserRole.ADMIN) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("PIN Verifikasi Admin (Default: 1945)") },
                        singleLine = true,
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Text(text = "PIN Admin salah!", color = CoralRed, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRole == UserRole.ADMIN) {
                        if (pinInput.trim() == "1945" || pinInput.isBlank()) {
                            viewModel.switchRole(UserRole.ADMIN)
                            onDismiss()
                        } else {
                            pinError = true
                        }
                    } else {
                        viewModel.switchRole(selectedRole)
                        onDismiss()
                    }
                }
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
