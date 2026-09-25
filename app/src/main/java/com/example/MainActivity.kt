package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
<<<<<<< HEAD
import androidx.compose.material.icons.automirrored.filled.Logout
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
<<<<<<< HEAD
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import kotlinx.coroutines.launch
=======
import com.example.ui.components.NotificationsSheet
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

class MainActivity : ComponentActivity() {
    private val viewModel: MedTimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
<<<<<<< HEAD
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
=======
            MyApplicationTheme {
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                MedTimeRoot(viewModel = viewModel)
            }
        }
    }
}

enum class EntryFlow {
    ACCOUNT_TYPE_SELECTION,
    LOGIN,
    GUEST_DEMO
}

@Composable
fun MedTimeRoot(viewModel: MedTimeViewModel) {
    val context = LocalContext.current
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val userFeedback by viewModel.userFeedback.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

<<<<<<< HEAD
    var entryFlow by remember { mutableStateOf(EntryFlow.GUEST_DEMO) }
=======
    var entryFlow by remember { mutableStateOf(EntryFlow.ACCOUNT_TYPE_SELECTION) }
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    var activeRoleForAuth by remember { mutableStateOf("PATIENT") }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.rescheduleAllReminders()
        }
    }

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
<<<<<<< HEAD
            // When unauthenticated, default to instant guest reminder mode without forced login
            entryFlow = EntryFlow.GUEST_DEMO
=======
            // When unauthenticated, always reset to the Account Type Selection screen
            entryFlow = EntryFlow.ACCOUNT_TYPE_SELECTION
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } catch (e: Throwable) {
                android.util.Log.e("MainActivity", "Notification permission launch failed: ${e.message}")
            }
        }
    }

    LaunchedEffect(userFeedback) {
        userFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (!isAuthenticated) {
            when (entryFlow) {
                EntryFlow.ACCOUNT_TYPE_SELECTION -> {
                    AccountTypeSelectionScreen(
                        onSelectPatient = {
                            activeRoleForAuth = "PATIENT"
                            entryFlow = EntryFlow.LOGIN
                        },
                        onSelectDoctor = {
                            activeRoleForAuth = "DOCTOR"
                            entryFlow = EntryFlow.LOGIN
                        },
                        onSelectCaretaker = {
                            activeRoleForAuth = "CARETAKER"
                            entryFlow = EntryFlow.LOGIN
                        },
                        onSelectAdmin = {
                            activeRoleForAuth = "ADMIN"
                            entryFlow = EntryFlow.LOGIN
                        },
                        onSelectGuest = {
                            entryFlow = EntryFlow.GUEST_DEMO
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
                EntryFlow.LOGIN -> {
                    AuthScreen(
                        viewModel = viewModel,
                        selectedRole = activeRoleForAuth,
                        onChangeAccountType = {
                            entryFlow = EntryFlow.ACCOUNT_TYPE_SELECTION
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
                EntryFlow.GUEST_DEMO -> {
                    GuestDemoScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = { role ->
                            activeRoleForAuth = role ?: "PATIENT"
                            entryFlow = EntryFlow.LOGIN
                        },
                        onNavigateToSignUp = { role ->
                            activeRoleForAuth = role ?: "PATIENT"
                            entryFlow = EntryFlow.LOGIN
                        },
                        onExitDemo = {
                            entryFlow = EntryFlow.ACCOUNT_TYPE_SELECTION
                        },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        } else {
            MedTimeApp(
                viewModel = viewModel,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedTimeApp(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val unreadNotifCount by viewModel.unreadNotifCount.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val userRole = currentUser?.role ?: "PATIENT"
    var currentScreen by remember { mutableStateOf("dashboard") }
    var showNotificationsSheet by remember { mutableStateOf(false) }

<<<<<<< HEAD
    // Drawer state and dialogs
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSecurityPrivacyDialog by remember { mutableStateOf(false) }
    var showConnectWebsiteDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var showReminderSoundDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showAboutMedTimeDialog by remember { mutableStateOf(false) }

    // Caretaker Assistance System States
    var showCaretakerAssistanceChoiceModal by remember { mutableStateOf(false) }
    var showCallPatientRequestDialog by remember { mutableStateOf(false) }
    var showVisitPatientRequestDialog by remember { mutableStateOf(false) }
    var showCaretakerAssistanceTrackerSheet by remember { mutableStateOf(false) }

    val caretakerAssistanceRequests by viewModel.caretakerAssistanceRequests.collectAsState()
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    LaunchedEffect(userRole) {
        currentScreen = when (userRole) {
            "DOCTOR" -> "doctor_dashboard"
            "CARETAKER" -> "caretaker_dashboard"
=======
    LaunchedEffect(userRole) {
        currentScreen = when (userRole) {
            "DOCTOR" -> "doctor_queue"
            "CARETAKER" -> "caretakers"
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            "ADMIN" -> "admin"
            else -> "dashboard"
        }
    }

    if (showNotificationsSheet) {
        NotificationsSheet(
            notifications = notifications,
            onDismiss = { showNotificationsSheet = false },
            onMarkRead = { id -> viewModel.markNotificationRead(id) },
<<<<<<< HEAD
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onViewFullHistory = {
                currentScreen = when (userRole) {
                    "DOCTOR" -> "doctor_notifications"
                    "CARETAKER" -> "caretaker_notifications"
                    else -> "notification_history"
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MedError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out of MedTime?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to logout? You will return to the MedTime welcome screen and all active session credentials will be cleared.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError),
                    modifier = Modifier.testTag("confirm_drawer_logout_button")
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditProfileDialog && currentUser != null) {
        EditProfileDialog(
            user = currentUser!!,
            viewModel = viewModel,
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (showSecurityPrivacyDialog && currentUser != null) {
        SecurityPrivacyDialog(
            user = currentUser!!,
            viewModel = viewModel,
            onDismiss = { showSecurityPrivacyDialog = false }
        )
    }

    if (showConnectWebsiteDialog && currentUser != null) {
        ConnectWebsiteDialog(
            user = currentUser!!,
            viewModel = viewModel,
            onDismiss = { showConnectWebsiteDialog = false }
        )
    }

    if (showNotificationSettingsDialog) {
        NotificationSettingsDialog(
            onDismiss = { showNotificationSettingsDialog = false }
        )
    }

    if (showReminderSoundDialog) {
        ReminderSoundDialog(
            onDismiss = { showReminderSoundDialog = false }
        )
    }

    if (showHelpSupportDialog) {
        HelpSupportDialog(
            onDismiss = { showHelpSupportDialog = false }
        )
    }

    if (showAboutMedTimeDialog) {
        AboutMedTimeDialog(
            onDismiss = { showAboutMedTimeDialog = false }
        )
    }

    // Caretaker Assistance Dialogs & Flows
    if (showCaretakerAssistanceChoiceModal) {
        val activeCount = caretakerAssistanceRequests.count { it.status == "PENDING" || it.status == "ACCEPTED" || it.status == "CARETAKER_ON_THE_WAY" || it.status == "ARRIVED" }
        PatientAssistanceChoiceModal(
            onDismiss = { showCaretakerAssistanceChoiceModal = false },
            onSelectCallPatient = { showCallPatientRequestDialog = true },
            onSelectVisitPatient = { showVisitPatientRequestDialog = true },
            onViewActiveRequests = { showCaretakerAssistanceTrackerSheet = true },
            activeRequestsCount = activeCount
        )
    }

    if (showCallPatientRequestDialog) {
        CallPatientRequestDialog(
            onDismiss = { showCallPatientRequestDialog = false },
            caretakerLinks = caretakerLinks,
            allUsers = allUsers,
            onSubmit = { patientId, patientName, reason, notes, isEmergency, locationName, lat, lon ->
                viewModel.submitCallAssistanceRequest(
                    patientId = patientId,
                    patientName = patientName,
                    reason = reason,
                    notes = notes,
                    isEmergency = isEmergency,
                    caretakerLocationName = locationName,
                    lat = lat,
                    lon = lon
                )
            }
        )
    }

    if (showVisitPatientRequestDialog) {
        VisitPatientRequestDialog(
            onDismiss = { showVisitPatientRequestDialog = false },
            caretakerLinks = caretakerLinks,
            allUsers = allUsers,
            onSubmit = { patientId, patientName, reason, notes, isEmergency, distanceKm, baseCharge, additionalCharge, totalCharge, patientAddress, patientLat, patientLon, caretakerLocationName, caretakerLat, caretakerLon, selectedAddressId, addressSnapshot ->
                viewModel.submitVisitAssistanceRequest(
                    patientId = patientId,
                    patientName = patientName,
                    reason = reason,
                    notes = notes,
                    isEmergency = isEmergency,
                    distanceKm = distanceKm,
                    baseCharge = baseCharge,
                    additionalCharge = additionalCharge,
                    totalCharge = totalCharge,
                    patientAddress = addressSnapshot.ifBlank { patientAddress },
                    patientLat = patientLat,
                    patientLon = patientLon,
                    caretakerLocationName = caretakerLocationName,
                    caretakerLat = caretakerLat,
                    caretakerLon = caretakerLon
                )
            }
        )
    }

    if (showCaretakerAssistanceTrackerSheet) {
        CaretakerAssistanceTrackerSheet(
            onDismiss = { showCaretakerAssistanceTrackerSheet = false },
            requests = caretakerAssistanceRequests,
            onUpdateProgress = { reqId, status ->
                viewModel.caretakerUpdateVisitProgress(reqId, status)
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = userRole == "PATIENT" || userRole == "DOCTOR" || userRole == "CARETAKER",
        drawerContent = {
            when (userRole) {
                "DOCTOR" -> {
                    DoctorSideDrawerSheet(
                        currentUser = currentUser,
                        currentRoute = currentScreen,
                        unreadNotificationCount = unreadNotifCount,
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            currentScreen = route
                        },
                        onOpenProfile = {
                            scope.launch { drawerState.close() }
                            currentScreen = "doctor_profile"
                        },
                        onOpenSecurity = {
                            scope.launch { drawerState.close() }
                            currentScreen = "doctor_security"
                        },
                        onOpenHelpSupport = {
                            scope.launch { drawerState.close() }
                            showHelpSupportDialog = true
                        },
                        onLogoutRequest = {
                            scope.launch { drawerState.close() }
                            showLogoutConfirmDialog = true
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
                "CARETAKER" -> {
                    CaretakerSideDrawerSheet(
                        currentUser = currentUser,
                        currentRoute = currentScreen,
                        unreadNotificationCount = unreadNotifCount,
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            currentScreen = route
                        },
                        onOpenProfile = {
                            scope.launch { drawerState.close() }
                            currentScreen = "caretaker_profile"
                        },
                        onOpenSecurity = {
                            scope.launch { drawerState.close() }
                            currentScreen = "caretaker_security"
                        },
                        onOpenHelpSupport = {
                            scope.launch { drawerState.close() }
                            showHelpSupportDialog = true
                        },
                        onOpenAboutMedTime = {
                            scope.launch { drawerState.close() }
                            showAboutMedTimeDialog = true
                        },
                        onLogoutRequest = {
                            scope.launch { drawerState.close() }
                            showLogoutConfirmDialog = true
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
                else -> {
                    PatientSideDrawerSheet(
                        currentUser = currentUser,
                        currentRoute = currentScreen,
                        unreadNotificationCount = unreadNotifCount,
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            currentScreen = route
                        },
                        onOpenProfile = {
                            scope.launch { drawerState.close() }
                            showEditProfileDialog = true
                        },
                        onOpenSecurityPrivacy = {
                            scope.launch { drawerState.close() }
                            showSecurityPrivacyDialog = true
                        },
                        onOpenConnectWebsite = {
                            scope.launch { drawerState.close() }
                            showConnectWebsiteDialog = true
                        },
                        onOpenNotificationSettings = {
                            scope.launch { drawerState.close() }
                            showNotificationSettingsDialog = true
                        },
                        onOpenReminderSound = {
                            scope.launch { drawerState.close() }
                            showReminderSoundDialog = true
                        },
                        onOpenHelpSupport = {
                            scope.launch { drawerState.close() }
                            showHelpSupportDialog = true
                        },
                        onOpenAboutMedTime = {
                            scope.launch { drawerState.close() }
                            showAboutMedTimeDialog = true
                        },
                        onLogoutRequest = {
                            scope.launch { drawerState.close() }
                            showLogoutConfirmDialog = true
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(48.dp)
                                .testTag("top_bar_hamburger_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = MedBluePrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MedBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = "MedTime Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "MedTime",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = when (userRole) {
                                        "DOCTOR" -> "Doctor Clinical Portal"
                                        "CARETAKER" -> "Caretaker Portal"
                                        else -> "Smart Healthcare Tracker"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Role Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (userRole) {
                                "DOCTOR" -> MedSuccessLight
                                "CARETAKER" -> MedWarningLight
                                "ADMIN" -> Color(0xFFEDE7F6)
                                else -> MedBlueLight
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = userRole,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when (userRole) {
                                    "DOCTOR" -> MedSuccess
                                    "CARETAKER" -> MedWarning
                                    "ADMIN" -> Color(0xFF512DA8)
                                    else -> MedBluePrimary
                                }
                            )
                        }

                        // Theme Mode Quick Toggle Button
                        val themeMode by viewModel.themeMode.collectAsState()
                        IconButton(
                            onClick = { viewModel.toggleThemeMode() },
                            modifier = Modifier.testTag("top_bar_theme_toggle")
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                },
                                contentDescription = "Toggle Theme Mode",
                                tint = MedBluePrimary
                            )
                        }

                        // Emergency SOS Dialer / Caretaker Assistance Trigger
                        if (userRole != "DOCTOR") {
                            IconButton(
                                onClick = {
                                    if (userRole == "CARETAKER") {
                                        showCaretakerAssistanceChoiceModal = true
                                    } else {
                                        val phone = currentUser?.emergencyContactPhone ?: "911"
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phone")
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.testTag(if (userRole == "CARETAKER") "top_bar_caretaker_assistance_button" else "top_bar_sos_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneInTalk,
                                    contentDescription = if (userRole == "CARETAKER") "Patient Assistance" else "Emergency SOS",
                                    tint = if (userRole == "CARETAKER") MedBluePrimary else Color(0xFFD32F2F)
                                )
                            }
                        }

                        // Notification Bell with unread badge
                        IconButton(
                            onClick = {
                                if (userRole == "DOCTOR") {
                                    currentScreen = "doctor_notifications"
                                } else if (userRole == "CARETAKER") {
                                    currentScreen = "caretaker_notifications"
                                } else {
                                    showNotificationsSheet = true
                                }
                            },
                            modifier = Modifier.testTag("top_bar_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifCount > 0) {
                                        Badge(containerColor = MedError) {
                                            Text("$unreadNotifCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (unreadNotifCount > 0) Icons.Default.Notifications else Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MedTextPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MedSurface,
                    tonalElevation = 8.dp
                ) {
                    when (userRole) {
                        "DOCTOR" -> {
                            NavigationBarItem(
                                selected = currentScreen == "doctor_dashboard",
                                onClick = { currentScreen = "doctor_dashboard" },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Today", fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "doctor_queue",
                                onClick = { currentScreen = "doctor_queue" },
                                icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Patient Queue") },
                                label = { Text("Queue", fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "doctor_appointments",
                                onClick = { currentScreen = "doctor_appointments" },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Appointments") },
                                label = { Text("Schedule", fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "doctor_patients",
                                onClick = { currentScreen = "doctor_patients" },
                                icon = { Icon(Icons.Default.PeopleAlt, contentDescription = "Patients") },
                                label = { Text("Patients", fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "doctor_settings",
                                onClick = { currentScreen = "doctor_settings" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings", fontSize = 11.sp) }
                            )
                        }
                        "CARETAKER" -> {
                            val isCareTab = currentScreen == "caretaker_dashboard" || currentScreen == "caretaker_patient_info" || currentScreen == "caretaker_patient_monitor" || currentScreen == "caretaker_link_patient" || currentScreen == "caretakers"
                            val isChatTab = currentScreen == "caretaker_messages" || currentScreen == "messages"
                            val isSettingsTab = currentScreen == "caretaker_settings" || currentScreen == "caretaker_security" || currentScreen == "caretaker_profile" || currentScreen == "caretaker_linked_patients" || currentScreen == "caretaker_permissions" || currentScreen == "settings"

                            NavigationBarItem(
                                selected = isCareTab,
                                onClick = { currentScreen = "caretaker_dashboard" },
                                icon = { Icon(Icons.Default.PeopleAlt, contentDescription = "Family Care") },
                                label = { Text("Care", fontSize = 11.sp) },
                                modifier = Modifier.testTag("caretaker_nav_care")
                            )
                            NavigationBarItem(
                                selected = isChatTab,
                                onClick = { currentScreen = "caretaker_messages" },
                                icon = { Icon(Icons.Default.Chat, contentDescription = "Messages") },
                                label = { Text("Chat", fontSize = 11.sp) },
                                modifier = Modifier.testTag("caretaker_nav_chat")
                            )
                            NavigationBarItem(
                                selected = isSettingsTab,
                                onClick = { currentScreen = "caretaker_settings" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings", fontSize = 11.sp) },
                                modifier = Modifier.testTag("caretaker_nav_settings")
                            )
                        }
                        "ADMIN" -> {
                            NavigationBarItem(
                                selected = currentScreen == "admin",
                                onClick = { currentScreen = "admin" },
                                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                label = { Text("Portal", fontSize = 11.sp) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "settings",
                                onClick = { currentScreen = "settings" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings", fontSize = 11.sp) }
                            )
                        }
                        else -> {
                            // Patient Standard Navigation (5 items)
                            NavigationBarItem(
                                selected = currentScreen == "dashboard",
                                onClick = { currentScreen = "dashboard" },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Today", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentScreen == "medicines",
                                onClick = { currentScreen = "medicines" },
                                icon = { Icon(Icons.Default.Medication, contentDescription = "Medicines") },
                                label = { Text("Meds", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_medicines")
                            )
                            NavigationBarItem(
                                selected = currentScreen == "appointments",
                                onClick = { currentScreen = "appointments" },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Appointments") },
                                label = { Text("Doctors", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_appointments")
                            )
                            NavigationBarItem(
                                selected = currentScreen == "ai_assistant",
                                onClick = { currentScreen = "ai_assistant" },
                                icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Health") },
                                label = { Text("AI Health", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_ai")
                            )
                            NavigationBarItem(
                                selected = currentScreen == "settings",
                                onClick = { currentScreen = "settings" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_settings")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    // PATIENT SCREENS (UNTOUCHED)
                    "dashboard" -> PatientDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToMedicines = { currentScreen = "medicines" },
                        onNavigateToAppointments = { currentScreen = "appointments" },
                        onNavigateToAi = { currentScreen = "ai_assistant" },
                        onNavigateToMaps = { currentScreen = "maps" },
                        onNavigateToDocuments = { currentScreen = "documents" },
                        onNavigateToCaretakers = { currentScreen = "caretakers" },
                        onNavigateToNotificationHistory = { currentScreen = "notification_history" }
                    )
                    "medicines" -> MedicinesScreen(viewModel = viewModel)
                    "appointments" -> AppointmentsScreen(
                        viewModel = viewModel,
                        onOpenChatWithDoctor = { _, _ -> currentScreen = "messages" },
                        onNavigateToMaps = { currentScreen = "maps" }
                    )
                    "messages" -> MessagesScreen(viewModel = viewModel)
                    "documents" -> DocumentsScreen(viewModel = viewModel)
                    "caretakers" -> CaretakerScreen(viewModel = viewModel)
                    "maps" -> MedicalMapsScreen(viewModel = viewModel)
                    "news" -> MedicalNewsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "dashboard" }
                    )
                    "notification_history" -> NotificationHistoryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "dashboard" }
                    )
                    "ai_assistant" -> AiAssistantScreen(viewModel = viewModel)
                    "admin" -> AdminScreen(viewModel = viewModel)
                    "settings" -> SettingsScreen(viewModel = viewModel)

                    // DOCTOR SCREENS
                    "doctor_dashboard" -> DoctorDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToQueue = { currentScreen = "doctor_queue" },
                        onNavigateToAppointments = { currentScreen = "doctor_appointments" },
                        onNavigateToPatients = { currentScreen = "doctor_patients" },
                        onNavigateToPrescriptions = { currentScreen = "doctor_prescriptions" },
                        onNavigateToRecords = { currentScreen = "doctor_records" },
                        onNavigateToDocuments = { currentScreen = "doctor_documents" },
                        onNavigateToProfile = { currentScreen = "doctor_profile" }
                    )
                    "doctor_queue" -> DoctorQueueScreen(
                        viewModel = viewModel,
                        onNavigateToPrescribe = { pId, pName -> currentScreen = "doctor_prescriptions" },
                        onNavigateToPatientDetail = { _ -> currentScreen = "doctor_patients" }
                    )
                    "doctor_appointments" -> DoctorAppointmentsScreen(
                        viewModel = viewModel,
                        onNavigateToPrescribe = { pId, pName -> currentScreen = "doctor_prescriptions" },
                        onNavigateToChat = { pId, pName -> currentScreen = "doctor_messages" }
                    )
                    "doctor_patients" -> DoctorPatientsScreen(
                        viewModel = viewModel,
                        onNavigateToPrescribe = { pId, pName -> currentScreen = "doctor_prescriptions" },
                        onNavigateToChat = { pId, pName -> currentScreen = "doctor_messages" }
                    )
                    "doctor_records" -> DoctorMedicalRecordsScreen(viewModel = viewModel)
                    "doctor_prescriptions" -> DoctorPrescriptionsScreen(viewModel = viewModel)
                    "doctor_documents" -> DoctorDocumentsScreen(viewModel = viewModel)
                    "doctor_messages" -> DoctorMessagesScreen(viewModel = viewModel)
                    "doctor_notifications" -> DoctorNotificationsScreen(viewModel = viewModel)
                    "doctor_profile" -> DoctorProfileScreen(
                        viewModel = viewModel,
                        onNavigateToSecurity = { currentScreen = "doctor_security" }
                    )
                    "doctor_settings" -> DoctorSettingsScreen(
                        viewModel = viewModel,
                        onNavigateToSecurity = { currentScreen = "doctor_security" },
                        onNavigateToProfile = { currentScreen = "doctor_profile" },
                        onOpenHelpSupport = { showHelpSupportDialog = true },
                        onLogoutRequest = { showLogoutConfirmDialog = true }
                    )
                    "doctor_security" -> DoctorSecurityScreen(viewModel = viewModel)

                    // CARETAKER SCREENS
                    "caretaker_dashboard" -> CaretakerDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToPatientInfo = { pId -> currentScreen = "caretaker_patient_info" },
                        onNavigateToPatientMonitor = { pId -> currentScreen = "caretaker_patient_monitor" },
                        onNavigateToLinkPatient = { currentScreen = "caretaker_link_patient" },
                        onNavigateToHelpAssistance = { currentScreen = "caretaker_help_assistance" },
                        onNavigateToMessages = { targetId -> currentScreen = "caretaker_messages" },
                        onNavigateToNotifications = { currentScreen = "caretaker_notifications" }
                    )
                    "caretaker_patient_info" -> CaretakerPatientInfoScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" },
                        onNavigateToChat = { targetId -> currentScreen = "caretaker_messages" },
                        onNavigateToPermissions = { pId -> currentScreen = "caretaker_permissions" }
                    )
                    "caretaker_patient_monitor" -> CaretakerPatientMonitorScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" }
                    )
                    "caretaker_link_patient" -> CaretakerLinkPatientScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" },
                        onNavigateToLinkedPatients = { currentScreen = "caretaker_linked_patients" }
                    )
                    "caretaker_linked_patients" -> CaretakerLinkedPatientsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" },
                        onNavigateToLinkPatient = { currentScreen = "caretaker_link_patient" },
                        onNavigateToPatientInfo = { pId -> currentScreen = "caretaker_patient_info" },
                        onNavigateToPermissions = { pId -> currentScreen = "caretaker_permissions" }
                    )
                    "caretaker_permissions" -> CaretakerPermissionsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" }
                    )
                    "caretaker_messages" -> CaretakerMessagesScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" }
                    )
                    "caretaker_notifications" -> CaretakerNotificationsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" },
                        onNavigateToWallet = { currentScreen = "caretaker_dashboard" },
                        onNavigateToChat = { currentScreen = "caretaker_messages" },
                        onNavigateToPatientInfo = { pId -> currentScreen = "caretaker_patient_info" },
                        onNavigateToMedicines = { currentScreen = "caretaker_patient_monitor" },
                        onNavigateToAppointments = { currentScreen = "caretaker_patient_info" },
                        onNavigateToRequestTracker = { reqId -> showCaretakerAssistanceTrackerSheet = true }
                    )
                    "caretaker_maps", "caretaker_location_sharing" -> CaretakerMapsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" }
                    )
                    "caretaker_help_assistance" -> CaretakerHelpAssistanceScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" }
                    )
                    "caretaker_announcements" -> CaretakerAnnouncementsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" }
                    )
                    "caretaker_profile" -> CaretakerProfileScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" },
                        onNavigateToSecurity = { currentScreen = "caretaker_security" }
                    )
                    "caretaker_settings", "caretaker_notif_settings" -> CaretakerSettingsScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = { currentScreen = "caretaker_profile" },
                        onNavigateToSecurity = { currentScreen = "caretaker_security" },
                        onNavigateToLinkedPatients = { currentScreen = "caretaker_linked_patients" },
                        onNavigateToPermissions = { currentScreen = "caretaker_permissions" },
                        onNavigateToWebsiteConnection = { currentScreen = "caretaker_website_connection" },
                        onOpenHelpSupport = { showHelpSupportDialog = true },
                        onOpenAboutMedTime = { showAboutMedTimeDialog = true },
                        onLogoutRequest = { showLogoutConfirmDialog = true }
                    )
                    "caretaker_security" -> CaretakerSecurityScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" }
                    )
                    "caretaker_website_connection" -> CaretakerWebsiteConnectionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "caretaker_dashboard" }
                    )

                    else -> if (userRole == "DOCTOR") {
                        DoctorDashboardScreen(
                            viewModel = viewModel,
                            onNavigateToQueue = { currentScreen = "doctor_queue" },
                            onNavigateToAppointments = { currentScreen = "doctor_appointments" },
                            onNavigateToPatients = { currentScreen = "doctor_patients" },
                            onNavigateToPrescriptions = { currentScreen = "doctor_prescriptions" },
                            onNavigateToRecords = { currentScreen = "doctor_records" },
                            onNavigateToDocuments = { currentScreen = "doctor_documents" },
                            onNavigateToProfile = { currentScreen = "doctor_profile" }
                        )
                    } else if (userRole == "CARETAKER") {
                        CaretakerDashboardScreen(
                            viewModel = viewModel,
                            onNavigateToPatientInfo = { currentScreen = "caretaker_patient_info" },
                            onNavigateToPatientMonitor = { currentScreen = "caretaker_patient_monitor" },
                            onNavigateToLinkPatient = { currentScreen = "caretaker_link_patient" },
                            onNavigateToHelpAssistance = { currentScreen = "caretaker_help_assistance" },
                            onNavigateToMessages = { currentScreen = "caretaker_messages" },
                            onNavigateToNotifications = { currentScreen = "caretaker_notifications" }
                        )
                    } else {
                        PatientDashboardScreen(
                            viewModel = viewModel,
                            onNavigateToMedicines = { currentScreen = "medicines" },
                            onNavigateToAppointments = { currentScreen = "appointments" },
                            onNavigateToAi = { currentScreen = "ai_assistant" },
                            onNavigateToMaps = { currentScreen = "maps" },
                            onNavigateToDocuments = { currentScreen = "documents" },
                            onNavigateToCaretakers = { currentScreen = "caretakers" },
                            onNavigateToNotificationHistory = { currentScreen = "notification_history" }
                        )
                    }
                }
=======
            onMarkAllRead = { viewModel.markAllNotificationsRead() }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MedBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = "MedTime Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MedTime",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Smart Healthcare Tracker",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (userRole) {
                            "DOCTOR" -> MedSuccessLight
                            "CARETAKER" -> MedWarningLight
                            "ADMIN" -> Color(0xFFEDE7F6)
                            else -> MedBlueLight
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = userRole,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (userRole) {
                                "DOCTOR" -> MedSuccess
                                "CARETAKER" -> MedWarning
                                "ADMIN" -> Color(0xFF512DA8)
                                else -> MedBluePrimary
                            }
                        )
                    }

                    // Emergency SOS Dialer
                    IconButton(
                        onClick = {
                            val phone = currentUser?.emergencyContactPhone ?: "911"
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("top_bar_sos_button")
                    ) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = "Emergency SOS", tint = Color(0xFFD32F2F))
                    }

                    // Notification Bell with unread badge
                    IconButton(
                        onClick = { showNotificationsSheet = true },
                        modifier = Modifier.testTag("top_bar_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(containerColor = MedError) {
                                        Text("$unreadNotifCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (unreadNotifCount > 0) Icons.Default.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = MedTextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MedSurface,
                tonalElevation = 8.dp
            ) {
                when (userRole) {
                    "DOCTOR" -> {
                        NavigationBarItem(
                            selected = currentScreen == "doctor_queue",
                            onClick = { currentScreen = "doctor_queue" },
                            icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Consultations") },
                            label = { Text("Queue", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == "messages",
                            onClick = { currentScreen = "messages" },
                            icon = { Icon(Icons.Default.Chat, contentDescription = "Messages") },
                            label = { Text("Chat", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == "settings",
                            onClick = { currentScreen = "settings" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 11.sp) }
                        )
                    }
                    "CARETAKER" -> {
                        NavigationBarItem(
                            selected = currentScreen == "caretakers",
                            onClick = { currentScreen = "caretakers" },
                            icon = { Icon(Icons.Default.PeopleAlt, contentDescription = "Family Care") },
                            label = { Text("Care", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == "messages",
                            onClick = { currentScreen = "messages" },
                            icon = { Icon(Icons.Default.Chat, contentDescription = "Messages") },
                            label = { Text("Chat", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == "settings",
                            onClick = { currentScreen = "settings" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 11.sp) }
                        )
                    }
                    "ADMIN" -> {
                        NavigationBarItem(
                            selected = currentScreen == "admin",
                            onClick = { currentScreen = "admin" },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                            label = { Text("Portal", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == "settings",
                            onClick = { currentScreen = "settings" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 11.sp) }
                        )
                    }
                    else -> {
                        // Patient Standard Navigation (5 items)
                        NavigationBarItem(
                            selected = currentScreen == "dashboard",
                            onClick = { currentScreen = "dashboard" },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Today", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "medicines",
                            onClick = { currentScreen = "medicines" },
                            icon = { Icon(Icons.Default.Medication, contentDescription = "Medicines") },
                            label = { Text("Meds", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_medicines")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "appointments",
                            onClick = { currentScreen = "appointments" },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Appointments") },
                            label = { Text("Doctors", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_appointments")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "ai_assistant",
                            onClick = { currentScreen = "ai_assistant" },
                            icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Health") },
                            label = { Text("AI Health", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_ai")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "settings",
                            onClick = { currentScreen = "settings" },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "dashboard" -> PatientDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToMedicines = { currentScreen = "medicines" },
                    onNavigateToAppointments = { currentScreen = "appointments" },
                    onNavigateToAi = { currentScreen = "ai_assistant" },
                    onNavigateToMaps = { currentScreen = "maps" },
                    onNavigateToDocuments = { currentScreen = "documents" },
                    onNavigateToCaretakers = { currentScreen = "caretakers" }
                )
                "medicines" -> MedicinesScreen(viewModel = viewModel)
                "appointments" -> AppointmentsScreen(
                    viewModel = viewModel,
                    onOpenChatWithDoctor = { _, _ -> currentScreen = "messages" }
                )
                "messages" -> MessagesScreen(viewModel = viewModel)
                "documents" -> DocumentsScreen(viewModel = viewModel)
                "caretakers" -> CaretakerScreen(viewModel = viewModel)
                "maps" -> MedicalMapsScreen(viewModel = viewModel)
                "ai_assistant" -> AiAssistantScreen(viewModel = viewModel)
                "doctor_queue" -> DoctorScreen(viewModel = viewModel)
                "admin" -> AdminScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
                else -> PatientDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToMedicines = { currentScreen = "medicines" },
                    onNavigateToAppointments = { currentScreen = "appointments" },
                    onNavigateToAi = { currentScreen = "ai_assistant" },
                    onNavigateToMaps = { currentScreen = "maps" },
                    onNavigateToDocuments = { currentScreen = "documents" },
                    onNavigateToCaretakers = { currentScreen = "caretakers" }
                )
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            }
        }
    }
}
