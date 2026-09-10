package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LessonSchedule
import com.example.data.PicketDuty
import com.example.data.Student
import com.example.data.UserRole
import com.example.ui.ClassViewModel
import com.example.ui.theme.*

@Composable
fun ScheduleScreen(
    viewModel: ClassViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val picketSchedule by viewModel.picketSchedule.collectAsState()
    val students by viewModel.students.collectAsState()
    val lessonSchedules by viewModel.lessonSchedules.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Jadwal Pelajaran, 1: Jadwal Piket
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    val initialDay = remember {
        val cal = java.util.Calendar.getInstance()
        when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Senin"
            java.util.Calendar.TUESDAY -> "Selasa"
            java.util.Calendar.WEDNESDAY -> "Rabu"
            java.util.Calendar.THURSDAY -> "Kamis"
            java.util.Calendar.FRIDAY -> "Jumat"
            java.util.Calendar.SATURDAY -> "Sabtu"
            else -> "Senin"
        }
    }
    var selectedDay by remember { mutableStateOf(initialDay) }

    GlassBackgroundBox(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("schedule_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Tab Switcher
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Pelajaran", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                SegmentedButton(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Piket", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day Selector Chips (Glass aesthetic)
            ScrollableTabRow(
                selectedTabIndex = days.indexOf(selectedDay),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                days.forEach { day ->
                    val isSelected = selectedDay == day
                    Tab(
                        selected = isSelected,
                        onClick = { selectedDay = day },
                        text = {
                            Text(
                                text = day,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedSubTab == 0) {
                // Lesson Schedule for Selected Day
                val dayLessons = remember(lessonSchedules, selectedDay) {
                    lessonSchedules.filter { it.dayName.equals(selectedDay, ignoreCase = true) }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mata Pelajaran Hari $selectedDay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = PrimaryBlue.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${dayLessons.size} Sesi Belajar",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }

                    items(dayLessons) { lesson ->
                        LessonCard(lesson = lesson)
                    }
                }
            } else {
                // Picket for Selected Day
                val currentPicket = picketSchedule.find { it.dayName.equals(selectedDay, ignoreCase = true) }
                    ?: PicketDuty(selectedDay, emptyList(), isDoneToday = false)

                PicketTabContent(
                    picket = currentPicket,
                    students = students,
                    canEdit = currentRole == UserRole.ADMIN || currentRole == UserRole.PICKET,
                    onToggleTask = { taskName ->
                        viewModel.togglePicketTask(
                            selectedDay,
                            taskName,
                            if (currentRole == UserRole.ADMIN) "Admin IX-H" else "Piket Hari Ini"
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: LessonSchedule) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Time Slot Badge
            Surface(
                color = PrimaryBlue.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueLight.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.timeSlot.replace(" - ", "\n"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark,
                        lineHeight = 14.sp
                    )
                }
            }

            // Subject Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = lesson.teacher,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = lesson.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Room Badge
            Surface(
                color = AccentGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = lesson.room,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )
            }
        }
    }
}

@Composable
private fun PicketTabContent(
    picket: PicketDuty,
    students: List<Student>,
    canEdit: Boolean,
    onToggleTask: (String) -> Unit
) {
    val dutyStudents = remember(picket, students) {
        students.filter { picket.studentIds.contains(it.id) }
    }

    val standardTasks = listOf(
        "Menyapu & Mengepel Lantai Kelas",
        "Membersihkan & Menghapus Papan Tulis",
        "Merapikan Meja Guru & Taplak",
        "Membuang Sampah ke TPS Luar",
        "Menyiram Tanaman Depan Kelas IX-H",
        "Menutup Jendela & Mengunci Pintu Kelas"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Picket Status Header Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        if (picket.isDoneToday) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                        contentDescription = null,
                        tint = if (picket.isDoneToday) Color(0xFF15803D) else Color(0xFFB45309),
                        modifier = Modifier.size(32.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (picket.isDoneToday) "Piket Selesai Diverifikasi" else "Tugas Piket Sedang Berjalan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (picket.isDoneToday) Color(0xFF15803D) else Color(0xFFB45309)
                        )
                        Text(
                            text = if (picket.isDoneToday) "Diverifikasi oleh: ${picket.checkedBy}" else "Silakan centang tugas piket yang telah tuntas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Assigned Students with Photo Avatars
        item {
            Text(
                text = "Petugas Piket Hari ${picket.dayName} (${dutyStudents.size} Siswa)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dutyStudents.forEach { student ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderBrush()),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            StudentPhotoAvatar(student = student, size = 38.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = student.nickname,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "#${student.id}",
                                fontSize = 10.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Interactive Checklist
        item {
            Text(
                text = "Daftar Tugas Kebersihan & Kerapian",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(standardTasks) { task ->
            val isChecked = picket.tasksCompleted.contains(task)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { if (canEdit) onToggleTask(task) },
                        enabled = canEdit
                    )
                    Text(
                        text = task,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isChecked) MaterialTheme.colorScheme.onSurface else TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (!canEdit) {
            item {
                Text(
                    text = "*Masuk sebagai Piket/Ketua atau Admin untuk mencentang tugas piket.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
