package com.example.ui.components

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
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.MovieSite
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditSiteDialog(
    siteToEdit: MovieSite? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, category: String, posterUrl: String, description: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf(siteToEdit?.title ?: "") }
    var url by remember { mutableStateOf(siteToEdit?.url ?: "") }
    var category by remember { mutableStateOf(siteToEdit?.category ?: "Movies") }
    var posterUrl by remember { mutableStateOf(siteToEdit?.posterUrl ?: "") }
    var description by remember { mutableStateOf(siteToEdit?.description ?: "") }
    var tags by remember { mutableStateOf(siteToEdit?.tags ?: "Movie Portal, Free") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Movies", "TV Shows", "Anime", "Classics", "Docs", "Custom")

    // Curated poster backdrop presets
    val presetPosters = listOf(
        "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80",
        "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&q=80",
        "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=500&q=80",
        "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=500&q=80",
        "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=500&q=80",
        "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&q=80"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("add_edit_site_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2B3345)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CinemaRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (siteToEdit != null) Icons.Default.Movie else Icons.Default.AddLink,
                                contentDescription = null,
                                tint = CinemaRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (siteToEdit != null) "Edit Movie Website" else "Add Movie Website",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Plays like a movie app with Ad-Shield active",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShieldGreen
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ad-block assurance badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = ShieldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Automatic popup blocker, banner filter, and cinema overlay cleaner enabled.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Website Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Movie Website / Portal Name") },
                    placeholder = { Text("e.g. Cinema Hub, Anime Stream, Classic Films") },
                    leadingIcon = {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = CinemaRed)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("site_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaRed,
                        unfocusedBorderColor = Color(0xFF2D3748),
                        focusedLabelColor = CinemaRed,
                        cursorColor = CinemaRed
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Website URL Input
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        errorMessage = null
                    },
                    label = { Text("Website URL / Stream Link") },
                    placeholder = { Text("e.g. https://my-movie-site.com") },
                    leadingIcon = {
                        Icon(Icons.Default.AddLink, contentDescription = null, tint = CinemaGold)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("site_url_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = Color(0xFF2D3748),
                        focusedLabelColor = CinemaGold,
                        cursorColor = CinemaGold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CinemaRed else DarkSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                                .testTag("category_option_$cat")
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Poster Image Selection
                Text(
                    text = "Movie Poster / Thumbnail",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = posterUrl,
                    onValueChange = { posterUrl = it },
                    label = { Text("Image URL (optional)") },
                    placeholder = { Text("Paste image URL or pick preset below") },
                    leadingIcon = {
                        Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("site_poster_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaRed,
                        unfocusedBorderColor = Color(0xFF2D3748)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Poster Presets
                Text(
                    text = "Or choose a cinema artwork preset:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetPosters.forEachIndexed { index, preset ->
                        val isSelected = posterUrl == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) CinemaRed else Color(0xFF2D3748),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { posterUrl = preset }
                                .testTag("poster_preset_$index")
                        ) {
                            AsyncImage(
                                model = preset,
                                contentDescription = "Preset $index",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Note") },
                    placeholder = { Text("e.g. Best HD movies, anime, subtitles") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("site_description_input"),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaRed,
                        unfocusedBorderColor = Color(0xFF2D3748)
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = CinemaRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.testTag("cancel_site_button")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter a name for the movie website."
                                return@Button
                            }
                            if (url.isBlank()) {
                                errorMessage = "Please enter a website URL."
                                return@Button
                            }
                            // Default poster if none provided
                            val finalPoster = if (posterUrl.isBlank()) presetPosters.first() else posterUrl
                            onSave(title, url, category, finalPoster, description, tags)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_site_button")
                    ) {
                        Text(if (siteToEdit != null) "Update Website" else "Add to Cinema Hub")
                    }
                }
            }
        }
    }
}
