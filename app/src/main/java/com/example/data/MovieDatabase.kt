package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [MovieSite::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {

    abstract fun movieSiteDao(): MovieSiteDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getInstance(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "milon_movies.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).movieSiteDao().insertAll(getInitialCuratedSites())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialCuratedSites(): List<MovieSite> {
            return listOf(
                MovieSite(
                    title = "Internet Archive Cinema",
                    url = "https://archive.org/details/feature_films",
                    category = "Classics",
                    posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80",
                    description = "Watch thousands of free feature films, film noir classics, silent masterpieces, and golden era cinema.",
                    isFavorite = true,
                    isPinned = true,
                    isCurated = true,
                    tags = "Public Domain, 1080p, Free"
                ),
                MovieSite(
                    title = "Blender Open Cinema",
                    url = "https://durian.blender.org/",
                    category = "Movies",
                    posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&q=80",
                    description = "Open source sci-fi and CGI cinema showcase including Tears of Steel 4K, Sintel, and Cosmos Laundromat.",
                    isFavorite = false,
                    isPinned = true,
                    isCurated = true,
                    tags = "4K, Sci-Fi, VFX"
                ),
                MovieSite(
                    title = "Public Domain Movies",
                    url = "https://publicdomainmovies.info/",
                    category = "Movies",
                    posterUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=500&q=80",
                    description = "Huge archive of legal free streaming movies across comedy, horror, mystery, and westerns.",
                    isFavorite = false,
                    isPinned = false,
                    isCurated = true,
                    tags = "Classics, Vintage, Free"
                ),
                MovieSite(
                    title = "NASA TV & Space Cinema",
                    url = "https://www.nasa.gov/live",
                    category = "Docs",
                    posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&q=80",
                    description = "Live rocket launches, space station views, and astronomical science documentaries.",
                    isFavorite = false,
                    isPinned = false,
                    isCurated = true,
                    tags = "Documentary, Live, Space"
                ),
                MovieSite(
                    title = "Plex Free Movies",
                    url = "https://app.plex.tv/desktop/#!/",
                    category = "Movies",
                    posterUrl = "https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=500&q=80",
                    description = "Stream thousands of legal free movies and TV series across all modern genres.",
                    isFavorite = false,
                    isPinned = false,
                    isCurated = true,
                    tags = "Studio Films, Free Stream"
                ),
                MovieSite(
                    title = "Pluto TV Web",
                    url = "https://pluto.tv/",
                    category = "TV Shows",
                    posterUrl = "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=500&q=80",
                    description = "Hundreds of live channels, crime dramas, comedies, and binge-worthy movie marathons.",
                    isFavorite = false,
                    isPinned = false,
                    isCurated = true,
                    tags = "Live TV, Series, Free"
                )
            )
        }
    }
}
