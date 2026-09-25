package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

private data class CaretakerAnnouncement(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val tag: String,
    val isUrgent: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerAnnouncementsScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val announcements = remember {
        listOf(
            CaretakerAnnouncement(
                id = "1",
                title = "New Caregiver Safety Guidelines v2.4",
                content = "MedTime has updated its emergency escalation protocols. Caregivers can now trigger immediate automated SMS alerts to authorized emergency contacts when a critical medication is missed past 60 minutes.",
                date = "Today, 09:30 AM",
                tag = "Protocol Update",
                isUrgent = true
            ),
            CaretakerAnnouncement(
                id = "2",
                title = "Home Care Travel Fare Structure Announced",
                content = "To support caretakers conducting home visits, official travel assistance is calculated transparently at ₹30 for the first 1–3 km, and ₹10/km for every subsequent kilometer. Wallet balance can be managed directly in the Help & Assistance tab.",
                date = "Yesterday",
                tag = "Policy Notice"
            ),
            CaretakerAnnouncement(
                id = "3",
                title = "Upcoming Cloud Synchronization Maintenance",
                content = "Scheduled cloud database optimization will occur on Sunday between 02:00 AM - 03:00 AM UTC. Offline adherence logs recorded locally on your device will automatically sync upon completion.",
                date = "3 days ago",
                tag = "Maintenance"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Announcements", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MedBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Official MedTime Notices (${announcements.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
            }

            items(announcements) { ann ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (ann.isUrgent) MedErrorLight else MedBlueLight
                            ) {
                                Text(
                                    text = ann.tag,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (ann.isUrgent) MedError else MedBluePrimary
                                )
                            }
                            Text(ann.date, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }

                        Text(
                            text = ann.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        Text(
                            text = ann.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MedTextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
