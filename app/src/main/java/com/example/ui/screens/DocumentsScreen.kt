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
    val recycleBinDocuments by viewModel.recycleBinDocuments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFormatFilter by remember { mutableStateOf("ALL") } // "ALL", "PDF", "IMAGE", "STARRED", "RECYCLE_BIN"
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var docToDelete by remember { mutableStateOf<MedicalDocumentEntity?>(null) }
    var docToPermanentlyDelete by remember { mutableStateOf<MedicalDocumentEntity?>(null) }
    var showEmptyRecycleBinDialog by remember { mutableStateOf(false) }
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
            title = { Text("Move to Recycle Bin?") },
            text = { Text("Move '${doc.title}' to the Recycle Bin? You can restore it anytime or delete it permanently later.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDocument(doc.id)
                        docToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    Text("Move to Bin")
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (docToPermanentlyDelete != null) {
        val doc = docToPermanentlyDelete!!
        AlertDialog(
            onDismissRequest = { docToPermanentlyDelete = null },
            title = { Text("Permanently Delete?") },
            text = { Text("This will permanently remove '${doc.title}' (${doc.fileName}). This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentlyDeleteDocument(doc.id)
                        docToPermanentlyDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { docToPermanentlyDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEmptyRecycleBinDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyRecycleBinDialog = false },
            title = { Text("Empty Recycle Bin?") },
            text = { Text("Are you sure you want to permanently delete all ${recycleBinDocuments.size} item(s) in the Recycle Bin? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyRecycleBin()
                        showEmptyRecycleBinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Empty Bin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyRecycleBinDialog = false }) {
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
            },
            onAcceptPrescription = {
                viewModel.acceptPrescriptionIntoReminders(docToView!!.id)
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

                    // Format Filter Chips: All, PDFs, Images/Scans, Starred, Recycle Bin
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFormatFilter == "ALL",
                                onClick = { selectedFormatFilter = "ALL" },
                                label = { Text("All (${documents.size})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "ALL") FontWeight.Bold else FontWeight.Normal) }
                            )
                        }

                        item {
                            FilterChip(
                                selected = selectedFormatFilter == "PDF",
                                onClick = { selectedFormatFilter = "PDF" },
                                label = { Text("PDFs (${documents.count { it.fileFormat.equals("PDF", ignoreCase = true) }})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "PDF") FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                                }
                            )
                        }

                        item {
                            FilterChip(
                                selected = selectedFormatFilter == "IMAGE",
                                onClick = { selectedFormatFilter = "IMAGE" },
                                label = { Text("Scans (${documents.count { it.fileFormat.equals("IMAGE", ignoreCase = true) }})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "IMAGE") FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    Icon(Icons.Default.PermMedia, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(14.dp))
                                }
                            )
                        }

                        item {
                            FilterChip(
                                selected = selectedFormatFilter == "STARRED",
                                onClick = { selectedFormatFilter = "STARRED" },
                                label = { Text("Starred (${documents.count { it.isFavorite }})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "STARRED") FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                }
                            )
                        }

                        item {
                            FilterChip(
                                selected = selectedFormatFilter == "RECYCLE_BIN",
                                onClick = { selectedFormatFilter = "RECYCLE_BIN" },
                                label = { Text("Recycle Bin (${recycleBinDocuments.size})", fontSize = 11.sp, fontWeight = if (selectedFormatFilter == "RECYCLE_BIN") FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = if (selectedFormatFilter == "RECYCLE_BIN") Color.White else Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE65100),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 2. Category Filter Row (only when in active documents)
            if (selectedFormatFilter != "RECYCLE_BIN") {
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
            }

            // 3. Storage & Document Metrics Strip or Recycle Bin Header
            if (selectedFormatFilter == "RECYCLE_BIN") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFE65100))
                            Column {
                                Text(
                                    text = "Recycle Bin (${recycleBinDocuments.size} files)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = "Restore items back to records or delete them forever.",
                                    fontSize = 11.sp,
                                    color = MedTextSecondary
                                )
                            }
                        }

                        if (recycleBinDocuments.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showEmptyRecycleBinDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Empty Bin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
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
            }

            // Filter computation
            val activeList = if (selectedFormatFilter == "RECYCLE_BIN") recycleBinDocuments else documents
            val filtered = activeList.filter { doc ->
                val matchesFormat = when (selectedFormatFilter) {
                    "PDF" -> doc.fileFormat.equals("PDF", ignoreCase = true)
                    "IMAGE" -> doc.fileFormat.equals("IMAGE", ignoreCase = true)
                    "STARRED" -> doc.isFavorite
                    else -> true
                }
                val matchesCategory = selectedFormatFilter == "RECYCLE_BIN" || selectedCategory == "All" || doc.type.equals(selectedCategory, ignoreCase = true)
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
                        Icon(
                            imageVector = if (selectedFormatFilter == "RECYCLE_BIN") Icons.Default.DeleteOutline else Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MedTextTertiary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedFormatFilter == "RECYCLE_BIN") "Recycle Bin is Empty" else (if (searchQuery.isNotBlank()) "No Matching Documents" else "No Documents Found"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedFormatFilter == "RECYCLE_BIN") "Any medical files you delete will appear here safely for recovery." else (if (searchQuery.isNotBlank()) "Try refining your query or clear format filters." else "Upload your clinical records, laboratory PDFs, or imaging scans to keep them organized."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                        if (selectedFormatFilter != "RECYCLE_BIN") {
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
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { doc ->
                        val isPdf = doc.fileFormat.equals("PDF", ignoreCase = true)
                        val isRecycled = selectedFormatFilter == "RECYCLE_BIN"
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
                                    if (isRecycled) {
                                        Text(
                                            text = "In Recycle Bin",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFE65100),
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { viewModel.restoreDocument(doc.id) },
                                                modifier = Modifier.height(34.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                                                contentPadding = PaddingValues(horizontal = 10.dp)
                                            ) {
                                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { docToPermanentlyDelete = doc },
                                                modifier = Modifier.height(34.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError),
                                                contentPadding = PaddingValues(horizontal = 10.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Delete Forever", fontSize = 11.sp)
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Status: ${doc.uploadStatus}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MedSuccess,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (doc.type.contains("Prescription", ignoreCase = true)) {
                                                Button(
                                                    onClick = { viewModel.acceptPrescriptionIntoReminders(doc.id) },
                                                    modifier = Modifier.height(34.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                                ) {
                                                    Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Import Rx", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

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
                                                Icon(Icons.Outlined.Delete, contentDescription = "Move to Recycle Bin", tint = MedError, modifier = Modifier.size(16.dp))
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
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary, fontSize = 10.sp)
    }
}
