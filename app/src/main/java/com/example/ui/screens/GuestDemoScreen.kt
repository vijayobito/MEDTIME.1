package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
<<<<<<< HEAD
import com.example.ui.components.DailyAdherenceChartCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardStats
=======
import com.example.ui.theme.*
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import com.example.ui.viewmodel.MedTimeViewModel

// Separate in-memory demo data structures for Guest Mode
data class GuestDemoReminder(
    val id: String,
    val medicineName: String,
    val dosage: String,
    val timeSlot: String,
    val instructions: String,
    val status: String = "PENDING" // "PENDING", "TAKEN", "SNOOZED", "SKIPPED"
)

data class HealthNewsArticle(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val fullContent: String,
    val readTime: String,
    val date: String,
    val source: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestDemoScreen(
    viewModel: MedTimeViewModel,
    onNavigateToLogin: (String?) -> Unit,
    onNavigateToSignUp: (String?) -> Unit,
    onExitDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedGuestNav by remember { mutableStateOf("home") } // "home", "reminders", "news", "maps", "about"
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    var restrictedFeatureName by remember { mutableStateOf("this feature") }

    // Separate DEMO DATA (independent from Room database)
    var demoReminders by remember {
        mutableStateOf(
            listOf(
                GuestDemoReminder(
                    id = "demo_1",
                    medicineName = "Vitamin D3 (Cholecalciferol)",
                    dosage = "1000 IU • 1 capsule",
                    timeSlot = "08:00 AM (Morning)",
                    instructions = "Take with breakfast and plenty of water",
                    status = "PENDING"
                ),
                GuestDemoReminder(
                    id = "demo_2",
                    medicineName = "Amoxicillin Trihydrate",
                    dosage = "500 mg • 1 tablet",
                    timeSlot = "01:30 PM (Afternoon)",
                    instructions = "Take after lunch. Complete full antibiotic course",
                    status = "TAKEN"
                ),
                GuestDemoReminder(
                    id = "demo_3",
                    medicineName = "Atorvastatin Calcium",
                    dosage = "20 mg • 1 tablet",
                    timeSlot = "09:30 PM (Bedtime)",
                    instructions = "Take at bedtime for cholesterol management",
                    status = "PENDING"
                ),
                GuestDemoReminder(
                    id = "demo_4",
                    medicineName = "Daily Blood Pressure Check",
                    dosage = "Rest 5 min prior",
                    timeSlot = "07:00 PM (Evening)",
                    instructions = "Record systolic/diastolic readings in log",
                    status = "PENDING"
                )
            )
        )
    }

    val healthArticles = remember {
        listOf(
            HealthNewsArticle(
                id = "news_1",
                category = "Medication Safety",
                title = "Why Consistency in Medication Timing Maximizes Therapeutic Efficacy",
                summary = "Maintaining regular dosing intervals prevents blood serum level spikes and troughs, ensuring optimal therapeutic concentration.",
                fullContent = "Clinical pharmacokinetics shows that taking prescription medications at consistent daily intervals maintains a steady therapeutic index. When doses are missed or erratic, drug concentration can fall below the minimum effective concentration or temporarily spike into toxic ranges. Smart reminder systems like MedTime help reduce non-adherence by up to 64%.",
                readTime = "3 min read",
                date = "Today",
                source = "Clinical Pharmacology Journal"
            ),
            HealthNewsArticle(
                id = "news_2",
                category = "Cardiovascular Health",
                title = "Hydration and Electrolytes: Supporting Daily Heart Function",
                summary = "Proper hydration paired with balanced sodium and potassium levels significantly stabilizes blood pressure.",
                fullContent = "Dehydration strains the heart by reducing circulating plasma volume, causing the heart to beat faster to maintain cardiac output. Healthcare professionals advise steady hydration throughout the day, especially when taking diuretic medications or statins.",
                readTime = "4 min read",
                date = "Yesterday",
                source = "American Heart Association"
            ),
            HealthNewsArticle(
                id = "news_3",
                category = "Digital Health",
                title = "How Caretaker Synchronization Prevents Accidental Double-Dosing",
                summary = "Real-time sync between family members and patients eliminates ambiguity over whether an elderly parent has taken their evening medication.",
                fullContent = "Accidental double dosing is among the most frequent medication errors in multi-generational households. MedTime's authorized caretaker link notifies caregivers the moment a dose is checked off, bringing peace of mind to families.",
                readTime = "2 min read",
                date = "2 days ago",
                source = "World Health Organization"
            ),
            HealthNewsArticle(
                id = "news_4",
                category = "Nutrition & Wellness",
                title = "Food-Drug Interactions: What You Need to Know Before Taking Antibiotics",
                summary = "Certain foods like grapefruit juice or dairy products can inhibit absorption of specific oral antibiotics.",
                fullContent = "Calcium in milk and cheese binds to tetracyclines and fluoroquinolones, preventing the gut from absorbing the antibiotic effectively. Always check the instruction tags on your MedTime prescription schedule.",
                readTime = "5 min read",
                date = "3 days ago",
                source = "National Institutes of Health"
            )
        )
    }

    var selectedArticleForRead by remember { mutableStateOf<HealthNewsArticle?>(null) }

    fun triggerRestrictedFeature(featureName: String) {
        restrictedFeatureName = featureName
        showLoginRequiredDialog = true
    }

    // Exact Login Required Dialog as specified in prompt
    if (showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Login Required",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This feature requires a MedTime account.",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Create an account or login to access your personal healthcare information.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Text(
                            text = "Requested: $restrictedFeatureName",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextTertiary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginRequiredDialog = false
                        onNavigateToLogin("PATIENT")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    modifier = Modifier.testTag("dialog_btn_login")
                ) {
                    Text("Login")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showLoginRequiredDialog = false
                            onNavigateToSignUp("PATIENT")
                        },
                        modifier = Modifier.testTag("dialog_btn_create_account")
                    ) {
                        Text("Create Account", color = MedBluePrimary)
                    }
                    TextButton(
                        onClick = { showLoginRequiredDialog = false },
                        modifier = Modifier.testTag("dialog_btn_continue_demo")
                    ) {
                        Text("Continue Demo", color = MedTextSecondary)
                    }
                }
            }
        )
    }

    if (selectedArticleForRead != null) {
        val art = selectedArticleForRead!!
        AlertDialog(
            onDismissRequest = { selectedArticleForRead = null },
            title = {
                Column {
                    Surface(shape = RoundedCornerShape(6.dp), color = MedBlueLight) {
                        Text(
                            text = art.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = art.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Source: ${art.source} • ${art.date} • ${art.readTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedTextTertiary
                    )
                    HorizontalDivider(color = MedBorder)
                    Text(
                        text = art.fullContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedTextPrimary,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedArticleForRead = null }) {
                    Text("Close")
                }
            }
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MedBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = "MedTime Logo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "MedTime",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        // DEMO MODE Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE65100))
                                )
                                Text(
                                    text = "DEMO MODE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Exit Demo / Login button
                    TextButton(
                        onClick = onExitDemo,
                        modifier = Modifier.testTag("btn_exit_demo")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Exit Demo",
                            tint = MedBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Exit Demo",
                            color = MedBluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
            )
        },
        bottomBar = {
            // Guest Navigation: Reminders, News, Maps, About
            NavigationBar(
                containerColor = MedSurface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedGuestNav == "home",
                    onClick = { selectedGuestNav = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Demo Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    modifier = Modifier.testTag("guest_nav_home")
                )
                NavigationBarItem(
                    selected = selectedGuestNav == "reminders",
                    onClick = { selectedGuestNav = "reminders" },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = "Reminders") },
                    label = { Text("Reminders", fontSize = 11.sp) },
                    modifier = Modifier.testTag("guest_nav_reminders")
                )
                NavigationBarItem(
                    selected = selectedGuestNav == "news",
                    onClick = { selectedGuestNav = "news" },
                    icon = { Icon(Icons.Default.Article, contentDescription = "News") },
                    label = { Text("News", fontSize = 11.sp) },
                    modifier = Modifier.testTag("guest_nav_news")
                )
                NavigationBarItem(
                    selected = selectedGuestNav == "maps",
                    onClick = { selectedGuestNav = "maps" },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Maps") },
                    label = { Text("Maps", fontSize = 11.sp) },
                    modifier = Modifier.testTag("guest_nav_maps")
                )
                NavigationBarItem(
                    selected = selectedGuestNav == "about",
                    onClick = { selectedGuestNav = "about" },
                    icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                    label = { Text("About", fontSize = 11.sp) },
                    modifier = Modifier.testTag("guest_nav_about")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MedBackground)
        ) {
            // Small Banner across top
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE0F2F1),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF80CBC4))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF00695C),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "You're exploring MedTime as a guest. No account is required for demo features.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF004D40)
                    )
                }
            }

            // Main Content Body based on guest navigation tab
            when (selectedGuestNav) {
                "home" -> GuestHomeView(
                    onOpenReminders = { selectedGuestNav = "reminders" },
                    onOpenNews = { selectedGuestNav = "news" },
                    onOpenMaps = { selectedGuestNav = "maps" },
                    onLoginClick = { onNavigateToLogin("PATIENT") },
                    onCreateAccountClick = { onNavigateToSignUp("PATIENT") },
                    onTriggerRestricted = { triggerRestrictedFeature(it) }
                )

                "reminders" -> GuestRemindersView(
                    reminders = demoReminders,
                    onUpdateStatus = { id, newStatus ->
                        demoReminders = demoReminders.map {
                            if (it.id == id) it.copy(status = newStatus) else it
                        }
                    },
                    onResetDemo = {
                        demoReminders = demoReminders.map { it.copy(status = "PENDING") }
                    },
                    onTriggerRestricted = { triggerRestrictedFeature(it) }
                )

                "news" -> GuestNewsView(
                    articles = healthArticles,
                    onSelectArticle = { selectedArticleForRead = it }
                )

                "maps" -> MedicalMapsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )

                "about" -> GuestAboutView(
                    onLogin = { onNavigateToLogin("PATIENT") },
                    onCreateAccount = { onNavigateToSignUp("PATIENT") }
                )

                else -> GuestHomeView(
                    onOpenReminders = { selectedGuestNav = "reminders" },
                    onOpenNews = { selectedGuestNav = "news" },
                    onOpenMaps = { selectedGuestNav = "maps" },
                    onLoginClick = { onNavigateToLogin("PATIENT") },
                    onCreateAccountClick = { onNavigateToSignUp("PATIENT") },
                    onTriggerRestricted = { triggerRestrictedFeature(it) }
                )
            }
        }
    }
}

// ----------------------------------------------------
// 1. GUEST DEMO HOME VIEW
// ----------------------------------------------------
@Composable
private fun GuestHomeView(
    onOpenReminders: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenMaps: () -> Unit,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onTriggerRestricted: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Explore MedTime Demo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Experience smart reminder notifications, up-to-date healthcare news, and nearby medical facility maps with live demo data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Feature 1: Medication Reminders
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MedBlueLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = MedBluePrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Medication Reminders",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Try the MedTime reminder experience",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenReminders,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_guest_open_reminders"),
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Open Reminders", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature 2: Healthcare News
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEDE7F6)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Article, contentDescription = null, tint = Color(0xFF673AB7))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Healthcare News",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Explore health & medical news",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenNews,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_guest_view_news"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF512DA8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View News", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature 3: Medical Maps
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MedSuccessLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = MedSuccess)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Medical Maps",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Find hospitals, clinics & pharmacies",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenMaps,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_guest_open_maps"),
                        colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Open Maps", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Want the full MedTime experience?
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, MedBlueLight, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = MedBluePrimary,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Want the full MedTime experience?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sign in or register to sync your real prescriptions, connect with your doctor, share with caretakers, and store medical documents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_guest_login"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Login", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onCreateAccountClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_guest_create_account"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Restricted Feature Testing Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Restricted Clinical Features (Requires Login)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap any private feature below to verify the guest authorization gate:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val restrictedList = listOf(
                        "My Medicines",
                        "Personal Medicine History",
                        "Appointments",
                        "Messages & Consultations",
                        "Medical Documents",
                        "Emergency Contacts",
                        "Caretaker Sync",
                        "Doctor Patient Data",
                        "Admin Portal"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(restrictedList) { feature ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onTriggerRestricted(feature) },
                                color = Color(0xFFF5F5F5),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MedTextSecondary, modifier = Modifier.size(12.dp))
                                    Text(text = feature, fontSize = 11.sp, color = MedTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. GUEST REMINDERS VIEW (Separated DEMO DATA)
// ----------------------------------------------------
@Composable
private fun GuestRemindersView(
    reminders: List<GuestDemoReminder>,
    onUpdateStatus: (String, String) -> Unit,
    onResetDemo: () -> Unit,
    onTriggerRestricted: (String) -> Unit
) {
    val takenCount = reminders.count { it.status == "TAKEN" }
    val totalCount = reminders.size
    val adherencePercent = if (totalCount > 0) (takenCount * 100) / totalCount else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
<<<<<<< HEAD
        // Demo adherence chart card
        item {
            val missedCount = reminders.count { it.status == "SKIPPED" || it.status == "MISSED" }
            val pendingCount = reminders.count { it.status == "PENDING" || it.status == "SNOOZED" }
            val demoStats = DashboardStats(
                totalScheduled = totalCount,
                takenCount = takenCount,
                pendingCount = pendingCount,
                missedCount = missedCount,
                adherencePercent = adherencePercent
            )

            DailyAdherenceChartCard(
                stats = demoStats,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Reset demo control row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Demo Medication Doses",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                TextButton(onClick = onResetDemo) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Demo", fontSize = 12.sp)
=======
        // Demo adherence card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Demo Schedule",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "$takenCount of $totalCount taken • $adherencePercent% adherence",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }

                        TextButton(onClick = onResetDemo) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { adherencePercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MedSuccess,
                        trackColor = MedBorder
                    )
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                }
            }
        }

        // Demo Reminders List
        items(reminders, key = { it.id }) { item ->
            val isTaken = item.status == "TAKEN"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = if (isTaken) 1.5.dp else 1.dp,
                        color = if (isTaken) MedSuccess.copy(alpha = 0.5f) else MedBorder,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTaken) MedSuccessLight.copy(alpha = 0.3f) else MedSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isTaken) MedSuccessLight else MedBlueLight
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isTaken) Icons.Default.CheckCircle else Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = if (isTaken) MedSuccess else MedBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = item.medicineName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = item.dosage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedBlueDark,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Scheduled: ${item.timeSlot}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (item.status) {
                                "TAKEN" -> MedSuccessLight
                                "SNOOZED" -> MedWarningLight
                                else -> Color(0xFFEEEEEE)
                            }
                        ) {
                            Text(
                                text = item.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (item.status) {
                                    "TAKEN" -> MedSuccess
                                    "SNOOZED" -> MedWarning
                                    else -> MedTextSecondary
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Instructions: ${item.instructions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MedBorder, thickness = 0.6.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.status != "TAKEN") {
                            Button(
                                onClick = { onUpdateStatus(item.id, "TAKEN") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Take", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onUpdateStatus(item.id, "SNOOZED") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Snooze", fontSize = 12.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onUpdateStatus(item.id, "PENDING") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark as Pending", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Add Medicine lock trigger
        item {
            OutlinedButton(
                onClick = { onTriggerRestricted("Add Custom Medicine & Prescriptions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Medicine (Login Required)")
            }
        }
    }
}

// ----------------------------------------------------
// 3. GUEST HEALTHCARE NEWS VIEW
// ----------------------------------------------------
@Composable
private fun GuestNewsView(
    articles: List<HealthNewsArticle>,
    onSelectArticle: (HealthNewsArticle) -> Unit
) {
    var filterCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Medication Safety", "Cardiovascular Health", "Digital Health", "Nutrition & Wellness")

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MedSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = filterCategory == cat,
                    onClick = { filterCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        val filteredArticles = if (filterCategory == "All") articles else articles.filter { it.category == filterCategory }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredArticles, key = { it.id }) { article ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
                        .clickable { onSelectArticle(article) },
                    colors = CardDefaults.cardColors(containerColor = MedSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MedBlueLight
                            ) {
                                Text(
                                    text = article.category,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedBluePrimary
                                )
                            }
                            Text(
                                text = "${article.date} • ${article.readTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Source: ${article.source}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextTertiary,
                                fontSize = 10.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Read article",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedBluePrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. GUEST ABOUT VIEW
// ----------------------------------------------------
@Composable
private fun GuestAboutView(
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MedBluePrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MedTime",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Smart Healthcare & Medication Tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "MedTime connects patients, doctors, and caretakers on a unified platform. It provides automated reminder alarms, multi-page clinical document storage, real-time doctor consultation scheduling, and emergency geolocation assistance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onLogin,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                        ) {
                            Text("Login")
                        }

                        OutlinedButton(
                            onClick = onCreateAccount,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Register")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Security & Regulatory Standards",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    SecurityBadgeItem(
                        title = "HIPAA Compliant Data Handling",
                        desc = "All personal health records and doctor consultation messages are protected under strict medical privacy standards."
                    )
                    SecurityBadgeItem(
                        title = "GDPR & End-to-End Encryption",
                        desc = "Rest-API requests and clinical document metadata are encrypted with zero unauthorized data sharing."
                    )
                    SecurityBadgeItem(
                        title = "Strict Data Separation in Demo Mode",
                        desc = "Guest explorations do not read or write real patient records, keeping the database secure and hygienic."
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityBadgeItem(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(18.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MedTextSecondary, fontSize = 11.sp)
        }
    }
}
