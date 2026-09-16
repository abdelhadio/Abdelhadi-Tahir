package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MovieSite
import com.example.ui.components.AddEditSiteDialog
import com.example.ui.components.CinemaCard
import com.example.ui.components.FeaturedShowcase
import com.example.ui.theater.TheaterPlayerView
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CinemaHomeScreen(
    viewModel: MovieViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheaterSite by viewModel.activeTheaterSite.collectAsStateWithLifecycle()
    val isAmbientMode by viewModel.isTheaterAmbientMode.collectAsStateWithLifecycle()
    val isDesktopMode by viewModel.isDesktopMode.collectAsStateWithLifecycle()
    val isAddDialogOpen by viewModel.isAddSiteDialogOpen.collectAsStateWithLifecycle()
    val editingSite by viewModel.editingSite.collectAsStateWithLifecycle()

    val filteredSites by viewModel.filteredSites.collectAsStateWithLifecycle()
    val allSites by viewModel.allSites.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val myCustomSites = allSites.filter { !it.isCurated }
    val featuredSite = allSites.firstOrNull { it.isPinned } ?: allSites.firstOrNull()

    val categories = listOf("All", "My Sites", "Movies", "TV Shows", "Anime", "Classics", "Docs", "Favorites")

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        if (activeTheaterSite != null) {
            // Theater Player Mode: plays the movie site ad-free like a native movie app
            TheaterPlayerView(
                site = activeTheaterSite!!,
                onClose = { viewModel.closeTheater() },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onAdBlocked = { viewModel.recordAdBlocked() },
                isAmbientMode = isAmbientMode,
                onToggleAmbientMode = { viewModel.toggleAmbientMode() },
                isDesktopMode = isDesktopMode,
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Cinema Home Catalog
            Scaffold(
                containerColor = DarkBackground,
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openAddSiteDialog(null) },
                        containerColor = CinemaRed,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Add Movie Site", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("add_site_fab")
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .testTag("cinema_home_list"),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    // Header Bar with App Branding & Quick Add
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface)
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Custom Cinema App Logo
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_milon_logo),
                                        contentDescription = "Milon Logo",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "MILON",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = CinemaRed,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "MOVIES",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                        Text(
                                            text = "Ad-Free Cinema Hub",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ShieldGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = CinemaRed.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { viewModel.openAddSiteDialog(null) }
                                        .testTag("top_add_site_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Website",
                                            tint = CinemaRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Add Site",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CinemaRed
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Search bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search movie titles, genres, websites...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = TextSecondary
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.setSearchQuery("") },
                                            modifier = Modifier.testTag("clear_search_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = TextSecondary
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cinema_search_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant,
                                    focusedBorderColor = CinemaRed,
                                    unfocusedBorderColor = Color(0xFF2B3345),
                                    cursorColor = CinemaRed
                                )
                            )
                        }
                    }

                    // Categories Horizontal Chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CinemaRed else DarkSurfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setSelectedCategory(cat) }
                                        .testTag("category_chip_$cat")
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Featured Hero Section (only when not actively searching)
                    if (searchQuery.isEmpty() && (selectedCategory == "All" || selectedCategory == "Movies")) {
                        item {
                            FeaturedShowcase(
                                featuredSite = featuredSite,
                                onOpen = { viewModel.openSiteInTheater(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // "My Added Movie Portals" horizontal shelf
                    if (searchQuery.isEmpty() && selectedCategory != "Classics" && selectedCategory != "Docs") {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "My Movie Websites",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = CinemaRed.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${myCustomSites.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = CinemaRed,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "+ Add New",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CinemaGold,
                                        modifier = Modifier
                                            .clickable { viewModel.openAddSiteDialog(null) }
                                            .padding(4.dp)
                                            .testTag("shelf_add_new_button")
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (myCustomSites.isEmpty()) {
                                    // Empty state card encouraging adding a movie website
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { viewModel.openAddSiteDialog(null) }
                                            .testTag("empty_sites_banner"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2B3345)))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(CircleShape)
                                                    .background(CinemaRed.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = CinemaRed,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    text = "Add your favorite movie websites",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Watch them inside this movie app with no popups or ads.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(myCustomSites, key = { it.id }) { site ->
                                            Box(modifier = Modifier.width(220.dp)) {
                                                CinemaCard(
                                                    site = site,
                                                    onOpen = { viewModel.openSiteInTheater(site) },
                                                    onFavoriteToggle = { viewModel.toggleFavorite(site) },
                                                    onEdit = { viewModel.openAddSiteDialog(site) },
                                                    onDelete = { viewModel.deleteSite(site) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Main Stream Catalog Section Header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = if (selectedCategory == "All") "Streaming Portals & Cinema Catalog" else "$selectedCategory Cinema",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Protected by Milon Ad-Shield & Cinema Theater View",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShieldGreen
                            )
                        }
                    }

                    // Catalog items
                    if (filteredSites.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No movie websites found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Try adjusting your search or add a new website.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    } else {
                        items(filteredSites, key = { it.id }) { site ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                CinemaCard(
                                    site = site,
                                    onOpen = { viewModel.openSiteInTheater(site) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(site) },
                                    onEdit = { viewModel.openAddSiteDialog(site) },
                                    onDelete = { viewModel.deleteSite(site) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Movie Website Dialog
        if (isAddDialogOpen) {
            AddEditSiteDialog(
                siteToEdit = editingSite,
                onDismiss = { viewModel.closeAddSiteDialog() },
                onSave = { title, url, category, posterUrl, description, tags ->
                    viewModel.saveSite(title, url, category, posterUrl, description, tags)
                }
            )
        }
    }
}
