package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.ClassViewModel
import com.example.ui.theme.*

enum class AdminTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SISWA("Siswa", Icons.Default.Groups),
    JADWAL("Jadwal", Icons.Default.CalendarMonth),
    PIKET("Piket", Icons.Default.CleaningServices),
    KAS("Kas", Icons.Default.AccountBalanceWallet),
    PENGATURAN("Kelas & PIN", Icons.Default.Settings),
    CLOUD("Cloud Sync", Icons.Default.CloudSync)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlCenterDialog(
    viewModel: ClassViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(AdminTab.SISWA) }

    val students by viewModel.students.collectAsState()
    val classSettings by viewModel.classSettings.collectAsState()
    val lessonSchedules by viewModel.lessonSchedules.collectAsState()
    val picketSchedule by viewModel.picketSchedule.collectAsState()
    val cashTransactions by viewModel.cashTransactions.collectAsState()
    val nominalKas by viewModel.nominalKasPerHari.collectAsState()

    // Sub-dialog states
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var editingScheduleIndex by remember { mutableStateOf<Int?>(null) }
    var editingScheduleItem by remember { mutableStateOf<LessonSchedule?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isGlassDark()) GlassDarkCard else Color(0xFFF8FAFC)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderBrush())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CoralRed.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed)
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = CoralRed,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Panel Master Admin Kelas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Kelola & atur seluruh isi database aplikasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Admin Tabs Navigation (Glass Scrollable Tab Row)
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    AdminTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(tab.title, fontSize = 12.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // Tab Content Body
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        AdminTab.SISWA -> AdminStudentsTab(
                            students = students,
                            onEdit = { editingStudent = it },
                            onDelete = {
                                viewModel.deleteStudent(it)
                                Toast.makeText(context, "Siswa dihapus", Toast.LENGTH_SHORT).show()
                            },
                            onAdd = { showAddStudentDialog = true }
                        )
                        AdminTab.JADWAL -> AdminScheduleTab(
                            schedules = lessonSchedules,
                            onAdd = { showAddScheduleDialog = true },
                            onEdit = { index, item ->
                                editingScheduleIndex = index
                                editingScheduleItem = item
                            },
                            onDelete = { index ->
                                viewModel.deleteLessonSchedule(index)
                                Toast.makeText(context, "Jadwal dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                        AdminTab.PIKET -> AdminPicketTab(
                            picketSchedule = picketSchedule,
                            students = students,
                            onUpdate = { day, ids ->
                                viewModel.updatePicketSchedule(day, ids)
                                Toast.makeText(context, "Jadwal piket $day diperbarui", Toast.LENGTH_SHORT).show()
                            },
                            onReset = { day ->
                                viewModel.resetPicketStatus(day)
                                Toast.makeText(context, "Status piket $day direset", Toast.LENGTH_SHORT).show()
                            }
                        )
                        AdminTab.KAS -> AdminCashTab(
                            nominalKas = nominalKas,
                            transactions = cashTransactions,
                            onUpdateNominal = { newNominal ->
                                viewModel.updateNominalKas(newNominal)
                                Toast.makeText(context, "Tarif kas diubah ke Rp $newNominal", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteTransaction = { txId ->
                                viewModel.deleteCashTransaction(txId)
                                Toast.makeText(context, "Transaksi $txId dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                        AdminTab.PENGATURAN -> AdminSettingsTab(
                            settings = classSettings,
                            onSave = { updated ->
                                viewModel.updateClassSettings(updated)
                                Toast.makeText(context, "Pengaturan kelas berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        AdminTab.CLOUD -> AdminCloudTab(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Sub-dialog: Edit Student
    if (editingStudent != null) {
        EditStudentDialog(
            student = editingStudent!!,
            onDismiss = { editingStudent = null },
            onSave = { updated ->
                viewModel.updateStudent(updated)
                editingStudent = null
                Toast.makeText(context, "Data ${updated.name} berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Sub-dialog: Add Student
    if (showAddStudentDialog) {
        val nextId = (students.maxOfOrNull { it.id } ?: 32) + 1
        EditStudentDialog(
            student = Student(
                id = nextId,
                name = "",
                nickname = "",
                gender = "L",
                role = "Anggota",
                pin = "${1000 + nextId}",
                dreamSchool = "SMAN 1",
                avatarEmoji = "🎓",
                photoUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&auto=format&fit=crop&q=80",
                bio = "Programmer & AI Engineer"
            ),
            isNew = true,
            onDismiss = { showAddStudentDialog = false },
            onSave = { newStudent ->
                viewModel.addStudent(newStudent)
                showAddStudentDialog = false
                Toast.makeText(context, "Siswa baru berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Sub-dialog: Add / Edit Lesson Schedule
    if (showAddScheduleDialog || editingScheduleItem != null) {
        EditScheduleDialog(
            initial = editingScheduleItem ?: LessonSchedule("Senin", "07:00 - 08:00", "", "", "Ruang IX-H"),
            isNew = showAddScheduleDialog,
            onDismiss = {
                showAddScheduleDialog = false
                editingScheduleItem = null
                editingScheduleIndex = null
            },
            onSave = { schedule ->
                if (showAddScheduleDialog) {
                    viewModel.addLessonSchedule(schedule)
                } else if (editingScheduleIndex != null) {
                    viewModel.updateLessonSchedule(editingScheduleIndex!!, schedule)
                }
                showAddScheduleDialog = false
                editingScheduleItem = null
                editingScheduleIndex = null
                Toast.makeText(context, "Jadwal pelajaran disimpan!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// 1. Tab Manajemen Siswa
@Composable
private fun AdminStudentsTab(
    students: List<Student>,
    onEdit: (Student) -> Unit,
    onDelete: (Int) -> Unit,
    onAdd: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students
        else students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.nickname.contains(searchQuery, ignoreCase = true) ||
            it.id.toString() == searchQuery.trim()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari siswa / absen...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { student ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudentPhotoAvatar(student = student, size = 44.dp)

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("#${student.id}", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 12.sp)
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(
                                text = "${student.role} • PIN: ${student.pin} • ${student.dreamSchool}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { onEdit(student) }, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { onDelete(student.id) }, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = CoralRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// 2. Tab Jadwal Pelajaran
@Composable
private fun AdminScheduleTab(
    schedules: List<LessonSchedule>,
    onAdd: () -> Unit,
    onEdit: (Int, LessonSchedule) -> Unit,
    onDelete: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total ${schedules.size} Jadwal Pelajaran", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah Mapel")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(schedules) { index, schedule ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = schedule.dayName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(schedule.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${schedule.timeSlot} • ${schedule.teacher} (${schedule.room})", fontSize = 11.sp, color = TextSecondary)
                        }

                        IconButton(onClick = { onEdit(index, schedule) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { onDelete(index) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = CoralRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// 3. Tab Jadwal Piket
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AdminPicketTab(
    picketSchedule: List<PicketDuty>,
    students: List<Student>,
    onUpdate: (String, List<Int>) -> Unit,
    onReset: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(picketSchedule) { duty ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(duty.dayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${duty.studentIds.size} Petugas Piket Terdaftar",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = { onReset(duty.dayName) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Piket", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of student chips in this picket
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    duty.studentIds.forEach { id ->
                        val student = students.find { it.id == id }
                        Surface(
                            color = if (isGlassDark()) Color(0xFF334155) else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#$id ${student?.nickname ?: ""}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. Tab Keuangan Kas
@Composable
private fun AdminCashTab(
    nominalKas: Long,
    transactions: List<CashTransaction>,
    onUpdateNominal: (Long) -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    var nominalInput by remember { mutableStateOf(nominalKas.toString()) }

    Column(modifier = Modifier.fillMaxSize()) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Tarif Iuran Kas Harian", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = { nominalInput = it },
                    prefix = { Text("Rp ") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val amount = nominalInput.toLongOrNull() ?: 2000L
                        onUpdateNominal(amount)
                    }
                ) {
                    Text("Simpan")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Buku Catatan Transaksi Kas (${transactions.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions, key = { it.id }) { tx ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (tx.type == CashType.PEMASUKAN) EmeraldGreen.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Icon(
                                if (tx.type == CashType.PEMASUKAN) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (tx.type == CashType.PEMASUKAN) EmeraldGreen else CoralRed,
                                modifier = Modifier.padding(6.dp).size(16.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.category, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${tx.date} • ${tx.recordedBy}", fontSize = 10.sp, color = TextSecondary)
                        }

                        Text(
                            text = "Rp ${java.text.NumberFormat.getIntegerInstance(java.util.Locale.GERMAN).format(tx.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (tx.type == CashType.PEMASUKAN) EmeraldGreen else CoralRed
                        )

                        IconButton(onClick = { onDeleteTransaction(tx.id) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = CoralRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// 5. Tab Pengaturan Kelas & PIN Admin
@Composable
private fun AdminSettingsTab(
    settings: ClassSettings,
    onSave: (ClassSettings) -> Unit
) {
    var className by remember { mutableStateOf(settings.className) }
    var academicYear by remember { mutableStateOf(settings.academicYear) }
    var teacherName by remember { mutableStateOf(settings.homeroomTeacher) }
    var teacherSubject by remember { mutableStateOf(settings.homeroomTeacherSubject) }
    var classMotto by remember { mutableStateOf(settings.classMotto) }
    var announcement by remember { mutableStateOf(settings.announcement) }
    var adminPin by remember { mutableStateOf(settings.adminPin) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(shape = RoundedCornerShape(16.dp)) {
                Text("Identitas Kelas & Wali Kelas", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Nama Kelas") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text("Tahun Ajaran") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Nama Wali Kelas") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = teacherSubject,
                    onValueChange = { teacherSubject = it },
                    label = { Text("Mata Pelajaran Wali Kelas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            GlassCard(shape = RoundedCornerShape(16.dp)) {
                Text("Motto & Pengumuman Berjalan", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = classMotto,
                    onValueChange = { classMotto = it },
                    label = { Text("Slogan / Motto Kelas IX-H") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = announcement,
                    onValueChange = { announcement = it },
                    label = { Text("Teks Pengumuman Papan Informasi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }

        item {
            GlassCard(shape = RoundedCornerShape(16.dp)) {
                Text("Keamanan: PIN Login Admin Kelas", fontWeight = FontWeight.Bold, color = CoralRed)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = adminPin,
                    onValueChange = { adminPin = it },
                    label = { Text("Kode PIN Admin (Kerahasiaan Tinggi)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        item {
            Button(
                onClick = {
                    onSave(
                        settings.copy(
                            className = className.trim(),
                            academicYear = academicYear.trim(),
                            homeroomTeacher = teacherName.trim(),
                            homeroomTeacherSubject = teacherSubject.trim(),
                            classMotto = classMotto.trim(),
                            announcement = announcement.trim(),
                            adminPin = adminPin.trim()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Semua Pengaturan")
            }
        }
    }
}

// Sub-Dialog Edit Siswa (Lengkap dengan Foto URL dan Bio)
@Composable
private fun EditStudentDialog(
    student: Student,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var idInput by remember { mutableStateOf(student.id.toString()) }
    var name by remember { mutableStateOf(student.name) }
    var nickname by remember { mutableStateOf(student.nickname) }
    var gender by remember { mutableStateOf(student.gender) }
    var role by remember { mutableStateOf(student.role) }
    var pin by remember { mutableStateOf(student.pin) }
    var dreamSchool by remember { mutableStateOf(student.dreamSchool) }
    var photoUrl by remember { mutableStateOf(student.photoUrl) }
    var bio by remember { mutableStateOf(student.bio) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = if (isGlassDark()) GlassDarkCard else Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Tambah Siswa Baru" else "Edit Profil Siswa #${student.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Photo preview and URL
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudentPhotoAvatar(
                        student = student.copy(photoUrl = photoUrl, gender = gender, name = name.ifEmpty { "Siswa" }),
                        size = 56.dp
                    )
                    OutlinedTextField(
                        value = photoUrl,
                        onValueChange = { photoUrl = it },
                        label = { Text("URL Foto Profil Siswa") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = idInput,
                        onValueChange = { idInput = it },
                        label = { Text("Absen #") },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Panggilan") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Jabatan / Role") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("PIN Presensi") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dreamSchool,
                        onValueChange = { dreamSchool = it },
                        label = { Text("Target SMA/SMK") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Cita-cita & Profesi Impian") },
                    placeholder = { Text("Contoh: Dokter Spesialis, Software Engineer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val id = idInput.toIntOrNull() ?: student.id
                            onSave(
                                student.copy(
                                    id = id,
                                    name = name.trim(),
                                    nickname = nickname.trim(),
                                    gender = gender,
                                    role = role.trim(),
                                    pin = pin.trim(),
                                    dreamSchool = dreamSchool.trim(),
                                    photoUrl = photoUrl.trim(),
                                    bio = bio.trim()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Sub-Dialog Edit / Tambah Jadwal Pelajaran
@Composable
private fun EditScheduleDialog(
    initial: LessonSchedule,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (LessonSchedule) -> Unit
) {
    var dayName by remember { mutableStateOf(initial.dayName) }
    var timeSlot by remember { mutableStateOf(initial.timeSlot) }
    var subject by remember { mutableStateOf(initial.subject) }
    var teacher by remember { mutableStateOf(initial.teacher) }
    var room by remember { mutableStateOf(initial.room) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isGlassDark()) GlassDarkCard else Color.White)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isNew) "Tambah Jadwal Pelajaran" else "Edit Jadwal Pelajaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = dayName,
                    onValueChange = { dayName = it },
                    label = { Text("Hari (Senin / Selasa / dst)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Jam (Contoh: 07:00 - 08:30)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Mata Pelajaran") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Guru Pengampu") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Ruang Kelas") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            onSave(
                                LessonSchedule(
                                    dayName = dayName.trim(),
                                    timeSlot = timeSlot.trim(),
                                    subject = subject.trim(),
                                    teacher = teacher.trim(),
                                    room = room.trim()
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCloudTab(viewModel: ClassViewModel) {
    val context = LocalContext.current
    val cloudManager = remember { CloudSyncManager.instance }
    val syncStatus by cloudManager.syncStatus.collectAsState()
    val statusMessage by cloudManager.statusMessage.collectAsState()
    val lastSyncedTime by cloudManager.lastSyncedTime.collectAsState()
    val autoSyncEnabled by cloudManager.autoSyncEnabled.collectAsState()

    var inputProjectId by remember {
        mutableStateOf(
            if (cloudManager.projectId.value.isNotBlank()) cloudManager.projectId.value else CloudSyncManager.DEFAULT_PROJECT_ID
        )
    }
    var inputApiKey by remember { mutableStateOf(cloudManager.apiKey.value) }
    var isConnecting by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            val statusColor = when (syncStatus) {
                CloudSyncStatus.ONLINE -> EmeraldGreen
                CloudSyncStatus.SYNCING, CloudSyncStatus.CONNECTING -> PrimaryBlue
                CloudSyncStatus.ERROR -> CoralRed
                CloudSyncStatus.OFFLINE -> TextSecondary
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = statusColor.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = "Status: ${syncStatus.label}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        if (lastSyncedTime != null) {
                            Text(
                                text = "Sync: $lastSyncedTime",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Sync Real-time",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Dengarkan update data secara otomatis",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = { isChecked ->
                            cloudManager.setAutoSync(isChecked, viewModel.repository)
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = "Aksi Sinkronisasi Database",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        isUploading = true
                        cloudManager.uploadAllToCloud(viewModel.repository) { success, msg ->
                            isUploading = false
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isUploading && syncStatus.isOnline,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mengunggah...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload ke Cloud", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = {
                        cloudManager.attachRealtimeListeners(viewModel.repository)
                        Toast.makeText(context, "Menyinkronkan data terbaru dari Cloud...", Toast.LENGTH_SHORT).show()
                    },
                    enabled = syncStatus.isOnline,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tarik dari Cloud", fontSize = 12.sp)
                }
            }
        }

        item {
            Text(
                text = "Koneksi Firebase Cloud",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = inputProjectId,
                onValueChange = { inputProjectId = it },
                label = { Text("Firebase Project ID") },
                placeholder = { Text("contoh: kelas-ixh-hub") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = inputApiKey,
                onValueChange = { inputApiKey = it },
                label = { Text("Web / Android API Key") },
                placeholder = { Text("AIzaSy...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (inputProjectId.isBlank()) {
                            Toast.makeText(context, "Harap masukkan Firebase Project ID!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isConnecting = true
                        cloudManager.connectWithCustomCredentials(
                            context = context,
                            newProjectId = inputProjectId.trim(),
                            newApiKey = inputApiKey.trim(),
                            repository = viewModel.repository
                        ) { success, msg ->
                            isConnecting = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isConnecting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Menghubungkan...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hubungkan Cloud", fontSize = 12.sp)
                    }
                }

                if (syncStatus.isOnline) {
                    OutlinedButton(
                        onClick = {
                            cloudManager.disconnect()
                            Toast.makeText(context, "Kembali ke mode offline.", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed)
                    ) {
                        Text("Putuskan", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ℹ️ Fitur Online Real-time Multi-Device:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Saat online, setiap update (keuangan kas, target cita-cita siswa, presensi, jadwal) langsung disiarkan ke seluruh HP siswa lain secara instan via Firebase Firestore.\n" +
                               "• Jika sedang offline/tidak ada internet, aplikasi tetap bekerja lancar dan dapat diunggah nanti saat kembali online.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
