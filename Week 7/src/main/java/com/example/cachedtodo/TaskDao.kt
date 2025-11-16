package com.example.cachedtodo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY title ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<Task>

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :taskId")
    suspend fun markTaskAsSynced(taskId: String)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Delete
    suspend fun deletePermanently(task: Task)
}