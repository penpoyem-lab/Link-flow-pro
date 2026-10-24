package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkFlowDao {
    @Query("SELECT * FROM download_tasks ORDER BY createdAt DESC")
    fun observeAllDownloads(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE jobId = :jobId LIMIT 1")
    suspend fun getDownloadById(jobId: String): DownloadTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(task: DownloadTaskEntity)

    @Query("UPDATE download_tasks SET fileName = :newFileName WHERE jobId = :jobId")
    suspend fun renameDownloadFile(jobId: String, newFileName: String)

    @Query("DELETE FROM download_tasks WHERE jobId = :jobId")
    suspend fun deleteDownloadById(jobId: String)

    @Query("DELETE FROM download_tasks WHERE state = 'COMPLETED'")
    suspend fun clearCompletedDownloads()

    @Query("DELETE FROM download_tasks WHERE state = 'FAILED'")
    suspend fun clearFailedDownloads()

    @Query("DELETE FROM download_tasks")
    suspend fun clearAll()
}
