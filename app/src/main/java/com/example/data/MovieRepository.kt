package com.example.data

import kotlinx.coroutines.flow.Flow

class MovieRepository(private val dao: MovieSiteDao) {

    val allSites: Flow<List<MovieSite>> = dao.getAllSites()
    val favoriteSites: Flow<List<MovieSite>> = dao.getFavoriteSites()
    val recentSites: Flow<List<MovieSite>> = dao.getRecentSites()

    suspend fun getSiteById(id: Long): MovieSite? = dao.getSiteById(id)

    suspend fun ensureDefaultCuratedSites() {
        val count = dao.getSitesCount()
        if (count == 0) {
            dao.insertAll(MovieDatabase.getInitialCuratedSites())
        }
    }

    suspend fun addSite(
        title: String,
        url: String,
        category: String,
        posterUrl: String = "",
        description: String = "",
        tags: String = "Custom, Movie Stream"
    ): Long {
        val cleanUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val site = MovieSite(
            title = title.trim(),
            url = cleanUrl.trim(),
            category = category,
            posterUrl = posterUrl.trim(),
            description = description.trim(),
            tags = tags.trim(),
            isCurated = false
        )
        return dao.insertSite(site)
    }

    suspend fun updateSite(site: MovieSite) {
        dao.updateSite(site)
    }

    suspend fun deleteSite(site: MovieSite) {
        dao.deleteSite(site)
    }

    suspend fun toggleFavorite(site: MovieSite) {
        dao.setFavorite(site.id, !site.isFavorite)
    }

    suspend fun markVisited(siteId: Long) {
        dao.updateLastVisited(siteId, System.currentTimeMillis())
    }

    suspend fun recordBlockedAds(siteId: Long, count: Int) {
        if (count > 0) {
            dao.incrementAdBlock(siteId, count)
        }
    }
}
