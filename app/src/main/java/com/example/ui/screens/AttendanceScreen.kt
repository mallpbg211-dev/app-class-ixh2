package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.data.AttendanceStatus
import com.example.data.Student
import com.example.data.UserRole
import com.example.ui.ClassViewModel
import com.example.ui.theme.*

@Composable
fun AttendanceScreen(
    viewModel: ClassViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val students by viewModel.students.collectAsState()
    val attendanceMap by viewModel.attendance.collectAsState()
    val selectedDate by viewModel.selectedAttendanceDate.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }
    var isPinSuccess by remember { mutableStateOf(false) }

    var studentForNote by remember { mutableStateOf<Student?>(null) }
    var noteStatus by remember { mutableStateOf(AttendanceStatus.IZIN) }

    val todayRecords = remember(attendanceMap, selectedDate, students) {
        val records = attendanceMap[selectedDate] ?: students.map {
            AttendanceRecord(it.id, selectedDate, AttendanceStatus.HADIR)
        }
        val recordMap = records.associateBy { it.studentId }
        students.map { student ->
            recordMap[student.id] ?: AttendanceRecord(student.id, selectedDate, AttendanceStatus.HADIR)
        }
    }

    val hadirCount = todayRecords.count { it.status == AttendanceStatus.HADIR }
    val izinCount = todayRecords.count { it.status == AttendanceStatus.IZIN }
    val sakitCount = todayRecords.count { it.status == AttendanceStatus.SAKIT }
    val alpaCount = todayRecords.count { it.status == AttendanceStatus.ALPA }

    val canEdit = currentRole == UserRole.ADMIN || currentRole == UserRole.PICKET

    GlassBackgroundBox(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("attendance_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
        // Date & Role Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Presensi Harian",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openModal("ANALYTICS") },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("attendance_analytics_btn")
                            ) {
                                Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Analitik", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.openModal("EXPORT") },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rekap", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Status Stat Counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttendanceStatBadge(label = "Hadir", count = hadirCount, color = EmeraldGreen, modifier = Modifier.weight(1f))
                        AttendanceStatBadge(label = "Izin", count = izinCount, color = PrimaryBlue, modifier = Modifier.weight(1f))
                        AttendanceStatBadge(label = "Sakit", count = sakitCount, color = AccentGold, modifier = Modifier.weight(1f))
                        AttendanceStatBadge(label = "Alpa", count = alpaCount, color = CoralRed, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Fast PIN Input Card (The required workflow when admin inputs from paper notes)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_attendance_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Input Presensi Cepat via PIN Siswa",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Masukkan kode PIN 4 digit acak siswa (sesuai catatan piket dari sekolah) untuk menandai kehadiran instan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 6) pinInput = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("Contoh: 4821") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pin_input_field"),
                            singleLine = true,
                            enabled = canEdit
                        )

                        Button(
                            onClick = {
                                if (pinInput.isNotBlank()) {
                                    val (success, msg) = viewModel.submitPinAttendance(pinInput.trim())
                                    isPinSuccess = success
                                    pinMessage = msg
                                    if (success) pinInput = ""
                                }
                            },
                            enabled = canEdit && pinInput.length >= 4,
                            modifier = Modifier.testTag("submit_pin_button")
                        ) {
                            Text("Check-In")
                        }
                    }

                    // Result Message Banner
                    pinMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = if (isPinSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (isPinSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isPinSuccess) Color(0xFF15803D) else Color(0xFFB91C1C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isPinSuccess) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Student Attendance List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Presensi 32 Siswa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (!canEdit) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Mode Baca (Siswa)",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Students Items
        items(todayRecords, key = { it.studentId }) { record ->
            val student = students.find { it.id == record.studentId }
            if (student != null) {
                AttendanceStudentCard(
                    student = student,
                    record = record,
                    canEdit = canEdit,
                    onStatusChange = { newStatus ->
                        if (newStatus == AttendanceStatus.IZIN || newStatus == AttendanceStatus.SAKIT) {
                            studentForNote = student
                            noteStatus = newStatus
                        } else {
                            viewModel.updateAttendanceStatus(student.id, newStatus)
                        }
                    }
                )
            }
        }
    }

    // Dialog Note for Izin / Sakit
    if (studentForNote != null) {
        var reasonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { studentForNote = null },
            title = { Text("Keterangan ${noteStatus.label}") },
            text = {
                Column {
                    Text(
                        text = "Siswa: ${studentForNote!!.name} (#${studentForNote!!.id})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Alasan / Catatan") },
                        placeholder = { Text("Contoh: Surat Dokter / Keperluan Keluarga") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAttendanceStatus(
                            studentForNote!!.id,
                            noteStatus,
                            reasonText.trim()
                        )
                        studentForNote = null
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentForNote = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
}

@Composable
private fun AttendanceStatBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun AttendanceStudentCard(
    student: Student,
    record: AttendanceRecord,
    canEdit: Boolean,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Student Photo Avatar
                StudentPhotoAvatar(student = student, size = 44.dp)

                // Student name & note
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (record.notes.isNotBlank()) {
                        Text(
                            text = "Catatan: ${record.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "PIN: ${student.pin}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Current Status Badge
                val (badgeBg, badgeFg) = when (record.status) {
                    AttendanceStatus.HADIR -> Pair(Color(0xFFDCFCE7), EmeraldGreen)
                    AttendanceStatus.IZIN -> Pair(Color(0xFFE0F2FE), PrimaryBlue)
                    AttendanceStatus.SAKIT -> Pair(Color(0xFFFEF3C7), AccentGold)
                    AttendanceStatus.ALPA -> Pair(Color(0xFFFEE2E2), CoralRed)
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = record.status.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg
                    )
                }
            }

            // Quick Status Chips for Admin / Picket
            if (canEdit) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AttendanceStatus.values().forEach { status ->
                        val isSelected = record.status == status
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onStatusChange(status) },
                            color = if (isSelected) {
                                when (status) {
                                    AttendanceStatus.HADIR -> EmeraldGreen
                                    AttendanceStatus.IZIN -> PrimaryBlue
                                    AttendanceStatus.SAKIT -> AccentGold
                                    AttendanceStatus.ALPA -> CoralRed
                                }
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            }
                        ) {
                            Text(
                                text = status.shortCode + " (" + status.label + ")",
                                modifier = Modifier.padding(vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
