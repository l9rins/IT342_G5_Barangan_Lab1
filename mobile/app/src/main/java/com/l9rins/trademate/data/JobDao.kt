package com.l9rins.trademate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY id DESC")
    fun getAllJobs(): Flow<List<Job>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job)

    @Delete
    suspend fun deleteJob(job: Job)

    // --- ANALYTICS QUERIES ---

    // 1. Total Pending Jobs
    @Query("SELECT COUNT(*) FROM jobs WHERE status = 'Pending'")
    fun getPendingCount(): Flow<Int>

    // 2. Total Active Jobs
    @Query("SELECT COUNT(*) FROM jobs WHERE status = 'Active'")
    fun getActiveCount(): Flow<Int>

    // 3. Total Completed Jobs
    @Query("SELECT COUNT(*) FROM jobs WHERE status = 'Paid'")
    fun getCompletedCount(): Flow<Int>

    // 4. Total Revenue (Sum of 'Paid' jobs)
    @Query("SELECT SUM(price) FROM jobs WHERE status = 'Paid'")
    fun getTotalRevenue(): Flow<Double?>
}