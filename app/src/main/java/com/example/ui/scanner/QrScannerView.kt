package com.example.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.*
import com.example.util.QrAccountPayload
import com.example.util.QrCodeUtils
import java.util.concurrent.Executors

/**
 * High-performance QR Code Scanner Composable using CameraX & Google ML Kit.
 * Includes live camera preview, scanning target overlay with laser animation,
 * flashlight toggle, camera lens switch, and instant link confirmation sheet.
 */
@Composable
fun QrScannerView(
    onAccountScanned: (QrAccountPayload) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialTargetRole: String = "ALL" // "PATIENT", "DOCTOR", "CARETAKER", "ALL"
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var detectedPayload by remember { mutableStateOf<QrAccountPayload?>(null) }
    var showManualInputDialog by remember { mutableStateOf(false) }
    var showDemoPresets by remember { mutableStateOf(false) }

    // Laser scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("qr_scanner_container")
    ) {
        if (hasCameraPermission) {
            // CameraX Live Preview View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val analyzer = QrCodeAnalyzer { rawString ->
                            val parsed = QrCodeUtils.parseQrPayload(rawString)
                            if (parsed != null && detectedPayload == null) {
                                detectedPayload = parsed
                            }
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, analyzer)
                            }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            provider.unbindAll()
                            val cam = provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraControl = cam.cameraControl
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = {
                    // Update camera selector if lens facing changes
                    val provider = cameraProvider
                    if (provider != null) {
                        try {
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()
                            provider.unbindAll()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = it.surfaceProvider
                            }
                            val analyzer = QrCodeAnalyzer { rawString ->
                                val parsed = QrCodeUtils.parseQrPayload(rawString)
                                if (parsed != null && detectedPayload == null) {
                                    detectedPayload = parsed
                                }
                            }
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
                                }
                            val cam = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                            cameraControl = cam.cameraControl
                            cameraControl?.enableTorch(isTorchOn)
                        } catch (_: Exception) {}
                    }
                }
            )

            // Scanning Overlay & Viewfinder
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val boxSize = (canvasWidth * 0.72f).coerceAtMost(320.dp.toPx())
                val left = (canvasWidth - boxSize) / 2
                val top = (canvasHeight - boxSize) / 2 - 40.dp.toPx()

                // Dimmed surrounding background
                // Top
                drawRect(Color(0x88000000), topLeft = Offset(0f, 0f), size = Size(canvasWidth, top))
                // Bottom
                drawRect(Color(0x88000000), topLeft = Offset(0f, top + boxSize), size = Size(canvasWidth, canvasHeight - (top + boxSize)))
                // Left
                drawRect(Color(0x88000000), topLeft = Offset(0f, top), size = Size(left, boxSize))
                // Right
                drawRect(Color(0x88000000), topLeft = Offset(left + boxSize, top), size = Size(canvasWidth - (left + boxSize), boxSize))

                // Viewfinder Border with rounded corners
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(left, top),
                    size = Size(boxSize, boxSize),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Corner Brackets (Accent)
                val cornerLength = 28.dp.toPx()
                val cornerStroke = 4.5.dp.toPx()
                val accentColor = Color(0xFF29B6F6)

                // Top-Left
                drawLine(accentColor, Offset(left, top + 8.dp.toPx()), Offset(left + cornerLength, top + 8.dp.toPx()), cornerStroke, StrokeCap.Round)
                drawLine(accentColor, Offset(left + 8.dp.toPx(), top), Offset(left + 8.dp.toPx(), top + cornerLength), cornerStroke, StrokeCap.Round)

                // Top-Right
                drawLine(accentColor, Offset(left + boxSize - cornerLength, top + 8.dp.toPx()), Offset(left + boxSize, top + 8.dp.toPx()), cornerStroke, StrokeCap.Round)
                drawLine(accentColor, Offset(left + boxSize - 8.dp.toPx(), top), Offset(left + boxSize - 8.dp.toPx(), top + cornerLength), cornerStroke, StrokeCap.Round)

                // Bottom-Left
                drawLine(accentColor, Offset(left, top + boxSize - 8.dp.toPx()), Offset(left + cornerLength, top + boxSize - 8.dp.toPx()), cornerStroke, StrokeCap.Round)
                drawLine(accentColor, Offset(left + 8.dp.toPx(), top + boxSize - cornerLength), Offset(left + 8.dp.toPx(), top + boxSize), cornerStroke, StrokeCap.Round)

                // Bottom-Right
                drawLine(accentColor, Offset(left + boxSize - cornerLength, top + boxSize - 8.dp.toPx()), Offset(left + boxSize, top + boxSize - 8.dp.toPx()), cornerStroke, StrokeCap.Round)
                drawLine(accentColor, Offset(left + boxSize - 8.dp.toPx(), top + boxSize - cornerLength), Offset(left + boxSize - 8.dp.toPx(), top + boxSize), cornerStroke, StrokeCap.Round)

                // Animated Laser Line
                if (detectedPayload == null) {
                    val laserY = top + (boxSize * laserPosition)
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF00E5FF),
                                Color.White,
                                Color(0xFF00E5FF),
                                Color.Transparent
                            )
                        ),
                        start = Offset(left + 10.dp.toPx(), laserY),
                        end = Offset(left + boxSize - 10.dp.toPx(), laserY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        } else {
            // Camera Permission Denied / Request View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MedBluePrimary.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MedBluePrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Camera Permission Needed",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "MedTime uses your device camera with Google ML Kit to instantly scan patient and doctor QR codes for secure account linking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_grant_camera_permission")
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant Camera Access")
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showManualInputDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().testTag("btn_manual_code_fallback")
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter Linking Code Manually")
                }
            }
        }

        // Top Controls Bar (Back, Title, Flashlight, Flip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .testTag("btn_close_scanner")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            Text(
                text = "Scan QR Code",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Flashlight / Torch Toggle
                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isTorchOn) MedBluePrimary else Color.Black.copy(alpha = 0.6f))
                        .testTag("btn_toggle_torch")
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = Color.White
                    )
                }

                // Camera Switch (Back/Front)
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("btn_switch_camera")
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera", tint = Color.White)
                }
            }
        }

        // Bottom Action Bar & Instructions (when no code detected yet)
        if (detectedPayload == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Align patient or doctor QR code inside frame",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }

                // Action Buttons: Manual Entry & Quick Demo Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showManualInputDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("btn_manual_entry")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enter Code", color = Color.White, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { showDemoPresets = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("btn_demo_presets")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Demo Codes", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        // Scanned Result Bottom Confirmation Sheet
        AnimatedVisibility(
            visible = detectedPayload != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val payload = detectedPayload
            if (payload != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("scanned_result_sheet"),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = MedSuccessLight
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess)
                                    }
                                }
                                Column {
                                    Text(
                                        text = "QR Code Detected",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    Text(
                                        text = "Verified MedTime Account",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (payload.role.uppercase()) {
                                    "DOCTOR" -> MedBlueLight
                                    "CARETAKER" -> MedPurpleLight
                                    else -> MedSuccessLight
                                }
                            ) {
                                Text(
                                    text = payload.displayRole,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (payload.role.uppercase()) {
                                        "DOCTOR" -> MedBluePrimary
                                        "CARETAKER" -> MedPurple
                                        else -> MedSuccess
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MedBorder)

                        // Scanned Account Details Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MedBackground
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MedBluePrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (payload.role.uppercase()) {
                                            "DOCTOR" -> Icons.Default.MedicalServices
                                            "CARETAKER" -> Icons.Default.FamilyRestroom
                                            else -> Icons.Default.Person
                                        },
                                        contentDescription = null,
                                        tint = MedBluePrimary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = payload.name.ifBlank { "Healthcare Member" },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    if (payload.specialty.isNotBlank()) {
                                        Text(
                                            text = payload.specialty,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MedBluePrimary
                                        )
                                    }
                                    Text(
                                        text = "Code: ${payload.code}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MedTextSecondary
                                    )
                                }
                            }
                        }

                        // Actions: Confirm & Link vs Scan Again
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { detectedPayload = null },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("btn_rescan_qr")
                            ) {
                                Text("Rescan")
                            }

                            Button(
                                onClick = {
                                    onAccountScanned(payload)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.5f).testTag("btn_confirm_qr_link")
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Link Account")
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Code Input Dialog
    if (showManualInputDialog) {
        var manualCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showManualInputDialog = false },
            title = {
                Text("Enter Account Linking Code", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter the 8-character linking code displayed on the patient or doctor's screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { manualCode = it },
                        placeholder = { Text("e.g. MED-7842") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_manual_qr_code")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualCode.isNotBlank()) {
                            val parsed = QrCodeUtils.parseQrPayload(manualCode.trim())
                            if (parsed != null) {
                                detectedPayload = parsed
                                showManualInputDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("Lookup Code")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Quick Demo Presets Dialog
    if (showDemoPresets) {
        AlertDialog(
            onDismissRequest = { showDemoPresets = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MedBluePrimary)
                    Text("Select Demo QR Code", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Simulate instant camera scan using built-in verified MedTime user accounts:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )

                    // Patient Preset 1
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                detectedPayload = QrAccountPayload(
                                    code = "MED-7842",
                                    role = "PATIENT",
                                    userId = "p1",
                                    name = "John Doe (Patient)",
                                    email = "john.doe@medtime.com"
                                )
                                showDemoPresets = false
                            },
                        color = MedBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MedSuccess)
                            Column {
                                Text("John Doe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Patient • Code: MED-7842", fontSize = 12.sp, color = MedTextSecondary)
                            }
                        }
                    }

                    // Doctor Preset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                detectedPayload = QrAccountPayload(
                                    code = "DOC-9021",
                                    role = "DOCTOR",
                                    userId = "d1",
                                    name = "Dr. Robert Vance, MD",
                                    specialty = "Cardiology & Internal Medicine",
                                    hospital = "Metro General Hospital",
                                    email = "dr.vance@metrohospital.org"
                                )
                                showDemoPresets = false
                            },
                        color = MedBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedBluePrimary)
                            Column {
                                Text("Dr. Robert Vance, MD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Cardiologist • Code: DOC-9021", fontSize = 12.sp, color = MedTextSecondary)
                            }
                        }
                    }

                    // Caretaker Preset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                detectedPayload = QrAccountPayload(
                                    code = "CARE-3341",
                                    role = "CARETAKER",
                                    userId = "c1",
                                    name = "Sarah Doe (Caregiver)",
                                    email = "sarah.doe@medtime.com"
                                )
                                showDemoPresets = false
                            },
                        color = MedBackground
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = MedPurple)
                            Column {
                                Text("Sarah Doe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Family Caregiver • Code: CARE-3341", fontSize = 12.sp, color = MedTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDemoPresets = false }) {
                    Text("Close")
                }
            }
        )
    }
}
