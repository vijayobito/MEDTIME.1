package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalDocumentEntity
import com.example.ui.components.AddDocumentDialog
import com.example.ui.components.DocumentDetailsAndPreviewDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFormatFilter by remember { mutableStateOf("ALL") } // "ALL", "PDF", "IMAGE", "STARRED"
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var docToDelete by remember { mutableStateOf<MedicalDocumentEntity?>(null) }
    var docToView by remember { mutableStateOf<MedicalDocumentEntity?>(null) }

    val categories = listOf("All", "Prescription", "Lab Report", "Scan", "Doctor Notes", "Discharge Summary", "Insurance")

    if (showAddDialog) {
        AddDocumentDialog(
            onDismiss = { showAddDialog = false },
            onAddDocument = { title, type, clinic, date, size, notes, fileName, fileFormat, mimeType, fileUri, fileSizeBytes, pageCount, resolution, tags, isFavorite ->
                viewModel.addDocument(
                    title = title,
                    type = type,
                    clinic = clinic,
                    date = date,
                    size = size,
                    notes = notes,
                    fileName = fileName,
                    fileFormat = fileFormat,
                    mimeType = mimeType,
                    fileUri = fileUri,
                    fileSizeBytes = fileSizeBytes,
                    pageCount = pageCount,
                    resolution = resolution,
                    tags = tags,
                    isFavorite = isFavorite
                )
            }
        )
    }

    if (docToDelete != null) {
        val doc = docToDelete!!
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text("Delete Document?") },
            text = { Text("Are you sure you want to delete '${doc.title}' (${doc.fileName})? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDocument(doc.id)
                        docToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (docToView != null) {
        DocumentDetailsAndPreviewDialog(
            document = docToView!!,
            onDismiss = { docToView = null },
            onToggleFavorite = { fav ->
                viewModel.toggleDocumentFavorite(docToView!!.id, fav)
                docToView = docToView!!.copy(isFavorite = fav)
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MedBluePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.UploadFile, contentDescription = "Upload") },
                text = { Text("Upload Document", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_document")
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MedBackground)
        ) {
            // 1. Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MedSurface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by title, provider, tag, or notes...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MedTextSecondary, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_documents"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MedBackground,
                            focusedContainerColor = MedBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Format Filter Chips: All, PDFs, Images/Scans, Starred
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFormatFilter == "ALL",
                            onClick = { selectedFormatFilter = "ALL" },
                            label = { Text("All (${documents.size})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "ALL") FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedFormatFilter == "PDF",
                            onClick = { selectedFormatFilter = "PDF" },
                            label = { Text("PDFs (${documents.count { it.fileFormat.equals("PDF", ignoreCase = true) }})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "PDF") FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.weight(1.1f)
                        )

                        FilterChip(
                            selected = selectedFormatFilter == "IMAGE",
                            onClick = { selectedFormatFilter = "IMAGE" },
                            label = { Text("Scans (${documents.count { it.fileFormat.equals("IMAGE", ignoreCase = true) }})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "IMAGE") FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(Icons.Default.PermMedia, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.weight(1.1f)
                        )

                        FilterChip(
                            selected = selectedFormatFilter == "STARRED",
                            onClick = { selectedFormatFilter = "STARRED" },
                            label = { Text("Starred (${documents.count { it.isFavorite }})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "STARRED") FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }
            }

            // 2. Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MedSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // 3. Storage & Document Metrics Strip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(label = "Total Records", value = "${documents.size}")
                    MetricItem(label = "PDF Files", value = "${documents.count { it.fileFormat.equals("PDF", ignoreCase = true) }}")
                    MetricItem(label = "Medical Scans", value = "${documents.count { it.fileFormat.equals("IMAGE", ignoreCase = true) }}")
                    MetricItem(
                        label = "Storage Used",
                        value = run {
                            val totalBytes = documents.sumOf { it.fileSizeBytes.takeIf { s -> s > 0 } ?: 1048576L }
                            val mb = totalBytes.toDouble() / (1024.0 * 1024.0)
                            String.format("%.1f MB", mb)
                        }
                    )
                }
            }

            // Filter computation
            val filtered = documents.filter { doc ->
                val matchesFormat = when (selectedFormatFilter) {
                    "PDF" -> doc.fileFormat.equals("PDF", ignoreCase = true)
                    "IMAGE" -> doc.fileFormat.equals("IMAGE", ignoreCase = true)
                    "STARRED" -> doc.isFavorite
                    else -> true
                }
                val matchesCategory = selectedCategory == "All" || doc.type.equals(selectedCategory, ignoreCase = true)
                val matchesSearch = searchQuery.isBlank() ||
                        doc.title.contains(searchQuery, ignoreCase = true) ||
                        doc.doctorOrClinic.contains(searchQuery, ignoreCase = true) ||
                        doc.fileName.contains(searchQuery, ignoreCase = true) ||
                        doc.tags.contains(searchQuery, ignoreCase = true) ||
                        doc.notes.contains(searchQuery, ignoreCase = true)

                matchesFormat && matchesCategory && matchesSearch
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No Matching Documents" else "No Documents Found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try refining your query or clear format filters." else "Upload your clinical records, laboratory PDFs, or imaging scans to keep them organized.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Medical Record")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { doc ->
                        val isPdf = doc.fileFormat.equals("PDF", ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
                                .clickable { docToView = doc },
                            colors = CardDefaults.cardColors(containerColor = MedSurface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Format Icon
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isPdf) Color(0xFFFFEBEE) else Color(0xFFEDE7F6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.PermMedia,
                                                contentDescription = null,
                                                tint = if (isPdf) Color(0xFFD32F2F) else Color(0xFF673AB7),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = doc.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MedTextPrimary
                                            )
                                            Text(
                                                text = doc.fileName.ifBlank { if (isPdf) "report.pdf" else "diagnostic_scan.jpg" },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MedBlueDark,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "${doc.doctorOrClinic} • ${doc.dateAdded}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MedTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleDocumentFavorite(doc.id, !doc.isFavorite) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (doc.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Favorite",
                                            tint = if (doc.isFavorite) Color(0xFFFFB300) else MedTextTertiary
                                        )
                                    }
                                }

                                // Technical metadata pills
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isPdf) Color(0xFFFFEBEE) else Color(0xFFEDE7F6)
                                    ) {
                                        Text(
                                            text = doc.fileFormat,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPdf) Color(0xFFD32F2F) else Color(0xFF673AB7)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MedBlueLight
                                    ) {
                                        Text(
                                            text = doc.type,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MedBluePrimary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFEEEEEE)
                                    ) {
                                        Text(
                                            text = if (isPdf) "${doc.pageCount} pages" else doc.resolution.ifBlank { "High Res" },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            color = MedTextSecondary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFEEEEEE)
                                    ) {
                                        Text(
                                            text = doc.fileSize,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            color = MedTextSecondary
                                        )
                                    }
                                }

                                if (doc.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = doc.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextSecondary,
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MedBorder, thickness = 0.6.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Status: ${doc.uploadStatus}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedSuccess,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { docToView = doc },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Inspect & Preview", fontSize = 11.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = doc.mimeType
                                                    putExtra(Intent.EXTRA_SUBJECT, "Medical Record: ${doc.title}")
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "Medical Record: ${doc.title}\nFacility: ${doc.doctorOrClinic}\nDate: ${doc.dateAdded}\nSummary: ${doc.notes}"
                                                    )
                                                }
                                                val shareIntent = Intent.createChooser(sendIntent, "Share Document")
                                                context.startActivity(shareIntent)
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = { docToDelete = doc },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MedError, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary, fontSize = 10.sp)
    }
}
