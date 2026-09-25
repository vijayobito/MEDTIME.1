package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*

data class DrawerMenuItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val section: String,
    val route: String? = null,
    val badgeCount: Int = 0,
    val isDestructive: Boolean = false,
    val keywords: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSideDrawerSheet(
    currentUser: UserEntity?,
    currentRoute: String,
    unreadNotificationCount: Int,
    onNavigate: (route: String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSecurityPrivacy: () -> Unit,
    onOpenConnectWebsite: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenReminderSound: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onOpenAboutMedTime: () -> Unit,
    onLogoutRequest: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    // Exactly specified menu ordering & sections
    val allMenuItems = remember(unreadNotificationCount) {
        listOf(
            // SECTION: MAIN
            DrawerMenuItem(
                id = "dashboard",
                title = "Main Dashboard",
                subtitle = "Today's doses & health overview",
                icon = Icons.Default.Dashboard,
                section = "MAIN",
                route = "dashboard",
                keywords = listOf("home", "overview", "today", "schedule", "doses")
            ),
            DrawerMenuItem(
                id = "medicines",
                title = "Medicine Reminder",
                subtitle = "Medicines, schedules & reminders",
                icon = Icons.Default.Medication,
                section = "MAIN",
                route = "medicines",
                keywords = listOf("medicine", "medication", "pill", "prescription", "alarm", "schedule", "dose")
            ),
            DrawerMenuItem(
                id = "history",
                title = "Medicine History & Adherence",
                subtitle = "Taken, missed, skipped & adherence",
                icon = Icons.Default.BarChart,
                section = "MAIN",
                route = "medicines",
                keywords = listOf("history", "adherence", "score", "taken", "missed", "skipped", "analytics", "medicine")
            ),
            DrawerMenuItem(
                id = "appointments",
                title = "Doctor Appointments",
                subtitle = "Book & manage consultations",
                icon = Icons.Default.CalendarMonth,
                section = "MAIN",
                route = "appointments",
                keywords = listOf("doctor", "appointment", "consultation", "visit", "booking", "specialist")
            ),
            DrawerMenuItem(
                id = "messages",
                title = "Messages",
                subtitle = "Chat with doctors & caretakers",
                icon = Icons.Default.Chat,
                section = "MAIN",
                route = "messages",
                keywords = listOf("chat", "message", "doctor", "caretaker", "inbox", "conversation")
            ),

            // SECTION: HEALTH
            DrawerMenuItem(
                id = "ai_assistant",
                title = "MedTime AI Assistant",
                subtitle = "Medical questions & health guidance",
                icon = Icons.Default.Psychology,
                section = "HEALTH",
                route = "ai_assistant",
                keywords = listOf("ai", "assistant", "questions", "advice", "guidance", "bot", "symptoms")
            ),
            DrawerMenuItem(
                id = "documents",
                title = "Medical Documents",
                subtitle = "Reports, prescriptions & medical files",
                icon = Icons.Default.Description,
                section = "HEALTH",
                route = "documents",
                keywords = listOf("document", "report", "lab", "prescription", "files", "pdf", "scan")
            ),
            DrawerMenuItem(
                id = "maps",
                title = "Healthcare Maps & Clinics",
                subtitle = "Nearby hospitals & pharmacies",
                icon = Icons.Default.Map,
                section = "HEALTH",
                route = "maps",
                keywords = listOf("map", "clinic", "hospital", "pharmacy", "nearby", "location", "emergency")
            ),
            DrawerMenuItem(
                id = "news",
                title = "Medical News & Tips",
                subtitle = "Medical information & announcements",
                icon = Icons.Default.Article,
                section = "HEALTH",
                route = "news",
                keywords = listOf("news", "tips", "announcements", "articles", "health", "updates")
            ),

            // SECTION: CONNECTIONS
            DrawerMenuItem(
                id = "my_doctors",
                title = "My Doctors",
                subtitle = "Connected doctors & prescriptions",
                icon = Icons.Default.MedicalServices,
                section = "CONNECTIONS",
                route = "appointments",
                keywords = listOf("doctor", "specialist", "prescriptions", "connected", "physician")
            ),
            DrawerMenuItem(
                id = "caretakers",
                title = "My Caretaker",
                subtitle = "Manage caretaker access & permissions",
                icon = Icons.Default.PeopleAlt,
                section = "CONNECTIONS",
                route = "caretakers",
                keywords = listOf("caretaker", "caregiver", "family", "access", "permissions", "sync")
            ),
            DrawerMenuItem(
                id = "connect_website",
                title = "Connect to Website",
                subtitle = "Scan QR to connect your MedTime account",
                icon = Icons.Default.QrCodeScanner,
                section = "CONNECTIONS",
                route = null,
                keywords = listOf("connect", "website", "qr", "browser", "web", "login", "desktop", "portal")
            ),

            // SECTION: NOTIFICATIONS
            DrawerMenuItem(
                id = "notifications",
                title = "Notification Center",
                subtitle = "Medicines, appointments, messages & alerts",
                icon = Icons.Default.Notifications,
                section = "NOTIFICATIONS",
                route = "notification_history",
                badgeCount = unreadNotificationCount,
                keywords = listOf("notification", "notifications", "alerts", "unread", "broadcasts", "center", "history")
            ),

            // SECTION: SETTINGS & SECURITY
            DrawerMenuItem(
                id = "settings",
                title = "Settings",
                subtitle = "App preferences & account settings",
                icon = Icons.Default.Settings,
                section = "SETTINGS & SECURITY",
                route = "settings",
                keywords = listOf("settings", "preferences", "config", "account", "theme")
            ),
            DrawerMenuItem(
                id = "security_privacy",
                title = "Security & Privacy",
                subtitle = "Password, 2FA, sessions & permissions",
                icon = Icons.Default.Security,
                section = "SETTINGS & SECURITY",
                route = null,
                keywords = listOf("security", "privacy", "password", "2fa", "two-factor", "sessions", "permissions", "settings")
            ),
            DrawerMenuItem(
                id = "notification_settings",
                title = "Notification Settings",
                subtitle = "Control your MedTime notifications",
                icon = Icons.Default.NotificationsActive,
                section = "SETTINGS & SECURITY",
                route = null,
                keywords = listOf("notification settings", "alerts", "channels", "dnd", "push", "settings")
            ),
            DrawerMenuItem(
                id = "reminder_sound",
                title = "Reminder Sound",
                subtitle = "Sound & vibration preferences",
                icon = Icons.Default.VolumeUp,
                section = "SETTINGS & SECURITY",
                route = null,
                keywords = listOf("sound", "reminder", "alarm", "volume", "vibration", "tone", "ringtone", "settings")
            ),

            // SECTION: ACCOUNT
            DrawerMenuItem(
                id = "profile",
                title = "Profile",
                subtitle = "Personal information",
                icon = Icons.Default.Person,
                section = "ACCOUNT",
                route = null,
                keywords = listOf("profile", "account", "user", "personal", "details", "contact")
            ),
            DrawerMenuItem(
                id = "help_support",
                title = "Help & Support",
                subtitle = "Get help with MedTime",
                icon = Icons.Default.HelpOutline,
                section = "ACCOUNT",
                route = null,
                keywords = listOf("help", "support", "faq", "guide", "helpline", "emergency", "questions")
            ),
            DrawerMenuItem(
                id = "about_medtime",
                title = "About MedTime",
                subtitle = "Version and application information",
                icon = Icons.Default.Info,
                section = "ACCOUNT",
                route = null,
                keywords = listOf("about", "version", "info", "app", "application", "license")
            ),
            DrawerMenuItem(
                id = "logout",
                title = "Logout",
                subtitle = "Sign out of MedTime",
                icon = Icons.AutoMirrored.Filled.Logout,
                section = "ACCOUNT",
                route = null,
                isDestructive = true,
                keywords = listOf("logout", "sign out", "exit", "quit", "disconnect")
            )
        )
    }

    // Interactive Search Logic matching prompt rules
    val filteredItems = remember(searchQuery, allMenuItems) {
        if (searchQuery.isBlank()) {
            allMenuItems
        } else {
            val q = searchQuery.trim().lowercase()
            allMenuItems.filter { item ->
                item.title.lowercase().contains(q) ||
                item.subtitle.lowercase().contains(q) ||
                item.section.lowercase().contains(q) ||
                item.keywords.any { it.contains(q) }
            }
        }
    }

    // Preserve the precise section ordering
    val sectionOrder = listOf("MAIN", "HEALTH", "CONNECTIONS", "NOTIFICATIONS", "SETTINGS & SECURITY", "ACCOUNT")
    val groupedSections = remember(filteredItems) {
        val groups = filteredItems.groupBy { it.section }
        sectionOrder.mapNotNull { section ->
            val items = groups[section]
            if (items != null) Pair(section, items) else null
        }
    }

    // ModalDrawerSheet with 82-86% width & smooth rounded edges
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxWidth(0.84f)
            .fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp, topStart = 0.dp, bottomStart = 0.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 8.dp
    ) {
        // Entire drawer content is scrollable via LazyColumn so bottom options are NEVER clipped!
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("patient_side_drawer_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. BRANDING HEADER
            item(key = "drawer_header") {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
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
                                    .background(MedBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "MedTime Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "MedTime",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 21.sp,
                                        color = MedBlueDark
                                    )
                                )
                                Text(
                                    text = "Patient Portal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MedTextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onCloseDrawer,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MedSurfaceVariant.copy(alpha = 0.8f))
                                .testTag("drawer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = MedTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. PATIENT PROFILE CARD (Dynamic Profile)
            item(key = "drawer_profile_card") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MedBlueLight.copy(alpha = 0.55f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, MedBluePrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar initials box
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MedBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = (currentUser?.name?.trim()?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "VK").uppercase()
                                Text(
                                    text = initials.ifBlank { "PT" },
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentUser?.name ?: "Vijay Kumar",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MedTextPrimary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    // Active indicator pill
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MedSuccessLight,
                                        border = androidx.compose.foundation.BorderStroke(0.8.dp, MedSuccess.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MedSuccess)
                                            )
                                            Text(
                                                text = "Active",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MedSuccessDark
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = currentUser?.email?.ifBlank { currentUser?.phone } ?: "patient@medtime.app",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MedTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onCloseDrawer()
                                onOpenProfile()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("drawer_view_profile_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MedBluePrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View & Edit Profile", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. SEARCH BAR ("Search MedTime")
            item(key = "drawer_search_bar") {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        text = "Search MedTime",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MedTextSecondary,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search features...", fontSize = 13.sp, color = MedTextTertiary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MedBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp), tint = MedTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drawer_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MedSurface,
                            unfocusedContainerColor = MedSurfaceVariant.copy(alpha = 0.6f),
                            focusedBorderColor = MedBluePrimary,
                            unfocusedBorderColor = MedBorder,
                            focusedTextColor = MedTextPrimary,
                            unfocusedTextColor = MedTextPrimary
                        )
                    )
                }
            }

            // 4. EMPTY SEARCH STATE
            if (filteredItems.isEmpty()) {
                item(key = "drawer_no_features_found") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MedSurfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MedTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No features found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Try searching for medicines, appointments, documents, maps, or settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            TextButton(onClick = { searchQuery = "" }) {
                                Text("Clear Search", color = MedBluePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. CATEGORIZED MENU ITEMS
            groupedSections.forEach { (sectionName, items) ->
                item(key = "section_header_$sectionName") {
                    Text(
                        text = sectionName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.5.sp,
                            color = MedTextSecondary
                        ),
                        modifier = Modifier.padding(start = 6.dp, top = 14.dp, bottom = 4.dp)
                    )
                }

                items(items, key = { it.id }) { item ->
                    val isSelected = when (item.id) {
                        "dashboard" -> currentRoute == "dashboard"
                        "medicines" -> currentRoute == "medicines"
                        "history" -> currentRoute == "medicines" && false
                        "appointments" -> currentRoute == "appointments"
                        "messages" -> currentRoute == "messages"
                        "ai_assistant" -> currentRoute == "ai_assistant"
                        "documents" -> currentRoute == "documents"
                        "maps" -> currentRoute == "maps"
                        "news" -> currentRoute == "news"
                        "caretakers" -> currentRoute == "caretakers"
                        "notifications" -> currentRoute == "notification_history"
                        "settings" -> currentRoute == "settings"
                        else -> false
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onCloseDrawer()
                                when (item.id) {
                                    "connect_website" -> onOpenConnectWebsite()
                                    "security_privacy" -> onOpenSecurityPrivacy()
                                    "notification_settings" -> onOpenNotificationSettings()
                                    "reminder_sound" -> onOpenReminderSound()
                                    "profile" -> onOpenProfile()
                                    "help_support" -> onOpenHelpSupport()
                                    "about_medtime" -> onOpenAboutMedTime()
                                    "logout" -> onLogoutRequest()
                                    else -> {
                                        if (item.route != null) {
                                            onNavigate(item.route)
                                        }
                                    }
                                }
                            }
                            .testTag("drawer_item_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            item.isDestructive -> Color(0xFFFFEBEE).copy(alpha = 0.6f)
                            isSelected -> MedBlueLight
                            else -> Color.Transparent
                        },
                        border = when {
                            isSelected -> androidx.compose.foundation.BorderStroke(1.2.dp, MedBluePrimary.copy(alpha = 0.5f))
                            item.isDestructive -> androidx.compose.foundation.BorderStroke(0.8.dp, MedError.copy(alpha = 0.2f))
                            else -> null
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // High-contrast Icon container (38x38dp touch area)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            item.isDestructive -> MedError
                                            isSelected -> MedBluePrimary
                                            else -> MedSurfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = when {
                                        item.isDestructive -> Color.White
                                        isSelected -> Color.White
                                        else -> MedBluePrimary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Title & Subtitle with great contrast & crisp alignment
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (item.isDestructive) MedErrorDark else MedTextPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.5.sp,
                                        color = if (item.isDestructive) MedError.copy(alpha = 0.85f) else MedTextSecondary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Badge count for notifications or Trailing chevron
                            if (item.badgeCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = MedError
                                ) {
                                    Text(
                                        text = "${item.badgeCount}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = if (item.isDestructive) MedError.copy(alpha = 0.6f) else MedTextTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. FOOTER / VERSION INFO
            item(key = "drawer_footer") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalDivider(color = MedDivider, modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                        text = "MedTime",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary,
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedTextSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}
