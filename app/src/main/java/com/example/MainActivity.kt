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
import com.example.ui.components.NotificationsSheet
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MedTimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
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

    var entryFlow by remember { mutableStateOf(EntryFlow.ACCOUNT_TYPE_SELECTION) }
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
            // When unauthenticated, always reset to the Account Type Selection screen
            entryFlow = EntryFlow.ACCOUNT_TYPE_SELECTION
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

    LaunchedEffect(userRole) {
        currentScreen = when (userRole) {
            "DOCTOR" -> "doctor_queue"
            "CARETAKER" -> "caretakers"
            "ADMIN" -> "admin"
            else -> "dashboard"
        }
    }

    if (showNotificationsSheet) {
        NotificationsSheet(
            notifications = notifications,
            onDismiss = { showNotificationsSheet = false },
            onMarkRead = { id -> viewModel.markNotificationRead(id) },
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
            }
        }
    }
}
