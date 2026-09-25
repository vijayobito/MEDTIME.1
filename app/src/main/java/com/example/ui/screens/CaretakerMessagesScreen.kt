package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerMessagesScreen(
    viewModel: MedTimeViewModel,
    initialTargetUserId: String = "",
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()

    val approvedLinks = remember(caretakerLinks) {
        caretakerLinks.filter { it.status.equals("APPROVED", ignoreCase = true) }
    }

    var selectedFilterCategory by remember { mutableStateOf("PATIENTS") } // "PATIENTS" or "DOCTORS"

    val patientsList = remember(allUsers, approvedLinks) {
        val linkedIds = approvedLinks.map { it.patientId }.toSet()
        allUsers.filter { it.role == "PATIENT" && (linkedIds.isEmpty() || linkedIds.contains(it.id)) }
    }

    val doctorsList = remember(allUsers) {
        allUsers.filter { it.role == "DOCTOR" }
    }

    val contactList = if (selectedFilterCategory == "PATIENTS") patientsList else doctorsList

    var activeContactId by remember(contactList, initialTargetUserId) {
        mutableStateOf(
            if (initialTargetUserId.isNotBlank()) initialTargetUserId
            else contactList.firstOrNull()?.id ?: "patient_1"
        )
    }

    val activeContact = remember(allUsers, activeContactId) {
        allUsers.firstOrNull { it.id == activeContactId } ?: contactList.firstOrNull()
    }

    var messages by remember { mutableStateOf<List<MessageEntity>>(emptyList()) }
    LaunchedEffect(activeContactId) {
        viewModel.getConversationMessages(activeContactId).collect {
            messages = it
        }
    }

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val initials = activeContact?.name?.take(2)?.uppercase() ?: "PT"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (activeContact?.role == "DOCTOR") MedSuccessLight else MedBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (activeContact?.role == "DOCTOR") MedSuccess else MedBluePrimary
                            )
                        }
                        Column {
                            Text(
                                text = activeContact?.name ?: "Care Line",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = if (activeContact?.role == "DOCTOR") "Authorized Physician" else "Linked Patient",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MedBackground)
                .padding(padding)
        ) {
            // Category Tabs: "Linked Patients" | "Authorized Doctors"
            Surface(color = MedSurface, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilterCategory == "PATIENTS",
                            onClick = {
                                selectedFilterCategory = "PATIENTS"
                                if (patientsList.isNotEmpty()) activeContactId = patientsList.first().id
                            },
                            label = { Text("Linked Patients (${patientsList.size})") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedFilterCategory == "DOCTORS",
                            onClick = {
                                selectedFilterCategory = "DOCTORS"
                                if (doctorsList.isNotEmpty()) activeContactId = doctorsList.first().id
                            },
                            label = { Text("Physicians (${doctorsList.size})") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (contactList.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(contactList) { user ->
                                val isSel = user.id == activeContactId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) MedBlueLight else MedSurfaceVariant,
                                    border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, MedBluePrimary) else null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { activeContactId = user.id }
                                ) {
                                    Text(
                                        text = user.name,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium),
                                        color = if (isSel) MedBluePrimary else MedTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start a secure care conversation with ${activeContact?.name ?: "this contact"}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }
                } else {
                    items(messages) { msg ->
                        val isMe = msg.senderId == (currentUser?.id ?: "caretaker_1")
                        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                ),
                                color = if (isMe) MedBluePrimary else MedSurface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    if (!isMe) {
                                        Text(
                                            text = msg.senderName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MedBluePrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMe) Color.White else MedTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isMe) Color.White.copy(alpha = 0.7f) else MedTextSecondary,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                color = MedSurface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type a message...", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("caretaker_message_input"),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MedBluePrimary,
                            unfocusedBorderColor = MedBorderLight,
                            focusedContainerColor = MedSurfaceVariant,
                            unfocusedContainerColor = MedSurfaceVariant
                        ),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank() && activeContact != null) {
                                val text = messageText.trim()
                                messageText = ""
                                viewModel.sendMessage(
                                    receiverId = activeContact.id,
                                    receiverName = activeContact.name,
                                    content = text
                                )
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MedBluePrimary)
                            .testTag("caretaker_send_message_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
