package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*

private data class CaretakerDrawerItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val badge: String? = null,
    val section: String
)

@Composable
fun CaretakerSideDrawerSheet(
    currentUser: UserEntity?,
    currentRoute: String,
    unreadNotificationCount: Int = 0,
    onNavigate: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onOpenAboutMedTime: () -> Unit,
    onLogoutRequest: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val allDrawerItems = remember(unreadNotificationCount) {
        listOf(
            // MAIN
            CaretakerDrawerItem("Caretaker Dashboard", "caretaker_dashboard", Icons.Default.Groups, null, "MAIN"),
            CaretakerDrawerItem("Patient Information", "caretaker_patient_info", Icons.Default.PersonSearch, null, "MAIN"),
            CaretakerDrawerItem("Patient Monitor", "caretaker_patient_monitor", Icons.Default.MonitorHeart, null, "MAIN"),
            CaretakerDrawerItem("Link Patient", "caretaker_link_patient", Icons.Default.Link, null, "MAIN"),

            // COMMUNICATION
            CaretakerDrawerItem("Messages", "caretaker_messages", Icons.Default.Chat, null, "COMMUNICATION"),
            CaretakerDrawerItem("Notifications", "caretaker_notifications", Icons.Default.Notifications, if (unreadNotificationCount > 0) "$unreadNotificationCount" else null, "COMMUNICATION"),

            // SERVICES
            CaretakerDrawerItem("Healthcare Maps", "caretaker_maps", Icons.Default.Map, null, "SERVICES"),
            CaretakerDrawerItem("Help / Assistance", "caretaker_help_assistance", Icons.Default.EmergencyShare, null, "SERVICES"),
            CaretakerDrawerItem("Announcements", "caretaker_announcements", Icons.Default.Campaign, null, "SERVICES"),

            // PATIENT CONNECTIONS
            CaretakerDrawerItem("Linked Patients", "caretaker_linked_patients", Icons.Default.PeopleAlt, null, "PATIENT CONNECTIONS"),
            CaretakerDrawerItem("Manage Patient Permissions", "caretaker_permissions", Icons.Default.AdminPanelSettings, null, "PATIENT CONNECTIONS"),

            // ACCOUNT & SECURITY
            CaretakerDrawerItem("Settings", "caretaker_settings", Icons.Default.Settings, null, "ACCOUNT & SECURITY"),
            CaretakerDrawerItem("Security & Privacy", "caretaker_security", Icons.Default.Shield, null, "ACCOUNT & SECURITY"),
            CaretakerDrawerItem("Notification Settings", "caretaker_notif_settings", Icons.Default.NotificationsActive, null, "ACCOUNT & SECURITY"),
            CaretakerDrawerItem("Location Sharing", "caretaker_location_sharing", Icons.Default.LocationOn, null, "ACCOUNT & SECURITY"),
            CaretakerDrawerItem("Website Connection", "caretaker_website_connection", Icons.Default.QrCodeScanner, null, "ACCOUNT & SECURITY"),

            // ACCOUNT
            CaretakerDrawerItem("Profile", "caretaker_profile", Icons.Default.Person, null, "ACCOUNT"),
            CaretakerDrawerItem("Help & Support", "caretaker_support", Icons.Default.HelpOutline, null, "ACCOUNT"),
            CaretakerDrawerItem("About MedTime", "caretaker_about", Icons.Default.Info, null, "ACCOUNT")
        )
    }

    val filteredItems = remember(searchQuery, allDrawerItems) {
        if (searchQuery.isBlank()) allDrawerItems
        else allDrawerItems.filter {
            it.title.contains(searchQuery.trim(), ignoreCase = true) ||
            it.section.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .fillMaxHeight(),
        drawerContainerColor = MedSurface,
        drawerTonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Header: MedTime Caretaker Portal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MedBluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "MedTime Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "MedTime",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Caretaker Portal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedBluePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = onCloseDrawer) {
                    Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = MedTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caretaker Profile Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorderLight, RoundedCornerShape(16.dp)),
                color = MedSurfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val initials = currentUser?.name?.take(2)?.uppercase() ?: "ED"
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MedWarningLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedWarning
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = currentUser?.name ?: "Emily Davis",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MedSuccess)
                                )
                            }
                            Text(
                                text = "Active Caretaker",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            onCloseDrawer()
                            onOpenProfile()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("drawer_view_caretaker_profile_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MedSurface,
                            contentColor = MedBluePrimary
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View & Edit Caretaker Profile", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("caretaker_drawer_search_input"),
                placeholder = { Text("Search features...", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MedTextSecondary, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MedTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MedBluePrimary,
                    unfocusedBorderColor = MedBorderLight,
                    focusedContainerColor = MedSurfaceVariant,
                    unfocusedContainerColor = MedSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sectioned Items
            val sections = listOf(
                "MAIN",
                "COMMUNICATION",
                "SERVICES",
                "PATIENT CONNECTIONS",
                "ACCOUNT & SECURITY",
                "ACCOUNT"
            )

            sections.forEach { sectionName ->
                val itemsInSection = filteredItems.filter { it.section == sectionName }
                if (itemsInSection.isNotEmpty()) {
                    Text(
                        text = sectionName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextSecondary,
                        modifier = Modifier.padding(start = 8.dp, top = 14.dp, bottom = 6.dp)
                    )

                    itemsInSection.forEach { item ->
                        val isSelected = currentRoute == item.route
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onCloseDrawer()
                                    when (item.route) {
                                        "caretaker_support" -> onOpenHelpSupport()
                                        "caretaker_about" -> onOpenAboutMedTime()
                                        "caretaker_profile" -> onOpenProfile()
                                        "caretaker_security" -> onOpenSecurity()
                                        else -> onNavigate(item.route)
                                    }
                                },
                            color = if (isSelected) MedBlueLight else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) MedBluePrimary else MedTextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) MedBluePrimary else MedTextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (item.badge != null) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MedError,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = item.badge,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        tint = MedTextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MedBorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            // Logout Option
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onCloseDrawer()
                        onLogoutRequest()
                    }
                    .testTag("caretaker_drawer_logout_button"),
                color = MedErrorLight.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = MedError,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedError
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "MedTime v1.0.0 (Caretaker Edition)",
                style = MaterialTheme.typography.labelSmall,
                color = MedTextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
