package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieSiteDao {

    @Query("SELECT * FROM movie_sites ORDER BY isPinned DESC, lastVisitedAt DESC, id DESC")
    fun getAllSites(): Flow<List<MovieSite>>

    @Query("SELECT * FROM movie_sites WHERE isFavorite = 1 ORDER BY lastVisitedAt DESC")
    fun getFavoriteSites(): Flow<List<MovieSite>>

    @Query("SELECT * FROM movie_sites WHERE lastVisitedAt > 0 ORDER BY lastVisitedAt DESC LIMIT 10")
    fun getRecentSites(): Flow<List<MovieSite>>

    @Query("SELECT * FROM movie_sites WHERE id = :id LIMIT 1")
    suspend fun getSiteById(id: Long): MovieSite?

    @Query("SELECT COUNT(*) FROM movie_sites")
    suspend fun getSitesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: MovieSite): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sites: List<MovieSite>)

    @Update
    suspend fun updateSite(site: MovieSite)

    @Delete
    suspend fun deleteSite(site: MovieSite)

    @Query("DELETE FROM movie_sites WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE movie_sites SET lastVisitedAt = :timestamp WHERE id = :id")
    suspend fun updateLastVisited(id: Long, timestamp: Long)

    @Query("UPDATE movie_sites SET adBlockCount = adBlockCount + :increment WHERE id = :id")
    suspend fun incrementAdBlock(id: Long, increment: Int)

    @Query("UPDATE movie_sites SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
