package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.example.data.*
import com.example.ui.ClassViewModel
import com.example.ui.theme.*

@Composable
fun StudentDetailDialog(
    student: Student,
    viewModel: ClassViewModel,
    onDismiss: () -> Unit,
    onEditStudent: (() -> Unit)? = null
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val attendanceMap by viewModel.attendance.collectAsState()
    val dailyCashMap by viewModel.dailyStudentCash.collectAsState()
    val todayCashDate by viewModel.selectedCashDate.collectAsState()
    val todayAttendanceDate by viewModel.selectedAttendanceDate.collectAsState()

    val todayAttendance = attendanceMap[todayAttendanceDate]?.find { it.studentId == student.id }
    val todayCash = dailyCashMap[todayCashDate]?.find { it.studentId == student.id }

    val isOfficer = student.role != "Anggota"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isGlassDark()) GlassDarkCard else Color(0xFFFFFFFF).copy(alpha = 0.95f)
            ),
            border = BorderStroke(1.5.dp, glassBorderBrush())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "KARTU PELAJAR DIGITAL IX-H",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-resolution Student Photo Frame with Holographic / Specular Glass Border
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(
                            BorderStroke(
                                3.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF818CF8),
                                        Color(0xFFEC4899),
                                        Color(0xFFFBBF24),
                                        Color(0xFF38BDF8)
                                    )
                                )
                            ),
                            CircleShape
                        )
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (student.photoUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = student.photoUrl,
                            contentDescription = "Foto ${student.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (student.gender == "L") PrimaryBlueLight else Color(0xFFF472B6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = student.avatarEmoji, fontSize = 40.sp)
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (student.gender == "L") PrimaryBlue else Color(0xFFDB2777)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.nickname.take(2).uppercase(),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (student.gender == "L") PrimaryBlue else Color(0xFFDB2777)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.nickname.take(2).uppercase(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Student Name & Nickname
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Panggilan: \"${student.nickname}\" • Absen #${student.id} (${if (student.gender == "L") "Laki-laki" else "Perempuan"})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Badge
                if (isOfficer) {
                    Surface(
                        color = AccentGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AccentGoldLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "⭐ ${student.role}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cita-cita & Profesi Impian Card
                if (student.bio.isNotBlank()) {
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = if (isGlassDark()) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFF1F5F9).copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🚀", fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cita-cita & Profesi Impian",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = student.bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Grid of 2 Badges: Attendance Today & Cash Status Today
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Attendance Badge
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = when (todayAttendance?.status) {
                            AttendanceStatus.HADIR -> EmeraldGreen.copy(alpha = 0.15f)
                            AttendanceStatus.SAKIT -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                            AttendanceStatus.IZIN -> PrimaryBlue.copy(alpha = 0.15f)
                            else -> CoralRed.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Presensi Hari Ini", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = todayAttendance?.status?.label ?: "HADIR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = when (todayAttendance?.status) {
                                    AttendanceStatus.HADIR -> EmeraldGreen
                                    AttendanceStatus.SAKIT -> Color(0xFFB45309)
                                    AttendanceStatus.IZIN -> PrimaryBlueDark
                                    else -> CoralRed
                                }
                            )
                        }
                    }

                    // Cash Status Badge
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = if (todayCash?.isPaid == true) EmeraldGreen.copy(alpha = 0.15f) else CoralRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Status Kas Hari Ini", fontSize = 10.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (todayCash?.isPaid == true) "Lunas (Rp 2.000)" else "Belum Bayar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (todayCash?.isPaid == true) EmeraldGreen else CoralRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dream School & PIN
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Target Lanjutan:", fontSize = 10.sp, color = TextSecondary)
                            Text(student.dreamSchool, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        if (currentRole == UserRole.ADMIN || currentRole == UserRole.PICKET) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PIN Presensi:", fontSize = 10.sp, color = TextSecondary)
                                Text(student.pin, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = PrimaryBlue)
                            }
                        }
                    }
                }

                // Admin Action Button if logged in as Admin
                if (currentRole == UserRole.ADMIN && onEditStudent != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onEditStudent,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Seluruh Data Siswa Ini (Admin)")
                    }
                }
            }
        }
    }
}
