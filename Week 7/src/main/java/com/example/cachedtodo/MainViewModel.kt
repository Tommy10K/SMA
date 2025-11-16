package com.example.cachedtodo

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    val allTasks: LiveData<List<Task>> = repository.allTasks.asLiveData()

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("ViewModel", "Rețea detectată! Începe sincronizarea.")
            viewModelScope.launch {
                repository.syncPendingTasks()
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.refreshDataFromServer()
        }

        viewModelScope.launch {
            repository.syncPendingTasks()
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    fun addTask(title: String) {
        if (title.isBlank()) return

        viewModelScope.launch {
            repository.addTask(title)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            repository.refreshDataFromServer()
            repository.syncPendingTasks()
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}