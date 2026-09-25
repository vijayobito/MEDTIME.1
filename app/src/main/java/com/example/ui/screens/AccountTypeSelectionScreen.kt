package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AccountTypeOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBgColor: Color,
    val badge: String? = null
)

@Composable
fun AccountTypeSelectionScreen(
    onSelectPatient: () -> Unit,
    onSelectDoctor: () -> Unit,
    onSelectCaretaker: () -> Unit,
    onSelectAdmin: () -> Unit,
    onSelectGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedOptionId by remember { mutableStateOf<String?>(null) }
    var isTransitioning by remember { mutableStateOf(false) }

    val options = remember {
        listOf(
            AccountTypeOption(
                id = "PATIENT",
                title = "Patient",
                description = "Manage medicines, reminders & health",
                icon = Icons.Default.Person,
                iconTint = MedBluePrimary,
                iconBgColor = MedBlueLight
            ),
            AccountTypeOption(
                id = "DOCTOR",
                title = "Doctor",
                description = "Manage patients & appointments",
                icon = Icons.Default.MedicalServices,
                iconTint = MedSuccess,
                iconBgColor = MedSuccessLight
            ),
            AccountTypeOption(
                id = "CARETAKER",
                title = "Caretaker",
                description = "Support an authorized patient",
                icon = Icons.Default.PeopleAlt,
                iconTint = MedWarning,
                iconBgColor = MedWarningLight
            ),
            AccountTypeOption(
                id = "ADMIN",
                title = "Administrator",
                description = "Manage the MedTime platform",
                icon = Icons.Default.AdminPanelSettings,
                iconTint = Color(0xFF512DA8),
                iconBgColor = Color(0xFFEDE7F6)
            ),
            AccountTypeOption(
                id = "GUEST",
                title = "Guest / Demo",
                description = "Try reminders, news & maps without an account",
                icon = Icons.Default.Visibility,
                iconTint = Color(0xFF00897B),
                iconBgColor = Color(0xFFE0F2F1),
                badge = "NO LOGIN REQUIRED"
            )
        )
    }

    fun handleOptionClick(option: AccountTypeOption) {
        if (isTransitioning) return
        selectedOptionId = option.id
        isTransitioning = true

        coroutineScope.launch {
            // Smooth brief animation showing the neat selected state before proceeding
            delay(280)
            when (option.id) {
                "PATIENT" -> onSelectPatient()
                "DOCTOR" -> onSelectDoctor()
                "CARETAKER" -> onSelectCaretaker()
                "ADMIN" -> onSelectAdmin()
                "GUEST" -> onSelectGuest()
            }
            isTransitioning = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 32.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Branding Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    // Pill / Logo Badge
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MedBluePrimary,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = "MedTime Pill Logo",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "MEDTIME",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize = 17.sp
                        ),
                        color = MedBluePrimary
                    )

                    Text(
                        text = "Smart Healthcare Tracker",
                        style = MaterialTheme.typography.labelMedium,
                        color = MedTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Welcome Title & Subtitle
                    Text(
                        text = "Welcome to MedTime",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MedTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "How would you like to continue?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        color = MedTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. The 5 Selectable Account Type Cards
            items(options.size, key = { options[it].id }) { index ->
                val option = options[index]
                val isSelected = selectedOptionId == option.id

                val cardScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 1f,
                    animationSpec = tween(durationMillis = 200),
                    label = "card_scale"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(cardScale)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MedBluePrimary else MedBorder,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = { handleOptionClick(option) }
                        )
                        .testTag("account_type_${option.id.lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MedBlueLight.copy(alpha = 0.5f) else MedSurface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Option Icon Container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(option.iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = "${option.title} icon",
                                tint = option.iconTint,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Title & Description
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MedTextPrimary
                                )

                                if (option.badge != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE0F2F1)
                                    ) {
                                        Text(
                                            text = option.badge,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF00695C)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = option.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = MedTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Selected indicator or Chevron
                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = MedBluePrimary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Continue",
                                tint = MedTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 3. Footer Slogan & Privacy Note
            item {
                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your health. Simplified.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 13.sp
                        ),
                        color = MedTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MedBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "HIPAA & GDPR Certified Secure Healthcare Platform",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
