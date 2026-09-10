package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@Composable
fun AuthScreen(
    viewModel: MedTimeViewModel,
    selectedRole: String = "PATIENT",
    onChangeAccountType: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isLoginMode by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current

    val defaultEmail = when (selectedRole.uppercase()) {
        "DOCTOR" -> "doctor@medtime.com"
        "CARETAKER" -> "caretaker@medtime.com"
        "ADMIN" -> "admin@medtime.com"
        else -> "patient@medtime.com"
    }

    // Login state
    var loginIdentifier by remember(selectedRole) { mutableStateOf(defaultEmail) }
    var loginPassword by remember { mutableStateOf("password123") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Sign up state
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regDob by remember { mutableStateOf("1990-05-15") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regRole by remember(selectedRole) { mutableStateOf(if (selectedRole.uppercase() == "ADMIN") "PATIENT" else selectedRole.uppercase()) }
    var regSpecialty by remember { mutableStateOf("") }
    var regHospital by remember { mutableStateOf("") }
    var regLicense by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    if (showForgotPasswordDialog) {
        var resetIdentifier by remember { mutableStateOf(loginIdentifier) }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Password", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter your registered email address or mobile number to receive secure reset instructions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedTextSecondary
                    )
                    OutlinedTextField(
                        value = resetIdentifier,
                        onValueChange = { resetIdentifier = it },
                        label = { Text("Email or Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reset_identifier_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestPasswordReset(resetIdentifier) { success, msg ->
                            if (success) {
                                successMessage = msg
                                showForgotPasswordDialog = false
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    modifier = Modifier.testTag("send_reset_link_button")
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Change Account Type Navigation Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onChangeAccountType,
                        modifier = Modifier.testTag("btn_change_account_type")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MedBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Change Account Type",
                            color = MedBluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 1. App Header with Role-Specific Title
            item {
                val roleTitle = when (selectedRole.uppercase()) {
                    "DOCTOR" -> "Doctor Portal"
                    "CARETAKER" -> "Caretaker Access"
                    "ADMIN" -> "Administrator Portal"
                    else -> "Patient Access"
                }

                val roleSubtitle = when (selectedRole.uppercase()) {
                    "DOCTOR" -> "Manage patients, prescriptions & appointments"
                    "CARETAKER" -> "Support an authorized family patient"
                    "ADMIN" -> "Manage the MedTime platform & compliance"
                    else -> "Manage medicines, reminders & health schedules"
                }

                val roleIcon = when (selectedRole.uppercase()) {
                    "DOCTOR" -> Icons.Default.MedicalServices
                    "CARETAKER" -> Icons.Default.PeopleAlt
                    "ADMIN" -> Icons.Default.AdminPanelSettings
                    else -> Icons.Default.Medication
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = when (selectedRole.uppercase()) {
                            "DOCTOR" -> MedSuccess
                            "CARETAKER" -> MedWarning
                            "ADMIN" -> Color(0xFF512DA8)
                            else -> MedBluePrimary
                        },
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = roleIcon,
                                contentDescription = "$roleTitle Logo",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isLoginMode) "$roleTitle Login" else "Create $roleTitle Account",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MedTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = roleSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp
                        ),
                        color = MedTextSecondary
                    )
                }
            }

            // 2. Auth Mode Tab Switcher (Login / Register) - Exclude Admin from public registration
            if (selectedRole.uppercase() != "ADMIN") {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                        color = MedSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isLoginMode = true
                                        errorMessage = null
                                        successMessage = null
                                    }
                                    .testTag("tab_login"),
                                color = if (isLoginMode) MedBluePrimary else Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Log In",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isLoginMode) Color.White else MedTextSecondary
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isLoginMode = false
                                        errorMessage = null
                                        successMessage = null
                                    }
                                    .testTag("tab_register"),
                                color = if (!isLoginMode) MedBluePrimary else Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Create Account",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (!isLoginMode) Color.White else MedTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Status feedback banner
            if (errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MedError.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedError.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MedError)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MedError
                            )
                        }
                    }
                }
            }

            if (successMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MedSuccess.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedSuccess.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = MedSuccess)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = successMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MedSuccess
                            )
                        }
                    }
                }
            }

            // 3. Forms
            if (isLoginMode) {
                // LOGIN FORM
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = MedSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Welcome Back",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Sign in to access your prescriptions, reminders, and clinical records.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )

                            // Email / Mobile Input
                            OutlinedTextField(
                                value = loginIdentifier,
                                onValueChange = {
                                    loginIdentifier = it
                                    errorMessage = null
                                },
                                label = { Text("Email or Mobile Number") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = MedBluePrimary)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_email_input")
                            )

                            // Password Input
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = {
                                    loginPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = MedBluePrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (loginPasswordVisible) "Hide password" else "Show password",
                                            tint = MedTextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        viewModel.login(loginIdentifier, loginPassword, rememberMe) { success, msg ->
                                            if (!success) errorMessage = msg
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input")
                            )

                            // Remember me & Forgot password
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                                ) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MedBluePrimary)
                                    )
                                    Text(
                                        text = "Remember me",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextPrimary
                                    )
                                }

                                TextButton(
                                    onClick = { showForgotPasswordDialog = true },
                                    modifier = Modifier.testTag("forgot_password_button")
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedBluePrimary
                                    )
                                }
                            }

                            // Submit Button
                            val submitButtonText = if (selectedRole.uppercase() == "ADMIN") "SECURE ADMIN LOGIN" else "LOG IN"
                            val submitIcon = if (selectedRole.uppercase() == "ADMIN") Icons.Default.Shield else Icons.Default.Login

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.login(loginIdentifier, loginPassword, rememberMe) { success, msg ->
                                        if (!success) errorMessage = msg
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("login_submit_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedRole.uppercase() == "ADMIN") Color(0xFF512DA8) else MedBluePrimary
                                )
                            ) {
                                Icon(submitIcon, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = submitButtonText,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (selectedRole.uppercase() != "ADMIN") {
                                // ──────── OR ────────
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = MedBorder)
                                    Text(
                                        text = "   OR   ",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextSecondary
                                    )
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = MedBorder)
                                }

                                // Continue with Google
                                OutlinedButton(
                                    onClick = {
                                        viewModel.quickLoginAsRole(selectedRole)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_social_google"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google",
                                        tint = Color(0xFFEA4335),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Google",
                                        color = MedTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }

                                // Continue with Facebook
                                OutlinedButton(
                                    onClick = {
                                        viewModel.quickLoginAsRole(selectedRole)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_social_facebook"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Facebook",
                                        tint = Color(0xFF1877F2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Facebook",
                                        color = MedTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }

                                // Continue with Apple
                                OutlinedButton(
                                    onClick = {
                                        viewModel.quickLoginAsRole(selectedRole)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_social_apple"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneIphone,
                                        contentDescription = "Apple",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Apple",
                                        color = MedTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Create Account switch
                                TextButton(
                                    onClick = {
                                        isLoginMode = false
                                        errorMessage = null
                                        successMessage = null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_create_account_toggle")
                                ) {
                                    Text(
                                        text = "Don't have an account? Create Account",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedBluePrimary
                                    )
                                }
                            } else {
                                // Administrator notice
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEDE7F6),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1C4E9))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFF512DA8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Administrator access is restricted to authorized platform operators. Public registration is disabled.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF4A148C),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            // ← Change Account Type
                            OutlinedButton(
                                onClick = onChangeAccountType,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("btn_change_account_type_card"),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = MedTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "← Change Account Type",
                                    color = MedTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Quick Demo Access Section (Tailored to active role)
                item {
                    val activeRoleTitle = when (selectedRole.uppercase()) {
                        "DOCTOR" -> "Doctor"
                        "CARETAKER" -> "Caretaker"
                        "ADMIN" -> "Administrator"
                        else -> "Patient"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = MedSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MedBluePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1-Tap Demo Credentials ($activeRoleTitle)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                            }

                            Text(
                                text = "Instantly explore MedTime with the configured $activeRoleTitle credentials:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )

                            when (selectedRole.uppercase()) {
                                "DOCTOR" -> DemoRoleItem(
                                    roleName = "Doctor",
                                    accountName = "Dr. Sarah Mitchell, MD",
                                    roleDesc = "Appointments queue, medical license, patient prescriptions",
                                    icon = Icons.Default.MedicalServices,
                                    onClick = { viewModel.quickLoginAsRole("DOCTOR") }
                                )
                                "CARETAKER" -> DemoRoleItem(
                                    roleName = "Caretaker",
                                    accountName = "Emily Davis",
                                    roleDesc = "Family adherence monitor, medication alerts, sync codes",
                                    icon = Icons.Default.PeopleAlt,
                                    onClick = { viewModel.quickLoginAsRole("CARETAKER") }
                                )
                                "ADMIN" -> DemoRoleItem(
                                    roleName = "Administrator",
                                    accountName = "System Administrator",
                                    roleDesc = "Doctor verification approval, security audit logs, governance",
                                    icon = Icons.Default.AdminPanelSettings,
                                    onClick = { viewModel.quickLoginAsRole("ADMIN") }
                                )
                                else -> DemoRoleItem(
                                    roleName = "Patient",
                                    accountName = "Vijay Kumar",
                                    roleDesc = "Medication tracking, today's schedule, SOS & doctor bookings",
                                    icon = Icons.Default.Medication,
                                    onClick = { viewModel.quickLoginAsRole("PATIENT") }
                                )
                            }
                        }
                    }
                }
            } else {
                // SIGN UP FORM
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = MedSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Create Your Account",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Join MedTime for reliable medication adherence and health coordination.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )

                            // Select Role Card Selector
                            Text(
                                text = "Select Account Role",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RoleSelectionChip(
                                    role = "PATIENT",
                                    label = "Patient",
                                    icon = Icons.Default.Person,
                                    isSelected = regRole == "PATIENT",
                                    modifier = Modifier.weight(1f),
                                    onClick = { regRole = "PATIENT" }
                                )
                                RoleSelectionChip(
                                    role = "DOCTOR",
                                    label = "Doctor",
                                    icon = Icons.Default.LocalHospital,
                                    isSelected = regRole == "DOCTOR",
                                    modifier = Modifier.weight(1f),
                                    onClick = { regRole = "DOCTOR" }
                                )
                                RoleSelectionChip(
                                    role = "CARETAKER",
                                    label = "Caretaker",
                                    icon = Icons.Default.VolunteerActivism,
                                    isSelected = regRole == "CARETAKER",
                                    modifier = Modifier.weight(1f),
                                    onClick = { regRole = "CARETAKER" }
                                )
                            }

                            // Full Name
                            OutlinedTextField(
                                value = regName,
                                onValueChange = {
                                    regName = it
                                    errorMessage = null
                                },
                                label = { Text("Full Name *") },
                                leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = MedBluePrimary) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("signup_name_input")
                            )

                            // Email
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = {
                                    regEmail = it
                                    errorMessage = null
                                },
                                label = { Text("Email Address *") },
                                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = MedBluePrimary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("signup_email_input")
                            )

                            // Phone
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = {
                                    regPhone = it
                                    errorMessage = null
                                },
                                label = { Text("Mobile Number *") },
                                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = MedBluePrimary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("signup_phone_input")
                            )

                            // Date of Birth
                            OutlinedTextField(
                                value = regDob,
                                onValueChange = { regDob = it },
                                label = { Text("Date of Birth (YYYY-MM-DD)") },
                                leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MedBluePrimary) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("signup_dob_input")
                            )

                            // If Doctor: Additional fields
                            if (regRole == "DOCTOR") {
                                OutlinedTextField(
                                    value = regSpecialty,
                                    onValueChange = { regSpecialty = it },
                                    label = { Text("Medical Specialty (e.g. Cardiologist)") },
                                    leadingIcon = { Icon(Icons.Outlined.Work, contentDescription = null, tint = MedBluePrimary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("signup_doctor_specialty")
                                )

                                OutlinedTextField(
                                    value = regHospital,
                                    onValueChange = { regHospital = it },
                                    label = { Text("Affiliated Hospital / Clinic") },
                                    leadingIcon = { Icon(Icons.Outlined.Apartment, contentDescription = null, tint = MedBluePrimary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("signup_doctor_hospital")
                                )

                                OutlinedTextField(
                                    value = regLicense,
                                    onValueChange = { regLicense = it },
                                    label = { Text("Medical License ID") },
                                    leadingIcon = { Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = MedBluePrimary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("signup_doctor_license")
                                )
                            }

                            // Password
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = {
                                    regPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Password (min 6 characters) *") },
                                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = MedBluePrimary) },
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = MedTextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("signup_password_input")
                            )

                            // Confirm Password
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = {
                                    regConfirmPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm Password *") },
                                leadingIcon = { Icon(Icons.Outlined.LockClock, contentDescription = null, tint = MedBluePrimary) },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("signup_confirm_password_input")
                            )

                            // Create Account Button
                            Button(
                                onClick = {
                                    if (regPassword != regConfirmPassword) {
                                        errorMessage = "Passwords do not match."
                                        return@Button
                                    }
                                    focusManager.clearFocus()
                                    viewModel.registerUser(
                                        name = regName,
                                        email = regEmail,
                                        role = regRole,
                                        phone = regPhone,
                                        password = regPassword,
                                        dob = regDob,
                                        specialty = regSpecialty,
                                        hospital = regHospital,
                                        license = regLicense
                                    ) { success, msg ->
                                        if (!success) errorMessage = msg
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("signup_submit_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CREATE ACCOUNT",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Privacy & Compliance Note
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MedBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HIPAA & GDPR Compliant Medical Data Encryption",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoRoleItem(
    roleName: String,
    accountName: String,
    roleDesc: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(1.dp, MedBorder, RoundedCornerShape(12.dp)),
        color = MedBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MedBlueLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = roleName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedBluePrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $accountName",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MedTextPrimary
                    )
                }
                Text(
                    text = roleDesc,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MedTextSecondary,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MedTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RoleSelectionChip(
    role: String,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MedBluePrimary else MedBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isSelected) MedBlueLight.copy(alpha = 0.5f) else MedSurface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MedBluePrimary else MedTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) MedBluePrimary else MedTextPrimary
            )
        }
    }
}
