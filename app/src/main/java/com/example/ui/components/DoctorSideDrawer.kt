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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserEntity
import com.example.ui.theme.*

data class DoctorDrawerItem(
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
fun DoctorSideDrawerSheet(
    currentUser: UserEntity?,
    currentRoute: String,
    unreadNotificationCount: Int,
    onNavigate: (route: String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onLogoutRequest: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val allMenuItems = remember(unreadNotificationCount) {
        listOf(
            // SECTION: MAIN
            DoctorDrawerItem(
                id = "doctor_dashboard",
                title = "Dashboard",
                subtitle = "Today's clinical overview & stats",
                icon = Icons.Default.Dashboard,
                section = "MAIN",
                route = "doctor_dashboard",
                keywords = listOf("dashboard", "home", "overview", "today", "stats", "summary")
            ),
            DoctorDrawerItem(
                id = "doctor_queue",
                title = "Patient Queue",
                subtitle = "Waiting & in-consultation patients",
                icon = Icons.Default.MedicalServices,
                section = "MAIN",
                route = "doctor_queue",
                badgeCount = 3,
                keywords = listOf("queue", "waiting", "consultation", "patient", "triage", "live")
            ),
            DoctorDrawerItem(
                id = "doctor_appointments",
                title = "Appointments",
                subtitle = "Today's schedule & bookings",
                icon = Icons.Default.CalendarMonth,
                section = "MAIN",
                route = "doctor_appointments",
                keywords = listOf("appointments", "schedule", "calendar", "consultations", "booking", "slots")
            ),
            DoctorDrawerItem(
                id = "doctor_patients",
                title = "Patients",
                subtitle = "Connected & assigned patient roster",
                icon = Icons.Default.PeopleAlt,
                section = "MAIN",
                route = "doctor_patients",
                keywords = listOf("patients", "roster", "directory", "clients", "cases", "history")
            ),

            // SECTION: CLINICAL
            DoctorDrawerItem(
                id = "doctor_records",
                title = "Medical Records",
                subtitle = "Electronic health records & history",
                icon = Icons.Default.FolderShared,
                section = "CLINICAL",
                route = "doctor_records",
                keywords = listOf("records", "ehr", "medical", "history", "notes", "allergies", "vitals")
            ),
            DoctorDrawerItem(
                id = "doctor_prescriptions",
                title = "Prescriptions",
                subtitle = "Create & manage digital prescriptions",
                icon = Icons.Default.PostAdd,
                section = "CLINICAL",
                route = "doctor_prescriptions",
                keywords = listOf("prescription", "prescribe", "rx", "medicine", "dosage", "drugs")
            ),
            DoctorDrawerItem(
                id = "doctor_documents",
                title = "Patient Documents",
                subtitle = "Shared lab reports, scans & files",
                icon = Icons.Default.Description,
                section = "CLINICAL",
                route = "doctor_documents",
                keywords = listOf("documents", "reports", "labs", "scans", "imaging", "files", "pdf")
            ),

            // SECTION: COMMUNICATION
            DoctorDrawerItem(
                id = "doctor_messages",
                title = "Messages",
                subtitle = "Patient & caregiver conversations",
                icon = Icons.Default.Chat,
                section = "COMMUNICATION",
                route = "doctor_messages",
                badgeCount = 2,
                keywords = listOf("messages", "chat", "inbox", "conversations", "patient", "caretaker")
            ),
            DoctorDrawerItem(
                id = "doctor_notifications",
                title = "Notifications",
                subtitle = "Appointments, alerts & requests",
                icon = Icons.Default.Notifications,
                section = "COMMUNICATION",
                route = "doctor_notifications",
                badgeCount = unreadNotificationCount,
                keywords = listOf("notifications", "alerts", "updates", "reminders", "inbox")
            ),

            // SECTION: SETTINGS & SECURITY
            DoctorDrawerItem(
                id = "doctor_settings",
                title = "Settings",
                subtitle = "Availability, schedule & clinic settings",
                icon = Icons.Default.Settings,
                section = "SETTINGS & SECURITY",
                route = "doctor_settings",
                keywords = listOf("settings", "preferences", "schedule", "working hours", "clinic", "slot")
            ),
            DoctorDrawerItem(
                id = "doctor_security",
                title = "Security & Privacy",
                subtitle = "Password, 2FA, sessions & license",
                icon = Icons.Default.Security,
                section = "SETTINGS & SECURITY",
                route = "doctor_security",
                keywords = listOf("security", "privacy", "password", "2fa", "sessions", "login", "auth")
            ),

            // SECTION: ACCOUNT
            DoctorDrawerItem(
                id = "doctor_profile",
                title = "Profile",
                subtitle = "Professional credentials & qualifications",
                icon = Icons.Default.AccountCircle,
                section = "ACCOUNT",
                route = "doctor_profile",
                keywords = listOf("profile", "doctor", "specialty", "license", "credentials", "bio")
            ),
            DoctorDrawerItem(
                id = "doctor_help",
                title = "Help & Support",
                subtitle = "Clinical desk support & guidelines",
                icon = Icons.Default.HelpOutline,
                section = "ACCOUNT",
                keywords = listOf("help", "support", "faq", "contact", "guide", "desk")
            ),
            DoctorDrawerItem(
                id = "doctor_logout",
                title = "Logout",
                subtitle = "Sign out of MedTime Doctor Portal",
                icon = Icons.AutoMirrored.Filled.Logout,
                section = "ACCOUNT",
                isDestructive = true,
                keywords = listOf("logout", "sign out", "exit", "disconnect")
            )
        )
    }

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

    val groupedItems = remember(filteredItems) {
        filteredItems.groupBy { it.section }
    }

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .fillMaxHeight()
            .testTag("doctor_side_drawer_sheet"),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = MedNavy,
        drawerContentColor = Color.White,
        drawerTonalElevation = 12.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. DOCTOR PROFILE HEADER
            DoctorDrawerHeader(
                currentUser = currentUser,
                onOpenProfile = onOpenProfile,
                onCloseDrawer = onCloseDrawer
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            // 2. SEARCH MENU BAR
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Doctor Menu",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search menu items...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("doctor_drawer_search_input")
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 3. MENU ITEMS LIST (Grouped by Section)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                groupedItems.forEach { (section, items) ->
                    item(key = "section_$section") {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MedBlueLight.copy(alpha = 0.85f),
                            modifier = Modifier.padding(start = 12.dp, top = 14.dp, bottom = 6.dp)
                        )
                    }

                    items(items, key = { it.id }) { item ->
                        val isSelected = item.route != null && currentRoute == item.route

                        DoctorDrawerRow(
                            item = item,
                            isSelected = isSelected,
                            onClick = {
                                when (item.id) {
                                    "doctor_profile" -> onOpenProfile()
                                    "doctor_security" -> onOpenSecurity()
                                    "doctor_help" -> onOpenHelpSupport()
                                    "doctor_logout" -> onLogoutRequest()
                                    else -> {
                                        if (item.route != null) {
                                            onNavigate(item.route)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MedTime Clinical • v2.6.0 Pro",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorDrawerHeader(
    currentUser: UserEntity?,
    onOpenProfile: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val doctorName = currentUser?.name?.takeIf { it.isNotBlank() } ?: "Dr. Sarah Mitchell, MD"
    val specialty = currentUser?.doctorSpecialty?.takeIf { it.isNotBlank() } ?: "Cardiology & Internal Medicine"
    val hospital = currentUser?.doctorHospital?.takeIf { it.isNotBlank() } ?: "City General Hospital"
    val photoUrl = currentUser?.doctorProfilePhotoUrl?.takeIf { it.isNotBlank() }
        ?: "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Profile Photo with Online Indicator
            Box(modifier = Modifier.size(62.dp)) {
                Surface(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(2.dp, MedBlueLight, CircleShape),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Doctor Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Active / Online Badge
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MedSuccess)
                        .border(2.dp, MedNavy, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            // Close Drawer Icon
            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Menu",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = doctorName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MedBluePrimary.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "DOCTOR",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = "$specialty • $hospital",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MedSuccess)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Active • In Consultation",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MedSuccessLight,
                    fontSize = 11.sp
                )
            }
        }

        // View & Edit Profile Button
        OutlinedButton(
            onClick = onOpenProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .testTag("btn_doctor_drawer_view_profile"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "View & Edit Profile",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun DoctorDrawerRow(
    item: DoctorDrawerItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MedBluePrimary
        item.isDestructive -> Color(0xFFC62828).copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val contentColor = when {
        isSelected -> Color.White
        item.isDestructive -> Color(0xFFFF8A80)
        else -> Color.White
    }

    val iconColor = when {
        isSelected -> Color.White
        item.isDestructive -> Color(0xFFFF5252)
        else -> MedBlueLight
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("doctor_nav_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.badgeCount > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color.White else MedBluePrimary
                ) {
                    Text(
                        text = "${item.badgeCount}",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) MedBluePrimary else Color.White,
                        fontSize = 11.sp
                    )
                }
            } else if (!item.isDestructive) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
