package com.example.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.ClassViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

enum class AnalyticsTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Ringkasan", Icons.Default.Dashboard),
    FINANCE("Kas & Biaya", Icons.Default.AccountBalanceWallet),
    ATTENDANCE("Presensi", Icons.Default.FactCheck),
    DEMOGRAPHICS("Profil & Cita-Cita", Icons.Default.School)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDialog(
    viewModel: ClassViewModel,
    onDismiss: () -> Unit
) {
    val students by viewModel.students.collectAsState()
    val transactions by viewModel.cashTransactions.collectAsState()
    val attendanceMap by viewModel.attendance.collectAsState()
    val selectedAttendanceDate by viewModel.selectedAttendanceDate.collectAsState()
    val dailyCashMap by viewModel.dailyStudentCash.collectAsState()
    val selectedCashDate by viewModel.selectedCashDate.collectAsState()
    val nominalPerHari by viewModel.nominalKasPerHari.collectAsState()

    var selectedTab by remember { mutableStateOf(AnalyticsTab.OVERVIEW) }

    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    // Cash Calculations
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == CashType.PEMASUKAN }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == CashType.PENGELUARAN }.sumOf { it.amount }
    }
    val currentBalance = totalIncome - totalExpense

    // Daily Cash Stats
    val dailyCashList = remember(dailyCashMap, selectedCashDate, students) {
        val list = dailyCashMap[selectedCashDate] ?: emptyList()
        val recMap = list.associateBy { it.studentId }
        students.map { student ->
            recMap[student.id] ?: StudentDailyCash(student.id, selectedCashDate, false, 0L)
        }
    }
    val paidCount = dailyCashList.count { it.isPaid }
    val unpaidCount = dailyCashList.size - paidCount
    val cashComplianceRate = if (students.isNotEmpty()) (paidCount.toFloat() / students.size.toFloat()) * 100f else 0f

    // Attendance Stats
    val todayAttendance = remember(attendanceMap, selectedAttendanceDate, students) {
        val list = attendanceMap[selectedAttendanceDate] ?: emptyList()
        val recMap = list.associateBy { it.studentId }
        students.map { student ->
            recMap[student.id] ?: AttendanceRecord(student.id, selectedAttendanceDate, AttendanceStatus.HADIR)
        }
    }
    val hadirCount = todayAttendance.count { it.status == AttendanceStatus.HADIR }
    val izinCount = todayAttendance.count { it.status == AttendanceStatus.IZIN }
    val sakitCount = todayAttendance.count { it.status == AttendanceStatus.SAKIT }
    val alpaCount = todayAttendance.count { it.status == AttendanceStatus.ALPA }
    val attendanceRate = if (students.isNotEmpty()) (hadirCount.toFloat() / students.size.toFloat()) * 100f else 0f

    // Demographics
    val maleCount = students.count { it.gender.startsWith("L", ignoreCase = true) }
    val femaleCount = students.count { it.gender.startsWith("P", ignoreCase = true) }
    val officersCount = students.count { it.role != "Anggota" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("analytics_dialog"),
            shape = RoundedCornerShape(26.dp),
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimaryBlue, PurpleMystery)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Pusat Analitik & Statistik",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Statistik Kelas IX-H (Keuangan, Presensi & Profil)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_analytics_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    AnalyticsTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = tab.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // Tab Content Body
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        AnalyticsTab.OVERVIEW -> OverviewAnalyticsTab(
                            studentsCount = students.size,
                            currentBalance = currentBalance,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            attendanceRate = attendanceRate,
                            hadirCount = hadirCount,
                            paidCount = paidCount,
                            cashComplianceRate = cashComplianceRate,
                            rupiahFormat = rupiahFormat,
                            onExportClick = {
                                onDismiss()
                                viewModel.openModal("EXPORT")
                            }
                        )
                        AnalyticsTab.FINANCE -> FinanceAnalyticsTab(
                            transactions = transactions,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            currentBalance = currentBalance,
                            paidCount = paidCount,
                            unpaidCount = unpaidCount,
                            totalStudents = students.size,
                            nominalPerHari = nominalPerHari,
                            rupiahFormat = rupiahFormat
                        )
                        AnalyticsTab.ATTENDANCE -> AttendanceAnalyticsTab(
                            hadirCount = hadirCount,
                            izinCount = izinCount,
                            sakitCount = sakitCount,
                            alpaCount = alpaCount,
                            totalStudents = students.size,
                            attendanceRate = attendanceRate,
                            selectedDate = selectedAttendanceDate
                        )
                        AnalyticsTab.DEMOGRAPHICS -> DemographicsAnalyticsTab(
                            students = students,
                            maleCount = maleCount,
                            femaleCount = femaleCount,
                            officersCount = officersCount
                        )
                    }
                }
            }
        }
    }
}

// 1. Overview Tab
@Composable
private fun OverviewAnalyticsTab(
    studentsCount: Int,
    currentBalance: Long,
    totalIncome: Long,
    totalExpense: Long,
    attendanceRate: Float,
    hadirCount: Int,
    paidCount: Int,
    cashComplianceRate: Float,
    rupiahFormat: NumberFormat,
    onExportClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Hero KPI Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan Performa Kelas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricBox(
                            title = "Saldo Kas",
                            value = rupiahFormat.format(currentBalance),
                            color = PrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = "Kehadiran",
                            value = "${attendanceRate.toInt()}%",
                            color = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = "Kas Hari Ini",
                            value = "${cashComplianceRate.toInt()}%",
                            color = AccentGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            // Visual Progress Bars
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Indikator Utama",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Attendance Progress
                    ProgressIndicatorRow(
                        label = "Tingkat Kehadiran Siswa Hari Ini",
                        fraction = (attendanceRate / 100f).coerceIn(0f, 1f),
                        progressText = "$hadirCount dari $studentsCount Hadir (${attendanceRate.toInt()}%)",
                        color = EmeraldGreen
                    )

                    // Cash Compliance Progress
                    ProgressIndicatorRow(
                        label = "Kepatuhan Pembayaran Kas Hari Ini",
                        fraction = (cashComplianceRate / 100f).coerceIn(0f, 1f),
                        progressText = "$paidCount dari $studentsCount Lunas (${cashComplianceRate.toInt()}%)",
                        color = PrimaryBlue
                    )

                    // Financial Health (Income vs Expense)
                    val incomeRatio = if (totalIncome + totalExpense > 0) totalIncome.toFloat() / (totalIncome + totalExpense).toFloat() else 1f
                    ProgressIndicatorRow(
                        label = "Rasio Pemasukan terhadap Pengeluaran",
                        fraction = incomeRatio.coerceIn(0f, 1f),
                        progressText = "${(incomeRatio * 100).toInt()}% Sehat (Surplus Rp ${rupiahFormat.format(currentBalance)})",
                        color = AccentGold
                    )
                }
            }
        }

        item {
            // Quick Export & Report Action
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PurpleMystery.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleMystery.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📄 Ekspor Laporan Lengkap",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PurpleMystery
                        )
                        Text(
                            text = "Cetak rekapitulasi data presensi dan kas resmi untuk wali kelas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onExportClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleMystery),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Buka Laporan", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// 2. Finance Tab
@Composable
private fun FinanceAnalyticsTab(
    transactions: List<CashTransaction>,
    totalIncome: Long,
    totalExpense: Long,
    currentBalance: Long,
    paidCount: Int,
    unpaidCount: Int,
    totalStudents: Int,
    nominalPerHari: Long,
    rupiahFormat: NumberFormat
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pemasukan", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        Text(rupiahFormat.format(totalIncome), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CoralRed.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pengeluaran", fontSize = 11.sp, color = CoralRed, fontWeight = FontWeight.Bold)
                        Text(rupiahFormat.format(totalExpense), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CoralRed)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Statistik Setoran Kas Harian", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "Tarif per siswa: ${rupiahFormat.format(nominalPerHari)} / hari",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🟢 Lunas ($paidCount siswa):", fontSize = 12.sp)
                        Text(rupiahFormat.format(paidCount * nominalPerHari), fontWeight = FontWeight.Bold, color = EmeraldGreen, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🔴 Belum Bayar ($unpaidCount siswa):", fontSize = 12.sp)
                        Text(rupiahFormat.format(unpaidCount * nominalPerHari), fontWeight = FontWeight.Bold, color = CoralRed, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🎯 Potensi Target Harian:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(rupiahFormat.format(totalStudents * nominalPerHari), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Riwayat Transaksi Terbaru (${transactions.size} total)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    transactions.take(5).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.description, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(tx.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val isIncome = tx.type == CashType.PEMASUKAN
                            Text(
                                text = (if (isIncome) "+ " else "- ") + rupiahFormat.format(tx.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isIncome) EmeraldGreen else CoralRed
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. Attendance Tab
@Composable
private fun AttendanceAnalyticsTab(
    hadirCount: Int,
    izinCount: Int,
    sakitCount: Int,
    alpaCount: Int,
    totalStudents: Int,
    attendanceRate: Float,
    selectedDate: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Tingkat Kehadiran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Tanggal: $selectedDate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "${attendanceRate.toInt()}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Distribusi Status Presensi", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    StatusDistributionBar(label = "Hadir", count = hadirCount, total = totalStudents, color = EmeraldGreen)
                    StatusDistributionBar(label = "Izin", count = izinCount, total = totalStudents, color = PrimaryBlue)
                    StatusDistributionBar(label = "Sakit", count = sakitCount, total = totalStudents, color = AccentGold)
                    StatusDistributionBar(label = "Alpa / Tanpa Keterangan", count = alpaCount, total = totalStudents, color = CoralRed)
                }
            }
        }
    }
}

// 4. Demographics & Dreams Tab
@Composable
private fun DemographicsAnalyticsTab(
    students: List<Student>,
    maleCount: Int,
    femaleCount: Int,
    officersCount: Int
) {
    val topDreams = remember(students) {
        students
            .filter { it.dreamSchool.isNotBlank() }
            .groupBy { it.dreamSchool.trim() }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(6)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Komposisi Gender Siswa IX-H", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricBox(title = "Laki-laki", value = "$maleCount Siswa (50%)", color = PrimaryBlue, modifier = Modifier.weight(1f))
                        MetricBox(title = "Perempuan", value = "$femaleCount Siswi (50%)", color = PurpleMystery, modifier = Modifier.weight(1f))
                    }
                    Text("⭐ Pengurus Inti Kelas: $officersCount siswa", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🎯", fontSize = 16.sp)
                        Text("Target SMA / SMK Favorit Siswa", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (topDreams.isEmpty()) {
                        Text("Belum ada data target sekolah impian.", fontSize = 12.sp, color = TextSecondary)
                    } else {
                        topDreams.forEach { (school, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = school,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryBlue.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "$count Siswa",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
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

@Composable
private fun MetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProgressIndicatorRow(
    label: String,
    fraction: Float,
    progressText: String,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(progressText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun StatusDistributionBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f
    ProgressIndicatorRow(
        label = label,
        fraction = fraction,
        progressText = "$count siswa (${(fraction * 100).toInt()}%)",
        color = color
    )
}
