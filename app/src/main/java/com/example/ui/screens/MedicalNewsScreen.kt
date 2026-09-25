package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AnnouncementEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

data class MedicalArticle(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val content: String,
    val author: String,
    val date: String,
    val readTime: String,
    val initialLikes: Int = 12,
    val tags: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalNewsScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val announcements by viewModel.announcements.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedArticle by remember { mutableStateOf<MedicalArticle?>(null) }
    var likedArticleIds by remember { mutableStateOf(setOf<String>()) }
    var savedArticleIds by remember { mutableStateOf(setOf<String>()) }
    var articleCommentMap by remember { mutableStateOf(mapOf<String, List<String>>()) }

    val categories = listOf("All", "Medications", "Preventive Care", "Cardiology", "Nutrition", "Clinical Broadcasts")

    // Curated articles + Dynamic integration with Admin Announcements
    val defaultArticles = remember {
        listOf(
            MedicalArticle(
                id = "art_1",
                title = "Managing Blood Pressure: The Role of Consistent Timing",
                category = "Cardiology",
                summary = "Why taking ACE inhibitors or Beta-blockers at the same exact time daily improves cardiovascular outcomes.",
                content = "Clinical trials confirm that circadian variations in blood pressure are best stabilized when anti-hypertensive therapies are ingested at strict 24-hour intervals. Skipping or delaying morning doses by even 3 hours can induce compensatory arterial pressure spikes. Always log doses immediately in MedTime to maintain your 7-day adherence above 85%.",
                author = "Dr. Sarah Jenkins, MD (Cardiology)",
                date = "Sep 20, 2026",
                readTime = "3 min read",
                initialLikes = 34,
                tags = listOf("Cardio", "Hypertension", "Dosing")
            ),
            MedicalArticle(
                id = "art_2",
                title = "Antibiotic Resistance & Why Completing the Course Matters",
                category = "Medications",
                summary = "Even when symptoms subside, continuing antibiotic regimens to completion prevents bacterial recurrence.",
                content = "Stopping antibacterial therapy early when you begin feeling better leaves surviving bacteria exposed to sub-lethal concentrations of the drug, fostering rapid mutation and resistance. MedTime automated pill inventory tracking ensures you never run short before the full course is finished.",
                author = "Clinical Pharmacology Board",
                date = "Sep 18, 2026",
                readTime = "4 min read",
                initialLikes = 48,
                tags = listOf("Antibiotics", "Safety", "Prescriptions")
            ),
            MedicalArticle(
                id = "art_3",
                title = "Food-Drug Interactions: What to Avoid with Common Pills",
                category = "Nutrition",
                summary = "Grapefruit, dairy, and high-potassium diets can profoundly alter drug absorption and liver metabolism.",
                content = "Calcium in dairy can chelate with fluoroquinolones and tetracyclines, reducing systemic absorption by up to 50%. Meanwhile, grapefruit furanocoumarins inhibit CYP3A4 enzymes, causing statin levels to soar. Check each medication's instructions in your MedTime dashboard before mealtime.",
                author = "Dietary Medicine Institute",
                date = "Sep 15, 2026",
                readTime = "5 min read",
                initialLikes = 62,
                tags = listOf("Interactions", "Diet", "Safety")
            ),
            MedicalArticle(
                id = "art_4",
                title = "Seasonal Influenza & Pneumococcal Vaccines: 2026 Guidelines",
                category = "Preventive Care",
                summary = "Updated immunization recommendations for high-risk patients, seniors, and immunocompromised individuals.",
                content = "Annual vaccination significantly lowers emergency hospitalizations. Consult your connected MedTime doctor to schedule your preventative visit this season and have vaccination certificates digitized directly to your Medical Documents tab.",
                author = "Public Health Directorate",
                date = "Sep 12, 2026",
                readTime = "2 min read",
                initialLikes = 29,
                tags = listOf("Vaccines", "Prevention", "Flu")
            )
        )
    }

    // Combine default articles with real announcements from Admin
    val dynamicArticles = remember(announcements, defaultArticles) {
        val announcementArticles = announcements.map { ann ->
            MedicalArticle(
                id = "ann_${ann.id}",
                title = "[Broadcast] ${ann.title}",
                category = "Clinical Broadcasts",
                summary = ann.content,
                content = "${ann.content}\n\nTarget Audience: ${ann.targetRole}\nBroadcast Priority: ${ann.priority}\nIssued by Hospital Admin.",
                author = ann.authorName.ifBlank { "Hospital Administrator" },
                date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(ann.timestamp)),
                readTime = "1 min read",
                initialLikes = 15,
                tags = listOf("Official", ann.priority.lowercase())
            )
        }
        announcementArticles + defaultArticles
    }

    val filteredArticles = remember(searchQuery, selectedCategory, dynamicArticles) {
        dynamicArticles.filter { article ->
            val matchesCategory = selectedCategory == "All" || article.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    article.title.contains(searchQuery, ignoreCase = true) ||
                    article.summary.contains(searchQuery, ignoreCase = true) ||
                    article.author.contains(searchQuery, ignoreCase = true) ||
                    article.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    // Full Article Reader Dialog
    if (selectedArticle != null) {
        val art = selectedArticle!!
        val isLiked = likedArticleIds.contains(art.id)
        val isSaved = savedArticleIds.contains(art.id)
        val comments = articleCommentMap[art.id] ?: emptyList()
        var newCommentText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { selectedArticle = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(20.dp)),
                color = MedSurface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedBlueLight
                        ) {
                            Text(
                                text = art.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedBluePrimary
                            )
                        }

                        IconButton(onClick = { selectedArticle = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = art.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "By ${art.author}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MedBluePrimary
                                )
                                Text("•", color = MedTextSecondary)
                                Text(text = art.date, style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                Text("•", color = MedTextSecondary)
                                Text(text = art.readTime, style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MedDivider)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Article Body
                            Text(
                                text = art.content,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MedTextPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tags
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                art.tags.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MedSurfaceVariant
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MedTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MedDivider)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Comments Section
                            Text(
                                text = "Patient Discussions (${comments.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                        }

                        if (comments.isEmpty()) {
                            item {
                                Text(
                                    text = "No questions posted yet. Ask a question or share your experience below!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                        } else {
                            items(comments) { comment ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MedSurfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                                            Text("Patient Community", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(comment, style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                                    }
                                }
                            }
                        }

                        item {
                            // Add comment box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    placeholder = { Text("Write a comment or query...", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        if (newCommentText.isNotBlank()) {
                                            val currentList = articleCommentMap[art.id] ?: emptyList()
                                            articleCommentMap = articleCommentMap + (art.id to (currentList + newCommentText.trim()))
                                            newCommentText = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                    enabled = newCommentText.isNotBlank()
                                ) {
                                    Text("Post")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MedDivider)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Toolbar (Like, Save, Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Like button
                            Button(
                                onClick = {
                                    likedArticleIds = if (isLiked) likedArticleIds - art.id else likedArticleIds + art.id
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLiked) MedError.copy(alpha = 0.15f) else MedSurfaceVariant,
                                    contentColor = if (isLiked) MedError else MedTextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val totalLikes = art.initialLikes + if (isLiked) 1 else 0
                                Text("$totalLikes Likes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Save button
                            IconButton(
                                onClick = {
                                    savedArticleIds = if (isSaved) savedArticleIds - art.id else savedArticleIds + art.id
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Save Article",
                                    tint = if (isSaved) MedBluePrimary else MedTextSecondary
                                )
                            }
                        }

                        // Share button
                        IconButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, art.title)
                                    putExtra(Intent.EXTRA_TEXT, "Read this medical health update on MedTime:\n\n${art.title}\n\n${art.summary}")
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Medical Article")
                                context.startActivity(shareIntent)
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = MedBluePrimary)
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MedBluePrimary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (onNavigateBack != null) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Newspaper, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Medical News & Tips",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Verified Clinical Insights & Health Broadcasts",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search medical topics, medications, guidelines...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedBluePrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("news_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MedSurface,
                    unfocusedContainerColor = MedSurface,
                    focusedBorderColor = MedBluePrimary,
                    unfocusedBorderColor = MedBorder
                )
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedBluePrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }

        // Article Cards List
        if (filteredArticles.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MedSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MedTextSecondary, modifier = Modifier.size(40.dp))
                        Text("No articles found", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                        Text("Try searching for a different health condition or topic.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                }
            }
        } else {
            items(filteredArticles, key = { it.id }) { article ->
                val isLiked = likedArticleIds.contains(article.id)
                val isSaved = savedArticleIds.contains(article.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedArticle = article }
                        .testTag("news_card_${article.id}"),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (article.category == "Clinical Broadcasts") MedWarningLight else MedBlueLight
                            ) {
                                Text(
                                    text = article.category,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (article.category == "Clinical Broadcasts") MedWarning else MedBluePrimary
                                )
                            }

                            Text(
                                text = "${article.date} • ${article.readTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "By ${article.author}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MedBluePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        likedArticleIds = if (isLiked) likedArticleIds - article.id else likedArticleIds + article.id
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLiked) MedError else MedTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        savedArticleIds = if (isSaved) savedArticleIds - article.id else savedArticleIds + article.id
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Save",
                                        tint = if (isSaved) MedBluePrimary else MedTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Button(
                                    onClick = { selectedArticle = article },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedBlueLight, contentColor = MedBluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Read", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
