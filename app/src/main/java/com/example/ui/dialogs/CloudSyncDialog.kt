package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ClassRepository
import com.example.data.CloudSyncManager
import com.example.data.CloudSyncStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncDialog(
    repository: ClassRepository,
    onDismiss: () -> Unit
) {
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("cloud_sync_dialog"),
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimaryBlue, PrimaryBlueDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Cloud Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cloud & Real-time Sync",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hubungkan data kelas IX-H multi-device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_cloud_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Banner
                    val bannerBg = when (syncStatus) {
                        CloudSyncStatus.ONLINE -> EmeraldGreen.copy(alpha = 0.12f)
                        CloudSyncStatus.SYNCING, CloudSyncStatus.CONNECTING -> PrimaryBlue.copy(alpha = 0.12f)
                        CloudSyncStatus.ERROR -> CoralRed.copy(alpha = 0.12f)
                        CloudSyncStatus.OFFLINE -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val statusColor = when (syncStatus) {
                        CloudSyncStatus.ONLINE -> EmeraldGreen
                        CloudSyncStatus.SYNCING, CloudSyncStatus.CONNECTING -> PrimaryBlue
                        CloudSyncStatus.ERROR -> CoralRed
                        CloudSyncStatus.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = bannerBg
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
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = syncStatus.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }

                                if (lastSyncedTime != null) {
                                    Text(
                                        text = "Sync: $lastSyncedTime",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                    // Firebase Rules Helper Card if access is denied or error
                    if (statusMessage.contains("PERMISSION_DENIED") || syncStatus == CloudSyncStatus.ERROR) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CoralRed.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = CoralRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Buka Izin Akses Firebase Rules",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralRed
                                    )
                                }

                                Text(
                                    text = "Firestore menolak akses karena tab Rules di Firebase Console masih terkunci. Salin kode rules di bawah, buka console lalu klik Publish:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "rules_version = '2';\nservice cloud.firestore {\n  match /databases/{database}/documents {\n    match /{document=**} {\n      allow read, write: if true;\n    }\n  }\n}",
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier = Modifier.padding(8.dp),
                                        color = CoralRed
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText(
                                                "Firestore Rules",
                                                "rules_version = '2';\nservice cloud.firestore {\n  match /databases/{database}/documents {\n    match /{document=**} {\n      allow read, write: if true;\n    }\n  }\n}"
                                            )
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Kode Rules berhasil disalin! Tempel di Firebase Console > Rules.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Salin Rules", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            cloudManager.attachRealtimeListeners(repository)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Coba Lagi", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Auto-sync toggle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Sync Real-time",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Dengarkan perubahan data seketika dari perangkat lain",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoSyncEnabled,
                                onCheckedChange = { isChecked ->
                                    cloudManager.setAutoSync(isChecked, repository)
                                },
                                modifier = Modifier.testTag("auto_sync_switch")
                            )
                        }
                    }

                    // Actions: Upload / Download Data
                    Text(
                        text = "Aksi Sinkronisasi Data",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isUploading = true
                                cloudManager.uploadAllToCloud(repository) { success, msg ->
                                    isUploading = false
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isUploading && syncStatus.isOnline,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_upload_cloud"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mengunggah...", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload ke Cloud", fontSize = 13.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                cloudManager.attachRealtimeListeners(repository)
                                Toast.makeText(context, "Menyinkronkan data terbaru dari Cloud...", Toast.LENGTH_SHORT).show()
                            },
                            enabled = syncStatus.isOnline,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_download_cloud"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tarik dari Cloud", fontSize = 13.sp)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Configuration Section
                    Text(
                        text = "Konfigurasi Firebase Project",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = inputProjectId,
                        onValueChange = { inputProjectId = it },
                        label = { Text("Firebase Project ID") },
                        placeholder = { Text("contoh: kelas-ixh-hub") },
                        leadingIcon = { Icon(Icons.Outlined.Dns, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_project_id"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        label = { Text("Web / Android API Key") },
                        placeholder = { Text("AIzaSy...") },
                        leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_api_key"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

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
                                    repository = repository
                                ) { success, msg ->
                                    isConnecting = false
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !isConnecting,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_connect_firebase"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Menghubungkan...")
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hubungkan Cloud")
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
                                Text("Putuskan")
                            }
                        }
                    }

                    // Guide Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💡", fontSize = 18.sp)
                                Text(
                                    text = "Panduan Real-time Online:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1. Buat project Firebase baru (100% gratis) di console.firebase.google.com.\n" +
                                       "2. Aktifkan Cloud Firestore dalam mode 'test' / buka izin read/write.\n" +
                                       "3. Masukkan Project ID dan API Key di atas, lalu klik 'Hubungkan Cloud'.\n" +
                                       "4. Alternatif: Tempatkan file google-services.json di direktori app/.\n" +
                                       "5. Klik 'Upload ke Cloud' agar 32 data siswa, jadwal, dan buku kas langsung tersimpan di Cloud dan bisa diakses seluruh siswa secara real-time!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
