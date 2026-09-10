package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CloudSyncManager
import com.example.data.Student
import com.example.data.UserRole
import com.example.ui.ClassViewModel
import com.example.ui.dialogs.AdminControlCenterDialog
import com.example.ui.dialogs.StudentDetailDialog
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ClassViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val classSettings by viewModel.classSettings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") } // Semua, Pengurus, Siswa L, Siswa P
    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showDreamDialog by remember { mutableStateOf(false) }
    var showAdminCenter by remember { mutableStateOf(false) }

    val filteredStudents = remember(students, searchQuery, selectedFilter) {
        students.filter { student ->
            val matchQuery = student.name.contains(searchQuery, ignoreCase = true) ||
                    student.nickname.contains(searchQuery, ignoreCase = true) ||
                    student.role.contains(searchQuery, ignoreCase = true) ||
                    student.id.toString() == searchQuery.trim()

            val matchFilter = when (selectedFilter) {
                "Pengurus" -> student.role != "Anggota"
                "Siswa L" -> student.gender == "L"
                "Siswa P" -> student.gender == "P"
                else -> true
            }

            matchQuery && matchFilter
        }
    }

    GlassBackgroundBox(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glass Hero Card Kelas IX-H
            item {
                ClassHeroBanner(
                    viewModel = viewModel,
                    onOpenAdmin = { showAdminCenter = true }
                )
            }

            // Quick Feature Shortcut Grid (Glass aesthetic)
            item {
                QuickActionsGrid(viewModel = viewModel)
            }

            // Sambutan Wali Kelas (Glassmorphic)
            item {
                TeacherWelcomeCard(
                    teacherName = classSettings.homeroomTeacher,
                    teacherSubject = classSettings.homeroomTeacherSubject
                )
            }

            // Visi & Misi Kelas IX-H (Glassmorphic)
            item {
                VisionMissionCard()
            }

            // Struktur Pengurus Inti (Glassmorphic)
            item {
                ClassStructureCard(
                    students = students,
                    onStudentClick = { selectedStudentForDetail = it }
                )
            }

            // Section Title: Daftar 32 Siswa dengan Foto
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "32 Profil Siswa IX-H",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Surface(
                                    color = PrimaryBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "HD Photos",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                            Text(
                                text = "Ketuk siswa untuk melihat Kartu Pelajar Digital & Foto Lengkap",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${filteredStudents.size}/32 Siswa",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Glass Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_search_input"),
                        placeholder = { Text("Cari nama, panggilan, absen, jabatan...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = PrimaryBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Semua", "Pengurus", "Siswa L", "Siswa P").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Students List with HD Photos and Glassmorphism styling
            items(filteredStudents, key = { it.id }) { student ->
                StudentItemCard(
                    student = student,
                    showPin = currentRole == UserRole.ADMIN || currentRole == UserRole.PICKET,
                    isAdmin = currentRole == UserRole.ADMIN,
                    onClick = { selectedStudentForDetail = student },
                    onEditDream = {
                        editingStudent = student
                        showDreamDialog = true
                    }
                )
            }
        }
    }

    // Modal: Detail Kartu Pelajar Siswa (dengan foto besar & biodata)
    if (selectedStudentForDetail != null) {
        StudentDetailDialog(
            student = selectedStudentForDetail!!,
            viewModel = viewModel,
            onDismiss = { selectedStudentForDetail = null },
            onEditStudent = {
                val s = selectedStudentForDetail
                selectedStudentForDetail = null
                if (s != null) {
                    showAdminCenter = true
                }
            }
        )
    }

    // Modal: Admin Master Control Center
    if (showAdminCenter) {
        AdminControlCenterDialog(
            viewModel = viewModel,
            onDismiss = { showAdminCenter = false }
        )
    }

    // Dialog Edit Target SMA/SMK
    if (showDreamDialog && editingStudent != null) {
        var dreamText by remember { mutableStateOf(editingStudent!!.dreamSchool) }

        AlertDialog(
            onDismissRequest = { showDreamDialog = false },
            title = { Text("Target SMA / SMK Impian") },
            text = {
                Column {
                    Text(
                        text = "Untuk: ${editingStudent!!.name} (#${editingStudent!!.id})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dreamText,
                        onValueChange = { dreamText = it },
                        label = { Text("Nama Sekolah & Jurusan") },
                        placeholder = { Text("Contoh: SMAN 1 (MIPA) / SMKN 2 (RPL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStudentDream(editingStudent!!.id, dreamText.trim())
                        showDreamDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDreamDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ClassHeroBanner(
    viewModel: ClassViewModel,
    onOpenAdmin: () -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val classSettings by viewModel.classSettings.collectAsState()

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("class_hero_card"),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F172A).copy(alpha = 0.85f),
                            Color(0xFF1E293B).copy(alpha = 0.85f),
                            Color(0xFF1E3A8A).copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = AccentGold.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGoldLight)
                    ) {
                        Text(
                            text = "👑 ${classSettings.className.uppercase()}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentGoldLight
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "T.A. ${classSettings.academicYear}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${classSettings.className} Hub",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "\"${classSettings.classMotto}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(label = "Siswa", value = "32 Siswa", icon = Icons.Default.Groups)
                    StatBadge(label = "Wali Kelas", value = classSettings.homeroomTeacher.split(" ").take(2).joinToString(" "), icon = Icons.Default.School)
                    StatBadge(label = "Ruang", value = "R. IX-H", icon = Icons.Default.MeetingRoom)
                }

                // Announcement banner if present
                if (classSettings.announcement.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = PrimaryBlue.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueLight.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📢", fontSize = 14.sp)
                            Text(
                                text = classSettings.announcement,
                                fontSize = 11.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Master Admin Fast Button
                if (currentRole == UserRole.ADMIN) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOpenAdmin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralRed.copy(alpha = 0.9f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Panel Master Pengaturan Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color.White.copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryBlueLight,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(text = label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(viewModel: ClassViewModel) {
    val cloudManager = remember { CloudSyncManager.instance }
    val syncStatus by cloudManager.syncStatus.collectAsState()
    val isOnline = syncStatus.isOnline

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                title = "Denah Duduk",
                icon = Icons.Default.GridOn,
                containerColor = Color(0xFF0284C7).copy(alpha = 0.15f),
                contentColor = PrimaryBlueDark,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.openModal("SEATING") }
            )
            QuickActionButton(
                title = "Galeri & Berita",
                icon = Icons.Default.PhotoLibrary,
                containerColor = Color(0xFFF59E0B).copy(alpha = 0.15f),
                contentColor = Color(0xFFB45309),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.openModal("GALLERY") }
            )
            QuickActionButton(
                title = "Countdown",
                icon = Icons.Default.Timer,
                containerColor = PurpleMystery.copy(alpha = 0.15f),
                contentColor = PurpleMystery,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.openModal("COUNTDOWN") }
            )
            QuickActionButton(
                title = "Medsos & Peta",
                icon = Icons.Default.Share,
                containerColor = EmeraldGreen.copy(alpha = 0.15f),
                contentColor = Color(0xFF15803D),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.openModal("SOCIAL_MAP") }
            )
        }

        // Analytics & Cloud Quick Banners
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.openModal("ANALYTICS") }
                    .testTag("quick_analytics_banner"),
                shape = RoundedCornerShape(14.dp),
                color = PurpleMystery.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleMystery.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = PurpleMystery,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "📊 Pusat Analitik",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleMystery
                        )
                        Text(
                            text = "Statistik Kas & Siswa",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.openModal("CLOUD_SYNC") }
                    .testTag("quick_cloud_sync_banner"),
                shape = RoundedCornerShape(14.dp),
                color = if (isOnline) EmeraldGreen.copy(alpha = 0.12f) else PrimaryBlue.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOnline) EmeraldGreen.copy(alpha = 0.4f) else PrimaryBlue.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = if (isOnline) EmeraldGreen else PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isOnline) "🟢 Cloud Sync" else "☁️ Cloud Offline",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOnline) "Live Terhubung" else "Ketuk Hubungkan",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderBrush())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TeacherWelcomeCard(
    teacherName: String,
    teacherSubject: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PrimaryBlue, AccentGold))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👩‍🏫", fontSize = 28.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Wali Kelas IX-H",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = teacherName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Guru Pengampu $teacherSubject",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "“Selamat datang di rumah belajar kelas IX-H tercinta! Tahun ini adalah tahun penentuan perjuangan kita di jenjang SMP. Mari kita satukan tekad, saling tolong-menolong, jaga kehormatan kelas, dan buktikan bahwa seluruh 32 siswa IX-H akan lulus dengan nilai gemilang dan lolos ke SMA/SMK impian!”",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun VisionMissionCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Flag, contentDescription = null, tint = AccentGold)
            Text(
                text = "Visi & Misi Kelas IX-H",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "🎯 Visi:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Mewujudkan kelas IX-H yang berakhlak mulia, unggul dalam sains & teknologi, berjiwa gotong royong, dan 100% diterima di jenjang lanjutan idaman.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
        )

        Text(
            text = "🚀 Misi:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        val missions = listOf(
            "Menciptakan ruang kelas yang bersih, sehat, dan nyaman untuk belajar aktif.",
            "Membudayakan literasi, disiplin waktu, dan kejujuran dalam setiap evaluasi.",
            "Menjaga persaudaraan yang erat serta peduli terhadap sesama kawan kelas.",
            "Mendukung setiap potensi akademik maupun non-akademik siswa secara maksimal."
        )
        missions.forEachIndexed { idx, item ->
            Row(
                modifier = Modifier.padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "${idx + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = item, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ClassStructureCard(
    students: List<Student>,
    onStudentClick: (Student) -> Unit
) {
    val officers = students.filter { it.role != "Anggota" }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.AccountTree, contentDescription = null, tint = PrimaryBlue)
            Text(
                text = "Struktur Pengurus IX-H",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            officers.chunked(2).forEach { rowOfficers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowOfficers.forEach { officer ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStudentClick(officer) },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderBrush())
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StudentPhotoAvatar(student = officer, size = 36.dp)
                                Column {
                                    Text(
                                        text = officer.role,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                    Text(
                                        text = officer.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    if (rowOfficers.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentItemCard(
    student: Student,
    showPin: Boolean,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEditDream: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_item_${student.id}"),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Student Photo Avatar with Glass Border & Officer Badge
            StudentPhotoAvatar(student = student, size = 52.dp)

            // Student Name & Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${student.id}",
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue,
                        fontSize = 12.sp
                    )
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = "(${student.nickname})", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (student.role != "Anggota") {
                        Surface(
                            color = AccentGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⭐ ${student.role}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    Text(
                        text = "🎯 ${student.dreamSchool}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (student.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🚀 Cita-cita:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Text(
                            text = student.bio,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (showPin) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Kode PIN: ${student.pin}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            // Target SMA action button
            IconButton(
                onClick = onEditDream,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = "Ubah Target SMA/SMK",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
