package com.example.cachedtodo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TaskRepository(private val context: Context) {

    private val taskDao = AppDatabase.getDatabase(context).taskDao()

    private val firestore = Firebase.firestore
    private val tasksCollection = firestore.collection("tasks")
    val allTasks = taskDao.getAllTasks()

    suspend fun addTask(title: String) {
        val task = Task(
            title = title,
            isCompleted = false,
            isSynced = false
        )
        taskDao.insertOrUpdateTask(task)
        Log.d("Repo", "Sarcină adăugată local în Room (backup).")

        syncPendingTasks()
    }

    suspend fun deleteTask(task: Task) {
        val taskToUpdate = task.copy(isDeleted = true, isSynced = false)
        taskDao.insertOrUpdateTask(taskToUpdate)
        Log.d("Repo", "Sarcina ${task.id} marcată pentru ștergere.")

        syncPendingTasks()
    }

    suspend fun syncPendingTasks() {
        if (!isNetworkAvailable()) {
            Log.d("Repo", "Fără net. Sincronizarea (upload) se amână.")
            return
        }

        val unsyncedTasks = taskDao.getUnsyncedTasks()
        if (unsyncedTasks.isEmpty()) {
            Log.d("Repo", "Totul este deja sincronizat (upload).")
            return
        }

        Log.d("Repo", "Începe sincronizarea (upload) a ${unsyncedTasks.size} sarcini...")

        unsyncedTasks.forEach { task ->
            try {
                if (task.isDeleted) {
                    tasksCollection.document(task.id).delete().await()
                    taskDao.deletePermanently(task)
                    Log.d("Repo", "Sarcina ${task.id} a fost ȘTEARSĂ de pe server.")
                } else {
                    val taskToUpload = task.copy(isSynced = true)
                    tasksCollection.document(taskToUpload.id).set(taskToUpload).await()
                    taskDao.markTaskAsSynced(task.id)
                    Log.d("Repo", "Sarcina ${task.id} a fost URCATĂ pe server.")
                }
            } catch (e: Exception) {
                Log.e("Repo", "Eroare la sincronizarea sarcinii ${task.id}", e)
            }
        }
    }

    suspend fun refreshDataFromServer() {
        if (!isNetworkAvailable()) {
            Log.d("Repo", "Fără net. Nu se poate reîmprospăta (download).")
            return
        }

        Log.d("Repo", "Se aduc date de pe Firebase (download)...")
        try {
            val snapshot = tasksCollection.get().await()
            val firebaseTasks = snapshot.toObjects<Task>()

            taskDao.clearAllTasks()

            firebaseTasks.forEach { task ->
                taskDao.insertOrUpdateTask(task.copy(isSynced = true))
            }
            Log.d("Repo", "Room (cache) a fost actualizat cu date de pe server.")

        } catch (e: Exception) {
            Log.e("Repo", "Eroare la descărcarea datelor", e)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}