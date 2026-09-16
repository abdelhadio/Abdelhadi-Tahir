package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MovieDatabase
import com.example.data.MovieRepository
import com.example.data.MovieSite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieRepository

    init {
        val database = MovieDatabase.getInstance(application)
        repository = MovieRepository(database.movieSiteDao())
        viewModelScope.launch {
            repository.ensureDefaultCuratedSites()
        }
    }

    val allSites: StateFlow<List<MovieSite>> = repository.allSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSites: StateFlow<List<MovieSite>> = repository.favoriteSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSites: StateFlow<List<MovieSite>> = repository.recentSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Category Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Filtered sites based on category and search query
    val filteredSites: StateFlow<List<MovieSite>> = combine(
        allSites,
        _searchQuery,
        _selectedCategory
    ) { sites, query, category ->
        sites.filter { site ->
            val matchesCategory = when (category) {
                "All" -> true
                "My Sites" -> !site.isCurated
                "Favorites" -> site.isFavorite
                else -> site.category.equals(category, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                site.title.contains(query, ignoreCase = true) ||
                site.description.contains(query, ignoreCase = true) ||
                site.tags.contains(query, ignoreCase = true) ||
                site.url.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Theater Mode State
    private val _activeTheaterSite = MutableStateFlow<MovieSite?>(null)
    val activeTheaterSite: StateFlow<MovieSite?> = _activeTheaterSite.asStateFlow()

    // Theater Controls
    private val _theaterAdBlockCount = MutableStateFlow(0)
    val theaterAdBlockCount: StateFlow<Int> = _theaterAdBlockCount.asStateFlow()

    private val _isTheaterAmbientMode = MutableStateFlow(false)
    val isTheaterAmbientMode: StateFlow<Boolean> = _isTheaterAmbientMode.asStateFlow()

    private val _isDesktopMode = MutableStateFlow(false)
    val isDesktopMode: StateFlow<Boolean> = _isDesktopMode.asStateFlow()

    // Add / Edit Dialog State
    private val _isAddSiteDialogOpen = MutableStateFlow(false)
    val isAddSiteDialogOpen: StateFlow<Boolean> = _isAddSiteDialogOpen.asStateFlow()

    private val _editingSite = MutableStateFlow<MovieSite?>(null)
    val editingSite: StateFlow<MovieSite?> = _editingSite.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openSiteInTheater(site: MovieSite) {
        _activeTheaterSite.value = site
        _theaterAdBlockCount.value = site.adBlockCount
        _isTheaterAmbientMode.value = false
        _isDesktopMode.value = false
        viewModelScope.launch {
            repository.markVisited(site.id)
        }
    }

    fun closeTheater() {
        val current = _activeTheaterSite.value
        if (current != null) {
            val totalBlocked = _theaterAdBlockCount.value
            val newlyBlocked = totalBlocked - current.adBlockCount
            if (newlyBlocked > 0) {
                viewModelScope.launch {
                    repository.recordBlockedAds(current.id, newlyBlocked)
                }
            }
        }
        _activeTheaterSite.value = null
    }

    fun recordAdBlocked() {
        _theaterAdBlockCount.value += 1
    }

    fun toggleAmbientMode() {
        _isTheaterAmbientMode.value = !_isTheaterAmbientMode.value
    }

    fun toggleDesktopMode() {
        _isDesktopMode.value = !_isDesktopMode.value
    }

    fun openAddSiteDialog(siteToEdit: MovieSite? = null) {
        _editingSite.value = siteToEdit
        _isAddSiteDialogOpen.value = true
    }

    fun closeAddSiteDialog() {
        _isAddSiteDialogOpen.value = false
        _editingSite.value = null
    }

    fun saveSite(
        title: String,
        url: String,
        category: String,
        posterUrl: String,
        description: String,
        tags: String
    ) {
        viewModelScope.launch {
            val editing = _editingSite.value
            if (editing != null) {
                val updated = editing.copy(
                    title = title.trim(),
                    url = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url.trim(),
                    category = category,
                    posterUrl = posterUrl.trim(),
                    description = description.trim(),
                    tags = tags.trim()
                )
                repository.updateSite(updated)
            } else {
                repository.addSite(
                    title = title,
                    url = url,
                    category = category,
                    posterUrl = posterUrl,
                    description = description,
                    tags = tags
                )
            }
            closeAddSiteDialog()
        }
    }

    fun deleteSite(site: MovieSite) {
        viewModelScope.launch {
            repository.deleteSite(site)
            if (_activeTheaterSite.value?.id == site.id) {
                _activeTheaterSite.value = null
            }
        }
    }

    fun toggleFavorite(site: MovieSite) {
        viewModelScope.launch {
            repository.toggleFavorite(site)
            if (_activeTheaterSite.value?.id == site.id) {
                _activeTheaterSite.value = site.copy(isFavorite = !site.isFavorite)
            }
        }
    }
}
