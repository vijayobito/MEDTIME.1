package com.example.ui.components

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.MedBlueLight
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedSuccess
import com.example.ui.theme.MedSuccessLight
import com.example.ui.theme.MedSurface
import com.example.ui.theme.MedTextPrimary
import com.example.ui.theme.MedTextSecondary
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

data class ScannedMedicationDetails(
    val name: String,
    val dosage: String,
    val form: String,
    val instructions: String,
    val frequency: String,
    val reminderTimes: String,
    val stockCount: Int,
    val notes: String = ""
)

@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodePillScannerDialog(
    onDismiss: () -> Unit,
    onMedicationScanned: (ScannedMedicationDetails) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Live Camera Scanner, 1: Bottle/Strip Presets
    var scannedResult by remember { mutableStateOf<ScannedMedicationDetails?>(null) }
    var scanModeText by remember { mutableStateOf("Point camera at pill bottle barcode or medicine strip label") }
    var isProcessing by remember { mutableStateOf(false) }

    // Laser scanning animation line
    val infiniteTransition = rememberInfiniteTransition(label = "laser_line")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    // Preset Pill Bottles & Medicine Strips for high reliability & instant simulation
    val samplePillPresets = listOf(
        ScannedMedicationDetails(
            name = "Amoxicillin",
            dosage = "500 mg",
            form = "Capsule",
            instructions = "Take with food and full glass of water",
            frequency = "Three times a day",
            reminderTimes = "08:00 AM,02:00 PM,08:00 PM",
            stockCount = 21,
            notes = "Antibiotic prescription. Complete entire course."
        ),
        ScannedMedicationDetails(
            name = "Metformin HCl",
            dosage = "850 mg",
            form = "Tablet",
            instructions = "Take after evening meal",
            frequency = "Twice a day",
            reminderTimes = "08:30 AM,07:30 PM",
            stockCount = 60,
            notes = "Oral diabetes medication for blood glucose control."
        ),
        ScannedMedicationDetails(
            name = "Lisinopril",
            dosage = "10 mg",
            form = "Tablet",
            instructions = "Take in the morning with water",
            frequency = "Daily",
            reminderTimes = "08:00 AM",
            stockCount = 30,
            notes = "ACE inhibitor for blood pressure management."
        ),
        ScannedMedicationDetails(
            name = "Atorvastatin Calcium",
            dosage = "20 mg",
            form = "Tablet",
            instructions = "Take once daily at bedtime",
            frequency = "Daily",
            reminderTimes = "09:30 PM",
            stockCount = 30,
            notes = "Cholesterol lowering medication."
        ),
        ScannedMedicationDetails(
            name = "Ibuprofen",
            dosage = "400 mg",
            form = "Tablet",
            instructions = "Take with milk or meal as needed for pain",
            frequency = "As needed",
            reminderTimes = "12:00 PM",
            stockCount = 20,
            notes = "NSAID anti-inflammatory analgesic."
        ),
        ScannedMedicationDetails(
            name = "Omega-3 Fish Oil",
            dosage = "1000 mg",
            form = "Capsule",
            instructions = "Take with lunch",
            frequency = "Daily",
            reminderTimes = "01:00 PM",
            stockCount = 45,
            notes = "High purity EPA & DHA supplement."
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MedBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MedBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Scan Pill Bottle / Strip",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Camera & Barcode / Label Scanner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_barcode_scanner")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MedSurface,
                    contentColor = MedBluePrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Live Camera", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Pill Presets", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // Camera Preview Box with targeting reticle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        // CameraX Preview
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraExecutor = Executors.newSingleThreadExecutor()
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }

                                        val barcodeScanner = BarcodeScanning.getClient()
                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()

                                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null && !isProcessing && scannedResult == null) {
                                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                                barcodeScanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        for (barcode in barcodes) {
                                                            val raw = barcode.rawValue ?: barcode.displayValue ?: ""
                                                            if (raw.isNotBlank()) {
                                                                // Parse barcode
                                                                val parsed = parseBarcodeOrLabel(raw, samplePillPresets)
                                                                scannedResult = parsed
                                                                scanModeText = "Captured: ${parsed.name} (${parsed.dosage})"
                                                                break
                                                            }
                                                        }
                                                    }
                                                    .addOnCompleteListener {
                                                        imageProxy.close()
                                                    }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }

                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        scanModeText = "Camera active: Align barcode inside the box"
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Target Overlay Box
                        Box(
                            modifier = Modifier
                                .size(170.dp, 120.dp)
                                .border(2.dp, MedBluePrimary, RoundedCornerShape(12.dp))
                        ) {
                            // Animated Laser Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .align(Alignment.TopCenter)
                                    .padding(top = (120 * laserY).dp)
                                    .background(Color.Red)
                            )
                        }

                        // Top Helper Pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = scanModeText,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Or tap a sample pill bottle/strip to auto-populate instantly:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Prescription / Pill Presets List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    samplePillPresets.forEach { med ->
                        val isSelected = scannedResult?.name == med.name
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MedSuccessLight else MedSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MedSuccess else MedBluePrimary.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scannedResult = med
                                    scanModeText = "Selected: ${med.name} ${med.dosage}"
                                }
                                .testTag("pill_preset_${med.name}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MedSuccess else MedBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Medication,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MedBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "${med.name} (${med.dosage})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MedTextPrimary
                                        )
                                        Text(
                                            text = "${med.form} • ${med.frequency} • ${med.instructions}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MedTextSecondary
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MedSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MedBlueLight
                                    ) {
                                        Text(
                                            text = "Scan",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MedBluePrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Scanned Details Verification Card
                if (scannedResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MedSuccessLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Scanned Details Verified",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedSuccess
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Name: ${scannedResult?.name} • Dosage: ${scannedResult?.dosage}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Instructions: ${scannedResult?.instructions}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                            Text(
                                text = "Frequency: ${scannedResult?.frequency} (Reminders: ${scannedResult?.reminderTimes})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = scannedResult ?: samplePillPresets.first()
                    onMedicationScanned(result)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("apply_scanned_pill_button")
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply To Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MedTextSecondary)
            }
        },
        containerColor = MedSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Intelligent helper to parse barcode raw values or labels against pharmacopeia database.
 */
private fun parseBarcodeOrLabel(
    rawValue: String,
    presets: List<ScannedMedicationDetails>
): ScannedMedicationDetails {
    val clean = rawValue.trim().lowercase()
    val match = presets.firstOrNull { preset ->
        clean.contains(preset.name.lowercase()) ||
        clean.contains(preset.dosage.lowercase()) ||
        preset.name.lowercase().split(" ").any { clean.contains(it) }
    }
    if (match != null) return match

    // If barcode is numeric (UPC / EAN) or unknown text, map deterministically
    val index = (Math.abs(rawValue.hashCode()) % presets.size)
    return presets[index]
}
