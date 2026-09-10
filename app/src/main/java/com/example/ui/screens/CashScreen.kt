package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.ClassViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashScreen(
    viewModel: ClassViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val transactions by viewModel.cashTransactions.collectAsState()
    val students by viewModel.students.collectAsState()
    val dailyCashMap by viewModel.dailyStudentCash.collectAsState()
    val selectedCashDate by viewModel.selectedCashDate.collectAsState()
    val nominalPerHari by viewModel.nominalKasPerHari.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // 0: Setoran Kas Harian Siswa (Bendahara), 1: Buku Kas & Pengeluaran
    var currentSubTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showNominalDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showMarkAllConfirmDialog by remember { mutableStateOf(false) }
    var showDepositConfirmDialog by remember { mutableStateOf(false) }
    var noteStudentTarget by remember { mutableStateOf<Pair<Student, StudentDailyCash>?>(null) }

    // Filtering & Searching in Daily Student Cash
    var studentSearchQuery by remember { mutableStateOf("") }
    var studentCashFilter by remember { mutableIntStateOf(0) } // 0: Semua, 1: Sudah Bayar, 2: Belum Bayar

    // Filtering in Transactions
    var filterTxType by remember { mutableStateOf<CashType?>(null) }

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    // Daily Cash List mapped for all 32 students
    val dailyCashList = remember(dailyCashMap, selectedCashDate, students, nominalPerHari) {
        val recorded = dailyCashMap[selectedCashDate] ?: emptyList()
        students.map { student ->
            recorded.find { it.studentId == student.id } ?: StudentDailyCash(
                studentId = student.id,
                date = selectedCashDate,
                isPaid = false,
                amount = 0L,
                paidAt = "",
                note = "Belum bayar"
            )
        }
    }

    val paidCount = remember(dailyCashList) { dailyCashList.count { it.isPaid } }
    val unpaidCount = remember(dailyCashList) { dailyCashList.size - paidCount }
    val totalCollectedToday = remember(dailyCashList, nominalPerHari) {
        dailyCashList.filter { it.isPaid }.sumOf { if (it.amount > 0) it.amount else nominalPerHari }
    }
    val targetClassToday = remember(students, nominalPerHari) {
        students.size * nominalPerHari
    }
    val progressPercentage = if (students.isNotEmpty()) (paidCount.toFloat() / students.size.toFloat()) else 0f

    // Total Book Cash calculations
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == CashType.PEMASUKAN }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == CashType.PENGELUARAN }.sumOf { it.amount }
    }
    val currentBalance = totalIncome - totalExpense

    val filteredTransactions = remember(transactions, filterTxType) {
        if (filterTxType == null) transactions else transactions.filter { it.type == filterTxType }
    }

    // Filtered Student List
    val filteredStudentsWithCash = remember(students, dailyCashList, studentSearchQuery, studentCashFilter) {
        val paired = students.map { student ->
            val cashRecord = dailyCashList.find { it.studentId == student.id }
                ?: StudentDailyCash(student.id, selectedCashDate, false, 0L)
            Pair(student, cashRecord)
        }

        paired.filter { (student, cash) ->
            val matchQuery = studentSearchQuery.isBlank() ||
                    student.name.contains(studentSearchQuery, ignoreCase = true) ||
                    student.nickname.contains(studentSearchQuery, ignoreCase = true) ||
                    student.id.toString() == studentSearchQuery.trim()

            val matchStatus = when (studentCashFilter) {
                1 -> cash.isPaid
                2 -> !cash.isPaid
                else -> true
            }

            matchQuery && matchStatus
        }
    }

    val isTreasurerOrAdmin = currentRole == UserRole.ADMIN || currentRole == UserRole.PICKET

    Scaffold(
        modifier = modifier.testTag("cash_screen"),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (currentSubTab == 1 && currentRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showAddTxDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_cash_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Catat Pengeluaran/Pemasukan")
                }
            }
        }
    ) { paddingValues ->
        GlassBackgroundBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Main Sub-Tab Switcher
            TabRow(
                selectedTabIndex = currentSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = currentSubTab == 0,
                    onClick = { currentSubTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Setoran (${paidCount}/32)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
                Tab(
                    selected = currentSubTab == 1,
                    onClick = { currentSubTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Buku Kas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
            }

            if (currentSubTab == 0) {
                // ==========================================
                // TAB 0: SETORAN KAS HARIAN SISWA (BENDAHARA)
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
                ) {
                    // Date Navigation Bar
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.selectCashDate(shiftCashDate(selectedCashDate, -1))
                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Hari Sebelumnya")
                                    }

                                    Surface(
                                        onClick = { showDateDialog = true },
                                        shape = RoundedCornerShape(20.dp),
                                        color = Navy900.copy(alpha = 0.08f),
                                        modifier = Modifier.testTag("cash_date_picker_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = formatIndonesianFullDate(selectedCashDate),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.selectCashDate(shiftCashDate(selectedCashDate, 1))
                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Hari Berikutnya")
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick Date Shortcut Chips (Hari Ini - Senin s/d Sabtu)
                                val todayFormatted = remember { ensureNonSunday(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SuggestionChip(
                                        onClick = { viewModel.selectCashDate(todayFormatted) },
                                        label = { Text("Kembali ke Hari Sekolah") },
                                        icon = {
                                            Icon(
                                                Icons.Default.Today,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (selectedCashDate == todayFormatted) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Daily Cash Status Hero Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Navy900)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "KAS HARIAN IX-H",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentGoldLight
                                            )
                                            Surface(
                                                color = Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "32 Siswa",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = rupiahFormat.format(totalCollectedToday),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Tarif / Hari",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "${rupiahFormat.format(nominalPerHari)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGoldLight
                                        )
                                        if (currentRole == UserRole.ADMIN) {
                                            Text(
                                                text = "Ubah Tarif",
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                modifier = Modifier
                                                    .clickable { showNominalDialog = true }
                                                    .padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Progress Indicator for 32 Students
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Progres Setoran: $paidCount / 32 Siswa (${(progressPercentage * 100).toInt()}%)",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Target: ${rupiahFormat.format(targetClassToday)}",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { progressPercentage },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (paidCount == 32) EmeraldGreen else AccentGold,
                                        trackColor = Color.White.copy(alpha = 0.15f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Breakdown Stats (Sudah vs Belum)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldGreen.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = EmeraldGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column {
                                                Text("Sudah Bayar", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                                Text("$paidCount Siswa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                            }
                                        }
                                    }

                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(CoralRed.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = CoralRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column {
                                                Text("Belum Kas", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                                Text("$unpaidCount Siswa", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bendahara Action Bar (Salin WA, Tandai Semua, Setor ke Kas)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Aksi Bendahara & Rekap",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.openModal("ANALYTICS") },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("cash_analytics_btn")
                                        ) {
                                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Analitik Kas", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                        }

                                        if (!isTreasurerOrAdmin) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Mode Siswa",
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Copy WhatsApp Bill Summary
                                    OutlinedButton(
                                        onClick = {
                                            val text = buildWhatsAppSummary(
                                                dateStr = selectedCashDate,
                                                students = students,
                                                dailyList = dailyCashList,
                                                nominal = nominalPerHari,
                                                totalCollected = totalCollectedToday,
                                                target = targetClassToday,
                                                rupiahFormat = rupiahFormat
                                            )
                                            clipboardManager.setText(AnnotatedString(text))
                                            Toast.makeText(context, "Rekap tagihan kas berhasil disalin ke WhatsApp!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("copy_wa_recap_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Salin WA", fontSize = 12.sp)
                                    }

                                    if (isTreasurerOrAdmin) {
                                        // Mark all paid button
                                        Button(
                                            onClick = { showMarkAllConfirmDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("mark_all_cash_button"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Semua Lunas", fontSize = 12.sp)
                                        }
                                    }
                                }

                                if (isTreasurerOrAdmin && totalCollectedToday > 0) {
                                    Button(
                                        onClick = { showDepositConfirmDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("deposit_to_ledger_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                    ) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Setorkan ${rupiahFormat.format(totalCollectedToday)} ke Buku Kas Kelas",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Search & Filter Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = studentSearchQuery,
                                onValueChange = { studentSearchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("student_cash_search_input"),
                                placeholder = { Text("Cari nama atau absen (1 - 32)...") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    if (studentSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { studentSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )

                            // Filter Status Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = studentCashFilter == 0,
                                    onClick = { studentCashFilter = 0 },
                                    label = { Text("Semua (32)") }
                                )
                                FilterChip(
                                    selected = studentCashFilter == 1,
                                    onClick = { studentCashFilter = 1 },
                                    label = { Text("Sudah Kas ($paidCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen.copy(alpha = 0.2f),
                                        selectedLabelColor = EmeraldGreen
                                    )
                                )
                                FilterChip(
                                    selected = studentCashFilter == 2,
                                    onClick = { studentCashFilter = 2 },
                                    label = { Text("Belum Kas ($unpaidCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralRed.copy(alpha = 0.2f),
                                        selectedLabelColor = CoralRed
                                    )
                                )
                            }
                        }
                    }

                    // List of 32 Students with Cash Status
                    items(filteredStudentsWithCash, key = { it.first.id }) { (student, cash) ->
                        DailyStudentCashCard(
                            student = student,
                            cash = cash,
                            nominalPerHari = nominalPerHari,
                            rupiahFormat = rupiahFormat,
                            isTreasurerOrAdmin = isTreasurerOrAdmin,
                            onToggleCash = {
                                val nextStatus = !cash.isPaid
                                viewModel.toggleStudentDailyCash(student.id, nextStatus)
                                val msg = if (nextStatus) "${student.name} ditandai Lunas" else "Status kas ${student.name} dibatalkan"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onEditNote = {
                                noteStudentTarget = Pair(student, cash)
                            }
                        )
                    }
                }
            } else {
                // ==========================================
                // TAB 1: BUKU KAS KELAS & PENGELUARAN
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    // Main Balance Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Navy900)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Saldo Kas IX-H",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = rupiahFormat.format(currentBalance),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        color = AccentGold.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ) {
                                        Icon(
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = AccentGoldLight,
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // In/Out Sub-Cards
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(text = "Total Masuk", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                                Text(
                                                    text = rupiahFormat.format(totalIncome),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldGreen
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = CoralRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(text = "Total Keluar", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                                Text(
                                                    text = rupiahFormat.format(totalExpense),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CoralRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Nominal Kas Setting Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Ketentuan Kas Kelas",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tarif: ${rupiahFormat.format(nominalPerHari)} / siswa per hari (32 Siswa)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (currentRole == UserRole.ADMIN) {
                                    TextButton(onClick = { showNominalDialog = true }) {
                                        Text("Ubah Tarif")
                                    }
                                }
                            }
                        }
                    }

                    // Transactions Header & Filter Chips
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Riwayat Arus Kas (${filteredTransactions.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (currentRole == UserRole.ADMIN) {
                                    FilledTonalButton(
                                        onClick = { showAddTxDialog = true },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Catat Kas", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = filterTxType == null,
                                    onClick = { filterTxType = null },
                                    label = { Text("Semua") }
                                )
                                FilterChip(
                                    selected = filterTxType == CashType.PEMASUKAN,
                                    onClick = { filterTxType = CashType.PEMASUKAN },
                                    label = { Text("Pemasukan") }
                                )
                                FilterChip(
                                    selected = filterTxType == CashType.PENGELUARAN,
                                    onClick = { filterTxType = CashType.PENGELUARAN },
                                    label = { Text("Pengeluaran") }
                                )
                            }
                        }
                    }

                    // Transaction Items
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionCard(tx = tx, rupiahFormat = rupiahFormat)
                    }
                }
            }
        }
    }
}

    // Dialog: Tandai Semua Lunas
    if (showMarkAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllConfirmDialog = false },
            title = { Text("Tandai Semua Siswa Lunas?") },
            text = {
                Text("Aksi ini akan mencatat status Kas Lunas untuk seluruh 32 siswa kelas IX-H pada tanggal ${formatIndonesianFullDate(selectedCashDate)} (Total: ${rupiahFormat.format(targetClassToday)}).")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAllCashForSelectedDate(true)
                        showMarkAllConfirmDialog = false
                        Toast.makeText(context, "Seluruh 32 siswa ditandai Lunas!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Ya, Tandai Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAllConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Setorkan ke Buku Kas
    if (showDepositConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDepositConfirmDialog = false },
            title = { Text("Setorkan ke Kas Utama?") },
            text = {
                Text("Total setoran kas yang terkumpul hari ini (${rupiahFormat.format(totalCollectedToday)} dari $paidCount siswa) akan dicatat sebagai Pemasukan di Buku Kas Kelas IX-H.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.depositDailyCashToBook(recorder = "Bendahara 1 (Aurel)")
                        showDepositConfirmDialog = false
                        currentSubTab = 1 // Switch to ledger to view updated transactions
                        Toast.makeText(context, "Kas harian berhasil disetorkan ke Buku Kas!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Setorkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Catatan Tambahan Siswa
    if (noteStudentTarget != null) {
        val target = noteStudentTarget!!
        var noteText by remember { mutableStateOf(target.second.note) }

        AlertDialog(
            onDismissRequest = { noteStudentTarget = null },
            title = { Text("Catatan Kas - ${target.first.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Absen #${target.first.id} • Tanggal: ${selectedCashDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Catatan Setoran") },
                        placeholder = { Text("Contoh: Titip ke Aurel, Bayar untuk 2 hari...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleStudentDailyCash(
                            studentId = target.first.id,
                            isPaid = target.second.isPaid,
                            note = noteText.trim()
                        )
                        noteStudentTarget = null
                        Toast.makeText(context, "Catatan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteStudentTarget = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Dialog: Pilih Tanggal Kas
    if (showDateDialog) {
        val todayRaw = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
        val todayStr = remember { ensureNonSunday(todayRaw) }
        var dateInput by remember { mutableStateOf(selectedCashDate) }
        val isInputSunday = remember(dateInput) { isSunday(dateInput) }

        AlertDialog(
            onDismissRequest = { showDateDialog = false },
            title = { Text("Pilih Tanggal Kas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Kas kelas ditarik setiap hari sekolah (Senin s/d Sabtu). Hari Minggu tidak ada penarikan kas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Format: YYYY-MM-DD") },
                        placeholder = { Text("2026-09-09") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = if (isInputSunday) {
                            { Text("⚠️ Hari Minggu libur. Otomatis dialihkan ke hari Senin berikutnya.", color = CoralRed) }
                        } else null
                    )

                    Text(
                        text = "Pilihan Cepat Hari Sekolah:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = dateInput == todayStr,
                            onClick = { dateInput = todayStr },
                            label = { Text("Hari Ini") }
                        )
                        FilterChip(
                            selected = dateInput == shiftCashDate(todayStr, -1),
                            onClick = { dateInput = shiftCashDate(todayStr, -1) },
                            label = { Text("Kemarin") }
                        )
                        FilterChip(
                            selected = dateInput == shiftCashDate(todayStr, -2),
                            onClick = { dateInput = shiftCashDate(todayStr, -2) },
                            label = { Text("-2 Hari") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dateInput.isNotBlank()) {
                            viewModel.selectCashDate(ensureNonSunday(dateInput.trim()))
                            showDateDialog = false
                        }
                    }
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Tambah Transaksi Manual Buku Kas
    if (showAddTxDialog) {
        var txType by remember { mutableStateOf(CashType.PENGELUARAN) }
        var amountStr by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Alat Kebersihan") }
        var description by remember { mutableStateOf("") }
        var recorder by remember { mutableStateOf("Bendahara 1 (Aurel)") }

        AlertDialog(
            onDismissRequest = { showAddTxDialog = false },
            title = { Text("Catat Arus Kas Kelas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = txType == CashType.PEMASUKAN,
                            onClick = { txType = CashType.PEMASUKAN },
                            shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) {
                            Text("Pemasukan")
                        }
                        SegmentedButton(
                            selected = txType == CashType.PENGELUARAN,
                            onClick = { txType = CashType.PENGELUARAN },
                            shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) {
                            Text("Pengeluaran")
                        }
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal (Rp)") },
                        placeholder = { Text("Contoh: 25000") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        placeholder = { Text("Spidol / Fotokopi / Kasus Sosial") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Keterangan") },
                        placeholder = { Text("Rincian keperluan...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recorder,
                        onValueChange = { recorder = it },
                        label = { Text("Dicatat Oleh") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = amountStr.toLongOrNull() ?: 0L
                        if (amountVal > 0 && description.isNotBlank()) {
                            viewModel.addCashTransaction(
                                type = txType,
                                amount = amountVal,
                                category = category.trim(),
                                desc = description.trim(),
                                recorder = recorder.trim()
                            )
                            showAddTxDialog = false
                            Toast.makeText(context, "Transaksi berhasil dicatat", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTxDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Ubah Nominal Kas
    if (showNominalDialog) {
        var newNominalStr by remember { mutableStateOf(nominalPerHari.toString()) }

        AlertDialog(
            onDismissRequest = { showNominalDialog = false },
            title = { Text("Ubah Tarif Kas Harian") },
            text = {
                Column {
                    Text(
                        text = "Tentukan besaran iuran kas per hari yang disepakati oleh ke-32 siswa kelas IX-H.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newNominalStr,
                        onValueChange = { newNominalStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Tarif Kas per Siswa / Hari (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Estimasi per hari (32 siswa): ${rupiahFormat.format((newNominalStr.toLongOrNull() ?: 0L) * 32)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newNominalStr.toLongOrNull() ?: nominalPerHari
                        viewModel.updateNominalKas(parsed)
                        showNominalDialog = false
                        Toast.makeText(context, "Tarif kas diperbarui menjadi ${rupiahFormat.format(parsed)}/hari", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNominalDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun DailyStudentCashCard(
    student: Student,
    cash: StudentDailyCash,
    nominalPerHari: Long,
    rupiahFormat: NumberFormat,
    isTreasurerOrAdmin: Boolean,
    onToggleCash: () -> Unit,
    onEditNote: () -> Unit
) {
    val isPaid = cash.isPaid

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_cash_item_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPaid) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Student Photo Avatar
            StudentPhotoAvatar(student = student, size = 46.dp)

            // Student Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "#${student.id}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (student.role != "Anggota") {
                        Text(
                            text = student.role,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "•", fontSize = 11.sp, color = TextSecondary)
                    }

                    if (isPaid) {
                        Text(
                            text = "Lunas ${rupiahFormat.format(if (cash.amount > 0) cash.amount else nominalPerHari)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        if (cash.paidAt.isNotBlank()) {
                            Text(text = "(${cash.paidAt})", fontSize = 11.sp, color = TextSecondary)
                        }
                    } else {
                        Text(
                            text = "Belum Bayar (${rupiahFormat.format(nominalPerHari)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralRed
                        )
                    }
                }

                if (cash.note.isNotBlank() && cash.note != "Lunas" && cash.note != "Belum bayar") {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Catatan: ${cash.note}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Status Badge & Action Controls
            if (isTreasurerOrAdmin) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditNote,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.EditNote,
                            contentDescription = "Catatan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isPaid) {
                        FilledTonalButton(
                            onClick = onToggleCash,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = EmeraldGreen.copy(alpha = 0.2f),
                                contentColor = EmeraldGreen
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lunas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onToggleCash,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Bayar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Read-only indicator for student mode
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPaid) EmeraldGreen.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (isPaid) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isPaid) EmeraldGreen else CoralRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isPaid) "Lunas" else "Belum",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) EmeraldGreen else CoralRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(tx: CashTransaction, rupiahFormat: NumberFormat) {
    val isIncome = tx.type == CashType.PEMASUKAN

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isIncome) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isIncome) EmeraldGreen else CoralRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tx.category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = (if (isIncome) "+ " else "- ") + rupiahFormat.format(tx.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) EmeraldGreen else CoralRed
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = tx.date, fontSize = 11.sp, color = TextSecondary)
                    Text(text = "•", fontSize = 11.sp, color = TextSecondary)
                    Text(text = tx.recordedBy, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

// Helpers
private fun formatIndonesianFullDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(dateStr) ?: Date()
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        formatter.format(date)
    } catch (e: Exception) {
        dateStr
    }
}

private fun isSunday(dateStr: String): Boolean {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            time = parser.parse(dateStr) ?: Date()
        }
        calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
    } catch (e: Exception) {
        false
    }
}

private fun ensureNonSunday(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            time = parser.parse(dateStr) ?: Date()
        }
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Hari Minggu dialihkan ke Senin
        }
        parser.format(calendar.time)
    } catch (e: Exception) {
        dateStr
    }
}

private fun shiftCashDate(currentDateStr: String, days: Int): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            time = parser.parse(currentDateStr) ?: Date()
        }
        val direction = if (days >= 0) 1 else -1
        var steps = kotlin.math.abs(days)
        while (steps > 0) {
            calendar.add(Calendar.DAY_OF_YEAR, direction)
            // Uang kas hanya Senin - Sabtu, tidak ada hari Minggu
            if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                steps--
            }
        }
        parser.format(calendar.time)
    } catch (e: Exception) {
        currentDateStr
    }
}

private fun buildWhatsAppSummary(
    dateStr: String,
    students: List<Student>,
    dailyList: List<StudentDailyCash>,
    nominal: Long,
    totalCollected: Long,
    target: Long,
    rupiahFormat: NumberFormat
): String {
    val paidStudents = students.filter { s -> dailyList.any { it.studentId == s.id && it.isPaid } }
    val unpaidStudents = students.filter { s -> dailyList.any { it.studentId == s.id && !it.isPaid } }
    val formattedDate = formatIndonesianFullDate(dateStr)

    return buildString {
        appendLine("📢 *REKAP KAS HARIAN KELAS IX-H*")
        appendLine("🗓 *Hari/Tanggal:* $formattedDate")
        appendLine("💰 *Tarif Kas:* ${rupiahFormat.format(nominal)} / siswa per hari")
        appendLine("📊 *Status:* ${paidStudents.size} dari ${students.size} Siswa Lunas")
        appendLine("💵 *Terkumpul:* ${rupiahFormat.format(totalCollected)} / ${rupiahFormat.format(target)}")
        appendLine("━━━━━━━━━━━━━━━━━━━")
        appendLine("✅ *SUDAH BAYAR (${paidStudents.size} Siswa):*")
        if (paidStudents.isEmpty()) {
            appendLine("- (Belum ada yang menyetor)")
        } else {
            paidStudents.forEach { s ->
                val record = dailyList.find { it.studentId == s.id }
                val timeInfo = if (record != null && record.paidAt.isNotBlank()) " [${record.paidAt}]" else ""
                appendLine("${s.id}. ${s.name} (${s.nickname})$timeInfo")
            }
        }
        appendLine()
        appendLine("❌ *BELUM BAYAR (${unpaidStudents.size} Siswa):*")
        if (unpaidStudents.isEmpty()) {
            appendLine("🎉 *Semua siswa sudah lunas tepat waktu! Terima kasih banyak.* ✨")
        } else {
            unpaidStudents.forEach { s ->
                appendLine("${s.id}. ${s.name} (${s.nickname})")
            }
            appendLine()
            appendLine("⚠️ *Catatan:* Mohon segera menyetorkan uang kas kepada Bendahara 1 (Aurel) atau Bendahara 2 (Cantika). Terima kasih! 🙏")
        }
    }
}
