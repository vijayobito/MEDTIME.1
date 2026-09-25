package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardStats

/**
 * Lightweight, high-performance daily medicine adherence chart
 * visualizing Taken, Missed, and Pending doses using Jetpack Compose Canvas graphics.
 */
@Composable
fun DailyAdherenceChartCard(
    stats: DashboardStats,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val total = stats.totalScheduled
    val taken = stats.takenCount
    val missed = stats.missedCount
    val pending = stats.pendingCount

    val takenPct = if (total > 0) ((taken.toFloat() / total) * 100).toInt() else 100
    val missedPct = if (total > 0) ((missed.toFloat() / total) * 100).toInt() else 0
    val pendingPct = if (total > 0) ((pending.toFloat() / total) * 100).toInt() else 0

    // Animation progress
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(taken, missed, pending, total) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MedBorder, RoundedCornerShape(20.dp))
            .testTag("daily_adherence_chart_card"),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
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
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MedBlueLight
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MedBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Daily Adherence Chart",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Taken vs. Missed vs. Pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }

                // Adherence Status Pill
                val (statusText, statusBg, statusColor) = when {
                    total == 0 -> Triple("No Doses", MedBlueLight, MedBlueDark)
                    missed > 0 -> Triple("$missed Missed", MedErrorLight, MedError)
                    pending == 0 && taken == total -> Triple("100% Perfect", MedSuccessLight, MedSuccess)
                    taken > 0 -> Triple("On Track", MedSuccessLight, MedSuccess)
                    else -> Triple("Pending", MedWarningLight, MedWarning)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }
            }

            HorizontalDivider(color = MedBorder)

            // Main Visualizer Row: Donut Chart + Central Gauge + Breakdown Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Animated Canvas Donut Ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .testTag("adherence_donut_chart"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Background Track
                        drawArc(
                            color = Color(0xFFECEFF1),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth)
                        )

                        if (total > 0) {
                            val anim = animationProgress.value
                            val takenSweep = (taken.toFloat() / total) * 360f * anim
                            val missedSweep = (missed.toFloat() / total) * 360f * anim
                            val pendingSweep = (pending.toFloat() / total) * 360f * anim

                            var currentStart = -90f

                            // Taken segment (Green)
                            if (takenSweep > 0f) {
                                drawArc(
                                    color = Color(0xFF2E7D32),
                                    startAngle = currentStart,
                                    sweepAngle = takenSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                currentStart += takenSweep
                            }

                            // Missed segment (Red)
                            if (missedSweep > 0f) {
                                drawArc(
                                    color = Color(0xFFD32F2F),
                                    startAngle = currentStart,
                                    sweepAngle = missedSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                currentStart += missedSweep
                            }

                            // Pending segment (Amber/Orange)
                            if (pendingSweep > 0f) {
                                drawArc(
                                    color = Color(0xFFF57C00),
                                    startAngle = currentStart,
                                    sweepAngle = pendingSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        } else {
                            // Empty state track
                            drawArc(
                                color = Color(0xFF81C784),
                                startAngle = -90f,
                                sweepAngle = 360f * animationProgress.value,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Central Center Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (total > 0) "${stats.adherencePercent}%" else "100%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = when {
                                total == 0 -> MedSuccess
                                stats.adherencePercent >= 80 -> MedSuccess
                                stats.adherencePercent >= 50 -> MedWarning
                                else -> MedError
                            }
                        )
                        Text(
                            text = "Adherence",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MedTextSecondary
                        )
                    }
                }

                // Right-side Percentage & Quantity Breakdown
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Taken Dose Metric
                    AdherenceMetricRow(
                        label = "Taken Doses",
                        count = taken,
                        total = total,
                        percent = takenPct,
                        color = Color(0xFF2E7D32),
                        bgColor = Color(0xFFE8F5E9),
                        testTag = "adherence_stat_taken"
                    )

                    // Missed Dose Metric
                    AdherenceMetricRow(
                        label = "Missed / Skipped",
                        count = missed,
                        total = total,
                        percent = missedPct,
                        color = Color(0xFFD32F2F),
                        bgColor = Color(0xFFFFEBEE),
                        testTag = "adherence_stat_missed"
                    )

                    // Pending Dose Metric
                    AdherenceMetricRow(
                        label = "Pending Today",
                        count = pending,
                        total = total,
                        percent = pendingPct,
                        color = Color(0xFFF57C00),
                        bgColor = Color(0xFFFFF3E0),
                        testTag = "adherence_stat_pending"
                    )
                }
            }

            // Summary Info / Health Context Note
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = when {
                    missed > 0 -> MedErrorLight.copy(alpha = 0.5f)
                    stats.adherencePercent == 100 && total > 0 -> MedSuccessLight.copy(alpha = 0.5f)
                    else -> MedBlueLight.copy(alpha = 0.5f)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = when {
                            missed > 0 -> Icons.Default.WarningAmber
                            stats.adherencePercent == 100 && total > 0 -> Icons.Default.CheckCircle
                            else -> Icons.Default.Lightbulb
                        },
                        contentDescription = null,
                        tint = when {
                            missed > 0 -> MedError
                            stats.adherencePercent == 100 && total > 0 -> MedSuccess
                            else -> MedBluePrimary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when {
                            total == 0 -> "No prescriptions scheduled for today. Add medications to track daily adherence."
                            missed > 0 -> "You have $missed missed dose today. Please consult your physician before taking double doses."
                            pending == 0 && taken == total -> "Great job! All $total scheduled doses have been taken on time today."
                            taken > 0 -> "Good progress! $taken of $total doses logged. Remember to take remaining doses on time."
                            else -> "$pending doses scheduled for today. Timely intake helps maintain optimal therapeutic levels."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MedTextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AdherenceMetricRow(
    label: String,
    count: Int,
    total: Int,
    percent: Int,
    color: Color,
    bgColor: Color,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MedTextPrimary
                )
            }

            Text(
                text = "$count ($percent%)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }

        // Horizontal Mini Progress Indicator
        LinearProgressIndicator(
            progress = { if (total > 0) (count.toFloat() / total) else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = bgColor
        )
    }
}
