package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_sites")
data class MovieSite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val category: String = "Movies",
    val posterUrl: String = "",
    val description: String = "",
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val lastVisitedAt: Long = 0L,
    val adBlockCount: Int = 0,
    val isCurated: Boolean = false,
    val tags: String = "Free, HD",
    val customUserAgent: String = ""
)
