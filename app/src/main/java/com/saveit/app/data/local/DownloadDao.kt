package com.saveit.app.data.local

import androidx.room.*
import com.saveit.app.data.model.DownloadItem
import com.saveit.app.data.model.DownloadStatus
import com.saveit.app.data.model.Platform
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE platform = :platform ORDER BY timestamp DESC")
    fun getDownloadsByPlatform(platform: Platform): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY timestamp DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: Long): DownloadItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItem): Long

    @Update
    suspend fun updateDownload(item: DownloadItem)

    @Delete
    suspend fun deleteDownload(item: DownloadItem)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Long)

    @Query("UPDATE downloads SET status = :status, progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: Long, status: DownloadStatus, progress: Int)

    @Query("UPDATE downloads SET status = :status, filePath = :filePath, fileSize = :fileSize WHERE id = :id")
    suspend fun updateCompleted(id: Long, status: DownloadStatus, filePath: String, fileSize: Long)

    @Query("UPDATE downloads SET status = :status, errorMessage = :error WHERE id = :id")
    suspend fun updateFailed(id: Long, status: DownloadStatus, error: String)
}
