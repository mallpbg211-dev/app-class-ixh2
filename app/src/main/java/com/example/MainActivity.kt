package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.data.CloudSyncManager
import com.example.data.CloudSyncStatus
import com.example.data.UserRole
import com.example.ui.AppNavTab
import com.example.ui.ClassViewModel
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        IXHHubApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IXHHubApp(viewModel: ClassViewModel = viewModel()) {
  val context = LocalContext.current
  val cloudManager = remember { CloudSyncManager.instance }
  val syncStatus by cloudManager.syncStatus.collectAsState()

  LaunchedEffect(Unit) {
    cloudManager.init(context, viewModel.repository)
  }

  val currentTab by viewModel.currentTab.collectAsState()
  val currentRole by viewModel.currentRole.collectAsState()
  val activeModal by viewModel.activeModal.collectAsState()
  val activeGameId by viewModel.activeGameId.collectAsState()

  var showRoleDialog by remember { mutableStateOf(false) }
  var showAdminControlCenter by remember { mutableStateOf(false) }
  var showCloudSyncDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      if (activeGameId == null) {
        TopAppBar(
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "IX-H",
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }

              Text(
                text = when (currentTab) {
                  AppNavTab.PROFIL -> "Profil"
                  AppNavTab.JADWAL -> "Jadwal"
                  AppNavTab.KAS -> "Kas Kelas"
                  AppNavTab.ABSENSI -> "Presensi"
                  AppNavTab.GAME_HUB -> "Game Hub"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          },
          actions = {
            // Cloud Online Status & Sync Button
            Surface(
              onClick = { showCloudSyncDialog = true },
              shape = RoundedCornerShape(14.dp),
              color = when (syncStatus) {
                CloudSyncStatus.ONLINE -> EmeraldGreen.copy(alpha = 0.15f)
                CloudSyncStatus.SYNCING, CloudSyncStatus.CONNECTING -> PrimaryBlue.copy(alpha = 0.15f)
                CloudSyncStatus.ERROR -> CoralRed.copy(alpha = 0.15f)
                CloudSyncStatus.OFFLINE -> MaterialTheme.colorScheme.surfaceVariant
              },
              modifier = Modifier
                .padding(end = 4.dp)
                .testTag("topbar_cloud_sync_btn")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Icon(
                  imageVector = when (syncStatus) {
                    CloudSyncStatus.ONLINE -> Icons.Default.CloudDone
                    CloudSyncStatus.SYNCING, CloudSyncStatus.CONNECTING -> Icons.Default.CloudSync
                    CloudSyncStatus.ERROR -> Icons.Default.CloudOff
                    CloudSyncStatus.OFFLINE -> Icons.Default.CloudQueue
                  },
                  contentDescription = "Cloud Status",
                  tint = when (syncStatus) {
                    CloudSyncStatus.ONLINE -> EmeraldGreen
                    CloudSyncStatus.SYNCING, CloudSyncStatus.CONNECTING -> PrimaryBlue
                    CloudSyncStatus.ERROR -> CoralRed
                    CloudSyncStatus.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = when (syncStatus) {
                    CloudSyncStatus.ONLINE -> "Online"
                    CloudSyncStatus.SYNCING -> "Sync..."
                    CloudSyncStatus.CONNECTING -> "..."
                    CloudSyncStatus.ERROR -> "Error"
                    CloudSyncStatus.OFFLINE -> "Offline"
                  },
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = when (syncStatus) {
                    CloudSyncStatus.ONLINE -> EmeraldGreen
                    CloudSyncStatus.SYNCING, CloudSyncStatus.CONNECTING -> PrimaryBlue
                    CloudSyncStatus.ERROR -> CoralRed
                    CloudSyncStatus.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                  }
                )
              }
            }

            // Pusat Analitik & Statistik Button
            IconButton(
              onClick = { viewModel.openModal("ANALYTICS") },
              modifier = Modifier
                .size(36.dp)
                .testTag("topbar_analytics_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = "Pusat Analitik & Statistik",
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
              )
            }

            if (currentRole == UserRole.ADMIN) {
              IconButton(
                onClick = { showAdminControlCenter = true },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("topbar_admin_center_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = "Panel Master Admin",
                  tint = CoralRed,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Surface(
              onClick = { showRoleDialog = true },
              shape = RoundedCornerShape(16.dp),
              color = when (currentRole) {
                UserRole.ADMIN -> Color(0xFFFEE2E2)
                UserRole.PICKET -> Color(0xFFFEF3C7)
                UserRole.STUDENT -> Color(0xFFE0F2FE)
              },
              modifier = Modifier
                .padding(end = 8.dp)
                .testTag("role_switcher_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Icon(
                  imageVector = when (currentRole) {
                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    UserRole.PICKET -> Icons.Default.CleaningServices
                    UserRole.STUDENT -> Icons.Default.Person
                  },
                  contentDescription = null,
                  tint = when (currentRole) {
                    UserRole.ADMIN -> CoralRed
                    UserRole.PICKET -> Color(0xFFB45309)
                    UserRole.STUDENT -> PrimaryBlueDark
                  },
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = currentRole.label,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = when (currentRole) {
                    UserRole.ADMIN -> CoralRed
                    UserRole.PICKET -> Color(0xFFB45309)
                    UserRole.STUDENT -> PrimaryBlueDark
                  }
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (currentTab == AppNavTab.GAME_HUB) DarkSurface else MaterialTheme.colorScheme.surface,
            titleContentColor = if (currentTab == AppNavTab.GAME_HUB) Color.White else MaterialTheme.colorScheme.onSurface
          )
        )
      }
    },
    bottomBar = {
      if (activeGameId == null) {
        NavigationBar(
          containerColor = if (currentTab == AppNavTab.GAME_HUB) DarkSurface else MaterialTheme.colorScheme.surface,
          windowInsets = NavigationBarDefaults.windowInsets
        ) {
          NavigationBarItem(
            selected = currentTab == AppNavTab.PROFIL,
            onClick = { viewModel.selectTab(AppNavTab.PROFIL) },
            icon = { Icon(Icons.Default.School, contentDescription = "Profil") },
            label = { Text("Profil", fontSize = 11.sp) }
          )
          NavigationBarItem(
            selected = currentTab == AppNavTab.JADWAL,
            onClick = { viewModel.selectTab(AppNavTab.JADWAL) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Jadwal") },
            label = { Text("Jadwal", fontSize = 11.sp) }
          )
          NavigationBarItem(
            selected = currentTab == AppNavTab.KAS,
            onClick = { viewModel.selectTab(AppNavTab.KAS) },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Kas") },
            label = { Text("Kas", fontSize = 11.sp) }
          )
          NavigationBarItem(
            selected = currentTab == AppNavTab.ABSENSI,
            onClick = { viewModel.selectTab(AppNavTab.ABSENSI) },
            icon = { Icon(Icons.Default.FactCheck, contentDescription = "Absensi") },
            label = { Text("Absensi", fontSize = 11.sp) }
          )
          NavigationBarItem(
            selected = currentTab == AppNavTab.GAME_HUB,
            onClick = { viewModel.selectTab(AppNavTab.GAME_HUB) },
            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Games") },
            label = { Text("Game Hub", fontSize = 11.sp) }
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        AppNavTab.PROFIL -> ProfileScreen(viewModel = viewModel)
        AppNavTab.JADWAL -> ScheduleScreen(viewModel = viewModel)
        AppNavTab.KAS -> CashScreen(viewModel = viewModel)
        AppNavTab.ABSENSI -> AttendanceScreen(viewModel = viewModel)
        AppNavTab.GAME_HUB -> GameHubScreen(viewModel = viewModel)
      }
    }
  }

  // Modals
  when (activeModal) {
    "SEATING" -> SeatingPlanDialog(viewModel = viewModel, onDismiss = { viewModel.openModal(null) })
    "GALLERY" -> GalleryDialog(viewModel = viewModel, onDismiss = { viewModel.openModal(null) })
    "COUNTDOWN" -> CountdownDialog(viewModel = viewModel, onDismiss = { viewModel.openModal(null) })
    "SOCIAL_MAP" -> SocialMapDialog(onDismiss = { viewModel.openModal(null) })
    "EXPORT" -> ExportReportDialog(viewModel = viewModel, onDismiss = { viewModel.openModal(null) })
    "ADMIN_CENTER" -> AdminControlCenterDialog(viewModel = viewModel, onDismiss = { viewModel.openModal(null) })
    "CLOUD_SYNC" -> CloudSyncDialog(repository = viewModel.repository, onDismiss = { viewModel.openModal(null) })
    "ANALYTICS" -> AnalyticsDialog(viewModel = viewModel, onDismiss = { viewModel.openModal(null) })
  }

  if (showCloudSyncDialog) {
    CloudSyncDialog(repository = viewModel.repository, onDismiss = { showCloudSyncDialog = false })
  }

  if (showAdminControlCenter) {
    AdminControlCenterDialog(viewModel = viewModel, onDismiss = { showAdminControlCenter = false })
  }

  if (showRoleDialog) {
    RoleSwitcherDialog(viewModel = viewModel, onDismiss = { showRoleDialog = false })
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
